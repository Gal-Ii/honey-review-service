package app.review.service;

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
                .authorName(request.getAuthorName())
                .rating(request.getRating())
                .comment(request.getComment())
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
        review.setComment(request.getComment());
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
}
