package com.yourcompany.cafeteria.util;

import org.flywaydb.core.Flyway;
import javax.sql.DataSource;
import java.sql.Connection;

public class TestDatabase {
    public static Connection open() throws Exception {
        DataSourceProvider.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        DataSource ds = DataSourceProvider.getDataSource();
        Flyway flyway = Flyway.configure().dataSource(ds).locations("classpath:db/migration").cleanDisabled(false).load();
        flyway.clean();
        flyway.migrate();
        return ds.getConnection();
    }
}
