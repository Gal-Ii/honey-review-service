package app.review.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReviewResponse {

    private UUID id;

    private UUID productId;

    private UUID userId;

    private String authorName;

    private Integer rating;

    private String comment;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
