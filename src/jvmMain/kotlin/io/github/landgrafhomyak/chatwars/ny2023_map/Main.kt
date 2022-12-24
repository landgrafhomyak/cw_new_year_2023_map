package io.github.landgrafhomyak.chatwars.ny2023_map

import io.github.landgrafhomyak.chatwars.ny2023_map.db.SqliteDatabase
import io.github.landgrafhomyak.chatwars.ny2023_map.server.SiteServer
import org.sqlite.jdbc4.JDBC4Connection
import java.util.*

public object Main {
    @JvmStatic
    public fun main(argv: Array<String>) {
        val dbpath = System.getProperty("sqlite")
        @Suppress("UNUSED_VARIABLE")
        val server = SiteServer(
            SqliteDatabase(JDBC4Connection(String.format("file:%s", dbpath), dbpath, Properties()))
        )
        //        final Thread botThread = new Thread(
//                new BotRunner(
//                        System.getProperty("token"),
//                        server
//                )
//        );
//        botThread.setDaemon(false);
//        botThread.start();
    }
}