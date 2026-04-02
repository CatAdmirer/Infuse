package com.catadmirer.infuseSMP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Message {
    private String message;
    private List<String> placeholders;

    public Message(MessageType messageType) {
        message = MessageConfig.getMessage(messageType);
        placeholders = new ArrayList<>(messageType.placeholders);
    }

    public Message applyPlaceholder(String placeholder, Object value) {
        this.message = message.replace(String.format("%%%s%%", placeholder), String.valueOf(value));
        placeholders.remove(placeholder);

        return this;
    }

    public Message applyPlaceholders(Map<String,Object> placeholders) {
        placeholders.forEach(this::applyPlaceholder);
        return this;
    }

    // Text serializers
    public static final MiniMessage minimessage = MiniMessage.miniMessage();
    public static final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();

    public String toString() {
        return message;
    }

    public List<String> toStringList() {
        return List.of(message.split("\n"));
    }

    public List<Component> toComponentList() {
        if (!placeholders.isEmpty()) {
            throw new IllegalStateException("Not all placeholders have been registered.");
        }
        
        return toStringList().stream().map(m -> MiniMessage.miniMessage().deserialize("<i:false>" + m)).toList();
    }

    public Component toComponent() {
        if (!placeholders.isEmpty()) {
            throw new IllegalStateException("Not all placeholders have been registered.");
        }

        return MiniMessage.miniMessage().deserialize("<i:false>" + toString());
    }

    /**
     * Helper function that allows minimessage translation for an arbitrary string.
     * 
     * @param message The minimessage string to translate
     * 
     * @return The {@link Component} that can be sent to players.
     */
    public static Component toComponent(String message) {
        return MiniMessage.miniMessage().deserialize("<i:false>" + message);
    }

    public static enum MessageType {
        EFFECT_BROADCAST(List.of("player", "item", "x", "y", "z", "dimension"), "🧪 %player% is cooking up the %item%<reset> at %x%, %y%, %z%... %dimension%"),
        DISCORD_BROADCAST(List.of("player", "item", "x", "y", "z", "dimension"), "%player% is cooking up the %item% at %x%, %y%, %z% in %dimension% @everyone"),
        EFFECT_FINISHED(List.of("item"), "%item% has been brewed!"),

        REGULAR_BROADCAST(List.of("item", "x", "y", "z", "dimension"), "🧪 A %item%<reset> has been crafted at <#90D5FF><b>%x%, %y%, %z%... %dimension%"),

        SLOT_EMPTY(List.of("slot"), "<red>You don't have any effect equipped in slot %slot%."),
        EFFECT_NONE_EQUIPPED(List.of("slot"), "<red>You don't have an Effect equipped in slot %slot%."),

        WITHDRAW_INVALID("<red>Invalid usage. Use /ldrain or /rdrain"),

        TRUST_CONSOLEUSAGE("<red>Only players can use this command."),
        TRUST_INCORRECTUSAGE(List.of("label"), "<red>Usage: /%label% <player>"),
        TRUST_NOPLAYER("<red>Player not found."),
        TRUST_SELF("<red>You always trust yourself. Surely..."),
        TRUST_ADDED(List.of("target"), "<green>You now trust %target%."),
        TRUST_ALREADYTRUSTED(List.of("target"), "<green>You already trust %target%."),
        TRUST_REMOVED(List.of("target"), "<green>You no longer trust %target%."),
        TRUST_NOTTRUSTED(List.of("target"), "<green>You already didn't trust %target%."),

        EFFECT_NOBREWING("<red>You need to craft this in a brewing stand!"),
        DEATH_MESSAGE(List.of("victim", "killer"), "%victim% was slain by %killer%"),

        CONTROLS_USAGE("<red>Usage: /controls <offhand|command>"),
        CONTROLS_INVALID_PARAM("<red>Invalid option. Use \"offhand\" or \"command\"."),

        INFUSE_INVALID_PARAM("<red>Please use the tab completions as a reference."),
        INFUSE_INVALID_SLOT(List.of("slot"), "<red>Invalid Argument! Could not identify slot %slot%.  Please use \"1\" or \"2\"."),
        INFUSE_CONTROLS_USAGE("<red>Usage: /infuse controls <offhand|command>"),
        INFUSE_CONTROLS_SUCCESS(List.of("controlMode"), "<dark_red>Your controls are now %controlMode%"),

        INFUSE_SETEFFECT_USAGE("<red>Invalid Argument! Please use /infuse setEffect <player> <aug_fire|ocean> <1|2>"),
        INFUSE_SETEFFECT_SUCCESS(List.of("slot", "player_name", "effect_name"), "<green>Successfully set the effect in slot %slot% of player %player_name% to %effect_name%."),

        INFUSE_GIVEEFFECT_USAGE("<red>Invalid Argument! Please use /infuse giveEffect <player> <aug_fire|ocean>"),
        INFUSE_GIVEEFFECT_SUCCESS(List.of("effect_color", "effect_name"), "%effect_color%You recieved the %effect_name%"),

        INFUSE_CLEAREFFECT_USAGE("<red>Invalid Argument! Please use /infuse clearEffect <player>"),
        INFUSE_CLEAREFFECT_SUCCESS(List.of("player_name"), "<green>Cleared %player_name%'s effects"),

        INFUSE_COOLDOWN_USAGE("<red>Invalid Argument! Please use /infuse cooldown <player>"),
        INFUSE_COOLDOWN_SUCCESS(List.of("player_name"), "<green>Removed %player_name%'s cooldown"),

        CLEAREFFECTS_USAGE("<red>Usage: /infuse clearEffects <player>"),

        JOIN_ABILITY_NOTIFY(List.of("control_mode"), "<gray>Your ability mode is set to: %control_mode%"),
        
        DRAIN_SUCCESS(List.of("effect_name"), "<green>You have drained your: %effect_name%"),

        EFFECT_EQUIPPED(List.of("effect_name"), "<green>You have equipped the %effect_name%"),

        SWAP_NO_EFFECTS("<red>You do not have any effects equipped to swap."),
        SWAP_SUCCESS("<green>Your Effects have been swapped."),

        THIEF_STEAL(List.of("victim", "effect_name"), "<yellow>You stole %victim%'s %effect_name% Effect"),

        RECIPE_NOT_FOUND("<red>No recipe found for this potion."),
        RECIPE_DISABLED("Recipe is disabled/broken"),

        ERROR_INV_FULL("<red>Your inventory is full! Make space before unequipping."),
        ERROR_NOT_PLAYER("<red>Only players can use this command."),
        ERROR_NOT_OP("<red>You must be OP to run this command."),
        ERROR_INVALID_COMMAND("<red>Invalid command."),
        ERROR_RITUAL_ACTIVE("<red>A ritual is already in progress!"),
        ERROR_TARGET_NOT_FOUND("<red>Player not found or not online."),

        // Effect messages
        EMERALD_NAME("<#009420>Emerald Effect"),
        EMERALD_LORE("<#009420><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#009420>$ <dark_gray>Looting 5", "<#009420>$ <dark_gray>Luck 10", "<#009420>$ <dark_gray>1.5x EXP", "<#009420>$ <dark_gray>Consumables have a 15% chance of not being consumed", "<#009420>$ <dark_gray>Enchanting table always on level 30", "<dark_gray>", "<#009420><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#009420>$ <dark_gray>Hero of the village 255", "<#009420>$ <dark_gray>Consumables have a 25% chance of not being consumed", "<#009420>$ <dark_gray>3x EXP", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_EMERALD_NAME("<#009420>Augmented Emerald Effect"),
        AUG_EMERALD_LORE("<#009420><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#009420>$ <dark_gray>Looting 5", "<#009420>$ <dark_gray>Luck 10", "<#009420>$ <dark_gray>1.5x EXP", "<#009420>$ <dark_gray>Consumables have a 15% chance of not being consumed", "<#009420>$ <dark_gray>Enchanting table always on level 30", "<dark_gray>", "<#009420><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#009420>$ <dark_gray>Hero of the village 255", "<#009420>$ <dark_gray>Consumables have a 25% chance of not being consumed", "<#009420>$ <dark_gray>3x EXP", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
        
        ENDER_NAME("<dark_purple>Ender Effect"),
        ENDER_LORE("<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <dark_gray>All nearby untrusted players have glowing", "<dark_purple>⭐ <dark_gray>Use dragon's breath to shoot powerful fireballs that curse players", "<dark_purple>⭐ <dark_gray>Curse untrusted players on hit which shares damage with all", "<dark_purple>⭐ <dark_gray>cursed players", "<dark_gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <dark_gray>Teleport to the cursor position within a 15 block radius", "<dark_purple>⭐ <dark_gray>Instantly kills any mob and curses players", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 10s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),
        AUG_ENDER_NAME("<dark_purple>Augmented Ender Effect"),
        AUG_ENDER_LORE("<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <dark_gray>All nearby untrusted players have glowing", "<dark_purple>⭐ <dark_gray>Use dragon's breath to shoot powerful fireballs that curse players", "<dark_purple>⭐ <dark_gray>Curse untrusted players on hit which shares damage with all", "<dark_purple>⭐ <dark_gray>cursed players", "<dark_gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>⭐ <dark_gray>Teleport to the cursor position within a 15 block radius", "<dark_purple>⭐ <dark_gray>Instantly kills any mob and curses players", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        FEATHER_NAME("<#BEA3CA>Feather Effect"),
        FEATHER_LORE("<#BEA3CA><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>No fall damage", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Attacking from 7+ block fall does a mace hit", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Auto windcharge counter after being attacked 10 times", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 0.5x cooldown", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 2x velocity", "<dark_gray>", "<#BEA3CA><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Launches the player upward", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Slams the player back down", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 2s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_FEATHER_NAME("<#BEA3CA>Augmented Feather Effect"),
        AUG_FEATHER_LORE("<#BEA3CA><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>No fall damage", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Attacking from 7+ block fall does a mace hit", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Auto windcharge counter after being attacked 10 times", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 0.5x cooldown", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 2x velocity", "<dark_gray>", "<#BEA3CA><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Launches the player upward", "<#BEA3CA>\ud83e\udeb6 <dark_gray>Slams the player back down", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 2s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
        
        FIRE_NAME("<#E85720>Fire Effect"),
        FIRE_LORE("<#E85720><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E85720>\ud83d\udd25 <dark_gray>Fire Resistance", "<#E85720>\ud83d\udd25 <dark_gray>Full charged bow shots set arrows on fire", "<#E85720>\ud83d\udd25 <dark_gray>In lava, no fall damage", "<#E85720>\ud83d\udd25 <dark_gray>Every 10 hits sets target on fire for 5s", "<dark_gray>", "<#E85720><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#E85720>\ud83d\udd25 <dark_gray>Set surrounding enemies on fire (5 block radius)", "<dark_gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_FIRE_NAME("<#E85720>Augmented Fire Effect"),
        AUG_FIRE_LORE("<#E85720><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E85720>\ud83d\udd25 <dark_gray>Fire Resistance", "<#E85720>\ud83d\udd25 <dark_gray>Full charged bow shots set arrows on fire", "<#E85720>\ud83d\udd25 <dark_gray>In lava, no fall damage", "<#E85720>\ud83d\udd25 <dark_gray>Every 10 hits sets target on fire for 5s", "<dark_gray>", "<#E85720><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#E85720>\ud83d\udd25 <dark_gray>Set surrounding enemies on fire (5 block radius)", "<dark_gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
        
        FROST_NAME("<aqua>Frost Effect"),
        FROST_LORE("<aqua><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <dark_gray>Speed 3 on ice and snow", "<aqua>❄ <dark_gray>Freeze player every 10 hits", "<aqua>❄ <dark_gray>Frozen enemies can't use windcharges", "<dark_gray>", "<aqua><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <dark_gray>Reduce enemies jump strength and freeze them every hit", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
        AUG_FROST_NAME("<aqua>Augmented Frost Effect"),
        AUG_FROST_LORE("<aqua><bold><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <dark_gray>Speed 3 on ice and snow", "<aqua>❄ <dark_gray>Freeze player every 10 hits", "<aqua>❄ <dark_gray>Frozen enemies can't use windcharges", "<dark_gray>", "<aqua><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<aqua>❄ <dark_gray>Reduce enemies jump strength and freeze them every hit", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),

        HASTE_NAME("<#BD934F>Haste Effect"),
        HASTE_LORE("<#BD934F><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BD934F>⛏ <dark_gray>Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes", "<#BD934F>⛏ <dark_gray>Halved shield cooldown when stunned", "<dark_gray>", "<#BD934F><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BD934F>⛏ <dark_gray>Attack faster", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_HASTE_NAME("<#BD934F>Augmented Haste Effect"),
        AUG_HASTE_LORE("<#BD934F><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BD934F>⛏ <dark_gray>Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes", "<#BD934F>⛏ <dark_gray>Halved shield cooldown when stunned", "<dark_gray>", "<#BD934F><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#BD934F>⛏ <dark_gray>Attack faster", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
        
        HEART_NAME("<red>Heart Effect"),
        HEART_LORE("<red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <dark_gray>+5 Hearts", "<red>❤ <dark_gray>All food gives absorption", "<red>❤ <dark_gray>Egaps gives +10 absorption hearts", "<red>❤ <dark_gray>See player's health every 10 hits", "<dark_gray>", "<red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <dark_gray>Heal players to 20 hearts instantly", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 60s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 120s"),
        AUG_HEART_NAME("<red>Augmented Heart Effect"),
        AUG_HEART_LORE("<red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <dark_gray>+5 Hearts", "<red>❤ <dark_gray>All food gives absorption", "<red>❤ <dark_gray>Egaps gives +10 absorption hearts", "<red>❤ <dark_gray>See player's health every 10 hits", "<dark_gray>", "<red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<red>❤ <dark_gray>Heal players to 20 hearts instantly", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 60s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),

        INVIS_NAME("<#2B0078>Invis Effect"),
        INVIS_LORE("<#2B0078><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#2B0078>\ud83d\udc41 <dark_gray>Permanent Invisibility", "<#2B0078>\ud83d\udc41 <dark_gray>Full bow shot blinds the target for 5s and gives blindness for 2s", "<#2B0078>\ud83d\udc41 <dark_gray>Mobs cannot target you", "<dark_gray>", "<#2B0078><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#2B0078>\ud83d\udc41 <dark_gray>Creates a 5×5 hollow circle of black dust particles", "<#2B0078>\ud83d\udc41 <dark_gray>Inside: allies become fully invisible; enemies get blindness", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
        AUG_INVIS_NAME("<#2B0078>Augmented Invis Effect"),
        AUG_INVIS_LORE("<#2B0078><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#2B0078>\ud83d\udc41 <dark_gray>Permanent Invisibility", "<#2B0078>\ud83d\udc41 <dark_gray>Full bow shot blinds the target for 5s and gives blindness for 2s", "<#2B0078>\ud83d\udc41 <dark_gray>Mobs cannot target you", "<dark_gray>", "<#2B0078><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#2B0078>\ud83d\udc41 <dark_gray>Creates a 5×5 hollow circle of black dust particles", "<#2B0078>\ud83d\udc41 <dark_gray>Inside: allies become fully invisible; enemies get blindness", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),

        OCEAN_NAME("<blue>Ocean Effect"),
        OCEAN_LORE("<blue><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<blue>🫧 <dark_gray>Swim faster", "<blue>🫧 <dark_gray>Breathe underwater", "<blue>🫧 <dark_gray>Make everyone around you start drowning when in water", "<blue>🫧 <dark_gray>Tridents pull players", "<dark_gray>", "<blue><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<blue>🫧 <dark_gray>Creates a Whirlhole", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_OCEAN_NAME("<blue>Augmented Ocean Effect"),
        AUG_OCEAN_LORE("<blue><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<blue>🫧 <dark_gray>Swim faster", "<blue>🫧 <dark_gray>Breathe underwater", "<blue>🫧 <dark_gray>Make everyone around you start drowning when in water", "<blue>🫧 <dark_gray>Tridents pull players", "<dark_gray>", "<blue><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<blue>🫧 <dark_gray>Creates a Whirlhole", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
        
        REGEN_NAME("<#FC00DD>Regen Effect"),
        REGEN_LORE("<#B0009A><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#FC00DD>+ <dark_gray>No hunger loss", "<#FC00DD>+ <dark_gray>Permanent Regeneration", "<#FC00DD>+ <dark_gray>Every hit gives Regeneration 2 for 3.0 seconds", "<#FC00DD>+ <dark_gray>All food gives +3.0 saturation bars", "<#FC00DD>+ <dark_gray>Eat anytime", "<#FC00DD>+ <dark_gray>10th hit takes away 1.0 hunger bar from your target", "<dark_gray>", "<#B0009A>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#FC00DD>+ <dark_gray>Damage dealt heals you and nearby teammates", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_REGEN_NAME("<#FC00DD>Augmented Regen Effect"),
        AUG_REGEN_LORE("<#B0009A><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#FC00DD>+ <dark_gray>No hunger loss", "<#FC00DD>+ <dark_gray>Permanent Regeneration", "<#FC00DD>+ <dark_gray>Every hit gives Regeneration 2 for 3.0 seconds", "<#FC00DD>+ <dark_gray>All food gives +3.0 saturation bars", "<#FC00DD>+ <dark_gray>Eat anytime", "<#FC00DD>+ <dark_gray>10th hit takes away 1.0 hunger bar from your target", "<dark_gray>", "<#B0009A>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:", "<#FC00DD>+ <dark_gray>Damage dealt heals you and nearby teammates", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        SPEED_NAME("<#E8BD74>Speed Effect"),
        SPEED_LORE("<#E8BD74><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed 1", "<#E8BD74>⋘ <dark_gray>Increase speed level by 1 after each hit", "<#E8BD74>⋘ <dark_gray>Speed resets after 1 second of no activity", "<#E8BD74>⋘ <dark_gray>Ranged weapons charge 1.5x faster", "<#E8BD74>⋘ <dark_gray>Enemy invincibility frames are halved", "<dark_gray>", "<#E8BD74><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed Dash", "<dark_gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 20s"),
        AUG_SPEED_NAME("<#E8BD74>Augmented Speed Effect"),
        AUG_SPEED_LORE("<#E8BD74><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed 1", "<#E8BD74>⋘ <dark_gray>Increase speed level by 1 after each hit", "<#E8BD74>⋘ <dark_gray>Speed resets after 1 second of no activity", "<#E8BD74>⋘ <dark_gray>Ranged weapons charge 1.5x faster", "<#E8BD74>⋘ <dark_gray>Enemy invincibility frames are halved", "<dark_gray>", "<#E8BD74><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<#E8BD74>⋘ <dark_gray>Speed Dash", "<dark_gray>", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 10s"),

        STRENGTH_NAME("<dark_red>Strength Effect"),
        STRENGTH_LORE("<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>Double Damage to all mobs", "<dark_red>\ud83d\udee1 <dark_gray>Disable shields for 10 seconds", "<dark_red>\ud83d\udee1 <dark_gray>Ranged weapons pierce shields", "<dark_red>\ud83d\udee1 <dark_gray>+1 Damage when under 6 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+2 Damage when under 4 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+3 Damage when under 2 hearts", "<dark_gray>", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>All attacks are critical for 15 seconds", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_STRENGTH_NAME("<dark_red>Augmented Strength Effect"),
        AUG_STRENGTH_LORE("<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>Double Damage to all mobs", "<dark_red>\ud83d\udee1 <dark_gray>Disable shields for 10 seconds", "<dark_red>\ud83d\udee1 <dark_gray>Ranged weapons pierce shields", "<dark_red>\ud83d\udee1 <dark_gray>+1 Damage when under 6 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+2 Damage when under 4 hearts", "<dark_red>\ud83d\udee1 <dark_gray>+3 Damage when under 2 hearts", "<dark_gray>", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>\ud83d\udee1 <dark_gray>All attacks are critical for 15 seconds", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
        
        THUNDER_NAME("<yellow>Thunder Effect"),
        THUNDER_LORE("<yellow><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <dark_gray>Chain lightning", "<yellow>⚡ <dark_gray>Tridents Strikes Lightning ", "<dark_gray>", "<yellow><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <dark_gray>Strike enemies with lightning and make a thunderstorm", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_THUNDER_NAME("<yellow>Augmented Thunder Effect"),
        AUG_THUNDER_LORE("<yellow><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <dark_gray>Chain lightning", "<yellow>⚡ <dark_gray>Tridents Strikes Lightning ", "<dark_gray>", "<yellow><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<yellow>⚡ <dark_gray>Strike enemies with lightning and make a thunderstorm", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),
        
        // Extra effect messages
        APOPHIS_NAME("<dark_purple>Apophis Effect"),
        APOPHIS_LORE("<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <dark_gray>Combine Fire, Emerald and Heart's effects", "<dark_purple>🍼 <dark_gray>Have a custom skin and nametag", "<dark_gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <dark_gray>Activate Fire, Emerald and Heart's sparks", "<dark_purple>🍼 <dark_gray>Upon hitting a player blind their screen", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 2m"),
        AUG_APOPHIS_NAME("<dark_purple>Augmented Apophis Effect"),
        AUG_APOPHIS_LORE("<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <dark_gray>Combine Fire, Emerald and Heart's effects", "<dark_purple>🍼 <dark_gray>Have a custom skin and nametag", "<dark_gray>", "<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_purple>🍼 <dark_gray>Activate Fire, Emerald and Heart's sparks", "<dark_purple>🍼 <dark_gray>Upon hitting a player blind their screen", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 1m 30s"),
        
        THIEF_NAME("<dark_red>Thief Effect"),
        THIEF_LORE("<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <dark_gray>You're not shown on tablist", "<dark_red>🥷 <dark_gray>Your footsteps don't make noise", "<dark_red>🥷 <dark_gray>Kill a player to disguise yourself as them", "", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <dark_gray>Temporarily steal your opponents effect", "", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: Unknown", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: Unknown"),
        AUG_THIEF_NAME("<dark_red>Augmented Thief Effect"),
        AUG_THIEF_LORE("<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <dark_gray>You're not shown on tablist", "<dark_red>🥷 <dark_gray>Your footsteps don't make noise", "<dark_red>🥷 <dark_gray>Kill a player to disguise yourself as them", "<dark_gray>", "<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:", "<dark_red>🥷 <dark_gray>Temporarily steal your opponents effect", "<dark_gray>", "<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: Unknown", "<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: Unknown");

        // Enum attributes
        public final String configKey = name().toLowerCase();
        public final String defaultValue;
        public final List<String> placeholders;

        MessageType(String defaultValue) {
            this(List.of(), defaultValue.split("\n"));
        }

        MessageType(String... defaultValue) {
            this(List.of(), defaultValue);
        }

        MessageType(List<String> placeholders, String defaultValue) {
            this.placeholders = placeholders;
            this.defaultValue = defaultValue;
        }

        MessageType(List<String> placeholders, String... defaultValue) {
            this(placeholders, String.join("\n", defaultValue));
        }
    }
}