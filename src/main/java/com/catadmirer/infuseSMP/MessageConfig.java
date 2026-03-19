package com.catadmirer.infuseSMP;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.EffectMapping;

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

    public static void applyUpdates() {
        config.set("invis.kill_invis", null);
        config.set("invis.death_invis", null);

        if (!config.contains("death_message")) {
            config.set("death_message", "%victim% was slain by %killer%");
        }
        
        // Renaming configs to match the new layout
        if (config.contains("controls.usage")) {
            config.set("controls_usage", config.get("controls.usage", MessageType.CONTROLS_USAGE.defaultValue));
            config.set("controls.usage", null);
        }

        if (config.contains("controls.invalid_param")) {
            config.set("controls_invalid_param", config.get("controls.invalid_param", MessageType.CONTROLS_INVALID_PARAM.defaultValue));
            config.set("controls.invalid_param", null);
        }

        if (config.contains("infuse.invalid_param")) {
            config.set("infuse_invalid_param", config.get("infuse.invalid_param", MessageType.INFUSE_INVALID_PARAM.defaultValue));
            config.set("infuse.invalid_param", null);
        }

        if (config.contains("infuse.invalid_slot")) {
            config.set("infuse_invalid_slot", config.get("infuse.invalid_slot", MessageType.INFUSE_INVALID_SLOT.defaultValue));
            config.set("infuse.invalid_slot", null);
        }

        if (config.contains("infuse_controls.usage")) {
            config.set("infuse_controls_usage", config.get("infuse_controls.usage", MessageType.INFUSE_CONTROLS_USAGE.defaultValue));
            config.set("infuse_controls.usage", null);
        }

        if (config.contains("infuse_controls.success")) {
            config.set("infuse_controls_success", config.get("infuse_controls.success", MessageType.INFUSE_CONTROLS_SUCCESS.defaultValue));
            config.set("infuse_controls.success", null);
        }

        if (config.contains("infuse_seteffect.usage")) {
            config.set("infuse_seteffect_usage", config.get("infuse_seteffect.usage", MessageType.INFUSE_SETEFFECT_USAGE.defaultValue));
            config.set("infuse_seteffect.usage", null);
        }

        if (config.contains("infuse_seteffect.success")) {
            config.set("infuse_seteffect_success", config.get("infuse_seteffect.success", MessageType.INFUSE_SETEFFECT_SUCCESS.defaultValue));
            config.set("infuse_seteffect.success", null);
        }

        if (config.contains("infuse_geteffect.usage")) {
            config.set("infuse_giveeffect_usage", config.get("infuse_geteffect.usage", MessageType.INFUSE_GIVEEFFECT_USAGE.defaultValue));
            config.set("infuse_geteffect.usage", null);
        }

        if (config.contains("infuse_geteffect.success")) {
            config.set("infuse_giveeffect_success", config.get("infuse_geteffect.success", MessageType.INFUSE_GIVEEFFECT_SUCCESS.defaultValue));
            config.set("infuse_geteffect.success", null);
        }

        if (config.contains("infuse_cleareffect.usage")) {
            config.set("infuse_cleareffect_usage", config.get("infuse_cleareffect.usage", MessageType.INFUSE_CLEAREFFECT_USAGE.defaultValue));
            config.set("infuse_cleareffect.usage", null);
        }

        if (config.contains("infuse_cleareffect.success")) {
            config.set("infuse_cleareffect_success", config.get("infuse_cleareffect.success", MessageType.INFUSE_CLEAREFFECT_SUCCESS.defaultValue));
            config.set("infuse_cleareffect.success", null);
        }

        if (config.contains("infuse_cooldown.usage")) {
            config.set("infuse_cooldown_usage", config.get("infuse_cooldown.usage", MessageType.INFUSE_COOLDOWN_USAGE.defaultValue));
            config.set("infuse_cooldown.usage", null);
        }

        if (config.contains("infuse_cooldown.success")) {
            config.set("infuse_cooldown_success", config.get("infuse_cooldown.success", MessageType.INFUSE_COOLDOWN_SUCCESS.defaultValue));
            config.set("infuse_cooldown.success", null);
        }

        if (config.contains("cleareffects.usage")) {
            config.set("cleareffects_usage", config.get("cleareffects.usage", MessageType.CLEAREFFECTS_USAGE.defaultValue));
            config.set("cleareffects.usage", null);
        }

        if (config.contains("onjoin.ability_notify")) {
            config.set("join_ability_notify", config.get("onjoin.ability_notify", MessageType.JOIN_ABILITY_NOTIFY.defaultValue));
            config.set("onjoin.ability_notify", null);
        }

        if (config.contains("drain.success")) {
            config.set("drain_success", config.get("drain.success", MessageType.DRAIN_SUCCESS.defaultValue));
            config.set("drain.success", null);
        }

        if (config.contains("errors.inv_full")) {
            config.set("error_inv_full", config.get("errors.inv_full", MessageType.ERROR_INV_FULL.defaultValue));
            config.set("errors.inv_full", null);
        }

        if (config.contains("errors.not_player")) {
            config.set("error_not_player", config.get("errors.not_player", MessageType.ERROR_NOT_PLAYER.defaultValue));
            config.set("errors.not_player", null);
        }

        if (config.contains("errors.not_op")) {
            config.set("error_not_op", config.get("errors.not_op", MessageType.ERROR_NOT_OP.defaultValue));
            config.set("errors.not_op", null);
        }

        if (config.contains("errors.invalid_command")) {
            config.set("error_invalid_command", config.get("errors.invalid_command", MessageType.ERROR_INVALID_COMMAND.defaultValue));
            config.set("errors.invalid_command", null);
        }

        if (config.contains("errors.ritual_active")) {
            config.set("error_ritual_active", config.get("errors.ritual_active", MessageType.ERROR_RITUAL_ACTIVE.defaultValue));
            config.set("errors.ritual_active", null);
        }

        if (config.contains("errors.target_not_found")) {
            config.set("error_target_not_found", config.get("errors.target_not_found", MessageType.ERROR_TARGET_NOT_FOUND.defaultValue));
            config.set("errors.target_not_found", null);
        }

        for (EffectMapping mapping : EffectMapping.values()) {
            String effectKey = mapping.name();
            MessageType effect_name = MessageType.valueOf(effectKey + "_NAME");
            MessageType effect_lore = MessageType.valueOf(effectKey + "_LORE");

            effectKey = effectKey.toLowerCase();

            if (config.contains(effectKey + ".effect_name")) {
                config.set(effectKey + "_name", config.get(effectKey + ".effect_name", effect_name.defaultValue));
                config.set(effectKey + ".effect_name", null);
            }

            if (config.contains(effectKey + ".effect_lore")) {
                config.set(effectKey + "_lore", config.get(effectKey + ".effect_lore", effect_lore.defaultValue));
                config.set(effectKey + ".effect_lore", null);
            }
        }
        
        try {
            config.save(file);
        } catch (IOException err) {
            err.printStackTrace();
        }
    }
}
