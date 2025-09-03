package com.yourcompany.cafeteria.util;

import org.flywaydb.core.Flyway;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public class TestDatabase {
    public static Connection open() throws Exception {
        DataSourceProvider.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        DataSource ds = DataSourceProvider.getDataSource();

        // Get a connection and wipe the DB completely to avoid test interference
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("DROP ALL OBJECTS");
        }

        Flyway flyway = Flyway.configure().dataSource(ds).locations("classpath:db/migration").load();
        // No need to call clean() anymore since we are dropping all objects.
        flyway.migrate();

        return ds.getConnection();
    }
}
