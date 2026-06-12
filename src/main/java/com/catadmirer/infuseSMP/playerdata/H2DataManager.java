package com.catadmirer.infuseSMP.playerdata;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.h2.jdbcx.JdbcDataSource;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;

@NullMarked
public class H2DataManager implements DataManager {
    private final DataSource dataSource;

    public H2DataManager(Infuse plugin) {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException err) {
            Infuse.LOGGER.error("Could not load the H2 driver", err);
        }

        // Creating the JDBC DataSource
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:./" + plugin.getDataFolder().getPath() + "/data/playerdata");
        dataSource.setUser("infuse");
        dataSource.setPassword("");
        dataSource.setDescription("Infuse PlayerData Storage");

        this.dataSource = dataSource;
    }

    @Override
    public void load() {
        final String createPlayerDataTable = "CREATE TABLE IF NOT EXISTS player_data(player UUID PRIMARY KEY NOT NULL, slot_1 INTEGER, slot_2 INTEGER, offhand_control BOOLEAN NOT NULL);";
        final String createTrustTable = "CREATE TABLE IF NOT EXISTS trusts(truster UUID NOT NULL, trusted UUID NOT NULL);";
        final String createCraftedTable = "CREATE TABLE IF NOT EXISTS crafted_effects(effect INTEGER PRIMARY KEY NOT NULL, crafted INTEGER NOT NULL);";

        try (Connection conn = dataSource.getConnection()) {
            // Creating the tables if they don't exist
            Statement stmt = conn.createStatement();
            stmt.execute(createPlayerDataTable);
            stmt.execute(createTrustTable);
            stmt.execute(createCraftedTable);
            stmt.close();

            // Commiting any changes
            conn.commit();

            Infuse.LOGGER.info("Successfully loaded H2 database!");
        } catch (SQLException err) {
            Infuse.LOGGER.error("Could not open connection to H2 database", err);
        }
    }

    @Override
    public int getExistingCount(InfuseEffect effect) {
        String sql = "SELECT crafted FROM crafted_effects WHERE effect = ?;";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, effect.serialize());

            ResultSet results = stmt.executeQuery();
            if (!results.next()) return 0;

            return results.getInt(1);
        } catch (SQLException e) {
            Infuse.LOGGER.error("Database error!", e);
        }

        return 0;
    }

    @Override
    public void setExistingCount(InfuseEffect effect, int count) {
        String sql = "INSERT OR REPLACE INTO crafted_effects(effect, crafted) VALUES (?, ?);";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, effect.serialize());
            stmt.setInt(2, count);

            stmt.executeUpdate();
        } catch (SQLException e) {
            Infuse.LOGGER.error("Database error!", e);
        }
    }

    /**
     * Creates a new player object in the database.
     * Both effects default to null, and the offhand_control defaults to false.
     * If a row for the player already exists, nothing happens.
     *
     * @param player The player to create an entry for.
     */
    public void createNewPlayer(OfflinePlayer player) {
        String newPlayer = "INSERT OR IGNORE INTO player_data (player, offhand_control) VALUES (?, FALSE);";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(newPlayer)) {
            stmt.setObject(1, player.getUniqueId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Infuse.LOGGER.error("Database error!", e);
        }
    }

    @Override
    public Set<OfflinePlayer> getTrusted(OfflinePlayer player) {
        String selectStr = "SELECT trusted FROM trusts WHERE truster = ?";
        UUID trusterUUID = player.getUniqueId();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(selectStr);
            stmt.setObject(1, trusterUUID);

            Set<UUID> trustedUUIDs = new HashSet<>();
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                try {
                    trustedUUIDs.add(result.getObject(1, UUID.class));
                } catch (SQLException err) {
                    Infuse.LOGGER.warn("Invalid UUID in SQL results.  Skipping value.");
                }
            }

            return trustedUUIDs.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
        } catch (SQLException err) {
            Infuse.LOGGER.info("Failed to connect to database.", err);
        }
        return Set.of();
    }

    @Override
    public void setTrusted(OfflinePlayer player, Set<OfflinePlayer> allTrusted) {
        Set<OfflinePlayer> trustedCopy = new HashSet<>(allTrusted);

        String findUnused = "SELECT * FROM trusts WHERE truster = ?;";
        String deleteExtra = "DELETE FROM trusts WHERE truster = ? AND trusted = ?;";

        String insertTrusted = "INSERT INTO trusts (truster, trusted) VALUES (?, ?);";

        try (Connection conn = dataSource.getConnection()) {
            // Removing players who are no longer trusted and filtering out players who are already trusted from the copy of the set
            try (PreparedStatement stmt = conn.prepareStatement(findUnused);
                 PreparedStatement delStmt = conn.prepareStatement(deleteExtra)) {
                stmt.setObject(1, player.getUniqueId());
                stmt.execute();
                ResultSet results = stmt.getResultSet();

                delStmt.setObject(1, player.getUniqueId());

                // Looping through the trusted players
                while (results.next()) {
                    OfflinePlayer trusted = Bukkit.getOfflinePlayer(results.getObject(1, UUID.class));

                    // Skipping already trusted players
                    if (trustedCopy.contains(trusted)) {
                        trustedCopy.remove(trusted);
                        continue;
                    }

                    // Removing players who are no longer trusted
                    delStmt.setObject(2, trusted.getUniqueId());
                    delStmt.addBatch();
                }

                // Executing the delete statement
                delStmt.executeBatch();
            } catch (SQLException e) {
                Infuse.LOGGER.error("Database error!", e);
            }

            // Adding the rest of the players to trust
            try (PreparedStatement stmt = conn.prepareStatement(insertTrusted)) {
                stmt.setObject(1, player.getUniqueId());

                for (OfflinePlayer trusted : trustedCopy) {
                    stmt.setObject(2, trusted.getUniqueId());
                    stmt.addBatch();
                }

                stmt.executeBatch();
            } catch (SQLException err) {
                Infuse.LOGGER.error("Failed to insert players back into the database", err);
            }
        } catch (SQLException err) {
            Infuse.LOGGER.info("Failed to connect to database.", err);
        }

        throw new UnsupportedOperationException("Unimplemented method 'setTrusted'");
    }

    @Override
    public void addTrust(OfflinePlayer player, OfflinePlayer toTrust) {
        String insertElem = """
                            INSERT INTO trusts (truster, trusted)
                            SELECT ?, ?
                            WHERE NOT EXISTS (
                                SELECT * FROM trusts WHERE truster = ? AND trusted = ?
                            );""";

        UUID trusterUUID = player.getUniqueId();
        UUID toTrustUUID = toTrust.getUniqueId();

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(insertElem)) {
                stmt.setObject(1, trusterUUID);
                stmt.setObject(2, toTrustUUID);
                stmt.setObject(3, trusterUUID);
                stmt.setObject(4, toTrustUUID);
                stmt.execute();
            } catch (SQLException err) {
                Infuse.LOGGER.error("Failed to insert data into database", err);
            }
        } catch (SQLException err) {
            Infuse.LOGGER.info("Failed to connect to database.", err);
        }
    }

    @Override
    public void removeTrust(OfflinePlayer player, OfflinePlayer untrusted) {
        String deleteElem = "DELETE FROM trusts WHERE truster = ? AND trusted = ?";

        UUID trusterUUID = player.getUniqueId();
        UUID trustedUUID = untrusted.getUniqueId();

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(deleteElem)) {
                stmt.setObject(1, trusterUUID);
                stmt.setObject(2, trustedUUID);
                stmt.execute();
            } catch (SQLException err) {
                Infuse.LOGGER.error("Failed to remove data from the database", err);
            }
        } catch (SQLException err) {
            Infuse.LOGGER.info("Failed to connect to database.", err);
        }
    }

    @Override
    public boolean isTrusted(OfflinePlayer player, OfflinePlayer trusted) {
        String selectStr = "SELECT trusted FROM trusts WHERE truster = ? AND trusted = ?";

        UUID trusterUUID = player.getUniqueId();
        UUID trustedUUID = trusted.getUniqueId();

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(selectStr)) {
                stmt.setObject(1, trusterUUID);
                stmt.setObject(2, trustedUUID);

                return stmt.executeQuery().next();
            } catch (SQLException e) {
                Infuse.LOGGER.error("Failed to execute SQL \"{}\"", selectStr, e);
            }
        } catch (SQLException err) {
            Infuse.LOGGER.info("Failed to connect to database.", err);
        }

        return false;
    }

    @Override
    public void setEffect(OfflinePlayer player, String slot, @Nullable InfuseEffect effect) {
        createNewPlayer(player);

        // Making sure slot is "1" or "2"
        if (!slot.equals("1") && !slot.equals("2")) {
            Infuse.LOGGER.warn("Slot '{}' is not a valid slot.  Please use \"1\" or \"2\"", slot);
            return;
        }

        // Constructing sql based on specified slot
        final String setEffectSQL = "UPDATE player_data SET slot_1 = ? WHERE player = ?;";

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(setEffectSQL);
            stmt.setObject(1, effect == null ? null : effect.serialize());
            stmt.setObject(2, player.getUniqueId());

            stmt.executeUpdate();
        } catch (SQLException err) {
            Infuse.LOGGER.error("Could not open connection to H2 database", err);
        }
    }

    @Nullable
    @Override
    public InfuseEffect getEffect(OfflinePlayer player, String slot) {
        // Making sure slot is "1" or "2"
        if (!slot.equals("1") && !slot.equals("2")) {
            Infuse.LOGGER.warn("Slot '{}' is not a valid slot.  Please use \"1\" or \"2\"", slot);
            return null;
        }

        // Constructing sql based on specified slot
        final String getEffectSQL = "SELECT (slot_" + slot + ") FROM player_data WHERE player = ?;";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(getEffectSQL)) {
                stmt.setObject(1, player.getUniqueId());

                ResultSet results = stmt.executeQuery();

                // Triggers when no effect is equipped or the effect is null
                if (!results.next() || results.wasNull()) {
                    return null;
                }

                return InfuseEffect.deserialize(results.getInt(1));
            } catch (SQLException e) {
                Infuse.LOGGER.error("Failed to read effects from the database", e);
            }
        } catch (SQLException err) {
            Infuse.LOGGER.error("Could not open connection to H2 database", err);
        }

        return null;
    }

    @Override
    public void setControlMode(OfflinePlayer player, String controlMode) {
        createNewPlayer(player);

        boolean offhandControls;
        if (controlMode.equals("offhand")) {
            offhandControls = true;
        } else if (controlMode.equals("command")) {
            offhandControls = false;
        } else {
            Infuse.LOGGER.error("Invalid control mode \"{}\".  Please use \"offhand\" or \"command\"", controlMode);
            return;
        }

        String sql = "UPDATE player_data SET offhand_control = ? WHERE player = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, offhandControls);
            stmt.setObject(2, player.getUniqueId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            Infuse.LOGGER.error("Database error!", e);
        }
    }

    @Override
    public String getControlMode(OfflinePlayer player) {
        String sql = "SELECT offhand_control FROM player_data WHERE player = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, player.getUniqueId());

            ResultSet results = stmt.executeQuery();
            return results.getBoolean(1) ? "offhand" : "command";
        } catch (SQLException e) {
            Infuse.LOGGER.error("Database error!", e);
        }

        return "command";
    }

    @Override
    public void applyUpdates() {}
}
