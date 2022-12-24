package io.github.landgrafhomyak.chatwars.ny2023_map;

import io.github.landgrafhomyak.chatwars.ny2023_map.db.DatabaseException;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.SqliteDatabase;
import org.sqlite.jdbc4.JDBC4Connection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public final class Main {
    public static void main(String[] argv) throws SQLException, IOException, DatabaseException {
        final String dbpath = System.getProperty("sqlite");
        final SiteRunner server = new SiteRunner(
                new SqliteDatabase(new JDBC4Connection(String.format("file:%s", dbpath), dbpath, new Properties()))
        );
        final Thread botThread = new Thread(
                new BotRunner(
                        System.getProperty("token"),
                        server
                )
        );
        botThread.setDaemon(false);
        botThread.start();
    }

}
