package org.openrow.probe;

import java.util.*;

/** Authoritative real-workout accounting, independent of games and Android drawing. */
public final class WorkoutSession {
    public final Properties data=new Properties();
    public final List<String> pending=new ArrayList<>();
    private long last=-1,lastDistanceAt=-1,motionAt=-1,powerAt=-1,driveEnd=-1,pulseCount=-1,calorieAt=-1,sampleAt=-1,resumed=-1,powerResetAt=-1,cadenceAt=-1;
    private double baseline=Double.NaN,previousPower=Double.NaN,previousCalories=Double.NaN;
    public RowingTelemetryFrame latest;
    public int block=-1;public String phase="WORK";
    public WorkoutSession(String id,String origin,long started){
        if(!validId(id)||started<0)throw new IllegalArgumentException("Invalid workout");
        data.setProperty("version","1");data.setProperty("id",id);data.setProperty("origin",origin);data.setProperty("started",""+started);data.setProperty("paused","true");
    }
    public WorkoutSession(Properties saved){data.putAll(saved);if(!"1".equals(data.getProperty("version"))||!validId(id()))throw new IllegalArgumentException("Invalid workout save");
        for(String k:data.stringPropertyNames())if(k.startsWith("athlete.")||k.startsWith("credited.")||k.startsWith("block.")||k.equals("elapsed")||k.equals("bytes"))number(k);
        data.setProperty("paused","true");
    }
    public static boolean validId(String id){return id!=null&&id.matches("[A-Za-z0-9_-]{1,80}");}
    public String id(){return data.getProperty("id");}
    public boolean finished(){return Boolean.parseBoolean(data.getProperty("finished","false"));}
    public boolean paused(){return Boolean.parseBoolean(data.getProperty("paused","true"));}
    public double number(String key){double n=Double.parseDouble(data.getProperty(key,"0"));if(!Double.isFinite(n)||n<0)throw new IllegalArgumentException("Invalid workout metric "+key);return n;}
    public double total(String metric){return number("athlete."+metric);}
    private void add(String key,double n){if(Double.isFinite(n)&&n>=0)data.setProperty(key,""+(number(key)+n));}
    private void metric(String key,double n){add("athlete."+key,n);if(block>=0)add("block."+block+"."+key,n);}
    private void peak(String key,double n){if(Double.isFinite(n)&&n>=0)data.setProperty(key,""+Math.max(number(key),n));}
    public void resetBaselines(){last=lastDistanceAt=motionAt=powerAt=driveEnd=pulseCount=calorieAt=-1;baseline=previousPower=previousCalories=Double.NaN;}
    public void resume(long now){if(!finished()){resetBaselines();data.setProperty("paused","false");last=resumed=powerResetAt=now;}}
    public void pause(long now){boolean wasPaused=paused();tick(now);data.setProperty("paused","true");resetBaselines();if(!wasPaused){sampleAt=-1;sample(now);}}
    public void finish(long now,long wall){pause(now);data.setProperty("finished","true");data.setProperty("ended",""+wall);if(total("distance")>0||total("strokes")>0)data.setProperty("athlete.workouts","1");}
    public boolean moving(long now){return !paused()&&!finished()&&latest!=null&&latest.distanceAvailable()&&now-latest.distanceAt<=5000&&motionAt>=0&&now>=motionAt&&now-motionAt<1600;}
    public double observedSpeed(){return number("live.speed");}
    public void tick(long now){
        if(last>=0&&now>last&&!paused()&&!finished()){
            // No extrapolation over a suspended scheduler or stale distance channel.
            double seconds=Math.min(1,(now-last)/1000.0);add("elapsed",seconds);
            if(moving(now))metric("motionSeconds",seconds);
        }last=now;
    }
    public void accept(RowingTelemetryFrame f,boolean stroke){
        tick(f.at);latest=f;
        if(!f.connected){if(!paused())pause(f.at);return;}
        if(paused()||finished()){resetBaselines();return;}
        if(stroke)metric("strokes",1);
        if(f.distanceAvailable()&&f.distanceAt!=lastDistanceAt){
            if(lastDistanceAt>=0&&f.distanceAt>lastDistanceAt&&f.distanceAt-lastDistanceAt<=5000&&Double.isFinite(baseline)){
                double delta=f.distance-baseline,seconds=(f.distanceAt-lastDistanceAt)/1000.0;
                if(delta>0&&delta<=Math.max(20,seconds*15)){metric("distance",delta);metric("paceDistance",delta);metric("paceSeconds",seconds);motionAt=f.at;data.setProperty("live.speed",""+Math.min(6,delta/Math.max(.25,seconds)));}
            }baseline=f.distance;lastDistanceAt=f.distanceAt;
        }
        if(!f.powerAvailable()||!moving(f.at)){powerAt=-1;previousPower=Double.NaN;powerResetAt=f.at;}
        else if(f.powerAt>=powerResetAt&&f.powerAt!=powerAt){
            if(powerAt>=0&&f.powerAt>powerAt&&f.powerAt-powerAt<=1500){double seconds=(f.powerAt-powerAt)/1000.0;metric("energyJ",(f.watts+previousPower)*.5*seconds);metric("powerSeconds",seconds);}
            peak("peakPower",f.watts);powerAt=f.powerAt;previousPower=f.watts;
        }
        if(moving(f.at)){
            if(Double.isFinite(f.speed)){peak("peakSpeed",f.speed);}
            if(Double.isFinite(f.cadence)&&f.cadence>0&&f.cadence<=150){ // once per second, not per USB packet
                if(cadenceAt<0||f.at-cadenceAt>=1000){metric("cadenceSum",f.cadence);metric("cadenceCount",1);cadenceAt=f.at;}
            }
            if(f.driveEnd>driveEnd&&Double.isFinite(f.drive)&&f.driveEnd-f.drive*1000>=resumed){metric("driveSeconds",f.drive);metric("driveCount",1);if(Double.isFinite(f.recovery)&&f.driveEnd-(f.drive+f.recovery)*1000>=resumed){metric("recoverySeconds",f.recovery);metric("recoveryCount",1);}}
            if(pulseCount>=0&&f.pulses>=pulseCount)metric("pulses",f.pulses-pulseCount);
            if(Double.isFinite(f.calories))data.setProperty("caloriesObserved","true");
            if(calorieAt>=0&&f.at>calorieAt&&f.at-calorieAt<=10000&&Double.isFinite(f.calories)&&Double.isFinite(previousCalories)){double delta=f.calories-previousCalories;if(delta>=0&&delta<=(f.at-calorieAt)/1000.0*2)metric("estimatedCalories",delta);}
        }
        driveEnd=f.driveEnd;pulseCount=f.pulses;
        if(Double.isFinite(f.calories)&&f.calories!=previousCalories){previousCalories=f.calories;calorieAt=f.at;}
    }
    public void sample(long now){
        if(sampleAt>=0&&now-sampleAt<1000)return;sampleAt=now;
        boolean valid=!paused()&&!finished()&&latest!=null&&latest.distanceAvailable(),moving=moving(now);
        pending.add(String.format(Locale.US,"%.3f,%d,%s,%s,%s,%s,%s,%s,%s,%s",number("elapsed"),block,phase,paused()?"paused":moving?"rowing":"idle",valid&&latest.powerAvailable()?Double.toString(latest.watts):"",valid&&moving?Double.toString(observedSpeed()):"",valid&&Double.isFinite(latest.cadence)?Double.toString(latest.cadence):"",Double.toString(total("distance")),Double.toString(total("strokes")),Long.toString(now)));
    }
    public Properties credit(){Properties p=new Properties();p.putAll(data);for(String m:AthleteHistory.METRICS)p.setProperty("athlete."+m,""+Math.max(0,total(m)-number("credited."+m)));return p;}
    public void migrate(Properties legacy){
        String[] names={"distance","strokes","motionSeconds"},keys={"game.v4","game.counts","game.sessionMotion"};
        for(int i=0;i<names.length;i++){String v=legacy.getProperty(keys[i],"0");if(i==1){String[] counts=v.split(",");v=counts.length>1?counts[1]:"0";}double n=Double.parseDouble(v);if(n<0||!Double.isFinite(n))throw new IllegalArgumentException("Invalid legacy workout");data.setProperty("athlete."+names[i],""+n);}
        for(String m:new String[]{"energyJ","powerSeconds","estimatedCalories","pulses","driveSeconds","driveCount"})data.setProperty("athlete."+m,legacy.getProperty("session.stats."+m,"0"));
        for(String m:AthleteHistory.METRICS)data.setProperty("credited."+m,""+total(m));
        data.setProperty("elapsed",legacy.getProperty("game.v6","0"));data.setProperty("legacyPartial","true");
    }
}
