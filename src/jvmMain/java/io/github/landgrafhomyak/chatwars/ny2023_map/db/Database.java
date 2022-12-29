package io.github.landgrafhomyak.chatwars.ny2023_map.db;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;

/**
 * Интерфейс для чтения и сохранения карты в базу данных.
 * Все checked exceptions должны быть обёрнуты в {@link DatabaseException}.
 */
public interface Database {
    /**
     * Сохраняет участок карты в базу данных.
     *
     * @param data   Одномерный массив размера <b><i>как минимум</i></b> {@code width * height}.
     *               Локации идут в порядке
     *               {@code (0, 0), (0, 1), ... {0, width-1}, {1, 0}, ... {height-1, 0}, ... {height-1, width-1}}.
     *               Если тип локации не известен, элемент должен быть null.
     *               Такой элемент не должен перезаписывать данные о локации, если её тип известен базе данных.
     * @param startX Координата на горизонтальной оси координат первого элемента массива.
     * @param startY Координата на вертикальной оси координат первого элемента массива.
     * @param width  Ширина участка карты.
     * @param height Высота участка карты.
     * @see #saveMap(TileType[], int, int, int, int)
     */
    void saveMap(TileType[] data, int startX, int startY, int width, int height) throws DatabaseException;

    /**
     * Возвращает одномерный массив размером {@code width * height} с типами локаций в порядке
     * {@code (0, 0), (0, 1), ... {0, width-1}, {1, 0}, ... {height-1, 0}, ... {height-1, width-1}}.
     * Если тип локации неизвестен, соответствующий элемент в массиве будет равен {@code null}.
     *
     * @param startX Координата на горизонтальной оси координат первого элемента массива.
     * @param startY Координата на вертикальной оси координат первого элемента массива.
     * @param width  Ширина участка карты.
     * @param height Высота участка карты.
     * @see #getMap(int, int, int, int)
     * @see #getBorders()
     */
    TileType[] getMap(int startX, int startY, int width, int height) throws DatabaseException;

    /**
     * Абсолютные координаты участка карты.
     */
    final class Rect {
        /**
         * Минимальная координата на горизонтальной оси.
         */
        public final int minX;

        /**
         * Максимальная координата на горизонтальной оси.
         */
        public final int maxX;

        /**
         * Минимальная координата на вертикальной оси.
         */
        public final int minY;

        /**
         * Максимальная координата на вертикальной оси.
         */
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

        /**
         * Абсолютное значение {@link #minX}.
         */
        public int absMinX() {
            return this.minX < 0 ? -this.minX : this.minX;
        }

        /**
         * Абсолютное значение {@link #maxX}.
         */
        public int absMaxX() {
            return this.maxX < 0 ? -this.maxX : this.maxX;
        }

        /**
         * Абсолютное значение {@link #minY}.
         */
        public int absMinY() {
            return this.minY < 0 ? -this.minY : this.minY;
        }

        /**
         * Абсолютное значение {@link #maxY}.
         */
        public int absMaxY() {
            return this.maxY < 0 ? -this.maxY : this.maxY;
        }

        /**
         * Ширина участка карты с данными границами.
         */
        public int width() {
            return this.maxX - this.minX + 1;
        }

        /**
         * Высота участка карты с данными границами.
         */
        public int height() {
            return this.maxY - this.minY + 1;
        }
    }

    /**
     * Возвращает границы хранящейся в базе данных карте.
     *
     * @see #getMap(int, int, int, int)
     */
    Database.Rect getBorders() throws DatabaseException;
}
