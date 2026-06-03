package net.sparklypower.sparklypaper;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.Pair;
import org.slf4j.Logger;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HalloweenManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(factory -> {
        Thread thread = new Thread(factory);
        thread.setName("halloween-timer-updater");
        thread.setPriority(1);
        return thread;
    });

    private static ScheduledFuture<?> future;
    private static Pair<Long, Long> spookyEpoch;
    private static Pair<Long, Long> halloweenEpoch;

    private static long getEpochMillisAtDate(Month month, int day, boolean start) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = LocalDateTime.of(
                now.getYear(), month, day, start ? 0 : 23, start ? 0 : 59, start ? 0 : 59, start ? 0 : 999_999_999
        );

        if (now.isAfter(target)) target = target.plusYears(1);
        return target.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
    }

    private static void syncEpoch() {
        LOGGER.info("Updating Spooky Season and Halloween epoch...");
        spookyEpoch = Pair.of(
                getEpochMillisAtDate(Month.OCTOBER, 20, true),
                getEpochMillisAtDate(Month.NOVEMBER, 3, false)
        );
        halloweenEpoch = Pair.of(
                getEpochMillisAtDate(Month.OCTOBER, 31, true),
                getEpochMillisAtDate(Month.OCTOBER, 31, false)
        );
        LOGGER.info("Successfully updated Spooky Season and Halloween epoch");
    }

    public static void syncConfiguration() {
        if (dev.instellar.stellar.configuration.GlobalConfiguration.get().entities.spookyOptimize && future == null) {
            startSyncEpochTask();
        } else if (!dev.instellar.stellar.configuration.GlobalConfiguration.get().entities.spookyOptimize && future != null) {
            future.cancel(true);
            future = null;
        }
    }

    public static void startSyncEpochTask() {
        if (!dev.instellar.stellar.configuration.GlobalConfiguration.get().entities.spookyOptimize) return;
        future = EXECUTOR.scheduleAtFixedRate(HalloweenManager::syncEpoch, 0, 90, TimeUnit.DAYS);
    }

    public static boolean isSpookySeason() {
        return spookyEpoch.first() <= System.currentTimeMillis() && System.currentTimeMillis() <= spookyEpoch.second();
    }

    public static boolean isHalloween() {
        return halloweenEpoch.first() <= System.currentTimeMillis() && System.currentTimeMillis() <= halloweenEpoch.second();
    }

}
