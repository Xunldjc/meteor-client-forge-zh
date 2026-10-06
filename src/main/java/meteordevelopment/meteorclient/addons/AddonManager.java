/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.addons;

import meteordevelopment.meteorclient.MeteorClient;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class AddonManager {
    public static final List<MeteorAddon> ADDONS = new ArrayList<>();

    public static void init() {
        MeteorClient.ADDON = new MeteorAddon() {
            @Override public void onInitialize() {}
            @Override public String getPackage() { return "meteordevelopment.meteorclient"; }
            @Override public String getWebsite() { return "https://meteorclient.com"; }
            @Override public GithubRepo getRepo() { return new GithubRepo("MeteorDevelopment", "meteor-client"); }
            @Override public String getCommit() { return MeteorClient.MOD_META.getCommit(); }
        };
        MeteorClient.ADDON.name = MeteorClient.MOD_META.getName();
        MeteorClient.ADDON.authors = new String[] {"MineGame159", "squidoodly", "seasnail"};
        MeteorClient.ADDON.color.parse("145,61,226");

        // Native addons declare a standard META-INF/services provider, not a Fabric entrypoint.
        for (MeteorAddon addon : ServiceLoader.load(MeteorAddon.class)) {
            if (addon.name == null || addon.authors == null || addon.authors.length == 0) {
                throw new IllegalStateException("Forge addon must declare its name and authors: " + addon.getClass().getName());
            }
            ADDONS.add(addon);
        }
    }
}
