package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InfuseEffect implements Listener {
    private static final Map<Integer,InfuseEffect> REGISTERED_BY_ID = new HashMap<>();
    private static final Map<String,InfuseEffect> REGISTERED_BY_KEY = new HashMap<>();

    public static final NamespacedKey EFFECT_KEY = new NamespacedKey("infuse", "effect_key");
    public static final NamespacedKey AUG_KEY = new NamespacedKey("infuse", "aug");

    protected final String key;
    protected final int id;
    protected final boolean augmented;
    protected final Color potionColor;
    protected final BossBar.Color ritualColor;
    protected final Material backgroundMaterial;
    protected final Infuse plugin = Infuse.getInstance();

    public InfuseEffect(String key, int id, boolean augmented, Color potionColor, BossBar.Color ritualColor, Material backgroundMaterial) {
        this.key = key;
        this.id = id;
        this.augmented = augmented;
        this.potionColor = potionColor;
        this.ritualColor = ritualColor;
        this.backgroundMaterial = backgroundMaterial;
    }

    public static boolean isRegistered(InfuseEffect effect) {
        return isRegistered(effect.id);
    }

    public static boolean isRegistered(int id) {
        return REGISTERED_BY_ID.containsKey(id);
    }

    public static boolean isRegistered(String key) {
        return REGISTERED_BY_KEY.containsKey(key);
    }

    public static boolean register(InfuseEffect effect) {
        // Enforcing the id limit
        if (effect.id > 100) {
            Infuse.LOGGER.warn("Effect id {} for {} is invalid.  Effect ids cannot be >100.", effect.id, effect.key);
            return false;
        }

        if (isRegistered(effect.id)) {
            InfuseEffect existing = REGISTERED_BY_ID.get(effect.id);
            Infuse.LOGGER.warn("Effect id {} has already been taken by {}.  Cannot assign it to {}.", effect.id, existing.key, effect.key);
            return false;
        }

        if (isRegistered(effect.key)) {
            InfuseEffect existing = REGISTERED_BY_KEY.get(effect.key);
            Infuse.LOGGER.warn("Effect key {} has already been taken by {}.  Cannot assign it to {}.", effect.key, existing.key, effect.key);
            return false;
        }

        // Attempting to register the effect
        REGISTERED_BY_ID.put(effect.id, effect);
        REGISTERED_BY_KEY.put(effect.key, effect);

        // Registering event listeners in the effect
        Bukkit.getPluginManager().registerEvents(effect, Infuse.getInstance());

        return true;
    }

    /**
     * Gets a registered effect.
     * 
     * @param id The id of the effect.
     * 
     * @return The registered effect or null if no effect is registered under the specified id.
     */
    @Nullable
    public static InfuseEffect getEffect(int id) {
        return REGISTERED_BY_ID.get(id);
    }

    /**
     * Gets a registered effect.
     * 
     * @param id The id of the effect.
     * 
     * @return The registered effect or null if no effect is registered under the specified id.
     */
    @Nullable
    public static InfuseEffect getEffect(EffectConstants.Id id) {
        return REGISTERED_BY_ID.get(id.value());
    }

    /**
     * Gets a registered effect.
     * 
     * @param key The key of the effect.
     * 
     * @return The registered effect or null if no effect is registered under the specified key.
     */
    @Nullable
    public static InfuseEffect getEffect(String key) {
        return REGISTERED_BY_ID.values().stream().filter(e -> e.key.equals(key)).findFirst().orElse(null);
    }

    /**
     * Gets a registered effect.
     * 
     * @param item An item created by an effect.
     * 
     * @return The registered effect or null if the item does not come from a registered effect.
     */
    public static InfuseEffect getEffect(@Nullable ItemStack item) {
        if (item == null) return null;
        if (item.getType() != Material.POTION) return null;

        String key = item.getPersistentDataContainer().get(EFFECT_KEY, PersistentDataType.STRING);
        if (key == null) return null;

        return getEffect(key);
    }

    /** Gets the list of registered effects. */
    @NonNull
    @Unmodifiable
    public static List<InfuseEffect> getRegisteredEffects() {
        return List.copyOf(REGISTERED_BY_ID.values());
    }

    public int getId() {
        return id;
    }

    public String getPlainKey() {
        return key;
    }

    public String getKey() {
        return toString();
    }

    public boolean isAugmented() {
        return augmented;
    }

    public Color getPotionColor() {
        return potionColor;
    }

    public BossBar.Color getRitualColor() {
        return ritualColor;
    }

    public Material getBackgroundMaterial() {
        return backgroundMaterial;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof InfuseEffect effect)) return false;

        return effect.augmented == this.augmented && effect.id == this.id;
    }

    @Override
    public String toString() {
        return (augmented ? "aug_" : "") + key;
    }

    public abstract void equip(Player owner);
    public abstract void unequip(Player owner);

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated()
    public void applyPassives(Player owner) {}
    public abstract void activateSpark(Player owner);

    public abstract InfuseEffect getRegularVersion();
    public abstract InfuseEffect getAugmentedVersion();

    public abstract Message getName();
    public abstract Message getLore();

    public char getIcon() {
        return (char) Integer.parseInt("E" + (augmented ? 2 : 0) + String.format("%02d", id + 1), 16);
    }

    public char getActiveIcon() {
        return (char) Integer.parseInt("E" + (augmented ? 3 : 1) + String.format("%02d", id + 1), 16);
    }

    /**
     * Creates an {@link ItemStack} representation of the effect for a player to consume.
     *
     * @return The corresponding {@link ItemStack}
     */
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.POTION);

        // Adjusting item data
        item.setData(DataComponentTypes.CUSTOM_NAME, getName().toComponent());
        item.setData(DataComponentTypes.LORE, ItemLore.lore(getLore().toComponentList()));
        item.editPersistentDataContainer(c -> c.set(EFFECT_KEY, PersistentDataType.STRING, toString()));

        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.POTION_CONTENTS));
        item.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().customColor(org.bukkit.Color.fromARGB(potionColor.getRGB())));

        if (augmented) {
            item.setData(DataComponentTypes.ITEM_MODEL, AUG_KEY);
        }

        return item;
    }

    @Nullable
    public ItemStack createItemWithLimits() {
        // Only regular effects should be put here
        if (isAugmented()) return null;

        // Creating the potion from the effect
        ItemStack potionItem = createItem();

        // Getting an instance of the plugin to read configs
        Infuse plugin = Infuse.getInstance();

        int augLeft = plugin.getMainConfig().getCraftLimit(getAugmentedVersion()) - plugin.getDataManager().getExistingCount(getAugmentedVersion());
        int regLeft = plugin.getMainConfig().getCraftLimit(getRegularVersion()) - plugin.getDataManager().getExistingCount(getRegularVersion());

        List<Component> lore = new ArrayList<>();
        lore.add(Message.toComponent("<gray>Augmented Limit: <aqua>" + augLeft));
        lore.add(Message.toComponent("<gray>Regular Limit: <aqua>" + regLeft));
        potionItem.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

        return potionItem;
    }

    /**
     * Checks if an {@link ItemStack} was created by this effect.
     *
     * @param item The item to check.
     *
     * @return Whether or not the item was created by this effect.
     */
    public boolean itemMatches(@Nullable ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.POTION) return false;

        return key.equals(item.getPersistentDataContainer().get(EFFECT_KEY, PersistentDataType.STRING));
    }
}
