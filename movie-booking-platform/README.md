# Movie Booking Platform

An online movie ticket booking platform built with Spring Boot that caters to both B2B (theatre partners) and B2C (end customers) clients.

## 🎯 Features Implemented

### READ Scenario
**Browse theatres currently running a movie in the town, including show timing by a chosen date**
- API to search for shows by movie, city, and date
- Returns list of theatres with show timings
- Includes available seat count and starting prices
- Shows applicable offers for each show

### WRITE Scenario
**Book movie tickets by selecting a theatre, timing, and preferred seats for the day**
- API to book tickets with seat selection
- Validates seat availability with pessimistic locking (handles concurrent bookings)
- Automatic offer application
- Returns booking confirmation with pricing details

### Offers Implemented
1. **50% discount on 3rd ticket** - Applied to the cheapest ticket when booking 3+ tickets
2. **20% discount for afternoon shows** - Automatically applied for shows between 12 PM and 5 PM

## 🏗️ Architecture & Design Patterns

### Design Patterns Used
1. **Service Layer Pattern** - Business logic separation from controllers
2. **Repository Pattern** - Data access abstraction
3. **Builder Pattern** - Object construction (using Lombok @Builder)
4. **Strategy Pattern** - Pricing calculations (extensible for future offers)
5. **DTO Pattern** - Data transfer between layers

### Key Architectural Decisions
1. **Pessimistic Locking** - Prevents race conditions during concurrent seat bookings
2. **Optimistic Locking** - Version field on ShowSeat for additional concurrency control
3. **Transaction Management** - Ensures data consistency during booking
4. **Layered Architecture** - Controller → Service → Repository

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database (for demo)
- **Lombok** - Boilerplate code reduction
- **SpringDoc OpenAPI** - API documentation
- **Maven** - Build tool

## 📦 Project Structure

```
src/main/java/com/moviebooking/
├── MovieBookingApplication.java    # Main application
├── config/
│   ├── DataInitializer.java        # Sample data setup
│   └── OpenApiConfig.java          # Swagger configuration
├── controller/
│   ├── ShowBrowsingController.java # READ APIs
│   └── BookingController.java      # WRITE APIs
├── service/
│   ├── ShowBrowsingService.java    # Browse shows interface
│   ├── BookingService.java         # Booking interface
│   ├── PricingService.java         # Pricing interface
│   └── impl/                       # Service implementations
├── repository/                     # JPA Repositories
├── entity/                         # JPA Entities
├── dto/
│   ├── request/                    # Request DTOs
│   └── response/                   # Response DTOs
├── enums/                          # Enumerations
└── exception/                      # Custom exceptions & handler
```

## 🚀 How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Steps

1. **Navigate to project directory**
   ```bash
   cd movie-booking-platform
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:moviebooking`)
   - API Docs: http://localhost:8080/api-docs

## 📡 API Endpoints

### Show Browsing APIs (READ)

#### Browse Shows
```
GET /api/v1/shows/browse?movieId=1&city=Mumbai&date=2026-01-30
```

#### Get Show Seats
```
GET /api/v1/shows/{showId}/seats
```

### Booking APIs (WRITE)

#### Book Tickets
```
POST /api/v1/bookings
Content-Type: application/json

{
    "showId": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "customerPhone": "+91-9876543210",
    "seatIds": [1, 2, 3]
}
```

#### Get Booking Details
```
GET /api/v1/bookings/{bookingReference}
```

#### Cancel Booking
```
DELETE /api/v1/bookings/{bookingReference}
```

## 🧪 Sample API Requests

### 1. Browse Shows (with curl)
```bash
curl -X GET "http://localhost:8080/api/v1/shows/browse?movieId=1&city=Mumbai&date=2026-01-30"
```

### 2. Get Seat Availability
```bash
curl -X GET "http://localhost:8080/api/v1/shows/1/seats"
```

### 3. Book Tickets (3 seats - gets 50% off on 3rd ticket)
```bash
curl -X POST "http://localhost:8080/api/v1/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "showId": 3,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "customerPhone": "+91-9876543210",
    "seatIds": [21, 22, 23]
  }'
```
*Note: Show ID 3 is an afternoon show (2:00 PM) - gets both 20% afternoon discount AND 50% off 3rd ticket*

### 4. Get Booking
```bash
curl -X GET "http://localhost:8080/api/v1/bookings/BK12345ABC"
```

## 📊 Data Model

### Entity Relationship
```
City (1) ──── (N) Theatre (1) ──── (N) Screen (1) ──── (N) Seat
                    │                      │
                    │                      │
                    └─────── (N) Show (N) ─┘
                                  │
                                  │
                    Booking (1) ──┴── (N) ShowSeat
```

### Key Entities
- **City**: Location where theatres operate
- **Theatre**: B2B partner with multiple screens
- **Screen**: Auditorium with seat layout
- **Seat**: Individual seat with category and pricing
- **Movie**: Movie details
- **Show**: A movie screening at a specific time
- **ShowSeat**: Seat availability for a specific show
- **Booking**: Customer booking with payment info

## 🔒 Non-Functional Requirements Addressed

### Transaction Management
- Pessimistic locking on seat selection
- Atomic booking operations
- Rollback on failure

### Scalability Considerations
- Stateless REST APIs (can be load balanced)
- Database can be scaled independently
- Caching can be added for read-heavy operations

### Security (Discussion Points)
- Input validation with Bean Validation
- Parameterized queries (JPA - prevents SQL injection)
- Exception handling (no stack traces exposed)

## 🎨 Future Enhancements

1. **Authentication & Authorization** - JWT-based security
2. **Payment Gateway Integration** - Razorpay/Stripe
3. **Caching** - Redis for show/seat availability
4. **Message Queue** - RabbitMQ/Kafka for booking confirmations
5. **Search Service** - Elasticsearch for movie search
6. **Notifications** - Email/SMS confirmations
7. **Rate Limiting** - Prevent abuse
8. **Circuit Breaker** - Resilience patterns

## 👤 Author

Interview Candidate - Sapient Technical Assessment

---

*This solution demonstrates clean architecture, SOLID principles, and production-ready coding practices.*
