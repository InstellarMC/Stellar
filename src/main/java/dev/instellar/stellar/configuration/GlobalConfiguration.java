package dev.instellar.stellar.configuration;

import com.mohistmc.org.spongepowered.configurate.objectmapping.meta.Comment;
import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.NestedSetting;
import io.papermc.paper.configuration.constraint.Constraints;
import io.papermc.paper.configuration.type.number.IntOr;
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

    public Items items;

    public class Items extends ConfigurationPart {

        @Comment("Configurates interval in ticks at which the Map item update task runs.")
        @Constraints.Min(1)
        @NestedSetting({"map", "update-interval"})
        public IntOr.Default mapUpdateInterval = IntOr.Default.USE_DEFAULT;
    }

    public Performance performance;

    public class Performance extends ConfigurationPart {

        public boolean asyncPlayerJoin = false;
    }

    public Verbosity verbosity;

    public class Verbosity extends ConfigurationPart {

        public boolean offlineMode = false;
    }
}
