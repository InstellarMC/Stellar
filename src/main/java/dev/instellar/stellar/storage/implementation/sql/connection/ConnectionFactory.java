package dev.instellar.stellar.storage.implementation.sql.connection;

import com.mojang.logging.LogUtils;
import dev.instellar.stellar.storage.implementation.sql.StatementProcessor;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {

    static final Logger LOGGER = LogUtils.getLogger();

    String getImplementationName();

    void init();

    void shutdown() throws Exception;

    StatementProcessor getStatementProcessor();

    Connection getConnection() throws SQLException;

}
