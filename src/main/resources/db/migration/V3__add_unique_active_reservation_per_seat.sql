UPDATE reservations
SET status = 'EXPIRED'
WHERE status = 'HELD'
  AND expires_at < now();

CREATE UNIQUE INDEX uq_active_reservation_per_seat
    ON reservations (seat_id)
    WHERE status IN ('HELD', 'CONFIRMED');