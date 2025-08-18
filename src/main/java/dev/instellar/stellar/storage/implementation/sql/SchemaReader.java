package dev.instellar.stellar.storage.implementation.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SchemaReader {

    private SchemaReader() {
        throw new AssertionError();
    }

    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile("^CREATE TABLE [`\"']([^`\"']+)[`\"'].*", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_INDEX_PATTERN = Pattern.compile("^CREATE (?:UNIQUE\\s)?INDEX.* ON [`\"']([^`\"']+)[`\"'].*", Pattern.CASE_INSENSITIVE);

    /**
     * Parses a schema file to a list of SQL statements
     *
     * @param is the input stream to read from
     * @return a list of statements
     * @throws IOException if an error occurs whilst reading the file
     */
    public static List<String> getStatements(final InputStream is) throws IOException {
        final List<String> queries = new LinkedList<>();

        try (final var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("--") || line.startsWith("#")) {
                    continue;
                }

                sb.append(line);

                // check for end of declaration
                if (line.endsWith(";")) {
                    sb.deleteCharAt(sb.length() - 1);

                    String result = sb.toString().trim().replaceAll(" +", " ");
                    if (!result.isEmpty()) {
                        queries.add(result);
                    }

                    // reset
                    sb = new StringBuilder();
                }
            }
        }

        return queries;
    }

    public static String tableFromStatement(final String statement) {
        final var table = CREATE_TABLE_PATTERN.matcher(statement);
        if (table.matches()) {
            return table.group(1).toLowerCase(Locale.ROOT);
        }

        final var index = CREATE_INDEX_PATTERN.matcher(statement);
        if (index.matches()) {
            return index.group(1).toLowerCase(Locale.ROOT);
        }

        throw new IllegalArgumentException("Unknown statement type: " + statement);
    }

    /**
     * Filters which statements should be executed based on the current list of tables in the database
     *
     * @param statements the statements to filter
     * @param currentTables the current tables in the database
     * @return the filtered list of statements
     */
    public static List<String> filterStatements(final List<String> statements, final List<String> currentTables) {
        return statements.stream()
                .filter(statement -> !currentTables.contains(tableFromStatement(statement)))
                .toList();
    }
}
