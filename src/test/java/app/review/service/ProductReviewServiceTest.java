package app.review.service;

import app.review.repository.ProductReviewRepository;
import app.review.model.entity.ProductReview;
import app.review.web.dto.ReviewResponse;
import app.review.web.dto.CreateReviewRequest;
import app.review.web.dto.UpdateReviewRequest;
import app.review.exception.ReviewAlreadyExistsException;
import app.review.exception.ReviewNotFoundException;
import app.review.exception.UnauthorizedReviewOperationException;
import app.review.exception.InvalidReviewDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceTest {

    @Mock
    private ProductReviewRepository productReviewRepository;

    private ProductReviewService productReviewService;

    @BeforeEach
    void setUp() {
        productReviewService =
                new ProductReviewService(productReviewRepository);
    }

    @Test
    void getReviewsByProductIdShouldReturnMappedReviews() {
        UUID reviewId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdOn = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedOn = LocalDateTime.now();

        ProductReview review = ProductReview.builder()
                .id(reviewId)
                .productId(productId)
                .userId(userId)
                .authorName("Ivan Ivanov")
                .rating(5)
                .comment("Excellent natural honey.")
                .createdOn(createdOn)
                .updatedOn(updatedOn)
                .build();

        when(productReviewRepository
                .findAllByProductIdOrderByCreatedOnDesc(productId))
                .thenReturn(List.of(review));

        List<ReviewResponse> result =
                productReviewService.getReviewsByProductId(productId);

        assertEquals(1, result.size());

        ReviewResponse response = result.get(0);

        assertEquals(reviewId, response.getId());
        assertEquals(productId, response.getProductId());
        assertEquals(userId, response.getUserId());
        assertEquals("Ivan Ivanov", response.getAuthorName());
        assertEquals(5, response.getRating());
        assertEquals("Excellent natural honey.", response.getComment());
        assertEquals(createdOn, response.getCreatedOn());
        assertEquals(updatedOn, response.getUpdatedOn());

        verify(productReviewRepository)
                .findAllByProductIdOrderByCreatedOnDesc(productId);
    }

    @Test
    void createReviewShouldSaveAndReturnReview() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CreateReviewRequest request = new CreateReviewRequest();
        request.setProductId(productId);
        request.setUserId(userId);
        request.setAuthorName("Ivan Ivanov");
        request.setRating(5);
        request.setComment("Excellent natural honey.");

        when(productReviewRepository
                .findByProductIdAndUserId(productId, userId))
                .thenReturn(Optional.empty());

        when(productReviewRepository.save(any(ProductReview.class)))
                .thenAnswer(invocation -> {
                    ProductReview review = invocation.getArgument(0);
                    review.setId(reviewId);
                    return review;
                });

        ReviewResponse result =
                productReviewService.createReview(request);

        assertNotNull(result);
        assertEquals(reviewId, result.getId());
        assertEquals(productId, result.getProductId());
        assertEquals(userId, result.getUserId());
        assertEquals("Ivan Ivanov", result.getAuthorName());
        assertEquals(5, result.getRating());
        assertEquals("Excellent natural honey.", result.getComment());
        assertNotNull(result.getCreatedOn());
        assertNotNull(result.getUpdatedOn());

        verify(productReviewRepository)
                .findByProductIdAndUserId(productId, userId);

        verify(productReviewRepository)
                .save(any(ProductReview.class));
    }

    @Test
    void createReviewShouldThrowWhenReviewAlreadyExists() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateReviewRequest request = new CreateReviewRequest();
        request.setProductId(productId);
        request.setUserId(userId);
        request.setAuthorName("Ivan Ivanov");
        request.setRating(5);
        request.setComment("Excellent natural honey.");

        ProductReview existingReview = ProductReview.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .userId(userId)
                .authorName("Ivan Ivanov")
                .rating(4)
                .comment("This review already exists.")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(productReviewRepository
                .findByProductIdAndUserId(productId, userId))
                .thenReturn(Optional.of(existingReview));

        ReviewAlreadyExistsException exception =
                assertThrows(
                        ReviewAlreadyExistsException.class,
                        () -> productReviewService.createReview(request)
                );

        assertEquals(
                "The user has already reviewed this product.",
                exception.getMessage()
        );

        verify(productReviewRepository)
                .findByProductIdAndUserId(productId, userId);

        verify(productReviewRepository, never())
                .save(any(ProductReview.class));
    }

    @Test
    void updateReviewShouldUpdateAndReturnReview() {
        UUID reviewId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime oldUpdatedOn = LocalDateTime.now().minusDays(1);

        ProductReview existingReview = ProductReview.builder()
                .id(reviewId)
                .productId(productId)
                .userId(userId)
                .authorName("Ivan Ivanov")
                .rating(3)
                .comment("The original review comment.")
                .createdOn(LocalDateTime.now().minusDays(2))
                .updatedOn(oldUpdatedOn)
                .build();

        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setUserId(userId);
        request.setRating(5);
        request.setComment("The updated review comment.");

        when(productReviewRepository.findById(reviewId))
                .thenReturn(Optional.of(existingReview));

        when(productReviewRepository.save(existingReview))
                .thenReturn(existingReview);

        ReviewResponse result =
                productReviewService.updateReview(reviewId, request);

        assertEquals(reviewId, result.getId());
        assertEquals(productId, result.getProductId());
        assertEquals(userId, result.getUserId());
        assertEquals(5, result.getRating());
        assertEquals(
                "The updated review comment.",
                result.getComment()
        );

        assertNotNull(result.getUpdatedOn());

        verify(productReviewRepository).findById(reviewId);
        verify(productReviewRepository).save(existingReview);
    }

    @Test
    void updateReviewShouldThrowWhenReviewDoesNotExist() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setUserId(userId);
        request.setRating(4);
        request.setComment("The updated review comment.");

        when(productReviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        ReviewNotFoundException exception =
                assertThrows(
                        ReviewNotFoundException.class,
                        () -> productReviewService.updateReview(
                                reviewId,
                                request
                        )
                );

        assertEquals(
                "Review with ID " + reviewId + " was not found.",
                exception.getMessage()
        );

        verify(productReviewRepository).findById(reviewId);

        verify(productReviewRepository, never())
                .save(any(ProductReview.class));
    }

    @Test
    void updateReviewShouldThrowWhenReviewBelongsToAnotherUser() {
        UUID reviewId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        ProductReview existingReview = ProductReview.builder()
                .id(reviewId)
                .productId(UUID.randomUUID())
                .userId(ownerId)
                .authorName("Ivan Ivanov")
                .rating(4)
                .comment("The original review comment.")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setUserId(anotherUserId);
        request.setRating(5);
        request.setComment("Attempt to change another review.");

        when(productReviewRepository.findById(reviewId))
                .thenReturn(Optional.of(existingReview));

        UnauthorizedReviewOperationException exception =
                assertThrows(
                        UnauthorizedReviewOperationException.class,
                        () -> productReviewService.updateReview(
                                reviewId,
                                request
                        )
                );

        assertEquals(
                "You cannot update another user's review.",
                exception.getMessage()
        );

        verify(productReviewRepository).findById(reviewId);

        verify(productReviewRepository, never())
                .save(any(ProductReview.class));
    }

    @Test
    void deleteReviewShouldDeleteReviewWhenUserIsOwner() {
        UUID reviewId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProductReview existingReview = ProductReview.builder()
                .id(reviewId)
                .productId(productId)
                .userId(userId)
                .authorName("Ivan Ivanov")
                .rating(5)
                .comment("Excellent natural honey.")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(productReviewRepository.findById(reviewId))
                .thenReturn(Optional.of(existingReview));

        productReviewService.deleteReview(reviewId, userId);

        verify(productReviewRepository).findById(reviewId);
        verify(productReviewRepository).delete(existingReview);
    }

    @Test
    void deleteReviewShouldThrowWhenReviewDoesNotExist() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(productReviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        ReviewNotFoundException exception =
                assertThrows(
                        ReviewNotFoundException.class,
                        () -> productReviewService.deleteReview(
                                reviewId,
                                userId
                        )
                );

        assertEquals(
                "Review with ID " + reviewId + " was not found.",
                exception.getMessage()
        );

        verify(productReviewRepository).findById(reviewId);

        verify(productReviewRepository, never())
                .delete(any(ProductReview.class));
    }

    @Test
    void deleteReviewShouldThrowWhenReviewBelongsToAnotherUser() {
        UUID reviewId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        ProductReview existingReview = ProductReview.builder()
                .id(reviewId)
                .productId(UUID.randomUUID())
                .userId(ownerId)
                .authorName("Ivan Ivanov")
                .rating(5)
                .comment("Excellent natural honey.")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(productReviewRepository.findById(reviewId))
                .thenReturn(Optional.of(existingReview));

        UnauthorizedReviewOperationException exception =
                assertThrows(
                        UnauthorizedReviewOperationException.class,
                        () -> productReviewService.deleteReview(
                                reviewId,
                                anotherUserId
                        )
                );

        assertEquals(
                "You cannot delete another user's review.",
                exception.getMessage()
        );

        verify(productReviewRepository).findById(reviewId);

        verify(productReviewRepository, never())
                .delete(any(ProductReview.class));
    }

    @Test
    void getReviewsShouldRejectNullProductId() {
        InvalidReviewDataException exception =
                assertThrows(
                        InvalidReviewDataException.class,
                        () -> productReviewService
                                .getReviewsByProductId(null)
                );

        assertEquals(
                "Product id is required.",
                exception.getMessage()
        );

        verifyNoInteractions(productReviewRepository);
    }

    @Test
    void createReviewShouldRejectNullRequest() {
        InvalidReviewDataException exception =
                assertThrows(
                        InvalidReviewDataException.class,
                        () -> productReviewService
                                .createReview(null)
                );

        assertEquals(
                "Review request is required.",
                exception.getMessage()
        );

        verifyNoInteractions(productReviewRepository);
    }

    @Test
    void createReviewShouldRejectMissingProductId() {
        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setUserId(UUID.randomUUID());
        request.setAuthorName("Ivan Ivanov");
        request.setRating(5);
        request.setComment(
                "Excellent natural honey."
        );

        InvalidReviewDataException exception =
                assertThrows(
                        InvalidReviewDataException.class,
                        () -> productReviewService
                                .createReview(request)
                );

        assertEquals(
                "Product id is required.",
                exception.getMessage()
        );

        verifyNoInteractions(productReviewRepository);
    }

    @Test
    void createReviewShouldRejectInvalidAuthorName() {
        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setProductId(UUID.randomUUID());
        request.setUserId(UUID.randomUUID());
        request.setAuthorName("A");
        request.setRating(5);
        request.setComment(
                "Excellent natural honey."
        );

        InvalidReviewDataException exception =
                assertThrows(
                        InvalidReviewDataException.class,
                        () -> productReviewService
                                .createReview(request)
                );

        assertEquals(
                "Author name must be between 3 and 50 symbols.",
                exception.getMessage()
        );

        verifyNoInteractions(productReviewRepository);
    }

    @Test
    void createReviewShouldRejectInvalidRating() {
        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setProductId(UUID.randomUUID());
        request.setUserId(UUID.randomUUID());
        request.setAuthorName("Ivan Ivanov");
        request.setRating(6);
        request.setComment(
                "Excellent natural honey."
        );

        InvalidReviewDataException exception =
                assertThrows(
                        InvalidReviewDataException.class,
                        () -> productReviewService
                                .createReview(request)
                );

        assertEquals(
                "Rating must be between 1 and 5.",
                exception.getMessage()
        );

        verifyNoInteractions(productReviewRepository);
    }

    @Test
    void createReviewShouldRejectShortComment() {
        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setProductId(UUID.randomUUID());
        request.setUserId(UUID.randomUUID());
        request.setAuthorName("Ivan Ivanov");
        request.setRating(5);
        request.setComment("Short");

        InvalidReviewDataException exception =
                assertThrows(
                        InvalidReviewDataException.class,
                        () -> productReviewService
                                .createReview(request)
                );

        assertEquals(
                "Comment must be between 10 and 1000 symbols.",
                exception.getMessage()
        );

        verifyNoInteractions(productReviewRepository);
    }

    @Test
    void updateReviewShouldRejectNullReviewId() {
        UpdateReviewRequest request =
                new UpdateReviewRequest();

        request.setUserId(UUID.randomUUID());
        request.setRating(5);
        request.setComment(
                "Excellent natural honey."
        );

        InvalidReviewDataException exception =
                assertThrows(
                        InvalidReviewDataException.class,
                        () -> productReviewService
                                .updateReview(null, request)
                );

        assertEquals(
                "Review id is required.",
                exception.getMessage()
        );

        verifyNoInteractions(productReviewRepository);
    }

    @Test
    void updateReviewShouldRejectNullRequest() {
        InvalidReviewDataException exception =
                assertThrows(
                        InvalidReviewDataException.class,
                        () -> productReviewService.updateReview(
                                UUID.randomUUID(),
                                null
                        )
                );

        assertEquals(
                "Review update request is required.",
                exception.getMessage()
        );

        verifyNoInteractions(productReviewRepository);
    }

    @Test
    void deleteReviewShouldRejectNullUserId() {
        InvalidReviewDataException exception =
                assertThrows(
                        InvalidReviewDataException.class,
                        () -> productReviewService.deleteReview(
                                UUID.randomUUID(),
                                null
                        )
                );

        assertEquals(
                "User id is required.",
                exception.getMessage()
        );

        verifyNoInteractions(productReviewRepository);
    }
}
