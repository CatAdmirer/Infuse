package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.kyori.adventure.text.Component;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Thief extends InfuseEffect {
    public Thief() {
        super(EffectIds.THIEF, "thief", false);
    }

    public Thief(boolean augmented) {
        super(EffectIds.THIEF, "thief", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_THIEF_NAME.toComponent() : Messages.THIEF_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_THIEF_LORE.getComponentList() : Messages.THIEF_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Thief(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Thief(false);
    }

    // Hiding a thief user from the rest of the players online
    @Override
    public void equip(Infuse plugin, Player thiefUser) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.unlistPlayer(thiefUser);
            player.hidePlayer(plugin, thiefUser);
        }

        owner = thiefUser;
    }
    
    @Override
    public void unequip(Infuse plugin, Player thiefUser) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.listPlayer(thiefUser);
            player.showPlayer(plugin, thiefUser);
        }

        owner = thiefUser;
    }

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "thief")) return;

        // Applying effects for the thief spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown);
    }
}