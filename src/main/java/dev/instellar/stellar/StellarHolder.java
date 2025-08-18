package dev.instellar.stellar;

import org.jspecify.annotations.Nullable;

public final class StellarHolder {

    static @Nullable Stellar INSTANCE;
    public static void init() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Stellar is already set!");
        }

        INSTANCE = new Stellar();
    }

    private StellarHolder() {
        throw new AssertionError();
    }

}
