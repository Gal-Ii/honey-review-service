package app.review.exception;

import app.review.web.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler =
            new GlobalExceptionHandler();

    @Test
    void handleReviewAlreadyExistsShouldReturnConflict() {
        ReviewAlreadyExistsException exception =
                new ReviewAlreadyExistsException(
                        "The user has already reviewed this product."
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleReviewAlreadyExists(
                        exception
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());

        assertEquals(
                HttpStatus.CONFLICT.value(),
                response.getBody().getStatus()
        );

        assertEquals(
                HttpStatus.CONFLICT.getReasonPhrase(),
                response.getBody().getError()
        );

        assertEquals(
                "The user has already reviewed this product.",
                response.getBody().getMessage()
        );
    }
}
