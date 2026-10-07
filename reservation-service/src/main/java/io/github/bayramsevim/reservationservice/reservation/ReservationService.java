package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.common.RateLimitService;
import io.github.bayramsevim.reservationservice.exception.ForbiddenOperationException;
import io.github.bayramsevim.reservationservice.exception.InvalidCredentialsException;
import io.github.bayramsevim.reservationservice.exception.NotFoundException;
import io.github.bayramsevim.reservationservice.exception.RateLimitExceededException;
import io.github.bayramsevim.reservationservice.seat.Seat;
import io.github.bayramsevim.reservationservice.seat.SeatRepository;
import io.github.bayramsevim.reservationservice.user.User;
import io.github.bayramsevim.reservationservice.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final SeatHoldService seatHoldService;
    private final RateLimitService rateLimitService;
    private final ReservationEventPublisher reservationEventPublisher;

    public ReservationService(ReservationRepository reservationRepository,
                              SeatRepository seatRepository,
                              UserRepository userRepository,
                              Clock clock, SeatHoldService seatHoldService, RateLimitService rateLimitService, ReservationEventPublisher reservationEventPublisher) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.clock = clock;
        this.seatHoldService = seatHoldService;
        this.rateLimitService = rateLimitService;
        this.reservationEventPublisher = reservationEventPublisher;
    }

    @Transactional
    public ReservationResponse hold(Long userId, Long seatId){
        if (!rateLimitService.tryAcquire(userId)) {
            throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
        }
        boolean seatHeld = seatHoldService.tryHold(seatId, userId);
        if (!seatHeld) {
            throw new IllegalStateException("Koltuk zaten rezerve edilmiş veya tutulmuş");
        }
       try{
           User user = userRepository.findById(userId).orElseThrow(() -> new InvalidCredentialsException("User not authorized"));
           Seat seat = seatRepository.findByIdForUpdate(seatId).orElseThrow(() -> new NotFoundException("Seat not found"));

           boolean seatTaken = reservationRepository.existsBySeatIdAndStatusIn(seatId, List.of(ReservationStatus.HELD, ReservationStatus.CONFIRMED));
           if (seatTaken)
               throw new IllegalStateException("Koltuk zaten rezerve edilmiş veya tutulmuş");

           Instant now = Instant.now(clock);
           Instant expiresAt = now.plus(Duration.ofMinutes(10));

           Reservation reservation = new Reservation(seat,user,expiresAt);

           try{
               return ReservationResponse.from(reservationRepository.save(reservation));
           }
           catch (DataIntegrityViolationException e){
               throw new IllegalStateException("Koltuk zaten rezerve edilmiş veya tutulmuş");
           }
       }
       catch (Exception e){
              seatHoldService.release(seatId);
              throw e;
       }
    }

    @Transactional
    public ReservationResponse confirm(Long reservationId,Long userId){
        Reservation reservation = findOwned(reservationId, userId);
        reservation.confirm(Instant.now(clock));
        reservationEventPublisher.publishConfirmed(reservation);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancel(Long reservationId,Long userId){
        Reservation reservation = findOwned(reservationId, userId);
        reservation.cancel(Instant.now(clock));
        seatHoldService.release(reservation.getSeat().getId());
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long reservationId,Long userId) {
        Reservation reservation =  findOwned(reservationId, userId);
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream().map(ReservationResponse::from).toList();
    }

    @Transactional
    public int expireOverdue() {
        Instant now = Instant.now(clock);
        int expiredCount = 0;
        List<Reservation> overdueReservations = reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.HELD, now);
        for (Reservation reservation : overdueReservations) {
            reservation.expire(now);
            seatHoldService.release(reservation.getSeat().getId());
            expiredCount++;
        }
        return expiredCount;
    }

    private Reservation findOwned(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new NotFoundException("Reservation not found"));
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("Kullanıcının bu işleme yetkisi yok");
        }
        return reservation;
    }
}
