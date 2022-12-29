package io.github.landgrafhomyak.chatwars.ny2023_map

import io.github.landgrafhomyak.chatwars.ny2023_map.db.Database
import io.github.landgrafhomyak.chatwars.ny2023_map.db.NullDatabase
import io.github.landgrafhomyak.chatwars.ny2023_map.db.RectangleCacheDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Главная функция для потока синхронизации карты с конкурентами. Скачивает чужие карты раз в час.
 */
internal class SyncWithOpponentsRunner(db: Database) : Runnable {
    /**
     * Кеш для объединения чужих карт перед сохранением и предотвращения выделения новых объектов при каждом запросе.
     */
    private class Cache(private val uncached: Database) : RectangleCacheDatabase(NullDatabase.INSTANCE) {
        fun commit() {
            this.uncached.saveMap(this.cache, this.rect.minX, this.rect.minY, this.rect.width(), this.rect.height())
        }
    }

    /**
     * HTTP клиент для выполнения запросов.
     */
    private val client = HttpClient.newHttpClient()

    /**
     * Локальный кеш карты.
     * @see SyncWithOpponentsRunner.Cache
     */
    private val db = Cache(db)

    /**
     * Занимает поток, отправляя запросы к конкурентам раз в час.
     */
    override fun run() {
        while (true) {
            try {
                this.opponent1()
                this.db.commit()
                Thread.sleep(1000 * 3600)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    /**
     * Функция-обёртка для подавления ошибок и вывода их в [System.out].
     * Используется для того чтобы ошибка при запросе к одному конкуренту не отменяла запросов к остальным.
     */
    private inline fun supressErrors(block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun opponent1() = supressErrors {
        val data = this.client.send(
            HttpRequest
                .newBuilder(URI.create("https://siboil.store/api/data"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"tgQuery\":\"\"}"))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        ).body()
        val map = JSONObject(data).getJSONArray("map")
        for (cell in map) {
            val (x, y) = ((cell as JSONArray)[0] as String).split("#").map { s -> s.toInt() }
            this.db.saveMap(arrayOf(TileType.fromEmoji(cell[1] as String)!!), x, y, 1, 1)
        }
    }
}