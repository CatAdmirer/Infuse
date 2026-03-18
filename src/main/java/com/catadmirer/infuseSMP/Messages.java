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
    EFFECT_BROADCAST("effect_broadcast", "🧪 %player% is cooking up the %item%<reset> at %x%, %y%, %z%... %dimension%"),
    DISCORD_BROADCAST("discord_broadcast", "%player% is cooking up the %item% at %x%, %y%, %z% in %dimension% @everyone"),
    EFFECT_FINISHED("effect_finished", "%item% has been brewed!"),

    REGULAR_BROADCAST("regular_effect_broadcast", "🧪 A %item%<reset> has been crafted at <#90D5FF><b>%x%, %y%, %z%... %dimension%"),

    SLOT_EMPTY("slot_empty", "<red>You don't have any effect equipped in slot %slot%."),
    EFFECT_NONE_EQUIPPED("effect_none_equipped", "<red>You don't have an Effect equipped in slot %slot%."),

    WITHDRAW_INVALID("withdraw_invalid", "<red>Invalid usage. Use /ldrain or /rdrain"),

    TRUST_CONSOLEUSAGE("trust_consoleusage", "<red>Only players can use this command."),
    TRUST_INCORRECTUSAGE("trust_incorrectusage", "<red>Usage: /%label% <player>"),
    TRUST_NOPLAYER("trust_noplayer", "<red>Player not found."),
    TRUST_SELF("trust_self", "<red>You always trust yourself. Surely..."),
    TRUST_ADDED("trust_added", "<green>You now trust %target%."),
    TRUST_ALREADYTRUSTED("trust_alreadytrusted", "<green>You already trust %target%."),
    TRUST_REMOVED("trust_removed", "<green>You no longer trust %target%."),
    TRUST_NOTTRUSTED("trust_nottrusted", "<green>You already didn't trust %target%."),

    EFFECT_NOBREWING("effect_nobrewing", "<red>You need to craft this in a brewing stand!"),
    DEATH_MESSAGE("death_message", "%victim% was slain by %killer%"),

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
    EMERALD_NAME("emerald.effect_name", "<#009420>Emerald Effect"),
    EMERALD_LORE("emerald.effect_lore", "<#009420><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#009420>$ <dark_gray>Looting 5", "<#009420>$ <dark_gray>Luck 10", "<#009420>$ <dark_gray>1.5x EXP", "<#009420>$ <dark_gray>Consumables have a 15% chance of not being consumed", "<#009420>$ <dark_gray>Enchanting table always on level 30", "<dark_gray>", "<#009420><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#009420>$ <dark_gray>Hero of the village 255", "<#009420>$ <dark_gray>Consumables have a 25% chance of not being consumed", "<#009420>$ <dark_gray>3x EXP", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_EMERALD_NAME("aug_emerald.effect_name", "<#009420>Augmented Emerald Effect"),
    AUG_EMERALD_LORE("aug_emerald.effect_lore", "<#009420><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#009420>$ <dark_gray>Looting 5", "<#009420>$ <dark_gray>Luck 10", "<#009420>$ <dark_gray>1.5x EXP", "<#009420>$ <dark_gray>Consumables have a 15% chance of not being consumed", "<#009420>$ <dark_gray>Enchanting table always on level 30", "<dark_gray>", "<#009420><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#009420>$ <dark_gray>Hero of the village 255", "<#009420>$ <dark_gray>Consumables have a 25% chance of not being consumed", "<#009420>$ <dark_gray>3x EXP", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    ENDER_NAME("ender.effect_name", "<dark_purple>Ender Effect"),
    ENDER_LORE("ender.effect_lore", "<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <dark_gray>All nearby untrusted players have glowing", "<dark_purple>⭐ <dark_gray>Use dragon's breath to shoot powerful fireballs that curse players", "<dark_purple>⭐ <dark_gray>Curse untrusted players on hit which shares damage with all", "<dark_purple>⭐ <dark_gray>cursed players", "<dark_gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <dark_gray>Teleport to the cursor position within a 15 block radius", "<dark_purple>⭐ <dark_gray>Instantly kills any mob and curses players", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 10s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),
    AUG_ENDER_NAME("aug_ender.effect_name", "<dark_purple>Augmented Ender Effect"),
    AUG_ENDER_LORE("aug_ender.effect_lore", "<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <dark_gray>All nearby untrusted players have glowing", "<dark_purple>⭐ <dark_gray>Use dragon's breath to shoot powerful fireballs that curse players", "<dark_purple>⭐ <dark_gray>Curse untrusted players on hit which shares damage with all", "<dark_purple>⭐ <dark_gray>cursed players", "<dark_gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <dark_gray>Teleport to the cursor position within a 15 block radius", "<dark_purple>⭐ <dark_gray>Instantly kills any mob and curses players", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

    FEATHER_NAME("feather.effect_name", "<#BEA3CA>Feather Effect"),
    FEATHER_LORE("feather.effect_lore", "<#BEA3CA><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>No fall damage", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Attacking from 7+ block fall does a mace hit", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Auto windcharge counter after being attacked 10 times", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 0.5x cooldown", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 2x velocity", "<dark_gray>", "<#BEA3CA><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Launches the player upward", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Slams the player back down", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 2s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_FEATHER_NAME("aug_feather.effect_name", "<#BEA3CA>Augmented Feather Effect"),
    AUG_FEATHER_LORE("aug_feather.effect_lore", "<#BEA3CA><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>No fall damage", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Attacking from 7+ block fall does a mace hit", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Auto windcharge counter after being attacked 10 times", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 0.5x cooldown", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 2x velocity", "<dark_gray>", "<#BEA3CA><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Launches the player upward", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Slams the player back down", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 2s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    FIRE_NAME("fire.effect_name", "<#E85720>Fire Effect"),
    FIRE_LORE("fire.effect_lore", "<#E85720><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E85720>\ud83d\udd25 <dark_gray>Fire Resistance", "<#E85720>\ud83d\udd25 <dark_gray>Full charged bow shots set arrows on fire", "<#E85720>\ud83d\udd25 <dark_gray>In lava, no fall damage", "<#E85720>\ud83d\udd25 <dark_gray>Every 10 hits sets target on fire for 5s", "<dark_gray>", "<#E85720><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#E85720>\ud83d\udd25 <dark_gray>Set surrounding enemies on fire (5 block radius)", "<dark_gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_FIRE_NAME("aug_fire.effect_name", "<#E85720>Augmented Fire Effect"),
    AUG_FIRE_LORE("aug_fire.effect_lore", "<#E85720><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E85720>\ud83d\udd25 <dark_gray>Fire Resistance", "<#E85720>\ud83d\udd25 <dark_gray>Full charged bow shots set arrows on fire", "<#E85720>\ud83d\udd25 <dark_gray>In lava, no fall damage", "<#E85720>\ud83d\udd25 <dark_gray>Every 10 hits sets target on fire for 5s", "<dark_gray>", "<#E85720><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#E85720>\ud83d\udd25 <dark_gray>Set surrounding enemies on fire (5 block radius)", "<dark_gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    FROST_NAME("frost.effect_name", "<aqua>Frost Effect"),
    FROST_LORE("frost.effect_lore", "<aqua><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <dark_gray>Speed 3 on ice and snow", "<aqua>❄ <dark_gray>Freeze player every 10 hits", "<aqua>❄ <dark_gray>Frozen enemies can't use windcharges", "<dark_gray>", "<aqua><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <dark_gray>Reduce enemies jump strength and freeze them every hit", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
    AUG_FROST_NAME("aug_frost.effect_name", "<aqua>Augmented Frost Effect"),
    AUG_FROST_LORE("aug_frost.effect_lore", "<aqua><bold><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <dark_gray>Speed 3 on ice and snow", "<aqua>❄ <dark_gray>Freeze player every 10 hits", "<aqua>❄ <dark_gray>Frozen enemies can't use windcharges", "<dark_gray>", "<aqua><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <dark_gray>Reduce enemies jump strength and freeze them every hit", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),

    HASTE_NAME("haste.effect_name", "<#BD934F>Haste Effect"),
    HASTE_LORE("haste.effect_lore", "<#BD934F><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BD934F>⛏ <dark_gray>Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes", "<#BD934F>⛏ <dark_gray>Halved shield cooldown when stunned", "<dark_gray>", "<#BD934F><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BD934F>⛏ <dark_gray>Attack faster", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_HASTE_NAME("aug_haste.effect_name", "<#BD934F>Augmented Haste Effect"),
    AUG_HASTE_LORE("aug_haste.effect_lore", "<#BD934F><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BD934F>⛏ <dark_gray>Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes", "<#BD934F>⛏ <dark_gray>Halved shield cooldown when stunned", "<dark_gray>", "<#BD934F><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BD934F>⛏ <dark_gray>Attack faster", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    HEART_NAME("heart.effect_name", "<red>Heart Effect"),
    HEART_LORE("heart.effect_lore", "<red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <dark_gray>+5 Hearts", "<red>❤ <dark_gray>All food gives absorption", "<red>❤ <dark_gray>Egaps gives +10 absorption hearts", "<red>❤ <dark_gray>See player's health every 10 hits", "<dark_gray>", "<red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <dark_gray>Heal players to 20 hearts instantly", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 60s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 120s"),
    AUG_HEART_NAME("aug_heart.effect_name", "<red>Augmented Heart Effect"),
    AUG_HEART_LORE("aug_heart.effect_lore", "<red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <dark_gray>+5 Hearts", "<red>❤ <dark_gray>All food gives absorption", "<red>❤ <dark_gray>Egaps gives +10 absorption hearts", "<red>❤ <dark_gray>See player's health every 10 hits", "<dark_gray>", "<red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <dark_gray>Heal players to 20 hearts instantly", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 60s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),

    INVIS_NAME("invisibility.effect_name", "<#2B0078>Invis Effect"),
    INVIS_LORE("invisibility.effect_lore", "<#2B0078><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#2B0078>\ud83d\udc41 <dark_gray>Permanent Invisibility", "<#2B0078>\ud83d\udc41 <dark_gray>Full bow shot blinds the target for 5s and gives blindness for 2s", "<#2B0078>\ud83d\udc41 <dark_gray>Mobs cannot target you", "<dark_gray>", "<#2B0078><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#2B0078>\ud83d\udc41 <dark_gray>Creates a 5×5 hollow circle of black dust particles", "<#2B0078>\ud83d\udc41 <dark_gray>Inside: allies become fully invisible; enemies get blindness", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
    AUG_INVIS_NAME("aug_invisibility.effect_name", "<#2B0078>Augmented Invis Effect"),
    AUG_INVIS_LORE("aug_invisibility.effect_lore", "<#2B0078><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#2B0078>\ud83d\udc41 <dark_gray>Permanent Invisibility", "<#2B0078>\ud83d\udc41 <dark_gray>Full bow shot blinds the target for 5s and gives blindness for 2s", "<#2B0078>\ud83d\udc41 <dark_gray>Mobs cannot target you", "<dark_gray>", "<#2B0078><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#2B0078>\ud83d\udc41 <dark_gray>Creates a 5×5 hollow circle of black dust particles", "<#2B0078>\ud83d\udc41 <dark_gray>Inside: allies become fully invisible; enemies get blindness", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),

    OCEAN_NAME("ocean.effect_name", "<blue>Ocean Effect"),
    OCEAN_LORE("ocean.effect_lore", "<blue><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<blue>🫧 <dark_gray>Swim faster", "<blue>🫧 <dark_gray>Breathe underwater", "<blue>🫧 <dark_gray>Make everyone around you start drowning when in water", "<blue>🫧 <dark_gray>Tridents pull players", "<dark_gray>", "<blue><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<blue>🫧 <dark_gray>Creates a Whirlhole", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_OCEAN_NAME("aug_ocean.effect_name", "<blue>Augmented Ocean Effect"),
    AUG_OCEAN_LORE("aug_ocean.effect_lore", "<blue><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<blue>🫧 <dark_gray>Swim faster", "<blue>🫧 <dark_gray>Breathe underwater", "<blue>🫧 <dark_gray>Make everyone around you start drowning when in water", "<blue>🫧 <dark_gray>Tridents pull players", "<dark_gray>", "<blue><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<blue>🫧 <dark_gray>Creates a Whirlhole", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    REGEN_NAME("regen.effect_name", "<#FC00DD>Regen Effect"),
    REGEN_LORE("regen.effect_lore", "<#B0009A><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#FC00DD>+ <dark_gray>No hunger loss", "<#FC00DD>+ <dark_gray>Permanent Regeneration", "<#FC00DD>+ <dark_gray>Every hit gives Regeneration 2 for 3.0 seconds", "<#FC00DD>+ <dark_gray>All food gives +3.0 saturation bars", "<#FC00DD>+ <dark_gray>Eat anytime", "<#FC00DD>+ <dark_gray>10th hit takes away 1.0 hunger bar from your target", "<dark_gray>", "<#B0009A>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#FC00DD>+ <dark_gray>Damage dealt heals you and nearby teammates", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_REGEN_NAME("aug_regen.effect_name", "<#FC00DD>Augmented Regen Effect"),
    AUG_REGEN_LORE("aug_regen.effect_lore", "<#B0009A><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#FC00DD>+ <dark_gray>No hunger loss", "<#FC00DD>+ <dark_gray>Permanent Regeneration", "<#FC00DD>+ <dark_gray>Every hit gives Regeneration 2 for 3.0 seconds", "<#FC00DD>+ <dark_gray>All food gives +3.0 saturation bars", "<#FC00DD>+ <dark_gray>Eat anytime", "<#FC00DD>+ <dark_gray>10th hit takes away 1.0 hunger bar from your target", "<dark_gray>", "<#B0009A>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#FC00DD>+ <dark_gray>Damage dealt heals you and nearby teammates", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

    SPEED_NAME("speed.effect_name", "<#E8BD74>Speed Effect"),
    SPEED_LORE("speed.effect_lore", "<#E8BD74><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed 1", "<#E8BD74>⋘ <dark_gray>Increase speed level by 1 after each hit", "<#E8BD74>⋘ <dark_gray>Speed resets after 1 second of no activity", "<#E8BD74>⋘ <dark_gray>Ranged weapons charge 1.5x faster", "<#E8BD74>⋘ <dark_gray>Enemy invincibility frames are halved", "<dark_gray>", "<#E8BD74><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed Dash", "<dark_gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 20s"),
    AUG_SPEED_NAME("aug_speed.effect_name", "<#E8BD74>Augmented Speed Effect"),
    AUG_SPEED_LORE("aug_speed.effect_lore", "<#E8BD74><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed 1", "<#E8BD74>⋘ <dark_gray>Increase speed level by 1 after each hit", "<#E8BD74>⋘ <dark_gray>Speed resets after 1 second of no activity", "<#E8BD74>⋘ <dark_gray>Ranged weapons charge 1.5x faster", "<#E8BD74>⋘ <dark_gray>Enemy invincibility frames are halved", "<dark_gray>", "<#E8BD74><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed Dash", "<dark_gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 10s"),

    STRENGTH_NAME("strength.effect_name", "<dark_red>Strength Effect"),
    STRENGTH_LORE("strength.effect_lore", "<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>Double Damage to all mobs", "<dark_red>\ud83d\udee1 <dark_gray>Disable shields for 10 seconds", "<dark_red>\ud83d\udee1 <dark_gray>Ranged weapons pierce shields", "<dark_red>\ud83d\udee1 <dark_gray>+1 Damage when under 6 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+2 Damage when under 4 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+3 Damage when under 2 hearts", "<dark_gray>", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>All attacks are critical for 15 seconds", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_STRENGTH_NAME("aug_strength.effect_name", "<dark_red>Augmented Strength Effect"),
    AUG_STRENGTH_LORE("aug_strength.effect_lore", "<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>Double Damage to all mobs", "<dark_red>\ud83d\udee1 <dark_gray>Disable shields for 10 seconds", "<dark_red>\ud83d\udee1 <dark_gray>Ranged weapons pierce shields", "<dark_red>\ud83d\udee1 <dark_gray>+1 Damage when under 6 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+2 Damage when under 4 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+3 Damage when under 2 hearts", "<dark_gray>", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>All attacks are critical for 15 seconds", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    THUNDER_NAME("thunder.effect_name", "<yellow>Thunder Effect"),
    THUNDER_LORE("thunder.effect_lore", "<yellow><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <dark_gray>Chain lightning", "<yellow>⚡ <dark_gray>Tridents Strikes Lightning ", "<dark_gray>", "<yellow><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <dark_gray>Strike enemies with lightning and make a thunderstorm", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_THUNDER_NAME("aug_thunder.effect_name", "<yellow>Augmented Thunder Effect"),
    AUG_THUNDER_LORE("aug_thunder.effect_lore", "<yellow><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <dark_gray>Chain lightning", "<yellow>⚡ <dark_gray>Tridents Strikes Lightning ", "<dark_gray>", "<yellow><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <dark_gray>Strike enemies with lightning and make a thunderstorm", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    
    // Extra effect messages
    APOPHIS_NAME("apophis.effect_name", "<dark_purple>Apophis Effect"),
    APOPHIS_LORE("apophis.effect_lore", "<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <dark_gray>Combine Fire, Emerald and Heart's effects", "<dark_purple>🍼 <dark_gray>Have a custom skin and nametag", "<dark_gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <dark_gray>Activate Fire, Emerald and Heart's sparks", "<dark_purple>🍼 <dark_gray>Upon hitting a player blind their screen", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 2m"),
    AUG_APOPHIS_NAME("aug_apophis.effect_name", "<dark_purple>Augmented Apophis Effect"),
    AUG_APOPHIS_LORE("aug_apophis.effect_lore", "<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <dark_gray>Combine Fire, Emerald and Heart's effects", "<dark_purple>🍼 <dark_gray>Have a custom skin and nametag", "<dark_gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <dark_gray>Activate Fire, Emerald and Heart's sparks", "<dark_purple>🍼 <dark_gray>Upon hitting a player blind their screen", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 1m 30s"),
    
    THIEF_NAME("thief.effect_name", "<dark_red>Thief Effect"),
    THIEF_LORE("thief.effect_lore", "<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <dark_gray>You're not shown on tablist", "<dark_red>🥷 <dark_gray>Your footsteps don't make noise", "<dark_red>🥷 <dark_gray>Kill a player to disguise yourself as them", "", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <dark_gray>Temporarily steal your opponents effect", "", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: Unknown", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: Unknown"),
    AUG_THIEF_NAME("aug_thief.effect_name", "<dark_red>Augmented Thief Effect"),
    AUG_THIEF_LORE("aug_thief.effect_lore", "<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <dark_gray>You're not shown on tablist", "<dark_red>🥷 <dark_gray>Your footsteps don't make noise", "<dark_red>🥷 <dark_gray>Kill a player to disguise yourself as them", "<dark_gray>", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <dark_gray>Temporarily steal your opponents effect", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: Unknown", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: Unknown");

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
            config.set(key.configKey, key.defaultValue);
            try {
                config.save(file);
            } catch (IOException err) {
                err.printStackTrace();
            }
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
        return MiniMessage.miniMessage().deserialize("<i:false>" + message);
    }

    public static void applyUpdates() {
        config.set("invis.kill_invis", null);
        config.set("invis.death_invis", null);
        config.set("death_message", "%victim% was slain by %killer%");
        
        try {
            config.save(file);
        } catch (IOException err) {
            err.printStackTrace();
        }
    }
}