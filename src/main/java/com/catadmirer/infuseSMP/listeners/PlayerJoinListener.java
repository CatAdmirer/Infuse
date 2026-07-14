package com.catadmirer.infuseSMP.listeners;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.managers.EffectCraftManager;

public class PlayerJoinListener implements Listener {
    private final Infuse plugin;

    public PlayerJoinListener(Infuse plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void giveRecipes(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player all the infuse recipes
        InfuseEffect.getRegisteredEffects().values().stream().map(plugin.getRecipeManager()::getRecipeKey).forEach(player::discoverRecipe);
    }

    @EventHandler
    public void tellControlMode(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Telling the player their current control mode
        String controlMode = plugin.getDataManager().getControlMode(player.getUniqueId());
        if (controlMode == null) controlMode = "Offhand";
        boolean offhandEnabled = controlMode.equalsIgnoreCase("Offhand");
        player.addAttachment(plugin, "ability.use", !offhandEnabled);

        Message msg = new Message(MessageType.JOIN_ABILITY_NOTIFY);
        msg.applyPlaceholder("control_mode", controlMode);
        player.sendMessage(msg.toComponent());
    }

    /** Activates the player's effects and assigns them a starting effect if they haven't played before. */
    @EventHandler
    public void activateEffects(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player their starting effects if they haven't joined before
        if (plugin.getMainConfig().joinEffectsEnabled() && !player.hasPlayedBefore()) {
            List<InfuseEffect> effects = plugin.getMainConfig().joinEffects();
            if (effects.isEmpty()) return;

            InfuseEffect effect = effects.get((int) (Math.random() * effects.size()));
            plugin.getEffectManager().equipEffect(player, effect, "1", false);
            return;
        }

        // Enabling each effect
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        if (effect != null) effect.equip(player);

        effect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");
        if (effect != null) effect.equip(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!EffectCraftManager.isRitual()) return;
        
        event.getPlayer().showBossBar(EffectCraftManager.getBar());
    }
}