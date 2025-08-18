package io.papermc.paper.util;

import ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.concurrent.TimeUnit;

public class KeepAlive {

    public long lastKeepAliveTx = System.nanoTime();

    public static final record KeepAliveResponse(long txTimeNS, long rxTimeNS) {
        public long latencyNS() {
            return this.rxTimeNS - this.txTimeNS;
        }
    }

    public static final record PendingKeepAlive(long txTimeNS, long challengeId) {
    }

    public final MultiThreadedQueue<PendingKeepAlive> pendingKeepAlives = new MultiThreadedQueue<>();

    public final PingCalculator pingCalculator1m = new PingCalculator(TimeUnit.MINUTES.toNanos(1L));
    public final PingCalculator pingCalculator5s = new PingCalculator(TimeUnit.SECONDS.toNanos(5L));

    public static final class PingCalculator {

        private final long intervalNS;
        private final MultiThreadedQueue<KeepAliveResponse> responses = new MultiThreadedQueue<>();

        private long timeSumNS;
        private int timeSumCount;
        private volatile long lastAverageNS;

        public PingCalculator(long intervalNS) {
            this.intervalNS = intervalNS;
        }

        public void update(KeepAliveResponse response) {
            long currTime = response.txTimeNS;

            this.responses.add(response);

            ++this.timeSumCount;
            this.timeSumNS += response.latencyNS();

            // remove out-of-window times
            KeepAliveResponse removed;
            while ((removed = this.responses.pollIf((ka) -> (currTime - ka.txTimeNS) > this.intervalNS)) != null) {
                --this.timeSumCount;
                this.timeSumNS -= removed.latencyNS();
            }

            this.lastAverageNS = this.timeSumNS / (long) this.timeSumCount;
        }

        public int getAvgLatencyMS() {
            return (int) TimeUnit.NANOSECONDS.toMillis(this.getAvgLatencyNS());
        }

        public long getAvgLatencyNS() {
            return this.lastAverageNS;
        }

        public LongArrayList getAllNS() {
            final var ret = new LongArrayList();

            for (KeepAliveResponse response : this.responses) {
                ret.add(response.latencyNS());
            }

            return ret;
        }
    }
}
