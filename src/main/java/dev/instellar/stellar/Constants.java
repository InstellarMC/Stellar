package dev.instellar.stellar;

public final class Constants {

    private Constants() {
        throw new AssertionError();
    }

    public static final class FlagProperties {

        public static final String ExecutorNameFormat = System.getProperty("stellar.thread-name", "Stellar Backend Executor #%d");
    }
}
