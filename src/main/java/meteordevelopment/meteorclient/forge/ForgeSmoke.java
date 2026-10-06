package meteordevelopment.meteorclient.forge;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.screens.ModuleScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import meteordevelopment.meteorclient.systems.modules.render.Fullbright;
import meteordevelopment.meteorclient.utils.misc.ChineseTranslations;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.ScreenshotRecorder;
import java.io.IOException;
import java.nio.file.Path;
import org.spongepowered.asm.mixin.MixinEnvironment;

public final class ForgeSmoke {
    private static boolean opened;
    private static int frames;
    private static int phase;
    private static long phaseStarted;
    private static int worldAge;
    private static Vec3d worldPosition;
    private static boolean fullbrightWasActive;
    private static final long STARTED = System.nanoTime();
    private static final long TIMEOUT = java.util.concurrent.TimeUnit.SECONDS.toNanos(Math.max(1L, Long.getLong("meteor.smoke.timeoutSeconds", 180L)));

    private ForgeSmoke() {}

    public static void tick() {
        var client = MeteorClient.mc;
        if (System.nanoTime() - STARTED > TIMEOUT) throw new IllegalStateException("Smoke test timed out: " + client.currentScreen);
        if (phase >= 3 && phase <= 7 && client.currentScreen instanceof DisconnectedScreen)
            throw new IllegalStateException("Smoke test disconnected before completion");
        if (client.getOverlay() != null) return;
        if (phase == 2 && client.currentScreen instanceof CreateWorldScreen screen) {
            var creator = screen.getWorldCreator();
            creator.setWorldName("MeteorForgeSmoke-" + System.currentTimeMillis());
            creator.setGameMode(WorldCreator.Mode.CREATIVE);
            creator.setDifficulty(Difficulty.PEACEFUL);
            creator.setSeed("0");
            creator.setGenerateStructures(false);
            creator.setWorldType(creator.getNormalWorldTypes().stream().filter(type -> type.preset().matchesKey(WorldPresets.FLAT)).findFirst().orElseThrow());
            client.options.getViewDistance().setValue(Boolean.getBoolean("meteor.smoke.regression") ? 8 : 2);
            client.options.getSimulationDistance().setValue(Boolean.getBoolean("meteor.smoke.regression") ? 8 : 5);
            phase = 3;
            screen.createLevel();
            return;
        }
        if (phase == 3 && client.world != null && client.player != null && client.currentScreen == null) {
            worldAge = client.player.age;
            worldPosition = client.player.getPos();
            Fullbright fullbright = Modules.get().get(Fullbright.class);
            fullbrightWasActive = fullbright.isActive();
            fullbright.toggle();
            if (fullbright.isActive() == fullbrightWasActive) throw new IllegalStateException("Module toggle failed");
            client.options.forwardKey.setPressed(true);
            phase = 4;
            MeteorClient.LOG.info("METEOR_FORGE_WORLD_ENTERED start={} fullbright={}", worldPosition, fullbright.isActive());
        }
        if (client.currentScreen instanceof AccessibilityOnboardingScreen) client.setScreen(new TitleScreen());
        if (!opened && client.currentScreen instanceof TitleScreen) {
            if (!Boolean.getBoolean("meteor.smoke.skipGlobalAudit")) MixinEnvironment.getCurrentEnvironment().audit();
            if (Boolean.getBoolean("meteor.smoke.payload")) ForgePayloadSmoke.run();
            int count = Modules.get().getAll().size();
            if (count < 168) throw new IllegalStateException("Incomplete module initialization: " + count);
            if (ChineseTranslations.isChinese()) {
                for (var module : Modules.get().getAll()) {
                    if (ChineseTranslations.get("module." + module.name + ".title", null) == null
                        || ChineseTranslations.get("module." + module.name + ".description", null) == null)
                        throw new IllegalStateException("Missing module translation: " + module.name);
                }
            }
            Tabs.get().get(0).openScreen(GuiThemes.get());
            opened = true;
            phaseStarted = System.nanoTime();
        }
    }

    public static void afterRender() {
        var client = MeteorClient.mc;
        if (phase == 7) {
            if (ForgeBaritoneSmoke.afterRender()) finishWorldTest();
            return;
        }
        if (phase == 6) {
            ForgeRegressionSmoke.afterRender();
            return;
        }
        if (phase == 4 && client.player.age - worldAge >= 40) {
            client.options.forwardKey.setPressed(false);
            double movement = client.player.getPos().distanceTo(worldPosition);
            if (movement < 0.1) throw new IllegalStateException("Player did not move: " + movement);
            capture("meteor-forge-world.png");
            Fullbright fullbright = Modules.get().get(Fullbright.class);
            fullbright.toggle();
            if (fullbright.isActive() != fullbrightWasActive) throw new IllegalStateException("Module state was not restored");
            MeteorClient.LOG.info("METEOR_FORGE_WORLD_PASS ticks={} movement={} fullbrightToggleRestored=true", client.player.age - worldAge, movement);
            MeteorClient.LOG.info("METEOR_FORGE_SMOKE_PASS modules={} chinese={} world=true", Modules.get().getAll().size(), ChineseTranslations.isChinese());
            if (Boolean.getBoolean("meteor.smoke.baritone")) {
                phase = 7;
                ForgeBaritoneSmoke.begin();
            } else finishWorldTest();
            return;
        }
        if (!opened || client.getOverlay() != null || !(client.currentScreen instanceof WidgetScreen)) return;
        frames++;
        if (frames >= 20 && System.nanoTime() - phaseStarted >= 600_000_000L) {
            capture(phase == 0 ? "meteor-forge-gui.png" : "meteor-forge-settings.png");
            if (phase == 0) {
                client.setScreen(new ModuleScreen(GuiThemes.get(), Modules.get().get(Velocity.class)));
                phase = 1;
                frames = 0;
                phaseStarted = System.nanoTime();
            }
            else {
                MeteorClient.LOG.info("METEOR_FORGE_GUI_PASS modules={} chinese={} settingsFrames={} mixinAudit={}", Modules.get().getAll().size(), ChineseTranslations.isChinese(), frames, !Boolean.getBoolean("meteor.smoke.skipGlobalAudit"));
                if (Boolean.getBoolean("meteor.smoke.world")) {
                    phase = 2;
                    CreateWorldScreen.create(client, new TitleScreen());
                } else {
                    MeteorClient.LOG.info("METEOR_FORGE_SMOKE_PASS modules={} chinese={} world=false", Modules.get().getAll().size(), ChineseTranslations.isChinese());
                    phase = 5;
                    client.scheduleStop();
                }
            }
        }
    }

    private static void finishWorldTest() {
        if (Boolean.getBoolean("meteor.smoke.regression")) {
            phase = 6;
            ForgeRegressionSmoke.begin();
        } else {
            phase = 5;
            MeteorClient.mc.scheduleStop();
        }
    }

    private static void capture(String name) {
        var client = MeteorClient.mc;
        Path screenshot = client.runDirectory.toPath().resolve(name);
        try (var image = ScreenshotRecorder.takeScreenshot(client.getFramebuffer())) {
            image.writeTo(screenshot);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save smoke screenshot", e);
        }
        MeteorClient.LOG.info("METEOR_FORGE_SCREENSHOT {}", screenshot.toAbsolutePath());
    }
}
