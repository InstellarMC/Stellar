package dev.instellar.stellar.storage.implementation.sql.connection.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.instellar.stellar.storage.implementation.sql.connection.ConnectionFactory;
import dev.instellar.stellar.storage.misc.StorageCredentials;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract {@link ConnectionFactory} using a {@link HikariDataSource}.
 */
public abstract class HikariConnectionFactory implements ConnectionFactory {

    private final StorageCredentials credentials;
    private HikariDataSource hikari;

    public HikariConnectionFactory(final StorageCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Gets the default port used by the database
     *
     * @return the default port
     */
    protected abstract String defaultPort();

    /**
     * Configures the {@link HikariConfig} with the relevant database properties.
     *
     * <p>Each driver does this slightly differently...</p>
     *
     * @param config the hikari config
     * @param address the database address
     * @param port the database port
     * @param databaseName the database name
     * @param username the database username
     * @param password the database password
     */
    protected abstract void configureDatabase(final HikariConfig config, final String address, final String port, final String databaseName, final String username, final String password);

    /**
     * Allows the connection factory instance to override certain properties before they are set.
     *
     * @param properties the current properties
     */
    protected void overrideProperties(final Map<String, Object> properties) {
        // https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery
        properties.putIfAbsent("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));
    }

    /**
     * Sets the given connection properties onto the config.
     *
     * @param config the hikari config
     * @param properties the properties
     */
    protected void setProperties(final HikariConfig config, final Map<String, Object> properties) {
        for (final var property : properties.entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }
    }

    /**
     * Called after the Hikari pool has been initialised
     */
    protected void postInitialize() {
    }

    @Override
    public void init() {
        HikariConfig config;
        try {
            config = new HikariConfig();
        } catch (LinkageError e) {
            LOGGER.error("HikariCP is not available on the classpath. Please add it to your dependencies.", e);
            throw new RuntimeException("HikariCP is not available on the classpath. Please add it to your dependencies.", e);
        }

        // set pool name so the logging output can be linked back to us
        config.setPoolName("luckperms-hikari");

        // get the database info/credentials from the config file
        final var addressSplit = this.credentials.getAddress().split(":");
        final var address = addressSplit[0];
        final var port = addressSplit.length > 1 ? addressSplit[1] : defaultPort();

        // allow the implementation to configure the HikariConfig appropriately with these values
        try {
            configureDatabase(config, address, port, this.credentials.getDatabase(), this.credentials.getUsername(), this.credentials.getPassword());
        } catch (NoSuchMethodError e) {
            LOGGER.error("Seems HikariCP is not compatible with this version of Minecraft server.", e);
            throw new RuntimeException("HikariCP is not compatible with this version of Minecraft server. Please update HikariCP or use a different connection factory.", e);
        }

        // get the extra connection properties from the config
        final Map<String, Object> properties = new HashMap<>(this.credentials.getProperties());

        // allow the implementation to override/make changes to these properties
        overrideProperties(properties);

        // set the properties
        setProperties(config, properties);

        // configure the connection pool
        config.setMaximumPoolSize(this.credentials.getMaxPoolSize());
        config.setMinimumIdle(this.credentials.getMinIdleConnections());
        config.setMaxLifetime(this.credentials.getMaxLifetime());
        config.setKeepaliveTime(this.credentials.getKeepAliveTime());
        config.setConnectionTimeout(this.credentials.getConnectionTimeout());

        // don't perform any initial connection validation - we subsequently call #getConnection
        // to setup the schema anyways
        config.setInitializationFailTimeout(-1);

        this.hikari = new HikariDataSource(config);

        postInitialize();
    }

    @Override
    public void shutdown() {
        if (this.hikari != null) {
            this.hikari.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.hikari == null) {
            throw new SQLException("Unable to get a connection from the pool. (hikari is null)");
        }

        Connection connection = this.hikari.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool. (connection is null)");
        }

        return connection;
    }
}
