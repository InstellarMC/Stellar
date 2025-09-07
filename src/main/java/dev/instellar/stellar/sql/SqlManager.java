package dev.instellar.stellar.sql;

import dev.instellar.stellar.configuration.GlobalConfiguration;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/// This manager provides the basics for an abstraction over SQL connections.
///
/// The SQL service provides a pooled connection, so getting a connection
/// from the returned [DataSource] is not expensive. Therefore, we recommended
/// not keeping connections around, and closing them soon after use as shown above.
///
/// Any [java.sql.PreparedStatement] and [java.sql.ResultSet] created should also be closed after use,
/// with object.close() or, preferably, through a try-with-resources block.
public interface SqlManager {

    /// Returns whether can be used.
    default boolean isAvailable() {
        return GlobalConfiguration.get().sql.enabled;
    }

    /// Returns a data source for the internal database.
    ///
    /// @return A data source providing connections to the internal database.
    /// @throws SQLException if a connection to the internal database could not
    ///     be established
    DataSource dataSource() throws SQLException;

    /// Returns a data source for the provided JDBC connection string or
    /// an alias.
    ///
    /// A jdbc connection url is expected to be of the form:
    /// jdbc:&lt;engine&gt;://[&lt;username&gt;[:&lt;password&gt;]@]&lt;host
    /// &gt;/&lt;database&gt; or an alias (available aliases are known only by
    /// the service provider)
    ///
    /// @param jdbcConnection The jdbc url or connection alias
    /// @return A data source providing connections to the given URL.
    /// @throws SQLException if a connection to the given database could not
    ///     be established
    DataSource dataSource(String jdbcConnection) throws SQLException;

    /// Returns a data source for the provided JDBC connection string or an
    /// alias.
    ///
    /// A jdbc connection url is expected to be of the form:
    /// jdbc:&lt;engine&gt;://[&lt;username&gt;[:&lt;password&gt;]@]
    /// &lt;host&gt;/&lt;database&gt;
    /// or an alias (available aliases are known only by the service
    /// provider)
    ///
    /// @param plugin The plugin to lookup databases relative to (primarily
    ///     applying to file-backed databases)
    /// @param jdbcConnection The jdbc url or connection alias
    /// @return A data source providing connections to the given URL.
    /// @throws SQLException if a connection to the given database could not
    ///     be established
    DataSource dataSource(Plugin plugin, String jdbcConnection) throws SQLException;

    /// Returns a possible connection URL for a given alias.
    ///
    /// @param alias The alias to check
    /// @return The connection url as a String if it exists,
    ///          or [#empty()]
    Optional<String> connectionUrlFromAlias(String alias);

}
