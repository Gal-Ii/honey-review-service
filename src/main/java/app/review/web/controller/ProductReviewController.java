package app.review.web.controller;

import app.review.service.ProductReviewService;
import app.review.web.dto.CreateReviewRequest;
import app.review.web.dto.ReviewResponse;
import app.review.web.dto.UpdateReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @RequestParam UUID productId) {

        List<ReviewResponse> reviews =
                productReviewService.getReviewsByProductId(productId);

        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse review =
                productReviewService.createReview(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(review);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {

        ReviewResponse review =
                productReviewService.updateReview(reviewId, request);

        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @RequestParam("userId") UUID userId) {

        productReviewService.deleteReview(reviewId, userId);

        return ResponseEntity.noContent().build();
    }
}
