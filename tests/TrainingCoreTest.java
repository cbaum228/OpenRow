package org.openrow.probe;

import java.util.*;
import static org.openrow.probe.TrainingCore.*;
import org.openrow.probe.TrainingCore.Record;

/** Scenario tests: real adaptation evidence, interrupted clocks, and versioned history round trips. */
public final class TrainingCoreTest {
    private static int checks;
    private static final long NOW=1000*DAY;
    private static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    private static void fails(Runnable f,String why){boolean failed=false;try{f.run();}catch(RuntimeException expected){failed=true;}check(failed,why);}
    private static Runner run(Plan p,long at,boolean simulated){
        Runner r=new Runner(UUID.randomUUID().toString(),p,at,simulated);r.resume(0);
        for(int second=1;second<=p.seconds();second++){r.observeDistance(2);r.tick(second*1000L,true,true);}return r;
    }
    private static Record result(Plan p,long at,int effort,int manageable){Runner r=run(p,at,false);return new Record(r,at+p.seconds()*1000L,effort,manageable);}
    private static State empty(){return new State(Collections.<Record>emptyList());}
    private static Recommendation rec(State s,long now){return recommend(s,45,Readiness.READY,ReturnChoice.MAINTAIN,now);}
    public static void main(String[] args){
        Recommendation fresh=rec(empty(),NOW);check(fresh.recommended.provisional,"New athlete has provisional guidance");
        check(fresh.recommended.seconds()==600&&!fresh.recommended.controlled(),"New athlete starts comfortable, not a maximal assessment");
        check(fresh.alternatives.size()==2,"Recommendation includes alternatives");
        check(recommend(empty(),5,Readiness.READY,ReturnChoice.MAINTAIN,NOW).recommended.seconds()==300,"Available time caps dose");
        fails(()->recommend(empty(),10,Readiness.STOP,ReturnChoice.MAINTAIN,NOW),"Deferring produces no workout");
        Plan quick=assessment(Kind.QUICK,Category.ENDURANCE,Goal.GENERAL_CARDIO);
        Plan full=assessment(Kind.BENCHMARK,Category.ENDURANCE,Goal.GENERAL_CARDIO);
        check(quick.seconds()==300,"Quick baseline fits five minutes");check(full.seconds()==2100,"Full benchmark is fixed at 35 minutes");
        for(Category c:Category.values()){Plan p=assessment(Kind.STARTER,c,Goal.ENDURANCE);check(p.seconds()==780,"Starter fits 13 minutes");}
        Record benchmark=result(full,NOW-10*DAY,4,1);
        State assessed=new State(Arrays.asList(benchmark));
        for(Category c:Category.values()){
            check(assessed.evidence(c,NOW)==1,"Composite assessment contributes category evidence");
            check(benchmark.workDistance(c)>0,"Category retains its own work observation");
            check(assessed.toleranceSeconds(c,NOW)<full.seconds(),"Composite duration does not become each category's tolerance");
        }
        List<Record> good=new ArrayList<Record>();for(int i=0;i<3;i++)good.add(result(full,NOW-(10-i)*DAY,4,1));
        State trained=new State(good);Category chosen=trained.nextCategory(NOW);
        Plan progressed=rec(trained,NOW).recommended;
        check(progressed.seconds()==trained.toleranceSeconds(chosen,NOW)+60,"Repeated manageable work supports one-minute progression");
        check(progressed.reason.contains("Several"),"Progression records its explanation");
        check(rec(trained,NOW).recommended.protocol.equals(progressed.protocol)&&rec(trained,NOW).recommended.seconds()==progressed.seconds(),"Recommendation is deterministic");
        check(trained.confidence(chosen,NOW)==1,"Repeated fresh evidence increases confidence");
        check(trained.confidence(chosen,NOW+60*DAY)==0,"Old evidence loses confidence without deleting records");
        check(trained.history.size()==3,"Confidence aging preserves achievements and observations");
        List<Record> missing=new ArrayList<Record>();for(int i=0;i<4;i++)missing.add(result(full,NOW-(10-i)*DAY,-1,-1));
        check(rec(new State(missing),NOW).recommended.provisional,"Missing feedback cannot establish confident ability");
        List<Record> recentUnknown=new ArrayList<Record>(good);recentUnknown.add(result(full,NOW-DAY,-1,-1));State uncertain=new State(recentUnknown);
        check(rec(uncertain,NOW).recommended.seconds()<=uncertain.toleranceSeconds(uncertain.nextCategory(NOW),NOW),"Fresh missing feedback holds progression even after earlier success");
        List<Record> setbacks=new ArrayList<Record>(good);
        Plan work=progressed;
        setbacks.add(result(work,NOW-4*DAY,9,0));State oneBad=new State(setbacks);
        check(oneBad.toleranceSeconds(chosen,NOW)==trained.toleranceSeconds(chosen,NOW),"One difficult day does not redefine lasting tolerance");
        setbacks.add(result(work,NOW-3*DAY,9,0));State twoBad=new State(setbacks);
        check(twoBad.toleranceSeconds(chosen,NOW)<trained.toleranceSeconds(chosen,NOW),"Repeated difficulty lowers the current tolerance estimate");
        check(rec(twoBad,NOW).recommended.seconds()<progressed.seconds(),"Setbacks reduce the next workload");
        check(!recommend(trained,45,Readiness.TIRED,ReturnChoice.MAINTAIN,NOW).recommended.controlled(),"Tired check-in never prescribes controlled intervals");
        State afterBreak=new State(good);long back=good.get(2).ended+15*DAY;
        check(!afterBreak.returning(good.get(2).ended+14*DAY),"Exactly fourteen days is not longer than two weeks");
        check(afterBreak.returning(back),"Fifteen days triggers return check-in");
        int maintain=recommend(afterBreak,45,Readiness.READY,ReturnChoice.MAINTAIN,back).recommended.seconds();
        int lower=recommend(afterBreak,45,Readiness.READY,ReturnChoice.LOWER_LITTLE,back).recommended.seconds();
        int raise=recommend(afterBreak,45,Readiness.READY,ReturnChoice.RAISE_LITTLE,back).recommended.seconds();
        int lowest=recommend(afterBreak,45,Readiness.READY,ReturnChoice.LOWER_LOT,back).recommended.seconds();
        check(lowest<=lower&&lower<maintain&&raise==maintain+60,"Return choices have bounded, ordered effects");
        check(afterBreak.history.size()==3,"Return choice does not rewrite demonstrated history");
        State legacy=empty();legacy.lastActivityAt=NOW-15*DAY;check(legacy.returning(NOW)&&legacy.confidence(Category.ENDURANCE,NOW)==0,"Legacy activity date detects a break without inventing fitness evidence");
        check(recommend(afterBreak,45,Readiness.READY,ReturnChoice.RAISE_LITTLE,back+180*DAY).recommended.seconds()<=600,"Long absence keeps return exploratory");

        Runner clock=new Runner("clock",quick,NOW,false);clock.resume(1000);clock.tick(91000,true,true);
        check(clock.block==1&&clock.within==0,"Warm-up transitions on monotonic time");
        clock.pause(91000);clock.tick(191000,false,false);check(clock.elapsed==90,"Explicit pause freezes work");
        clock.resume(191000);clock.tick(281000,true,true);check(clock.block==2,"Resume continues the correct block");
        clock.tick(341000,false,true);check(clock.current().phase==Phase.COOL_DOWN,"Coasting does not postpone cool-down");
        clock.tick(401000,false,true);check(clock.finished&&clock.outcome==Outcome.COMPLETED,"Workout completes without any game win");
        check(clock.elapsed==300&&clock.rowingSeconds==180,"Rowing time is separate from prescribed recovery and coasting");
        check(clock.pauseSeconds==100,"Pause duration retained separately");
        Runner restored=Runner.restore(clock.save());check(restored.finished&&restored.elapsed==300,"Completed checkpoint round-trips");
        Runner partial=new Runner("partial",full,NOW,false);partial.resume(0);partial.tick(65000,true,true);partial.observeDistance(123);
        Properties saved=partial.save();Runner restart=Runner.restore(saved);restart.tick(999999999,false,false);
        check(restart.paused&&restart.elapsed==65&&restart.distance==123,"Restart stays paused and earns no offline work");
        check(restart.interruptions==1,"Process interruption is retained for benchmark comparability");
        restart.resume(999999999);restart.tick(1000000999L,true,true);check(restart.elapsed==66,"New monotonic origin resumes only new time");
        restart.stop(1000000999L);Record shortRow=new Record(restart,NOW+DAY,5,1);
        check(shortRow.outcome==Outcome.EARLY_STOP&&shortRow.distance==123,"Early stop preserves partial observations");
        check(!shortRow.success()&&!shortRow.comparable(),"Partial work cannot be promoted as completed assessment");
        Runner reduced=run(quick,NOW,false); // Use a second active runner to test explicit adjustments.
        reduced=new Runner("reduced",quick,NOW,false);reduced.resume(0);reduced.easier();reduced.tick(300000,true,true);
        Record adjusted=new Record(reduced,NOW+DAY,4,1);
        check(adjusted.adjusted&&!adjusted.success()&&!adjusted.comparable(),"Easier targets do not masquerade as unmodified benchmark success");
        Runner skipped=new Runner("skip",full,NOW,false);skipped.resume(0);skipped.tick(400000,true,true);skipped.shortenWork();
        check(skipped.current().phase==Phase.RECOVERY&&skipped.adjusted,"Shortening work preserves the following recovery");
        skipped.extendRecovery(400000);skipped.tick(500000,false,true);check(skipped.paused&&skipped.elapsed==400,"Recovery can be extended without more work");
        Runner gaps=new Runner("gaps",quick,NOW,false);gaps.resume(0);gaps.tick(100000,true,true);gaps.tick(300000,false,false);
        check(new Record(gaps,NOW+DAY,5,1).missingSeconds==200,"Telemetry gaps are retained");
        check(!new Record(gaps,NOW+DAY,5,1).usable(),"Missing telemetry cannot become a fitness decline or success");
        Runner coasting=new Runner("coasting",full,NOW,false);coasting.resume(0);coasting.tick(300000,true,true);coasting.tick(full.seconds()*1000L,false,true);
        Record coasted=new Record(coasting,NOW+DAY,4,1);check(!coasted.success()&&!coasted.contributes(Category.SUSTAINED),"Running out the clock after warm-up does not demonstrate work capacity");
        Runner interrupted=new Runner("interrupted",quick,NOW,false);interrupted.resume(0);interrupted.tick(90000,true,true);interrupted.pause(90000);interrupted.resume(100000);interrupted.tick(310000,true,true);
        Record resumedBenchmark=new Record(interrupted,NOW+DAY,4,1);check(!resumedBenchmark.comparable(),"Paused benchmark is retained but not compared");
        check(resumedBenchmark.timeline.size()>3,"Record retains transitions and user interruptions");
        Runner repeatedResume=new Runner("frequent",quick,NOW,false);repeatedResume.resume(0);repeatedResume.resume(500);repeatedResume.tick(1000,true,true);
        check(repeatedResume.elapsed==1,"Frequent telemetry cannot keep resetting an already-running clock");
        Record unexpectedlyHard=result(fresh.recommended,NOW-DAY,6,1);check(unexpectedlyHard.difficult()&&!unexpectedlyHard.success(),"Much harder than prescribed prevents progression despite manageability answer");
        fails(()->gaps.observeDistance(Double.NaN),"Reject nonfinite observations");
        Properties corrupt=clock.save();corrupt.setProperty("elapsed","NaN");fails(()->Runner.restore(corrupt),"Reject corrupt time");
        final Properties bad=clock.save();bad.setProperty("version","99");fails(()->Runner.restore(bad),"Reject unknown plan versions");
        History history=new History();Runner finished=run(full,NOW-10*DAY,false);Record result=new Record(finished,NOW-9*DAY,4,1);
        history.put(result,finished);history.put(result,finished);check(history.records().size()==1,"Repeated result commit is idempotent");
        History roundtrip=new History(history.data);check(roundtrip.records().get(0).plan.blocks.size()==full.blocks.size(),"History retains complete versioned plan");
        check(roundtrip.records().get(0).workDistance(Category.SUSTAINED)==result.workDistance(Category.SUSTAINED),"History retains per-block measured distance");
        Runner simulated=run(full,NOW,true);history.put(new Record(simulated,NOW+DAY,3,1),simulated);
        check(history.records().size()==1,"Simulated results never enter training history");
        check(new State(Arrays.asList(result,result,new Record(simulated,NOW+DAY,3,1))).history.size()==1,"State excludes duplicate IDs and simulations");
        history.settings(Goal.ENDURANCE,Mode.COACH);check(new History(history.data).state().mode==Mode.COACH,"Mode and goal survive reload");
        Record first=result(full,NOW-20*DAY,4,1);Runner more=run(full,NOW-10*DAY,false);
        for(int i=0;i<more.blockDistance.length;i++)if(more.plan.blocks.get(i).category==Category.SUSTAINED)more.blockDistance[i]*=1.1;
        Record later=new Record(more,NOW-9*DAY,4,1);State trends=new State(Arrays.asList(first,later));
        check(Math.abs(trends.benchmarkChange(Category.SUSTAINED,NOW)-.1)<.0001,"Same-protocol benchmarks retain category-specific progress");
        check(trends.benchmarkChange(Category.ENDURANCE,NOW)==0,"Improvement in one category does not fabricate improvement elsewhere");
        GameWorkoutAdapter.Frame frame=new GameWorkoutAdapter.Frame(clock);check(frame.finished&&frame.remainingBlockSeconds==0,"Shared game frame reports authoritative completion");
        System.out.println("PASS: "+checks+" training scenarios");
    }
}
