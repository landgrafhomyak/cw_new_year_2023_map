package io.github.landgrafhomyak.chatwars.ny2023_map.server;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.Database;
import io.github.landgrafhomyak.chatwars.ny2023_map.db.DatabaseException;
import kotlin.NotImplementedError;

import java.io.IOException;
import java.io.OutputStream;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

/**
 * Кеш базы данных с {@link TileType#serial сериализацией в байтовый формат}.
 * Хранит карту в форме квадрата с центром в {@code 0#0}. Увеличение карты в одном направлении повлечёт увеличение в остальных.
 */
final class SquareCacheDatabase implements Database {
    /**
     * Радиус кеша - расстояние от цетра квадрата до границы.
     */
    @SuppressWarnings("UnusedAssignment")
    private int radius = 0;
    /**
     * Кешированные локации.
     * Имеет размер {@code (radius * 2 + 1) * (radius * 2 + 1)}.
     */
    @SuppressWarnings("UnusedAssignment")
    private TileType[] cache = new TileType[]{TileType.ZERO};
    /**
     * Исходная база данных, из которой подгружаются сохранённые локации.
     */
    private final Database uncached;
    /**
     * Кешированный объект с границами кешированной карты.
     */
    @SuppressWarnings("UnusedAssignment")
    private Rect rect = new Rect(0, 0, 0, 0);
    /**
     * Кешированное байтовое представление {@link #cache}.
     *
     * @see TileType#serial
     */
    private byte[] serialized = new byte[]{(byte) (TileType.ZERO.serial)};

    public SquareCacheDatabase(final Database uncached) throws DatabaseException {
        this.uncached = uncached;

        final Database.Rect borders = uncached.getBorders();
        this.radius = borders.absMinX();
        if (borders.absMaxX() > this.radius) this.radius = borders.absMaxX();
        if (borders.absMinY() > this.radius) this.radius = borders.absMinY();
        if (borders.absMaxY() > this.radius) this.radius = borders.absMaxY();
        this.cache = this.uncached.getMap(-this.radius, -this.radius, this.radius * 2 + 1, this.radius * 2 + 1);
        this.rect = new Database.Rect(-this.radius, this.radius, -this.radius, this.radius);
        this.serialize();
    }

    /**
     * Проверяет что в {@link #cache кеше} есть достаточно места для сохранения указанного участка карты,
     * расширяя {@link #cache кеш} при необходимости.
     *
     * @see Database#saveMap(TileType[], int, int, int, int)
     */
    @SuppressWarnings("ManualMinMaxCalculation")
    private synchronized void ensureCapacity(int startX, int startY, int width, int height) {
        if (startX < 0) startX = -startX;
        if (startY < 0) startY = -startY;
        final int newRadius;
        if (startX + width > startY + height) newRadius = startX + width;
        else newRadius = startY + height;
        if (newRadius <= this.radius) return;
        final int extend = newRadius - this.radius;
        final int newDiameter = newRadius * 2 + 1;
        final int oldDiameter = this.radius * 2 + 1;
        final TileType[] newCache = new TileType[newDiameter * newDiameter];
        for (int n = extend * newDiameter + extend, o = 0; o < this.cache.length; n += newDiameter, o += oldDiameter) {
            arraycopy(this.cache, o, newCache, n, oldDiameter);
        }
        this.cache = newCache;
        this.radius = newRadius;
        this.rect = new Database.Rect(-newRadius, newRadius, -newRadius, newRadius);
    }

    @Override
    public synchronized void saveMap(TileType[] data, int startX, int startY, int width, int height) throws DatabaseException {
        this.uncached.saveMap(data, startX, startY, width, height);
        this.ensureCapacity(startX, startY, width, height);
        startX += this.radius;
        startY += this.radius;
        final int diameter = this.radius * 2 + 1;
        int o = startY * diameter + startX;
        int i = 0;
        for (int y = 0; y < height; y++, o += diameter - width) {
            for (int x = 0; x < width; x++, o++, i++) {
                if (this.cache[o] == null) {
                    this.cache[o] = data[i];
                }
            }
        }
        this.serialize();
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public TileType[] getMap(int startX, int startY, int width, int height) throws DatabaseException {
        // todo
        throw new NotImplementedError("Cached 'getMap'");
    }

    @Override
    public Rect getBorders() {
        return this.rect;
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
}
