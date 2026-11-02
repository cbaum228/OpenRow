package org.openrow.probe;

public final class CampaignSessionTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void close(double a,double b,String message){check(Math.abs(a-b)<.001,message+": "+b);}
    private static CampaignSession.Plan plan(int rest){return new CampaignSession.Plan(false,1000,30,150,rest);}
    public static void main(String[] args) {
        CampaignSession.Plan p=plan(30);check(p.plannedMeters()==1000,"Distance plan retains selected meters");close(5,p.estimatedRowingMinutes(),"Distance time estimate uses pace");
        close(106,20*p.chargeScale(),"Winch thresholds rounded to useful whole meters");
        CampaignSession.Plan time=new CampaignSession.Plan(true,500,15,150,0);
        check(time.plannedMeters()==3000,"Time estimate derives rowing budget from chosen pace");close(15,time.estimatedRowingMinutes(),"Time plan estimate consistent");
        check(new CampaignSession.Plan(true,500,15,180,0).plannedMeters()==2500,"Changing planning pace changes time plan distance");
        try{new CampaignSession.Plan(false,0,30,150,0);throw new AssertionError("Invalid plan accepted");}catch(IllegalArgumentException expected){checks++;}
        CampaignSession s=new CampaignSession(p,false);check(!s.captureFort(2),"Cannot skip route stages");
        s.distance(100);s.distance(-5);s.distance(Double.NaN);s.shot();s.tick(2000);check(s.distanceMeters==100&&s.shots==1&&s.activeMillis==2000,"Only valid active metrics counted");
        check(s.captureFort(0),"Capture advances stage once");check(!s.captureFort(0),"Duplicate victory ignored");
        check(s.phase==CampaignSession.Phase.REST&&s.completedForts==1&&s.restMillis==30000,"Rest starts after first capture");
        s.distance(99);s.shot();s.tick(1000);check(s.distanceMeters==100&&s.shots==1&&s.activeMillis==2000&&s.restMillis==29000,"Rest excluded from active totals");
        check(!s.continueAfterRest(false),"Countdown respected unless user skips");
        CampaignSession restored=CampaignSession.restore(s.checkpoint());check(restored!=null&&restored.phase==CampaignSession.Phase.REST&&restored.completedForts==1,"Checkpoint restores route and phase");
        check(restored.restMillis==29000&&restored.distanceMeters==100&&restored.shots==1,"Checkpoint restores totals and remaining rest");
        check(restored.continueAfterRest(true)&&restored.currentFort()==1,"Manual skip continues to next fort");
        restored.captureFort(1);restored.tick(30000);check(restored.continueAfterRest(false),"Completed rest enables continue");
        check(!restored.simulated,"Real input marker retained");
        check(CampaignSession.restore("broken")==null,"Malformed checkpoint ignored");
        check(CampaignSession.restore(s.checkpoint().replace("phase=REST","phase=UNKNOWN"))==null,"Unknown state ignored");
        check(CampaignSession.restore(s.checkpoint().replace("distance=100.0","distance=NaN"))==null,"Nonfinite restored metrics rejected");
        fullRoute();
        System.out.println("PASS: "+checks+" campaign checks");
    }
    private static void fullRoute(){
        CampaignSession s=new CampaignSession(plan(0),true);
        for(int fort=0;fort<4;fort++) {
            CastlebreakerGame g=new CastlebreakerGame();g.setChargeScale(s.plan.chargeScale());g.selectFort(s.currentFort());
            java.util.List<CastlebreakerGame.Block> defenders=new java.util.ArrayList<>();for(CastlebreakerGame.Block b:g.blocks)if(b.target)defenders.add(b);
            for(CastlebreakerGame.Block target:defenders) {
                if(target.fallen)continue;
                double meters=target.reinforced?g.metersPerFullCharge:g.readyMeters();g.addDistance(meters);s.distance(meters);
                g.aim(Math.toDegrees(Math.atan2(target.homeX,target.homeZ-2)));check(g.fire(),"Planned charge can fire in fort "+fort);s.shot();
                for(int step=0;step<480;step++)g.step(1.0/120);s.tick(4000);
            }
            check(g.won(),"Scaled route fort "+fort+" completable");check(s.captureFort(fort),"Route progression accepts fort "+fort);
            if(fort<3)check(s.continueAfterRest(false),"Manual rest continues on request");
        }
        check(s.phase==CampaignSession.Phase.COMPLETE&&s.completedForts==4,"Campaign ends after four captures");
        close(1007,s.distanceMeters,"Efficient route approximately matches chosen distance");check(s.shots==13,"Summary accumulates shots across forts");
        long elapsed=s.activeMillis;s.tick(10000);s.distance(20);s.shot();check(s.activeMillis==elapsed&&s.distanceMeters==1007&&s.shots==13,"Summary freezes completed totals");
        CampaignSession saved=CampaignSession.restore(s.checkpoint());check(saved!=null&&saved.simulated&&saved.phase==CampaignSession.Phase.COMPLETE,"Completed test results persist with explicit test marker");
        check(!s.continueAfterRest(true)&&!s.captureFort(3),"Cannot advance a completed campaign");
        CastlebreakerGame free=new CastlebreakerGame();close(20,free.metersPerFullCharge,"Free play charge unaffected");
        try{free.setChargeScale(Double.NaN);throw new AssertionError("Invalid scale accepted");}catch(IllegalArgumentException expected){checks++;}
    }
}
