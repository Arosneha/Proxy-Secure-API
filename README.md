# Secure API Proxy Service

A backend application developed using Java 21 and Spring Boot. It consists of two independently runnable Spring Boot services:

1. **Auth Service** – manages user registration, authentication, password hashing, role management, and JWT generation.
2. **Proxy Service** – validates JWTs locally, enforces role-based authorization, applies rate limiting, tracks API usage, and integrates with the OpenWeatherMap API.

Each service has its own PostgreSQL database and can be started and deployed independently.

---

## 1. Architecture

The application consists of two independent Spring Boot applications.

| Service | Port | Responsibility |
|---|---:|---|
| Auth Service | 8081 | Registration, login, password hashing, role management, and JWT generation |
| Proxy Service | 8082 | JWT validation, authorization, rate limiting, usage tracking, and weather API integration |

### Architecture diagram

```text
                      CLIENT / FRONTEND
                             |
                  +----------+----------+
                  |                     |
            Register / Login       Weather / Usage
                  |                     |
                  v                     v
       +----------------------+  +----------------------+
       |     AUTH SERVICE     |  |    PROXY SERVICE     |
       |      Port 8081       |  |      Port 8082       |
       |                      |  |                      |
       | User Registration    |  | JWT Validation       |
       | User Authentication  |  | Public Key           |
       | BCrypt Passwords     |  | Role Authorization   |
       | Role Management      |  | Rate Limiting        |
       | JWT Generation       |  | Usage Tracking       |
       | RSA Private Key      |  | Weather API Client   |
       +----------+-----------+  +----------+-----------+
                  |                         |
                  v                         +-------> OpenWeatherMap
           +-------------+                  |          API
           |   auth_db   |                  |          API Key
           |    users    |                  |
           +-------------+                  v
                                       +-------------+
                                       |   proxy_db  |
                                       |  api_usage  |
                                       +-------------+

       AUTH SERVICE SIGNS JWTs USING THE PRIVATE KEY

       PROXY SERVICE VALIDATES JWTs USING THE PUBLIC KEY

       NO AUTH-SERVICE CALL FOR NORMAL PROXY REQUESTS
```

### How the architecture works

1. The client registers or logs in through Auth Service.
2. Auth Service verifies the credentials and generates a signed JWT.
3. The client receives the JWT.
4. The client sends the JWT to Proxy Service in the Authorization header.
5. Proxy Service validates the JWT locally using the RSA public key.
6. Proxy Service checks the user's role and processes the request.

**The services are independent.** The diagram represents the client's logical request flow, not a startup sequence or runtime dependency.

- Auth Service can run independently.
- Proxy Service can run independently when its own database, configuration, and public key are available.
- Proxy Service does not call Auth Service for every incoming request.
- Each service owns its own PostgreSQL database.
- Auth Service signs JWTs using the RSA private key.
- Proxy Service validates JWTs using the matching RSA public key.

---

## 2. Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4.1.2 | Backend framework |
| Maven | Build and dependency management |
| Spring Security | Authentication and authorization |
| JWT | Token-based authentication |
| RS256 | Asymmetric JWT signing |
| BCrypt | Password hashing |
| PostgreSQL | Database |
| Spring Data JPA | Database persistence |
| Flyway | Database migrations |
| Jakarta Validation | Request validation |
| Spring RestClient | External API communication |
| JUnit 5 | Testing |
| Mockito | Mock-based testing |
| OpenWeatherMap | Weather information |
| dotenv-java | Loading `.env` values in Proxy Service |

---

## 3. Project Structure

The following structure reflects the project's source code. Secret files such as RSA keys and local environment files are intentionally excluded from the shared ZIP and must be supplied during local setup.

```text
Proxy-secure-API/
│
├── auth-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/auth_service/
│   │   │   │   ├── Config/
│   │   │   │   │   ├── JwtVerificationConfig.java
│   │   │   │   │   ├── PasswordConfig.java
│   │   │   │   │   └── RsaKeyConfig.java
│   │   │   │   │
│   │   │   │   ├── Controller/
│   │   │   │   │   ├── AdminController.java
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   └── SecurityTestController.java
│   │   │   │   │
│   │   │   │   ├── Dto/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── LoginResponse.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── RegisterResponse.java
│   │   │   │   │   └── UpdateRoleRequest.java
│   │   │   │   │
│   │   │   │   ├── Entity/
│   │   │   │   │   └── User.java
│   │   │   │   │
│   │   │   │   ├── Enums/
│   │   │   │   │   └── Role.java
│   │   │   │   │
│   │   │   │   ├── Exception/
│   │   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   │
│   │   │   │   ├── Repository/
│   │   │   │   │   └── UserRepository.java
│   │   │   │   │
│   │   │   │   ├── Service/
│   │   │   │   │   ├── AuthenticationService.java
│   │   │   │   │   ├── JwtService.java
│   │   │   │   │   ├── RegistrationService.java
│   │   │   │   │   └── RoleManagementService.java
│   │   │   │   │
│   │   │   │   ├── AuthServiceApplication.java
│   │   │   │   └── RsaKeyGenerator.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── db/migration/
│   │   │       │   └── V1__create_users_table.sql
│   │   │       └── application.properties
│   │   │
│   │   └── test/java/com/example/auth_service/
│   │       ├── AuthServiceApplicationTests.java
│   │       └── AuthServiceTest.java
│   │
│   └── pom.xml
│
├── proxy-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/proxy_service/
│   │   │   │   ├── Client/
│   │   │   │   │   ├── GeocodingClient.java
│   │   │   │   │   └── OpenWeatherClient.java
│   │   │   │   │
│   │   │   │   ├── Config/
│   │   │   │   │   ├── JwtConfig.java
│   │   │   │   │   ├── RateLimitInterceptor.java
│   │   │   │   │   ├── RestClientConfig.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── WeatherClientConfig.java
│   │   │   │   │   └── WebConfig.java
│   │   │   │   │
│   │   │   │   ├── Controller/
│   │   │   │   │   ├── AdminUsageController.java
│   │   │   │   │   ├── TestController.java
│   │   │   │   │   ├── UsageController.java
│   │   │   │   │   └── WeatherTestController.java
│   │   │   │   │
│   │   │   │   ├── Dto/
│   │   │   │   │   ├── CurrentWeatherResponse.java
│   │   │   │   │   ├── ForecastResponse.java
│   │   │   │   │   ├── GeocodingResponse.java
│   │   │   │   │   ├── OpenWeatherCurrentResponse.java
│   │   │   │   │   ├── OpenWeatherForecastResponse.java
│   │   │   │   │   ├── UsageResponse.java
│   │   │   │   │   └── WeatherSearchResponse.java
│   │   │   │   │
│   │   │   │   ├── Entity/
│   │   │   │   │   └── ApiUsage.java
│   │   │   │   │
│   │   │   │   ├── Exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   └── WeatherApiException.java
│   │   │   │   │
│   │   │   │   ├── Repository/
│   │   │   │   │   └── ApiUsageRepository.java
│   │   │   │   │
│   │   │   │   ├── Service/
│   │   │   │   │   ├── RateLimitService.java
│   │   │   │   │   ├── UsageTrackingService.java
│   │   │   │   │   └── WeatherService.java
│   │   │   │   │
│   │   │   │   └── ProxyServiceApplication.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── db/migration/
│   │   │       │   └── V2__create_api_usage_table.sql
│   │   │       └── application.properties
│   │   │
│   │   └── test/java/com/example/proxy_service/
│   │       ├── Client/
│   │       │   └── OpenWeatherClientTest.java
│   │       ├── Config/
│   │       │   └── JwtConfigTest.java
│   │       ├── Exception/
│   │       │   └── GlobalExceptionHandlerTest.java
│   │       ├── Service/
│   │       │   ├── RateLimitServiceTest.java
│   │       │   ├── UsageTrackingServiceTest.java
│   │       │   └── WeatherServiceTest.java
│   │       └── ProxyServiceApplicationTests.java
│   │
│   └── pom.xml
│
└── README.md
```

Each service has its own Maven `pom.xml` and can be built separately.

---

## 4. Auth Service

**Port:** `8081`

Base package:

```text
com.example.auth_service
```

The Auth Service manages user accounts and authentication.

### Main components

| Component | Responsibility |
|---|---|
| `AuthController` | Registration and login endpoints |
| `AdminController` | Administrator role-management endpoint |
| `AuthenticationService` | Credential verification and login |
| `RegistrationService` | User registration |
| `JwtService` | JWT generation |
| `RoleManagementService` | User role updates |
| `UserRepository` | User database operations |
| `PasswordConfig` | BCrypt password encoder and security configuration |
| `RsaKeyConfig` | RSA key loading and JWT encoder configuration |
| `JwtVerificationConfig` | JWT decoder configuration |

### Auth Service endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate and receive a JWT |
| PUT | `/api/admin/users/{userId}/role` | Update another user's role |
| GET | `/api/test/security` | Authenticated JWT test endpoint |

The registration and login endpoints are permitted without an existing JWT. Other protected endpoints require authentication. Role management additionally requires the ADMIN role.

### Registration request

```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json
```

Example:

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "Password123!"
}
```

Registration validation:

- Username must not be blank and must contain 3–50 characters.
- Email must be valid and must not exceed 255 characters.
- Password must contain 8–72 characters.
- Username and email must be unique.

The registration service trims the username, normalizes the email to lowercase, hashes the password using BCrypt, and assigns the `USER` role.

New registrations cannot choose their own role.

### Login request

```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json
```

Example:

```json
{
  "email": "alice@example.com",
  "password": "Password123!"
}
```

The login response contains:

- `accessToken`
- `tokenType`
- `expiresIn`

The token type is `Bearer`, and the expiration is 900 seconds.

### Role management

The role-management endpoint is restricted to authenticated ADMIN users.

The implementation follows these rules:

- ADMIN can change another user's role to USER, PREMIUM, or ADMIN.
- ADMIN can promote a USER to PREMIUM or ADMIN.
- ADMIN can change a PREMIUM user's role to USER or ADMIN.
- ADMIN can change another ADMIN's role to USER, PREMIUM, or ADMIN.
- ADMIN cannot change their own role.
- USER and PREMIUM cannot manage user roles.
- New registrations are always assigned the USER role.

The initial ADMIN account must be provisioned separately. Once an ADMIN account exists, that administrator can manage the roles of other accounts, including other ADMIN accounts.

---

## 5. Proxy Service

**Port:** `8082`

Base package:

```text
com.example.proxy_service
```

The Proxy Service processes protected weather and usage requests.

### Main components

| Component | Responsibility |
|---|---|
| `OpenWeatherClient` | Current weather and forecast API calls |
| `GeocodingClient` | City-to-coordinate lookup |
| `WeatherService` | Weather processing and response mapping |
| `JwtConfig` | RSA public key loading and JWT validation |
| `SecurityConfig` | Resource server security |
| `RateLimitInterceptor` | Rate-limit enforcement |
| `RateLimitService` | Per-user rate-limit counters |
| `UsageTrackingService` | Usage recording and retrieval |
| `ApiUsageRepository` | Usage database operations |
| `AdminUsageController` | Administrator usage endpoint |
| `UsageController` | Personal usage endpoint |
| `WeatherTestController` | Weather endpoints |
| `TestController` | JWT authentication test endpoint |

### Proxy Service endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/weather/current` | Current weather by coordinates |
| GET | `/api/weather/search` | Weather search by city |
| GET | `/api/weather/forecast` | Five-day forecast |
| GET | `/api/usage/me` | View personal usage |
| GET | `/api/admin/usage` | View all usage records |
| GET | `/api/test` | JWT authentication test endpoint |

The `/api/test` endpoint returns a simple success message after authentication.

All Proxy Service API endpoints require authentication. Requests must include a valid JWT in the Authorization header using the Bearer token format.

---

## 6. Role-Based Access Control

The application defines three roles:

- `USER`
- `PREMIUM`
- `ADMIN`

### Proxy Service permissions

| Endpoint | USER | PREMIUM | ADMIN |
|---|:---:|:---:|:---:|
| Current weather | Yes | Yes | No |
| City weather search | Yes | Yes | No |
| Five-day forecast | No | Yes | No |
| Personal usage | Yes | Yes | Yes |
| Administrator usage | No | No | Yes |

### USER

Can:

- Access current weather.
- Search weather by city.
- View personal usage.

Cannot:

- Access the five-day forecast.
- View all usage records.
- Manage user roles.

### PREMIUM

Can:

- Access all USER endpoints.
- Access the five-day forecast.
- View personal usage.

Cannot:

- View all usage records.
- Manage user roles.

### ADMIN

Can:

- View personal usage.
- View all usage records.
- Manage other users' roles through Auth Service.
- Assign USER, PREMIUM, or ADMIN roles to other accounts.
- Change the role of another administrator.

An ADMIN cannot change their own role.

ADMIN does not automatically receive weather endpoint access under the current Proxy Service authorization rules.

Role management is performed through Auth Service, while weather and usage authorization is enforced by Proxy Service.

---

## 7. JWT Authentication and RSA Keys

The application uses JSON Web Tokens signed with the RS256 algorithm.

### Token generation

Auth Service:

1. Receives login credentials.
2. Finds the user by email.
3. Verifies the password using BCrypt.
4. Generates a JWT.
5. Signs the JWT using the RSA private key.
6. Returns the token to the client.

### Token validation

Proxy Service:

1. Receives the bearer token.
2. Validates the signature using the RSA public key.
3. Validates the token's timestamp and expiration.
4. Validates the issuer.
5. Extracts the subject and role.
6. Maps the role to a Spring Security authority.

The Proxy Service does not need to contact Auth Service for normal token validation.

### JWT claims

| Claim | Value |
|---|---|
| Issuer | `secure-api-auth-service` |
| Subject | User email |
| Role | `USER`, `PREMIUM`, or `ADMIN` |
| Lifetime | 900 seconds |
| Signing algorithm | RS256 |

The role is mapped to the Spring Security authority format:

```text
USER     -> ROLE_USER
PREMIUM  -> ROLE_PREMIUM
ADMIN    -> ROLE_ADMIN
```

### Role changes and existing JWTs

A user's role is included in the JWT when the token is generated.

If an administrator changes a user's role, an already-issued JWT retains its original role claim until the token expires.

For example:

1. A USER logs in and receives a JWT containing the USER role.
2. An ADMIN changes that account's role to PREMIUM.
3. The existing JWT still contains the USER role.
4. The user must log in again to receive a new JWT containing the PREMIUM role.

Because Proxy Service validates JWTs locally, it does not automatically retrieve updated roles from Auth Service.

Immediate token revocation or role-change propagation is not implemented. With the current 15-minute token lifetime, an existing token can continue to carry its previous role until expiration.

### RSA key files

Auth Service loads:

- RSA private key.
- RSA public key.

Proxy Service loads:

- RSA public key only.

Both services must use the matching key pair.

The project includes `RsaKeyGenerator.java` in Auth Service.

OpenSSL can also generate the key pair:

```bash
mkdir -p secrets

openssl genpkey \
  -algorithm RSA \
  -out secrets/private.pem \
  -pkeyopt rsa_keygen_bits:2048

openssl rsa \
  -pubout \
  -in secrets/private.pem \
  -out secrets/public.pem
```

Keep the private key secure and accessible only to Auth Service.

The public key can be shared with Proxy Service because it is used for signature verification.

---

## 8. Database Design

The application uses two separate PostgreSQL databases.

### Auth Service database

Database name:

```text
auth_db
```

Main table:

```text
users
```

The `User` entity and `UserRepository` manage user records.

The table includes:

- `id`
- `username`
- `email`
- `password`
- `role`
- `created_at`
- `updated_at`

The role is stored as a string and is restricted by the application to:

```text
USER
PREMIUM
ADMIN
```

### Proxy Service database

Database name:

```text
proxy_db
```

Main table:

```text
api_usage
```

The `ApiUsage` entity and `ApiUsageRepository` manage API usage records.

The table includes:

- `id`
- `user_email`
- `request_count`
- `created_at`
- `updated_at`

Each user email has one usage record, and the request count cannot be negative.

### Database migrations

Auth Service:

```text
src/main/resources/db/migration/
└── V1__create_users_table.sql
```

Proxy Service:

```text
src/main/resources/db/migration/
└── V2__create_api_usage_table.sql
```

Flyway manages schema migrations for each service independently.

---

## 9. API Usage Tracking

Proxy Service tracks weather API usage using the authenticated user's email from the JWT subject.

The `UsageTrackingService` records requests through `ApiUsageRepository`.

The repository uses PostgreSQL's atomic upsert operation:

```sql
INSERT INTO api_usage (user_email, request_count)
VALUES (:email, 1)
ON CONFLICT (user_email)
DO UPDATE SET
    request_count = api_usage.request_count + 1,
    updated_at = CURRENT_TIMESTAMP;
```

This increments the request count without requiring a separate read-and-update operation.

### When requests are counted

- Current weather requests are counted before the upstream weather call.
- Forecast requests are counted before the upstream weather call.
- City search requests are counted after geocoding successfully identifies a location and before the weather call.
- An upstream weather request that fails after the increment is still counted.
- A city that cannot be resolved during geocoding is not counted as a weather request.
- Usage endpoints themselves do not increment weather usage.

**Important distinction:** API usage tracking records weather request usage in PostgreSQL. Rate limiting is a separate mechanism that counts authenticated requests in memory. A request to a usage endpoint can consume rate-limit capacity without incrementing the weather usage count.

The usage counter represents the application's tracked weather requests. It is not an exact account of every individual HTTP call made to OpenWeatherMap. For example, a city search may make both a geocoding request and a current weather request.

---

## 10. Rate Limiting

Proxy Service implements an in-memory rate limiter using:

- `ConcurrentHashMap`
- `AtomicInteger`

The implementation is divided into:

- `RateLimitService`
- `RateLimitInterceptor`
- `WebConfig`

### Application rate limits

| Role | Requests per 60-second window |
|---|---:|
| USER | 10 |
| PREMIUM | 30 |
| ADMIN | 60 |

The implementation uses a fixed 60-second window and identifies users using their JWT subject.

These limits are configured by the application and are separate from OpenWeatherMap's API quotas.

### Request flow

1. A request reaches Proxy Service.
2. Spring Security authenticates the JWT.
3. The interceptor obtains the authenticated user's email and role.
4. The rate-limit service checks the user's current counter.
5. If the limit has been reached, the request is rejected with HTTP 429.
6. Otherwise, the request continues.

The interceptor is registered for `/api/**`.

The rate limit is shared across the user's matching API requests, rather than being a separate limit for each endpoint.

For example, a USER has a total allowance of 10 requests during the applicable 60-second window across the intercepted `/api/**` endpoints.

### Current limitations

The rate limiter is in-memory and per application instance.

Therefore:

- Counters reset when Proxy Service restarts.
- Multiple Proxy Service instances do not share rate-limit state.
- Inactive user entries are not automatically removed.
- The implementation does not return a `Retry-After` header.
- Because the interceptor runs before controller method-level authorization, a request that is later rejected with 403 can still consume rate-limit capacity.
- Auth Service requests are not handled by this Proxy Service rate limiter.

A shared store such as Redis could be considered for distributed rate limiting.

---

## 11. OpenWeatherMap Integration

Proxy Service communicates with OpenWeatherMap using:

- `OpenWeatherClient`
- `GeocodingClient`

The `WeatherService` processes the external responses and maps them into application DTOs.

The application exposes its own response structures rather than directly returning the external API response format.

### Current weather

```http
GET /api/weather/current?lat=13.0827&lon=80.2707
```

### City search

```http
GET /api/weather/search?city=Chennai
```

The city search process:

1. Uses the geocoding client to find coordinates.
2. Returns a not-found response if no location is found.
3. Uses the first geocoding result.
4. Calls the current weather endpoint using those coordinates.
5. Returns the mapped weather search response.

### Five-day forecast

```http
GET /api/weather/forecast?lat=13.0827&lon=80.2707
```

Forecast timestamps are converted to UTC `LocalDateTime` values in the response mapping.

### Input validation

Latitude:

```text
Minimum: -90
Maximum: 90
```

Longitude:

```text
Minimum: -180
Maximum: 180
```

City:

```text
Must not be blank.
Maximum length: 100 characters.
```

The OpenWeatherMap API key is used only by the Proxy Service weather and geocoding clients.

### OpenWeatherMap API quota

OpenWeatherMap has its own API usage limits. These are separate from the USER, PREMIUM, and ADMIN limits configured in this application.

According to OpenWeatherMap's published Free Weather API access information:

- **60 API calls per minute**
- **1,000,000 API calls per month**

The 1,000-calls-per-day free allowance shown on OpenWeatherMap's website applies to its separate One Call API 4.0 pay-as-you-call product. It should not be treated as the general daily quota for the Current Weather and five-day Forecast APIs used by this project.

The provider's limits and subscription terms can change. Check the official [OpenWeatherMap pricing page](https://openweathermap.org/price) for the current limits applicable to your account and API products.

The OpenWeatherMap quota is associated with the provider account/API usage, not separately allocated to each role in this application. Multiple users of this Proxy Service share the same configured upstream API key.

**The application's usage table does not measure the exact OpenWeatherMap account quota.** It tracks application-level weather requests by user. A city search can make more than one upstream API call.

---

## 12. Configuration and Environment Variables

Sensitive values should not be hardcoded into source code or committed to version control.

Both services use `application.properties` for Spring configuration.

### Auth Service configuration

The Auth Service uses settings such as:

```properties
spring.application.name=auth-service
server.port=8081

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/auth_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.jpa.show-sql=false

spring.flyway.enabled=true

jwt.public-key-path=${JWT_PUBLIC_KEY_PATH:Secret/public.pem}
jwt.private-key-path=${JWT_PRIVATE_KEY_PATH:Secret/private.pem}
```

Configure the database password through the `DB_PASSWORD` environment variable. If the local `application.properties` still has a blank password property, update it to use the environment variable or configure the password directly for local development.

The Auth Service needs both RSA keys because its RSA configuration loads the private key for signing and the public key for JWT encoder/verification configuration.

### Proxy Service configuration

The Proxy Service uses settings such as:

```properties
spring.application.name=proxy-service
server.port=8082

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/proxy_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.jpa.show-sql=false

spring.flyway.enabled=true

jwt.public-key-path=${JWT_PUBLIC_KEY_PATH:secrets/public.pem}

weather.api.base-url=https://api.openweathermap.org
weather.api.key=${OPENWEATHER_API_KEY}
```

### Environment variable responsibilities

| Variable | Auth Service | Proxy Service |
|---|:---:|:---:|
| `DB_URL` | Yes | Yes |
| `DB_USERNAME` | Yes | Yes |
| `DB_PASSWORD` | Yes | Yes |
| `JWT_PRIVATE_KEY_PATH` | Yes | No |
| `JWT_PUBLIC_KEY_PATH` | Yes | Yes |
| `OPENWEATHER_API_KEY` | No | Yes |

The exact paths are configurable. The default key paths in the two services use different folder capitalization, so set explicit paths that match your actual directory and operating system.

### IntelliJ environment variables

Environment variables can be configured separately for each service:

1. Open IntelliJ IDEA.
2. Go to **Run → Edit Configurations**.
3. Select Auth Service.
4. Add its database and RSA key path variables.
5. Select Proxy Service.
6. Add its database, public key path, and OpenWeatherMap API key variables.

Proxy Service loads a `.env` file using `dotenv-java` in `ProxyServiceApplication`. Auth Service does not load a `.env` file through that mechanism.

---

## 13. Running the Application

### Prerequisites

Install:

- Java 21
- Maven
- PostgreSQL
- OpenSSL, if generating RSA keys using OpenSSL
- An OpenWeatherMap API key

### Step 1: Create the databases

Create the two databases in PostgreSQL:

```sql
CREATE DATABASE auth_db;

CREATE DATABASE proxy_db;
```

Configure the database credentials for both services.

### Step 2: Generate RSA keys

Generate the RSA private and public key pair.

Configure the private key path in Auth Service and the matching public key path in both services.

### Step 3: Configure Auth Service

Set:

- Database URL.
- Database username.
- Database password.
- RSA private key path.
- RSA public key path.

### Step 4: Configure Proxy Service

Set:

- Database URL.
- Database username.
- Database password.
- RSA public key path.
- OpenWeatherMap API key.

### Step 5: Start Auth Service

Open a terminal:

```bash
cd auth-service

mvn spring-boot:run
```

Auth Service runs at:

```text
http://localhost:8081
```

### Step 6: Start Proxy Service

Open another terminal:

```bash
cd proxy-service

mvn spring-boot:run
```

Proxy Service runs at:

```text
http://localhost:8082
```

Both services must be started separately. Starting one does not automatically start the other.

---

## 14. Example API Workflow

### Step 1: Register

```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json
```

Request:

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "Password123!"
}
```

Expected success status: `201 Created`.

New accounts receive the USER role by default.

### Step 2: Login

```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "alice@example.com",
  "password": "Password123!"
}
```

The response includes an access token, token type, and expiration.

Example response shape:

```json
{
  "accessToken": "YOUR_JWT_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Step 3: Get current weather

```http
GET http://localhost:8082/api/weather/current?lat=13.0827&lon=80.2707
Authorization: Bearer YOUR_JWT_TOKEN
```

### Step 4: Search by city

```http
GET http://localhost:8082/api/weather/search?city=Chennai
Authorization: Bearer YOUR_JWT_TOKEN
```

### Step 5: View personal usage

```http
GET http://localhost:8082/api/usage/me
Authorization: Bearer YOUR_JWT_TOKEN
```

### Step 6: Access the forecast

A PREMIUM user can call:

```http
GET http://localhost:8082/api/weather/forecast?lat=13.0827&lon=80.2707
Authorization: Bearer YOUR_JWT_TOKEN
```

A USER does not have permission to access this endpoint.

### Step 7: View administrator usage

An ADMIN user can call:

```http
GET http://localhost:8082/api/admin/usage
Authorization: Bearer YOUR_JWT_TOKEN
```

### Step 8: Change another user's role

An ADMIN can change another user's role through Auth Service:

```http
PUT http://localhost:8081/api/admin/users/2/role
Authorization: Bearer ADMIN_JWT_TOKEN
Content-Type: application/json
```

Example request:

```json
{
  "role": "PREMIUM"
}
```

The target user must not be the administrator making the request.

An ADMIN can also assign the ADMIN role to another account:

```json
{
  "role": "ADMIN"
}
```

After a role change, the target user must log in again to receive a JWT containing the updated role.

---

## 15. Error Handling

Both services use a `GlobalExceptionHandler`.

Auth Service handles:

- Duplicate username or email using HTTP 409.
- Invalid login credentials using HTTP 401.
- Illegal argument errors using HTTP 400.

Proxy Service handles:

- Invalid request parameters using HTTP 400.
- Illegal argument errors using HTTP 400.
- Weather API errors using the status provided by `WeatherApiException`.

The weather and geocoding clients map upstream failures as follows:

| Situation | HTTP status |
|---|---:|
| Upstream resource not found | 404 |
| Upstream rate limit | 429 |
| Upstream authentication error | 502 |
| Other upstream HTTP error | 502 |
| Connection failure | 502 |
| Timeout | 504 |

The error messages returned by the weather clients are generic and do not include the original upstream request URL or API key.

Authentication and authorization failures are handled by the respective Spring Security configurations.

Typical security response statuses are:

| Situation | HTTP status |
|---|---:|
| Missing or invalid JWT | 401 Unauthorized |
| Valid JWT without required role | 403 Forbidden |
| Rate limit exceeded | 429 Too Many Requests |

---

## 16. Testing

The project contains separate test classes for both services.

### Auth Service tests

```text
src/test/java/com/example/auth_service/

├── AuthServiceApplicationTests.java
└── AuthServiceTest.java
```

The `AuthServiceTest` class covers registration, duplicate username and email handling, login, incorrect credentials, JWT claims, and role-management behavior.

### Proxy Service tests

```text
src/test/java/com/example/proxy_service/

├── Client/
│   └── OpenWeatherClientTest.java
│
├── Config/
│   └── JwtConfigTest.java
│
├── Exception/
│   └── GlobalExceptionHandlerTest.java
│
├── Service/
│   ├── RateLimitServiceTest.java
│   ├── UsageTrackingServiceTest.java
│   └── WeatherServiceTest.java
│
└── ProxyServiceApplicationTests.java
```

These tests cover weather client behavior, JWT configuration, exception handling, rate limiting, usage tracking, weather service behavior, and application context startup.

### Run Auth Service tests

```bash
cd auth-service

mvn test
```

### Run Proxy Service tests

```bash
cd proxy-service

mvn test
```

Test results should be confirmed from the actual Maven output before reporting a successful test run.

---

## 17. Security Considerations

The application includes:

- BCrypt password hashing.
- RS256 JWT signing.
- RSA private key restricted to Auth Service.
- RSA public key used for JWT validation.
- JWT expiration and issuer validation.
- Role-based authorization.
- Stateless security configuration.
- Backend-only OpenWeatherMap API key usage.
- Separate databases for user and API usage data.
- Centralized exception handling.

### Secret files and Git exclusions

RSA key files and secret configuration files should not be committed to version control.

A root-level `.gitignore` should include:

```gitignore
# Secrets
/secrets/
Secret/
.env
**/.env
*.pem
*.key

# Build output
**/target/

# IDE files
.idea/
.vscode/

# Logs
*.log

# OS files
.DS_Store
Thumbs.db
```

The `*.pem` rule excludes both public and private PEM files from Git. Public keys can be regenerated or securely distributed when needed.

Before running the application, provide the required keys and credentials through local configuration or environment variables.

---

## 18. Known Limitations

1. **In-memory rate limiting:** Rate-limit state is not shared across multiple Proxy Service instances and resets on restart.
2. **No inactive-entry eviction:** The current rate-limit map does not automatically remove inactive user entries.
3. **No Retry-After header:** The rate-limit response does not provide a retry time.
4. **JWT lifetime:** Tokens expire after 15 minutes. A refresh-token workflow is not included.
5. **Administrator provisioning:** The initial ADMIN account must be provisioned separately. Once an ADMIN exists, that administrator can assign or change roles for other accounts, including other ADMIN accounts. An administrator cannot change their own role.
6. **Separate startup:** Auth Service and Proxy Service must be started independently.
7. **External API dependency:** Weather requests depend on OpenWeatherMap availability, account limits, and a valid API key.
8. **Timeout configuration:** The current `RestClientConfig` creates a basic RestClient builder and does not explicitly configure connection or read timeout durations.
9. **Existing JWT role claims:** Role changes do not update already-issued JWTs. A user must log in again to receive a token containing the updated role. Immediate token revocation is not implemented.
10. **Per-instance rate limiting:** Running multiple Proxy Service instances can result in independent counters for the same user because rate-limit state is not stored centrally.

Items 1 and 10 describe related aspects of the same in-memory rate-limiting design. These are limitations of the current implementation and potential areas for future improvement.

---

## 19. Assignment Coverage

| Requirement | Implementation |
|---|---|
| Independent Auth and Proxy services | Two Spring Boot applications |
| User registration | `RegistrationService` |
| User login | `AuthenticationService` |
| Password hashing | `PasswordConfig` and BCrypt |
| JWT generation | `JwtService` |
| RSA key handling | `RsaKeyConfig` and `JwtConfig` |
| JWT verification | Spring Security JWT resource server |
| Role management | `RoleManagementService` |
| Role-based authorization | Spring Security method authorization |
| Weather API integration | `OpenWeatherClient` |
| City geocoding | `GeocodingClient` |
| Weather processing | `WeatherService` |
| API usage tracking | `UsageTrackingService` |
| Usage persistence | `ApiUsageRepository` |
| Rate limiting | `RateLimitService` and `RateLimitInterceptor` |
| Separate databases | `auth_db` and `proxy_db` |
| Database migrations | Flyway |
| Exception handling | `GlobalExceptionHandler` |

---

## 20. Conclusion

The Secure API Proxy Service demonstrates a two-service backend architecture with independent authentication and API proxy responsibilities.

Auth Service manages registration, login, password verification, role management, and JWT generation.

Proxy Service validates JWTs locally, applies role-based authorization and rate limiting, tracks API usage, and communicates with OpenWeatherMap.

The services use separate PostgreSQL databases and can be started and deployed independently. RSA-based token signing and verification provide the authentication boundary between them.

The project also documents its current implementation trade-offs and possible future improvements.