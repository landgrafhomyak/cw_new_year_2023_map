package io.github.landgrafhomyak.chatwars.ny2023_map.bot;

import org.json.JSONObject;

final class Update7Message {
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

    public final long updateId;
    public final long messageId;
    public final long chatId;
    public final long forwardFromUId;
    public final String rawText;
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
