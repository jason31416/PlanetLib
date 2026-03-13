package cn.jason31416.planetlib.data;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;

public class HikariSQLInstance implements SQLInstance {
    @Getter
    private final HikariDataSource dataSource;
    private final SqlDialect dialect;
    private final Executor asyncExecutor;

    public HikariSQLInstance(HikariDataSource dataSource, SqlDialect dialect) {
        this(dataSource, dialect, null);
    }

    public HikariSQLInstance(HikariDataSource dataSource, SqlDialect dialect, Executor asyncExecutor) {
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get SQL connection", e);
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }

    @Override
    public Executor getAsyncExecutor() {
        if (asyncExecutor != null) {
            return asyncExecutor;
        }
        return SQLInstance.super.getAsyncExecutor();
    }

    @Override
    public SqlDialect getDialect() {
        return dialect;
    }

}
