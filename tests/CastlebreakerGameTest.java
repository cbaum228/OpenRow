package org.openrow.probe;

public final class CastlebreakerGameTest {
    private static int checks;
    private static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static void advance(CastlebreakerGame g,double seconds){for(int i=0;i<(int)(seconds*120);i++)g.step(1.0/120);}
    private static void charge(CastlebreakerGame g,double meters){g.addDistance(meters);}
    public static void main(String[] args) {
        CastlebreakerGame g=new CastlebreakerGame();
        check(g.targetsDown==0 && g.blocks.size()==28,"Fort created");
        advance(g,10);check(g.targetsDown==0,"Fort stable before impact");
        for(CastlebreakerGame.Block b:g.blocks)check(!b.moving,"Each block initially supported");
        check(!g.fire(),"Cannot fire empty winch");charge(g,5);check(g.charge==25,"Five meters supplies 25 percent charge");
        check(!g.fire(),"Cannot fire before ten meters");
        charge(g,20);check(g.charge==100,"Charge capped");
        g.aim(-50);check(g.aim==-22,"Aim clamped left");g.aim(50);check(g.aim==22,"Aim clamped right");
        g.aim(0);check(g.fire(),"Charged stone launches");check(g.charge==0 && g.shots==1,"Launch spends charge");
        check(!g.fire(),"No second projectile during flight");charge(g,5);check(g.charge==25,"Can wind next shot during flight");
        advance(g,12);check(!g.flying,"Flight resolves");
        g.reset();charge(g,10);g.fire();advance(g,10);
        check(g.targetsDown>=1,"Center shot collapses center beacon");
        System.out.println("Center shot: "+g.targetsDown+" beacon(s) down");
        g.aim(-12.1);charge(g,10);g.fire();advance(g,10);
        g.aim(12.1);charge(g,10);g.fire();advance(g,10);
        check(g.won(),"Three aimed shots can complete fort");check(!g.addDistance(5) && !g.fire(),"Win stops charging and firing");
        g.reset();check(!g.won() && g.shots==0 && g.charge==0,"Reset creates fresh encounter");
        // Same fixed simulation steps give the same outcome regardless of rendering.
        CastlebreakerGame a=new CastlebreakerGame(),b=new CastlebreakerGame();charge(a,10);charge(b,10);a.fire();b.fire();advance(a,7);advance(b,7);
        check(a.targetsDown==b.targetsDown && a.rockZ==b.rockZ,"Deterministic simulation");
        distanceInput();trajectory();fortsAndArmor();
        System.out.println("PASS: "+checks+" Castlebreaker checks");
    }
    private static void distanceInput() {
        CastlebreakerGame g=new CastlebreakerGame();
        check(!g.observeDistance(1200,true) && g.charge==0,"Existing monitor total is only a baseline");
        check(g.observeDistance(1205,true) && g.charge==25,"Only new distance charges");
        check(!g.observeDistance(1205,true) && g.charge==25,"Repeated reading does not charge again");
        check(!g.observeDistance(3,true) && g.charge==25,"S4 reset never grants phantom distance");
        g.observeDistance(8,true);check(g.charge==50,"Meter increments resume after S4 reset");
        g.observeDistance(18,false);check(g.charge==50,"Inactive/test-mode meters do not charge");
        g.observeDistance(19,true);check(g.charge==55,"Inactive distance is not awarded later");
        g.resetDistanceBaseline();g.observeDistance(9000,true);check(g.charge==55,"Reconnection establishes a fresh baseline");
        g.observeDistance(9001,true);check(g.charge==60,"Distance after reconnect charges normally");
        g.observeDistance(-1,true);g.observeDistance(70000,true);g.observeDistance(9002,true);
        check(g.charge==65,"Invalid readings cannot overwrite the baseline");
        double before=g.distanceRowed;
        g.addDistance(-5);g.addDistance(Double.NaN);g.addDistance(Double.POSITIVE_INFINITY);
        check(g.distanceRowed==before && g.charge==65,"Invalid distance additions ignored");
        g.reset();g.observeDistance(10000,true);check(g.charge==0 && g.distanceRowed==0,"Reset fort clears baseline and encounter distance");
        g.observeDistance(10020,true);check(g.charge==100,"Twenty new meters fully charges");g.fire();
        check(g.charge==0,"No excess charge is banked for next shot");
    }
    private static void trajectory() {
        for(int meters=10;meters<=20;meters++)for(double aim:new double[]{-12.1,0,12.1}) {
            CastlebreakerGame g=new CastlebreakerGame();charge(g,meters);g.aim(aim);
            double[] point=g.aimPoint();double time=g.timeToFort();
            check(time<1.15,"Ready shot reaches fort in about a second");
            check(Math.abs(point[2]-CastlebreakerGame.FORT_Z)<0.001,"Crosshair is on the fort plane");
            check(point[1]>1 && point[1]<6,"Every ready charge passes through tower height");
            check(g.fire(),"Every ready charge is fireable");
            double peak=CastlebreakerGame.LAUNCH_HEIGHT+g.rockVY*g.rockVY/(2*CastlebreakerGame.GRAVITY);
            check(peak<6,"Low arc stays below tower tops");
            advance(g,4);check(g.targetsDown>=1,"Ready shot at tower can topple its beacon");
        }
        CastlebreakerGame low=new CastlebreakerGame(),high=new CastlebreakerGame();charge(low,10);charge(high,20);low.fire();high.fire();
        check(high.rockVZ>low.rockVZ && high.launchCharge>low.launchCharge,"Extra charge provides a stronger faster shot");
    }
    private static void fortsAndArmor() {
        int[] counts={3,3,4,3};java.util.Set<String> layouts=new java.util.HashSet<>();
        for(int fort=0;fort<4;fort++) {
            CastlebreakerGame g=new CastlebreakerGame();g.selectFort(fort);advance(g,3);
            check(g.totalTargets==counts[fort] && g.targetsDown==0,"Correct defender count for fort "+fort);
            String signature="";
            java.util.List<CastlebreakerGame.Block> targets=new java.util.ArrayList<>();
            for(CastlebreakerGame.Block b:g.blocks) {
                check(!b.moving,"Fort "+fort+" initially stable");
                if(b.target){targets.add(b);signature+=b.x+":"+b.homeY+":"+b.reinforced+";";}
            }
            layouts.add(signature);
            for(CastlebreakerGame.Block target:targets) {
                for(int attempt=0;attempt<5 && !target.fallen;attempt++) {
                    g.aim(Math.toDegrees(Math.atan2(target.homeX,target.homeZ-2)));charge(g,20);
                    check(g.fire(),"Siege shot available in fort "+fort);advance(g,4);
                }
            }
            check(g.won(),"Fort "+fort+" completable with aimed siege shots");
            int current=g.fortIndex;g.reset();check(g.fortIndex==current && g.targetsDown==0 && g.charge==0,"Reset retains selected fort");
        }
        check(layouts.size()==4,"Four distinct fort layouts");
        CastlebreakerGame light=new CastlebreakerGame();light.selectFort(1);charge(light,10);light.fire();advance(light,4);
        check(light.targetsDown==0,"Knight tower withstands a standard shot");
        boolean cracked=false;for(CastlebreakerGame.Block b:light.blocks)if(b.reinforced && b.health<b.maxHealth && !b.moving)cracked=true;
        check(cracked,"Light hit leaves persistent armor damage");
        int lightShots=1;while(light.targetsDown==0 && lightShots<6){charge(light,10);light.fire();advance(light,4);lightShots++;}
        check(light.targetsDown>0 && lightShots>=3 && lightShots<=6,"Repeated light hits eventually break armor");
        CastlebreakerGame heavy=new CastlebreakerGame();heavy.selectFort(1);charge(heavy,20);heavy.fire();advance(heavy,4);
        check(heavy.targetsDown==1,"One full siege shot collapses knight tower");
        check(lightShots*10>20,"Full charge uses less rowing distance than several light shots against armor");
        heavy.selectFort(4);check(heavy.fortIndex==0,"Next fort wraps to first");
        heavy.selectFort(-1);check(heavy.fortIndex==3,"Previous fort wraps to last");
        System.out.println("Armor: "+lightShots+" standard shots versus one full siege shot; all four forts cleared");
    }
}
