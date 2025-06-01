package kuke.board.comment.api;

import java.util.List;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

public class CommentApiTest {

    RestClient restClient = RestClient.create("http://localhost:9002");

    @Test
    void create() {
        CommentResponse res1 = createComment(
            new CommentCreateRequest(1L, "my content1", null, 1L));
        CommentResponse res2 = createComment(
            new CommentCreateRequest(1L, "my content2", res1.getCommentId(), 1L));
        CommentResponse res3 = createComment(
            new CommentCreateRequest(1L, "my content3", res1.getCommentId(), 1L));

        System.out.println("commentId = %s".formatted(res1.getCommentId()));
        System.out.println("\tcommentId = %s".formatted(res2.getCommentId()));
        System.out.println("\tcommentId = %s".formatted(res3.getCommentId()));

//        commentId = 187126099661692928
//        commentId = 187126099808493568
//        commentId = 187126099846242304

    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
            .uri("/v1/comments/{commentId}", 187126099661692928L)
            .retrieve()
            .body(CommentResponse.class);

        System.out.println("comment = " + response);
    }

    @Test
    void delete() {
//        commentId = 187126099661692928
//        commentId = 187126099808493568
//        commentId = 187126099846242304

        restClient.delete()
            .uri("/v1/comments/{commentId}", 187126099846242304L)
            .retrieve();
    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
            .uri("/v1/comments")
            .body(request)
            .retrieve()
            .body(CommentResponse.class);
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
//            .uri("/v1/comments?articleId=1&page=50000&pageSize=10")
            .uri("/v1/comments?articleId=1&page=1&pageSize=10")
            .retrieve()
            .body(CommentPageResponse.class);

        /**
         * 1번 page 조회 결과
         * response.getCommentCount() = 101
         * comment.getCommentId() = 187127617155825664
         * 	comment.getCommentId() = 187127617189380099
         * comment.getCommentId() = 187127617155825665
         * 	comment.getCommentId() = 187127617189380103
         * comment.getCommentId() = 187127617155825666
         * 	comment.getCommentId() = 187127617189380141
         * comment.getCommentId() = 187127617155825667
         * 	comment.getCommentId() = 187127617189380102
         * comment.getCommentId() = 187127617155825668
         * 	comment.getCommentId() = 187127617189380097
         */
        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            if (!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
            .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
            .retrieve()
            .body(new ParameterizedTypeReference<List<CommentResponse>>() {
            });

        System.out.println("firstPage");
        for (CommentResponse comment : response1) {
            if (!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        Long lastParentCommentId = response1.getLast().getParentCommentId();
        Long lastCommentId = response1.getLast().getCommentId();

        List<CommentResponse> response2 = restClient.get()
            .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s"
                .formatted(lastParentCommentId, lastCommentId))
            .retrieve()
            .body(new ParameterizedTypeReference<List<CommentResponse>>() {
            });

        System.out.println("secondPage");
        for (CommentResponse comment : response2) {
            if (!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequest {

        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }

}
