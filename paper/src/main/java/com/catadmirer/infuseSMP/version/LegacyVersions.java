package com.catadmirer.infuseSMP.version;

import java.util.HashMap;
import java.util.Map;

public final class LegacyVersions {
    private static final LegacyVersions INSTANCE = new LegacyVersions();

    static {
        INSTANCE.register(MinecraftVersion._1_15_2, "0.27.0");
        INSTANCE.register(MinecraftVersion._1_16_3, "0.7");
        INSTANCE.register(MinecraftVersion._1_17,   "0.11.4");
        INSTANCE.register(MinecraftVersion._1_18,   "0.11.10");
        INSTANCE.register(MinecraftVersion._1_18_1, "0.12.2");
        INSTANCE.register(MinecraftVersion._1_19,   "0.15.2");
        INSTANCE.register(MinecraftVersion._1_19_1, "0.14.0");
        INSTANCE.register(MinecraftVersion._1_19_2, "0.21.11");
        INSTANCE.register(MinecraftVersion._1_19_3, "0.21.11");
        INSTANCE.register(MinecraftVersion._1_20,   "0.22.6");
        INSTANCE.register(MinecraftVersion._1_20_1, "0.24.9");
        INSTANCE.register(MinecraftVersion._1_20_2, "0.26.0");
        INSTANCE.register(MinecraftVersion._1_20_3, "0.24.9");
        INSTANCE.register(MinecraftVersion._1_20_5, "0.27.0");
        INSTANCE.register(MinecraftVersion._1_21_3, "0.30.10");
        INSTANCE.register(MinecraftVersion._1_21_6, "0.30.10");
    }

    private final Map<MinecraftVersion,String> legacyInvSeePlusPlusVersions = new HashMap<>();

    private LegacyVersions() {}

    private void register(MinecraftVersion minecraftVersion, String infuseVersion) {
        legacyInvSeePlusPlusVersions.put(minecraftVersion, infuseVersion);
    }

    private String get(MinecraftVersion version) {
        return legacyInvSeePlusPlusVersions.get(version);
    }

    public static String getLegacyVersionMessage(MinecraftVersion version) {
        String infuseVersion = INSTANCE.get(version);
        if (infuseVersion == null) return null;

        return "The latest release of Infuse that supported Minecraft " + version + " is Infuse v" + infuseVersion + ".\n" + 
               "You can download this release from Modrinth: https://modrinth.com/plugin/infusesmp/version/" + infuseVersion + "?version=" + version.toString() + "#download\n" + 
               "It is also available on GitHub: https://github.com/CatAdmirer/Infuse/releases/tag/v" + infuseVersion + "\n";
    }
}