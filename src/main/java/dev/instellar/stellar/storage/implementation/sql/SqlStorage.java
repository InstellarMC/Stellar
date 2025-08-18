package dev.instellar.stellar.storage.implementation.sql;

import dev.instellar.stellar.storage.implementation.StorageImplementation;
import dev.instellar.stellar.storage.implementation.sql.connection.ConnectionFactory;
import lombok.Getter;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public final class SqlStorage extends StorageImplementation {

    private static final String PLAYER_SELECT_LATEST_DATA_ID_BY_UUID = "SELECT pl_latest_data FROM {prefix}player WHERE pl_game_uuid = UNHEX(REPLACE(?, '-', ''))";

    @Getter
    private final ConnectionFactory connectionFactory;
    @Getter
    private final StatementProcessor statementProcessor;

    public SqlStorage(final ConnectionFactory connectionFactory, final String tablePrefix) {
        this.connectionFactory = connectionFactory;
        this.statementProcessor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix));
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    @Override
    public void init() throws Exception {
        this.connectionFactory.init();

        List<String> tables;
        try (Connection c = this.connectionFactory.getConnection()) {
            tables = listTables(c);
        }
        applySchema(tables);
    }

    private static List<String> listTables(final Connection connection) throws SQLException {
        final List<String> tables = new ArrayList<>();
        try (final var rs = connection.getMetaData().getTables(connection.getCatalog(), null, "%", null)) {
            while (rs.next()) {
                tables.add(rs.getString(3).toLowerCase(Locale.ROOT));
            }
        }
        return tables;
    }

    private void applySchema(final List<String> existingTables) throws IOException, SQLException {
        final var schemaFileName = "dev/instellar/stellar/schema/%s.sql".formatted(this.connectionFactory.getImplementationName().toLowerCase(Locale.ROOT));

        List<String> statements;
        try (final var in = this.getClass().getClassLoader().getResourceAsStream(schemaFileName)) {
            if (in == null) {
                throw new IOException("Couldn't locate schema file for %s".formatted(this.connectionFactory.getImplementationName()));
            }

            statements = SchemaReader.getStatements(in).stream()
                    .map(this.statementProcessor::process)
                    .toList();
        }

        statements = SchemaReader.filterStatements(statements, existingTables);
        if (statements.isEmpty()) {
            return;
        }

        try (Connection connection = this.connectionFactory.getConnection()) {
            boolean utf8mb4Unsupported = false;

            try (final var s = connection.createStatement()) {
                for (final var query : statements) {
                    s.addBatch(query);
                }

                try {
                    s.executeBatch();
                } catch (final BatchUpdateException e) {
                    if (e.getMessage().contains("Unknown character set")) {
                        utf8mb4Unsupported = true;
                    } else {
                        throw e;
                    }
                }
            }

            // try again
            if (utf8mb4Unsupported) {
                try (final var s = connection.createStatement()) {
                    for (final var query : statements) {
                        s.addBatch(query.replace("utf8mb4", "utf8"));
                    }

                    s.executeBatch();
                }
            }
        }
    }

    @Override
    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            LOGGER.error("Exception whilst disabling SQL storage", e);
        }
    }

    public Map<String, String> getPlayer(final UUID playerId) throws SQLException {
        try (final var c = this.connectionFactory.getConnection()) {
            try (final var ps = c.prepareStatement(this.statementProcessor.process(PLAYER_SELECT_LATEST_DATA_ID_BY_UUID))) {
                ps.setString(1, playerId.toString());
                try (final var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        final String latestDataId = rs.getString("pl_latest_data");
                        if (latestDataId != null) {
                            return Collections.singletonMap(playerId.toString(), latestDataId);
                        }
                    }
                    return Collections.emptyMap();
                }
            }
        } catch (final SQLException e) {
            return Map.of();
        }

    }
}
