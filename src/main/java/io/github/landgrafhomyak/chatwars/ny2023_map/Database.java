package io.github.landgrafhomyak.chatwars.ny2023_map;

public interface Database {
    void saveMap(TileType[] data, int startX, int startY, int width, int height) throws DatabaseException;

    TileType[] getMap(int startX, int startY, int width, int height) throws DatabaseException;

    /*
    void setBuildings(int x, int y, Building[] data);

    void setBuildings(int x, int y, Building[] data, Castle visibleFor);

    Building[] getBuildings(int x, int y);

    Building[] getBuildings(int x, int y, Castle visibleFor);

    void clearBuildings();
     */

    final class Rect {
        public final int minX;
        public final int maxX;
        public final int minY;
        public final int maxY;

        public Rect(
                final int minX,
                final int maxX,
                final int minY,
                final int maxY
        ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }

        public int absMinX() {
            return this.minX < 0 ? -this.minX : this.minX;
        }

        public int absMaxX() {
            return this.maxX < 0 ? -this.maxX : this.maxX;
        }

        public int absMinY() {
            return this.minY < 0 ? -this.minY : this.minY;
        }

        public int absMaxY() {
            return this.maxY < 0 ? -this.maxY : this.maxY;
        }
    }

    Database.Rect getBorders() throws DatabaseException;
}
