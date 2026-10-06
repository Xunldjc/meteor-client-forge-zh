package meteordevelopment.meteorclient.utils.misc;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

public final class TextWrapping {
    private TextWrapping() {}

    public static List<String> wrap(String text, double maxWidth, ToDoubleFunction<String> width) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\\R", -1)) {
            BreakIterator breaks = BreakIterator.getLineInstance(Locale.ROOT);
            breaks.setText(paragraph);
            String line = "";
            int start = breaks.first();
            for (int end = breaks.next(); end != BreakIterator.DONE; start = end, end = breaks.next()) {
                String token = paragraph.substring(start, end);
                if (!line.isEmpty() && width.applyAsDouble((line + token).stripTrailing()) > maxWidth) {
                    lines.add(line.stripTrailing());
                    line = "";
                }
                if (line.isEmpty()) token = token.stripLeading();
                // Split oversized tokens at code-point boundaries, always consuming input.
                if (width.applyAsDouble(token.stripTrailing()) > maxWidth) {
                    for (int i = 0; i < token.length();) {
                        int next = i + Character.charCount(token.codePointAt(i));
                        String character = token.substring(i, next);
                        if (!line.isEmpty() && width.applyAsDouble(line + character) > maxWidth) {
                            lines.add(line.stripTrailing());
                            line = "";
                        }
                        line += character;
                        i = next;
                    }
                } else line += token;
            }
            lines.add(line.stripTrailing());
        }
        return lines;
    }
}
