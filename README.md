# Honey Review Service

Honey Review Service is an independent Spring Boot REST microservice for product reviews in the Honey Shop application. The main application consumes its API through an OpenFeign client.

The service runs independently on port `8081` and uses a database separate from the main Honey Shop application.

## Technology Stack

- Java 17
- Spring Boot 3.4.0
- Spring Web
- Spring Data JPA
- Bean Validation
- MySQL
- H2 for automated tests
- Maven
- Lombok and SLF4J
- JaCoCo

## Domain Model

The service manages the `ProductReview` domain entity. Every review uses a UUID primary key and contains:

- product identifier
- authoring user identifier
- author name
- rating from 1 to 5
- review comment
- creation and update timestamps

A user can submit no more than one review for the same product.

## Supported Functionality

- List all reviews for a product
- Create a product review
- Update a review belonging to the requesting user
- Delete a review belonging to the requesting user
- Reject duplicate product reviews by the same user
- Return meaningful validation and operation errors

All state-changing operations include SLF4J log statements.

## REST API

Base path:

```text
/api/v1/reviews
```

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/reviews?productId={productId}` | List reviews for a product |
| `POST` | `/api/v1/reviews` | Create a review |
| `PUT` | `/api/v1/reviews/{reviewId}` | Update a review |
| `DELETE` | `/api/v1/reviews/{reviewId}?userId={userId}` | Delete a review |

### Create review request

```json
{
  "productId": "11111111-1111-1111-1111-111111111111",
  "userId": "22222222-2222-2222-2222-222222222222",
  "authorName": "Honey Customer",
  "rating": 5,
  "comment": "Excellent honey with a rich natural flavor."
}
```

### Update review request

```json
{
  "userId": "22222222-2222-2222-2222-222222222222",
  "rating": 4,
  "comment": "Very good honey with a pleasant natural flavor."
}
```

## Validation and Error Handling

Validation is applied to request DTOs, the entity, and service operations. The service handles:

- invalid request bodies
- invalid UUID parameter values
- missing request parameters
- duplicate reviews
- missing reviews
- attempts to update or delete another user's review
- unexpected server errors

Errors are returned as JSON responses containing a timestamp, HTTP status, error name, and message.

## Configuration

The service listens on:

```properties
server.port=8081
```

The MySQL database configuration reads credentials from environment variables:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/honey-review-service?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Required environment variables:

```text
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
```

## Running the Service

Prerequisites:

- Java 17 or newer
- MySQL Server

From the project directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8081/api/v1/reviews
```

The Honey Shop main application is configured to communicate with the service through its `ReviewFeignClient`.

## Testing and Coverage

The project contains:

- unit tests for `ProductReviewService`
- an integration test for `ProductReviewRepository`
- API tests for `ProductReviewController`
- tests for global exception handling
- a Spring application-context test

Run the complete test and coverage verification:

```powershell
.\mvnw.cmd verify
```

JaCoCo enforces a minimum of 70% line coverage. At the latest verification, all 20 tests passed and the measured line coverage was 79.02%.

## Repository

Public repository:

```text
https://github.com/Gal-Ii/honey-review-service
```
