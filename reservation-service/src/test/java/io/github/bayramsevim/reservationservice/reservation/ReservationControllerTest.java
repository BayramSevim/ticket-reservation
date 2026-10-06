package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.config.SecurityConfig;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(ReservationController.class)
@Import(SecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ReservationService reservationService;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    void holdWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                { "seatId": 1 }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void holdWithTokenReturns201() throws Exception {
        when(reservationService.hold(42L, 1L)).thenReturn(new ReservationResponse(
                1L,
                ReservationStatus.HELD,
                "11-A",
                "The Phantom of the Opera",
                Instant.parse("2023-01-01T00:00:00Z"),
                null,
                null));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(token -> token.subject("42")))
                        .content("""
                                { "seatId": 1 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/1"));
    }

    @Test
    void holdWithoutSeatIdReturns400() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .with(jwt().jwt(token -> token.subject("42")))
                        .content("""
                                {}
                                """))
                .andExpect(status().isBadRequest());

        verify(reservationService, never()).hold(any(), any());
    }
}