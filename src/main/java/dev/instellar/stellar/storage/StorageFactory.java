package dev.instellar.stellar.storage;

import com.mojang.logging.LogUtils;
import dev.instellar.stellar.configuration.GlobalConfiguration;
import dev.instellar.stellar.configuration.type.StorageEngine;
import dev.instellar.stellar.storage.implementation.StorageImplementation;
import dev.instellar.stellar.storage.implementation.sql.SqlStorage;
import dev.instellar.stellar.storage.implementation.sql.connection.hikari.MariaDbConnectionFactory;
import dev.instellar.stellar.storage.misc.StorageCredentials;
import org.slf4j.Logger;

public final class StorageFactory {

    private static final Logger LOGGER = LogUtils.getLogger();

    private StorageFactory() {
        throw new AssertionError();
    }

    public static Storage createStorage() {
        final var engine = GlobalConfiguration.get().storage.storageEngine;
        LOGGER.info("Loading storage engine: {}", engine.name());
        return new Storage(createImplementation(engine));
    }

    private static StorageImplementation createImplementation(final StorageEngine engine) {
        final var config = GlobalConfiguration.get();
        return switch (engine) {
            case MARIADB -> new SqlStorage(new MariaDbConnectionFactory(StorageCredentials.fromGlobalConfiguration(config)), config.storage.tablePrefix);
            default -> throw new IllegalArgumentException("Unsupported storage engine: " + engine);
        };
    }

}
