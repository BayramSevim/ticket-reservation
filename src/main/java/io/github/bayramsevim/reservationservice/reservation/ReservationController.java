package io.github.bayramsevim.reservationservice.reservation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService ) {
        this.reservationService = reservationService;

    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable Long id ,@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return reservationService.getReservation(id, userId);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> hold(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody HoldReservationRequest request) {
        Long userId = Long.valueOf(jwt.getSubject());
        ReservationResponse response = reservationService.hold(userId, request.seatId());
        return ResponseEntity
                .created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponse> confirm(@PathVariable Long id,
                                                       @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        ReservationResponse response = reservationService.confirm(id, userId);
        return ResponseEntity
                .ok()
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long id,
                                                      @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        ReservationResponse response = reservationService.cancel(id, userId);
        return ResponseEntity
                .ok()
                .body(response);
    }
}