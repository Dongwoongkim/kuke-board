package kuke.board.like.service;


import jakarta.transaction.Transactional;
import kuke.board.common.snowflake.Snowflake;
import kuke.board.like.entity.ArticleLike;
import kuke.board.like.entity.ArticleLikeCount;
import kuke.board.like.repository.ArticleLikeCountRepository;
import kuke.board.like.repository.ArticleLikeRepository;
import kuke.board.like.service.response.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {

    private final Snowflake snowflake = new Snowflake();
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleLikeCountRepository articleLikeCountRepository;

    public ArticleLikeResponse read(Long articleId, Long userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
            .map(ArticleLikeResponse::from)
            .orElseThrow();
    }

    /**
     * update 구문 - Pessimistic Lock 1
     */
    @Transactional
    public void likePessimisticLock1(Long articleId, Long userId) {
        articleLikeRepository.save(
            ArticleLike.create(snowflake.nextId(),
                articleId,
                userId)
        );

        int result = articleLikeCountRepository.increase(articleId);

        if (result == 0) {
            // 최초 요청시 생성
            // 트래픽이 순식간에 몰릴 수 있는 상황에서는 유실될 수 있으므로, 게시글 생성 시점에 미리 0으로 초기화 해둘 수 있음
            articleLikeCountRepository.save(
                ArticleLikeCount.init(articleId, 1L)
            );
        }
    }

    /**
     * update 구문 - Pessimistic Lock 1
     */
    @Transactional
    public void unlikePessimisticLock1(Long articleId, Long userId) {
//        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
//            .ifPresent(() -> {
//                int d = articleLikeRepository.deleteByArticleIdAndUserId(articleId, userId);
//                articleLikeCountRepository.decrease(articleId);
//            });
    }

    /**
     * select ... for update + update 구문 - Pessimistic Lock 2
     */
    @Transactional
    public void likePessimisticLock2(Long articleId, Long userId) {
        articleLikeRepository.save(
            ArticleLike.create(snowflake.nextId(),
                articleId,
                userId)
        );

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
            .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));

        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount);
    }

    /**
     * select ... for update + update 구문 - Pessimistic Lock 2
     */
    @Transactional
    public void unlikePessimisticLock2(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
            .ifPresent(articleLike -> {
                articleLikeRepository.delete(articleLike);
                ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId).orElseThrow();
                articleLikeCount.decrease();
            });
    }

    /**
     * update 구문 - Optimistic Lock
     */
    @Transactional
    public void likeOptimisticLock(Long articleId, Long userId) {
        articleLikeRepository.save(
            ArticleLike.create(snowflake.nextId(),
                articleId,
                userId)
        );

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId)
            .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount);
    }

    /**
     * update 구문 - Optimistic Lock
     */
    @Transactional
    public void unlikeOptimisticLock(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
            .ifPresent(articleLike -> {
                articleLikeRepository.delete(articleLike);
                articleLikeCountRepository.decrease(articleId);
            });
    }

    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
            .map(ArticleLikeCount::getLikeCount)
            .orElse(0L);
    }
}
