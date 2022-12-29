package io.github.landgrafhomyak.chatwars.ny2023_map.db;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;

import static java.lang.System.arraycopy;

/**
 * Кеширует карту перед сохранением в {@link #uncached базу данных}.
 * Кеш представляет собой минимальный прямоугольник, в который помещается карта.
 * Не поддерживает многопоточный доступ.
 * <p>
 * Изначально не содержит в себе никаких данных.
 * Для инициализации кеша определите чтение {@link #uncached базы данных} в классе-наследнике.
 * Любые изменения в базе данных в обход этого кеша в нём не отображаются.
 */
public class RectangleCacheDatabase implements Database {
    /**
     * Одномерное представление кешированной карты. Размер вычисляется на основании значения {@link #rect}
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

    public RectangleCacheDatabase(final Database uncached) {
        this.uncached = uncached;
    }

    /**
     * Проверяет что в {@link #cache кеше} есть достаточно места для сохранения указанного участка карты,
     * расширяя {@link #cache кеш} при необходимости.
     *
     * @see Database#saveMap(TileType[], int, int, int, int)
     */
    private void ensureCapacity(int startX, int startY, int width, int height) {
        int westExtend = startX < this.rect.minX ? this.rect.minX - startX : 0;
        int eastExtend = startX + width > this.rect.maxX ? startX + width - this.rect.maxX : 0;
        int southExtend = startY < this.rect.minY ? this.rect.minY - startY : 0;
        int northExtend = startY + height > this.rect.maxY ? startY + height - this.rect.maxY : 0;
        if (northExtend == 0 && southExtend == 0 && eastExtend == 0 && westExtend == 0)
            return;
        final Database.Rect newRect = new Database.Rect(
                this.rect.minX - westExtend,
                this.rect.maxX + eastExtend,
                this.rect.minY - southExtend,
                this.rect.maxY + northExtend
        );
        final int newWidth = newRect.maxX - newRect.minX + 1;
        final TileType[] newCache = new TileType[newWidth * (newRect.maxY - newRect.minY + 1)];
        final int oldWidth = this.rect.maxX - this.rect.minX + 1;
        int o = 0;
        int n = newWidth * southExtend + westExtend;
        for (int y = this.rect.minY; y <= this.rect.maxY; y++, o += oldWidth, n += newWidth) {
            arraycopy(this.cache, o, newCache, n, oldWidth);
        }
        this.rect = newRect;
        this.cache = newCache;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void saveMap(TileType[] data, int startX, int startY, int width, int height) throws DatabaseException {
        this.uncached.saveMap(data, startX, startY, width, height);
        this.ensureCapacity(startX, startY, width, height);
        final int cWidth = this.rect.maxX - this.rect.minX + 1;
        startY -= this.rect.minY;
        startX -= this.rect.minX;
        int o = startY * cWidth + startX;
        int i = 0;
        for (int y = 0; y < height; y++, o += cWidth - width) {
            for (int x = 0; x < width; x++, o++, i++) {
                if (this.cache[o] == null && data[i] != null) {
                    this.cache[o] = data[i];
                }
            }
        }
    }

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
