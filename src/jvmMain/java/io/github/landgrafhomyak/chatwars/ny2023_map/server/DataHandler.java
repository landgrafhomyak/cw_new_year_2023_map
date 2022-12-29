package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.Database;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Обработчик запросов к API методу получения карты.
 *
 * @see SerializedSynchronizedCompressedCachedDatabase
 */
final class DataHandler implements HttpHandler {
    /**
     * Путь к методу.
     */
    public final String path;

    /**
     * База данных.
     */
    private final SerializedSynchronizedCompressedCachedDatabase db;

    DataHandler(String path, SerializedSynchronizedCompressedCachedDatabase db) {
        this.path = path;
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!Objects.equals(exchange.getRequestURI().getPath(), this.path)) {
            exchange.getResponseHeaders().add("Location", this.path);
            exchange.sendResponseHeaders(301, 0);
            return;
        }
        final Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/octet-stream");
        final Database.Rect rect = this.db.getBorders();
        headers.add("North", Integer.toString(rect.maxY));
        headers.add("East", Integer.toString(rect.maxX));
        headers.add("South", Integer.toString(rect.minY));
        headers.add("West", Integer.toString(rect.minX));
        synchronized (this.db) {
            exchange.sendResponseHeaders(200, this.db.serializedSize);
            try (final OutputStream os = exchange.getResponseBody()) {
                os.write(this.db.serialized, 0, this.db.serializedSize);
            }
        }
        exchange.close();
    }
}
