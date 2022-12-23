package io.github.landgrafhomyak.chatwars.ny2023_map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.lang.System.arraycopy;

final class SiteRunner implements Database {
    @SuppressWarnings("FieldCanBeLocal")
    private final HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
    private final Database uncached;
    private TileType[] cache;
    private int cacheRadius;
    private byte[] generatedPage;

    public SiteRunner(Database db) throws IOException, DatabaseException {
        synchronized (this) {
            this.uncached = db;
            final Database.Rect borders = db.getBorders();
            this.cacheRadius = borders.absMinX();
            if (borders.absMaxX() > this.cacheRadius) this.cacheRadius = borders.absMaxX();
            if (borders.absMinY() > this.cacheRadius) this.cacheRadius = borders.absMinY();
            if (borders.absMaxY() > this.cacheRadius) this.cacheRadius = borders.absMaxY();
            this.cache = this.uncached.getMap(-this.cacheRadius, -this.cacheRadius, this.cacheRadius * 2 + 1, this.cacheRadius * 2 + 1);
        }
        this.generatePage();
        // this.server.createContext("/img/", new ImagePages());
        this.server.createContext("/", new RootPage());
        this.server.start();
    }


    private synchronized void assertCapacity(int startX, int startY, int width, int height) {
        if (startX < 0) startX = -startX;
        if (startY < 0) startY = -startY;
        final int newRadius;
        if (startX + width > startY + height) newRadius = startX + width;
        else newRadius = startY + height;
        if (newRadius <= this.cacheRadius) return;
        final int extend = newRadius - this.cacheRadius;
        final int newDiameter = newRadius * 2 + 1;
        final int oldDiameter = this.cacheRadius * 2 + 1;
        final TileType[] newCache = new TileType[newDiameter * newDiameter];
        for (int n = extend * newDiameter + extend, o = 0; o < this.cache.length; n += newDiameter, o += oldDiameter) {
            arraycopy(this.cache, o, newCache, n, oldDiameter);
        }
        this.cache = newCache;
        this.cacheRadius = newRadius;
    }

    @Override
    public synchronized void saveMap(TileType[] data, int startX, int startY, int width, int height) throws DatabaseException {
        this.uncached.saveMap(data, startX, startY, width, height);
        this.assertCapacity(startX, startY, width, height);
        startX += this.cacheRadius;
        startY += this.cacheRadius;
        final int diameter = this.cacheRadius * 2 + 1;
        int o = startY * diameter + startX;
        int i = 0;
        for (int y = 0; y < height; y++, o += diameter - width) {
            for (int x = 0; x < width; x++, o++, i++) {
                if (this.cache[o] == null) {
                    this.cache[o] = data[i];
                }
            }
        }
        this.generatePage();

    }

    @Override
    public TileType[] getMap(int startX, int startY, int width, int height) {
        // todo
        throw new UnsupportedOperationException("getMap");
    }

    @Override
    public Rect getBorders() {
        return new Database.Rect(-this.cacheRadius, this.cacheRadius, -this.cacheRadius, this.cacheRadius);
    }

    private synchronized void generatePage() {
        final TileType[] data;
        final int radius;
        data = this.cache;
        radius = this.cacheRadius;

        final StringBuilder sb = new StringBuilder();
        sb.append(SiteRunner.pagePrefix);
        int i = data.length - radius * 2 - 1;
        for (int y = radius; y >= -radius; y--, i -= radius * 4 + 2) {
            sb.append("<tr>");
            for (int x = -radius; x <= radius; x++, i++) {
                sb.append("<td coords=\"");
                sb.append(x);
                sb.append('#');
                sb.append(y);
                sb.append("\" class=\"");
                if (data[i] != null) {
                    if (x < 0) {
                        if (y < 0) {
                            sb.append("black ");
                        } else if (y > 0) {
                            sb.append("red ");
                        } else {
                            sb.append("forbidden ");
                        }
                    } else if (x > 0) {
                        if (y < 0) {
                            sb.append("blue ");
                        } else if (y > 0) {
                            sb.append("white ");
                        } else {
                            sb.append("forbidden ");
                        }
                    } else {
                        sb.append("forbidden ");
                    }
                    sb.append(data[i].cssClass);
                } else if (x == 0 || y == 0) {
                    sb.append("forbidden-hidden");
                }
                if (x == 0 && y == 0)
                    sb.append("\" id=\"zero");
                sb.append("\">");
                if (data[i] != null) {
                    sb.append("<div></div>");
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append(SiteRunner.pageSuffix);
        final byte[] encoded = sb.toString().getBytes(StandardCharsets.UTF_8);
        synchronized (this) {
            this.generatedPage = encoded;
        }
    }

    private class RootPage implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(200, SiteRunner.this.generatedPage.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(SiteRunner.this.generatedPage);
            }

        }
    }

    private static String buildString(String... lines) {
        final StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
            // sb.append('\n');
        }
        return sb.toString();
    }

    static class ImagePages implements HttpHandler {
        private static final Map<String, byte[]> IMAGES = Map.of(
                "/img/zero.svg", buildString(
                        "<svg  version=\"1.1\" width=\"15px\" height=\"15px\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 15 15\">",
                        "<path d='M 1.5 7.5 l 5 -2 l 2 -5 l 2 5 l 5 2 l -5 2 l -2 5 l -2 -5 l -5 -2 z' fill='white' stroke-width=\"0\"/>",
                        "<path d='M 3.5 7.5 L 7 7 L 3.5 7.5 L 8 7 L 11.5 7.5 L 8 8 L 7.5 11.5 L 7 8 M 3.5 7.5 z' fill='lightgreen' stroke-width=\"0\"/>",
                        "</svg>"
                ).getBytes(StandardCharsets.UTF_8),
                "/img/fields.svg", buildString(
                        "<svg viewBox=\"0 0 15 15\">",
                        "</svg>"
                ).getBytes(StandardCharsets.UTF_8),
                "/img/forest.svg", buildString(
                        "<svg viewBox=\"0 0 15 15\">",
                        "</svg>"
                ).getBytes(StandardCharsets.UTF_8),
                "/img/valley.svg", buildString(
                        "<svg viewBox=\"0 0 15 15\">",
                        "</svg>"
                ).getBytes(StandardCharsets.UTF_8),
                "/img/tavern.svg", buildString(
                        "<svg viewBox=\"0 0 15 15\">",
                        "</svg>"
                ).getBytes(StandardCharsets.UTF_8),
                "/img/shop.svg", buildString(
                        "<svg viewBox=\"0 0 15 15\">",
                        "</svg>"
                ).getBytes(StandardCharsets.UTF_8),
                "/img/well.svg", buildString(
                        "<svg viewBox=\"0 0 15 15\">",
                        "</svg>"
                ).getBytes(StandardCharsets.UTF_8)
        );

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] img = ImagePages.IMAGES.get(exchange.getRequestURI().getPath());
            if (img == null) {
                exchange.sendResponseHeaders(404, 0);
                return;
            }
            exchange.getResponseHeaders().add("Content-Type", "image/svg+xml");
            exchange.sendResponseHeaders(200, img.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(img);
            }
        }
    }

    private static final String pagePrefix = buildString(
            "<!DOCTYPE html>",
            "<html lang=\"en\">",
            "<head>",
            "<meta charset=\"UTF-8\">",
            "<title>Map | Chat Wars: Explorers</title>",
            "<style>",
            "html {",
            "height: 100%;",
            "}",
            "body {",
            "font-family:sans-serif;",
            "font-weight:bold;",
            "background-size:100%;",
            "padding:0;",
            "margin:0;",
            "overflow:clip;",
            "height:100%;",
            "}",
            "div.header {",
            "padding:0;",
            "background-color:#d4dbe5;",
            "position:absolute;",
            "top:0;",
            "left:0;",
            "right:0;",
            "height:50px;",
            "}",
            "div.header > div.border {",
            "border:1px solid black;",
            "border-left:none;",
            "border-right:none;",
            "height:5px;",
            "background-color:#f0c079;",
            "position:absolute;",
            "bottom:0;",
            "left:0;",
            "right:0;",
            "}",
            "div.header > div.title {",
            "padding-top:15px;",
            "text-align:center;",
            "margin-bottom:5px;",
            "}",
            "div.header > div.title > a {",
            "color:black;",
            "}",
            "div.header > div.title > a:hover {",
            "color:#111;",
            "}",
            "div.header > div.title > a:active {",
            "color:#333;",
            "}",
            "div.header > div.title > a:visited {",
            "color:black;",
            "}",
            "div#map {",
            "background-color:black;",
            "position:absolute;",
            "bottom:0;",
            "left:0;",
            "right:0;",
            "top:50px;",
            "overflow:auto;",
            "padding:100px;",
            "}",
            "div#map > table {",
            "width:max-content;",
            "height:max-content;",
            "}",
            "div#map > table td {",
            "border-spacing:3px;",
            "width:15px;",
            "height:15px;",
            "padding:5px;",
            "}",
            "div#map > table td[coords]::after {",
            "content:attr(coords);",
            "pointer-events:none;",
            "position:absolute;",
            "padding:5px;",
            "background-color:#d4dbe5;",
            "margin-top:20px;",
            "display:none;",
            "text-align:center;",
            "width:min-content;",
            "}",
            "div#map > table td[coords]:hover::after {",
            "display:block;",
            "}",
            "div#map > table td[coords].forest::after {",
            "content:attr(coords) ' \\A Forest';",
            "}",
            "div#map > table td[coords].valley::after {",
            "content:attr(coords) ' \\A Valley';",
            "}",
            "div#map > table td[coords].fields::after {",
            "content:attr(coords) ' \\A Fields';",
            "}",
            "div#map > table td[coords].tavern::after {",
            "content:attr(coords) ' \\A Tavern';",
            "}",
            "div#map > table td[coords].shop::after {",
            "content:attr(coords) ' \\A Shop';",
            "}",
            "div#map > table td[coords].well::after {",
            "content:attr(coords) ' \\A Well';",
            "}",
            "div#map > table td[coords].zero::after {",
            "content:attr(coords) ' \\A Zero';",
            "}",
            "div#map > table td[coords].blue::after {",
            "border:1px solid #00f;",
            "background-color:#aaf;",
            "}",
            "div#map > table td[coords].red::after {",
            "border:1px solid #f00;",
            "background-color:#faa;",
            "}",
            "div#map > table td[coords].white::after {",
            "border:1px solid #fff;",
            "background-color:#eee;",
            "}",
            "div#map > table td[coords].black::after {",
            "border:1px solid #888;",
            "background-color:#aaa;",
            "}",
            "div#map > table td[coords].forbidden::after, div#map > table td[coords].forbidden-hidden::after {",
            "border:1px solid #f80;",
            "background-color:#ff0;",
            "}",
            "div#map > table td.forbidden {",
            "border:1px solid #ff0;",
            "}",
            "div#map > table td.forbidden-hidden {",
            "border:1px solid #880;",
            "}",
            "div#map > table td.black {",
            "border:1px solid #888;",
            "}",
            "div#map > table td.white {",
            "border:1px solid #fff;",
            "}",
            "div#map > table td.blue {",
            "border:1px solid #00f;",
            "}",
            "div#map > table td.red {",
            "border:1px solid #f00;",
            "}",
            "div#map > table td > div {",
            "width:100%;",
            "height:100%;",
            "}",
            "div#map > table td.valley > div {",
            "background-color:#222;",
            "}",
            "div#map > table td.forest > div {",
            "background-color:#060;",
            "}",
            "div#map > table td.fields > div {",
            "background-color:#880;",
            "}",
            "div#map > table td.zero > div {",
            "background-color:black;",
            "}",
            "div#map > table td.shop > div {",
            "background-color:orange;",
            "}",
            "div#map > table td.tavern > div {",
            "background-color:#a40;",
            "}",
            "div#map > table td.well > div {",
            "background-color:#f0f;",
            "}",
            "</style>",
            "</head>",
            "<body>",
            "<div class=\"header\">",
            "<div class=\"title\">",
            "<a href=\"https://t.me/ChatWarsExplorersBot\">Chat Wars:Explorers</a> Map | <a href=\"https://t.me/cwxomrkbot\">Bot to suggest map</a>",
            "</div>",
            "<div class=\"border\"></div>",
            "</div>",
            "<div id=\"map\">",
            "<table>"
    );
    private static final String pageSuffix = buildString(
            "</table>",
            "<script>",
            "document.getElementById('zero').scrollIntoView({'block':'center','inline':'center'})",
            "</script>",
            "</div>",
            "</body>",
            "</html>"
    );

}
