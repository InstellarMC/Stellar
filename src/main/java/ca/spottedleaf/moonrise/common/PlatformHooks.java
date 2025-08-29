package ca.spottedleaf.moonrise.common;

import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.NewChunkHolder;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

public interface PlatformHooks {

    public static PlatformHooks get() {
        return Holder.INSTANCE;
    }

    public String getBrand();

    public int getLightEmission(final BlockState blockState, final BlockGetter world, final BlockPos pos);

    public CompoundTag convertNBT(final DSL.TypeReference type, final DataFixer dataFixer, final CompoundTag nbt,
                                  final int fromVersion, final int toVersion);

    public static final class Holder {

        private static final PlatformHooks INSTANCE;

        static {
            INSTANCE = ServiceLoader.load(PlatformHooks.class, PlatformHooks.class.getClassLoader()).findFirst()
                    .orElseThrow(() -> new RuntimeException("Failed to locate PlatformHooks"));
        }

        private Holder() {}
    }

}
