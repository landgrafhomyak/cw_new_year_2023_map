package io.github.landgrafhomyak.chatwars.ny2023_map.db;

import io.github.landgrafhomyak.chatwars.ny2023_map.TileType;
import org.sqlite.SQLiteConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * База данных на основе <a href="https://ru.wikipedia.org/wiki/SQLite">SQLite</a>.
 * Не поддерживает многопоточность.
 */
public final class SqliteDatabase implements Database {
    private final SQLiteConnection connection;

    public SqliteDatabase(SQLiteConnection connection) throws SQLException {
        this.connection = connection;
        this.initDb();
    }

    /**
     * Инициализирует базу данных, создавая необходимые таблицы.
     */
    private void initDb() throws SQLException {
        try (PreparedStatement stmt = this.connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS map(x INTEGER NOT NULL, y INTEGER NOT NULL, type INTEGER NOT NULL, UNIQUE (x, y), PRIMARY KEY (x, y))"
        )) {
            stmt.executeUpdate();
        }
        if (!this.connection.getAutoCommit())
            this.connection.commit();
    }


    @Override
    public void saveMap(TileType[] data, int startX, int startY, int width, int height) throws DatabaseException {
        if (data.length < width * height) {
            throw new IllegalArgumentException("Tiles array length not matches width*height");
        }

        final StringBuilder sb = new StringBuilder("INSERT INTO map(x, y, type) VALUES ");
        int i = width * height - 1;
        for (int y = startY + height - 1; y >= startY; y--) {
            for (int x = startX + width - 1; x >= startX; x--, i--) {
                sb.append('(');
                sb.append(x);
                sb.append(',');
                sb.append(y);
                sb.append(',');
                sb.append((int) TileType.serial(data[i]));
                sb.append(')');
                sb.append(',');
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" ON CONFLICT(x, y) DO UPDATE SET type=excluded.type;");

        try {
            try (PreparedStatement stmt = this.connection.prepareStatement(
                    sb.toString()
            )) {
                stmt.executeUpdate();
            }
            if (!this.connection.getAutoCommit())
                this.connection.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public TileType[] getMap(int startX, int startY, int width, int height) throws DatabaseException {
        final TileType[] data;
        try {
            final ResultSet rs;
            try (PreparedStatement stmt = this.connection.prepareStatement(
                    "SELECT x, y, type from map WHERE  ? <= x AND x < ? AND ? <= y AND y < ?"
            )) {
                stmt.setInt(1, startX);
                stmt.setInt(2, startX + width);
                stmt.setInt(3, startY);
                stmt.setInt(4, startY + height);
                rs = stmt.executeQuery();


                data = new TileType[width * height];
                try (rs) {
                    while (rs.next()) {
                        data[
                                (rs.getInt(2) - startY) * width + (rs.getInt(1) - startX)
                                ] = TileType.fromSerial((char) rs.getInt(3));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return data;
    }

    @Override
    public Rect getBorders() throws DatabaseException {
        try {
            final ResultSet rs;
            try (PreparedStatement stmt = this.connection.prepareStatement(
                    "SELECT min(x), min(y), max(x), max(y) FROM map"
            )) {
                rs = stmt.executeQuery();


                try (rs) {
                    rs.next();
                    return new Database.Rect(
                            rs.getInt(1),
                            rs.getInt(3),
                            rs.getInt(2),
                            rs.getInt(4)
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
