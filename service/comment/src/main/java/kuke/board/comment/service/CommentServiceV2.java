package kuke.board.comment.service;

import static java.util.function.Predicate.not;

import jakarta.transaction.Transactional;
import java.util.List;
import kuke.board.comment.entity.CommentPath;
import kuke.board.comment.entity.CommentV2;
import kuke.board.comment.repository.CommentRepositoryV2;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentPageResponseV2;
import kuke.board.comment.service.response.CommentResponseV2;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceV2 {

    private final Snowflake snowflake = new Snowflake();
    private final CommentRepositoryV2 commentRepository;

    @Transactional
    public CommentResponseV2 create(CommentCreateRequestV2 request) {
        CommentV2 parent = findParent(request);
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        CommentV2 comment = commentRepository.save(
            CommentV2.create(
                snowflake.nextId(),
                request.getContent(),
                request.getArticleId(),
                request.getWriterId(),
                parentCommentPath.createChildCommentPath(
                    commentRepository.findDescendantTopPath(request.getArticleId(), parentCommentPath.getPath())
                        .orElse(null)
                )
            )
        );

        return CommentResponseV2.from(comment);
    }

    private CommentV2 findParent(CommentCreateRequestV2 request) {
        String parentPath = request.getParentPath();
        if (parentPath == null) {
            return null;
        }

        return commentRepository.findByPath(parentPath)
            .filter(not(CommentV2::getDeleted))
            .orElseThrow();
    }

    public CommentResponseV2 read(Long commentId) {
        return CommentResponseV2.from(
            commentRepository.findById(commentId)
                .orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
            .filter(not(CommentV2::getDeleted))
            .ifPresent(commentV2 -> {
                if (hasChildren(commentV2)) {
                    commentV2.delete();
                } else {
                    delete(commentV2);
                }
            });
    }

    private boolean hasChildren(CommentV2 commentV2) {
        return commentRepository.findDescendantTopPath(
            commentV2.getArticleId(),
            commentV2.getCommentPath().getPath()
        ).isPresent();
    }

    private void delete(CommentV2 comment) {
        commentRepository.delete(comment);

        // 부모댓글의 상태가 delete라면 상위 댓글 재귀적으로 DB에서 삭제
        if (!comment.isRoot()) {
            commentRepository.findByPath(comment.getCommentPath().getParentPath())
                .filter(CommentV2::getDeleted)
                .filter(not(this::hasChildren))
                .ifPresent(this::delete);
        }
    }

    public CommentPageResponseV2 readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponseV2.of(
            commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize)
                .stream()
                .map(CommentResponseV2::from)
                .toList(),
            commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponseV2> readAllInfiniteScroll(Long articleId, String lastCommentPath, Long pageSize) {
        List<CommentV2> comments = lastCommentPath == null ?
            commentRepository.findAllInfiniteScroll(articleId, pageSize) :
            commentRepository.findAllInfiniteScroll(articleId, lastCommentPath, pageSize);

        return comments.stream()
            .map(CommentResponseV2::from)
            .toList();
    }

}
