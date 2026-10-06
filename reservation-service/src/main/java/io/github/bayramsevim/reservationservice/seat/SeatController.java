package io.github.bayramsevim.reservationservice.seat;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/shows/{showId}/seats")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(seatService.getSeats(showId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatResponse> addSeats(@PathVariable Long showId,
                                                 @Valid @RequestBody CreateSeatRequest request) {
        SeatResponse response = seatService.addSeats(showId, request);
        return ResponseEntity.created(URI.create("/shows/" + showId + "/seats/" + response.id()))
                .body(response);
    }
}