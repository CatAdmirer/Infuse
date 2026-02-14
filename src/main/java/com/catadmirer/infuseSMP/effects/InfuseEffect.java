package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.Infuse;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InfuseEffect {
    public static final List<String> allKeys = List.of("emerald", "ender", "feather", "fire", "frost", "haste", "heart", "invis", "ocean", "regen", "speed", "strength", "thunder", "apophis", "thief", "aug_emerald", "aug_ender", "aug_feather", "aug_fire", "aug_frost", "aug_haste", "aug_heart", "aug_invis", "aug_ocean", "aug_regen", "aug_speed", "aug_strength", "aug_thunder", "aug_apophis", "aug_thief");

    protected final int id;
    protected final String name;
    protected final boolean augmented;
    protected OfflinePlayer owner;

    protected InfuseEffect(int id, String name, boolean augmented) {
        this(id, name, augmented, null);
    }

    protected InfuseEffect(int id, String name, boolean augmented, OfflinePlayer owner) {
        this.id = id;
        this.name = name;
        this.augmented = augmented;
        this.owner = owner;
    }

    public String getName() {
        return this.name;
    }

    public boolean isAugmented() {
        return this.augmented;
    }

    public String getKey() {
        return (augmented ? "aug_" + name : name);
    }

    public int getId() {
        return id;
    }

    @Nullable
    public OfflinePlayer getOwner() {
        return owner;
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
    }

    public abstract Component getItemName();

    public abstract List<Component> getItemLore();

    public abstract void equip(Infuse plugin, Player player);
    public abstract void unequip(Infuse plugin, Player player);

    public abstract void activateSpark(Infuse plugin, Player player);

    public abstract InfuseEffect getAugmentedForm();
    public abstract InfuseEffect getRegularForm();

    public char getIcon() {
        return (char) Integer.parseInt("E" + (augmented ? 2 : 0) + String.format("%02d", id), 16);
    }

    public char getActiveIcon() {
        return (char) Integer.parseInt("E" + (augmented ? 3 : 1) + String.format("%02d", id), 16);
    }

    // public abstract InfuseEffect regular();

    // public abstract InfuseEffect augmented();

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
            meta.displayName(getItemName());
            meta.lore(getItemLore());
            meta.setColor(org.bukkit.Color.fromARGB(EffectConstants.potionColor(id).getRGB()));
            meta.getPersistentDataContainer().set(Infuse.EFFECT_KEY, PersistentDataType.STRING, getKey());

            // Applying the custom model if the key has the "aug_" prefix
            if (augmented) {
                CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
                customModelData.setFloats(List.of(999f));
                meta.setCustomModelDataComponent(customModelData);
            }

            effectItem.setItemMeta(meta);
        }

        return effectItem;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof InfuseEffect effect)) return false;

        return effect.getKey().equals(getKey());
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

        return getKey().equals(item.getItemMeta().getPersistentDataContainer().get(Infuse.EFFECT_KEY, PersistentDataType.STRING));
    }

    /**
     * Gets an {@link InfuseEffect} from an {@link ItemStack}.
     * 
     * @param item The item to get the effect from.
     * 
     * @return The effect mapping the item corresponds to.  Returns null if the item is null or has an invalid effect key.
     */
    @Nullable
    public static InfuseEffect fromItem(@Nullable ItemStack item) {
        if (item == null) return null;
        if (item.getType() != Material.POTION) return null;
        if (!item.hasItemMeta()) return null;

        String key = item.getItemMeta().getPersistentDataContainer().get(Infuse.EFFECT_KEY, PersistentDataType.STRING);
        if (key == null) return null;

        return fromEffectKey(key);
    }

    /**
     * Gets an {@link InfuseEffect} from the effect's key.
     * 
     * @param key The key of the effect.
     * 
     * @return The effect mapping the item corresponds to.  Returns null if the key is invalid.
     */
    @Nullable
    public static InfuseEffect fromEffectKey(@Nullable String key) {
        // TODO: Add the rest of the effect keys
        switch (key) {
            case "emerald":
                return new Emerald();
        
            default:
                break;
        }

        return null;
    }
}
