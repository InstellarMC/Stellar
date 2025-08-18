package dev.instellar.stellar.configuration;

import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.ConfigurationPart;
import org.slf4j.Logger;

public final class GlobalConfiguration extends ConfigurationPart {

    private static final Logger LOGGER = LogUtils.getLogger();
    static final int CURRENT_VERSION = 1;
    private static GlobalConfiguration INSTANCE;
    public static boolean isFirstStart = false;
    public static GlobalConfiguration get() {
        return INSTANCE;
    }
    static void set(final GlobalConfiguration configuration) {
        INSTANCE = configuration;
    }

    public Players players;

    public class Players extends ConfigurationPart {

        public boolean useAsync = false;

    }

    public Performance performance;
    public static class Performance extends ConfigurationPart {

        public boolean asyncPlayerJoin = false;
    }
}
