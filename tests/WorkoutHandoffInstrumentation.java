package org.openrow.probe.telemetrytests;

import android.app.Instrumentation;
import android.os.Bundle;
import android.os.SystemClock;
import android.content.Context;
import org.openrow.probe.*;
import java.util.UUID;
import java.io.File;

/** Isolated profile fixture for starting Gravity Well before the S4 handshake completes. */
public final class WorkoutHandoffInstrumentation extends Instrumentation {
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    static Object field(Object o,String name)throws Exception{java.lang.reflect.Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    static void call(Object o,String name)throws Exception{java.lang.reflect.Method m=o.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(o);}
    static void check(boolean yes,String why){if(!yes)throw new AssertionError(why);}
    private void onUi(Runnable action){final Throwable[] failure=new Throwable[1];runOnMainSync(()->{try{action.run();}catch(Throwable bad){failure[0]=bad;}});if(failure[0]!=null)throw new RuntimeException(failure[0]);}
    private GravityWellView view;private WorkoutSessionController controller;private GravityWellGame game;
    @Override public void onStart(){Bundle result=new Bundle();int status=0;Context context=new ProfileContext(getTargetContext(),UUID.randomUUID().toString());
        try{
            onUi(()->{try{
                controller=new WorkoutSessionController(context);
                view=new GravityWellView(context,new GravityWellView.Controls(){public void back(){}public void connect(){}public void openWorkout(){}},controller);
                view.active(true);call(view,"start");game=(GravityWellGame)field(view,"game");
                check(!view.recording(),"Disconnected session is not recording");
                // Same gate as MainActivity.refreshWorkouts while the USB handshake is pending.
                if(!view.recording())controller.pause();
                view.rowerState(true);
                check(view.recording(),"Game accepts rowing after the handshake");
                check(!controller.active.paused(),"S4 handshake must resume the recorder with the game");
            }catch(Exception e){throw new RuntimeException(e);}});
            for(int i=0;i<=30;i++){final int distance=i;onUi(()->{
                long now=SystemClock.elapsedRealtime();controller.observe(new RowingTelemetryFrame(now,true,now,distance,now,100,2,24,-1,Double.NaN,Double.NaN,0,Double.NaN,"fixture"),distance%5==0);
                view.distance(distance);if(distance%5==0)view.stroke();
                game.gameplayContribution(controller.effort.contribution);game.advance(.1,.5,true,controller.active.moving(now));
            });SystemClock.sleep(100);}
            onUi(()->{
                check(controller.active.total("distance")==30,"Recorder counts observed distance");
                check(game.sessionDistance==30,"Game and recorder count the same input");
                check(game.earned>0,"Real-input campaign XP increases");
                controller.pause();view.rowerState(false);check(!view.recording(),"Disconnect stops game input");
                view.rowerState(true);check(controller.active.paused(),"Reconnect after a disconnect requires explicit resume");
                try{call(view,"resume");}catch(Exception e){throw new RuntimeException(e);}
                check(!controller.active.paused()&&view.recording(),"Explicit resume restarts both owners");
                view.active(false);controller.finish();
            });
            result.putString("stream","PASS: disconnected start, handshake, distance/strokes, campaign XP and reconnect pause\n");status=-1;
        }catch(Throwable bad){result.putString("stream",android.util.Log.getStackTraceString(bad));status=0;}
        finally{onUi(()->{if(view!=null)view.active(false);});remove(context.getFilesDir());}
        finish(status,result);
    }
    private static void remove(File f){File[] children=f.listFiles();if(children!=null)for(File child:children)remove(child);if(!f.delete()&&f.exists())throw new IllegalStateException("Fixture cleanup failed: "+f);}
}
