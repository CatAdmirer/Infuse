package com.catadmirer.infuseSMP.worldguard;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UseSparkHandler extends Handler {

    private static final Factory FACTORY = new Factory() {
        @Override
        public Handler create(Session session) {
            return new UseSparkHandler(session);
        }
    };

    public UseSparkHandler(Session session) {
        super(session);
    }

    @Override
    public void tick(LocalPlayer local, ApplicableRegionSet set) {
        final Player player = Bukkit.getPlayer(local.getUniqueId());
        if (player == null || !player.isOnline()) return;

        final boolean canUse = set.testState(local, WorldGuardHandler.USE_SPARKS);
        // other stuff here.
    }
}
