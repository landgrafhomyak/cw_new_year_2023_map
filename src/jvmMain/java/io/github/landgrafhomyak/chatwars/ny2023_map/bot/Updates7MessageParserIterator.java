package io.github.landgrafhomyak.chatwars.ny2023_map.bot;

import org.json.JSONObject;

import java.util.Iterator;


/**
 * Итератор для ленивого чтения и валидации <a href="https://core.telegram.org/bots/api#getting-updates">обновлений</a>.
 */
final class Updates7MessageParserIterator implements Iterator<Update7Message>, Iterable<Update7Message> {
    /**
     * Итератор JSON-списка с <a href="https://core.telegram.org/bots/api#update">обновлениями</a>.
     */
    private final Iterator<Object> raw;
    /**
     * Следующее обновление, которое будет возвращено функцией {@link #next()}.
     * Инициализируется в {@link #hasNext()}.
     */
    private Update7Message next = null;

    /**
     * <a href="https://core.telegram.org/bots/api#making-requests">Возвращённый методом API объект</a> без каких-либо
     * изменений, кроме проверки на успешность запроса ({@code {..., "ok":true, ...}}).
     *
     * @param object Результат метода в {@link JSONObject JSON формате}.
     */
    public Updates7MessageParserIterator(JSONObject object) {
        this.raw = object.getJSONArray("result").iterator();
    }

    /**
     * Проверяет что ещё остались {@link #raw необработанные обновления} и выставляет {@link #next}.
     *
     * @see Update7Message#fromJson(JSONObject)
     */
    @Override
    public boolean hasNext() {
        if (this.next != null) return true;
        while (this.raw.hasNext()) {
            Object rawUpd = this.raw.next();
            if (!(rawUpd instanceof JSONObject))
                continue;
            this.next = Update7Message.fromJson((JSONObject) rawUpd);
            return true;
        }
        return false;
    }

    /**
     * Возвращает содержимое поля {@link #next} и обнуляет его.
     *
     * @see #hasNext()
     */
    @Override
    public Update7Message next() {
        if (!this.hasNext()) throw new IllegalStateException("All updates already returned");
        final Update7Message next = this.next;
        this.next = null;
        return next;
    }

    /**
     * Возвращает сам себя. Костыль для использования в цикле {@code for}.
     */
    @Override
    public Iterator<Update7Message> iterator() {
        return this;
    }
}

