package meteordevelopment.meteorclient.utils.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public final class ChineseTranslations {
    private static final Properties TRANSLATIONS = new Properties();
    private static final boolean CHINESE = !"en_us".equals(System.getProperty("meteor.locale", "zh_cn"));

    static {
        try (InputStream input = ChineseTranslations.class.getResourceAsStream("/assets/meteor-client/lang/zh_cn.properties")) {
            if (input == null) throw new IllegalStateException("Missing Chinese translations");
            TRANSLATIONS.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load Chinese translations", e);
        }
    }

    private ChineseTranslations() {}

    public static boolean isChinese() { return CHINESE; }

    public static String get(String key, String fallback) {
        return CHINESE ? TRANSLATIONS.getProperty(key, fallback) : fallback;
    }

    public static String text(String english) {
        if (english == null || !CHINESE) return english;
        String translated = TRANSLATIONS.getProperty("ui." + english.toLowerCase(Locale.ROOT).replace(' ', '-'));
        if (translated != null) return translated;
        String label = english.stripTrailing();
        if (label.endsWith(":")) {
            label = label.substring(0, label.length() - 1);
            translated = TRANSLATIONS.getProperty("ui." + label.toLowerCase(Locale.ROOT).replace(' ', '-'));
            if (translated != null) return translated + english.substring(label.length());
        }
        return english;
    }
}
