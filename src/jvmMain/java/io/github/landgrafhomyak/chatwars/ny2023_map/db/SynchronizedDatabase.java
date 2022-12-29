package io.github.landgrafhomyak.chatwars.ny2023_map.db;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;

/**
 * Обёртка для синхронизации многопоточного доступа к {@link Database базе данных}.
 * Не поддерживает многопоточный доступ.
 */
public final class SynchronizedDatabase implements Database {
    /**
     * Несинхронизированная база данных.
     */
    private final Database wrapped;

    public SynchronizedDatabase(Database wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    synchronized public void saveMap(TileType[] data, int startX, int startY, int width, int height) throws DatabaseException {
        this.wrapped.saveMap(data, startX, startY, width, height);
    }

    @Override
    synchronized public TileType[] getMap(int startX, int startY, int width, int height) throws DatabaseException {
        return this.wrapped.getMap(startX, startY, width, height);
    }

    @Override
    synchronized public Rect getBorders() throws DatabaseException {
        return this.wrapped.getBorders();
    }
}
