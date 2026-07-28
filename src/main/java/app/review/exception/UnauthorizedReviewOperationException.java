package app.review.exception;

public class UnauthorizedReviewOperationException extends RuntimeException {

    public UnauthorizedReviewOperationException(String message) {
        super(message);
    }
}
