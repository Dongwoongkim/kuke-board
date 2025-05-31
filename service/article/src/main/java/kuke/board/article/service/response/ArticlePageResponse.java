package kuke.board.article.service.response;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ArticlePageResponse {

	private List<ArticleResponse> articles;
	private Long articleCount;

	public static ArticlePageResponse of(List<ArticleResponse> articles, Long articleCount) {
		ArticlePageResponse res = new ArticlePageResponse();
		res.articles = articles;
		res.articleCount = articleCount;
		return res;
	}
}
