package dev.instellar.stellar.util;

import dev.instellar.stellar.Stellar;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class JvmUtil {

    public static boolean dumpHeap() {
        final Path p = Path.of(".", "dumps",
                "heap-dump-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + "-server.hprof");
        Stellar.LOGGER.info("Writing JVM heap data to: {}", p.toAbsolutePath());
        try {
            Files.createDirectories(p.getParent());

            final Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            final Object hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
            final Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
            m.invoke(hotspotMBean, p.toString(), true);
            Stellar.LOGGER.info("Heap dump complete");
            return true;
        } catch (final Throwable t) {
            Stellar.LOGGER.error("Could not write heap to {}: {}", p, t);
            return false;
        }
    }
}
