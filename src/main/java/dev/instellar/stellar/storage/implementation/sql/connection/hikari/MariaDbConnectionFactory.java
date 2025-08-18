package dev.instellar.stellar.storage.implementation.sql.connection.hikari;

import dev.instellar.stellar.storage.implementation.sql.StatementProcessor;
import dev.instellar.stellar.storage.misc.StorageCredentials;

import java.util.Map;

public final class MariaDbConnectionFactory extends DriverBasedHikariConnectionFactory {

    public MariaDbConnectionFactory(final StorageCredentials credentials) {
        super(credentials);
    }

    @Override
    public String getImplementationName() {
        return "MariaDB";
    }

    @Override
    protected String defaultPort() {
        return "3306";
    }

    @Override
    protected String driverClassName() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected String driverJdbcIdentifier() {
        return "mariadb";
    }

    @Override
    protected void overrideProperties(final Map<String, Object> properties) {
        // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        properties.putIfAbsent("cachePrepStmts", "true");
        properties.putIfAbsent("prepStmtCacheSize", "250");
        properties.putIfAbsent("prepStmtCacheSqlLimit", "2048");
        properties.putIfAbsent("useServerPrepStmts", "true");
        properties.putIfAbsent("useLocalSessionState", "true");
        properties.putIfAbsent("rewriteBatchedStatements", "true");
        properties.putIfAbsent("cacheResultSetMetadata", "true");
        properties.putIfAbsent("cacheServerConfiguration", "true");
        properties.putIfAbsent("elideSetAutoCommits", "true");
        properties.putIfAbsent("maintainTimeStats", "false");
        properties.putIfAbsent("alwaysSendSetIsolation", "false");
        properties.putIfAbsent("cacheCallableStmts", "true");
        properties.putIfAbsent("characterEncoding", "UTF-8");

        // https://stackoverflow.com/a/54256150
        properties.putIfAbsent("serverTimezone", "Asia/Seoul");

        super.overrideProperties(properties);
    }

    @Override
    public StatementProcessor getStatementProcessor() {
        return StatementProcessor.USE_BACKTICKS;
    }
}
