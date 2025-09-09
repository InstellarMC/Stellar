package dev.instellar.stellar.world.storage;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.Optional;

public record StellarPlayerDataStorage(PlayerDataStorage vanilla) {

    private static final Logger LOGGER = LogUtils.getLogger();

    public void save(final ServerPlayer player) {
        // try (
        //     final var connection = Bukkit.getServer().sqlManager().dataSource().getConnection();
        //     final var pdOverrideStatement = connection.prepareStatement("""
        //             UPDATE player_data
        //                SET pd_
        //             """.trim())
        // ) {
        //     connection.setAutoCommit(false);
        // }
    }

    public Optional<CompoundTag> load(final ServerPlayer player) {
        try (
            final var connection = Bukkit.getServer().sqlManager().dataSource().getConnection();
            final var s = connection.prepareStatement("""
                    SELECT pl_id,
                      FROM player
                      JOIN player_revision ON pl_latest = pl_rev_id
                 LEFT JOIN player_server_data ON pl_id = psd_player
                 LEFT JOIN player_vanilla_data ON pl_id = pvd_id
                     WHERE pl_mc_uuid = UNHEX(REPLACE(?, '-', ''))
                 """.trim())
        ) {
            // TODO
        } catch (final SQLException e) {
            LOGGER.error("Failed to load player data for {}", player.getGameProfile().getName(), e);
        }
        return Optional.empty();
    }
}
