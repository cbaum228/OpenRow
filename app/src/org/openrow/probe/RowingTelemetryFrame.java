package org.openrow.probe;

/** Immutable observations. NaN is missing, never a fabricated zero. No game state. */
public final class RowingTelemetryFrame {
    public final long at,distanceAt,powerAt,driveEnd,pulses;
    public final boolean connected;
    public final double distance,watts,speed,cadence,drive,recovery,calories;
    public final String powerStatus;
    public RowingTelemetryFrame(long at,boolean connected,long distanceAt,double distance,long powerAt,double watts,double speed,double cadence,long driveEnd,double drive,double recovery,long pulses,double calories,String status){
        this.at=at;this.connected=connected;this.distanceAt=distanceAt;this.distance=distance;this.powerAt=powerAt;this.watts=watts;this.speed=speed;this.cadence=cadence;this.driveEnd=driveEnd;this.drive=drive;this.recovery=recovery;this.pulses=pulses;this.calories=calories;powerStatus=status;
    }
    public static RowingTelemetryFrame from(RowingMetrics m,long now){
        S4Channels c=m.channels;boolean online=m.isConnected();double w=online?c.watts(now):Double.NaN;
        String status=!online?"Disconnected":!c.knownFirmware()?"Unverified firmware":Double.isFinite(w)?"S4 estimated watts":"Watts unavailable · pace fallback";
        S4Channels.Sample d=c.get("055"),p=c.get("088");
        return new RowingTelemetryFrame(now,online,d==null?-1:d.at,online&&c.fresh("055",now,5000)?m.distance():Double.NaN,p==null?-1:p.at,w,online?c.speed(now):Double.NaN,online?m.cadence(now):Double.NaN,c.lastEnd(),c.driveSeconds(),c.recoverySeconds(),c.pulses(),online?c.calories(now):Double.NaN,status);
    }
    public boolean distanceAvailable(){return connected&&Double.isFinite(distance)&&distanceAt>=0&&at>=distanceAt&&at-distanceAt<=5000;}
    public boolean powerAvailable(){return connected&&Double.isFinite(watts)&&watts>=0&&watts<=3000&&powerAt>=0&&at>=powerAt&&at-powerAt<=1500;}
    public RowingTelemetryFrame withSpeed(double observedSpeed){return new RowingTelemetryFrame(at,connected,distanceAt,distance,powerAt,watts,observedSpeed,cadence,driveEnd,drive,recovery,pulses,calories,powerStatus);}
}
