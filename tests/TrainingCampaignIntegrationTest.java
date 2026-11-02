package org.openrow.probe;

import java.util.*;
import static org.openrow.probe.TrainingCore.*;

public final class TrainingCampaignIntegrationTest {
    private static int checks;
    private static void check(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
    private static Runner runner(Phase phase,int seconds){
        Plan p=new Plan("campaign-adapter","Integration fixture",Kind.TRAINING,Category.ENDURANCE,Goal.ENDURANCE,
            Arrays.asList(new Block(phase,Category.ENDURANCE,seconds,phase==Phase.WORK?3:1)),true);
        Runner r=new Runner("campaign-adapter",p,1000,false);r.resume(0);r.tick(1000,true,true);return r;
    }
    public static void main(String[] args){
        for(Phase phase:Phase.values()){
            Runner r=runner(phase,120);GameWorkoutAdapter.Frame f=new GameWorkoutAdapter.Frame(r);
            check(f.campaignWorkEligible(true)==(phase==Phase.WORK),"Only prescribed work contributes preparation");
            check(!f.campaignWorkEligible(false),"No movement gives no preparation in every phase");
            check(f.campaignBossWindow(true)==(phase==Phase.WORK),"Only work can introduce bosses");
        }
        Runner r=runner(Phase.WORK,120);r.within=60;GameWorkoutAdapter.Frame f=new GameWorkoutAdapter.Frame(r);
        check(f.campaignBossWindow(true),"Sixty-second introduction boundary included");r.within=60.01;f=new GameWorkoutAdapter.Frame(r);
        check(f.campaignWorkEligible(true)&&!f.campaignBossWindow(true),"Short work windows retain progress while deferring boss");
        r.pause(1000);check(!new GameWorkoutAdapter.Frame(r).campaignWorkEligible(true),"Paused trainer cannot advance campaign");
        r=runner(Phase.WORK,120);r.tick(2000,true,false);check(!new GameWorkoutAdapter.Frame(r).campaignWorkEligible(true),"Missing telemetry cannot advance campaign");
        r=runner(Phase.WORK,120);r.finished=true;check(!new GameWorkoutAdapter.Frame(r).campaignWorkEligible(true),"Completed trainer cannot advance campaign");
        r=runner(Phase.WORK,120);r.easier();check(new GameWorkoutAdapter.Frame(r).campaignWorkEligible(true),"Easier prescribed work still advances campaign");
        Plan p=new Plan("campaign-clock","Independent workout",Kind.TRAINING,Category.ENDURANCE,Goal.ENDURANCE,
            Arrays.asList(new Block(Phase.WARM_UP,Category.ENDURANCE,30,1),new Block(Phase.WORK,Category.ENDURANCE,90,3),new Block(Phase.RECOVERY,Category.ENDURANCE,30,1),new Block(Phase.COOL_DOWN,Category.ENDURANCE,30,1)),true);
        r=new Runner("campaign-clock",p,1000,false);r.resume(0);r.telemetryAvailable=true;
        GravityWellGame g=new GravityWellGame();g.enableCampaign(false,false);g.selectDestination(g.campaign.next().id);g.start(0);
        int eligible=0;for(int sec=1;sec<=p.seconds();sec++){
            f=new GameWorkoutAdapter.Frame(r);boolean moving=f.phase!=Phase.RECOVERY;
            g.campaignWindow(f.campaignWorkEligible(moving),f.campaignBossWindow(moving));
            if(f.campaignWorkEligible(moving))eligible++;
            for(int frame=0;frame<60;frame++)g.advance(GravityWellGame.STEP,.5,true,moving,TrainingVisuals.input(f,2,moving));
            r.tick(sec*1000L,moving,true);
        }
        check(r.finished&&g.running,"Workout completion is independent of campaign objective");check(Math.abs(g.campaign.progress-eligible)<1e-6,"Campaign accumulates only the eligible block");check(eligible==90,"Recovery and warm/cool excluded");
        g.finish();Properties saved=new Properties();g.save(saved);GravityWellGame copy=GravityWellGame.restore(saved);check(Math.abs(copy.campaign.progress-90)<1e-6,"Workout end retains incomplete survey");
        System.out.println("PASS: "+checks+" trainer/campaign integration checks");
    }
}
