package org.openrow.probe;

import android.content.Context;
import android.os.SystemClock;
import java.io.*;
import java.util.*;
import org.openrow.probe.TrainingCore.Record;

/** One owner per immutable profile context, shared by all recording surfaces. */
public final class WorkoutSessionController {
    private final Context context;public final WorkoutStore store;
    public WorkoutSession active;public String error;public RowingTelemetryFrame telemetry;
    private RowingEffortFrame.Resolver resolver;private long saved;
    public RowingEffortFrame effort=new RowingEffortFrame(0,"Pace",true,false);
    public GameWorkoutAdapter.Frame guided;public int guidedBlock=-1;
    public WorkoutSessionController(Context context)throws IOException{
        this.context=context;store=new WorkoutStore(context.getFilesDir());
        if(!Boolean.parseBoolean(store.state.getProperty("migrated","false"))){
            AthleteStore.refresh(context);
            Properties legacy=WorkoutStore.read(new File(context.getFilesDir(),"gravity-real-v1.properties"));
            if(Boolean.parseBoolean(legacy.getProperty("game.running","false"))){
                String id=legacy.getProperty("session.id");if(!WorkoutSession.validId(id))id=UUID.nameUUIDFromBytes(("legacy-"+legacy.getProperty("game.v3","0")+"-"+legacy.getProperty("game.v4","0")).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
                WorkoutSession migrated=new WorkoutSession(id,"Gravity Well",System.currentTimeMillis());migrated.migrate(legacy);store.save(migrated);
            }
            store.state.setProperty("migrated","true");store.saveState();
        }
        active=store.active();if(active!=null&&active.finished()){AthleteStore.syncSession(context,active);active=null;store.state.remove("active");store.saveState();}
        // Finishing commits the summary before the athlete receipt. Replay repairs that boundary.
        for(Properties p:store.history())AthleteStore.syncSession(context,new WorkoutSession(p));
        if(active!=null){restoreResolver();AthleteStore.syncSession(context,active);}
    }
    public boolean hasActive(){return active!=null&&!active.finished();}
    public boolean owns(String origin){return hasActive()&&origin.equals(active.data.getProperty("origin"));}
    public boolean start(String origin,String id){
        if(error!=null)return false;
        if(hasActive()){if(!owns(origin)||id!=null&&!active.id().equals(id))return false;return true;}
        active=new WorkoutSession(id==null?UUID.randomUUID().toString():id,origin,System.currentTimeMillis());
        guided=null;guidedBlock=0;
        try{TrainingStore training=new TrainingStore(context);double pace=GameplayEffort.reference(training.history.records(),System.currentTimeMillis());double power=powerReference(store.history(),training.history.records(),System.currentTimeMillis());
            active.data.setProperty("reference.pace",""+pace);active.data.setProperty("reference.power",""+power);restoreResolver();save();return error==null;
        }catch(Exception bad){fail(bad);return false;}
    }
    private void restoreResolver(){
        // Device validation is an explicit release gate, never inferred from idle samples.
        Context device=context;while(device instanceof android.content.ContextWrapper)device=((android.content.ContextWrapper)device).getBaseContext();
        boolean validated=device.getSharedPreferences("s4_validation",Context.MODE_PRIVATE).getBoolean("normalWaterPowerValidated",false);
        resolver=new RowingEffortFrame.Resolver(active.number("reference.power"),active.number("reference.pace"),validated);
    }
    public static double powerReference(List<Properties> sessions,List<Record> records,long now){
        return RowingEffortFrame.powerReference(sessions,records,now);
    }
    private static double parse(Properties p,String key){return Double.parseDouble(p.getProperty(key,"0"));}
    public void resume(){if(hasActive()&&error==null){active.resume(SystemClock.elapsedRealtime());restoreResolver();save();}}
    public void pause(){if(hasActive()){active.pause(SystemClock.elapsedRealtime());save();}}
    public void finish(){if(hasActive()){active.finish(SystemClock.elapsedRealtime(),System.currentTimeMillis());save();if(error==null){active=null;guided=null;guidedBlock=-1;}}}
    public void observe(RowingTelemetryFrame frame,boolean stroke){telemetry=frame;if(!hasActive()||error!=null)return;
        boolean wasPaused=active.paused();active.block=guidedBlock;active.phase=guided==null?"WORK":guided.phase.name();active.accept(frame,stroke);
        effort=resolver.resolve(frame.withSpeed(active.observedSpeed()),active.moving(frame.at),guided);
        if(!wasPaused&&active.paused())save();
    }
    public void tick(RowingTelemetryFrame frame){observe(frame,false);if(!hasActive()||error!=null||active.paused())return;active.sample(frame.at);if(frame.at-saved>=3000)save();}
    public void save(){if(active==null)return;try{store.save(active);AthleteStore.syncSession(context,active);saved=SystemClock.elapsedRealtime();error=null;}catch(Exception bad){fail(bad);}}
    private void fail(Exception bad){error="Workout save failed. Recording paused; tap Retry save.";if(active!=null)active.pause(SystemClock.elapsedRealtime());}
}
