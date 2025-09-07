package dev.instellar.stellar.command.subcommands;

import com.destroystokyo.paper.util.SneakyThrow;
import dev.instellar.stellar.configuration.GlobalConfiguration;
import dev.instellar.stellar.sql.SchemaReader;
import io.papermc.paper.command.CommandUtil;
import io.papermc.paper.command.PaperSubcommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@NullMarked
public final class DbCommand implements PaperSubcommand {

    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        this.process(sender, args);
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args) {
        if (args.length == 1) {
            return CommandUtil.getListMatchingLast(sender, args, "help", "init", "lookup");
        } else if (args.length == 2) {
            if ("init".equals(args[0].toLowerCase(Locale.ROOT))) {
                final var matches = GlobalConfiguration.get().sql.aliases.keySet();
                return CommandUtil.getListMatchingLast(sender, args, matches);
            } else if ("lookup".equals(args[0].toLowerCase(Locale.ROOT))) {
                return CommandUtil.getListMatchingLast(sender, args, this.listPlayers(true));
            }
        }

        return Collections.emptyList();
    }

    private void process(final CommandSender sender, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(text("Use /stellar db help for more information on a specific command", RED));
            return;
        }

        if ("help".equals(args[0].toLowerCase(Locale.ROOT))) {
            sender.sendMessage(text("Use /stellar db init [protocol] to initialize the database (creates tables, etc.)", RED));
            return;
        }

        if ("init".equals(args[0].toLowerCase(Locale.ROOT))) {
            if (args.length < 2) {
                sender.sendMessage(text("Use /stellar db init [protocol] to specify a protocol to initialize", RED));
                return;
            }

            if (!Bukkit.getServer().sqlManager().isAvailable()) {
                sender.sendMessage(text("SQL service is not enabled in the config", RED));
                return;
            }

            // final List<String> tables;
            // try (final var connection = sqlManager.dataSource().getConnection()) {
            //     tables = listTables(connection);
            // } catch (final SQLException e) {
            //     sender.sendMessage(text("Failed to connect to the database: " + e.getMessage(), RED));
            //     SneakyThrow.sneaky(e);
            //     return;
            // }

            // this.applySchema(sender, args[1].toLowerCase(Locale.ROOT), tables);
            this.applySchema(sender, args[1].toLowerCase(Locale.ROOT), List.of() /* unused */);
            return;
        }

        if ("lookup".equals(args[0].toLowerCase(Locale.ROOT))) {
            if (args.length < 2) {
                sender.sendMessage(text("Use /stellar db lookup [player] to look up a player in the database", RED));
                return;
            }

            final var name = args[1].toLowerCase(Locale.ROOT);
            final var players = this.listPlayers(false);
            if (!players.contains(name)) {
                sender.sendMessage(text("No player with the name '%s' was found in the database".formatted(name), RED));
            }

            // TODO: Expand this to show more information about the player
            sender.sendMessage(text("Found %d player(s) with the name '%s' in the database".formatted(
                    players.stream().filter(n -> n.equals(name)).count(),
                    name
            ), GREEN));
        } else {
            sender.sendMessage(text("Unknown subcommand. Use /stellar db help for more information.", RED));
        }
    }

    private void applySchema(final CommandSender sender, final String protocol, final List<String> existingTables) {
        final var schemaFilename = "dev/instellar/stellar/schema/%s.sql".formatted(protocol);

        final List<String> statements;
        try (final var is = this.getClass().getClassLoader().getResourceAsStream(schemaFilename)) {
            if (is == null) {
                sender.sendMessage(text("Couldn't locate schema file for protocol '%s'".formatted(protocol), RED));
                return;
            }

            statements = SchemaReader.getStatements(is).stream().toList();
        } catch (final Exception e) {
            sender.sendMessage(text("Failed to read schema for protocol '%s': %s".formatted(protocol, e.getMessage()), RED));
            SneakyThrow.sneaky(e);
            return;
        }

        if (statements.isEmpty()) {
            sender.sendMessage(text("Database is already up to date for protocol '%s'".formatted(protocol), GREEN));
            return;
        }

        try (final var connection = Bukkit.getServer().sqlManager().dataSource().getConnection()) {
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
                        SneakyThrow.sneaky(e);
                    }
                }
            }

            // try again
            if (utf8mb4Unsupported) {
                try (final var s = connection.createStatement()) {
                    for (String query : statements) {
                        s.addBatch(query.replace("utf8mb4", "utf8"));
                    }

                    s.executeBatch();
                }
            }

            sender.sendMessage(text("Applied %d statements for protocol '%s'".formatted(statements.size(), protocol), GREEN));
        } catch (final Exception e) {
            sender.sendMessage(text("Failed to apply schema for protocol '%s': %s".formatted(protocol, e.getMessage()), RED));
            SneakyThrow.sneaky(e);
        }
    }

    private @Nullable List<String> listPlayers;
    private long lastPlayerListUpdate = 0L;
    private static final long PLAYER_LIST_CACHE_DURATION = 60 * 1000L; // 1 minute
    private List<String> listPlayers(final boolean fromCache) {
        if (GlobalConfiguration.get().sql.enabled) {
            if (fromCache) {
                final var now = System.currentTimeMillis();
                if (this.listPlayers != null && (now - this.lastPlayerListUpdate) < PLAYER_LIST_CACHE_DURATION) {
                    return this.listPlayers;
                }

                this.lastPlayerListUpdate = now;
            }

            try (final var connection = Bukkit.getServer().sqlManager().dataSource().getConnection()) {
                final List<String> names = new ArrayList<>();

                try (final var ps = connection.prepareStatement("SELECT pl_mc_name, pl_nickname FROM player")) {
                    try (final var rs = ps.executeQuery()) {
                        while (rs.next()) {
                            names.add(rs.getString(1).toLowerCase(Locale.ROOT));
                        }
                    }
                }

                this.listPlayers = Collections.unmodifiableList(names);
                return names;
            } catch (final SQLException e) {
                SneakyThrow.sneaky(e);
            }
        }

        return List.of();
    }

    private static List<String> listTables(final Connection connection) throws SQLException {
        final List<String> tables = new ArrayList<>();

        try (final ResultSet rs = connection.getMetaData().getTables(connection.getCatalog(), null, "%", null)) {
            while (rs.next()) {
                tables.add(rs.getString(3).toLowerCase(Locale.ROOT));
            }
        }

        return tables;
    }
}
