package dev.instellar.stellar.storage.misc;

import dev.instellar.stellar.configuration.GlobalConfiguration;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record StorageCredentials(String address, int port, String database, String username, String password,
                                 int maxPoolSize, int minIdleConnections, int maxLifetime, int keepAliveTime,
                                 int connectionTimeout, Map<String, String> properties) {

    public StorageCredentials(
            final String address,
            final int port,
            final String database,
            final String username,
            final String password
    ) {
        this(address, port, database, username, password, 10, 10, 1800000, 0, 5000, Map.of());
    }

    public static StorageCredentials fromGlobalConfiguration(final GlobalConfiguration configuration) {
        final var storage = configuration.storage;
        return new StorageCredentials(
                storage.address,
                storage.port,
                storage.database,
                storage.username,
                storage.password
        );
    }

}
