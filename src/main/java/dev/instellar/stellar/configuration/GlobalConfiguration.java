package dev.instellar.stellar.configuration;

import com.mohistmc.org.spongepowered.configurate.objectmapping.meta.Comment;
import com.mojang.logging.LogUtils;
import dev.instellar.stellar.configuration.type.StorageEngine;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.NestedSetting;
import org.slf4j.Logger;

@SuppressWarnings("NotNullFieldNotInitialized")
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

    public Storage storage;
    public static class Storage extends ConfigurationPart {

        public boolean useDatabase = false;
        public String tablePrefix = "";
        public StorageEngine storageEngine = StorageEngine.MARIADB;
        public boolean loadPlayerdataFromDatabase = false;

        @NestedSetting({"credential", "address"})
        @Comment("""
        Define the address for the database connection.""")
        public String address = "localhost";

        @NestedSetting({"credential", "port"})
        @Comment("""
        The port for the database connection.
        Defaults to 3306 for MariaDB.""")
        public int port = 3306;

        @NestedSetting({"credential", "database"})
        @Comment("""
        The name of the database to store player, server, world, and other data in.
        This must be created before starting the server.""")
        public String database = "stellar";

        @NestedSetting({"credential", "username"})
        public String username = "root";

        @NestedSetting({"credential", "password"})
        public String password = "";
    }

    public Players players;
    public static class Players extends ConfigurationPart {

        public boolean useAsync = false;

    }

    public Performance performance;
    public static class Performance extends ConfigurationPart {

        public boolean asyncPlayerJoin = false;
    }
}
