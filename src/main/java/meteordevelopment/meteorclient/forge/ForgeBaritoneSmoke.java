package meteordevelopment.meteorclient.forge;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.Settings;
import baritone.api.event.events.ChatEvent;
import baritone.api.event.listener.AbstractGameEventListener;
import baritone.api.utils.input.Input;
import baritone.utils.BlockStateInterface;
import baritone.utils.accessor.IClientChunkProvider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Opt-in survival fixture that exercises the real prefixed chat and mining paths. */
public final class ForgeBaritoneSmoke {
    private static final String COMMAND = "#mine minecraft:stone";
    private static final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private static final Map<Settings.Setting<?>, Object> settings = new LinkedHashMap<>();
    private static final List<ItemStack> inventory = new ArrayList<>();
    private static IBaritone baritone;
    private static BlockPos floor;
    private static List<BlockPos> targets;
    private static Vec3d originalPosition;
    private static float originalYaw, originalPitch;
    private static GameMode originalMode;
    private static int originalSlot, phase, startedAge, stoppedAge;
    private static volatile boolean ready, restored;
    private static volatile int remaining = 3, collected;
    private static boolean sawChat, sawActive, sawPathing, sawAttack;
    private static double movement;

    private ForgeBaritoneSmoke() {}

    public static void begin() {
        baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        originalPosition = mc.player.getPos();
        originalYaw = mc.player.getYaw();
        originalPitch = mc.player.getPitch();
        floor = mc.player.getBlockPos().add(0, 6, 0);
        targets = List.of(floor.add(-1, 1, 6), floor.add(0, 1, 6), floor.add(1, 1, 6));
        var config = BaritoneAPI.getSettings();
        set(config.prefix, "#");
        set(config.prefixControl, true);
        set(config.allowBreak, true);
        set(config.allowPlace, false);
        set(config.autoTool, true);
        set(config.mineScanDroppedItems, true);
        baritone.getGameEventHandler().registerEventListener(new AbstractGameEventListener() {
            @Override public void onSendChatMessage(ChatEvent event) {
                if (event.getMessage().equals(COMMAND)) sawChat = true;
                if (event.getMessage().equals(COMMAND) || event.getMessage().equals("#stop"))
                    MeteorClient.LOG.info("BARITONE_CHAT_OBSERVED message=\"{}\" cancelled={}", event.getMessage(), event.isCancelled());
            }
        });
        startedAge = mc.player.age;
        phase = 1;
        mc.getServer().execute(() -> {
            var world = mc.getServer().getOverworld();
            var player = mc.getServer().getPlayerManager().getPlayer(mc.player.getUuid());
            originalMode = player.interactionManager.getGameMode();
            originalSlot = player.getInventory().selectedSlot;
            for (int i = 0; i < player.getInventory().size(); i++) inventory.add(player.getInventory().getStack(i).copy());
            for (int x = -12; x <= 12; x++) for (int z = -12; z <= 12; z++) for (int y = 0; y <= 7; y++) {
                var pos = floor.add(x, y, z);
                blocks.put(pos, world.getBlockState(pos));
                world.setBlockState(pos, y == 0 ? Blocks.BEDROCK.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
            }
            for (var target : targets) world.setBlockState(target, Blocks.STONE.getDefaultState(), 3);
            player.changeGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().selectedSlot = 0;
            player.getInventory().setStack(0, new ItemStack(Items.DIAMOND_PICKAXE));
            player.currentScreenHandler.sendContentUpdates();
            player.networkHandler.requestTeleport(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5, 0, 0);
            ready = true;
            MeteorClient.LOG.info("BARITONE_FIXTURE_READY targets={} mode=SURVIVAL tool=diamond_pickaxe", targets);
        });
    }

    public static boolean afterRender() {
        int age = mc.player.age - startedAge;
        if (age > 1600) throw new IllegalStateException("Baritone mine fixture timed out: remaining=" + remaining + " collected=" + collected);
        if (phase == 1) {
            if (!ready || age < 30 || mc.interactionManager.getCurrentGameMode() != GameMode.SURVIVAL
                || Math.abs(mc.player.getY() - floor.getY() - 1) > 0.1) return false;
            for (var target : targets) if (!mc.world.getBlockState(target).isOf(Blocks.STONE)) return false;
            if (!mc.world.getBlockState(floor).isOf(Blocks.BEDROCK)) return false;
            for (var block : List.of(Blocks.BARRIER, Blocks.BEDROCK, Blocks.STONE)) {
                try {
                    MeteorClient.LOG.info("BARITONE_FIXTURE_SHAPE block={} nullContextFullCube={}", block,
                        net.minecraft.block.Block.isShapeFullCube(block.getDefaultState().getCollisionShape(null, null)));
                } catch (Exception e) { MeteorClient.LOG.info("BARITONE_FIXTURE_SHAPE block={} nullContextException={}", block, e.toString()); }
            }
            var snapshot = ((IClientChunkProvider) mc.world.getChunkManager()).createThreadSafeCopy();
            var pathBlocks = new BlockStateInterface(baritone.getPlayerContext(), true);
            if (!pathBlocks.get0(floor).isOf(Blocks.BEDROCK)) throw new IllegalStateException("Worker snapshot lost fixture floor");
            for (var target : targets) if (!pathBlocks.get0(target).isOf(Blocks.STONE)) throw new IllegalStateException("Worker snapshot lost mining target");
            MeteorClient.LOG.info("BARITONE_CHUNK_SNAPSHOT_PASS provider={} snapshot={} loadedChunks={} floor=true targets=3", mc.world.getChunkManager().getClass().getName(), snapshot.getClass().getName(), snapshot.getLoadedChunkCount());
            mc.player.networkHandler.sendChatMessage(COMMAND);
            sawActive = baritone.getMineProcess().isActive();
            if (!sawChat || !sawActive) throw new IllegalStateException("Literal chat mine command did not activate Baritone");
            phase = 2;
            try (var image = ScreenshotRecorder.takeScreenshot(mc.getFramebuffer())) {
                image.writeTo(mc.runDirectory.toPath().resolve("baritone-mine-start.png"));
            } catch (java.io.IOException e) { throw new IllegalStateException(e); }
            MeteorClient.LOG.info("BARITONE_MINE_COMMAND command=\"{}\" chatObserved={} mineActive={}", COMMAND, sawChat, sawActive);
        }
        if (phase == 2) {
            sawActive |= baritone.getMineProcess().isActive();
            sawPathing |= baritone.getPathingBehavior().isPathing();
            sawAttack |= baritone.getInputOverrideHandler().isInputForcedDown(Input.CLICK_LEFT);
            movement = Math.max(movement, mc.player.getPos().distanceTo(new Vec3d(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5)));
            if (age % 100 == 0) MeteorClient.LOG.info("BARITONE_MINE_PROGRESS age={} position={} feet={} remaining={} collected={} pathing={} attack={} movement={}",
                age, mc.player.getPos(), baritone.getPlayerContext().playerFeet(), remaining, collected,
                baritone.getPathingBehavior().isPathing(), baritone.getInputOverrideHandler().isInputForcedDown(Input.CLICK_LEFT), movement);
            if (age % 10 == 0) mc.getServer().execute(() -> {
                var world = mc.getServer().getOverworld();
                remaining = (int) targets.stream().filter(pos -> world.getBlockState(pos).isOf(Blocks.STONE)).count();
                var player = mc.getServer().getPlayerManager().getPlayer(mc.player.getUuid());
                collected = player.getInventory().count(Items.COBBLESTONE);
            });
            if (remaining != 0 || collected < targets.size()) return false;
            if (!sawActive || !sawPathing || !sawAttack || movement < 1)
                throw new IllegalStateException("Expected survival movement, pathing and attack input");
            mc.player.networkHandler.sendChatMessage("#stop");
            stoppedAge = mc.player.age;
            phase = 3;
            MeteorClient.LOG.info("BARITONE_MINE_PASS command=\"{}\" removed=3 collectedCobblestone={} movement={} pathing={} attackInput={} mode=SURVIVAL", COMMAND, collected, movement, sawPathing, sawAttack);
        }
        if (phase == 3) {
            if (mc.player.age - stoppedAge < 10) return false;
            boolean forced = false;
            for (var input : Input.values()) forced |= baritone.getInputOverrideHandler().isInputForcedDown(input);
            if (baritone.getMineProcess().isActive() || baritone.getPathingBehavior().isPathing() || forced)
                throw new IllegalStateException("#stop left mine process, pathing or input active");
            try (var image = ScreenshotRecorder.takeScreenshot(mc.getFramebuffer())) {
                image.writeTo(mc.runDirectory.toPath().resolve("baritone-mine-result.png"));
            } catch (java.io.IOException e) { throw new IllegalStateException(e); }
            MeteorClient.LOG.info("BARITONE_STOP_PASS command=\"#stop\" mineActive=false pathing=false forcedInput=false");
            settings.forEach(ForgeBaritoneSmoke::restoreSetting);
            mc.getServer().execute(() -> {
                var world = mc.getServer().getOverworld();
                blocks.forEach((pos, state) -> world.setBlockState(pos, state, 3));
                var player = mc.getServer().getPlayerManager().getPlayer(mc.player.getUuid());
                player.getInventory().clear();
                for (int i = 0; i < inventory.size(); i++) player.getInventory().setStack(i, inventory.get(i));
                player.getInventory().selectedSlot = originalSlot;
                player.changeGameMode(originalMode);
                player.currentScreenHandler.sendContentUpdates();
                player.networkHandler.requestTeleport(originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
                restored = true;
            });
            phase = 4;
        }
        if (phase == 4 && restored && mc.interactionManager.getCurrentGameMode() == originalMode
            && mc.player.getPos().distanceTo(originalPosition) < 0.5) {
            MeteorClient.LOG.info("BARITONE_FIXTURE_RESTORED settings=true inventory=true world=true gameMode={} position={}", originalMode, mc.player.getPos());
            phase = 5;
            return true;
        }
        return false;
    }

    private static <T> void set(Settings.Setting<T> setting, T value) {
        settings.put(setting, setting.value);
        setting.value = value;
    }

    @SuppressWarnings("unchecked")
    private static void restoreSetting(Settings.Setting<?> setting, Object value) {
        ((Settings.Setting<Object>) setting).value = value;
    }
}
