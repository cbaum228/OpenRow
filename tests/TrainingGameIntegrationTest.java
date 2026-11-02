package org.openrow.probe;

import java.util.Properties;
import static org.openrow.probe.TrainingCore.*;

/** The same workout survives game checkpoints and finishes independently of simulation progress. */
public final class TrainingGameIntegrationTest {
    private static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
    public static void main(String[] args){
        Plan plan=assessment(Kind.STARTER,Category.REPEATABILITY,Goal.GENERAL_CARDIO);
        Runner workout=new Runner("integration",plan,1000,false);
        GravityWellGame game=new GravityWellGame();game.start(0);workout.resume(0);
        int recoveryFrames=0;
        for(int second=1;second<=plan.seconds();second++){
            boolean moving=workout.current().phase!=Phase.RECOVERY;
            if(moving){workout.observeDistance(2);game.input(2,second%3==0?1:0,40);}
            workout.tick(second*1000L,moving,true);
            // Render/simulation time can lag the workout's independent monotonic schedule.
            game.advance(.1,.4,true,moving);
            GameWorkoutAdapter.Frame frame=new GameWorkoutAdapter.Frame(workout);
            if(frame.phase==Phase.RECOVERY)recoveryFrames++;
            if(second==400){
                Properties gameSave=new Properties();game.save(gameSave);
                Properties trainingSave=workout.save();double distance=workout.distance;
                game=GravityWellGame.restore(gameSave);workout=Runner.restore(trainingSave);
                workout.tick(90000000,false,false);
                check(workout.paused&&workout.distance==distance,"Restart cannot earn exercise or game progress offline");
                workout.resume(400000);
            }
        }
        check(recoveryFrames>0,"Game receives timed recovery without requiring rowing");
        check(workout.finished&&game.running,"Training completion does not depend on game timer or victory");
        check(game.sessionSeconds<workout.elapsed,"Game simulation is not the exercise clock");
        game.finish();check(!game.running&&workout.outcome==Outcome.COMPLETED,"Adapter can save unfinished gameplay at workout completion");
        check(workout.distance==game.sessionDistance,"Shared session tracks the same real input without multiplying it");
        check(workout.plan.seconds()==780,"Checkpoint retains the original protocol");
        System.out.println("PASS: 7 training/game integration checks");
    }
}
