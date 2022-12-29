package io.github.landgrafhomyak.chatwars.ny2023_map.db;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;

/**
 * База данных-заглушка. Ничего не хранит и не возвращает.
 * Может быть использована для создания in-memory базы данных при помощи {@link RectangleCacheDatabase кеширования}.
 * <p>
 * Так как не хранит никаких данных, является синглтоном.
 *
 * @see NullDatabase#INSTANCE
 */
public final class NullDatabase implements Database {
    /**
     * Единственный экземпляр этого класса.
     */
    public static final NullDatabase INSTANCE = new NullDatabase();

    private NullDatabase() {
    }

    /**
     * Ничего не делает и не производит проверки.
     */
    @Override
    public void saveMap(TileType[] data, int startX, int startY, int width, int height) {
    }

    /**
     * Возвращает массив размером {@code width * height} заполненный {@code null}.
     */
    @Override
    public TileType[] getMap(int startX, int startY, int width, int height) {
        return new TileType[width * height];
    }

    /**
     * Экземпляр границ, который будет возвращаться из {@link #getBorders()}.
     * Нужно для уменьшения количества создаваемых объектов и уменьшения потребления памяти.
     */
    private static final Database.Rect bordersInstance = new Database.Rect(0, 0, 0, 0);

    /**
     * Возвращает границы, включающие в себя только центр карты.
     */
    @Override
    public Rect getBorders() {
        return NullDatabase.bordersInstance;
    }
}
