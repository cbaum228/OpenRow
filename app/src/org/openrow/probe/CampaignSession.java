package org.openrow.probe;

import java.io.*;
import java.util.Properties;

/** Four-fort session plan and progress. No dependency on Android, USB, or wall-clock time. */
public final class CampaignSession {
    public enum Phase { PLAYING, REST, COMPLETE }
    public static final double BASE_ROUTE_METERS=190;
    public static final class Plan {
        public final boolean byTime;
        public final int meters,minutes,paceSeconds,restSeconds;
        public Plan(boolean byTime,int meters,int minutes,int paceSeconds,int restSeconds) {
            if(meters<100 || meters>20000 || minutes<5 || minutes>90 || paceSeconds<90 || paceSeconds>300 || restSeconds<0 || restSeconds>120)
                throw new IllegalArgumentException("Invalid campaign plan");
            this.byTime=byTime;this.meters=meters;this.minutes=minutes;this.paceSeconds=paceSeconds;this.restSeconds=restSeconds;
        }
        public int plannedMeters(){return byTime?(int)(Math.round(minutes*60.0*500/paceSeconds/10)*10):meters;}
        public double estimatedRowingMinutes(){return plannedMeters()*paceSeconds/500.0/60;}
        public double chargeScale(){return Math.ceil(plannedMeters()*10.0/BASE_ROUTE_METERS)/10.0;}
    }
    public final Plan plan;
    public final boolean simulated;
    public Phase phase=Phase.PLAYING;
    public int completedForts,shots;
    public double distanceMeters;
    public long activeMillis,restMillis;
    public CampaignSession(Plan plan,boolean simulated){if(plan==null)throw new IllegalArgumentException("Plan required");this.plan=plan;this.simulated=simulated;}
    public int currentFort(){return Math.min(completedForts,3);}
    public void distance(double meters){if(phase==Phase.PLAYING && Double.isFinite(meters) && meters>0)distanceMeters+=meters;}
    public void shot(){if(phase==Phase.PLAYING)shots++;}
    public void tick(long millis){
        if(millis<0)return;
        if(phase==Phase.PLAYING)activeMillis+=millis;
        else if(phase==Phase.REST)restMillis=Math.max(0,restMillis-millis);
    }
    public boolean captureFort(int fort){
        if(phase!=Phase.PLAYING || fort!=completedForts)return false;
        completedForts++;
        phase=completedForts==4?Phase.COMPLETE:Phase.REST;
        restMillis=phase==Phase.REST?plan.restSeconds*1000L:0;
        return true;
    }
    public boolean continueAfterRest(boolean skip){
        if(phase!=Phase.REST || (!skip && restMillis>0))return false;
        restMillis=0;phase=Phase.PLAYING;return true;
    }
    /** Used only at stage boundaries; a restored PLAYING snapshot starts that fort fresh. */
    public String checkpoint() {
        Properties p=new Properties();
        p.setProperty("version","1");p.setProperty("time",Boolean.toString(plan.byTime));p.setProperty("meters",Integer.toString(plan.meters));
        p.setProperty("minutes",Integer.toString(plan.minutes));p.setProperty("pace",Integer.toString(plan.paceSeconds));p.setProperty("rest",Integer.toString(plan.restSeconds));
        p.setProperty("simulated",Boolean.toString(simulated));p.setProperty("phase",phase.name());p.setProperty("forts",Integer.toString(completedForts));
        p.setProperty("shots",Integer.toString(shots));p.setProperty("distance",Double.toString(distanceMeters));p.setProperty("active",Long.toString(activeMillis));p.setProperty("restLeft",Long.toString(restMillis));
        try {StringWriter out=new StringWriter();p.store(out,"OpenRow campaign checkpoint");return out.toString();}
        catch(IOException impossible){throw new IllegalStateException(impossible);}
    }
    public static CampaignSession restore(String value) {
        try {
            Properties p=new Properties();p.load(new StringReader(value));
            if(!"1".equals(p.getProperty("version")))return null;
            if(!validBoolean(p.getProperty("time")) || !validBoolean(p.getProperty("simulated")))return null;
            Plan plan=new Plan(Boolean.parseBoolean(p.getProperty("time")),Integer.parseInt(p.getProperty("meters")),Integer.parseInt(p.getProperty("minutes")),Integer.parseInt(p.getProperty("pace")),Integer.parseInt(p.getProperty("rest")));
            CampaignSession s=new CampaignSession(plan,Boolean.parseBoolean(p.getProperty("simulated")));
            s.phase=Phase.valueOf(p.getProperty("phase"));s.completedForts=Integer.parseInt(p.getProperty("forts"));s.shots=Integer.parseInt(p.getProperty("shots"));
            s.distanceMeters=Double.parseDouble(p.getProperty("distance"));s.activeMillis=Long.parseLong(p.getProperty("active"));s.restMillis=Long.parseLong(p.getProperty("restLeft"));
            if(s.completedForts<0 || s.completedForts>4 || s.shots<0 || s.activeMillis<0 || s.restMillis<0 || s.restMillis>120000 || !Double.isFinite(s.distanceMeters) || s.distanceMeters<0)return null;
            if((s.phase==Phase.COMPLETE)!=(s.completedForts==4) || (s.phase==Phase.REST && s.completedForts==0) || (s.phase!=Phase.REST && s.restMillis!=0))return null;
            return s;
        } catch(Exception invalid){return null;}
    }
    private static boolean validBoolean(String value){return "true".equals(value)||"false".equals(value);}
}
