package org.openrow.probe;

import java.util.Properties;

/** Shared athlete totals, imported idempotently from cumulative real-source counters. */
public final class AthleteHistory {
    public final Properties data=new Properties();
    public static final String[] METRICS={"distance","strokes","motionSeconds","workouts","energyJ","powerSeconds","estimatedCalories","pulses","driveSeconds","driveCount"};
    public static final String[][] BADGES={
        {"first","First row","Finish your first real workout","workouts","1"},
        {"km1","First kilometre","Row 1,000 real meters","distance","1000"},
        {"km10","River explorer","Row 10,000 real meters","distance","10000"},
        {"stroke1000","A thousand strokes","Take 1,000 real strokes","strokes","1000"},
        {"hour","An hour in motion","Accumulate 60 minutes of rowing motion","motionSeconds","3600"},
        {"row10","Building a rhythm","Finish 10 real workouts","workouts","10"}
    };
    public AthleteHistory(){data.setProperty("version","1");}
    public AthleteHistory(Properties saved){if(!"1".equals(saved.getProperty("version")))throw new IllegalArgumentException("Unknown athlete history");data.putAll(saved);}
    public double value(String key){return read(data,key);}
    private static double read(Properties p,String key){double n=Double.parseDouble(p.getProperty(key,"0"));if(!Double.isFinite(n)||n<0)throw new IllegalArgumentException("Invalid athlete metric");return n;}
    public void sync(Properties source,boolean simulated){
        if(simulated)return;
        for(String metric:METRICS){double next=read(source,"athlete."+metric),seen=value("seen.gravity."+metric);
            // A source rollback must not recreate already credited work.
            if(next>seen){data.setProperty("total."+metric,Double.toString(value("total."+metric)+next-seen));data.setProperty("seen.gravity."+metric,Double.toString(next));}}
        String id=source.getProperty("last.id");
        if(id!=null&&!id.equals(data.getProperty("seen.gravity.last",data.getProperty("last.id")))){
            for(String key:new java.util.HashSet<String>(data.stringPropertyNames()))if(key.startsWith("last."))data.remove(key);
            for(String key:source.stringPropertyNames())if(key.startsWith("last."))data.setProperty(key,source.getProperty(key));
            data.setProperty("last.game","Gravity Well");data.setProperty("seen.gravity.last",id);
        }
        for(String[] badge:BADGES)if(value("total."+badge[3])>=Double.parseDouble(badge[4]))data.setProperty("badge."+badge[0],"true");
    }
    public boolean earned(String id){return Boolean.parseBoolean(data.getProperty("badge."+id,"false"));}
    public void syncSession(Properties session){
        String id=session.getProperty("id");if(!WorkoutSession.validId(id))throw new IllegalArgumentException("Invalid receipt");
        for(String metric:METRICS){double next=read(session,"athlete."+metric),seen=value("seen.session."+id+"."+metric);
            if(next>seen){data.setProperty("total."+metric,""+(value("total."+metric)+next-seen));data.setProperty("seen.session."+id+"."+metric,""+next);}}
        if("true".equals(session.getProperty("finished"))&&read(session,"ended")>=value("last.ended")&&!id.equals(data.getProperty("seen.session.last"))&&read(session,"athlete.workouts")>0){
            for(String key:new java.util.HashSet<String>(data.stringPropertyNames()))if(key.startsWith("last."))data.remove(key);
            data.setProperty("last.id",id);data.setProperty("last.ended",session.getProperty("ended"));data.setProperty("last.game",session.getProperty("origin","Rowing"));
            for(String metric:METRICS)data.setProperty("last."+(metric.equals("motionSeconds")?"seconds":metric),session.getProperty("full."+metric,session.getProperty("athlete."+metric,"0")));
            for(String metric:new String[]{"peakPower","peakSpeed"})data.setProperty("last."+metric,session.getProperty(metric,"0"));
            data.setProperty("last.sensor","S4 USB · estimated watts and provisional calories");data.setProperty("seen.session.last",id);
        }
        for(String[] badge:BADGES)if(value("total."+badge[3])>=Double.parseDouble(badge[4]))data.setProperty("badge."+badge[0],"true");
    }
    public void resetTotals(long now){
        if(data.containsKey("last.id"))data.setProperty("seen.gravity.last",data.getProperty("last.id"));
        for(String key:new java.util.HashSet<String>(data.stringPropertyNames()))
            if(key.startsWith("badge.")||key.startsWith("trophy.")||key.startsWith("milestone.")||key.startsWith("last."))data.remove(key);
        for(String metric:METRICS)data.setProperty("total."+metric,"0");data.setProperty("resetAt",Long.toString(now));
    }
    public static final class Hold {
        private long started=-1; private boolean fired;
        public void start(long now){started=now;fired=false;}
        public void cancel(){started=-1;fired=false;}
        public double progress(long now){return started<0?0:Math.max(0,Math.min(1,(now-started)/5000.0));}
        public boolean complete(long now){if(started<0||fired||now-started<5000)return false;fired=true;return true;}
    }
}
