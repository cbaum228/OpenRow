package org.openrow.probe.telemetrytests;

import android.app.Instrumentation;
import android.os.Bundle;
import android.os.SystemClock;
import android.content.Context;
import org.openrow.probe.*;
import java.util.*;
import java.io.*;

/** Run only against a debug install. All exercise fixtures use fresh isolated profile IDs. */
public final class TelemetryInstrumentation extends Instrumentation {
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    int checks;void check(boolean good,String why){checks++;if(!good)throw new AssertionError(why);}
    static Object field(Object object,String name)throws Exception{java.lang.reflect.Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    static void call(Object object,String name)throws Exception{java.lang.reflect.Method m=object.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(object);}
    @Override public void onStart(){Bundle result=new Bundle();try{
        Context device=getTargetContext();ProfileStore registry=new ProfileStore(device);FamilyProfiles family=registry.read();
        String qa=family.add("Telemetry QA"),other=family.add("Other QA"),migration=family.add("Migration QA");
        Context context=new ProfileContext(device,qa);WorkoutSessionController controller=new WorkoutSessionController(context);
        check(controller.start("Standalone rowing",null),"start");String id=controller.active.id();long start=SystemClock.elapsedRealtime();controller.active.resume(start);
        for(int i=0;i<=240;i++){long at=start+i*500;double watts=i<80?100:i<160?200:150;RowingTelemetryFrame frame=new RowingTelemetryFrame(at,true,at,i,at,watts,2,24,-1,Double.NaN,Double.NaN,0,Double.NaN,"S4 estimated watts");controller.observe(frame,i%5==0);controller.active.sample(at);}
        controller.save();check(controller.error==null,"checkpoint");check(controller.active.total("distance")==240,"distance");double energy=controller.active.total("energyJ");check(energy>17000&&energy<19000,"energy");
        WorkoutSessionController restored=new WorkoutSessionController(context);check(restored.hasActive()&&restored.active.paused(),"restart pause");check(restored.active.id().equals(id),"stable identity");
        restored.finish();check(!restored.hasActive(),"finish");check(restored.store.history().size()==1,"history");
        AthleteHistory before=AthleteStore.read(context);double total=before.value("total.distance");new WorkoutSessionController(context);check(AthleteStore.read(context).value("total.distance")==total,"receipt replay");check(before.value("total.workouts")==1,"one workout");
        WorkoutSessionController independent=new WorkoutSessionController(new ProfileContext(device,other));check(!independent.hasActive()&&independent.store.history().isEmpty(),"profile isolation");
        Context oldContext=new ProfileContext(device,migration);Properties old=new Properties();old.setProperty("game.running","true");old.setProperty("game.v4","42");old.setProperty("game.counts","7,7");old.setProperty("game.sessionMotion","12");old.setProperty("game.v6","15");old.setProperty("session.id","migrating-row");old.setProperty("athlete.distance","42");old.setProperty("athlete.strokes","7");old.setProperty("athlete.motionSeconds","12");
        WorkoutStore.atomic(new File(oldContext.getFilesDir(),"gravity-real-v1.properties"),old);WorkoutSessionController imported=new WorkoutSessionController(oldContext);check(imported.hasActive(),"active migration");check(imported.active.total("distance")==42,"legacy distance");check(AthleteStore.read(oldContext).value("total.distance")==42,"import once");new WorkoutSessionController(oldContext);check(AthleteStore.read(oldContext).value("total.distance")==42,"migration replay");
        imported.finish();check(AthleteStore.read(oldContext).value("total.workouts")==1,"legacy finish count");check(AthleteStore.read(oldContext).value("total.distance")==42,"legacy finish no double distance");
        family.configure(qa,"Telemetry QA");family.configure(other,"Other QA");family.configure(migration,"Migration QA");family.select(qa);registry.save(family);
        android.content.Intent launch=new android.content.Intent().setClassName("org.openrow.probe","org.openrow.probe.MainActivity").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        final android.app.Activity activity=startActivitySync(launch);
        runOnMainSync(()->{try{
            WorkoutSessionController uiController=(WorkoutSessionController)field(activity,"workouts");GravityWellView view=(GravityWellView)field(activity,"gravityWell");RowingMetrics metrics=(RowingMetrics)field(activity,"metrics");
            call(activity,"showStandalone");check(uiController.start("Standalone rowing","guided-native"),"standalone start");
            TrainingController trainer=(TrainingController)field(view,"training");TrainingStore training=(TrainingStore)field(trainer,"store");
            TrainingCore.Plan plan=new TrainingCore.Plan("native-guided","Fixture",TrainingCore.Kind.TRAINING,TrainingCore.Category.ENDURANCE,TrainingCore.Goal.ENDURANCE,Arrays.asList(new TrainingCore.Block(TrainingCore.Phase.WARM_UP,TrainingCore.Category.ENDURANCE,5,2),new TrainingCore.Block(TrainingCore.Phase.WORK,TrainingCore.Category.ENDURANCE,10,3),new TrainingCore.Block(TrainingCore.Phase.COOL_DOWN,TrainingCore.Category.ENDURANCE,5,2)),true);
            training.active=new TrainingCore.Runner("guided-native",plan,System.currentTimeMillis(),false);((Properties)field(view,"ledger")).setProperty("training.session","guided-native");
            long now=SystemClock.elapsedRealtime();metrics.setConnected(true);metrics.accept("IV40210",now);metrics.accept("IDD0550000",now);view.rowerState(true);uiController.observe(RowingTelemetryFrame.from(metrics,now),false);
            uiController.resume();view.standaloneActive(true);check(!training.active.paused,"guided standalone resume");
            training.active.tick(now+6000,true,true);call(trainer,"publish");check(uiController.guided.phase==TrainingCore.Phase.WORK,"guided phase routing");check(uiController.guidedBlock==1,"guided block routing");
            call(activity,"showDashboard");check(training.active.paused&&uiController.active.paused(),"home pauses trainer and recorder");
            call(activity,"showStandalone");uiController.resume();view.standaloneActive(true);check(!training.active.paused&&!uiController.active.paused(),"guided standalone explicit resume");
            view.standalone(false);view.standaloneActive(false);check(!view.isStandalone(),"background callback retains original game surface");view.standalone(true);
            call(activity,"showDashboard");uiController.finish();training.active=null;training.save();activity.finish();
        }catch(Exception bad){throw new RuntimeException(bad);}});
        result.putString("stream","PASS: "+checks+" Android recorder, migration and profile checks\nQA="+qa+"\nOTHER="+other+"\nMIGRATION="+migration+"\n");finish(-1,result);
    }catch(Throwable bad){result.putString("stream",android.util.Log.getStackTraceString(bad));finish(0,result);}}
}
