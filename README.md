# Ticket Reservation — reservation-service

The reservation service of a ticket reservation system. Users list shows and seats, hold a seat for 10 minutes, then confirm or cancel before the hold expires.

**Tech stack:** Java 21, Spring Boot 4.1, Spring Data JPA (Hibernate), PostgreSQL 17, Flyway, Spring Security (JWT), Redis 7, JUnit 5, Mockito.

## Running

```bash
docker compose up -d      # PostgreSQL (5433) and Redis (6379)
cd reservation-service
./mvnw spring-boot:run    # application at http://localhost:8080
./mvnw test               # tests (PostgreSQL and Redis must be running)
```

The database schema is created by Flyway on startup (`reservation-service/src/main/resources/db/migration`). Hibernate never changes the schema, it only validates it (`ddl-auto: validate`).

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

Errors are returned as RFC 9457 `ProblemDetail`: 400 (validation), 401 (missing/invalid credentials), 403 (not allowed), 404 (not found), 409 (state conflict), 429 (rate limit exceeded).

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

## Redis

PostgreSQL is the source of truth. Redis is used for four jobs where a shared, fast store with built-in expiry fits better than a table.

| Job | Key | Expiry |
|---|---|---|
| Refresh tokens | random UUID → user | 7 days |
| Seat hold gate | `seat_hold:{seatId}` → user id | 10 minutes |
| Cache | `shows`, `seats::{showId}` | 5 minutes |
| Rate limit | `rate:hold:{userId}` → counter | 60 seconds |

### Seat hold gate
**Decision:** Before touching the database, `hold` runs `SET seat_hold:{seatId} NX EX 600`. Only the request that sets the key continues to PostgreSQL; the others get 409 immediately.
**Why:** Redis executes commands one at a time, so `SET NX` is atomic: out of 50 concurrent requests exactly one wins. The losers never open a transaction or wait for a row lock, which protects the connection pool (10 connections).
**Trade-off:** Redis and PostgreSQL are two systems, so they can disagree. If the database step fails, or the reservation is cancelled or expires, the key is released explicitly; otherwise the seat would stay blocked until the TTL runs out. The gate is an optimization, not the guarantee: the pessimistic lock and the partial unique index still decide who owns the seat.

### Scheduled expiry
**Decision:** A `@Scheduled` job runs every minute, marks `HELD` reservations whose `expires_at` has passed as `EXPIRED`, and releases their Redis keys.
**Why:** The Redis key disappears by itself, but the row in PostgreSQL does not. Without the job a stale `HELD` row would keep the partial unique index occupied.

### Cache-aside for read endpoints
**Decision:** `GET /shows` and `GET /shows/{showId}/seats` are cached with `@Cacheable`. Creating a show evicts the whole `shows` cache; adding a seat evicts only that show's entry (`key = "#showId"`). Every entry also has a 5-minute TTL.
**Why:** These lists are read far more often than they change. Eviction keeps the cache correct for writes that go through the service; the TTL is the safety net for writes that do not (a manual `UPDATE`, a future method that forgets `@CacheEvict`).
**Trade-off:** Values are stored as JSON, not Java serialization, so they are readable in `redis-cli` and do not depend on class names. The price is one typed serializer per cache in `CacheConfig`; a cache without its own configuration falls back to Java serialization and fails for records that are not `Serializable`.

### Rate limiting hold attempts
**Decision:** Each user gets 10 hold attempts per 60 seconds (fixed window). The counter is incremented first, before the seat gate and the database; over the limit the API returns 429.
**Why Redis and not an in-memory map:** With several application instances each would count separately and the real limit would be multiplied. Redis gives all instances one counter, and expiry resets it.
**Why a Lua script:** `INCR` and `EXPIRE` are two commands. If the application stops between them, the counter has no TTL and the user is blocked forever once it reaches the limit. The script runs both as one atomic step.
**Trade-off:** A fixed window allows a burst at the boundary (10 requests at the end of one window and 10 at the start of the next). Failed attempts count too, on purpose.

### What happens if Redis is down
- **Holds, cancels, cached reads, refresh, logout:** the request fails with 500. The service fails closed; there is no fallback path yet.
- **Requests with a valid access token that do not touch Redis** (confirm, listing own reservations) keep working, because a JWT is verified without Redis.
- **Data:** no reservation is lost, since reservations live in PostgreSQL. Refresh tokens are lost unless Redis persistence (RDB/AOF) is enabled, so users have to log in again. Seat hold keys and counters are also gone, but the database lock and unique index still prevent double booking.

### Known limitations
- `release` deletes the hold key without checking who owns it.
- With more than one application instance the expiry job would run on each of them.

## Roadmap

- Notification service with Kafka (outbox)
- Testcontainers, CI
