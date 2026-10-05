# Ticket Reservation — reservation-service

The reservation service of a ticket reservation system. Users list shows and seats, hold a seat for 10 minutes, then confirm or cancel before the hold expires.

**Tech stack:** Java 21, Spring Boot 4.1, Spring Data JPA (Hibernate), PostgreSQL 17, Flyway, Spring Security (JWT), Redis 7, JUnit 5, Mockito.

## Running

```bash
docker compose up -d      # PostgreSQL (5433) and Redis (6379)
./mvnw spring-boot:run    # application at http://localhost:8080
./mvnw test               # tests (PostgreSQL and Redis must be running)
```

The database schema is created by Flyway on startup (`src/main/resources/db/migration`). Hibernate never changes the schema, it only validates it (`ddl-auto: validate`).

## Endpoints

| Method | Path | Who | Description |
|---|---|---|---|
| POST | `/auth/register` | Anyone | Register. 201 |
| POST | `/auth/login` | Anyone | Returns access token + refresh token |
| POST | `/auth/refresh` | Anyone | New token pair from a refresh token |
| POST | `/auth/logout` | Anyone | Revokes the refresh token. 204 |
| GET | `/shows` | Anyone | List shows |
| POST | `/shows` | ADMIN | Create a show. 201 |
| GET | `/shows/{showId}/seats` | Anyone | Seats of a show |
| POST | `/shows/{showId}/seats` | ADMIN | Add a seat. 201 |
| POST | `/reservations` | Authenticated | Hold a seat for 10 minutes. 201 + `Location` |
| GET | `/reservations/me` | Authenticated | Own reservations |
| GET | `/reservations/{id}` | Owner | Single reservation |
| POST | `/reservations/{id}/confirm` | Owner | Confirm |
| DELETE | `/reservations/{id}` | Owner | Cancel |

Errors are returned as RFC 9457 `ProblemDetail`: 400 (validation), 401 (missing/invalid credentials), 403 (not allowed), 404 (not found), 409 (state conflict).

## Reservation states

```
HELD ──confirm──> CONFIRMED ──cancel──> CANCELLED
  │ └──cancel──> CANCELLED
  └──expire───> EXPIRED
```

The rules live inside the `Reservation` entity (no setters, behavior methods instead). Time is passed in as an `Instant`; the `Clock` lives in the service layer, so tests can use a fixed clock.

## Design decisions

### Persisting changes with dirty checking instead of `save()`
**Decision:** `confirm` and `cancel` are `@Transactional`. I load the reservation and call the entity method; I do not call `save()`.
**Why:** An entity returned by `findById` is managed. When the transaction ends, Hibernate compares the entity's initial state with its current state and sends an `UPDATE` if they differ. Without `@Transactional` the change would stay in memory and be lost without any error.
**Alternative:** Calling `save()` after every change. Easy to forget, and it does not group several steps into one transaction.

### Open Session in View disabled, DTO mapping in the service
**Decision:** `spring.jpa.open-in-view: false`. Services return DTOs (`record`), not entities.
**Why:** `seat` and `show` are LAZY. The session closes when the service method returns, so accessing them in the controller threw `LazyInitializationException`. I moved the mapping inside the transaction. Keeping the setting off surfaces this error early and stops the database connection from staying open for the whole request.
**Alternative:** Leaving it enabled. No error, but queries run unnoticed in the controller.

### N+1 problem and `@EntityGraph`
**Decision:** `@EntityGraph(attributePaths = {"seat", "seat.show"})` on `ReservationRepository.findByUserId`.
**Why:** Listing a user's reservations loaded the seat and the show with a separate query per reservation (21 queries for 10 reservations). With `@EntityGraph` everything is fetched in a single query using joins.
**Alternative:** `JOIN FETCH` in a `@Query`. Same result, but the query has to be written by hand.

### Ownership check in the service layer
**Decision:** The user id from the token is compared with the owner of the reservation (`findOwned`). On mismatch the response is 403.
**Why:** Without this check a user could read or cancel someone else's reservation just by changing the id in the URL (IDOR). The check lives in one method, so `confirm`, `cancel` and `getReservation` all go through it.
**Alternative:** Returning 404 instead of 403, which also hides the fact that the record exists.

### Access token + refresh token in Redis
**Decision:** The access token is a 15-minute JWT and is not stored on the server. The refresh token is a random UUID stored in Redis with a 7-day TTL and replaced on every use (rotation).
**Why:** A JWT is verified on every request without a database lookup, but it cannot be revoked, so it is short-lived. The refresh token saves the user from logging in every 15 minutes and can be revoked by deleting it from Redis on logout or when an account is compromised. Read and delete happen in one command (`GETDEL`), so the same token cannot be used twice.
**Alternative:** A single long-lived JWT. Simpler, but if stolen it cannot be stopped until it expires.

### Uniqueness enforced by the database, mapped to 409
**Decision:** The same seat label in the same show is prevented by a `UNIQUE (show_id, label)` constraint. `DataIntegrityViolationException` is mapped to 409 with a fixed message.
**Why:** A "check if it exists, then insert" in code would let two concurrent requests both insert; the constraint prevents that for certain. The exception message contains table and column names, so it is not sent to the client.
**Alternative:** Checking only in code. Not reliable under concurrent requests.

## Concurrency: preventing double booking

The core rule of the system: **a seat can have at most one active reservation.** The naive implementation ("check if the seat is free, then insert") breaks under concurrent requests, because the check and the insert are two separate steps (check-then-act). `@Transactional` does not help here: each request runs in its own transaction and, under PostgreSQL's default `READ COMMITTED` isolation, cannot see the other's uncommitted insert.

**How it was proven.** An integration test starts 50 threads that hold the same seat at the same time (`whenFiftyUsersHoldSameSeatConcurrently_onlyOneSucceeds`). With the naive code it failed with `expected: <1> but was: <10>`: ten reservations for one seat. The number 10 is the default HikariCP pool size, i.e. how many transactions could run the check simultaneously.

Three tools are used, each for the problem it fits:

| Problem | Tool | Where | Cost |
|---|---|---|---|
| Two active reservations for the same seat | Partial unique index | `V3` migration, `uq_active_reservation_per_seat` | Losers find out through a failed `INSERT` |
| Wasted inserts under heavy contention | Pessimistic lock on the seat row | `SeatRepository.findByIdForUpdate`, `@Lock(PESSIMISTIC_WRITE)` | Waiting requests hold a database connection |
| A stale copy overwriting a newer state (lost update) | Optimistic lock | `@Version` on `Reservation`, `V4` migration | The losing request gets 409 and must retry |

### Partial unique index: the guarantee
**Decision:** `CREATE UNIQUE INDEX ... ON reservations (seat_id) WHERE status IN ('HELD', 'CONFIRMED')`.
**Why:** A plain `UNIQUE (seat_id)` would make a seat unsellable forever after its first cancellation, because cancelled and expired rows are kept for history. The partial index applies uniqueness only to active rows. The check and the write happen atomically inside the database, so no code path can bypass it.
**Note:** The index could not be created at first because existing rows already violated the rule. The migration first marks stale `HELD` rows as `EXPIRED`, then creates the index. A migration has to handle existing data, not only the schema.

### Pessimistic lock: queueing instead of failing
**Decision:** `hold` reads the seat with `SELECT ... FOR NO KEY UPDATE`. The lock is held until the transaction commits, so the check and the insert both happen while the seat row is locked.
**Why:** With the index alone, 10 transactions attempted an insert and 9 were rejected. With the lock, requests queue on the seat row: in the same test only 1 insert is attempted and no constraint violation occurs.
**Why both:** The lock only protects code that asks for it. A future code path that forgets to lock would still be stopped by the index. The index guarantees correctness; the lock makes conflicts cheaper.

### Optimistic lock: protecting updates to the same reservation
**Decision:** `Reservation` has a `@Version` column. An update succeeds only if the version is still the one that was read; otherwise Hibernate throws `ObjectOptimisticLockingFailureException`, which is mapped to 409.
**Why:** Two operations can read the same `HELD` reservation at the same time, for example a user confirming while an expiry job expires it. Without versioning the last write silently wins and a paid reservation ends up `EXPIRED`. This was reproduced in a test (`staleExpireDoesNotOverwriteConfirmedReservation`) that failed with `expected: <CONFIRMED> but was: <EXPIRED>` before `@Version` was added.
**Why not a pessimistic lock here:** These conflicts are rare, so locking every read would cost more than an occasional retry.

### Deadlocks
Reproduced manually with two `psql` sessions locking two seats in opposite order: PostgreSQL detected the cycle and aborted one transaction with `deadlock detected`. Repeating the experiment with both sessions locking in the same order (lowest id first) produced no error; the second session simply waited. `hold` currently locks a single seat, so it cannot deadlock. If multi-seat holds are added, seat ids must be sorted before locking.

## Roadmap

- Scheduled job that expires stale holds
- Caching and rate limiting with Redis
- Notification service with Kafka (outbox)
- Testcontainers, CI
