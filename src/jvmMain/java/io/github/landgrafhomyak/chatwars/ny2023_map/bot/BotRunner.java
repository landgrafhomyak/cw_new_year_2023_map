package io.github.landgrafhomyak.chatwars.ny2023_map.bot;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.Database;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.DatabaseException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Главная функция потока для работы телеграм бота, принимающего карты для игроков.
 */
public final class BotRunner implements Runnable {
    /**
     * User ID игрового бота, из которого должны быть пересланы карты.
     */
    static final long GAME_BOT_UID = 5913926488L;

    /**
     * HTTP клиент.
     */
    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(21))
            .build();
    /**
     * Базовый URL для всех запросов для конкретного телеграм бота.
     */
    private final String prefixUrl;

    /**
     * База данных в которую будет сохраняться карта.
     */
    private final Database database;

    /**
     * @param token    Токен телеграм бота.
     * @param database База данных для сохранения карты.
     */
    public BotRunner(String token, Database database) {
        this.prefixUrl = String.format("https://api.telegram.org/bot%s/", token);
        this.database = database;
    }

    /**
     * Вспомогательная функция для отправления сообщения в ответ на другое.
     *
     * @param chat             ID чата, в который будет отправлено сообщение.
     * @param reply_to_message ID сообщения в чате, на которое будет ответить.
     * @param text             Текст сообщения.
     * @throws TgApiException Ошибка от серверов телеграма.
     */
    private void reply(long chat, long reply_to_message, String text) throws IOException, InterruptedException, TgApiException {
        this.request(
                "sendMessage",
                String.format(
                        "{\"chat_id\":%d,\"reply_to_message_id\":%d,\"text\":%s}",
                        chat, reply_to_message,
                        JSONObject.quote(text)
                )
        );
    }

    /**
     * Вспомогательная функция для отправки API запрос на сервера телеграма.
     *
     * @param method Название API метода.
     * @param data   Сериализованные в {@link JSONObject#toString() JSON} формате параметры запроса.
     * @return Ответ сервера в виде {@link JSONObject JSON}.
     * @throws TgApiException Ошибка от серверов телеграма.
     */
    private JSONObject request(String method, String data) throws IOException, InterruptedException, TgApiException {
        JSONObject response = new JSONObject(
                this.client.send(
                        HttpRequest.newBuilder(URI.create(String.format("%s%s", this.prefixUrl, method)))
                                .version(HttpClient.Version.HTTP_2)
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(data))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                ).body()
        );
        if (!response.getBoolean("ok"))
            throw new TgApiException(response.getString("description"));
        return response;
    }


    /**
     * Занимает поток, непрерывно запрашивая обновления от серверов телеграма и {@link #procMessage(Update7Message) обрабатывая их}.
     * Все ошибки выводятся в {@link System#err} и игнорируются.
     */
    @Override
    public void run() {
        long offset = 0;
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                final JSONObject updates;
                updates = this.request(
                        "getUpdates",
                        String.format("{\"offset\":%d,\"timeout\":20,\"allowed_updates\":[\"message\"]}", offset)
                );

                offset = -1;
                for (Update7Message update : new Updates7MessageParserIterator(updates)) {
                    if (update.updateId > offset)
                        offset = update.updateId;
                    try {
                        this.procMessage(update);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                offset++;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Разворот массива без создания нового.
     * <p>
     * Нужен для изменения порядка строк карты по убыванию (человеческое представление)
     * в порядок по возрастанию (машинное представление)
     */
    private static void reverseArrayInplace(String[] arr) {
        int i = 0;
        String temp;
        while (i < arr.length - i - 1) {
            temp = arr[i];
            arr[i] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = temp;
            i++;
        }
    }

    /**
     * Регулярное выражение для отделения координат и данных о ячейках карты от других данных в сообщении
     * (бонус локации, принадлежность локации, сопровождающий текст).
     */
    private static final Pattern mapPattern = Pattern.compile(
            "\uD83D\uDCCD: (-?\\d+)#(-?\\d+) – you are here\\.\n\\S+ Castle lands\\.\n(?:\\S+ \\wegion x\\d+ points bonus\n)?Region details /region\n\n(\\|[\\s\\S]+\\|)(?:\n\nExtended map: /map_\\d)?$"
    );

    /**
     * Регулярное выражение для извлечения информации о локации на которой находится игрок из сообщения с игровым профилем.
     */
    private static final Pattern profilePattern = Pattern.compile(
            "Global Battle #\\d in [^\n]*?!\n\n\\S+ \\S+ of \\S+ Castle\n\uD83C\uDFC5Level: [\\s\\S]+\nPosition: (-?\\d+)#(-?\\d+)\n(.+)\n[\\s\\S]*$"
    );

    /**
     * Функция для обработки сообщения.
     * <ul>
     *     <li>Проверяет что оно является текстовым, а не картинкой, стикером, видео и т.д.</li>
     *     <li>Проверяет что оно переслано из {@link #GAME_BOT_UID нужного бота}</li>
     *     <li>Извлекает карту если она есть и парсит её</li>
     *     <li>Сохраняет полученные данные в {@link #database в базу данных}</li>
     * </ul>
     *
     * @param msg Сообщение для обработки.
     * @throws InterruptedException Ошибка от серверов телеграма.
     */
    private void procMessage(Update7Message msg) throws TgApiException, IOException, InterruptedException {
        if (msg.rawText == null) {
            if (msg.verbose)
                this.reply(msg.chatId, msg.messageId, "\u274CMap must be a text");
            return;
        }
        if (msg.forwardFromUId != BotRunner.GAME_BOT_UID) {
            if (msg.verbose)
                this.reply(msg.chatId, msg.messageId, "\u274CMap must be forwarded from game bot");
            return;
        }

        Matcher m;
        if ((m = BotRunner.mapPattern.matcher(msg.rawText)).matches()) {
            final int centerX = Integer.parseInt(m.group(1));
            final int centerY = Integer.parseInt(m.group(2));
            String[] rows = m.group(3).split("\n");
            reverseArrayInplace(rows);
            String[][] rawData = new String[rows.length][];
            for (int y = 0; y < rows.length; y++) {
                rawData[y] = rows[y].split("\\|");
            }
            final int height = rawData.length;
            TileType[] data = new TileType[height * rawData[0].length];
            int i = 0;
            for (String[] row : rawData) {
                for (String cell : row) {
                    if (cell.length() > 0)
                        data[i++] = TileType.fromEmoji(cell);
                }
            }
            final int width = i / height;

            try {
                this.database.saveMap(
                        data,
                        centerX - width / 2,
                        centerY - height / 2,
                        width,
                        height
                );
            } catch (DatabaseException e) {
                e.printStackTrace();
                this.reply(msg.chatId, msg.messageId, "\u274CFailed to save map :(");
                return;
            }
        } else if ((m = BotRunner.profilePattern.matcher(msg.rawText)).matches()) {
            try {
                this.database.saveMap(
                        new TileType[]{TileType.fromEmoji(m.group(3))},
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        1,
                        1
                );
            } catch (DatabaseException e) {
                e.printStackTrace();
                this.reply(msg.chatId, msg.messageId, "\u274CFailed to save map :(");
                return;
            }
        } else {
            if (msg.verbose)
                this.reply(msg.chatId, msg.messageId, "\u274CNothing interesting in this message");
            return;
        }
        this.reply(msg.chatId, msg.messageId, "\u2705Map saved!");
    }
}

