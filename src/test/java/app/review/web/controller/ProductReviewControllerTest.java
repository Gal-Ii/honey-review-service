package app.review.web.controller;

import app.review.service.ProductReviewService;
import app.review.web.dto.CreateReviewRequest;
import app.review.web.dto.ReviewResponse;
import app.review.web.dto.UpdateReviewRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductReviewController.class)
class ProductReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductReviewService productReviewService;

    @Test
    void getReviewsShouldReturnReviewsWithOkStatus() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewResponse review = ReviewResponse.builder()
                .id(reviewId)
                .productId(productId)
                .userId(userId)
                .authorName("Ivan Ivanov")
                .rating(5)
                .comment("Excellent natural honey.")
                .build();

        when(productReviewService.getReviewsByProductId(productId))
                .thenReturn(List.of(review));

        mockMvc.perform(get("/api/v1/reviews")
                        .queryParam("productId", productId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$[0].id")
                        .value(reviewId.toString()))
                .andExpect(jsonPath("$[0].productId")
                        .value(productId.toString()))
                .andExpect(jsonPath("$[0].userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$[0].authorName")
                        .value("Ivan Ivanov"))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].comment")
                        .value("Excellent natural honey."));

        verify(productReviewService)
                .getReviewsByProductId(productId);
    }

    @Test
    void createReviewShouldReturnCreatedReview() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewResponse createdReview = ReviewResponse.builder()
                .id(reviewId)
                .productId(productId)
                .userId(userId)
                .authorName("Ivan Ivanov")
                .rating(5)
                .comment("Excellent natural honey.")
                .build();

        when(productReviewService.createReview(
                any(CreateReviewRequest.class)
        )).thenReturn(createdReview);

        String requestBody = """
                {
                  "productId": "%s",
                  "userId": "%s",
                  "authorName": "Ivan Ivanov",
                  "rating": 5,
                  "comment": "Excellent natural honey."
                }
                """.formatted(productId, userId);

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(reviewId.toString()))
                .andExpect(jsonPath("$.productId")
                        .value(productId.toString()))
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment")
                        .value("Excellent natural honey."));

        verify(productReviewService)
                .createReview(any(CreateReviewRequest.class));
    }

    @Test
    void createReviewShouldReturnJsonErrorForInvalidRequest()
            throws Exception {

        String requestBody = """
                {
                  "productId": "%s",
                  "userId": "%s",
                  "authorName": "Ivan Ivanov",
                  "rating": 6,
                  "comment": "short"
                }
                """.formatted(
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath(
                        "$.message",
                        containsString(
                                "Rating must be at most 5."
                        )
                ))
                .andExpect(jsonPath(
                        "$.message",
                        containsString(
                                "Comment must be between 10 and 1000 symbols."
                        )
                ));

        verify(productReviewService, never())
                .createReview(any(CreateReviewRequest.class));
    }

    @Test
    void updateReviewShouldReturnUpdatedReview() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewResponse updatedReview = ReviewResponse.builder()
                .id(reviewId)
                .productId(productId)
                .userId(userId)
                .authorName("Ivan Ivanov")
                .rating(4)
                .comment("The updated review comment.")
                .build();

        when(productReviewService.updateReview(
                eq(reviewId),
                any(UpdateReviewRequest.class)
        )).thenReturn(updatedReview);

        String requestBody = """
                {
                  "userId": "%s",
                  "rating": 4,
                  "comment": "The updated review comment."
                }
                """.formatted(userId);

        mockMvc.perform(put(
                        "/api/v1/reviews/{reviewId}",
                        reviewId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(reviewId.toString()))
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment")
                        .value("The updated review comment."));

        verify(productReviewService)
                .updateReview(
                        eq(reviewId),
                        any(UpdateReviewRequest.class)
                );
    }

    @Test
    void deleteReviewShouldReturnNoContent() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete(
                        "/api/v1/reviews/{reviewId}",
                        reviewId
                )
                        .queryParam(
                                "userId",
                                userId.toString()
                        ))
                .andExpect(status().isNoContent());

        verify(productReviewService)
                .deleteReview(reviewId, userId);
    }
}