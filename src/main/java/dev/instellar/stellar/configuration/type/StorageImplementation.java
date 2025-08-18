package dev.instellar.stellar.configuration.type;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public abstract class StorageImplementation {

    protected static final Logger LOGGER = LogUtils.getLogger();

    public abstract String getImplementationName();

    public void init() throws Exception {
    }

    public void shutdown() {
    }
}