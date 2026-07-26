package app.review.repository;

import app.review.model.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    List<ProductReview> findAllByProductIdOrderByCreatedOnDesc(UUID productId);

    Optional<ProductReview> findByProductIdAndUserId(UUID productId, UUID userId);
}
