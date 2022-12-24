package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

final class JsHandler implements HttpHandler {
    public final String path;
    private final String mapPath;
    private final byte[] script;
    private final byte[] map;

    JsHandler(final String path, final byte[] script, final byte[] map) {
        this.path = path;
        this.script = script;
        this.map = map;
        this.mapPath = String.format("%s.map", path);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final byte[] content;
        final String contentType;
        if (Objects.equals(exchange.getRequestURI().getPath(), this.path)) {
            content = this.script;
            contentType = "application/javascript";
        } else if (Objects.equals(exchange.getRequestURI().getPath(), this.mapPath)) {
            content = this.map;
            contentType = "application/json";
        } else {
            exchange.sendResponseHeaders(404, 0);
            return;
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, content.length);
        try (final OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }
}
