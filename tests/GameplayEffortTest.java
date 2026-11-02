package org.openrow.probe;

import java.util.*;
import static org.openrow.probe.TrainingCore.*;
import org.openrow.probe.TrainingCore.Record;

public final class GameplayEffortTest {
    private static int checks;
    private static void check(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
    private static Record record(String id,double speed,boolean simulated,long at){
        Plan p=new Plan("game-reference","Gameplay reference fixture",Kind.TRAINING,Category.ENDURANCE,Goal.ENDURANCE,Arrays.asList(new Block(Phase.WORK,Category.ENDURANCE,120,5)),true);
        Runner r=new Runner(id,p,at,simulated);r.resume(0);for(int sec=1;sec<=120;sec++){r.observeDistance(speed);r.tick(sec*1000L,true,true);}return new Record(r,at+120000,5,1);
    }
    public static void main(String[] args){long now=100*DAY;
        List<Record> low=Arrays.asList(record("low-a",1.5,false,now-2*DAY),record("low-b",1.5,false,now-DAY));
        List<Record> high=Arrays.asList(record("high-a",4,false,now-2*DAY),record("high-b",4,false,now-DAY));
        double a=GameplayEffort.reference(low,now),b=GameplayEffort.reference(high,now);
        check(a==1.5&&b==4,"Independent personal references use reported comparable effort");
        check(GameplayEffort.contribution(1.5,true,a,null)==GameplayEffort.contribution(4,true,b,null),"Comparable personal effort gives equal game contribution");
        check(GameplayEffort.contribution(0,false,a,null)==0,"No movement gives no game contribution");
        check(GameplayEffort.contribution(Double.NaN,true,a,null)==0,"Invalid telemetry gives no contribution");
        check(GameplayEffort.contribution(1,true,0,null)==1,"Unknown athlete gets neutral provisional gameplay reference");
        check(GameplayEffort.contribution(20,true,a,null)<=1.25,"Above-reference contribution is bounded");
        check(GameplayEffort.reference(Arrays.asList(record("fake",3,true,now-DAY)),now)==0,"Simulated history cannot calibrate real gameplay");
        check(GameplayEffort.reference(low,now+40*DAY)==0,"Stale gameplay references become provisional");
        for(Phase phase:Phase.values()){
            Plan p=new Plan("phase-reference","Fixture",Kind.TRAINING,Category.ENDURANCE,Goal.ENDURANCE,Arrays.asList(new Block(phase,Category.ENDURANCE,120,3)),true);
            Runner r=new Runner("phase",p,now,false);r.resume(0);r.tick(1000,true,true);GameWorkoutAdapter.Frame f=new GameWorkoutAdapter.Frame(r);
            double value=GameplayEffort.contribution(10,true,a,f);
            check(phase==Phase.WORK?Math.abs(value-.6)<1e-9:value==0,"Prescribed easy work is capped; warm-up/recovery/cool-down cannot pressure boss damage");
            r.pause(1000);check(GameplayEffort.contribution(2,true,a,new GameWorkoutAdapter.Frame(r))==0,"Pause removes contribution");
        }
        System.out.println("PASS: "+checks+" personal gameplay contribution checks");
    }
}
