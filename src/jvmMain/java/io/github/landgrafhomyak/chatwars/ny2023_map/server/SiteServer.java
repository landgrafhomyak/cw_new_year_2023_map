package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.Database;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.DatabaseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * HTTP сервер для отображения карты.
 */
public final class SiteServer {
    /**
     * Загружает ресурс локальный ресурс из исполняемого JAR архива.
     * Если ресурс не найден, завершает программу. Расчитан на инициализацию статических переменных.
     *
     * @param name Путь к ресурсу, должен начинаться с {@code '/'}.
     * @return Байтовое представление ресурса.
     */
    private static byte[] loadResource(final String name) {
        try {
            final InputStream is = SiteServer.class.getResourceAsStream(name);
            if (is == null)
                throw new FileNotFoundException(name);
            return is.readAllBytes();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private static final byte[] LOADER_JS = loadResource("/loader.js");
    private static final byte[] INDEX_CSS = loadResource("/index.css");
    private static final byte[] INDEX_HTML = loadResource("/index.html");


    private static final StaticHandler LOADER_JS_HANDLER = new StaticHandler("/loader.js", LOADER_JS, "application/javascript");
    private static final StaticHandler INDEX_CSS_HANDLER = new StaticHandler("/index.css", INDEX_CSS, "text/css");
    private static final RootHandler INDEX_HTML_HANDLER = new RootHandler(INDEX_HTML, "text/html");

    @SuppressWarnings("FieldCanBeLocal")
    private final HttpServer server;
    private final SquareCacheDatabase db;

    public SiteServer(final Database db) throws IOException, DatabaseException {
        this.server = HttpServer.create(new InetSocketAddress(80), 0);
        this.server.createContext(LOADER_JS_HANDLER.path, LOADER_JS_HANDLER);
        this.server.createContext(INDEX_CSS_HANDLER.path, INDEX_CSS_HANDLER);
        this.server.createContext(RootHandler.path(), INDEX_HTML_HANDLER);
        final String apiPath = "/data";
        this.db = new SquareCacheDatabase(db);
        this.server.createContext(apiPath, new DataHandler(apiPath, this.db));
        this.server.start();
    }

    public Database database() {
        return this.db;
    }
}
