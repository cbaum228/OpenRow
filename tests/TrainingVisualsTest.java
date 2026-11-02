package org.openrow.probe;

import java.util.*;
import static org.openrow.probe.TrainingCore.*;
import org.openrow.probe.TrainingCore.Record;

/** Real trainer frames and histories drive visual calibration, never exercise targets or economy. */
public final class TrainingVisualsTest {
    private static final long NOW=1000*DAY;
    private static int checks;
    private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    private static void near(double a,double b,String why){check(Math.abs(a-b)<.00001,why+" "+a+" / "+b);}
    private static Plan plan(Phase phase,int effort){return new Plan("visual-observation","fixture",Kind.STARTER,Category.ENDURANCE,Goal.ENDURANCE,Arrays.asList(new Block(phase,Category.ENDURANCE,120,effort)),false);}
    private static Runner run(double speed,long started,boolean simulated){Runner r=new Runner("row"+started,plan(Phase.WORK,3),started,simulated);r.resume(0);for(int i=1;i<=120;i++){r.observeDistance(speed);r.tick(i*1000L,true,true);}return r;}
    private static Record observation(double speed,long age){Runner r=run(speed,NOW-age,false);return new Record(r,r.started+120000,3,1);}
    private static GameWorkoutAdapter.Frame frame(Phase phase,int effort,TrainingVisuals.Profile profile){Runner r=new Runner("live",plan(phase,effort),NOW,false);r.resume(0);r.tick(1000,true,true);return new GameWorkoutAdapter.Frame(r,profile);}
    public static void main(String[] args){
        TrainingVisuals.Profile slow=TrainingVisuals.profile(Arrays.asList(observation(1.5,DAY)),NOW,""),fast=TrainingVisuals.profile(Arrays.asList(observation(3,DAY)),NOW,"");
        GameWorkoutAdapter.Frame slowFrame=frame(Phase.WORK,3,slow),fastFrame=frame(Phase.WORK,3,fast);
        near(slowFrame.visualReferenceSpeed,1.5,"Slower athlete retains their own observed reference");near(fastFrame.visualReferenceSpeed,3,"Faster athlete retains their own reference");
        near(TrainingVisuals.input(slowFrame,1.5,true),TrainingVisuals.input(fastFrame,3,true),"Equivalent personal effort gives equivalent visuals at different absolute speeds");
        near(TrainingVisuals.input(slowFrame,6,true),TrainingVisuals.input(slowFrame,1.5,true),"Going beyond the guided reference cannot unlock more brightness");
        check(TrainingVisuals.input(fastFrame,1.5,true)<TrainingVisuals.input(fastFrame,3,true),"Sub-reference motion still modulates known athlete visuals");
        for(Phase phase:Phase.values())for(int effort=1;effort<=6;effort++){
            GameWorkoutAdapter.Frame f=frame(phase,effort,slow);double level=TrainingVisuals.input(f,6,true);
            check(level>=0&&level<=TrainingVisuals.ceiling(f),"Every workout phase respects its visual ceiling");
            if(phase==Phase.RECOVERY)check(level<=.22,"Recovery stays quiet even at very high observed speed");
            if(phase==Phase.WARM_UP)check(level<=.35,"Warm-up remains gentle");
            if(phase==Phase.COOL_DOWN)check(level<=.25,"Cool-down remains gentle");
        }
        near(TrainingVisuals.input(slowFrame,1.5,false),0,"Stationary athlete cannot create active visuals");near(TrainingVisuals.input(slowFrame,Double.NaN,true),0,"Bad speed is ignored");
        Runner interrupted=new Runner("interrupt",plan(Phase.WORK,3),NOW,false);interrupted.resume(0);interrupted.tick(1000,true,false);
        near(TrainingVisuals.input(new GameWorkoutAdapter.Frame(interrupted,slow),1.5,true),0,"Missing telemetry cannot drive active effects");interrupted.pause(1000);
        near(TrainingVisuals.input(new GameWorkoutAdapter.Frame(interrupted,slow),1.5,true),0,"Paused training cannot request effects");
        Runner reduced=new Runner("reduced",plan(Phase.WORK,4),NOW,false);reduced.resume(0);reduced.tick(1000,true,true);double original=TrainingVisuals.ceiling(new GameWorkoutAdapter.Frame(reduced,slow));reduced.easier();
        check(TrainingVisuals.ceiling(new GameWorkoutAdapter.Frame(reduced,slow))<original,"An easier trainer adjustment lowers the visual ceiling");
        List<Record> unsuitable=new ArrayList<Record>();
        Runner simulated=run(5,NOW-DAY,true);unsuitable.add(new Record(simulated,simulated.started+120000,3,1));
        Runner adjusted=run(5,NOW-2*DAY,false);adjusted.adjusted=true;unsuitable.add(new Record(adjusted,adjusted.started+120000,3,1));
        Runner broken=run(5,NOW-3*DAY,false);broken.interruptions=1;unsuitable.add(new Record(broken,broken.started+120000,3,1));
        Runner missing=run(5,NOW-4*DAY,false);unsuitable.add(new Record(missing,missing.started+120000,-1,-1));
        Runner difficult=run(5,NOW-5*DAY,false);unsuitable.add(new Record(difficult,difficult.started+120000,8,0));
        unsuitable.add(observation(5,16*DAY));unsuitable.add(observation(5,-DAY));
        TrainingVisuals.Profile unknown=TrainingVisuals.profile(unsuitable,NOW,"");near(unknown.speed(Category.ENDURANCE,3),0,"Simulated, stale, future, difficult, adjusted, interrupted and unanswered evidence cannot calibrate visuals");
        GameWorkoutAdapter.Frame provisional=frame(Phase.WORK,3,unknown);near(TrainingVisuals.input(provisional,.5,true),TrainingVisuals.input(provisional,5,true),"Unknown ability uses the workout phase without pretending speed measures personal effort");
        near(slow.speed(Category.SUSTAINED,3),0,"An untested category remains unknown");near(slow.speed(Category.ENDURANCE,5),0,"No unobserved high-effort reference is invented");
        Record excluded=observation(4,DAY);near(TrainingVisuals.profile(Arrays.asList(excluded),NOW,excluded.id).speed(Category.ENDURANCE,3),0,"A session cannot calibrate itself");
        List<Record> robust=Arrays.asList(observation(1.5,DAY),observation(1.5,2*DAY),observation(5.9,3*DAY));
        near(TrainingVisuals.profile(robust,NOW,"").speed(Category.ENDURANCE,3),1.5,"Median evidence resists a single extreme observation");
        near(TrainingVisuals.freeInput(slow,1.5,true),TrainingVisuals.freeInput(fast,3,true),"Free rowing also uses athlete-relative references when available");near(TrainingVisuals.freeInput(unknown,5,true),.5,"Uncalibrated free rowing is provisional and steady");
        GravityWellGame low=new GravityWellGame(),high=new GravityWellGame();low.start(0);high.start(0);low.spawnKind(GravityWellGame.RED_GIANT,true);high.spawnKind(GravityWellGame.RED_GIANT,true);
        for(int i=0;i<300;i++){low.input(.1,0,2);high.input(.1,0,2);low.advance(GravityWellGame.STEP,.4,true,true,.1);high.advance(GravityWellGame.STEP,.4,true,true,.9);}
        near(low.field,high.field,"Visual normalization does not alter the physical gravity input");near(low.bossMass(),high.bossMass(),"Athlete-relative effects cannot alter consumed mass");near(low.xp,high.xp,"Athlete-relative effects cannot change XP");near(low.sessionDistance,high.sessionDistance,"Measured meters are unchanged");
        check(low.visualEnergy()<high.visualEnergy(),"Artwork receives its independent visual signal");
        Properties saved=new Properties();low.save(saved);GravityWellGame restored=GravityWellGame.restore(saved);near(low.visualField(),restored.visualField(),"Smoothed visual input survives a game checkpoint");
        for(int i=0;i<30;i++){low.advance(GravityWellGame.STEP,.4,true,true,.2);restored.advance(GravityWellGame.STEP,.4,true,true,.2);}near(low.visualEnergy(),restored.visualEnergy(),"Resumed visual smoothing is deterministic");near(low.xp,restored.xp,"Visual restore cannot duplicate rewards");
        Plan mixed=new Plan("visual-integration","clock isolation",Kind.TRAINING,Category.ENDURANCE,Goal.ENDURANCE,Arrays.asList(new Block(Phase.WARM_UP,Category.ENDURANCE,30,2),new Block(Phase.WORK,Category.ENDURANCE,60,3),new Block(Phase.RECOVERY,Category.ENDURANCE,30,1),new Block(Phase.COOL_DOWN,Category.ENDURANCE,30,1)),true);
        Runner workout=new Runner("independent-clock",mixed,NOW,false);workout.resume(0);GravityWellGame game=new GravityWellGame();game.start(0);game.spawnKind(GravityWellGame.NEUTRON,true);boolean recoverySeen=false;
        for(int sec=1;sec<=mixed.seconds();sec++){GameWorkoutAdapter.Frame f=new GameWorkoutAdapter.Frame(workout,slow);boolean moving=f.phase!=Phase.RECOVERY;
            if(moving){workout.observeDistance(1.5);game.input(1.5,0,30);}workout.tick(sec*1000L,moving,true);game.advance(.1,.4,true,moving,TrainingVisuals.input(f,1.5,moving));
            if(f.phase==Phase.RECOVERY){recoverySeen=true;check(game.activeBossId>0,"Recovery proceeds with an unfinished boss");near(TrainingVisuals.input(f,6,true),TrainingVisuals.ceiling(f),"Recovery cannot be intensified through overspeed");}}
        check(recoverySeen&&workout.finished&&game.running,"The trainer completes independently of the boss and render clock");near(workout.distance,game.sessionDistance,"Trainer and game share unmultiplied real distance");
        System.out.println("PASS: "+checks+" athlete-relative visual integration checks");
    }
}
