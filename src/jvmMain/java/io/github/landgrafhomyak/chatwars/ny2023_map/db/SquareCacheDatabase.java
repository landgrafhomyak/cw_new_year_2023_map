package io.github.landgrafhomyak.chatwars.ny2023_map.db;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;

import static java.lang.System.arraycopy;

/**
 * Кеширует карту перед сохранением в {@link #uncached базу данных}.
 * Кеш представляет собой минимальный квадрат с центром в координате {@code (0, 0)}, в который помещается карта.
 * <p>
 * Изначально не содержит в себе никаких данных.
 * Для инициализации кеша определите чтение {@link #uncached базы данных} в классе-наследнике.
 * Любые изменения в базе данных в обход этого кеша в нём не отображаются.
 *
 * @deprecated Есть более оптимальный по памяти алгоритм кеширования: {@link RectangleCacheDatabase}.
 */
@Deprecated(since = "1.2")
public class SquareCacheDatabase implements Database {
    /**
     * Радиус кеша - расстояние от цетра квадрата до границы.
     */
    protected int radius = 0;
    /**
     * Кешированные локации.
     * Имеет размер {@code (radius * 2 + 1) * (radius * 2 + 1)}.
     */
    protected TileType[] cache = new TileType[]{TileType.ZERO};
    /**
     * Исходная база данных, из которой подгружаются сохранённые локации.
     */
    private final Database uncached;
    /**
     * Кешированный объект с границами кешированной карты.
     */
    protected Rect rect = new Rect(0, 0, 0, 0);

    public SquareCacheDatabase(final Database uncached) {
        this.uncached = uncached;
    }

    /**
     * Проверяет что в {@link #cache кеше} есть достаточно места для сохранения указанного участка карты,
     * расширяя {@link #cache кеш} при необходимости.
     *
     * @see Database#saveMap(TileType[], int, int, int, int)
     */
    @SuppressWarnings("ManualMinMaxCalculation")
    private void ensureCapacity(int startX, int startY, int width, int height) {
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

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void saveMap(TileType[] data, int startX, int startY, int width, int height) throws DatabaseException {
        this.uncached.saveMap(data, startX, startY, width, height);
        this.ensureCapacity(startX, startY, width, height);
        startX += this.radius;
        startY += this.radius;
        final int diameter = this.radius * 2 + 1;
        int o = startY * diameter + startX;
        int i = 0;
        for (int y = 0; y < height; y++, o += diameter - width) {
            for (int x = 0; x < width; x++, o++, i++) {
                if (this.cache[o] == null && data[i] != null) {
                    this.cache[o] = data[i];
                }
            }
        }
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public TileType[] getMap(int startX, int startY, int width, int height) throws DatabaseException {
        // todo cache
        return this.uncached.getMap(startX, startY, width, height);
    }

    @Override
    public Rect getBorders() {
        return this.rect;
    }


}
