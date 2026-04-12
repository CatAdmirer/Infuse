package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.events.EffectEquipEvent;
import com.catadmirer.infuseSMP.events.EffectUnequipEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class ApophisManager {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private final Infuse plugin;
    
    private final ProfileProperty APOPHIS_SKIN = new ProfileProperty(
            "textures",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxNzg4NTA2MDQwNywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MwOTBmY2NjMjBmMWM3ZWMyMDBkNGVkMDUxMjQwNjM3ZmRmNjE5ZDg1Nzg0NWZhNWRmNWJkMzM1MWJiMjBkOCIKICAgIH0KICB9Cn0=",
            "mBgGwS28lqNz7rJCysD9SElJpA5q+34uTZK68JFXIFzuoN31KQg2VHjVDz+/nAr0yXdRwOrgL5rnRb2NbKBPyKSWdcB8A1nVHeNMpoJ5c5CzEERyOROUiTRxge/MIhYL7Fkj67fkh7Sc/l7BwDAf7/7OIgiAIleUTLZ9COnIN15gylTBldOo3JOka8TTNrI1i4QmnMsbgT0luQZzrUMRtZxIHNwx+26IevzCE+hpNdwiYqnDVZdayDLPVy1vv+i3C7AJGd9b7/2/qv0YmWxvT3uKrPR8+9fbSWltGx9ikrdXO17FrGc5u0gqmPWAaSSWw/NJmMhPenILh7/MvXA8mO2m7JeuhnM/EYzdOMB3qzvkUEVddFIngPl6LNE8XG1R+APFBsbpnpybB7dQphSud5DNfuZijqLDd735kykYlRMzw5VVGf7fONheLzSV42XRsIU+5IazHvmAZ4pxr72+r9bbS9vRW38ZgQIy6p8r4tLv9jfmqmcS9lEn1CAgDLAqZWGzIWeIgOdDsrWH4ia/1gj6oZVefRCr2dAS84NsOQUdoJDbS8G0+ArN+CWgnlcwOJCS6MB5kBmQl2FPvwLcSnnRcS66XKfH28Bu2/J3Hu5zRWbONuOLQTbYFxwftUtvS1IORKBCfWvlJTx5G/mz1KOGW89iOCpW8jdx8EmzpRI="
    );

    public ApophisManager(Infuse plugin) {
        this.plugin = plugin;
    }

    public void initDisguise(Player target) {
        UUID uuid = target.getUniqueId();

        // Getting the disguise file for the player
        File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers/" + uuid + ".yml");
        disguiseFile.getParentFile().mkdirs();

        // Skipping players who already have a disguise file
        if (disguiseFile.exists()) return;
        
        try {
            FileWriter writer = new FileWriter(disguiseFile);
            Optional<ProfileProperty> textures = target.getPlayerProfile().getProperties().stream().filter(property -> "textures".equals(property.getName())).findFirst();

            // Writing the urls to disk
            writer.write(mm.serialize(target.displayName()));
            writer.write("\n");
            if (textures.isEmpty()) {
                writer.write("null\nnull");
            } else {
                writer.write(textures.get().getValue());
                writer.write("\n");
                writer.write(String.valueOf(textures.get().getSignature()));
            }

            writer.flush();
            writer.close();
        } catch (IOException err) {
            Infuse.LOGGER.error("Failed to write to {}.  Make sure it can be created and edited by the user running the server.", disguiseFile.getPath());
        }
    }

    @EventHandler
    public void equipApophis(EffectEquipEvent event) {
        Player target = event.getPlayer();
        
        // Making sure the disguise file is created
        initDisguise(target);

        // Changing the player's skin
        PlayerProfile profile = target.getPlayerProfile();
        profile.setProperty(APOPHIS_SKIN);
        target.setPlayerProfile(profile);

        // Hiding the player's name
        Component apophisName = Component.text("Apophis", NamedTextColor.DARK_PURPLE);
        target.displayName(apophisName);
        target.playerListName(apophisName);
    }

    @EventHandler
    public void unequipApophis(EffectUnequipEvent event) {
        Player target = event.getPlayer();

        if (!target.isOnline()) {
            Infuse.LOGGER.warn("Could not remove {0}'s disguise as they are not online.", target.getName());
            return;
        }

        UUID uuid = target.getUniqueId();

        // Getting the player's skin info from the disguise file
        File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers/" + uuid + ".yml");

        try (Scanner scanner = new Scanner(disguiseFile)) {
            PlayerProfile profile = target.getPlayerProfile();
            String value = "";
            String signature = "";

            // Getting the player's name
            if (scanner.hasNextLine()) {
                String read = scanner.nextLine();
                target.displayName(mm.deserialize(read));
                target.playerListName(mm.deserialize(read));
            }

            // Getting the property value
            if (scanner.hasNextLine()) {
                value = scanner.nextLine();
            }

            // Getting the property signature
            if (scanner.hasNextLine()) {
                signature = scanner.nextLine();
                if (signature.equals("null")) {
                    signature = null;
                }
            }

            profile.setProperty(new ProfileProperty("textures", value, signature));

            target.setPlayerProfile(profile);
        } catch (FileNotFoundException err) {}

        // Deleting the disguise file
        if (disguiseFile.exists()) {
            disguiseFile.delete();
        }

        return;
    }
}