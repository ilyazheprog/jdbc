package org.example;

import lombok.Getter;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
public class DataSourceManager {
    private static DataSourceManager instance;

    private final PGSimpleDataSource dataSource;

    private DataSourceManager() throws IOException {
        Properties props = new Properties();
        InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("appdata/application.properties");
        props.load(input);

        String databaseHost = props.getProperty("database.host");
        String databaseUser = props.getProperty("database.user");
        int port = Integer.parseInt(props.getProperty("database.port"));
        String databaseName = props.getProperty("database.name");
        String databaseSchema = props.getProperty("database.schema");

        dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName(databaseName);
        dataSource.setCurrentSchema(databaseSchema);
        dataSource.setServerNames(new String[]{databaseHost});
        dataSource.setPortNumbers(new int[]{port});
        dataSource.setUser(databaseUser);
        dataSource.setPassword(System.getenv("DB_PASS"));
    }

    public static DataSourceManager getInstance() throws IOException {
        if (instance == null) {
            instance = new DataSourceManager();
        }

        return instance;
    }
}
