package com.catadmirer.infuseSMP.bukkit.listeners;

import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import com.catadmirer.infuseSMP.bukkit.managers.ParticleManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class EntityDropItemListener implements Listener {
    private final InfusePlugin plugin;

    public EntityDropItemListener(InfusePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(EntityDropItemEvent event) {
        final Item droppedItem = event.getItemDrop();
        ItemStack itemStack = droppedItem.getItemStack();
        InfuseEffect effect = InfuseEffect.fromItem(itemStack);
        if (effect == null) return;
        ParticleManager.dropEffect(plugin, false, effect, droppedItem.getLocation());
        droppedItem.setGlowing(true);
    }
}