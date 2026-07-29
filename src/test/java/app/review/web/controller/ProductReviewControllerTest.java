package app.review.web.controller;

import app.review.service.ProductReviewService;
import app.review.web.dto.ReviewResponse;
import app.review.web.dto.CreateReviewRequest;
import app.review.web.dto.UpdateReviewRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductReviewControllerTest {

    @Mock
    private ProductReviewService productReviewService;

    private ProductReviewController productReviewController;

    @BeforeEach
    void setUp() {
        productReviewController =
                new ProductReviewController(productReviewService);
    }

    @Test
    void getReviewsShouldReturnReviewsWithOkStatus() {
        UUID productId = UUID.randomUUID();

        ReviewResponse review = ReviewResponse.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .userId(UUID.randomUUID())
                .authorName("Ivan Ivanov")
                .rating(5)
                .comment("Excellent natural honey.")
                .build();

        when(productReviewService.getReviewsByProductId(productId))
                .thenReturn(List.of(review));

        ResponseEntity<List<ReviewResponse>> response =
                productReviewController.getReviews(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(review), response.getBody());

        verify(productReviewService)
                .getReviewsByProductId(productId);
    }

    @Test
    void createReviewShouldReturnCreatedReview() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateReviewRequest request = new CreateReviewRequest();
        request.setProductId(productId);
        request.setUserId(userId);
        request.setAuthorName("Ivan Ivanov");
        request.setRating(5);
        request.setComment("Excellent natural honey.");

        ReviewResponse createdReview = ReviewResponse.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .userId(userId)
                .authorName("Ivan Ivanov")
                .rating(5)
                .comment("Excellent natural honey.")
                .build();

        when(productReviewService.createReview(request))
                .thenReturn(createdReview);

        ResponseEntity<ReviewResponse> response =
                productReviewController.createReview(request);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );
        assertEquals(createdReview, response.getBody());

        verify(productReviewService).createReview(request);
    }

    @Test
    void updateReviewShouldReturnUpdatedReview() {
        UUID reviewId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setUserId(userId);
        request.setRating(4);
        request.setComment("The updated review comment.");

        ReviewResponse updatedReview = ReviewResponse.builder()
                .id(reviewId)
                .productId(productId)
                .userId(userId)
                .authorName("Ivan Ivanov")
                .rating(4)
                .comment("The updated review comment.")
                .build();

        when(productReviewService.updateReview(reviewId, request))
                .thenReturn(updatedReview);

        ResponseEntity<ReviewResponse> response =
                productReviewController.updateReview(
                        reviewId,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedReview, response.getBody());

        verify(productReviewService)
                .updateReview(reviewId, request);
    }

    @Test
    void deleteReviewShouldReturnNoContent() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ResponseEntity<Void> response =
                productReviewController.deleteReview(
                        reviewId,
                        userId
                );

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );
        assertEquals(null, response.getBody());

        verify(productReviewService)
                .deleteReview(reviewId, userId);
    }
}
