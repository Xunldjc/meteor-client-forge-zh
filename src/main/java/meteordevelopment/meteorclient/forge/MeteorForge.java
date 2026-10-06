package meteordevelopment.meteorclient.forge;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.tooltip.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import meteordevelopment.meteorclient.utils.misc.input.KeyBinds;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(MeteorForge.MOD_ID)
public final class MeteorForge {
    public static final String MOD_ID = "meteor_client";

    public MeteorForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(MeteorForge::registerTooltips);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(MeteorForge::registerKeys);
            // Full initialization remains at MinecraftClient's constructor tail.
            new MeteorClient().onInitializeClient();
        }
    }

    private static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(BannerTooltipComponent.class, MeteorTooltipData::getComponent);
        event.register(BookTooltipComponent.class, MeteorTooltipData::getComponent);
        event.register(ContainerTooltipComponent.class, MeteorTooltipData::getComponent);
        event.register(EntityTooltipComponent.class, MeteorTooltipData::getComponent);
        event.register(MapTooltipComponent.class, MeteorTooltipData::getComponent);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyBinds.OPEN_GUI);
        event.register(KeyBinds.OPEN_COMMANDS);
    }
}
