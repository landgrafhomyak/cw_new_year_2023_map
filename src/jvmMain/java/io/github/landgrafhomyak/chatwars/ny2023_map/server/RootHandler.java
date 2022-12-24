package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

final class RootHandler implements HttpHandler {
    private final byte[] content;
    private final String contentType;

    RootHandler(final byte[] content, final String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!Objects.equals(exchange.getRequestURI().getPath(), "/")) {
            exchange.sendResponseHeaders(404, 0);
            return;
        }
        exchange.getResponseHeaders().add("Content-Type", this.contentType);
        exchange.sendResponseHeaders(200, this.content.length);
        try (final OutputStream os = exchange.getResponseBody()) {
            os.write(this.content);
        }
    }
    public static String path() {
        return "/";
    }
}
