package org.openrow.probe;

/** Pure rowing data model. All timestamps are monotonic milliseconds, never wall time. */
public final class RowingMetrics {
    public final S4Channels channels = new S4Channels();
    private final long[] intervals = new long[3];
    private int count, next;
    private long lastStart = -1, latestInterval;
    private int distance = -1, strokes = -1, monitorCadence = -1;
    private boolean connected;

    public void reset() {
        clearCadence(); channels.reset(); distance = strokes = monitorCadence = -1; connected = false;
    }
    private void clearCadence() { count = next = 0; lastStart = -1; latestInterval = 0; }
    public void setConnected(boolean value) { connected = value; clearCadence(); channels.disconnected(); }
    public boolean isConnected() { return connected; }
    public int distance() { return distance; }
    public int strokes() { return strokes; }
    public int monitorCadence() { return monitorCadence; }
    public boolean accept(String line, long now) {
        if (!connected) return false;
        boolean started=line.equals("SS")&&strokeStart(now);
        channels.accept(line,now,started);
        if (line.matches("IDD055[0-9A-Fa-f]{4}") || (channels.get("055")==null&&line.matches("IDD057[0-9A-Fa-f]{4}"))) distance = Integer.parseInt(line.substring(6),16);
        else if (line.matches("IDD140[0-9A-Fa-f]{4}")) {
            int value = Integer.parseInt(line.substring(6),16);
            if (strokes >= 0 && value < strokes) clearCadence(); // Monitor reset or rollover.
            strokes = value;
        } else if (line.matches("IDS1A9[0-9A-Fa-f]{2}")) monitorCadence = Integer.parseInt(line.substring(6),16);
        return started;
    }
    private boolean strokeStart(long now) {
        if (lastStart >= 0 && now == lastStart) return false;
        if (lastStart < 0 || now < lastStart || now-lastStart >= idleAfter()) {
            clearCadence(); lastStart = now; return true;
        }
        long interval = now-lastStart;
        // Reject duplicate/bounced start events without moving the last accepted timestamp.
        if (interval < 500) return false;
        lastStart = now; latestInterval = interval;
        intervals[next] = interval; next = (next+1)%intervals.length;
        count = Math.min(count+1,intervals.length);
        return true;
    }
    public long idleAfter() {
        if (count == 0) return 15000;
        return Math.max(6000,Math.min(15000,Math.round(meanInterval()*2.5)));
    }
    private double meanInterval() {
        long sum=0; for(int i=0;i<count;i++) sum+=intervals[i];
        return count == 0 ? 0 : (double)sum/count;
    }
    public boolean isIdle(long now) { return connected && lastStart >= 0 && now-lastStart >= idleAfter(); }
    public double cadence(long now) { return valid(now) ? 60000.0/meanInterval() : Double.NaN; }
    public double immediateCadence(long now) { return valid(now) ? 60000.0/latestInterval : Double.NaN; }
    private boolean valid(long now) { return connected && count>0 && !isIdle(now); }
    public String cadenceState(long now) {
        if (!connected) return "Connect to begin";
        if (isIdle(now)) return "Paused · waiting for a new stroke";
        if (lastStart < 0) return "Waiting for your first stroke";
        if (count == 0) return "One stroke received · take another";
        return "Average of last " + count + (count == 1 ? " interval" : " intervals");
    }
}
