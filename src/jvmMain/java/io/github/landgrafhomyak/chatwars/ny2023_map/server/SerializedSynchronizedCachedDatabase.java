package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.Database;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.DatabaseException;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.RectangleCacheDatabase;

import java.io.IOException;
import java.io.OutputStream;

import static java.util.Arrays.copyOf;

final class SerializedSynchronizedCachedDatabase extends RectangleCacheDatabase {
    /**
     * Кешированное байтовое представление {@link #cache}.
     *
     * @see TileType#serial
     */
    private byte[] serialized = new byte[]{(byte) (TileType.ZERO.serial)};


    public SerializedSynchronizedCachedDatabase(Database uncached) throws DatabaseException {
        super(uncached);
        this.rect = uncached.getBorders();
        this.cache = super.getMap(this.rect.minX, this.rect.minY, this.rect.width(), this.rect.height());
        this.serialize();
    }


    private void serialize() {
        if (this.serialized.length != this.cache.length)
            this.serialized = copyOf(this.serialized, this.cache.length);
        int i = 0;
        for (final TileType type : this.cache) {
            this.serialized[i++] = (byte) (TileType.serial(type));
        }
    }

    /**
     * Экспортирует {@link #serialized сериализованный внутренний кеш} в указанный байтовый поток.
     *
     * @throws IOException См. {@link OutputStream#write(byte[])}.
     */
    @SuppressWarnings("unused")
    public synchronized void export(final OutputStream os) throws IOException {
        os.write(this.serialized);
    }

    /**
     * Возвращает {@link #serialized сериализованный внутренний кеш}.
     * На время его использования должна быть произведена синхронизация на этом объекте базы данных.
     * Изменение кеша недопустимо.
     */
    public byte[] serialized() {
        return this.serialized;
    }


    @SuppressWarnings("DuplicatedCode")
    @Override
    public synchronized void saveMap(TileType[] data, int startX, int startY, int width, int height) throws DatabaseException {
        super.saveMap(data, startX, startY, width, height);
        this.serialize();
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public synchronized TileType[] getMap(int startX, int startY, int width, int height) throws DatabaseException {
        return super.getMap(startX, startY, width, height);
    }

    @Override
    public synchronized Rect getBorders() {
        return this.rect;
    }
}
