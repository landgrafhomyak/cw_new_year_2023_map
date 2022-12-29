package io.github.landgrafhomyak.chatwars.ny2023_map.db;

/**
 * Исключение-обёртка для оборачивания других checked exception возникающих в функциях {@link Database}.
 */
public class DatabaseException extends Exception {
    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
