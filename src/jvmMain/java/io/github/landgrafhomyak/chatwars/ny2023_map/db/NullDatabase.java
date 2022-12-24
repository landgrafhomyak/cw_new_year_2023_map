package io.github.landgrafhomyak.chatwars.ny2023_map.db;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;

public final class NullDatabase implements Database {
    public static final NullDatabase INSTANCE = new NullDatabase();

    private NullDatabase() {
    }

    @Override
    public void saveMap(TileType[] data, int startX, int startY, int width, int height) {
    }

    @Override
    public TileType[] getMap(int startX, int startY, int width, int height) {
        return new TileType[width * height];
    }

    private static final Database.Rect bordersInstance = new Database.Rect(0, 0, 0, 0);

    @Override
    public Rect getBorders() {
        return NullDatabase.bordersInstance;
    }
}
