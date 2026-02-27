package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.*;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;

import java.awt.Color;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EffectMapping {
    // Defining regular effects
    EMERALD  ("emerald",   1, Color.GREEN,         BossBar.Color.GREEN,  Emerald::applyPassiveEffects,      Emerald::activateSpark),
    ENDER    ("ender",     2, new Color(0x800080), BossBar.Color.PURPLE, Ender::applyPassiveEffects,        Ender::activateSpark),
    FEATHER  ("feather",   3, new Color(0xBEA3CA), BossBar.Color.WHITE,  p -> {},        Feather::activateSpark),
    FIRE     ("fire",      4, new Color(0xEE5522), BossBar.Color.RED,    Fire::applyPassiveEffects,         Fire::activateSpark),
    FROST    ("frost",     5, new Color(0x55FFFF), BossBar.Color.BLUE,   Frost::applyPassiveEffects,        Frost::activateSpark),
    HASTE    ("haste",     6, new Color(0xFFCC33), BossBar.Color.YELLOW, Haste::applyPassiveEffects,        Haste::activateSpark),
    HEART    ("heart",     7, Color.RED,           BossBar.Color.RED,    Heart::applyPassiveEffects,        Heart::activateSpark),
    INVIS    ("invis",     8, new Color(0xAA00AA), BossBar.Color.PURPLE, Invisibility::applyPassiveEffects, Invisibility::activateSpark),
    OCEAN    ("ocean",     9, new Color(0x0066FF), BossBar.Color.BLUE,   Ocean::applyPassiveEffects,        Ocean::activateSpark),
    REGEN    ("regen",    10, new Color(0xFF5555), BossBar.Color.PINK,   p -> {},        Regen::activateSpark),
    SPEED    ("speed",    11, new Color(0xEEBB77), BossBar.Color.YELLOW, Speed::applyPassiveEffects,        Speed::activateSpark),
    STRENGTH ("strength", 12, new Color(0x800000), BossBar.Color.RED,    p -> {},     Strength::activateSpark),
    THUNDER  ("thunder",  13, Color.YELLOW,        BossBar.Color.YELLOW, p -> {},      Thunder::activateSpark),
    APOPHIS  ("apophis",  14, new Color(0x440044), BossBar.Color.PURPLE, Apophis::applyPassiveEffects,      Apophis::activateSpark),
    THIEF    ("thief",    15, new Color(0xAA0000), BossBar.Color.RED,    Thief::applyPassiveEffects,        Thief::activateSpark),

    // Defining augmented effects
    AUG_EMERALD(EMERALD),
    AUG_ENDER(ENDER),
    AUG_FEATHER(FEATHER),
    AUG_FIRE(FIRE),
    AUG_FROST(FROST),
    AUG_HASTE(HASTE),
    AUG_HEART(HEART),
    AUG_INVIS(INVIS),
    AUG_OCEAN(OCEAN),
    AUG_REGEN(REGEN),
    AUG_SPEED(SPEED),
    AUG_STRENGTH(STRENGTH),
    AUG_THUNDER(THUNDER),
    AUG_APOPHIS(APOPHIS),
    AUG_THIEF(THIEF);

    private final String key;
    private final int id;
    private final Color color;
    private final BossBar.Color ritualColor;
    private final Consumer<Player> passiveFunction;
    private final BiConsumer<Boolean,Player> sparkFunction;

    private EffectMapping regular;
    private EffectMapping augmented;

    /**
     * Constructor for regular effects.
     * 
     * @param key The base key for the effect.
     * @param id The id for the effect.
     * @param potionColor The color for the potion and related chat messages.
     * @param ritualColor The bossbar color to use during rituals.
     */
    private EffectMapping(String key, int id, Color potionColor, BossBar.Color ritualColor, Consumer<Player> passiveFunction, BiConsumer<Boolean,Player> sparkFunction) {
        this.key = key;
        this.id = id;
        this.color = potionColor;
        this.ritualColor = ritualColor;
        this.passiveFunction = passiveFunction;
        this.sparkFunction = sparkFunction;

        regular = this;
    }

    /**
     * Constructor for augmented effects.
     * Attributes from the base mapping will be copied to this one.
     * 
     * @param base The base effect mapping for this augmented effect.
     */
    private EffectMapping(EffectMapping base) {
        this.key = "aug_" + base.key;
        this.id = base.id;
        this.color = base.color;
        this.ritualColor = base.ritualColor;
        this.passiveFunction = base.passiveFunction;
        this.sparkFunction = base.sparkFunction;

        regular = base;
        augmented = this;
        base.augmented = this;
    }

    /**
     * Getting the key for the effect.
     * 
     * @return The key for the effect.
     */
    public String getKey() {
        return key;
    }

    /**
     * Getting the id for the effect.
     * 
     * @return The id for the effect.
     */
    public int getId() {
        return id;
    }

    /**
     * Getting the color for the effect.
     * 
     * @return The color for the effect.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Getting the boss bar color for rituals.
     *
     * @return The boss bar color for rituals.
     */
    public BossBar.Color getRitualColor() {
        return ritualColor;
    }

    /**
     * Getting the name of the effect from the config.
     * 
     * @return The name of the effect as defined in the config.
     */
    @NotNull
    public Message getName() {
        return switch (this) {
            case EMERALD -> new Message(MessageType.EMERALD_NAME);
            case ENDER -> new Message(MessageType.ENDER_NAME);
            case FEATHER -> new Message(MessageType.FEATHER_NAME);
            case FIRE -> new Message(MessageType.FIRE_NAME);
            case FROST -> new Message(MessageType.FROST_NAME);
            case HASTE -> new Message(MessageType.HASTE_NAME);
            case HEART -> new Message(MessageType.HEART_NAME);
            case INVIS -> new Message(MessageType.INVIS_NAME);
            case OCEAN -> new Message(MessageType.OCEAN_NAME);
            case REGEN -> new Message(MessageType.REGEN_NAME);
            case SPEED -> new Message(MessageType.SPEED_NAME);
            case STRENGTH -> new Message(MessageType.STRENGTH_NAME);
            case THUNDER -> new Message(MessageType.THUNDER_NAME);
            case APOPHIS -> new Message(MessageType.APOPHIS_NAME);
            case THIEF -> new Message(MessageType.THIEF_NAME);
            case AUG_EMERALD -> new Message(MessageType.AUG_EMERALD_NAME);
            case AUG_ENDER -> new Message(MessageType.AUG_ENDER_NAME);
            case AUG_FEATHER -> new Message(MessageType.AUG_FEATHER_NAME);
            case AUG_FIRE -> new Message(MessageType.AUG_FIRE_NAME);
            case AUG_FROST -> new Message(MessageType.AUG_FROST_NAME);
            case AUG_HASTE -> new Message(MessageType.AUG_HASTE_NAME);
            case AUG_HEART -> new Message(MessageType.AUG_HEART_NAME);
            case AUG_INVIS -> new Message(MessageType.AUG_INVIS_NAME);
            case AUG_OCEAN -> new Message(MessageType.AUG_OCEAN_NAME);
            case AUG_REGEN -> new Message(MessageType.AUG_REGEN_NAME);
            case AUG_SPEED -> new Message(MessageType.AUG_SPEED_NAME);
            case AUG_STRENGTH -> new Message(MessageType.AUG_STRENGTH_NAME);
            case AUG_THUNDER -> new Message(MessageType.AUG_THUNDER_NAME);
            case AUG_APOPHIS -> new Message(MessageType.AUG_APOPHIS_NAME);
            case AUG_THIEF -> new Message(MessageType.AUG_THIEF_NAME);
        };
    }

    /**
     * Getting the lore of the effect from the config.
     * 
     * @return The lore of the effect as defined in the config.
     */
    public Message getLore() {
        return switch (this) {
            case EMERALD -> new Message(MessageType.EMERALD_LORE);
            case ENDER -> new Message(MessageType.ENDER_LORE);
            case FEATHER -> new Message(MessageType.FEATHER_LORE);
            case FIRE -> new Message(MessageType.FIRE_LORE);
            case FROST -> new Message(MessageType.FROST_LORE);
            case HASTE -> new Message(MessageType.HASTE_LORE);
            case HEART -> new Message(MessageType.HEART_LORE);
            case INVIS -> new Message(MessageType.INVIS_LORE);
            case OCEAN -> new Message(MessageType.OCEAN_LORE);
            case REGEN -> new Message(MessageType.REGEN_LORE);
            case SPEED -> new Message(MessageType.SPEED_LORE);
            case STRENGTH -> new Message(MessageType.STRENGTH_LORE);
            case THUNDER -> new Message(MessageType.THUNDER_LORE);
            case APOPHIS -> new Message(MessageType.APOPHIS_LORE);
            case THIEF -> new Message(MessageType.THIEF_LORE);
            case AUG_EMERALD -> new Message(MessageType.AUG_EMERALD_LORE);
            case AUG_ENDER -> new Message(MessageType.AUG_ENDER_LORE);
            case AUG_FEATHER -> new Message(MessageType.AUG_FEATHER_LORE);
            case AUG_FIRE -> new Message(MessageType.AUG_FIRE_LORE);
            case AUG_FROST -> new Message(MessageType.AUG_FROST_LORE);
            case AUG_HASTE -> new Message(MessageType.AUG_HASTE_LORE);
            case AUG_HEART -> new Message(MessageType.AUG_HEART_LORE);
            case AUG_INVIS -> new Message(MessageType.AUG_INVIS_LORE);
            case AUG_OCEAN -> new Message(MessageType.AUG_OCEAN_LORE);
            case AUG_REGEN -> new Message(MessageType.AUG_REGEN_LORE);
            case AUG_SPEED -> new Message(MessageType.AUG_SPEED_LORE);
            case AUG_STRENGTH -> new Message(MessageType.AUG_STRENGTH_LORE);
            case AUG_THUNDER -> new Message(MessageType.AUG_THUNDER_LORE);
            case AUG_APOPHIS -> new Message(MessageType.AUG_APOPHIS_LORE);
            case AUG_THIEF -> new Message(MessageType.AUG_THIEF_LORE);
        };
    }

    public char getIcon() {
        return (char) Integer.parseInt("E" + (isAugmented() ? 2 : 0) + String.format("%02d", getId()), 16);
    }

    public char getActiveIcon() {
        return (char) Integer.parseInt("E" + (isAugmented() ? 3 : 1) + String.format("%02d", getId()), 16);
    }

    /**
     * Getting the effect in its regular form.
     * If the enum is already the regular form, it returns itself.
     * 
     * @return The regular version of the effect.
     */
    public EffectMapping regular() {
        return regular;
    }

    /**
     * Getting the effect in its augmented form.
     * If the enum is already the augmented form, it returns itself.
     * 
     * @return The augmented version of the effect.
     */
    public EffectMapping augmented() {
        return augmented;
    }

    /**
     * Gets whether or not this effect is augmented.
     * 
     * @return Whether or not this effect is augmented.
     */
    public boolean isAugmented() {
        return this == augmented;
    }


    /**
     * Creates the effect as a potion item.
     * 
     * @return An {@link ItemStack} instance for a player to use to get an effect.
     */
    @NotNull
    public ItemStack createItem() {
        ItemStack effectItem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effectItem.getItemMeta();

        if (meta != null) {
            // Setting the usual data
            meta.displayName(getName().toComponent());
            meta.lore(getLore().toComponentList());
            meta.setColor(org.bukkit.Color.fromARGB(color.getRGB()));
            meta.getPersistentDataContainer().set(Infuse.EFFECT_KEY, PersistentDataType.STRING, key);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

            // Applying the custom model if the key has the "aug_" prefix
            if (this == augmented) {
                CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
                customModelData.setFloats(List.of(999f));
                meta.setCustomModelDataComponent(customModelData);
            }

            effectItem.setItemMeta(meta);
        }

        return effectItem;
    }

    public void applyPassiveEffects(Player player) {
        passiveFunction.accept(player);
    }

    /**
     * Activates the spark for the player.
     * 
     * @param player The player to activate the spark ability for.
     */
    public void activateSpark(Player player) {
        sparkFunction.accept(isAugmented(), player);
    }

    /**
     * Checks if an {@link ItemStack} was created by this effect.
     * 
     * @param item The item to check.
     * 
     * @return Whether or not the item was created by this effect.
     */
    public boolean isEffect(@Nullable ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.POTION) return false;
        if (!item.hasItemMeta()) return false;

        return key.equals(item.getItemMeta().getPersistentDataContainer().get(Infuse.EFFECT_KEY, PersistentDataType.STRING));
    }

    /**
     * Gets an {@link EffectMapping} from an {@link ItemStack}.
     * 
     * @param item The item to get the effect from.
     * 
     * @return The effect mapping the item corresponds to.  Returns null if the item is null or has an invalid effect key.
     */
    @Nullable
    public static EffectMapping fromItem(@Nullable ItemStack item) {
        if (item == null) return null;
        if (item.getType() != Material.POTION) return null;
        if (!item.hasItemMeta()) return null;

        String key = item.getItemMeta().getPersistentDataContainer().get(Infuse.EFFECT_KEY, PersistentDataType.STRING);
        if (key == null) return null;

        return fromEffectKey(key);
    }

    /**
     * Gets an {@link EffectMapping} from the name of an effect.
     * 
     * @param name The name of the effect.
     * 
     * @return The effect mapping the item corresponds to.  Returns null if the name is not shared with any effect.
     */
    @Nullable
    public static EffectMapping fromEffectName(@Nullable String name) {
        for (EffectMapping mapping : values()) {
            
            if (mapping.getName().equals(name)) return mapping;
        }

        return null;
    }

    /**
     * Gets an {@link EffectMapping} from the effect's key.
     * 
     * @param key The key of the effect.
     * 
     * @return The effect mapping the item corresponds to.  Returns null if the key is invalid.
     */
    @Nullable
    public static EffectMapping fromEffectKey(@Nullable String key) {
        for (EffectMapping mapping : values()) {
            if (mapping.getKey().equalsIgnoreCase(key)) return mapping;
        }

        return null;
    }
}