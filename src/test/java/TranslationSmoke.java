import meteordevelopment.meteorclient.utils.misc.ChineseTranslations;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TranslationSmoke {
    public static void main(String[] args) throws Exception {
        Properties resource = new Properties();
        try (var stream = TranslationSmoke.class.getResourceAsStream("/assets/meteor-client/lang/zh_cn.properties")) {
            resource.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
        boolean chinese = ChineseTranslations.isChinese();
        assert ChineseTranslations.text(null) == null;
        assert ChineseTranslations.text("unchanged-user-input").equals("unchanged-user-input");
        assert ChineseTranslations.get("missing.key", "fallback").equals("fallback");
        assert ChineseTranslations.text("Save").equals(chinese ? "保存" : "Save");
        assert ChineseTranslations.text("Chat Feedback: ").equals(chinese ? "聊天反馈: " : "Chat Feedback: ");
        assert ChineseTranslations.get("module.flight.title", "Flight").equals(chinese ? "飞行" : "Flight");
        assert ChineseTranslations.get("setting.range.title", "Range").equals(chinese ? "范围" : "Range");
        long titles = resource.stringPropertyNames().stream().filter(k -> k.startsWith("module.") && k.endsWith(".title")).count();
        long descriptions = resource.stringPropertyNames().stream().filter(k -> k.startsWith("module.") && k.endsWith(".description")).count();
        assert titles == descriptions;
        assert titles >= 168;
        System.out.println("locale=" + (chinese ? "zh_cn" : "en_us"));
        System.out.println("Save=" + ChineseTranslations.text("Save"));
        System.out.println("Flight=" + ChineseTranslations.get("module.flight.title", "Flight"));
        System.out.println("module_titles=" + titles);
        System.out.println("module_descriptions=" + descriptions);
        System.out.println("null_and_unknown_fallback=PASS");
    }
}
