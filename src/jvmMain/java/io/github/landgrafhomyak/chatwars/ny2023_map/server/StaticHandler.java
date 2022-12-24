package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

final class StaticHandler implements HttpHandler {
    public final String path;
    private final byte[] content;
    private final String contentType;

    StaticHandler(final String path, final byte[] content, final String contentType) {
        this.path = path;
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!Objects.equals(exchange.getRequestURI().getPath(), this.path)) {
            exchange.getResponseHeaders().add("Location", this.path);
            exchange.sendResponseHeaders(301, 0);
            return;
        }
        exchange.getResponseHeaders().add("Content-Type", this.contentType);
        exchange.sendResponseHeaders(200, this.content.length);
        try (final OutputStream os = exchange.getResponseBody()) {
            os.write(this.content);
        }
    }
}
