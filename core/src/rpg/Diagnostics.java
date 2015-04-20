package rpg;

import com.google.common.base.Stopwatch;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public final class Diagnostics {

    static class TimeTracker {
        String trackingName;
        Stopwatch stopwatch;
        long lastTotalTimeMicroseconds;
        long totalTimeMicroseconds;
    }

    private static Map<String, TimeTracker> timeTrackerMap = new TreeMap<>();

    public static final String FRAME_TOTAL_TIME = "frameTotalTime";
    public static final String RENDER_TOTAL_TIME = "renderTotalTime";
    public static final String REPLICATE_TOTAL_TIME = "replicateTotalTime";
    public static final String GAMELOGIC_TOTAL_TIME = "gamelogicTotalTime";
    public static final String STEPPABLE_TOTAL_TIME = "steppableTotalTime";
    public static final String PROCESSCOMPONENTS_TOTAL_TIME = "processComponentsTotalTime";

    private Diagnostics() {

    }

    public static void beginTime(String trackingName) {
        Objects.requireNonNull(trackingName);
        TimeTracker t = timeTrackerMap.get(trackingName);
        if (t == null) {
            t = new TimeTracker();
            t.trackingName = trackingName;
            timeTrackerMap.put(trackingName, t);
        }
        if (t.stopwatch != null) {
            if (t.stopwatch.isRunning()) t.stopwatch.stop();
            t.totalTimeMicroseconds += t.stopwatch.elapsed(TimeUnit.MICROSECONDS);
        }
        t.stopwatch = Stopwatch.createStarted();
    }

    public static void endTime(String trackingName) {
        Objects.requireNonNull(trackingName);
        TimeTracker t = timeTrackerMap.get(trackingName);
        if (t == null) {
            throw new IllegalArgumentException("Tracking name " + trackingName + " doesn't have a tracker.");
        }
        if (t.stopwatch == null) {
            throw new RuntimeException();
        }
        t.totalTimeMicroseconds += t.stopwatch.stop().elapsed(TimeUnit.MICROSECONDS);
    }

    public static void resetTime(String trackingName) {
        Objects.requireNonNull(trackingName);
        TimeTracker t = timeTrackerMap.get(trackingName);
        if (t == null) {
            throw new IllegalArgumentException("Tracking name " + trackingName + " doesn't have a tracker.");
        }
        t.totalTimeMicroseconds = 0;
    }

    public static void resetTimes() {
        timeTrackerMap.values().forEach(t -> {
            t.lastTotalTimeMicroseconds = t.totalTimeMicroseconds;
            t.totalTimeMicroseconds = 0;
        });
    }

    public static long getLastTotalTime(String trackingName) {
        Objects.requireNonNull(trackingName);
        TimeTracker t = timeTrackerMap.get(trackingName);
        if (t == null) {
            return 0;
        }
        return t.lastTotalTimeMicroseconds;
    }
}
