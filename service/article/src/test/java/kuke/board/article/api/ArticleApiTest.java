package kuke.board.article.api;

import java.util.List;
import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

public class ArticleApiTest {

    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest("HI", "My Content", 1L, 1L));
        System.out.println("response = " + response);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
            .uri("/v1/articles")
            .body(request)
            .retrieve()
            .body(ArticleResponse.class);
    }

    @Test
    void readTest() {
        ArticleResponse response = read(185373113958047744L);
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
            .uri("/v1/articles/{articleId}", articleId)
            .retrieve()
            .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(185373113958047744L);
        ArticleResponse response = read(185373113958047744L);
        System.out.println("response = " + response);
    }

    void update(Long articleId) {
        restClient.put()
            .uri("/v1/articles/{articleId}", articleId)
            .body(new ArticleUpdateRequest("abc", "cont"))
            .retrieve();
    }

    @Test
    void deleteTest() {
        delete(185373113958047744L);
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
            .uri("/v1/articles?boardId=1&pageSize=30&page=50000")
            .retrieve()
            .body(ArticlePageResponse.class);

        System.out.println("response.getArticleCount() = " + response.getArticleCount());
        for (ArticleResponse article : response.getArticles()) {
            System.out.println("article.getId() = " + article.getArticleId());
        }
    }

    void delete(Long articleId) {
        restClient.delete()
            .uri("/v1/articles/{articleId}", articleId)
            .retrieve();
    }

    @Test
    void readAllInfiniteScrollTest() {
        List<ArticleResponse> response = restClient.get()
            .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5")
            .retrieve()
            .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
            });

        System.out.println("firstPage");
        for (ArticleResponse article : response) {
            System.out.println("article.getId() = " + article.getArticleId());
        }

        Long lastArticleId = response.getLast().getArticleId();

        List<ArticleResponse> response2 = restClient.get()
            .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5&lastArticleId=%s".formatted(lastArticleId))
            .retrieve()
            .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
            });

        System.out.println("secondPage");

        for (ArticleResponse articleResponse : response2) {
            System.out.println("articleResponse.getId() = " + articleResponse.getArticleId());
        }
    }

    @Test
    void countTest() {
        ArticleResponse response = create(new ArticleCreateRequest("HI", "My Content", 1L, 2L));

        Long count1 = restClient.get()
            .uri("/v1/articles/boards/{boardId}/count", 2L)
            .retrieve()
            .body(Long.class);

        System.out.println("count1 = " + count1);

        restClient.delete()
            .uri("/v1/articles/{articleId}", response.getArticleId())
            .retrieve()
            .body(Long.class);

        Long count2 = restClient.get()
            .uri("/v1/articles/boards/{boardId}/count", 2L)
            .retrieve()
            .body(Long.class);
        
        System.out.println("count2 = " + count2);
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {

        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {

        private String title;
        private String content;
    }
}
