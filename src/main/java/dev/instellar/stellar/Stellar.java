package dev.instellar.stellar;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import dev.instellar.stellar.storage.Storage;
import dev.instellar.stellar.storage.StorageFactory;
import lombok.Getter;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Stellar {

    public static Stellar get() {
        if (StellarHolder.INSTANCE == null) {
            throw new IllegalStateException("Stellar has not been initialized yet. The server has not started or already stopped.");
        }

        return StellarHolder.INSTANCE;
    }

    Stellar() {
        throw new AssertionError();
    }

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ExecutorService BACKEND_EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() / 2,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat(Constants.FlagProperties.ExecutorNameFormat)
                    .setThreadFactory(SidedThreadGroups.SERVER)
                    .setUncaughtExceptionHandler(new net.minecraft.DefaultUncaughtExceptionHandler(LOGGER))
                    .build()
    );

    @Getter
    private final Storage storage = StorageFactory.createStorage();

}
