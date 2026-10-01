package io.github.bayramsevim.reservationservice.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable Long id) {
        return reservationService.getReservation(id);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> hold(@RequestBody HoldReservationRequest request) {
        ReservationResponse response = reservationService.hold(request.userId(), request.seatId());
        return ResponseEntity
                .created(URI.create("/reservations/" + response.id()))
                .body(response);
    }
}