package dev.instellar.stellar.world.storage;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.slf4j.Logger;

import java.util.Optional;

public final class StellarPlayerDataStorage {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final PlayerDataStorage vanilla;

    public StellarPlayerDataStorage(final PlayerDataStorage vanilla) {
        this.vanilla = vanilla;
    }

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
        // try (
        //     final var connection = Bukkit.getServer().sqlManager().dataSource().getConnection();
        //     final var s = connection.prepareStatement("""
        //         SELECT p.pl_id,
        //           FROM player p
        //             JOIN player_data pd ON p.pl_latest_data = pd.pd_id
        //        LEFT JOIN player_vanilla_data
        //     """)
        // )
        return Optional.empty();
    }
}
