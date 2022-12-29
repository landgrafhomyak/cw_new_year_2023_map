package io.github.landgrafhomyak.chatwars.ny2023_map

import io.github.landgrafhomyak.chatwars.ny2023_map.bot.BotRunner
import io.github.landgrafhomyak.chatwars.ny2023_map.db.SqliteDatabase
import io.github.landgrafhomyak.chatwars.ny2023_map.server.SiteServer
import org.sqlite.jdbc4.JDBC4Connection
import java.util.*


public object Main {
    @JvmStatic
    private fun Runnable.runInThread(): Thread {
        val t = Thread(this)
        t.isDaemon = false
        t.start()
        return t
    }

    @Suppress("UNUSED_VARIABLE")
    @JvmStatic
    public fun main(argv: Array<String>) {
        val dbpath = System.getProperty("sqlite")

        @Suppress("UNUSED_VARIABLE")
        val server = SiteServer(
            SqliteDatabase(JDBC4Connection(String.format("file:%s", dbpath), dbpath, Properties()))
        )
        val syncThread = SyncWithOpponentsRunner(server.database()).runInThread()

        val botThread = BotRunner(System.getProperty("token"), server.database()).runInThread()
    }
}