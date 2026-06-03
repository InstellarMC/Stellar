package dev.instellar.stellar.world.storage;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.slf4j.Logger;

import java.util.Optional;

public record StellarPlayerDataStorage(PlayerDataStorage vanilla) {

    private static final Logger LOGGER = LogUtils.getLogger();

    public void save(final ServerPlayer player) {
    }

    public Optional<CompoundTag> load(final ServerPlayer player) {
        return Optional.empty();
    }
}
