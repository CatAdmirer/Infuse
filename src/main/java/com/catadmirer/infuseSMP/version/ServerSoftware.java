package com.catadmirer.infuseSMP.version;

import org.bukkit.Server;

import static com.catadmirer.infuseSMP.version.MinecraftPlatform.*;
import static com.catadmirer.infuseSMP.version.MinecraftVersion.*;

import java.lang.reflect.Field;
import java.util.Objects;

public class ServerSoftware {
    // TODO: only list supported server versions here.
    public static final ServerSoftware
            CRAFTBUKKIT_1_8_8 = new ServerSoftware(CRAFTBUKKIT, _1_8_8),
            CRAFTBUKKIT_1_12_2 = new ServerSoftware(CRAFTBUKKIT, _1_12_2),
            CRAFTBUKKIT_1_16_5 = new ServerSoftware(CRAFTBUKKIT, _1_16_5),
            CRAFTBUKKIT_1_17_1 = new ServerSoftware(CRAFTBUKKIT, _1_17_1),
            CRAFTBUKKIT_1_18_2 = new ServerSoftware(CRAFTBUKKIT, _1_18_2),
            CRAFTBUKKIT_1_19_4 = new ServerSoftware(CRAFTBUKKIT, _1_19_4),
            CRAFTBUKKIT_1_20_1 = new ServerSoftware(CRAFTBUKKIT, _1_20_1),
            CRAFTBUKKIT_1_20_4 = new ServerSoftware(CRAFTBUKKIT, _1_20_4),
            CRAFTBUKKIT_1_20_6 = new ServerSoftware(CRAFTBUKKIT, _1_20_6),
            CRAFTBUKKIT_1_21_1 = new ServerSoftware(CRAFTBUKKIT, _1_21_1),
            CRAFTBUKKIT_1_21_3 = new ServerSoftware(CRAFTBUKKIT, _1_21_3),
            CRAFTBUKKIT_1_21_4 = new ServerSoftware(CRAFTBUKKIT, _1_21_4),
            CRAFTBUKKIT_1_21_5 = new ServerSoftware(CRAFTBUKKIT, _1_21_5),
            CRAFTBUKKIT_1_21_6 = new ServerSoftware(CRAFTBUKKIT, _1_21_6),
            CRAFTBUKKIT_1_21_7 = new ServerSoftware(CRAFTBUKKIT, _1_21_7),
            CRAFTBUKKIT_1_21_8 = new ServerSoftware(CRAFTBUKKIT, _1_21_8),
            CRAFTBUKKIT_1_21_9 = new ServerSoftware(CRAFTBUKKIT, _1_21_9),
            CRAFTBUKKIT_1_21_10 = new ServerSoftware(CRAFTBUKKIT, _1_21_10),
            CRAFTBUKKIT_1_21_11 = new ServerSoftware(CRAFTBUKKIT, _1_21_11),
            CRAFTBUKKIT_26_1 = new ServerSoftware(CRAFTBUKKIT, _26_1),
            CRAFTBUKKIT_26_1_1 = new ServerSoftware(CRAFTBUKKIT, _26_1_1),
            CRAFTBUKKIT_26_1_2 = new ServerSoftware(CRAFTBUKKIT, _26_1_2),
            PAPER_1_21_11 = new ServerSoftware(PAPER, _1_21_11),
            PAPER_26_1_1 = new ServerSoftware(PAPER, _26_1_1),
            PAPER_26_1_2 = new ServerSoftware(PAPER, _26_1_2);

    private MinecraftPlatform platform;
    private MinecraftVersion version;

    public ServerSoftware(MinecraftPlatform platform, MinecraftVersion version) {
        this.platform = platform;
        this.version = version;
    }

    public static ServerSoftware detect(final Server server) {
        final String serverClassName = server.getClass().getName();
        return switch (serverClassName) {
            case "org.bukkit.craftbukkit.v1_8_R3.CraftServer" -> CRAFTBUKKIT_1_8_8;
            case "org.bukkit.craftbukkit.v1_12_R1.CraftServer" -> CRAFTBUKKIT_1_12_2;
            case "org.bukkit.craftbukkit.v1_15_R1.CraftServer" -> new ServerSoftware(CRAFTBUKKIT, _1_15_2);
            case "org.bukkit.craftbukkit.v1_16_R3.CraftServer" -> CRAFTBUKKIT_1_16_5;

            // Post 1.17 craftbukkit, determining version based on mappings
            case "org.bukkit.craftbukkit.v1_17_R1.CraftServer",
                 "org.bukkit.craftbukkit.v1_18_R1.CraftServer",
                 "org.bukkit.craftbukkit.v1_18_R2.CraftServer",
                 "org.bukkit.craftbukkit.v1_19_R1.CraftServer",
                 "org.bukkit.craftbukkit.v1_19_R2.CraftServer",
                 "org.bukkit.craftbukkit.v1_19_R3.CraftServer",
                 "org.bukkit.craftbukkit.v1_20_R1.CraftServer",
                 "org.bukkit.craftbukkit.v1_20_R2.CraftServer",
                 "org.bukkit.craftbukkit.v1_20_R3.CraftServer",
                 "org.bukkit.craftbukkit.v1_20_R4.CraftServer",
                 "org.bukkit.craftbukkit.v1_21_R1.CraftServer",
                 "org.bukkit.craftbukkit.v1_21_R2.CraftServer",
                 "org.bukkit.craftbukkit.v1_21_R3.CraftServer",
                 "org.bukkit.craftbukkit.v1_21_R4.CraftServer",
                 "org.bukkit.craftbukkit.v1_21_R5.CraftServer",
                 "org.bukkit.craftbukkit.v1_21_R6.CraftServer",
                 "org.bukkit.craftbukkit.v1_21_R7.CraftServer" -> {
                yield switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_17 -> new ServerSoftware(CRAFTBUKKIT, _1_17);
                    case CraftbukkitMappingsVersion._1_17_1 -> CRAFTBUKKIT_1_17_1;
                    case CraftbukkitMappingsVersion._1_18 -> new ServerSoftware(CRAFTBUKKIT, _1_18);
                    case CraftbukkitMappingsVersion._1_18_1 -> new ServerSoftware(CRAFTBUKKIT, _1_18_1);
                    case CraftbukkitMappingsVersion._1_18_2 -> CRAFTBUKKIT_1_18_2;
                    case CraftbukkitMappingsVersion._1_19 -> new ServerSoftware(CRAFTBUKKIT, _1_19);
                    case CraftbukkitMappingsVersion._1_19_1 -> new ServerSoftware(CRAFTBUKKIT, _1_19_1);
                    case CraftbukkitMappingsVersion._1_19_2 -> new ServerSoftware(CRAFTBUKKIT, _1_19_2);
                    case CraftbukkitMappingsVersion._1_19_3 -> new ServerSoftware(CRAFTBUKKIT, _1_19_3);
                    case CraftbukkitMappingsVersion._1_19_4 -> CRAFTBUKKIT_1_19_4;
                    case CraftbukkitMappingsVersion._1_20 -> new ServerSoftware(CRAFTBUKKIT, _1_20);
                    case CraftbukkitMappingsVersion._1_20_1 -> CRAFTBUKKIT_1_20_1;
                    case CraftbukkitMappingsVersion._1_20_2 -> new ServerSoftware(CRAFTBUKKIT, _1_20_2);
                    case CraftbukkitMappingsVersion._1_20_4 -> {
                        if (server.getBukkitVersion().equals("1.20.3-R0.1-SNAPSHOT")) {
                            yield new ServerSoftware(CRAFTBUKKIT, _1_20_3);
                        }

                        yield CRAFTBUKKIT_1_20_4;
                    }
                    case CraftbukkitMappingsVersion._1_20_5 -> new ServerSoftware(CRAFTBUKKIT, _1_20_5);
                    case CraftbukkitMappingsVersion._1_20_6 -> CRAFTBUKKIT_1_20_6;
                    case CraftbukkitMappingsVersion._1_21 -> new ServerSoftware(CRAFTBUKKIT, _1_21);
                    case CraftbukkitMappingsVersion._1_21_1 -> CRAFTBUKKIT_1_21_1;
                    case CraftbukkitMappingsVersion._1_21_3 -> CRAFTBUKKIT_1_21_3;
                    case CraftbukkitMappingsVersion._1_21_4 -> CRAFTBUKKIT_1_21_4;
                    case CraftbukkitMappingsVersion._1_21_5 -> CRAFTBUKKIT_1_21_5;
                    case CraftbukkitMappingsVersion._1_21_6 -> CRAFTBUKKIT_1_21_6;
                    case CraftbukkitMappingsVersion._1_21_7 -> CRAFTBUKKIT_1_21_7;
                    case CraftbukkitMappingsVersion._1_21_9 -> {
                        //unfortunately we have to do this since CraftBukkit 1.21.9 and 1.21.10 share the same mappings version.
                        if (server.getBukkitVersion().equals("1.21.9-R0.1-SNAPSHOT")) {
                            yield CRAFTBUKKIT_1_21_9;
                        }

                        //best-effort
                        yield CRAFTBUKKIT_1_21_10;
                    }
                    case CraftbukkitMappingsVersion._1_21_11 -> CRAFTBUKKIT_1_21_11;
                    default -> null;
                };
            }
            case "org.bukkit.craftbukkit.CraftServer" -> {
                // CraftBukkit 26.1 and up or Paper 1.20.4 and up:
                try {
                    // Call Server#getMinecraftVersion() to find out the version (this method was added by Paper).
                    yield new ServerSoftware(PAPER, MinecraftVersion.fromString(server.getMinecraftVersion()));
                } catch (NoSuchMethodError nsme) {
                    // Apparently we are not running on Paper
                    yield switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                        case CraftbukkitMappingsVersion._1_20_6 -> CRAFTBUKKIT_1_20_6;
                        case CraftbukkitMappingsVersion._1_21_1 -> CRAFTBUKKIT_1_21_1;
                        case CraftbukkitMappingsVersion._1_21_3 -> CRAFTBUKKIT_1_21_3;
                        case CraftbukkitMappingsVersion._1_21_4 -> CRAFTBUKKIT_1_21_4;
                        case CraftbukkitMappingsVersion._1_21_5 -> CRAFTBUKKIT_1_21_5;
                        case CraftbukkitMappingsVersion._1_21_6 -> CRAFTBUKKIT_1_21_6;
                        case CraftbukkitMappingsVersion._1_21_7 -> CRAFTBUKKIT_1_21_7;
                        case CraftbukkitMappingsVersion._1_21_9 -> CRAFTBUKKIT_1_21_9;
                        case CraftbukkitMappingsVersion._1_21_11 -> CRAFTBUKKIT_1_21_11;
                        case CraftbukkitMappingsVersion._26_1_1 -> {
                            yield switch (server.getBukkitVersion()) {
                                case "26.1-R0.1-SNAPSHOT" -> CRAFTBUKKIT_26_1;
                                case "26.1.1-R0.1-SNAPSHOT" -> CRAFTBUKKIT_26_1_1;
                                case "26.1.2-R0.1-SNAPSHOT" -> CRAFTBUKKIT_26_1_2;
                                default -> CRAFTBUKKIT_26_1_1;
                            };
                        }
                        default -> null;
                    };
                }
            }
            case "net.glowstone.GlowServer" -> {
                try {
                    Class<?> glowServerClass = Class.forName("org.glowstone.GlowServer");
                    Field gameVersionField = glowServerClass.getField("GAME_VERSION");
                    yield new ServerSoftware(GLOWSTONE, MinecraftVersion.fromString((String) gameVersionField.get(null)));
                } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                    assert false : "Not running on GlowStone.";
                    yield null;
                }
            }
            default -> null;
        };
    }

    @Override
    public String toString() {
        return platform + " version " + version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ServerSoftware that)) return false;

        return this.platform == that.platform && this.version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, version);
    }

    public MinecraftPlatform getPlatform() {
        return platform;
    }

    public MinecraftVersion getVersion() {
        return version;
    }
}