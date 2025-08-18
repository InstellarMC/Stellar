package dev.instellar.stellar.storage;

import com.mojang.logging.LogUtils;
import dev.instellar.stellar.configuration.type.StorageImplementation;
import org.slf4j.Logger;

public record Storage(StorageImplementation impl) {

    private static final Logger LOGGER = LogUtils.getLogger();

    public String getName() {
        return this.impl.getImplementationName();
    }

    public void init() {
        try {
            this.impl.init();
        } catch (Exception e) {
            LOGGER.error("Failed to init storage implementation", e);
        }
    }

    public void shutdown() {
        try {
            this.impl.shutdown();
        } catch (Exception e) {
            LOGGER.error("Failed to shutdown storage implementation", e);
        }
    }
}
