package app.review.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CreateReviewRequest {
    @NotNull(message = "Product ID is required.")
    private UUID productId;

    @NotNull(message = "User ID is required.")
    private UUID userId;

    @NotBlank(message = "Author name is required.")
    @Size(max = 50, message = "Author name must be at most 50 symbols.")
    private String authorName;

    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating must be at most 5.")
    private Integer rating;

    @NotBlank(message = "Comment is required.")
    @Size(
            min = 10,
            max = 1000,
            message = "Comment must be between 10 and 1000 symbols."
    )
    private String comment;

}
