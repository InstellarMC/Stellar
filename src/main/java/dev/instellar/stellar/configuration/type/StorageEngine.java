package dev.instellar.stellar.configuration.type;

import lombok.Getter;

@Getter
public enum StorageEngine {

    MYSQL(1),
    MARIADB(2);

    private final int id;

    StorageEngine(final int id) {
        this.id = id;
    }

    public static StorageEngine fromId(final int id) {
        for (final var storageEngine : values()) {
            if (storageEngine.getId() == id) {
                return storageEngine;
            }
        }

        throw new IllegalArgumentException("No enum constant with id " + id);
    }

}
