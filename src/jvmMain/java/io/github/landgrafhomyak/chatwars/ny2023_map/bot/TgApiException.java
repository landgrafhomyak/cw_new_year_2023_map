package io.github.landgrafhomyak.chatwars.ny2023_map.bot;

/**
 * <a href="https://core.telegram.org/bots/api#making-requests">Ошибка от серверов телеграма</a>.
 */
final class TgApiException extends Exception {
    TgApiException(String message) {
        super(message);
    }
}
