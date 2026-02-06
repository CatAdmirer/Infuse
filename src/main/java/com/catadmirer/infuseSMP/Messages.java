package com.catadmirer.infuseSMP;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/*
 * TODO:
 * Figure out a better way to get messages.
 * maybe individual functions so that placeholders can be applied correctly
 * Also some things need to be Lists, so make a better implementation for that.  Currently it converts lists to a string separated with "\n" then puts it 
 */

public enum Messages {
    EFFECT_BROADCAST("effect_broadcast", "🧪 %player% is cooking up the %item% at %x%, %y%, %z%... %dimension%"),
    DISCORD_BROADCAST("discord_broadcast", "%player% is cooking up the %item% at %x%, %y%, %z% in %dimension% @everyone"),
    EFFECT_FINISHED("effect_finished", "%item% has been brewed!"),

    SLOT_EMPTY("slot_empty", "<red>You don't have any effect equipped in slot %slot%."),
    EFFECT_NONE_EQUIPPED("effect_none_equipped", "<red>You don't have an Effect equipped in slot %slot%."),

    WITHDRAW_INVALID("withdraw_invalid", "<red>Invalid usage. Use /ldrain or /rdrain"),

    TRUST_CONSOLEUSAGE("trust_consoleusage", "<red>Only players can use this command."),
    TRUST_INCORRECTUSAGE("trust_incorrectusage", "<red>Usage: /%label% <player>"),
    TRUST_NOPLAYER("trust_noplayer", "<red>Player not found."),
    TRUST_SELF("trust_self", "<red>You always trust yourself. Surely..."),
    TRUST_ADDED("trust_added", "<green>You now trust %target%."),
    TRUST_REMOVED("trust_removed", "<green>You no longer trust %target%."),

    EFFECT_NOBREWING("effect_nobrewing", "<red>You need to craft this in a brewing stand!"),

    INVIS_KILL("invis.kill_invis", "%victim% was slain by %killer%"),
    INVIS_DEATH("invis.death_invis", "%victim% was slain by %killer%"),

    CONTROLS_USAGE("controls.usage", "<red>Usage: /controls <offhand|command>"),
    CONTROLS_INVALID_PARAM("controls.invalid_param", "<red>Invalid option. Use \"offhand\" or \"command\"."),

    INFUSE_INVALID_PARAM("infuse.invalid_param", "<red>Please use the tab completions as a reference."),
    INFUSE_INVALID_SLOT("infuse.invalid_slot", "<red>Invalid Argument! Could not identify slot %slot%.  Please use \"1\" or \"2\"."),
    INFUSE_CONTROLS_USAGE("infuse_controls.usage", "<red>Usage: /infuse controls <offhand|command>"),
    INFUSE_CONTROLS_SUCCESS("infuse_controls.success", "<dark_red>Your controls are now %controlMode%"),

    INFUSE_SETEFFECT_USAGE("infuse_seteffect.usage", "<red>Invalid Argument! Please use /infuse setEffect <player> <aug_fire|ocean> <1|2>"),
    INFUSE_SETEFFECT_SUCCESS("infuse_seteffect.success", "<green>Successfully set the effect in slot %slot% of player %player_name% to %effect_name%."),

    INFUSE_GIVEEFFECT_USAGE("infuse_geteffect.usage", "<red>Invalid Argument! Please use /infuse giveEffect <player> <aug_fire|ocean>"),
    INFUSE_GIVEEFFECT_SUCCESS("infuse_geteffect.success", "%effect_color%You recieved the %effect_name%"),

    INFUSE_CLEAREFFECT_USAGE("infuse_cleareffect.usage", "<red>Invalid Argument! Please use /infuse clearEffect <player>"),
    INFUSE_CLEAREFFECT_SUCCESS("infuse_cleareffect.success", "<green>Cleared %player_name%'s effects"),

    INFUSE_COOLDOWN_USAGE("infuse_cooldown.usage", "<red>Invalid Argument! Please use /infuse cooldown <player>"),
    INFUSE_COOLDOWN_SUCCESS("infuse_cooldown.success", "<green>Removed %player_name%'s cooldown"),

    CLEAREFFECTS_USAGE("cleareffects.usage", "<red>Usage: /infuse clearEffects <player>"),

    JOIN_ABILITY_NOTIFY("onjoin.ability_notify", "<gray>Your ability mode is set to: %control_mode%"),
    
    DRAIN_SUCCESS("drain.success", "<green>You have drained your: %effect_name%"),

    EFFECT_EQUIPPED("effect_equipped", "<green>You have equipped the %effect_name%"),

    SWAP_NO_EFFECTS("swap_no_effects", "<red>You do not have any effects equipped to swap."),
    SWAP_SUCCESS("swap_success", "<green>Your Effects have been swapped."),

    THIEF_STEAL("thief_steal", "<yellow>You stole %victim%'s %effect_name% Effect"),

    RECIPE_NOT_FOUND("recipe_not_found", "<red>No recipe found for this potion."),
    RECIPE_DISABLED("recipe_disabled", "Recipe is disabled/broken"),

    ERROR_INV_FULL("errors.inv_full", "<red>Your inventory is full! Make space before unequipping."),
    ERROR_NOT_PLAYER("errors.not_player", "<red>Only players can use this command."),
    ERROR_NOT_OP("errors.not_op", "<red>You must be OP to run this command."),
    ERROR_INVALID_COMMAND("errors.invalid_command", "<red>Invalid command."),
    ERROR_RITUAL_ACTIVE("errors.ritual_active", "<red>A ritual is already in progress!"),
    ERROR_TARGET_NOT_FOUND("errors.target_not_found", "<red>Player not found or not online."),

    // Effect messages
    EMERALD_NAME("emerald.effect_name", "<green>Emerald Effect"),
    EMERALD_LORE("emerald.effect_lore", "<green><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<green>$ <gray>Looting 5", "<green>$ <gray>Luck 10", "<green>$ <gray>1.5x EXP", "<green>$ <gray>Consumables have a 15% chance of not being consumed", "<green>$ <gray>Enchanting table always on level 30", "<gray>", "<green><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<green>$ <gray>Hero of the village 255", "<green>$ <gray>Consumables have a 25% chance of not being consumed", "<green>$ <gray>3x EXP", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_EMERALD_NAME("aug_emerald.effect_name", "<green>Augmented Emerald Effect"),
    AUG_EMERALD_LORE("aug_emerald.effect_lore", "<green><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<green>$ <gray>Looting 5", "<green>$ <gray>Luck 10", "<green>$ <gray>1.5x EXP", "<green>$ <gray>Consumables have a 15% chance of not being consumed", "<green>$ <gray>Enchanting table always on level 30", "<gray>", "<green><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<green>$ <gray>Hero of the village 255", "<green>$ <gray>Consumables have a 25% chance of not being consumed", "<green>$ <gray>3x EXP", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    ENDER_NAME("ender.effect_name", "<dark_purple>Ender Effect"),
    ENDER_LORE("ender.effect_lore", "<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <gray>All nearby untrusted players have glowing", "<dark_purple>⭐ <gray>Use dragon's breath to shoot powerful fireballs that curse players", "<dark_purple>⭐ <gray>Curse untrusted players on hit which shares damage with all", "<dark_purple>⭐ <gray>cursed players", "<gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <gray>Teleport to the cursor position within a 15 block radius", "<dark_purple>⭐ <gray>Instantly kills any mob and curses players", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 10s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),
    AUG_ENDER_NAME("aug_ender.effect_name", "<dark_purple>Augmented Ender Effect"),
    AUG_ENDER_LORE("aug_ender.effect_lore", "<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <gray>All nearby untrusted players have glowing", "<dark_purple>⭐ <gray>Use dragon's breath to shoot powerful fireballs that curse players", "<dark_purple>⭐ <gray>Curse untrusted players on hit which shares damage with all", "<dark_purple>⭐ <gray>cursed players", "<gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <gray>Teleport to the cursor position within a 15 block radius", "<dark_purple>⭐ <gray>Instantly kills any mob and curses players", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

    FEATHER_NAME("feather.effect_name", "<#BEA3CA>Feather Effect"),
    FEATHER_LORE("feather.effect_lore", "<#BEA3CA><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>No fall damage", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Attacking from 7+ block fall does a mace hit", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Auto windcharge counter after being attacked 10 times", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 0.5x cooldown", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 2x velocity", "<gray>", "<#BEA3CA><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Launches the player upward", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Slams the player back down", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 2s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_FEATHER_NAME("aug_feather.effect_name", "<#BEA3CA>Augmented Feather Effect"),
    AUG_FEATHER_LORE("aug_feather.effect_lore", "<#BEA3CA><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>No fall damage", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Attacking from 7+ block fall does a mace hit", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Auto windcharge counter after being attacked 10 times", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 0.5x cooldown", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 2x velocity", "<gray>", "<#BEA3CA><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Launches the player upward", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Slams the player back down", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 2s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    FIRE_NAME("fire.effect_name", "<#E85720>Fire Effect"),
    FIRE_LORE("fire.effect_lore", "<#E85720><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E85720>\ud83d\udd25 <dark_gray>Fire Resistance", "<#E85720>\ud83d\udd25 <dark_gray>Full charged bow shots set arrows on fire", "<#E85720>\ud83d\udd25 <dark_gray>In lava, no fall damage", "<#E85720>\ud83d\udd25 <dark_gray>Every 10 hits sets target on fire for 5s", "<gray>", "<#E85720><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#E85720>\ud83d\udd25 <dark_gray>Set surrounding enemies on fire (5 block radius)", "<gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_FIRE_NAME("aug_fire.effect_name", "<#E85720>Augmented Fire Effect"),
    AUG_FIRE_LORE("aug_fire.effect_lore", "<#E85720><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E85720>\ud83d\udd25 <dark_gray>Fire Resistance", "<#E85720>\ud83d\udd25 <dark_gray>Full charged bow shots set arrows on fire", "<#E85720>\ud83d\udd25 <dark_gray>In lava, no fall damage", "<#E85720>\ud83d\udd25 <dark_gray>Every 10 hits sets target on fire for 5s", "<gray>", "<#E85720><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#E85720>\ud83d\udd25 <dark_gray>Set surrounding enemies on fire (5 block radius)", "<gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    FROST_NAME("frost.effect_name", "<aqua>Frost Effect"),
    FROST_LORE("frost.effect_lore", "<aqua>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <gray>Speed 3 on ice and snow", "<aqua>❄ <gray>Freeze player every 10 hits", "<aqua>❄ <gray>Frozen enemies can't use windcharges", "<gray>", "<aqua>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <gray>Reduce enemies jump strength and freeze them every hit", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
    AUG_FROST_NAME("aug_frost.effect_name", "<aqua>Augmented Frost Effect"),
    AUG_FROST_LORE("aug_frost.effect_lore", "<aqua>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <gray>Speed 3 on ice and snow", "<aqua>❄ <gray>Freeze player every 10 hits", "<aqua>❄ <gray>Frozen enemies can't use windcharges", "<gray>", "<aqua>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <gray>Reduce enemies jump strength and freeze them every hit", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),

    HASTE_NAME("haste.effect_name", "<gold>Haste Effect"),
    HASTE_LORE("haste.effect_lore", "<gold>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<gold>⛏ <gray>Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes", "<gold>⛏ <gray>Halved shield cooldown when stunned", "<gray>", "<gold>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<gold>⛏ <gray>Attack faster", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_HASTE_NAME("aug_haste.effect_name", "<gold>Augmented Haste Effect"),
    AUG_HASTE_LORE("aug_haste.effect_lore", "<gold>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<gold>⛏ <gray>Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes", "<gold>⛏ <gray>Halved shield cooldown when stunned", "<gray>", "<gold>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<gold>⛏ <gray>Attack faster", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    HEART_NAME("heart.effect_name", "<red>Heart Effect"),
    HEART_LORE("heart.effect_lore", "<red>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <gray>+5 Hearts", "<red>❤ <gray>All food gives absorption", "<red>❤ <gray>Egaps gives +10 absorption hearts", "<red>❤ <gray>See player's health every 10 hits", "<gray>", "<red>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <gray>Heal players to 20 hearts instantly", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 60s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 120s"),
    AUG_HEART_NAME("aug_heart.effect_name", "<red>Augmented Heart Effect"),
    AUG_HEART_LORE("aug_heart.effect_lore", "<red>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <gray>+5 Hearts", "<red>❤ <gray>All food gives absorption", "<red>❤ <gray>Egaps gives +10 absorption hearts", "<red>❤ <gray>See player's health every 10 hits", "<gray>", "<red>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <gray>Heal players to 20 hearts instantly", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 60s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),

    INVIS_NAME("invisibility.effect_name", "<dark_purple>Invisibility Effect"),
    INVIS_LORE("invisibility.effect_lore", "<dark_purple>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>\ud83d\udc41 <gray>Permanent Invisibility", "<dark_purple>\ud83d\udc41 <gray>Full bow shot blinds the target for 5s and gives blindness for 2s", "<dark_purple>\ud83d\udc41 <gray>Mobs cannot target you", "<gray>", "<dark_purple>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>\ud83d\udc41 <gray>Creates a 5×5 hollow circle of black dust particles", "<dark_purple>\ud83d\udc41 <gray>Inside: allies become fully invisible; enemies get blindness", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
    AUG_INVIS_NAME("aug_invisibility.effect_name", "<dark_purple>Augmented Invisibility Effect"),
    AUG_INVIS_LORE("aug_invisibility.effect_lore", "<dark_purple>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>\ud83d\udc41 <gray>Permanent Invisibility", "<dark_purple>\ud83d\udc41 <gray>Full bow shot blinds the target for 5s and gives blindness for 2s", "<dark_purple>\ud83d\udc41 <gray>Mobs cannot target you", "<gray>", "<dark_purple>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>\ud83d\udc41 <gray>Creates a 5×5 hollow circle of black dust particles", "<dark_purple>\ud83d\udc41 <gray>Inside: allies become fully invisible; enemies get blindness", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),

    OCEAN_NAME("ocean.effect_name", "<blue>Ocean Effect"),
    OCEAN_LORE("ocean.effect_lore", "<blue><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<blue>🫧 <gray>Swim faster", "<blue>🫧 <gray>Breathe underwater", "<blue>🫧 <gray>Make everyone around you start drowning when in water", "<blue>🫧 <gray>Tridents pull players", "<gray>", "<gold><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<blue>🫧 <gray>Creates a Whirlhole", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_OCEAN_NAME("aug_ocean.effect_name", "<blue>Augmented Ocean Effect"),
    AUG_OCEAN_LORE("aug_ocean.effect_lore", "<blue><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<blue>🫧 <gray>Swim faster", "<blue>🫧 <gray>Breathe underwater", "<blue>🫧 <gray>Make everyone around you start drowning when in water", "<blue>🫧 <gray>Tridents pull players", "<gray>", "<gold><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<blue>🫧 <gray>Creates a Whirlhole", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    REGEN_NAME("regen.effect_name", "<red>Regeneration Effect"),
    REGEN_LORE("regen.effect_lore", "<gold>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<gold>\ud83d\ude91 <gray>Every hit grants Regeneration 2 for 1 second", "<gold>\ud83d\ude91 <gray>Healing gives 3 extra saturation bars", "<gray>", "<gold>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<gold>\ud83d\ude91 <gray>Apply lifesteal effect that heals you based on your damage dealt", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_REGEN_NAME("aug_regen.effect_name", "<red>Augmented Regeneration Effect"),
    AUG_REGEN_LORE("aug_regen.effect_lore", "<gold>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<gold>\ud83d\ude91 <gray>Every hit grants Regeneration 2 for 1 second", "<gold>\ud83d\ude91 <gray>Healing gives 3 extra saturation bars", "<gray>", "<gold>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<gold>\ud83d\ude91 <gray>Apply lifesteal effect that heals you based on your damage dealt", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

    SPEED_NAME("speed.effect_name", "<#E8BD74>Speed Effect"),
    SPEED_LORE("speed.effect_lore", "<#E8BD74><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed 1", "<#E8BD74>⋘ <dark_gray>Increase speed level by 1 after each hit", "<#E8BD74>⋘ <dark_gray>Speed resets after 1 second of no activity", "<#E8BD74>⋘ <dark_gray>Ranged weapons charge 1.5x faster", "<#E8BD74>⋘ <dark_gray>Enemy invincibility frames are halved", "<gray>", "<#E8BD74><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed Dash", "<gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 20s"),
    AUG_SPEED_NAME("aug_speed.effect_name", "<#E8BD74>Augmented Speed Effect"),
    AUG_SPEED_LORE("aug_speed.effect_lore", "<#E8BD74><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed 1", "<#E8BD74>⋘ <dark_gray>Increase speed level by 1 after each hit", "<#E8BD74>⋘ <dark_gray>Speed resets after 1 second of no activity", "<#E8BD74>⋘ <dark_gray>Ranged weapons charge 1.5x faster", "<#E8BD74>⋘ <dark_gray>Enemy invincibility frames are halved", "<gray>", "<#E8BD74><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed Dash", "<gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 10s"),

    STRENGTH_NAME("strength.effect_name", "<dark_red>Strength Effect"),
    STRENGTH_LORE("strength.effect_lore", "<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>Double Damage to all mobs", "<dark_red>\ud83d\udee1 <dark_gray>Disable shields for 10 seconds", "<dark_red>\ud83d\udee1 <dark_gray>Ranged weapons pierce shields", "<dark_red>\ud83d\udee1 <dark_gray>+1 Damage when under 6 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+2 Damage when under 4 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+3 Damage when under 2 hearts", "<gray>", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>All attacks are critical for 15 seconds", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_STRENGTH_NAME("aug_strength.effect_name", "<dark_red>Augmented Strength Effect"),
    AUG_STRENGTH_LORE("aug_strength.effect_lore", "<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>Double Damage to all mobs", "<dark_red>\ud83d\udee1 <dark_gray>Disable shields for 10 seconds", "<dark_red>\ud83d\udee1 <dark_gray>Ranged weapons pierce shields", "<dark_red>\ud83d\udee1 <dark_gray>+1 Damage when under 6 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+2 Damage when under 4 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+3 Damage when under 2 hearts", "<gray>", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>All attacks are critical for 15 seconds", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    THUNDER_NAME("thunder.effect_name", "<yellow>Thunder Effect"),
    THUNDER_LORE("thunder.effect_lore", "<yellow>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <gray>Chain lightning", "<yellow>⚡ <gray>Tridents Strikes Lightning ", "<gray>", "<yellow>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <gray>Strike enemies with lightning and make a thunderstorm", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_THUNDER_NAME("aug_thunder.effect_name", "<yellow>Augmented Thunder Effect"),
    AUG_THUNDER_LORE("aug_thunder.effect_lore", "<yellow>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <gray>Chain lightning", "<yellow>⚡ <gray>Tridents Strikes Lightning ", "<gray>", "<yellow>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <gray>Strike enemies with lightning and make a thunderstorm", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    // Extra effect messages
    APOPHIS_NAME("apophis.effect_name", "<dark_purple>Apophis Effect"),
    APOPHIS_LORE("apophis.effect_lore", "<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <gray>Combine Fire, Emerald and Heart's effects", "<dark_purple>🍼 <gray>Have a custom skin and nametag", "<gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <gray>Activate Fire, Emerald and Heart's sparks", "<dark_purple>🍼 <gray>Upon hitting a player blind their screen", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 2m"),
    AUG_APOPHIS_NAME("aug_apophis.effect_name", "<dark_purple>Augmented Apophis Effect"),
    AUG_APOPHIS_LORE("aug_apophis.effect_lore", "<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <gray>Combine Fire, Emerald and Heart's effects", "<dark_purple>🍼 <gray>Have a custom skin and nametag", "<gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <gray>Activate Fire, Emerald and Heart's sparks", "<dark_purple>🍼 <gray>Upon hitting a player blind their screen", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 1m 30s"),
    
    THIEF_NAME("thief.effect_name", "<dark_red>Thief Effect"),
    THIEF_LORE("thief.effect_lore", "<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <gray>You're not shown on tablist", "<dark_red>🥷 <gray>Your footsteps don't make noise", "<dark_red>🥷 <gray>Kill a player to shapeshift into them", "", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <gray>Temporarily steal your opponents effect", "", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: Unknown", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: Unknown"),
    AUG_THIEF_NAME("aug_thief.effect_name", "<dark_red>Augmented Thief Effect"),
    AUG_THIEF_LORE("aug_thief.effect_lore", "<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <gray>You're not shown on tablist", "<dark_red>🥷 <gray>Your footsteps don't make noise", "<dark_red>🥷 <gray>Kill a player to shapeshift into them", "<gray>", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <gray>Temporarily steal your opponents effect", "<gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: Unknown", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: Unknown");

    // Config and config files
    public static final File file = new File("plugins/Infuse/messages.yml");
    public static final YamlConfiguration config = new YamlConfiguration();

    // Text serializers
    public static final MiniMessage minimessage = MiniMessage.miniMessage();
    public static final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();

    // Enum attributes
    public final String configKey;
    public final String defaultValue;

    Messages(String configKey, String defaultValue) {
        this.configKey = configKey;
        this.defaultValue = defaultValue;
    }

    Messages(String configKey, String... defaultLines) {
        this.configKey = configKey;
        StringBuilder builder = new StringBuilder();
        for (String line : defaultLines) {
            builder.append(line);
            builder.append("\n");
        }
        this.defaultValue = builder.toString();
    }

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

    /**
     * Gets a message from the config.
     * 
     * @param key The message to retrieve.
     * 
     * @return A message from the config
     */
    public static String getMessage(Messages key) {
        if (!config.contains(key.configKey)) {
            Bukkit.getLogger().severe("Could not find \"" + key.configKey + "\" in the config.");
            return key.defaultValue;
        }

        // If the config is a list, it converts it into a single string separated by newlines.
        // Otherwise, it just returns the string.
        if (config.isList(key.configKey)) {
            StringBuilder retVal = new StringBuilder();
            for (String line : config.getStringList(key.configKey)) {
                retVal.append(line).append("\n");
            }

            return retVal.substring(0, retVal.length() - 1);
        } else {
            return config.getString(key.configKey);
        }
    }

    public String getMessage() {
        if (!config.contains(configKey)) {
            Bukkit.getLogger().severe("Could not find \"" + configKey + "\" in the config.");
            return defaultValue;
        }

        // If the config is a list, it converts it into a single string separated by newlines.
        // Otherwise, it just returns the string.
        if (config.isList(configKey)) {
            StringBuilder retVal = new StringBuilder();
            for (String line : config.getStringList(configKey)) {
                retVal.append(line).append("\n");
            }

            return retVal.substring(0, retVal.length() - 1);
        } else {
            return config.getString(configKey);
        }
    }

    public List<String> getStringList() {
        return List.of(getMessage().split("\n"));
    }

    public List<Component> getComponentList() {
        return getStringList().stream().map(Messages::toComponent).toList();
    }

    /**
     * Converts a message to a component by passing it through legacy and minimessage formatting.
     * 
     * @param message The message enum to convert.
     * 
     * @return The component value of the message.
     */
    public static Component toComponent(Messages message) {
        return toComponent(getMessage(message));
    }

    public Component toComponent() {
        return toComponent(getMessage(this));
    }

    /**
     * Converts a message to a component by passing it through legacy and minimessage formatting.
     * Also applies the default placeholders.
     * 
     * @param message The string to convert.
     * 
     * @return The component value of the message.
     */
    public static Component toComponent(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}