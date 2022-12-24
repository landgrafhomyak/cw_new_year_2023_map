package io.github.landgrafhomyak.chatwars.ny2023_map;

import io.github.landgrafhomyak.chatwars.ny2023_map.db.Database;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.DatabaseException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BotRunner implements Runnable {
    private static final long GAME_BOT_UID = 5913926488L;


    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(21))
            .build();
    private final String prefixUrl;
    private final Database database;

    public BotRunner(String token, Database database) {
        this.prefixUrl = String.format("https://api.telegram.org/bot%s/", token);
        this.database = database;
    }


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
                for (BotRunner.UpdateMessage update : new BotRunner.UpdatesIterator(updates)) {
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

    private void procMessage(BotRunner.UpdateMessage msg) throws TgApiException, IOException, InterruptedException {
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

    private static final Pattern mapPattern = Pattern.compile(
            "\uD83D\uDCCD: (-?\\d+)#(-?\\d+) – you are here\\.\n\\S+ Castle lands\\.\n(?:\\S+ \\wegion x\\d+ points bonus\n)?Region details /region\n\n(\\|[\\s\\S]+\\|)(?:\n\nExtended map: /map_\\d)?$"
    );
    private static final Pattern profilePattern = Pattern.compile(
            "Global Battle #4 in [^\n]*?!\n\n\\S+ \\S+ of \\S+ Castle\n\uD83C\uDFC5Level: [\\s\\S]+\nPosition: (-?\\d+)#(-?\\d+)\n(.+)\n[\\s\\S]*$"
    );


    private void reply(long chat, long message, String text) throws IOException, InterruptedException, TgApiException {
        this.request(
                "sendMessage",
                String.format(
                        "{\"chat_id\":%d,\"reply_to_message_id\":%d,\"text\":%s}",
                        chat, message,
                        JSONObject.quote(text)
                )
        );
    }

    private static class UpdatesIterator implements Iterator<BotRunner.UpdateMessage>, Iterable<BotRunner.UpdateMessage> {
        private final Iterator<Object> raw;
        private BotRunner.UpdateMessage next = null;

        public UpdatesIterator(JSONObject object) {
            this.raw = object.getJSONArray("result").iterator();
        }

        @Override
        public boolean hasNext() {
            if (this.next != null) return true;
            while (this.raw.hasNext()) {
                Object rawUpd = this.raw.next();
                if (!(rawUpd instanceof JSONObject))
                    continue;
                this.next = BotRunner.UpdateMessage.fromJson((JSONObject) rawUpd);
                return true;
            }
            return false;
        }

        @Override
        public BotRunner.UpdateMessage next() {
            if (!this.hasNext()) throw new IllegalStateException("All updates already returned");
            final BotRunner.UpdateMessage next = this.next;
            this.next = null;
            return next;
        }

        @Override
        public Iterator<BotRunner.UpdateMessage> iterator() {
            return this;
        }
    }

    private static class UpdateMessage {
        public static UpdateMessage fromJson(JSONObject rawUpdate) {
            final JSONObject message = rawUpdate.optJSONObject("message");
            if (message == null) return null;
            final JSONObject forwardFrom = message.optJSONObject("forward_from");
            final JSONObject chat = message.optJSONObject("chat");
            if (chat == null) return null;
            return new UpdateMessage(
                    rawUpdate.getLong("update_id"),
                    message.getLong("message_id"),
                    chat.getLong("id"),
                    forwardFrom == null ? 0 : forwardFrom.getLong("id"),
                    message.optString("text"),
                    chat.getString("type").equals("private")
            );
        }

        public final long updateId;
        public final long messageId;
        public final long chatId;
        public final long forwardFromUId;
        public final String rawText;
        public final boolean verbose;

        private UpdateMessage(
                final long updateOffset,
                final long messageId,
                final long chatId,
                final long forwardFromUId,
                final String rawText,
                final boolean verbose
        ) {
            this.updateId = updateOffset;
            this.messageId = messageId;
            this.chatId = chatId;
            this.forwardFromUId = forwardFromUId;
            this.rawText = rawText;
            this.verbose = verbose;
        }
    }

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
            throw new BotRunner.TgApiException(response.getString("description"));
        return response;

    }

    public static class TgApiException extends Exception {
        public TgApiException(String message) {
            super(message);
        }
    }

}

