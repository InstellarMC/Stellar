package org.dreeam.leaf.async;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class ShutdownExecutors {

    public static final Logger LOGGER = LogManager.getLogger("Leaf");

    public static void shutdown(MinecraftServer server) {
        if (AsyncPlayerDataSaving.IO_POOL != null) {
            LOGGER.info("Waiting for player I/O executor to shutdown...");
            AsyncPlayerDataSaving.IO_POOL.shutdown();
            try {
                AsyncPlayerDataSaving.IO_POOL.awaitTermination(60L, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
