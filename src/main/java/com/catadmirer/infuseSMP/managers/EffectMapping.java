package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.*;
import com.catadmirer.infuseSMP.playerdata.DataManager;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import java.awt.Color;
import java.util.List;
import java.util.function.BiConsumer;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EffectMapping {
    // Defining regular effects
    EMERALD  ("emerald",   1, Color.GREEN,         BossBar.Color.GREEN,  Emerald::activateSpark),
    ENDER    ("ender",     2, new Color(0x800080), BossBar.Color.PURPLE, Ender::activateSpark),
    FEATHER  ("feather",   3, new Color(0xBEA3CA), BossBar.Color.WHITE,  Feather::activateSpark),
    FIRE     ("fire",      4, new Color(0xEE5522), BossBar.Color.RED,    Fire::activateSpark),
    FROST    ("frost",     5, new Color(0x55FFFF), BossBar.Color.BLUE,   Frost::activateSpark),
    HASTE    ("haste",     6, new Color(0xFFCC33), BossBar.Color.YELLOW, Haste::activateSpark),
    HEART    ("heart",     7, Color.RED,           BossBar.Color.RED,    Heart::activateSpark),
    INVIS    ("invis",     8, new Color(0xAA00AA), BossBar.Color.PURPLE, Invisibility::activateSpark),
    OCEAN    ("ocean",     9, new Color(0x0066FF), BossBar.Color.BLUE,   Ocean::activateSpark),
    REGEN    ("regen",    10, new Color(0xFF5555), BossBar.Color.PINK,   Regen::activateSpark),
    SPEED    ("speed",    11, new Color(0xEEBB77), BossBar.Color.YELLOW, Speed::activateSpark),
    STRENGTH ("strength", 12, new Color(0x800000), BossBar.Color.RED,    Strength::activateSpark),
    THUNDER  ("thunder",  13, Color.YELLOW,        BossBar.Color.YELLOW, Thunder::activateSpark),
    APOPHIS  ("apophis",  14, new Color(0x440044), BossBar.Color.PURPLE, Apophis::activateSpark),
    THIEF    ("thief",    15, new Color(0xAA0000), BossBar.Color.RED,    Thief::activateSpark),

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
    private EffectMapping(String key, int id, Color potionColor, BossBar.Color ritualColor, BiConsumer<Boolean,Player> sparkFunction) {
        this.key = key;
        this.id = id;
        this.color = potionColor;
        this.ritualColor = ritualColor;
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
        this.sparkFunction = base.sparkFunction;

        regular = base;
        augmented = this;
        base.augmented = this;
    }

    private static DataManager dataManager;

    public static void init(DataManager dataManager) {
        EffectMapping.dataManager = dataManager;
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
    public String getName() {
        return switch (this) {
            case EMERALD -> Messages.EMERALD_NAME.getMessage();
            case ENDER -> Messages.ENDER_NAME.getMessage();
            case FEATHER -> Messages.FEATHER_NAME.getMessage();
            case FIRE -> Messages.FIRE_NAME.getMessage();
            case FROST -> Messages.FROST_NAME.getMessage();
            case HASTE -> Messages.HASTE_NAME.getMessage();
            case HEART -> Messages.HEART_NAME.getMessage();
            case INVIS -> Messages.INVIS_NAME.getMessage();
            case OCEAN -> Messages.OCEAN_NAME.getMessage();
            case REGEN -> Messages.REGEN_NAME.getMessage();
            case SPEED -> Messages.SPEED_NAME.getMessage();
            case STRENGTH -> Messages.STRENGTH_NAME.getMessage();
            case THUNDER -> Messages.THUNDER_NAME.getMessage();
            case APOPHIS -> Messages.APOPHIS_NAME.getMessage();
            case THIEF -> Messages.THIEF_NAME.getMessage();
            case AUG_EMERALD -> Messages.AUG_EMERALD_NAME.getMessage();
            case AUG_ENDER -> Messages.AUG_ENDER_NAME.getMessage();
            case AUG_FEATHER -> Messages.AUG_FEATHER_NAME.getMessage();
            case AUG_FIRE -> Messages.AUG_FIRE_NAME.getMessage();
            case AUG_FROST -> Messages.AUG_FROST_NAME.getMessage();
            case AUG_HASTE -> Messages.AUG_HASTE_NAME.getMessage();
            case AUG_HEART -> Messages.AUG_HEART_NAME.getMessage();
            case AUG_INVIS -> Messages.AUG_INVIS_NAME.getMessage();
            case AUG_OCEAN -> Messages.AUG_OCEAN_NAME.getMessage();
            case AUG_REGEN -> Messages.AUG_REGEN_NAME.getMessage();
            case AUG_SPEED -> Messages.AUG_SPEED_NAME.getMessage();
            case AUG_STRENGTH -> Messages.AUG_STRENGTH_NAME.getMessage();
            case AUG_THUNDER -> Messages.AUG_THUNDER_NAME.getMessage();
            case AUG_APOPHIS -> Messages.AUG_APOPHIS_NAME.getMessage();
            case AUG_THIEF -> Messages.AUG_THIEF_NAME.getMessage();
        };
    }

    /**
     * Getting the lore of the effect from the config.
     * 
     * @return The lore of the effect as defined in the config.
     */
    public List<String> getLore() {
        return switch (this) {
            case EMERALD -> Messages.EMERALD_LORE.getStringList();
            case ENDER -> Messages.ENDER_LORE.getStringList();
            case FEATHER -> Messages.FEATHER_LORE.getStringList();
            case FIRE -> Messages.FIRE_LORE.getStringList();
            case FROST -> Messages.FROST_LORE.getStringList();
            case HASTE -> Messages.HASTE_LORE.getStringList();
            case HEART -> Messages.HEART_LORE.getStringList();
            case INVIS -> Messages.INVIS_LORE.getStringList();
            case OCEAN -> Messages.OCEAN_LORE.getStringList();
            case REGEN -> Messages.REGEN_LORE.getStringList();
            case SPEED -> Messages.SPEED_LORE.getStringList();
            case STRENGTH -> Messages.STRENGTH_LORE.getStringList();
            case THUNDER -> Messages.THUNDER_LORE.getStringList();
            case APOPHIS -> Messages.APOPHIS_LORE.getStringList();
            case THIEF -> Messages.THIEF_LORE.getStringList();
            case AUG_EMERALD -> Messages.AUG_EMERALD_LORE.getStringList();
            case AUG_ENDER -> Messages.AUG_ENDER_LORE.getStringList();
            case AUG_FEATHER -> Messages.AUG_FEATHER_LORE.getStringList();
            case AUG_FIRE -> Messages.AUG_FIRE_LORE.getStringList();
            case AUG_FROST -> Messages.AUG_FROST_LORE.getStringList();
            case AUG_HASTE -> Messages.AUG_HASTE_LORE.getStringList();
            case AUG_HEART -> Messages.AUG_HEART_LORE.getStringList();
            case AUG_INVIS -> Messages.AUG_INVIS_LORE.getStringList();
            case AUG_OCEAN -> Messages.AUG_OCEAN_LORE.getStringList();
            case AUG_REGEN -> Messages.AUG_REGEN_LORE.getStringList();
            case AUG_SPEED -> Messages.AUG_SPEED_LORE.getStringList();
            case AUG_STRENGTH -> Messages.AUG_STRENGTH_LORE.getStringList();
            case AUG_THUNDER -> Messages.AUG_THUNDER_LORE.getStringList();
            case AUG_APOPHIS -> Messages.AUG_APOPHIS_LORE.getStringList();
            case AUG_THIEF -> Messages.AUG_THIEF_LORE.getStringList();
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
            meta.displayName(Messages.toComponent(getName()));
            meta.lore(getLore().stream().map(Messages::toComponent).toList());
            meta.setColor(org.bukkit.Color.fromARGB(color.getRGB()));
            meta.getPersistentDataContainer().set(Infuse.EFFECT_KEY, PersistentDataType.STRING, key);

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

    /**
     * Activates the spark for the player.
     * 
     * @param player The player to activate the spark ability for.
     */
    public void activateSpark(Player player) {
        sparkFunction.accept(isAugmented(), player);
    }

    /**
     * Checks if a player has this effect in any slot.
     * 
     * @param player The player to check for the effect.
     * 
     * @return Whether or not the player has this effect equipped in any slot.
     */
    public boolean hasEffect(OfflinePlayer player) {
        return hasEffect(player, "1") || hasEffect(player, "2");
    }

    /**
     * Checks if a player has this effect in a specific slot.
     * 
     * @param player The player to check for the effect.
     * @param slot The slot to check for the effect in.
     * 
     * @return Whether or not the player has this effect equipped in the provided slot.
     */
    public boolean hasEffect(OfflinePlayer player, String slot) {
        return this == dataManager.getEffect(player.getUniqueId(), slot);
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