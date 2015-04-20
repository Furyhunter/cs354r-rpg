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
        long totalTimeMicroseconds;
    }

    private static Map<String, TimeTracker> timeTrackerMap = new TreeMap<>();

    public static final String RENDER_TOTAL_TIME = "renderTotalTime";
    public static final String REPLICATE_TOTAL_TIME = "replicateTotalTime";
    public static final String GAMELOGIC_TOTAL_TIME = "gamelogicTotalTime";
    public static final String STEPPABLE_TOTAL_TIME = "steppableTotalTime";

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
            t.totalTimeMicroseconds += t.stopwatch.stop().elapsed(TimeUnit.MICROSECONDS);
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
        timeTrackerMap.values().forEach(t -> t.totalTimeMicroseconds = 0);
    }
}
