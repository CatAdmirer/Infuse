package com.catadmirer.infuseSMP;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.catadmirer.infuseSMP.Message.MessageType;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageConfig {
    // Config and config files
    public static final File file = new File("plugins/Infuse/messages.yml");
    public static final YamlConfiguration config = new YamlConfiguration();

    // Text serializers
    public static final MiniMessage minimessage = MiniMessage.miniMessage();
    public static final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();

    /**
     * Reloads the configuration.
     *
     * @return Whether the configuration was loaded successfully.
     */
    public static boolean load(Plugin plugin) {
        // Creating the file if it doesn't exist.
        // If the function returns false, the load function fails too.
        // Logging is handled by the function.
        if (!createFile(plugin, false)) {
            return false;
        }

        // Loading the config
        try {
            config.load(file);
            plugin.getLogger().info("Successfully loaded messages.yml");
            return true;
        } catch (InvalidConfigurationException err) {
            plugin.getLogger().severe("messages.yml contains an invalid YAML configuration.  Verify the contents of the file.");
        } catch (IOException err) {
            plugin.getLogger().severe("Could not find messages.yml.  Check that it exists.");
        }

        return false;
    }

    /**
     * Creating the config file. If it doesn't exist, it loads the default config. If the file does
     * exist, it will only replace it if the parameter is true.
     * 
     * @param replace Whether or not to replace the config file with the default configs.
     * @return Whether or not the file was created successfully.
     */
    public static boolean createFile(Plugin plugin, boolean replace) {
        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            plugin.saveResource(file.getName(), replace);
        }

        // Checking if the file still doesn't exist.
        if (!file.exists()) {
            plugin.getLogger().severe("Could not create messages.yml.  Check if it already exists.");
            return false;
        }

        return true;
    }

    public static String getMessage(MessageType message) {
        // Checking that the config contains the message
        if (!config.contains(message.configKey)) {
            Bukkit.getLogger().severe("Could not find \"" + message.configKey + "\" in the config.");
            config.set(message.configKey, message.defaultValue);
            try {
                config.save(file);
            } catch (IOException err) {
                err.printStackTrace();
            }
            
            return message.defaultValue;
        }

        // If the config is a list, it converts it into a single string separated by newlines.
        // Otherwise, it just returns the string.
        if (config.isList(message.configKey)) {
            StringBuilder retVal = new StringBuilder();
            for (String line : config.getStringList(message.configKey)) {
                retVal.append(line).append("\n");
            }

            return retVal.substring(0, retVal.length() - 1);
        } else {
            return config.getString(message.configKey);
        }
    }
}
