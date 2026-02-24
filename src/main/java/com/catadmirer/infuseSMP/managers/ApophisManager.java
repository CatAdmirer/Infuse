package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.destroystokyo.paper.profile.PlayerProfile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;

public class ApophisManager {
    private final Infuse plugin;
    private final Set<UUID> apohpisActive = new HashSet<>();
    private final URL apophisSkinUrl;

    public ApophisManager(Infuse plugin) {
        this.plugin = plugin;

        URL apophisSkinUrl = null;
        try {
            apophisSkinUrl = URI.create("http://textures.minecraft.net/texture/c090fccc20f1c7ec200d4ed051240637fdf619d857845fa5df5bd3351bb20d8").toURL();
        } catch (MalformedURLException err) {
            plugin.getLogger().log(Level.SEVERE, "Apophis skin could not be resolved.", err);
        }

        this.apophisSkinUrl = apophisSkinUrl;
    }

    public boolean disguiseAsApophis(Player target) {
        UUID uuid = target.getUniqueId();

        // Getting the disguise file for the player
        File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers/" + uuid + ".yml");
        disguiseFile.getParentFile().mkdirs();

        // Skipping players who already have the apophis effect
        if (disguiseFile.exists()) return false;

        // Saving the player's current skin to the disguise file
        PlayerTextures textures = target.getPlayerProfile().getTextures();
        
        // Getting the original skin and cape url
        URL ogSkin = textures.getSkin();
        URL ogCape = textures.getCape();

        try {
            FileWriter writer = new FileWriter(disguiseFile);

            // Writing the urls to disk
            writer.write(String.valueOf(ogSkin));
            writer.write("\n");
            writer.write(String.valueOf(ogCape));

            writer.flush();
            writer.close();
        } catch (IOException err) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write to {0}.  Make sure it can be created and edited by the user running the server.", disguiseFile.getPath());
        }

        // Disguising the player's skin
        textures.setSkin(apophisSkinUrl);
        textures.setCape(null);

        PlayerProfile profile = target.getPlayerProfile();
        profile.setTextures(textures);
        target.setPlayerProfile(profile);

        // Disguising the player's name
        Component apophisName = Component.text("Apophis", NamedTextColor.DARK_PURPLE);
        target.displayName(apophisName);
        target.playerListName(apophisName);
        target.customName(apophisName);
        target.setCustomNameVisible(true);
        
        return true;
    }

    public boolean unsetApophis(Player target) {
        if (!target.isOnline()) {
            plugin.getLogger().log(Level.WARNING, "Could not remove {0}'s disguise as they are not online.", target.getName());
            return false;
        }

        UUID uuid = target.getUniqueId();

        // Checking if the target has the apophis effect active
        if (!apohpisActive.contains(uuid)) return false;

        // Getting the player's skin info from the disguise file
        File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers/" + uuid + ".yml");

        try (Scanner scanner = new Scanner(disguiseFile)) {
            PlayerTextures textures = target.getPlayerProfile().getTextures();

            if (scanner.hasNextLine()) {
                String read = scanner.nextLine();
                try {
                    textures.setSkin(URI.create(read).toURL());
                } catch (MalformedURLException err) {
                    plugin.getLogger().log(Level.SEVERE, target.getName() + "'s original skin could not be found in their apophis disguise file.  Make sure the url is correct.");
                }
            }

            if (scanner.hasNextLine()) {
                String read = scanner.nextLine();
                try {
                    textures.setCape(URI.create(read).toURL());
                } catch (MalformedURLException err) {
                    plugin.getLogger().log(Level.SEVERE, target.getName() + "'s original cape could not be found in their apophis disguise file.  Make sure the url is correct.");
                }
            }
        } catch (FileNotFoundException err) {}


        // Deleting the disguise file
        if (disguiseFile.exists()) {
            disguiseFile.delete();
        }

        apohpisActive.remove(uuid);

        // Resetting the target's name
        target.displayName(target.name());
        target.playerListName(target.name());
        target.customName(null);
        target.setCustomNameVisible(false);

        return true;
    }

}