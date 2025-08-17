package dev.instellar.stellar;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Stellar {

    private Stellar() {
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

}
