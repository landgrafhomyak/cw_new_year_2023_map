package io.github.landgrafhomyak.chatwars.ny2023_map.bot;

import org.json.JSONObject;

/**
 * <a href="https://core.telegram.org/bots/api#message">Телеграм-сообщение</a>.
 * Содержит только необходимую этому приложению информацию.
 * Для экономии памяти, информация об <a href="https://core.telegram.org/bots/api#update">обновлении</a>
 * (ID обновления) также содержится в этом классе вместо отдельного объекта.
 */
final class Update7Message {
    /**
     * Парсит и валидирует данные <a href="https://core.telegram.org/bots/api#update">обновления</a>
     * из {@link JSONObject JSON формата}.
     * <p>
     * Требования к обновлению:
     * <ul>
     *     <li>Имеет тип NewMessage ({@code message})</li>
     *     <li>Содержит информацию о чате, в который отправлено сообщение.</li>
     * </ul>
     *
     * @param rawUpdate <a href="https://core.telegram.org/bots/api#update">Объект типа Update</a>
     *                  в формате {@link JSONObject JSON}.
     * @return {@link Update7Message Объект с данными} если требования выполняются, иначе {@code null}.
     */
    static Update7Message fromJson(JSONObject rawUpdate) {
        final JSONObject message = rawUpdate.optJSONObject("message");
        if (message == null) return null;
        final JSONObject forwardFrom = message.optJSONObject("forward_from");
        final JSONObject chat = message.optJSONObject("chat");
        if (chat == null) return null;
        return new Update7Message(
                rawUpdate.getLong("update_id"),
                message.getLong("message_id"),
                chat.getLong("id"),
                forwardFrom == null ? 0 : forwardFrom.getLong("id"),
                message.optString("text"),
                chat.getString("type").equals("private")
        );
    }

    /**
     * ID обновления.
     * Нужен для <a href="https://core.telegram.org/bots/api#getupdates">получения следующих обновлений</a>.
     * <p>
     * {@code update.update_id} | <a href="https://core.telegram.org/bots/api#update">{@code Update#update_id}</a>
     *
     * @see BotRunner#run()
     */
    public final long updateId;
    /**
     * ID сообщения.
     * Нужен для отправки ответного сообщения с результатами обработки.
     * <p>
     * {@code update.message.message_id} | <a href="https://core.telegram.org/bots/api#message">{@code Message#message_id}</a>
     */
    public final long messageId;
    /**
     * ID чата, в который отправлено сообщение.
     * Нужен для отправки ответного сообщения с результатами обработки.
     * <p>
     * {@code update.message.chat.id} | <a href="https://core.telegram.org/bots/api#chat">{@code Chat#id}</a>.
     */
    public final long chatId;
    /**
     * ID автора пересланного сообщения. Если сообщение не является пересланным, содержит {@code 0}.
     * Нужен для валидации источника карты и предотвращения записи сторонних данных.
     * <p>
     * {@code update.message.forward_from.id} | <a href="https://core.telegram.org/bots/api#user">{@code User#id}</a>
     *
     * @see BotRunner#GAME_BOT_UID
     */
    public final long forwardFromUId;
    /**
     * Тест сообщения. {@code null} если не является текстовым.
     * <p>
     * {@code update.message.text} | <a href="https://core.telegram.org/bots/api#message">{@code Message#text}</a>
     */
    public final String rawText;
    /**
     * Флаг, ставящийся если сообщения отправлено в личные сообщения боту. В чатах очищен, чтобы не отвечать на
     * каждое сообщение, так как в чате может быть происходить общение, не связанное с ботом.
     * <p>
     * {@code update.message.chat.type} | <a href="https://core.telegram.org/bots/api#chat">{@code Chat#type}</a>
     */
    public final boolean verbose;

    private Update7Message(
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
