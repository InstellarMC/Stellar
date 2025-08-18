package dev.instellar.stellar.storage.implementation.sql.connection.hikari;

import com.zaxxer.hikari.HikariConfig;
import dev.instellar.stellar.storage.misc.StorageCredentials;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Extension of {@link HikariConnectionFactory} that uses the driver class name to configure Hikari.
 */
public abstract class DriverBasedHikariConnectionFactory extends HikariConnectionFactory {

    protected DriverBasedHikariConnectionFactory(final StorageCredentials credentials) {
        super(credentials);
    }

    protected abstract String driverClassName();

    protected abstract String driverJdbcIdentifier();

    @Override
    protected void configureDatabase(final HikariConfig config, final String address, final String port, final String databaseName, final String username, final String password) {
        config.setDriverClassName(driverClassName());
        config.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s", driverJdbcIdentifier(), address, port, databaseName));
        config.setUsername(username);
        config.setPassword(password);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();

        // Calling Class.forName("<driver class name>") is enough to call the static initializer
        // which makes our driver available in DriverManager. We don't want that, so unregister it after
        // the pool has been setup.
        deregisterDriver(driverClassName());
    }

    private static void deregisterDriver(final String driverClassName) {
        final var drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            final var driver = drivers.nextElement();
            if (driver.getClass().getName().equals(driverClassName)) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (final SQLException ignored) {
                }
            }
        }
    }
}
