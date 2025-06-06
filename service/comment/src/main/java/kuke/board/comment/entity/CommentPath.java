package kuke.board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {

    private static final int DEPTH_CHUNK_SIZE = 5;
    private static final int MAX_DEPTH = 5;
    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    /**
     * MIN_CHUNK = 00000, MAX_CHUNK = zzzzz
     */
    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE);

    private String path;

    public static CommentPath create(String path) {
        if (isDepthOverFlowed(path)) {
            throw new IllegalStateException("Depth overflow");
        }

        CommentPath commentPath = new CommentPath();
        commentPath.path = path;

        return commentPath;
    }

    private static boolean isDepthOverFlowed(String path) {
        return calDepth(path) > MAX_DEPTH;
    }

    private static int calDepth(String path) {
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    public int getDepth() {
        return calDepth(path);
    }

    public boolean isRoot() {
        return calDepth(path) == 1;
    }

    public String getParentPath() {
        // if path is "zzzzz abcde 00000", return "zzzzz abcde"
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    public CommentPath createChildCommentPath(String descendantTopPath) {
        if (descendantTopPath == null) {
            return CommentPath.create(path + MIN_CHUNK); // path + 00000
        }
        String childrenTopPath = findChildrenTopPath(descendantTopPath);
        return CommentPath.create(increase(childrenTopPath));
    }

    private String findChildrenTopPath(String descendantTopPath) {
        return descendantTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    private String increase(String path) {
        // 00000 00000 -> 00000 00001
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);

        if (isChunkOverFlowed(lastChunk)) {
            throw new IllegalStateException("chunk overflow");
        }

        int charsetLength = CHARSET.length();

        int value = 0;
        for (char c : lastChunk.toCharArray()) {
            value = value * charsetLength + CHARSET.indexOf(c);
        }

        value = value + 1;

        String result = "";
        for (int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
            result = CHARSET.charAt(value % charsetLength) + result;
            value /= charsetLength;
        }

        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    private boolean isChunkOverFlowed(String lastChunk) {
        return MAX_CHUNK.equals(lastChunk);
    }
}
