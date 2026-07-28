package com.catadmirer.infuseSMP;

import java.util.function.BiFunction;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.catadmirer.infuseSMP.version.LegacyVersions;
import com.catadmirer.infuseSMP.version.MinecraftPlatform;
import com.catadmirer.infuseSMP.version.MinecraftVersion;
import com.catadmirer.infuseSMP.version.ServerSoftware;
import com.catadmirer.infuseSMP.version.SupportedServerSoftware;

public class VersionManager {
    // Loading supported versions
    static final SupportedServerSoftware<BiFunction<Plugin,BukkitScheduler,InfusePlatform>> SUPPORTED = new SupportedServerSoftware<>();
    static {
        // Registering spigot/paper implementations
        // TODO: Create and register implementations here
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_8.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_8_8);
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_12_2.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_12_2);
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_16_5.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_16_5);
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_17_1.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_17_1);
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_18_2.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_18_2);
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_19_4.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_19_4);
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_20_1.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_20_1, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_1));
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_20_4.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_20_4, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_4));
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_20_6.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_20_6, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_6));
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_21.InvseeImpl::new, new ServerSoftware(MinecraftPlatform.CRAFTBUKKIT, MinecraftVersion._1_21), new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21));
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_21_1.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_21_1, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_1));
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_21_4.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_21_4, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_4));
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_21_5.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_21_5, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_5));
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_21_7.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_21_7, ServerSoftware.CRAFTBUKKIT_1_21_8, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_7), new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_8));
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_21_9.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_21_9, ServerSoftware.CRAFTBUKKIT_1_21_10, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_9), new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_10));
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_1_21_11.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_1_21_11);
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.paper.impl_1_21_11.InvseeImpl::new, ServerSoftware.PAPER_1_21_11);
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.spigot.impl_26_1_1.InvseeImpl::new, ServerSoftware.CRAFTBUKKIT_26_1_1, ServerSoftware.CRAFTBUKKIT_26_1_2, ServerSoftware.CRAFTBUKKIT_26_1);
        // SUPPORTED.registerSupportedVersion(com.janboerman.invsee.paper.impl_26_1_1.InvseeImpl::new, ServerSoftware.PAPER_26_1_1, ServerSoftware.PAPER_26_1_2);

        // // Registering all versions of Glowstone
        // final MinecraftVersion[] minecraftVersions = MinecraftVersion.values();
        // for (int ord = MinecraftVersion._1_8.ordinal(); ord < MinecraftVersion._1_12_2.ordinal(); ord ++) {
        //     SUPPORTED.registerSupportedVersion(com.janboerman.invsee.glowstone.InvseeImpl::new, new ServerSoftware(MinecraftPlatform.GLOWSTONE, minecraftVersions[ord]));
        // }
    }

    private VersionManager() {};

    public static InfusePlatform setup(Plugin plugin, BukkitScheduler scheduler) {
        Server server = plugin.getServer();
        ServerSoftware serverSoftware = ServerSoftware.detect(server);
        plugin.getLogger().info("Detected server software: " + serverSoftware);

        // Unrecognized server software
        if (serverSoftware == null)
            throw new RuntimeException(SupportedServerSoftware.getUnsupportedPlatformMessage(server));

        // Finding supported version
        BiFunction<Plugin,BukkitScheduler,InfusePlatform> provider = SUPPORTED.getImplementationProvider(serverSoftware);

        // Handing unsupported version
        if (provider == null) {
            String supportedVersionsMessage = SUPPORTED.getUnsupportedVersionMessage(serverSoftware.getPlatform(), server);
            String legacyVersionsMessage = LegacyVersions.getLegacyVersionMessage(serverSoftware.getVersion());

            if (legacyVersionsMessage != null) {
                plugin.getLogger().severe(legacyVersionsMessage);
            }

            throw new RuntimeException(supportedVersionsMessage);
        }

        return provider.apply(plugin, scheduler);
    }
}