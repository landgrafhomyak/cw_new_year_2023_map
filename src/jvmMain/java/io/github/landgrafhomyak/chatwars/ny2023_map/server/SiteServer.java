package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import com.sun.net.httpserver.HttpServer;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.Database;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.DatabaseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Map;

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

    private static final JsHandler LOADER_JS_HANDLER = new JsHandler(
            "/loader.js",
            loadResource("/loader.js"),
            loadResource("/loader.js.map")
    );
    private static final StaticHandler INDEX_CSS_HANDLER = new StaticHandler(
            "/index.css",
            loadResource("/index.css"),
            "text/css"
    );
    private static final StaticHandler INDEX_HTML_HANDLER = new StaticHandler(
            "/",
            loadResource("/index.html"),
            "text/html"
    );
    private static final AssetsHandler ICONS_HANDLER = new AssetsHandler("image/svg+xml",
            Map.of(
                    "/icons/fields.svg", loadResource("/icons/fields.svg"),
                    "/icons/forest.svg", loadResource("/icons/forest.svg"),
                    "/icons/valley.svg", loadResource("/icons/valley.svg"),
                    "/icons/shop.svg", loadResource("/icons/shop.svg"),
                    "/icons/well.svg", loadResource("/icons/well.svg"),
                    "/icons/tavern.svg", loadResource("/icons/tavern.svg"),
                    "/icons/zero.svg", loadResource("/icons/zero.svg")
            ));

    @SuppressWarnings("FieldCanBeLocal")
    private final HttpServer server;
    private final SerializedSynchronizedCompressedCachedDatabase db;

    public SiteServer(final Database db) throws IOException, DatabaseException {
        this.server = HttpServer.create(new InetSocketAddress(80), 0);
        this.server.createContext(LOADER_JS_HANDLER.path, LOADER_JS_HANDLER);
        this.server.createContext(INDEX_CSS_HANDLER.path, INDEX_CSS_HANDLER);
        this.server.createContext("/icons/", ICONS_HANDLER);
        final String apiPath = "/data";
        this.db = new SerializedSynchronizedCompressedCachedDatabase(db);
        this.server.createContext(apiPath, new DataHandler(apiPath, this.db));
        this.server.start();
        this.server.createContext(INDEX_HTML_HANDLER.path, INDEX_HTML_HANDLER);
    }

    /**
     * База данных с синхронизацией и кешированием, через которую должна сохранятся карта для отображения на сайте.
     */
    public Database database() {
        return this.db;
    }
}
