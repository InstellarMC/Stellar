package dev.instellar.stellar.storage.misc;

import dev.instellar.stellar.configuration.GlobalConfiguration;
import lombok.Getter;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class StorageCredentials {

    private final String address;
    @Getter
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    @Getter
    private final int maxPoolSize;
    @Getter
    private final int minIdleConnections;
    @Getter
    private final int maxLifetime;
    @Getter
    private final int keepAliveTime;
    @Getter
    private final int connectionTimeout;
    @Getter
    private final Map<String, String> properties;

    public StorageCredentials(
            final String address,
            final int port,
            final String database,
            final String username,
            final String password,
            final int maxPoolSize,
            final int minIdleConnections,
            final int maxLifetime,
            final int keepAliveTime,
            final int connectionTimeout,
            final Map<String, String> properties
    ) {
        this.address = address;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.minIdleConnections = minIdleConnections;
        this.maxLifetime = maxLifetime;
        this.keepAliveTime = keepAliveTime;
        this.connectionTimeout = connectionTimeout;
        this.properties = properties;
    }

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

    public String getAddress() {
        return requireNonNull(this.address, "address");
    }

    public String getDatabase() {
        return requireNonNull(this.database, "database");
    }

    public String getUsername() {
        return requireNonNull(this.username, "username");
    }

    public String getPassword() {
        return requireNonNull(this.password, "password");
    }

}
