package com.catadmirer.infuseSMP.version;

import org.bukkit.Server;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SupportedServerSoftware<ImplementationProvider> {
    private final Map<ServerSoftware, ImplementationProvider> supportedVersions = new LinkedHashMap<>();

    public void registerSupportedVersion(ServerSoftware software, ImplementationProvider implementationSupplier) {
        this.supportedVersions.put(software, implementationSupplier);
    }

    public void registerSupportedVersion(ImplementationProvider implementationSupplier, ServerSoftware... softwares) {
        for (ServerSoftware software : softwares) {
            registerSupportedVersion(software, implementationSupplier);
        }
    }

    public ImplementationProvider getImplementationProvider(ServerSoftware software) {
        return supportedVersions.get(software);
    }

    public static String getUnsupportedPlatformMessage(Server server) {
        return server.getName() + " is not supported. Please run Infuse on (a fork of) one of the following server software: "
                + Arrays.stream(MinecraftPlatform.values())
                        .map(MinecraftPlatform::toString)
                        .collect(Collectors.joining(", ", "[", "]")) + ".";
    }

    public String getUnsupportedVersionMessage(MinecraftPlatform platform, Server server) {
        return platform + " version " + server.getVersion() + " is not supported by this release of Infuse. "
                + "Please use one of the following " + platform + " versions: " + supportedVersions.keySet().stream()
                        .filter(software -> software.getPlatform() == platform)
                        .map(software -> software.getVersion().toString())
                        .collect(Collectors.joining(", ", "[", "]")) + ". "
                + "Alternatively you can try upgrading Infuse if any of the versions listed here is older than your server's Minecraft version. "
                + "Infuse is available on Modrinth: https://https://modrinth.com/plugin/infusesmp and on GitHub: "
                + "https://github.com/CatAdmirer/Infuse/releases";
    }
}