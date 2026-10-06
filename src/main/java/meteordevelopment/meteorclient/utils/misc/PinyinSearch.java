package meteordevelopment.meteorclient.utils.misc;

import java.text.Normalizer;
import java.util.*;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.*;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/** Search-only aliases: display text, registry IDs and saved identifiers are unchanged. */
public final class PinyinSearch {
    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();
    private static final Map<String, Index> CACHE = Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Index> entry) { return size() > 4096; }
    });

    static {
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    private PinyinSearch() {}

    public static boolean matches(String text, String query, String... aliases) {
        if (query == null || query.isBlank()) return true;
        if (matchesOne(text, query)) return true;
        for (String alias : aliases) if (matchesOne(alias, query)) return true;
        return false;
    }

    private static boolean matchesOne(String text, String query) {
        if (text == null) return false;
        String source = normalize(text);
        String filter = normalize(query).trim();
        if (source.contains(filter)) return true;
        Index index = CACHE.get(source);
        if (index == null) {
            index = index(source);
            CACHE.put(source, index);
        }
        // Keep the existing all-words semantics, also accepting full pinyin and initials.
        for (String word : filter.split("\\s+")) {
            if (source.contains(word)) continue;
            String compact = word.replaceAll("[\\s_'\\-]", "");
            if (compact.isEmpty() || compact.length() > 256 || (!contains(index.full, compact) && !contains(index.initials, compact))) return false;
        }
        return true;
    }

    private static String normalize(String text) {
        String value = text.toLowerCase(Locale.ROOT).replace("u:", "v").replace("ü", "v");
        return Normalizer.normalize(value, Normalizer.Form.NFKD).replaceAll("\\p{M}", "")
            .replaceAll("§[0-9a-fk-or]", "");
    }

    private static Index index(String source) {
        List<String[]> full = new ArrayList<>();
        List<String[]> initials = new ArrayList<>();
        source.codePoints().forEach(codePoint -> {
            String[] pronunciations = null;
            if (Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN && Character.isBmpCodePoint(codePoint)) {
                try { pronunciations = PinyinHelper.toHanyuPinyinStringArray((char) codePoint, FORMAT); }
                catch (BadHanyuPinyinOutputFormatCombination e) { throw new IllegalStateException(e); }
            }
            if (pronunciations != null && pronunciations.length > 0) {
                String[] values = Arrays.stream(pronunciations).distinct().toArray(String[]::new);
                full.add(values);
                initials.add(Arrays.stream(values).map(value -> value.substring(0, 1)).distinct().toArray(String[]::new));
            } else if (Character.isLetterOrDigit(codePoint)) {
                String literal = new String(Character.toChars(codePoint));
                full.add(new String[]{literal});
                initials.add(new String[]{literal});
            }
        });
        return new Index(full, initials);
    }

    // Match a pronunciation graph without enumerating exponentially many polyphonic strings.
    private static boolean contains(List<String[]> segments, String query) {
        boolean[] prefixes = new boolean[query.length()];
        for (String[] alternatives : segments) {
            boolean[] ends = new boolean[query.length()];
            for (String syllable : alternatives) {
                boolean[] states = prefixes.clone();
                for (int c = 0; c < syllable.length(); c++) {
                    boolean[] next = new boolean[query.length()];
                    char letter = syllable.charAt(c);
                    for (int i = 0; i < query.length(); i++) {
                        if ((i == 0 || states[i]) && letter == query.charAt(i)) {
                            if (i + 1 == query.length()) return true;
                            next[i + 1] = true;
                        }
                    }
                    states = next;
                }
                for (int i = 0; i < ends.length; i++) ends[i] |= states[i];
            }
            prefixes = ends;
        }
        return false;
    }

    private record Index(List<String[]> full, List<String[]> initials) {}
}
