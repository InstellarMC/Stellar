package dev.instellar.stellar.storage.implementation.sql;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface StatementProcessor {

    StatementProcessor USE_BACKTICKS = s -> s.replace('\'', '`');

    StatementProcessor USE_DOUBLE_QUOTES = s -> s.replace('\'', '"');

    String process(String statement);

    default StatementProcessor compose(StatementProcessor before) {
        requireNonNull(before);
        return s -> process(before.process(s));
    }
}
