package app.review.service;

import app.review.exception.InvalidReviewDataException;
import app.review.exception.ReviewAlreadyExistsException;
import app.review.exception.ReviewNotFoundException;
import app.review.exception.UnauthorizedReviewOperationException;
import app.review.model.entity.ProductReview;
import app.review.repository.ProductReviewRepository;
import app.review.web.dto.CreateReviewRequest;
import app.review.web.dto.ReviewResponse;
import app.review.web.dto.UpdateReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReviewService {

    private final ProductReviewRepository productReviewRepository;

    public List<ReviewResponse> getReviewsByProductId(UUID productId) {

        if (productId == null) {
            throw new InvalidReviewDataException(
                    "Product id is required."
            );
        }

        return productReviewRepository
                .findAllByProductIdOrderByCreatedOnDesc(productId)
                .stream()
                .map(this::mapToReviewResponse)
                .toList();
    }

    private ReviewResponse mapToReviewResponse(ProductReview review) {

        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .authorName(review.getAuthorName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdOn(review.getCreatedOn())
                .updatedOn(review.getUpdatedOn())
                .build();
    }

    public ReviewResponse createReview(CreateReviewRequest request) {

        validateCreateRequest(request);

        productReviewRepository
                .findByProductIdAndUserId(request.getProductId(), request.getUserId())
                .ifPresent(review -> {
                    throw new ReviewAlreadyExistsException(
                            "The user has already reviewed this product."
                    );
                });

        LocalDateTime now = LocalDateTime.now();

        ProductReview review = ProductReview.builder()
                .productId(request.getProductId())
                .userId(request.getUserId())
                .authorName(request.getAuthorName().trim())
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .createdOn(now)
                .updatedOn(now)
                .build();

        ProductReview savedReview = productReviewRepository.save(review);

        log.info(
                "Created review [{}] for product [{}] by user [{}].",
                savedReview.getId(),
                savedReview.getProductId(),
                savedReview.getUserId()
        );

        return mapToReviewResponse(savedReview);
    }

    public ReviewResponse updateReview(
            UUID reviewId,
            UpdateReviewRequest request) {

        if (reviewId == null) {
            throw new InvalidReviewDataException(
                    "Review id is required."
            );
        }

        validateUpdateRequest(request);

        ProductReview review = productReviewRepository
                .findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review with ID " + reviewId + " was not found."
                ));

        if (!review.getUserId().equals(request.getUserId())) {
            throw new UnauthorizedReviewOperationException(
                    "You cannot update another user's review."
            );
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        review.setUpdatedOn(LocalDateTime.now());

        ProductReview updatedReview = productReviewRepository.save(review);
        log.info(
                "Updated review [{}] by user [{}].",
                updatedReview.getId(),
                updatedReview.getUserId()
        );

        return mapToReviewResponse(updatedReview);
    }

    public void deleteReview(UUID reviewId, UUID userId) {

        if (reviewId == null) {
            throw new InvalidReviewDataException(
                    "Review id is required."
            );
        }

        if (userId == null) {
            throw new InvalidReviewDataException(
                    "User id is required."
            );
        }

        ProductReview review = productReviewRepository
                .findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review with ID " + reviewId + " was not found."
                ));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedReviewOperationException(
                    "You cannot delete another user's review."
            );
        }

        productReviewRepository.delete(review);

        log.info(
                "Deleted review [{}] for product [{}] by user [{}].",
                review.getId(),
                review.getProductId(),
                review.getUserId()
        );
    }

    private void validateCreateRequest(
            CreateReviewRequest request) {

        if (request == null) {
            throw new InvalidReviewDataException(
                    "Review request is required."
            );
        }

        if (request.getProductId() == null) {
            throw new InvalidReviewDataException(
                    "Product id is required."
            );
        }

        if (request.getUserId() == null) {
            throw new InvalidReviewDataException(
                    "User id is required."
            );
        }

        if (request.getAuthorName() == null
                || request.getAuthorName().isBlank()) {

            throw new InvalidReviewDataException(
                    "Author name is required."
            );
        }

        String authorName = request.getAuthorName().trim();

        if (authorName.length() < 3
                || authorName.length() > 50) {

            throw new InvalidReviewDataException(
                    "Author name must be between 3 and 50 symbols."
            );
        }

        validateReviewContent(
                request.getRating(),
                request.getComment()
        );
    }

    private void validateUpdateRequest(
            UpdateReviewRequest request) {

        if (request == null) {
            throw new InvalidReviewDataException(
                    "Review update request is required."
            );
        }

        if (request.getUserId() == null) {
            throw new InvalidReviewDataException(
                    "User id is required."
            );
        }

        validateReviewContent(
                request.getRating(),
                request.getComment()
        );
    }

    private void validateReviewContent(
            Integer rating,
            String comment) {

        if (rating == null) {
            throw new InvalidReviewDataException(
                    "Rating is required."
            );
        }

        if (rating < 1 || rating > 5) {
            throw new InvalidReviewDataException(
                    "Rating must be between 1 and 5."
            );
        }

        if (comment == null || comment.isBlank()) {
            throw new InvalidReviewDataException(
                    "Comment is required."
            );
        }

        String trimmedComment = comment.trim();

        if (trimmedComment.length() < 10
                || trimmedComment.length() > 1000) {

            throw new InvalidReviewDataException(
                    "Comment must be between 10 and 1000 symbols."
            );
        }
    }
}
