package kuke.board.comment.api;

import java.util.List;
import kuke.board.comment.service.response.CommentPageResponseV2;
import kuke.board.comment.service.response.CommentResponseV2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

public class CommentApiV2Test {

    RestClient restClient = RestClient.create("http://localhost:9002");

    @Test
    void create() {
        CommentResponseV2 response = create(new CommentCreateRequestV2(1L, "content1", null, 1L));
        CommentResponseV2 response2 = create(new CommentCreateRequestV2(1L, "content2", response.getPath(), 1L));
        CommentResponseV2 response3 = create(new CommentCreateRequestV2(1L, "content3", response2.getPath(), 1L));

        System.out.println("response.getCommentId() = " + response.getCommentId());
        System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
        System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());

        System.out.println("response.getPath() = " + response.getPath());
        System.out.println("\tresponse.getPath() = " + response2.getPath());
        System.out.println("\t\tresponse.getPath() = " + response3.getPath());
    }

    @Test
    void read() {
        CommentResponseV2 response = restClient.get()
            .uri("/v2/comments/{commentId}", 189207609197596672L)
            .retrieve()
            .body(CommentResponseV2.class);

        System.out.println("response = " + response);
    }

    CommentResponseV2 create(CommentCreateRequestV2 request) {
        return restClient.post()
            .uri("/v2/comments")
            .body(request)
            .retrieve()
            .body(CommentResponseV2.class);
    }

    @Test
    void delete() {
        restClient.delete()
            .uri("/v2/comments/{commentId}", 189207609197596672L)
            .retrieve();
    }

    @Test
    void readAll() {
        CommentPageResponseV2 response = restClient.get()
            .uri("/v2/comments?articleId=1&pageSize=10&page=1")
            .retrieve()
            .body(CommentPageResponseV2.class);

        /**
         * response.getCommentCount() = 101
         * comment.getCommentId() = 189211559717212169
         * comment.getCommentId() = 189211559738183689
         * comment.getCommentId() = 189211559738183695
         * comment.getCommentId() = 189211559738183705
         * comment.getCommentId() = 189211559742378001
         * comment.getCommentId() = 189211559742378011
         * comment.getCommentId() = 189211559742378044
         * comment.getCommentId() = 189211559742378055
         * comment.getCommentId() = 189211559742378062
         * comment.getCommentId() = 189211559742378071
         */
        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponseV2 comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponseV2> response1 = restClient.get()
            .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
            .retrieve()
            .body(new ParameterizedTypeReference<List<CommentResponseV2>>() {
            });

        System.out.println("first-page");
        for (CommentResponseV2 comment : response1) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        String lastCommentPath = response1.getLast().getPath();

        System.out.println(lastCommentPath);

        List<CommentResponseV2> response2 = restClient.get()
            .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastCommentPath=%s".formatted(lastCommentPath))
            .retrieve()
            .body(new ParameterizedTypeReference<List<CommentResponseV2>>() {
            });

        System.out.println("second-page");
        for (CommentResponseV2 comment : response2) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    @Test
    void countTest() {
        CommentResponseV2 response = create(new CommentCreateRequestV2(10L, "content1", null, 1L));

        Long count1 = restClient.get()
            .uri("/v2/comments/articles/{articleId}/count", 10L)
            .retrieve()
            .body(Long.class);

        restClient.delete()
            .uri("/v2/comments/{commentId}", response.getCommentId())
            .retrieve();

        Long count2 = restClient.get()
            .uri("/v2/comments/articles/{articleId}/count", 10L)
            .retrieve()
            .body(Long.class);

        System.out.println("count1 = " + count1);
        System.out.println("count2 = " + count2);
    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequestV2 {

        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}
