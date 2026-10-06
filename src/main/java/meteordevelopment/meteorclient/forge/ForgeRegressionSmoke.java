package meteordevelopment.meteorclient.forge;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.screens.settings.*;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Opt-in deterministic search and entity-mask fixture; never active in normal play. */
public final class ForgeRegressionSmoke {
    private static int startedAge;
    private static Vec3d origin;
    private static ESP esp;
    private static boolean espWasActive;
    private static ESP.Mode originalMode;
    private static Set<EntityType<?>> originalTypes;
    private static boolean finished;
    private static int reportedAge;
    private static final boolean REQUIRE_FIX = Boolean.getBoolean("meteor.smoke.requireFix");

    private ForgeRegressionSmoke() {}

    public static void begin() {
        checkSearch();
        startedAge = mc.player.age;
        origin = mc.player.getPos();
        mc.options.getEntityDistanceScaling().setValue(0.5);
        mc.player.setYaw(0);
        mc.player.setPitch(0);
        esp = Modules.get().get(ESP.class);
        espWasActive = esp.isActive();
        originalMode = esp.mode.get();
        originalTypes = new java.util.HashSet<>(((EntityTypeListSetting) esp.settings.get("entities")).get());
        ((EntityTypeListSetting) esp.settings.get("entities")).set(new java.util.HashSet<>(Set.of(EntityType.COW, EntityType.PIG)));
        esp.mode.set(ESP.Mode.Shader);
        if (!espWasActive) esp.toggle();
        mc.getServer().execute(() -> {
            var world = mc.getServer().getOverworld();
            world.setTimeOfDay(6000);
            spawn(EntityType.COW, "RegressionNear", -5, 18);
            spawn(EntityType.COW, "RegressionFar", 8, 74);
            spawn(EntityType.PIG, "RegressionSmall", -8, 60);
        });
        mc.setScreen(null);
    }

    private static void spawn(EntityType<? extends MobEntity> type, String name, double dx, double dz) {
        var world = mc.getServer().getOverworld();
        world.getChunk((int) Math.floor((origin.x + dx) / 16), (int) Math.floor((origin.z + dz) / 16));
        MobEntity mob = type.create(world);
        if (mob == null) throw new IllegalStateException("Fixture spawn failed");
        mob.refreshPositionAndAngles(origin.x + dx, origin.y, origin.z + dz, 180, 0);
        mob.setAiDisabled(true);
        mob.setCustomName(Text.literal(name));
        boolean spawned = world.spawnEntity(mob);
        MeteorClient.LOG.info("REGRESSION_SPAWN name={} position={} spawned={} trackingChunks={}", name, mob.getPos(), spawned, type.getMaxTrackDistance());
    }

    private static void checkSearch() {
        boolean module = Modules.get().searchTitles("stts").iterator().next() instanceof ESP;
        boolean setting = Utils.searchInWords("实体", "shiti") > 0;
        boolean block = checkScreen(new BlockSettingScreen(GuiThemes.get(), new BlockSetting.Builder().name("fixture").build()), "shitou", "石头");
        boolean item = checkScreen(new ItemSettingScreen(GuiThemes.get(), new ItemSetting.Builder().name("fixture").build()), "zsj", "钻石剑");
        boolean entity = checkScreen(new EntityTypeListSettingScreen(GuiThemes.get(), new EntityTypeListSetting.Builder().name("fixture").build()), "jiangshi", "僵尸");
        MeteorClient.LOG.info("REGRESSION_SEARCH module={} setting={} block={} item={} entity={} language={}", module, setting, block, item, entity, mc.options.language);
        if (REQUIRE_FIX && !(module && setting && block && item && entity)) throw new IllegalStateException("Pinyin runtime search failed");
        mc.setScreen(null);
    }

    private static boolean checkScreen(WidgetScreen screen, String query, String expected) {
        mc.setScreen(screen);
        try {
            Field rootField = WidgetScreen.class.getDeclaredField("root");
            rootField.setAccessible(true);
            WContainer root = (WContainer) rootField.get(screen);
            List<WWidget> widgets = new ArrayList<>();
            flatten(root, widgets);
            WTextBox filter = (WTextBox) widgets.stream().filter(widget -> widget instanceof WTextBox).findFirst().orElseThrow();
            filter.set(query);
            filter.action.run();
            widgets.clear();
            flatten(root, widgets);
            return widgets.stream().anyMatch(widget -> widget instanceof WLabel label && label.get().equals(expected));
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }

    private static void flatten(WWidget widget, List<WWidget> result) {
        result.add(widget);
        if (widget instanceof WContainer container) for (var cell : container.cells) flatten(cell.widget(), result);
    }

    public static void afterRender() {
        if (finished || mc.player.age - startedAge < 120) return;
        List<Entity> fixtures = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity.hasCustomName() && entity.getCustomName().getString().startsWith("Regression")) fixtures.add(entity);
        }
        if (fixtures.size() < 3) {
            if (mc.player.age - reportedAge >= 100) {
                reportedAge = mc.player.age;
                MeteorClient.LOG.info("REGRESSION_WAIT age={} fixtures={}", mc.player.age - startedAge, fixtures.size());
            }
            return;
        }
        boolean maskPass = true;
        try (var image = ScreenshotRecorder.takeScreenshot(PostProcessShaders.ENTITY_OUTLINE.framebuffer)) {
            image.writeTo(mc.runDirectory.toPath().resolve("regression-entity-mask.png"));
            for (Entity entity : fixtures) {
                var box = entity.getBoundingBox();
                double minX = Double.POSITIVE_INFINITY, minY = minX, maxX = -minX, maxY = -minX;
                for (double x : new double[]{box.minX, box.maxX}) for (double y : new double[]{box.minY, box.maxY}) for (double z : new double[]{box.minZ, box.maxZ}) {
                    Vector3d point = new Vector3d(x, y, z);
                    if (!NametagUtils.to2D(point, 1, false)) throw new IllegalStateException("Fixture behind camera");
                    minX = Math.min(minX, point.x); maxX = Math.max(maxX, point.x);
                    minY = Math.min(minY, point.y); maxY = Math.max(maxY, point.y);
                }
                int pixels = 0;
                for (int x = Math.max(0, (int) minX - 2); x < Math.min(image.getWidth(), (int) maxX + 3); x++) {
                    for (int y = Math.max(0, (int) minY - 2); y < Math.min(image.getHeight(), (int) maxY + 3); y++) {
                        int color = image.getColor(x, y);
                        int red = color & 255, green = (color >>> 8) & 255, blue = (color >>> 16) & 255;
                        if (green > red + 64 && green > blue + 64) pixels++;
                    }
                }
                Vec3d camera = mc.gameRenderer.getCamera().getPos();
                boolean vanillaDistance = entity.shouldRender(camera.x, camera.y, camera.z);
                MeteorClient.LOG.info("REGRESSION_MASK name={} distance={} vanillaDistance={} pixels={} rect={},{},{},{}", entity.getCustomName().getString(), entity.getPos().distanceTo(camera), vanillaDistance, pixels, minX, minY, maxX, maxY);
                if (pixels == 0 || (!entity.getCustomName().getString().equals("RegressionNear") && vanillaDistance)) maskPass = false;
            }
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
        try (var image = ScreenshotRecorder.takeScreenshot(mc.getFramebuffer())) {
            image.writeTo(mc.runDirectory.toPath().resolve("regression-shader-world.png"));
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
        ((EntityTypeListSetting) esp.settings.get("entities")).set(originalTypes);
        esp.mode.set(originalMode);
        if (!espWasActive) esp.toggle();
        finished = true;
        MeteorClient.LOG.info("REGRESSION_RESULT requireFix={} maskPass={} moduleStateRestored=true", REQUIRE_FIX, maskPass);
        if (REQUIRE_FIX && !maskPass) throw new IllegalStateException("Far entity shader mask is missing");
        mc.scheduleStop();
    }
}
