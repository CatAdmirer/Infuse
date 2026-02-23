package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import java.util.List;
import net.kyori.adventure.text.Component;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Apophis extends InfuseEffect {
    public Apophis() {
        super(EffectIds.APOPHIS, "apophis", false);
    }

    public Apophis(boolean augmented) {
        super(EffectIds.APOPHIS, "apophis", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_APOPHIS_NAME.toComponent() : Messages.APOPHIS_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_APOPHIS_LORE.getComponentList() : Messages.APOPHIS_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Apophis(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Apophis(false);
    }

    @Override
    public void equip(Infuse plugin, Player apophisUser) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.unlistPlayer(apophisUser);
            player.hidePlayer(plugin, apophisUser);
        }

        owner = apophisUser;
    }
    
    @Override
    public void unequip(Infuse plugin, Player apophisUser) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.listPlayer(apophisUser);
            player.showPlayer(plugin, apophisUser);
        }

        owner = apophisUser;
    }

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "apophis")) return;

        // Applying effects for the apophis spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "apophis", duration);
        CooldownManager.setCooldown(playerUUID, "apophis", cooldown);
    }
}
