package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import io.github.landgrafhomyak.chatwars.ny2023_map.NullCharLineCompressing;
import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.Database;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.DatabaseException;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.RectangleCacheDatabase;

import java.io.IOException;
import java.io.OutputStream;

import static java.util.Arrays.copyOf;

/**
 * Модификация кеша под нужды {@link SiteServer HTTP сервера}. Добавлена сериализация и синхронизация.
 */
final class SerializedSynchronizedCompressedCachedDatabase extends RectangleCacheDatabase {
    /**
     * Кеш сериализованого представления {@link #cache}.
     * Так как доступ к сайту происходит чаще чем изменение кеша карты, имеет смысл кешировать и её сериализованную
     * версию вместо генерации на каждом обращении.
     *
     * @see TileType#serial
     */
    byte[] serialized = new byte[]{(byte) (TileType.ZERO.serial)};
    int serializedSize = 1;


    public SerializedSynchronizedCompressedCachedDatabase(Database uncached) throws DatabaseException {
        super(uncached);
        this.rect = uncached.getBorders();
        this.cache = super.getMap(this.rect.minX, this.rect.minY, this.rect.width(), this.rect.height());
        this.serialize();
    }


    /**
     * {@link TileType#serial Сериализует} {@link #cache} и сохраняет в {@link #serialized}.
     */
    private void serialize() {
        if (this.serialized.length < this.cache.length * 2)
            this.serialized = copyOf(this.serialized, this.cache.length * 2);
        this.serializedSize = NullCharLineCompressing.compress(this.cache, this.serialized);
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
