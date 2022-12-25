package io.github.landgrafhomyak.chatwars.ny2023_map.bot;

import org.json.JSONObject;

import java.util.Iterator;

final class Updates7MessageParserIterator implements Iterator<Update7Message>, Iterable<Update7Message> {
    private final Iterator<Object> raw;
    private Update7Message next = null;

    public Updates7MessageParserIterator(JSONObject object) {
        this.raw = object.getJSONArray("result").iterator();
    }

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

    @Override
    public Update7Message next() {
        if (!this.hasNext()) throw new IllegalStateException("All updates already returned");
        final Update7Message next = this.next;
        this.next = null;
        return next;
    }

    @Override
    public Iterator<Update7Message> iterator() {
        return this;
    }
}

