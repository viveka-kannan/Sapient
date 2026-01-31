# Quick Setup Guide for Movie Booking Platform

## Prerequisites Installation

### Option 1: Install Maven (Recommended)

#### Using Chocolatey (if installed):
```powershell
choco install maven
```

#### Manual Installation:
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH: `C:\Program Files\Apache\maven\bin`
4. Set `MAVEN_HOME` environment variable

### Option 2: Use IntelliJ IDEA or Eclipse (Easiest)

1. **IntelliJ IDEA** (Free Community Edition)
   - Download from: https://www.jetbrains.com/idea/download/
   - Open the project folder: `movie-booking-platform`
   - IntelliJ will auto-detect Maven and download dependencies
   - Run `MovieBookingApplication.java`

2. **Eclipse IDE**
   - Download from: https://www.eclipse.org/downloads/
   - File → Import → Maven → Existing Maven Projects
   - Select the project folder
   - Run as Spring Boot App

## Running the Application

### With Maven (after installation):
```bash
cd movie-booking-platform
mvn clean install
mvn spring-boot:run
```

### With Maven Wrapper (after fixing JAVA_HOME):
```bash
# Set JAVA_HOME first
set JAVA_HOME=C:\path\to\your\jdk
cd movie-booking-platform
.\mvnw.cmd spring-boot:run
```

## Testing the APIs

Once the application is running on http://localhost:8080:

### Access Swagger UI
Open browser: http://localhost:8080/swagger-ui.html

### H2 Database Console
Open browser: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:moviebooking
- User: sa
- Password: (leave empty)

## Sample API Requests

### 1. Browse Shows (READ Scenario)
```bash
curl -X GET "http://localhost:8080/api/v1/shows/browse?movieId=1&city=Mumbai&date=2026-01-30"
```

### 2. Get Seat Availability
```bash
curl -X GET "http://localhost:8080/api/v1/shows/1/seats"
```

### 3. Book Tickets (WRITE Scenario)
```bash
curl -X POST "http://localhost:8080/api/v1/bookings" ^
  -H "Content-Type: application/json" ^
  -d "{\"showId\": 3, \"customerName\": \"John Doe\", \"customerEmail\": \"john@example.com\", \"seatIds\": [21, 22, 23]}"
```

## Troubleshooting

### JAVA_HOME not set
Find your JDK installation and set JAVA_HOME:
```powershell
# Find Java location
where java

# Set JAVA_HOME (replace with actual path)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

### Port 8080 already in use
Edit `application.yml` and change:
```yaml
server:
  port: 8081
```
