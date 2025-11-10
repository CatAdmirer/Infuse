package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.ExtraEffects.Apophis;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ApophisManager implements CommandExecutor {

    private final Infuse plugin;

    private Apophis aphopis;

    private final Set<UUID> apohpisActive = new HashSet<>();

    private final File aphopisFile;


    public ApophisManager(Infuse plugin, String relativePath) {
        this.plugin = plugin;
        aphopis = new Apophis(plugin);
        this.aphopisFile = new File(plugin.getDataFolder(), relativePath);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player>");
            return true;
        }

        String playerName = args[0];

        if (command.getName().equalsIgnoreCase("setaphopis")) {
            Player target = Bukkit.getPlayerExact(playerName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found or not online.");
                return true;
            }
            return disguiseAsAphopis(target);
        }

        if (command.getName().equalsIgnoreCase("unsetaphopis")) {
            return unsetAphopis(sender, playerName);
        }

        return false;
    }

    public File getAphopisFile() {
        return aphopisFile;
    }

    public boolean disguiseAsAphopis(Player target) {
        UUID uuid = target.getUniqueId();
        if (apohpisActive.contains(uuid)) {
            return false;
        }

        try {
            Infuse.getInstance().saveOriginalSkin(target);
            apohpisActive.add(uuid);
            PlayerProfile profile = Bukkit.createProfile(uuid, target.getName());
            profile.setProperties(List.of(new ProfileProperty(
                    "textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTcxNzg4NTA2MDQwNywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MwOTBmY2NjMjBmMWM3ZWMyMDBkNGVkMDUxMjQwNjM3ZmRmNjE5ZDg1Nzg0NWZhNWRmNWJkMzM1MWJiMjBkOCIKICAgIH0KICB9Cn0=",
                    "mBgGwS28lqNz7rJCysD9SElJpA5q+34uTZK68JFXIFzuoN31KQg2VHjVDz+/nAr0yXdRwOrgL5rnRb2NbKBPyKSWdcB8A1nVHeNMpoJ5c5CzEERyOROUiTRxge/MIhYL7Fkj67fkh7Sc/l7BwDAf7/7OIgiAIleUTLZ9COnIN15gylTBldOo3JOka8TTNrI1i4QmnMsbgT0luQZzrUMRtZxIHNwx+26IevzCE+hpNdwiYqnDVZdayDLPVy1vv+i3C7AJGd9b7/2/qv0YmWxvT3uKrPR8+9fbSWltGx9ikrdXO17FrGc5u0gqmPWAaSSWw/NJmMhPenILh7/MvXA8mO2m7JeuhnM/EYzdOMB3qzvkUEVddFIngPl6LNE8XG1R+APFBsbpnpybB7dQphSud5DNfuZijqLDd735kykYlRMzw5VVGf7fONheLzSV42XRsIU+5IazHvmAZ4pxr72+r9bbS9vRW38ZgQIy6p8r4tLv9jfmqmcS9lEn1CAgDLAqZWGzIWeIgOdDsrWH4ia/1gj6oZVefRCr2dAS84NsOQUdoJDbS8G0+ArN+CWgnlcwOJCS6MB5kBmQl2FPvwLcSnnRcS66XKfH28Bu2/J3Hu5zRWbONuOLQTbYFxwftUtvS1IORKBCfWvlJTx5G/mz1KOGW89iOCpW8jdx8EmzpRI="
            )));
            target.setDisplayName("§5Aphopis");
            target.setPlayerListName("§5Aphopis");
            target.setCustomName("§5Aphopis");
            target.setCustomNameVisible(true);
            target.setPlayerProfile(profile);
            File disguiseFolder = new File(plugin.getDataFolder(), "AphopisPlayers");
            disguiseFolder.mkdirs();
            File disguiseFile = new File(disguiseFolder, uuid + ".yml");
            disguiseFile.createNewFile();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean unsetAphopis(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online.");
            return false;
        }

        UUID uuid = target.getUniqueId();

        if (!apohpisActive.contains(uuid)) {
            return false;
        }

        File disguiseFolder = new File(plugin.getDataFolder(), "AphopisPlayers");
        disguiseFolder.mkdirs();
        File disguiseFile = new File(disguiseFolder, uuid + ".yml");

        if (disguiseFile.exists()) {
            disguiseFile.delete();
        }

        apohpisActive.remove(uuid);

        target.setDisplayName(target.getName());
        target.setPlayerListName(target.getName());
        target.setCustomName(null);
        target.setCustomNameVisible(false);
        Infuse.getInstance().resetSkinWithoutKick(target);
        return true;
    }

}