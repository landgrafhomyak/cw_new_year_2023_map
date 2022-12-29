package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Обработчик HTTP запросов к статическим ресурсам с одинаковым MIME-type.
 */
final class AssetsHandler implements HttpHandler {
    /**
     * MIME-type содержимого {@link #contents}.
     */
    private final String contentType;
    /**
     * Статические ресурсы в формате {@code path -> content}.
     */
    private final Map<String, byte[]> contents;

    AssetsHandler(final String contentType, Map<String, byte[]> contents) {
        this.contentType = contentType;
        this.contents = contents;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final byte[] content = this.contents.get(exchange.getRequestURI().getPath());
        if (content == null) {
            exchange.sendResponseHeaders(404, 0);
            return;
        }
        exchange.getResponseHeaders().add("Content-Type", this.contentType);
        exchange.sendResponseHeaders(200, content.length);
        try (final OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }
}
