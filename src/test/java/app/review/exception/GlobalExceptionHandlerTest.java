package app.review.exception;

import app.review.web.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

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

    @Test
    void handleTypeMismatchShouldReturnBadRequest() {

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleTypeMismatch();

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());

        assertEquals(
                HttpStatus.BAD_REQUEST.value(),
                response.getBody().getStatus()
        );

        assertEquals(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                response.getBody().getError()
        );

        assertEquals(
                "The request contains an invalid parameter value.",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleMissingParameterShouldReturnBadRequest()
            throws MissingServletRequestParameterException {

        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException(
                        "productId",
                        "UUID"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleMissingParameter(exception);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                HttpStatus.BAD_REQUEST.value(),
                response.getBody().getStatus()
        );

        assertEquals(
                "Required request parameter is missing: productId",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleUnexpectedExceptionShouldReturnInternalServerError() {

        RuntimeException exception =
                new RuntimeException("Test unexpected error.");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleUnexpectedException(exception);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                response.getBody().getStatus()
        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                response.getBody().getError()
        );

        assertEquals(
                "An unexpected server error occurred.",
                response.getBody().getMessage()
        );
    }
}
