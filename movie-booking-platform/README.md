# Movie Booking Platform

A ticket booking system built with Spring Boot that lets users browse shows and book seats with automatic discount calculations.

## Quick Summary

This is an online movie ticket booking platform I built to demonstrate a real-world booking system. The core idea is simple - users pick a movie, choose a theatre and showtime, select their seats, and book. The system automatically applies discounts like 20% off for afternoon shows and 50% off on the third ticket.

I focused on two main scenarios: browsing available shows (read-heavy) and booking tickets (write-heavy with concurrency concerns). The booking flow was the trickier part since multiple users might try to book the same seat at the same time - I handled this using pessimistic locking at the database level.

## How to Run

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080/swagger-ui.html to try the APIs.

## System Design

### Architecture Approach

I went with a layered architecture that could evolve into microservices if needed. Right now it's a monolith, but the code is organized so that the Show Browsing and Booking logic are separate services internally. This makes it easier to split later without major refactoring.

For a production system, I'd add an API Gateway in front for rate limiting and auth, use Redis for caching show listings (since browsing is 90% of traffic), and put a message queue between booking and payment to handle failures gracefully.

### Database Design

The schema follows the real-world domain pretty closely. Cities have Theatres, Theatres have Screens, Screens have Seats. Then there's a Show entity that ties a Movie to a Screen at a specific time. The ShowSeat table is the junction that tracks availability and pricing per show - this is where the locking happens during booking.

I used H2 for development but the app is configured to work with MySQL/PostgreSQL in production. The key indexes are on (movie_id, show_date) for browsing queries and (show_id, status) for seat availability checks.

### Handling Concurrent Bookings

This was the main technical challenge. When two users click "book" on the same seat at the exact same time, only one should succeed. I solved this with `PESSIMISTIC_WRITE` locking in JPA:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId 
        AND ss.seat.id IN :seatIds AND ss.status = 'AVAILABLE'")
List<ShowSeat> findAvailableSeatsForBooking(Long showId, List<Long> seatIds);
```

The first transaction locks the rows, validates availability, and completes the booking. The second transaction waits, then finds the seats already booked and returns an error.

### Pricing and Offers

I kept the pricing logic in a separate service so it's easy to modify. Currently it handles:
- 20% discount for afternoon shows (12 PM - 5 PM)
- 50% off the cheapest ticket when booking 3 or more seats

The discounts stack, and the response shows exactly what was applied and how much the user saved.

## API Endpoints

**Browse Shows** - `GET /api/v1/shows/browse?movieId=1&city=Mumbai&date=2026-01-31`

Returns all theatres showing the movie with showtimes, available seats, and price range.

**Get Seat Layout** - `GET /api/v1/shows/{showId}/seats`

Returns the seat map with availability status and pricing for each seat.

**Book Tickets** - `POST /api/v1/bookings`

Takes show ID, seat IDs, and customer info. Returns booking reference with final amount after discounts.

**Get/Cancel Booking** - `GET/DELETE /api/v1/bookings/{reference}`

Retrieve booking details or cancel and release the seats.

## If This Were Production

For scaling, I'd cache the browsing queries in Redis since show listings don't change that often. The booking service would stay connected to the primary database for consistency. During peak times like new releases, I'd use a queue to handle booking requests so the system doesn't get overwhelmed.

For payments, I'd use a saga pattern - reserve seats first, then process payment, then confirm booking. If payment fails, release the seats. This keeps things consistent without distributed transactions.

Security-wise, I'd add JWT authentication through Spring Security, rate limiting at the API Gateway level, and make sure all PII is encrypted. The validation is already in place using Jakarta Bean Validation.

## Tech Choices

- **Java 17 + Spring Boot 3.2** - solid, well-documented, good for building REST APIs
- **Spring Data JPA** - handles the ORM and gives me locking out of the box
- **H2 (dev) / PostgreSQL (prod)** - in-memory for quick iteration, Postgres for real deployments
- **Lombok** - reduces boilerplate without sacrificing readability
- **OpenAPI/Swagger** - auto-generated API docs that actually stay in sync

## Project Structure

```
src/main/java/com/moviebooking/
├── controller/      # REST endpoints
├── service/         # Business logic
├── repository/      # Data access
├── entity/          # JPA entities
├── dto/             # Request/Response objects
├── exception/       # Error handling
└── config/          # App configuration
```

## What I'd Add Next

1. User authentication and booking history
2. Payment gateway integration (Razorpay/Stripe)
3. Real-time seat status updates via WebSocket
4. Email confirmations
5. Admin APIs for theatre partners to manage shows

---

Built for demonstration purposes. The focus was on clean code, proper error handling, and solving the concurrency problem correctly.
