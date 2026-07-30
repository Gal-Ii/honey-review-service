package app.review.repository;

import app.review.model.entity.ProductReview;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class ProductReviewRepositoryIntegrationTest {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Test
    void findByProductIdAndUserIdShouldReturnReview() {

        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProductReview review = createReview(
                productId,
                userId,
                "First review",
                LocalDateTime.now()
        );

        ProductReview savedReview =
                productReviewRepository.saveAndFlush(review);

        Optional<ProductReview> result =
                productReviewRepository.findByProductIdAndUserId(
                        productId,
                        userId
                );

        assertTrue(result.isPresent());
        assertEquals(savedReview.getId(), result.get().getId());
        assertEquals(productId, result.get().getProductId());
        assertEquals(userId, result.get().getUserId());
    }

    @Test
    void findAllByProductIdShouldReturnNewestReviewsFirst() {

        UUID productId = UUID.randomUUID();

        LocalDateTime olderDate =
                LocalDateTime.now().minusDays(2);

        LocalDateTime newerDate =
                LocalDateTime.now().minusDays(1);

        ProductReview olderReview = createReview(
                productId,
                UUID.randomUUID(),
                "Older review",
                olderDate
        );

        ProductReview newerReview = createReview(
                productId,
                UUID.randomUUID(),
                "Newer review",
                newerDate
        );

        productReviewRepository.saveAllAndFlush(
                List.of(olderReview, newerReview)
        );

        List<ProductReview> reviews =
                productReviewRepository
                        .findAllByProductIdOrderByCreatedOnDesc(productId);

        assertEquals(2, reviews.size());
        assertEquals("Newer review", reviews.get(0).getComment());
        assertEquals("Older review", reviews.get(1).getComment());
    }

    private ProductReview createReview(
            UUID productId,
            UUID userId,
            String comment,
            LocalDateTime createdOn) {

        return ProductReview.builder()
                .productId(productId)
                .userId(userId)
                .authorName("Test User")
                .rating(5)
                .comment(comment)
                .createdOn(createdOn)
                .updatedOn(createdOn)
                .build();
    }
}
