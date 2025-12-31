package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ApophisManager implements CommandExecutor {
    private final Infuse plugin;
    private final Set<UUID> apohpisActive = new HashSet<>();
    private final File apophisFile;

    private final ProfileProperty APOPHIS_SKIN = new ProfileProperty(
            "textures",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxNzg4NTA2MDQwNywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MwOTBmY2NjMjBmMWM3ZWMyMDBkNGVkMDUxMjQwNjM3ZmRmNjE5ZDg1Nzg0NWZhNWRmNWJkMzM1MWJiMjBkOCIKICAgIH0KICB9Cn0=",
            "mBgGwS28lqNz7rJCysD9SElJpA5q+34uTZK68JFXIFzuoN31KQg2VHjVDz+/nAr0yXdRwOrgL5rnRb2NbKBPyKSWdcB8A1nVHeNMpoJ5c5CzEERyOROUiTRxge/MIhYL7Fkj67fkh7Sc/l7BwDAf7/7OIgiAIleUTLZ9COnIN15gylTBldOo3JOka8TTNrI1i4QmnMsbgT0luQZzrUMRtZxIHNwx+26IevzCE+hpNdwiYqnDVZdayDLPVy1vv+i3C7AJGd9b7/2/qv0YmWxvT3uKrPR8+9fbSWltGx9ikrdXO17FrGc5u0gqmPWAaSSWw/NJmMhPenILh7/MvXA8mO2m7JeuhnM/EYzdOMB3qzvkUEVddFIngPl6LNE8XG1R+APFBsbpnpybB7dQphSud5DNfuZijqLDd735kykYlRMzw5VVGf7fONheLzSV42XRsIU+5IazHvmAZ4pxr72+r9bbS9vRW38ZgQIy6p8r4tLv9jfmqmcS9lEn1CAgDLAqZWGzIWeIgOdDsrWH4ia/1gj6oZVefRCr2dAS84NsOQUdoJDbS8G0+ArN+CWgnlcwOJCS6MB5kBmQl2FPvwLcSnnRcS66XKfH28Bu2/J3Hu5zRWbONuOLQTbYFxwftUtvS1IORKBCfWvlJTx5G/mz1KOGW89iOCpW8jdx8EmzpRI="
    );

    public ApophisManager(Infuse plugin, String relativePath) {
        this.plugin = plugin;
        this.apophisFile = new File(plugin.getDataFolder(), relativePath);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /" + label + " <player>");
            return true;
        }

        String playerName = args[0];

        if (command.getName().equalsIgnoreCase("setapophis")) {
            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage("§cPlayer not found or not online.");
                return true;
            }
            return disguiseAsApophis(target);
        }

        if (command.getName().equalsIgnoreCase("unsetapophis")) {
            return unsetApophis(sender, playerName);
        }

        return false;
    }

    public File getApophisFile() {
        return apophisFile;
    }

    public boolean disguiseAsApophis(Player target) {
        UUID uuid = target.getUniqueId();
        if (apohpisActive.contains(uuid))
            return false;

        try {
            Infuse.getInstance().saveOriginalSkin(target);
            apohpisActive.add(uuid);

            // Overriding the player's skin
            PlayerProfile profile = target.getPlayerProfile().clone();
            profile.getProperties().removeIf(p -> p.getName().equals("textures"));
            profile.getProperties().add(APOPHIS_SKIN);
            target.setPlayerProfile(profile);

            target.displayName(Component.text("Apophis", NamedTextColor.DARK_PURPLE));
            target.playerListName(Component.text("Apophis", NamedTextColor.DARK_PURPLE));
            target.customName(Component.text("Apophis", NamedTextColor.DARK_PURPLE));
            target.setCustomNameVisible(true);

            File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers" + uuid + ".yml");
            disguiseFile.getParentFile().mkdirs();
            disguiseFile.createNewFile();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean unsetApophis(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage("&cPlayer not found or not online.");
            return false;
        }

        UUID uuid = target.getUniqueId();

        if (!apohpisActive.contains(uuid)) {
            return false;
        }

        File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers" + uuid + ".yml");
        disguiseFile.getParentFile().mkdirs();

        if (disguiseFile.exists()) {
            disguiseFile.delete();
        }

        apohpisActive.remove(uuid);

        target.displayName(target.name());
        target.playerListName(target.name());
        target.customName(null);
        target.setCustomNameVisible(false);

        Infuse.getInstance().resetSkinWithoutKick(target);
        return true;
    }

}