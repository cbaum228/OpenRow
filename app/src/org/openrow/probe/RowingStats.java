package org.openrow.probe;

import java.util.Properties;

/** Additional athlete observations, independent of game rewards. No extrapolation across gaps. */
public final class RowingStats {
    private long powerAt=-1,driveStart=-1;
    private long calorieAt=-1,lastDriveEnd=-1;
    private double power=Double.NaN,calories=Double.NaN;
    public void interrupt(){powerAt=calorieAt=lastDriveEnd=driveStart=-1;power=calories=Double.NaN;}
    private double n(Properties p,String key){return Double.parseDouble(p.getProperty(key,"0"));}
    private void add(Properties p,String name,double v){if(v<0||!Double.isFinite(v))return;for(String prefix:new String[]{"athlete.","session.stats."})p.setProperty(prefix+name,Double.toString(n(p,prefix+name)+v));}
    private void peak(Properties p,String name,double v){if(Double.isFinite(v)&&v>=0)p.setProperty("session.stats."+name,Double.toString(Math.max(n(p,"session.stats."+name),v)));}
    public void begin(Properties p){for(String k:new java.util.HashSet<String>(p.stringPropertyNames()))if(k.startsWith("session.stats."))p.remove(k);interrupt();}
    public void accept(S4Channels c,String line,long now,Properties p,boolean eligible){
        if(!eligible){interrupt();return;}
        if(line.equals("SS"))driveStart=c.lastStart();
        if(!Double.isFinite(c.watts(now))){powerAt=-1;power=Double.NaN;}
        if(line.matches("IDD088[0-9A-Fa-f]{4}")&&c.get("088")!=null&&c.get("088").at==now){
            double w=c.watts(now);
            if(Double.isFinite(w)){
                // Trapezoidal integration of consecutive current samples, at most 1.5 s apart.
                if(powerAt>=0&&now>powerAt&&now-powerAt<=1500&&Double.isFinite(power)){
                    double seconds=(now-powerAt)/1000.0;add(p,"energyJ",(w+power)*.5*seconds);add(p,"powerSeconds",seconds);
                }
                peak(p,"peakPower",w);powerAt=now;power=w;
            }else{powerAt=-1;power=Double.NaN;}
        }
        if(line.matches("IDT08A[0-9A-Fa-f]{6}")&&c.get("08A")!=null&&c.get("08A").at==now){
            double next=c.calories(now);if(Double.isFinite(next)&&Double.isFinite(calories)&&calorieAt>=0&&now>calorieAt&&now-calorieAt<=10000&&next>=calories&&next-calories<=(now-calorieAt)/1000.0*2)add(p,"estimatedCalories",next-calories);calories=next;calorieAt=now;
        }
        if(line.matches("P[0-9A-Fa-f]{2}")){
            add(p,"pulses",c.get("pulse").raw);
        }
        if(line.startsWith("IRS"))return; // Queries are never observations.
        peak(p,"peakHeartRate",c.heartRate(now));
        peak(p,"peakSpeed",c.speed(now));
        if(line.equals("SE")&&driveStart>=0&&driveStart==c.lastStart()&&Double.isFinite(c.driveSeconds())&&c.lastEnd()>lastDriveEnd){add(p,"driveSeconds",c.driveSeconds());add(p,"driveCount",1);lastDriveEnd=c.lastEnd();}
    }
    public void complete(Properties p){
        for(String name:new String[]{"energyJ","powerSeconds","estimatedCalories","pulses","driveSeconds","driveCount","peakPower","peakHeartRate","peakSpeed"})p.setProperty("last."+name,p.getProperty("session.stats."+name,"0"));
        p.setProperty("last.sensor","S4 USB · estimated power/calories · provisional units");
    }
}
