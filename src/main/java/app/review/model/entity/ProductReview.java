package app.review.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "product_reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_review_product_user",
                columnNames = {"product_id", "user_id"}
        )
)
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Product id is required.")
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @NotNull(message = "User id is required.")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank(message = "Author name is required.")
    @Size(min = 3, max = 50,
            message = "Author name must be between 3 and 50 symbols.")
    @Column(name = "author_name", nullable = false, length = 50)
    private String authorName;

    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating must be at most 5.")
    @Column(nullable = false)
    private Integer rating;

    @NotBlank(message = "Comment is required.")
    @Size(min = 10, max = 1000,
            message = "Comment must be between 10 and 1000 symbols.")
    @Column(name = "review_comment", nullable = false, length = 1000)
    private String comment;

    @NotNull(message = "Creation date is required.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @NotNull(message = "Update date is required.")
    @Column(nullable = false)
    private LocalDateTime updatedOn;
}
