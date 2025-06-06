package kuke.board.comment.service.response;

import java.util.List;
import lombok.Getter;

@Getter
public class CommentPageResponseV2 {

    private List<CommentResponseV2> comments;
    private Long commentCount;

    public static CommentPageResponseV2 of(List<CommentResponseV2> comments, Long commentCount) {
        CommentPageResponseV2 response = new CommentPageResponseV2();
        response.comments = comments;
        response.commentCount = commentCount;
        return response;
    }
}
