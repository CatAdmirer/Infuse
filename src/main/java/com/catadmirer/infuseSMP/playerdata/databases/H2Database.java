package com.catadmirer.infuseSMP.playerdata.databases;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.playerdata.DataManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.bukkit.OfflinePlayer;
import org.h2.jdbcx.JdbcDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class H2Database implements DataManager {
    private final Infuse plugin;
    private final DataSource dataSource;

    public H2Database(Infuse plugin) {
        this.plugin = plugin;

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Creating the JDBC DataSource object
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:./" + plugin.getDataFolder().getPath() + "/data/playerdata");
        dataSource.setUser("infuse");
        dataSource.setPassword("");
        dataSource.setDescription("Infuse Playerdata Storage");

        this.dataSource = dataSource;
    }

    @Override
    public boolean load() {
        final String createPlayerDataDb = "CREATE TABLE IF NOT EXISTS player_data(player UUID PRIMARY KEY, slot_1 INTEGER NOT NULL, slot_2 INTEGER NOT NULL, offhand_control BOOLEAN NOT NULL);";
        final String createTrustDb = "CREATE TABLE IF NOT EXISTS trusts(truster UUID NOT NULL, trusted UUID NOT NULL);";

        try(Connection conn = dataSource.getConnection()) {
            // Creating the tables if they dont exist
            try (PreparedStatement stmt = conn.prepareStatement(createPlayerDataDb)) {
                stmt.execute();
            } catch (SQLException err) {
                plugin.getLogger().log(Level.WARNING, "Could not create the player_data table.", err);
            }

            try (PreparedStatement stmt = conn.prepareStatement(createTrustDb)) {
                stmt.execute();
            } catch (SQLException err) {
                plugin.getLogger().log(Level.WARNING, "Could not create the trusts table.", err);
            }
            
            conn.commit();
            return true;
        } catch (SQLException err) {
            plugin.getLogger().log(Level.SEVERE, "Could not open connection to H2 database", err);
        }

        return false;
    }

    public boolean save() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public @NotNull List<OfflinePlayer> getTrusted(@NotNull OfflinePlayer truster) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTrusted'");
    }

    @Override
    public void setTrusted(@NotNull OfflinePlayer truster, @NotNull List<OfflinePlayer> trusted) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTrusted'");
    }

    @Override
    public void addTrust(@NotNull OfflinePlayer truster, @NotNull OfflinePlayer toTrust) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addTrust'");
    }

    @Override
    public void removeTrust(@NotNull OfflinePlayer truster, @NotNull OfflinePlayer toRemove) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeTrust'");
    }

    @Override
    public boolean isTrusted(@NotNull OfflinePlayer truster, @NotNull OfflinePlayer toCheck) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isTrusted'");
    }

    @Override
    public void setEffect(@NotNull UUID playerUUID, @NotNull String slot, @NotNull EffectMapping effect) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setEffect'");
    }

    @Override
    public @Nullable EffectMapping getEffect(@NotNull UUID playerUUID, @NotNull String slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEffect'");
    }

    @Override
    public boolean hasEffect(OfflinePlayer player, EffectMapping effect, boolean differentiateAugmented, String slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasEffect'");
    }

    @Override
    public void removeEffect(@NotNull UUID playerUUID, @NotNull String slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeEffect'");
    }

    @Override
    public void setControlMode(@NotNull UUID playerUUID, @NotNull String defaultMode) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setControlMode'");
    }

    @Override
    public @NotNull String getControlMode(@NotNull UUID playerUUID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getControlMode'");
    }
}
