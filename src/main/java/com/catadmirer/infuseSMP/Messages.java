package com.catadmirer.infuseSMP;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/*
 * TODO:
 * Figure out a better way to get messages.
 * maybe individual functions so that placeholders can be applied correctly
 * Also some things need to be Lists, so make a better implementation for that
 */

public enum Messages {
    EFFECT_BROADCAST("effect_broadcast", "🧪 %player% is cooking up the %item% at %x%, %y%, %z%... %dimension%"),
    DISCORD_BROADCAST("discord_broadcast", "%player% is cooking up the %item% at %x%, %y%, %z% in %dimension% @everyone"),
    EFFECT_FINISHED("effect_finished", "%item% has been brewed!"),

    SLOT_EMPTY("slot_empty", "&cYou don't have any effect equipped in slot %slot%."),
    EFFECT_NONE_EQUIPPED("effect_none_equipped", "&cYou don't have an Effect equipped in slot %slot%."),

    WITHDRAW_INVALID("withdraw_invalid", "&cInvalid usage. Use /ldrain or /rdrain"),

    TRUST_CONSOLEUSAGE("trust_consoleusage", "&cOnly players can use this command."),
    TRUST_INCORRECTUSAGE("trust_incorrectusage", "&cUsage: /%label% <player>"),
    TRUST_NOPLAYER("trust_noplayer", "&cPlayer not found."),
    TRUST_SELF("trust_self", "&cYou always trust yourself. Surely..."),
    TRUST_ADDED("trust_added", "&aYou now trust %target%."),
    TRUST_REMOVED("trust_removed", "&eYou no longer trust %target%."),

    EFFECT_NOBREWING("effect_nobrewing", "&cYou need to craft this in a brewing stand!"),

    INVIS_KILL("invis.kill_invis", "%victim% was slain by %killer%"),
    INVIS_DEATH("invis.death_invis", "%victim% was slain by %killer%"),

    STRENGTH_NAME("strength.effect_name", "§4Strength Effect"),
    STRENGTH_LORE("strength.effect_lore", "§4§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§4\ud83d\udee1 §8Double Damage to all mobs", "§4\ud83d\udee1 §8Disable shields for 10 seconds", "§4\ud83d\udee1 §8Ranged weapons pierce shields", "§4\ud83d\udee1 §8+1 Damage when under 6 hearts", "§4\ud83d\udee1 §8+2 Damage when under 4 hearts", "§4\ud83d\udee1 §8+3 Damage when under 2 hearts", "§7", "§4§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§4\ud83d\udee1 §8All attacks are critical for 15 seconds", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_STRENGTH_NAME("aug_strength.effect_name", "§4Augmented Strength Effect"),
    AUG_STRENGTH_LORE("aug_strength.effect_lore", "§4§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§4\ud83d\udee1 §8Double Damage to all mobs", "§4\ud83d\udee1 §8Disable shields for 10 seconds", "§4\ud83d\udee1 §8Ranged weapons pierce shields", "§4\ud83d\udee1 §8+1 Damage when under 6 hearts", "§4\ud83d\udee1 §8+2 Damage when under 4 hearts", "§4\ud83d\udee1 §8+3 Damage when under 2 hearts", "§7", "§4§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§4\ud83d\udee1 §8All attacks are critical for 15 seconds", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    THUNDER_NAME("thunder.effect_name", "§eThunder Effect"),
    THUNDER_LORE("thunder.effect_lore", "§eᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§e⚡ §7Chain lightning", "§e⚡ §7Tridents Strikes Lightning ", "§7", "§eꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§e⚡ §7Strike enemies with lightning and make a thunderstorm", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 20s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_THUNDER_NAME("aug_thunder.effect_name", "§eAugmented Thunder Effect"),
    AUG_THUNDER_LORE("aug_thunder.effect_lore", "§eᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§e⚡ §7Chain lightning", "§e⚡ §7Tridents Strikes Lightning ", "§7", "§eꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§e⚡ §7Strike enemies with lightning and make a thunderstorm", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 20s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    SPEED_NAME("speed.effect_name", "§#E8BD74Speed Effect"),
    SPEED_LORE("speed.effect_lore", "§#E8BD74§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§#E8BD74⋘ §8Speed 1", "§#E8BD74⋘ §8Increase speed level by 1 after each hit", "§#E8BD74⋘ §8Speed resets after 1 second of no activity", "§#E8BD74⋘ §8Ranged weapons charge 1.5x faster", "§#E8BD74⋘ §8Enemy invincibility frames are halved", "§7", "§#E8BD74§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§#E8BD74⋘ §8Speed Dash", "§7", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 20s"),
    AUG_SPEED_NAME("aug_speed.effect_name", "§#E8BD74Augmented Speed Effect"),
    AUG_SPEED_LORE("aug_speed.effect_lore", "§#E8BD74§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§#E8BD74⋘ §8Speed 1", "§#E8BD74⋘ §8Increase speed level by 1 after each hit", "§#E8BD74⋘ §8Speed resets after 1 second of no activity", "§#E8BD74⋘ §8Ranged weapons charge 1.5x faster", "§#E8BD74⋘ §8Enemy invincibility frames are halved", "§7", "§#E8BD74§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§#E8BD74⋘ §8Speed Dash", "§7", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 10s"),
    REGEN_NAME("regen.effect_name", "§cRegeneration Effect"),
    REGEN_LORE("regen.effect_lore", "§6ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§6\ud83d\ude91 §7Every hit grants Regeneration 2 for 1 second", "§6\ud83d\ude91 §7Healing gives 3 extra saturation bars", "§7", "§6ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "§6\ud83d\ude91 §7Apply lifesteal effect that heals you based on your damage dealt", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_REGEN_NAME("aug_regen.effect_name", "§cAugmented Regeneration Effect"),
    AUG_REGEN_LORE("aug_regen.effect_lore", "§6ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§6\ud83d\ude91 §7Every hit grants Regeneration 2 for 1 second", "§6\ud83d\ude91 §7Healing gives 3 extra saturation bars", "§7", "§6ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "§6\ud83d\ude91 §7Apply lifesteal effect that heals you based on your damage dealt", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    OCEAN_NAME("ocean.effect_name", "§9Ocean Effect"),
    OCEAN_LORE("ocean.effect_lore", "§9§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§9🫧 §7Swim faster", "§9🫧 §7Breathe underwater", "§9🫧 §7Make everyone around you start drowning when in water", "§9🫧 §7Tridents pull players", "§7", "§6§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "§9🫧 §7Creates a Whirlhole", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 15s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_OCEAN_NAME("aug_ocean.effect_name", "§9Augmented Ocean Effect"),
    AUG_OCEAN_LORE("aug_ocean.effect_lore", "§9§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§9🫧 §7Swim faster", "§9🫧 §7Breathe underwater", "§9🫧 §7Make everyone around you start drowning when in water", "§9🫧 §7Tridents pull players", "§7", "§6§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "§9🫧 §7Creates a Whirlhole", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 15s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    INVIS_NAME("invisibility.effect_name", "§5Invisibility Effect"),
    INVIS_LORE("invisibility.effect_lore", "§5ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§5\ud83d\udc41 §7Permanent Invisibility", "§5\ud83d\udc41 §7Full bow shot blinds the target for 5s and gives blindness for 2s", "§5\ud83d\udc41 §7Mobs cannot target you", "§7", "§5ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§5\ud83d\udc41 §7Creates a 5×5 hollow circle of black dust particles", "§5\ud83d\udc41 §7Inside: allies become fully invisible; enemies get blindness", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
    AUG_INVIS_NAME("aug_invisibility.effect_name", "§5Augmented Invisibility Effect"),
    AUG_INVIS_LORE("aug_invisibility.effect_lore", "§5ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§5\ud83d\udc41 §7Permanent Invisibility", "§5\ud83d\udc41 §7Full bow shot blinds the target for 5s and gives blindness for 2s", "§5\ud83d\udc41 §7Mobs cannot target you", "§7", "§5ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§5\ud83d\udc41 §7Creates a 5×5 hollow circle of black dust particles", "§5\ud83d\udc41 §7Inside: allies become fully invisible; enemies get blindness", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),
    HEART_NAME("heart.effect_name", "§cHeart Effect"),
    HEART_LORE("heart.effect_lore", "§cᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§c❤ §7+5 Hearts", "§c❤ §7All food gives absorption", "§c❤ §7Egaps gives +10 absorption hearts", "§c❤ §7See player's health every 10 hits", "§7", "§cꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§c❤ §7Heal players to 20 hearts instantly", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 60s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 120s"),
    AUG_HEART_NAME("aug_heart.effect_name", "§cAugmented Heart Effect"),
    AUG_HEART_LORE("aug_heart.effect_lore", "§cᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§c❤ §7+5 Hearts", "§c❤ §7All food gives absorption", "§c❤ §7Egaps gives +10 absorption hearts", "§c❤ §7See player's health every 10 hits", "§7", "§cꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§c❤ §7Heal players to 20 hearts instantly", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 60s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    HASTE_NAME("haste.effect_name", "§6Haste Effect"),
    HASTE_LORE("haste.effect_lore", "§6ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§6⛏ §7Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes", "§6⛏ §7Halved shield cooldown when stunned", "§7", "§6ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§6⛏ §7Attack faster", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 15s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_HASTE_NAME("aug_haste.effect_name", "§6Augmented Haste Effect"),
    AUG_HASTE_LORE("aug_haste.effect_lore", "§6ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§6⛏ §7Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes", "§6⛏ §7Halved shield cooldown when stunned", "§7", "§6ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§6⛏ §7Attack faster", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 15s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    FROST_NAME("frost.effect_name", "§bFrost Effect"),
    FROST_LORE("frost.effect_lore", "§bᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§b❄ §7Speed 3 on ice and snow", "§b❄ §7Freeze player every 10 hits", "§b❄ §7Frozen enemies can't use windcharges", "§7", "§bꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§b❄ §7Reduce enemies jump strength and freeze them every hit", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
    AUG_FROST_NAME("aug_frost.effect_name", "§bAugmented Frost Effect"),
    AUG_FROST_LORE("aug_frost.effect_lore", "§bᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§b❄ §7Speed 3 on ice and snow", "§b❄ §7Freeze player every 10 hits", "§b❄ §7Frozen enemies can't use windcharges", "§7", "§bꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§b❄ §7Reduce enemies jump strength and freeze them every hit", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),
    FIRE_NAME("fire.effect_name", "§#E85720Fire Effect"),
    FIRE_LORE("fire.effect_lore", "§#E85720§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§#E85720\ud83d\udd25 §8Fire Resistance", "§#E85720\ud83d\udd25 §8Full charged bow shots set arrows on fire", "§#E85720\ud83d\udd25 §8In lava, no fall damage", "§#E85720\ud83d\udd25 §8Every 10 hits sets target on fire for 5s", "§7", "§#E85720§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "§#E85720\ud83d\udd25 §8Set surrounding enemies on fire (5 block radius)", "§7", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_FIRE_NAME("aug_fire.effect_name", "§#E85720Augmented Fire Effect"),
    AUG_FIRE_LORE("aug_fire.effect_lore", "§#E85720§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§#E85720\ud83d\udd25 §8Fire Resistance", "§#E85720\ud83d\udd25 §8Full charged bow shots set arrows on fire", "§#E85720\ud83d\udd25 §8In lava, no fall damage", "§#E85720\ud83d\udd25 §8Every 10 hits sets target on fire for 5s", "§7", "§#E85720§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "§#E85720\ud83d\udd25 §8Set surrounding enemies on fire (5 block radius)", "§7", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    FEATHER_NAME("feather.effect_name", "§#BEA3CAFeather Effect"),
    FEATHER_LORE("feather.effect_lore", "§#BEA3CA§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§#BEA3CA\ud83e\udeb6 §8No fall damage", "§#BEA3CA\ud83e\udeb6 §8Attacking from 7+ block fall does a mace hit", "§#BEA3CA\ud83e\udeb6 §8Auto windcharge counter after being attacked 10 times", "§#BEA3CA\ud83e\udeb6 §8Windcharges have 0.5x cooldown", "§#BEA3CA\ud83e\udeb6 §8Windcharges have 2x velocity", "§7", "§#BEA3CA§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§#BEA3CA\ud83e\udeb6 §8Launches the player upward", "§#BEA3CA\ud83e\udeb6 §8Slams the player back down", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 2s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_FEATHER_NAME("aug_feather.effect_name", "§#BEA3CAAugmented Feather Effect"),
    AUG_FEATHER_LORE("aug_feather.effect_lore", "§#BEA3CA§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§#BEA3CA\ud83e\udeb6 §8No fall damage", "§#BEA3CA\ud83e\udeb6 §8Attacking from 7+ block fall does a mace hit", "§#BEA3CA\ud83e\udeb6 §8Auto windcharge counter after being attacked 10 times", "§#BEA3CA\ud83e\udeb6 §8Windcharges have 0.5x cooldown", "§#BEA3CA\ud83e\udeb6 §8Windcharges have 2x velocity", "§7", "§#BEA3CA§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§#BEA3CA\ud83e\udeb6 §8Launches the player upward", "§#BEA3CA\ud83e\udeb6 §8Slams the player back down", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 2s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    EMERALD_NAME("emerald.effect_name", "§aEmerald Effect"),
    EMERALD_LORE("emerald.effect_lore", "§a§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§a$ §7Looting 5", "§a$ §7Luck 10", "§a$ §71.5x EXP", "§a$ §7Consumables have a 15% chance of not being consumed", "§a$ §7Enchanting table always on level 30", "§7", "§a§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§a$ §7Hero of the village 255", "§a$ §7Consumables have a 25% chance of not being consumed", "§a$ §73x EXP", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
    AUG_EMERALD_NAME("aug_emerald.effect_name", "§aAugmented Emerald Effect"),
    AUG_EMERALD_LORE("aug_emerald.effect_lore", "§a§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§a$ §7Looting 5", "§a$ §7Luck 10", "§a$ §71.5x EXP", "§a$ §7Consumables have a 15% chance of not being consumed", "§a$ §7Enchanting table always on level 30", "§7", "§a§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§a$ §7Hero of the village 255", "§a$ §7Consumables have a 25% chance of not being consumed", "§a$ §73x EXP", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    ENDER_NAME("ender.effect_name", "§5Ender Effect"),
    ENDER_LORE("ender.effect_lore", "§5§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§5⭐ §7All nearby untrusted players have glowing", "§5⭐ §7Use dragon's breath to shoot powerful fireballs that curse players", "§5⭐ §7Curse untrusted players on hit which shares damage with all", "§5⭐ §7cursed players", "§7", "§5§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§5⭐ §7Teleport to the cursor position within a 15 block radius", "§5⭐ §7Instantly kills any mob and curses players", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 10s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),
    AUG_ENDER_NAME("aug_ender.effect_name", "§5Augmented Ender Effect"),
    AUG_ENDER_LORE("aug_ender.effect_lore", "§5§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§5⭐ §7All nearby untrusted players have glowing", "§5⭐ §7Use dragon's breath to shoot powerful fireballs that curse players", "§5⭐ §7Curse untrusted players on hit which shares damage with all", "§5⭐ §7cursed players", "§7", "§5§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§5⭐ §7Teleport to the cursor position within a 15 block radius", "§5⭐ §7Instantly kills any mob and curses players", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 20s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
    APOPHIS_NAME("apophis.effect_name", "§5Apophis Effect"),
    APOPHIS_LORE("apophis.effect_lore", "§5§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§5🍼 §7Combine Fire, Emerald and Heart's effects", "§5🍼 §7Have a custom skin and nametag", "§7", "§5§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§5🍼 §7Activate Fire, Emerald and Heart's sparks", "§5🍼 §7Upon hitting a player blind their screen", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 20s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 2m"),
    AUG_APOPHIS_NAME("aug_apophis.effect_name", "§5Augmented Apophis Effect"),
    AUG_APOPHIS_LORE("aug_apophis.effect_lore", "§5§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§5🍼 §7Combine Fire, Emerald and Heart's effects", "§5🍼 §7Have a custom skin and nametag", "§7", "§5§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§5🍼 §7Activate Fire, Emerald and Heart's sparks", "§5🍼 §7Upon hitting a player blind their screen", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: 30s", "§3ᴄᴏᴏʟᴅᴏᴡɴ: 1m 30s"),
    AUG_THIEF_NAME("aug_thief.effect_name", "§4Augmented Thief Effect"),
    AUG_THIEF_LORE("aug_thief.effect_lore", "§4§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§4🥷 §7You're not shown on tablist", "§4🥷 §7Your footsteps don't make noise", "§4🥷 §7Kill a player to shapeshift into them", "§7", "§4§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§4🥷 §7Temporarily steal your opponents effect", "§7", "§3ᴅᴜʀᴀᴛɪᴏɴ: Unknown", "§3ᴄᴏᴏʟᴅᴏᴡɴ: Unknown"),
    THIEF_NAME("thief.effect_name", "§4Thief Effect"),
    THIEF_LORE("thief.effect_lore", "§4§lᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "§4🥷 §7You're not shown on tablist", "§4🥷 §7Your footsteps don't make noise", "§4🥷 §7Kill a player to shapeshift into them", "", "§4§lꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "§4🥷 §7Temporarily steal your opponents effect", "", "§3ᴅᴜʀᴀᴛɪᴏɴ: Unknown", "§3ᴄᴏᴏʟᴅᴏᴡɴ: Unknown")

    ;

    public static final File file = new File("plugins/Infuse/messages.yml");
    public static final FileConfiguration config = new YamlConfiguration();

    public static final MiniMessage minimessageSerializer = MiniMessage.miniMessage();
    public static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    public final String configKey;
    public final String defaultValue;

    Messages(String configKey, String defaultValue) {
        this.configKey = configKey;
        this.defaultValue = defaultValue;
    }

    Messages(String configKey, String... lines) {
        this.configKey = configKey;
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
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
            Infuse.getInstance().saveResource(file.getName(), replace);
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
        // Passing the string through legacy and minimessage serialization
        Component legacy = legacySerializer.deserialize(message);
        message = minimessageSerializer.serialize(legacy).replace("\\", "");

        return minimessageSerializer.deserialize(message);
    }


}