import meteordevelopment.meteorclient.utils.misc.PinyinSearch;

public final class PinyinSearchSmoke {
    public static void main(String[] args) {
        String[][] cases = {
            {"实体透视", "shititoushi"}, {"实体透视", "stts"}, {"实体透视", "toushi"},
            {"追踪线", "ZZX"}, {"石头", "shi tou"}, {"钻石剑", "zsj"},
            {"僵尸", "jiangshi"}, {"苦力怕", "klp"}, {"夜视", "yeshi"},
            {"重庆", "chongqing"}, {"重复", "chongfu"}, {"重力", "zhongli"},
            {"女巫", "nvwu"}, {"女巫", "nüwū"}, {"女巫", "nu:wu"},
            {"鑽石劍", "zuanshijian"}, {"实体 ESP", "shitiESP"},
            {"实体透视", "shí tǐ tòu shì"}, {"石头", "石头"}, {"§a僵尸", "js"},
            {"实体透视", "shi-ti-tou-shi"}, {"自动工具", "zdgj"}, {"音乐播放器", "yybfq"}
        };
        for (String[] pair : cases) assert PinyinSearch.matches(pair[0], pair[1]) : pair[0] + " / " + pair[1];
        assert !PinyinSearch.matches("石头", "jiangshi");
        assert !PinyinSearch.matches("僵尸", "zsj");
        assert PinyinSearch.matches("石头", "minecraft:stone", "minecraft:stone");
        assert PinyinSearch.matches("实体透视", "esp", "esp");
        assert PinyinSearch.matches("English Fallback", "fallback");
        assert PinyinSearch.matches("emoji 🧪 unsupported 𠀀", "𠀀");
        assert PinyinSearch.matches("anything", "");
        assert !PinyinSearch.matches(null, "stone");
        assert PinyinSearch.matches("重".repeat(80), "zhong".repeat(40));
        for (int i = 0; i < 5000; i++) PinyinSearch.matches("自定义" + i, "zdy");
        assert PinyinSearch.matches("实体透视", "stts");
        System.out.println("PINYIN_SEARCH_PASS full initials partial tones umlaut polyphonic traditional mixed chinese english registryId cacheBound");
    }
}
