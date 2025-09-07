package dev.instellar.stellar.sql;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.instellar.stellar.configuration.GlobalConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// Implementation of a SQL-using service.
///
/// This implementation does a few interesting things
/// - It's thread-safe
/// - It allows applying additional driver-specific connection
/// properties -- this allows us to do some light performance tuning in
/// cases where we don't want to be as conservative as the driver developers
/// - Caches DataSources. This cache is currently never cleared of stale entries
/// -- if some plugin makes database connections to a ton of different databases
/// we may want to implement this, but it is kinda unimportant.
@NullMarked
public final class StellarSqlManager implements SqlManager, Closeable {

    private static final Logger LOGGER = LogUtils.getLogger();
    static final Map<String, Properties> PROTOCOL_SPECIFIC_PROPS;
    static final Map<String, BiFunction<Plugin, String, String>> PATH_CANONICALIZERS;

    static {
        final ImmutableMap.Builder<String, Properties> builder = ImmutableMap.builder();
        final Properties mySqlProps = new Properties();
        // Config options based on http://assets.en.oreilly.com/1/event/21/Connector_J%20Performance%20Gems%20Presentation.pdf
        mySqlProps.setProperty("useConfigs", "maxPerformance");
        builder.put("com.mysql.jdbc.Driver", mySqlProps);
        builder.put("org.mariadb.jdbc.Driver", mySqlProps);

        PROTOCOL_SPECIFIC_PROPS = builder.build();
        PATH_CANONICALIZERS = ImmutableMap.of("h2", (plugin, orig) -> {
            final org.h2.engine.ConnectionInfo h2Info = new org.h2.engine.ConnectionInfo(orig);
            if (!h2Info.isPersistent() || h2Info.isRemote()) {
                return orig;
            }
            if (orig.startsWith("file:")) {
                orig = orig.substring("file:".length());
            }
            final Path origPath = Paths.get(orig);
            if (origPath.isAbsolute()) {
                return origPath.toString();
            }

            return plugin.getDataFolder().toPath().resolve(origPath).toAbsolutePath().toString();
        });
    }

    private @Nullable LoadingCache<ConnectionInfo, HikariDataSource> connectionCache;

    public StellarSqlManager() {
        this.buildConnectionCache();
    }

    public void buildConnectionCache() {
        this.connectionCache = null;
        this.connectionCache = Caffeine.newBuilder()
                .removalListener((RemovalListener<ConnectionInfo, HikariDataSource>) ((key, value, cause) -> {
                    if (value != null) {
                        value.close();
                    }
                }))
                .build((key) -> {
                    final HikariConfig config = new HikariConfig();
                    config.setUsername(key.user());
                    config.setPassword(key.password());
                    config.setDriverClassName(key.driverClassName());
                    // https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing for info on pool sizing
                    config.setMaximumPoolSize((Runtime.getRuntime().availableProcessors() * 2) + 1);
                    config.setLeakDetectionThreshold(60 * 1000);
                    final Properties driverSpecificProperties = StellarSqlManager.PROTOCOL_SPECIFIC_PROPS.get(key.driverClassName());
                    if (driverSpecificProperties != null) {
                        config.setDataSourceProperties(driverSpecificProperties);
                    }
                    config.setJdbcUrl(key.authlessUrl());
                    return new HikariDataSource(config);
                });
    }

    @Override
    public DataSource dataSource() throws SQLException {
        Preconditions.checkState(GlobalConfiguration.get().sql.enabled, "SQL State");
        Preconditions.checkNotNull(GlobalConfiguration.get().sql.defaultJdbcConnectionUri, "Connection uri");

        return this.dataSource(null, GlobalConfiguration.get().sql.defaultJdbcConnectionUri);
    }

    @Override
    public DataSource dataSource(final String jdbcConnection) throws SQLException {
        return this.dataSource(null, jdbcConnection);
    }

    @Override
    public DataSource dataSource(final @Nullable Plugin plugin, final String jdbcConnection) throws SQLException {
        Preconditions.checkNotNull(this.connectionCache, "Connection cache");

        final String jdbcConnectionString = this.connectionUrlFromAlias(jdbcConnection).orElse(jdbcConnection);
        final ConnectionInfo info = ConnectionInfo.fromUrl(plugin, jdbcConnectionString);
        try {
            return this.connectionCache.get(info);
        } catch (final CompletionException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.connectionCache != null) {
            this.connectionCache.invalidateAll();
        }
    }

    /// Create a new ConnectionInfo with the give parameters
    ///
    /// @param user            The username to use when connecting to th database
    /// @param password        The password to connect with. If user is not null, password must not be null
    /// @param driverClassName The class name of the driver to use for this connection
    /// @param authlessUrl     A JDBC url for this driver not containing authentication information
    /// @param fullUrl         The full jdbc url containing user, password, and database info
    public record ConnectionInfo(
        @Nullable String user,
        @Nullable String password,
        String driverClassName,
        String authlessUrl,
        String fullUrl
    ) {

        private static final Pattern URL_REGEX = Pattern.compile("(?:jdbc:)?([^:]+):(//)?(?:([^:]+)(?::([^@]+))?@)?(.*)");
        private static final String UTF_8 = StandardCharsets.UTF_8.name();

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final ConnectionInfo that = (ConnectionInfo) o;
            return Objects.equal(this.user, that.user)
                    && Objects.equal(this.password, that.password)
                    && Objects.equal(this.driverClassName, that.driverClassName)
                    && Objects.equal(this.authlessUrl, that.authlessUrl)
                    && Objects.equal(this.fullUrl, that.fullUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.user, this.password, this.driverClassName, this.authlessUrl, this.fullUrl);
        }

        /// Extracts the connection info from a JDBC url with additional authentication information as specified in [SqlManager].
        ///
        /// @param container The plugin to put a path relative to
        /// @param fullUrl   The full JDBC URL as specified in SqlService
        /// @return A constructed ConnectionInfo object using the info from the provided URL
        /// @throws SQLException If the driver for the given URL is not present
        public static ConnectionInfo fromUrl(final @Nullable Plugin container, final String fullUrl) throws SQLException {
            final Matcher match = ConnectionInfo.URL_REGEX.matcher(fullUrl);
            if (!match.matches()) {
                throw new IllegalArgumentException("URL " + fullUrl + " is not a valid JDBC URL");
            }

            final String protocol = match.group(1);
            final boolean hasSlashes = match.group(2) != null;
            final String user = ConnectionInfo.urlDecode(match.group(3));
            final String pass = ConnectionInfo.urlDecode(match.group(4));
            String serverDatabaseSpecifier = match.group(5);
            final BiFunction<Plugin, String, String> derelativizer = StellarSqlManager.PATH_CANONICALIZERS.get(protocol);
            if (container != null && derelativizer != null) {
                serverDatabaseSpecifier = derelativizer.apply(container, serverDatabaseSpecifier);
            }
            final String unauthedUrl = "jdbc:" + protocol + (hasSlashes ? "://" : ":") + serverDatabaseSpecifier;
            final String driverClass = DriverManager.getDriver(unauthedUrl).getClass().getCanonicalName();
            return new ConnectionInfo(user, pass, driverClass, unauthedUrl, fullUrl);
        }

        private static @Nullable String urlDecode(final @Nullable String str) {
            try {
                return str == null ? null : URLDecoder.decode(str, ConnectionInfo.UTF_8);
            } catch (final UnsupportedEncodingException e) {
                // If UTF-8 is not supported, we have bigger problems...
                throw new RuntimeException("UTF-8 is not supported on this system", e);
            }
        }
    }

    @Override
    public Optional<String> connectionUrlFromAlias(final String alias) {
        return Optional.ofNullable(GlobalConfiguration.get().sql.aliases.get(alias));
    }

}
