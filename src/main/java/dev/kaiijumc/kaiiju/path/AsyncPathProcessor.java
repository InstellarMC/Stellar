package dev.kaiijumc.kaiiju.path;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Path;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@DefaultQualifier(NotNull.class)
public class AsyncPathProcessor {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static @Nullable ThreadPoolExecutor EXECUTOR = generateExecutor();

    private static @Nullable ThreadPoolExecutor generateExecutor() {
        if (EXECUTOR != null) EXECUTOR.shutdownNow();
        if (!dev.instellar.stellar.configuration.GlobalConfiguration.get().entities.asyncPathProcess.enabled) return null;
        LOGGER.info("Using async pathfinding with a maximum of {} threads", dev.instellar.stellar.configuration.GlobalConfiguration.get().entities.asyncPathProcess.maxThreadSize());
        return new ThreadPoolExecutor(
                1,
                dev.instellar.stellar.configuration.GlobalConfiguration.get().entities.asyncPathProcess.maxThreadSize(),
                dev.instellar.stellar.configuration.GlobalConfiguration.get().entities.asyncPathProcess.keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("async-pathfinding-thread-%d").setPriority(Thread.NORM_PRIORITY - 2).build()
        );
    }

    public static void updateExecutor() {
        EXECUTOR = generateExecutor();
    }

    protected static CompletableFuture<Void> queue(AsyncPath path) {
        return CompletableFuture.runAsync(path::process, EXECUTOR);
    }

    public static void awaitProcess(Entity entity, @Nullable Path path, Consumer<@Nullable Path> after) {
        if (path == null || path.isProcessed() || !(path instanceof AsyncPath async)) {
            after.accept(path);
            return;
        }

        async.postProcessing(() -> entity.getBukkitEntity().taskScheduler.schedule(ignored -> after.accept(path), null, 1));
    }

}