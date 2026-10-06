import meteordevelopment.meteorclient.utils.misc.TextWrapping;
import java.util.List;

public class TextWrappingSmoke {
    public static void main(String[] args) {
        String cjk = "\u4e2d\u6587\u957f\u53e5\u6362\u884c\u6d4b\u8bd5";
        assert String.join("", TextWrapping.wrap(cjk, 3, String::length)).equals(cjk);
        assert TextWrapping.wrap("abcdefgh", 3, String::length).equals(List.of("abc", "def", "gh"));
        assert TextWrapping.wrap("hello world", 6, String::length).equals(List.of("hello", "world"));
        assert TextWrapping.wrap("a\nb", 3, String::length).equals(List.of("a", "b"));
        assert TextWrapping.wrap("ab", 0, String::length).equals(List.of("a", "b"));
        assert TextWrapping.wrap("\ud83d\ude00\ud83d\ude00", 1, String::length).equals(List.of("\ud83d\ude00", "\ud83d\ude00"));
        System.out.println("TEXT_WRAPPING_PASS chinese oversized newline zeroWidth surrogatePairs");
    }
}
