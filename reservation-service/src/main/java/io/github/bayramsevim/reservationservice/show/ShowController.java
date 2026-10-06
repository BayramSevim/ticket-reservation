package io.github.bayramsevim.reservationservice.show;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowResponse> create(@RequestBody @Valid CreateShowRequest request) {
        ShowResponse showResponse = showService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(showResponse);
    }

    @GetMapping
    public ResponseEntity<List<ShowResponse>> getShows() {
        return ResponseEntity.ok(showService.getShows());
    }
}
