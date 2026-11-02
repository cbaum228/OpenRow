package org.openrow.probe;

import java.util.*;

/** Production balance, migration, reward identity and interrupted-encounter regressions. */
public final class GravityWellCampaignTest {
    private static int checks;
    private static void check(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
    private static void near(double a,double b,String why){check(Math.abs(a-b)<1e-5,why+": "+a+" / "+b);}
    private static Properties save(GravityWellGame g){Properties p=new Properties();g.save(p);return p;}
    private static void rejects(Properties p){boolean failed=false;try{GravityWellGame.restore(p);}catch(RuntimeException expected){failed=true;}check(failed,"Corrupt/future saves are rejected");}
    private static GravityWellGame ready(int stage){GravityWellGame g=new GravityWellGame();g.enableCampaign(false,false);
        while(g.campaign.cursor<stage*4+3){GravityWellCampaign.Destination d=g.campaign.next();check(g.selectDestination(d.id),"Select sequential route");
            if(d.boss<0)g.campaign.tick(d.seconds,true);else{long id=g.nextBossId++;g.campaign.beginBoss(id);g.campaign.consumed(id,d.boss);}}
        check(g.selectDestination(g.campaign.next().id),"Select current boss");return g;}
    private static void seconds(GravityWellGame g,int seconds,boolean moving){for(int sec=0;sec<seconds;sec++)for(int part=0;part<4;part++){if(moving)g.input(.625,part==0?1:0,12.5);g.advance(.25,.5,true,moving);}}
    private static void fight(GravityWellGame g){g.start(0);g.campaignWindow(true,true);int second=0;do{seconds(g,1,true);second++;}while(g.activeBossId>0&&second<2000);check(second<2000,"Prepared fight completes");g.finish();}
    public static void main(String[] args){
        double hours=0;for(double h:GravityWellBalance.HOURS)hours+=h;near(hours,48,"Main route total");
        double prep=0;for(GravityWellCampaign.Destination d:GravityWellCampaign.DESTINATIONS)prep+=d.seconds;
        near(prep+10*300,48*3600,"Budget includes first-clear encounters");
        Set<String> ids=new HashSet<>();check(GravityWellGame.NODES.length==201,"Existing catalog retained");
        for(int i=0;i<GravityWellGame.NODES.length;i++){GravityWellGame.Node n=GravityWellGame.NODES[i];check(ids.add(n.id),"Stable unique node ID");for(int parent:n.prerequisites)check(parent>=0&&parent<201,"Valid prerequisite");
            if(GravityWellBalance.main(i)&&i>=9){int s=GravityWellBalance.unlockStage(i);check(s>=0&&s<10,"Main node has stage");if(s>0)check(GravityWellBalance.source(i)==GravityWellCampaign.specialMaterial(GravityWellCampaign.BOSSES[s-1]),"Main materials come from a previous boss");}}
        for(int s=0;s<10;s++){
            GravityWellGame g=ready(s);int kind=GravityWellCampaign.BOSSES[s];near(GravityWellBalance.seconds(g,kind),GravityWellBalance.UNPREPARED_MINUTES[s]*60,"Unprepared tier duration");
            GravityWellBalance.referenceLoadout(g,s);near(GravityWellBalance.seconds(g,kind),300,"Fully prepared reference duration");
            g.start(0);g.campaignWindow(true,false);seconds(g,1,true);check(g.activeBossId==0,"Short work window defers boss");
            g.campaignWindow(true,true);seconds(g,1,true);check(g.bossKind==kind&&g.activeBossId>0,"Boss order");
            seconds(g,90,true);double integrity=g.bossIntegrity(),mass=g.bossMass();
            g.campaignWindow(false,false);seconds(g,30,true);near(g.bossIntegrity(),integrity,"Recovery with real movement does not damage boss");near(g.bossMass(),mass,"Recovery retains visual mass");
            g.finish();Properties p=save(g);g=GravityWellGame.restore(p);near(g.bossIntegrity(),integrity,"Damage survives restart");
            g.start(0);g.campaignWindow(true,true);seconds(g,211,true);check(g.activeBossId==0,"Reference boss completes at five eligible minutes");
            check(g.campaign.cursor==(s+1)*4,"First clear advances exactly once");long group=g.campaign.lastRewardGroup;double[] materials=g.campaign.materials.clone();
            g.campaign.consumed(group,kind);check(Arrays.equals(materials,g.campaign.materials),"Completion is idempotent");
            g.finish();GravityWellGame copy=GravityWellGame.restore(save(g));copy.campaign.consumed(group,kind);check(Arrays.equals(materials,copy.campaign.materials),"Idempotence survives reload");
            near(g.distance,g.strokes*2.5,"Upgrades cannot multiply measured input");
        }
        earlyAndQueue();retreat();migration();economyAndMiniatures();specialistEffects();precision();
        System.out.println("PASS: "+checks+" campaign, balance, migration and reward checks");
    }
    private static void earlyAndQueue(){GravityWellGame g=new GravityWellGame();g.enableCampaign(false,false);g.selectDestination(g.campaign.next().id);g.campaign.autoQueue=true;
        check(!g.selectDestination("debris_boss"),"First boss not available before introduction");g.campaign.tick(1500,true);g.campaign.tick(400,true);
        check(g.campaign.earlyBoss(),"First boss can be encountered within 25 eligible minutes");check(g.selectDestination("debris_boss"),"Early attempt allowed");
        g.start(0);seconds(g,30,true);g.finish();g=GravityWellGame.restore(save(g));check(g.campaign.selected.equals("debris_boss")&&g.activeBossId>0,"Auto queue cannot replace an early active encounter");
        g=new GravityWellGame();g.enableCampaign(false,false);g.selectDestination(g.campaign.next().id);g.campaign.autoQueue=true;g.campaign.tick(g.campaign.next().seconds,true);
        check(g.campaign.selected.equals(g.campaign.next().id),"Auto queue follows preparation only");
        for(int i=0;i<2;i++)g.campaign.tick(g.campaign.next().seconds,true);check(g.campaign.pendingBoss()<0,"Queue does not silently introduce a new boss");
        g=new GravityWellGame();g.enableCampaign(false,false);g.selectDestination(g.campaign.next().id);g.start(0);g.campaignWindow(false,false);seconds(g,10,true);near(g.campaign.progress,0,"Only eligible work advances preparation");
        GravityWellCampaign queue=new GravityWellCampaign();queue.applyCoachDefault(true);check(queue.autoQueue,"Coach enables queue by default");queue.setAutoQueue(false);queue.applyCoachDefault(true);check(!queue.autoQueue,"Explicit map choice overrides coach default");
        Properties savedQueue=new Properties();queue.save(savedQueue);queue=GravityWellCampaign.restore(savedQueue,false);queue.applyCoachDefault(true);check(!queue.autoQueue,"Explicit queue preference survives restart");}
    private static void retreat(){GravityWellGame g=ready(1);GravityWellBalance.referenceLoadout(g,1);g.start(0);seconds(g,100,true);g.finish();
        long original=g.activeBossId;double integrity=g.bossIntegrity();check(g.selectDestination("repeat:dwarf"),"Retreat to full harvest allowed");check(g.parkedDestinations().contains("planetary_boss"),"Campaign encounter parked");
        g=GravityWellGame.restore(save(g));check(g.parkedDestinations().contains("planetary_boss"),"Parked encounter persists");fight(g);
        check(g.campaign.lastRewardGroup>original,"Later replay can finish first");check(g.selectDestination("planetary_boss"),"Original encounter resumes after replay");
        check(g.activeBossId==original,"Same reward identity resumes");near(g.bossIntegrity(),integrity,"Retreat does not heal boss");g.start(0);seconds(g,220,true);g.finish();
        check(g.campaign.cleared.contains("planet"),"Older encounter ID can legitimately finish after newer replay");
        double[] material=g.campaign.materials.clone();g.campaign.consumed(original,GravityWellGame.PLANET);check(Arrays.equals(material,g.campaign.materials),"Parked reward paid once");}
    private static void migration(){GravityWellGame g=ready(2);g.xp=54321;g.ranks[0]=3;g.start(0);seconds(g,30,true);g.finish();Properties p=save(g);
        p.setProperty("campaign.version","1");p.remove("campaign.balanceVersion");p.remove("game.balanceVersion");p.remove("game.bossIntegrity");p.remove("campaign.rewardCeiling");p.remove("campaign.rewarded");
        double fraction=g.bossMass()/g.bossInitialMass;GravityWellGame restored=GravityWellGame.restore(p);near(restored.xp,g.xp,"Existing XP preserved");near(restored.ranks[0],3,"Owned ranks preserved");near(restored.bossIntegrity(),fraction,"Old boss remaining fraction migrated");
        g=new GravityWellGame();g.enableCampaign(false,false);g.selectDestination(g.campaign.next().id);p=save(g);p.setProperty("campaign.version","1");p.setProperty("campaign.progress","900");p.remove("campaign.balanceVersion");p.remove("campaign.stageEligible");
        restored=GravityWellGame.restore(p);near(restored.campaign.progress,restored.campaign.next().seconds*.5,"Old preparation completion percentage retained");
        Properties bad=save(restored);bad.setProperty("campaign.progress","NaN");rejects(bad);bad=save(restored);bad.setProperty("game.upgrade.future_node","1");rejects(bad);bad=save(restored);bad.setProperty("campaign.balanceVersion","99");rejects(bad);
        g=new GravityWellGame();g.enableCampaign(true,false);check(g.labCheckpoint(10),"Synthetic checkpoint remains isolated");g.campaign.acceleration=120;GravityWellGame.restore(save(g),true);rejects(save(g));
        restored.enableCampaign(false,false);check(!restored.labCheckpoint(0),"Real career refuses lab grants");}
    private static void economyAndMiniatures(){GravityWellGame g=ready(1);g.finish();g.selectDestination("free");GravityWellBalance.referenceLoadout(g,1);g.start(0);seconds(g,100,true);
        int mini=0;for(GravityWellGame.Body b:g.bodies)if(b.miniature){mini++;check(b.kind==GravityWellGame.DWARF,"Mini keeps original material species");check(b.bossId==0,"Mini not a featured encounter");}
        check(mini==1,"One mini introduced into ordinary population");double originalSource=g.campaign.materials[5];seconds(g,100,true);check(g.campaign.materials[5]>originalSource,"Mini stripping yields source material");
        g.finish();GravityWellGame copy=GravityWellGame.restore(save(g));check(Arrays.equals(g.campaign.materials,copy.campaign.materials),"Mini save reload does not duplicate harvest");
        check(!copy.campaign.cleared.contains("planet"),"Minis cannot advance first clears");
        g=ready(0);g.xp=100000;Arrays.fill(g.campaign.materials,100);check(!g.purchase(GravityWellGame.mainNode(4,0)),"Future main stage remains locked despite wealth");
        int node=GravityWellGame.mainNode(1,0),cost=g.cost(node);check(g.purchase(node),"Unlocked main purchase");near(g.xp,100000-cost,"XP debit exact");
        double prev=GravityWellBalance.seconds(g,7);check(g.purchase(node),"Next rank purchase");check(GravityWellBalance.seconds(g,7)<prev,"Combat improvements reduce duration");
        g=ready(2);g.xp=1000000;Arrays.fill(g.campaign.materials,1000);for(int i=0;i<201;i++)if(GravityWellBalance.main(i)&&GravityWellBalance.unlockStage(i)<=2)g.ranks[i]=GravityWellGame.NODES[i].max;
        check(g.ranks[GravityWellGame.mainNode(8,0)]==2&&g.ranks[GravityWellGame.mainNode(9,0)]==0,"Gas giant and ice giant have distinct upgrade releases");
        g=new GravityWellGame();g.enableCampaign(false,false);g.start(0);g.gameplayContribution(1);g.campaignWindow(true,true);seconds(g,60,true);double earned=g.xp;
        near(earned,60,"Baseline normalized game XP rate");g.campaignWindow(false,false);seconds(g,60,true);near(g.xp,earned,"Recovery cannot earn work XP");
        check(g.distance>0,"Actual rowing remains recorded during recovery");}
    private static void precision(){for(int s=0;s<10;s++){
        GravityWellCampaign c=new GravityWellCampaign();c.cursor=s*4;c.selected=c.next().id;for(int earlier=0;earlier<s;earlier++)c.cleared.add(GravityWellGame.KINDS[GravityWellCampaign.BOSSES[earlier]].id);
        int frames=(int)Math.round(c.next().seconds*60);for(int f=0;f<frames-1;f++){c.tick(GravityWellGame.STEP,true);if(f>0&&f%72000==0){Properties p=new Properties();c.save(p);c=GravityWellCampaign.restore(p,false);}}
        check(c.cursor==s*4,"Not one frame early");c.tick(GravityWellGame.STEP,true);check(c.cursor==s*4+1,"Exact preparation budget completes");
    }}
    private static void specialistEffects(){
        GravityWellGame g=ready(9);GravityWellBalance.referenceLoadout(g,9);
        int lastRefinement=GravityWellGame.mainNode(24,3),lastYield=GravityWellGame.mainNode(24,6);
        double full=GravityWellBalance.xpFactor(g);g.ranks[lastRefinement]--;check(GravityWellBalance.xpFactor(g)<full,"Final refinement rank still has a useful effect");g.ranks[lastRefinement]++;
        full=GravityWellBalance.yieldFactor(g);g.ranks[lastYield]--;check(GravityWellBalance.yieldFactor(g)<full,"Final harvest rank still has a useful effect");g.ranks[lastYield]++;
        double before=GravityWellBalance.seconds(g,GravityWellGame.RED_DWARF);g.ranks[104]=1;near(GravityWellBalance.seconds(g,GravityWellGame.RED_DWARF),before,"Yield specialists do not masquerade as damage upgrades");
        g.ranks[103]=1;check(GravityWellBalance.seconds(g,GravityWellGame.RED_DWARF)<before,"Erosion specialist improves its target encounter");
        g.selectDestination("free");g.spawnKind(GravityWellGame.COMET,false);GravityWellGame.Body comet=g.bodies.get(0);comet.captured=true;comet.orbitRadius=170;comet.captureAge=5;comet.x=170;comet.y=0;
        GravityWellGame enhanced=GravityWellGame.restore(save(g));enhanced.ranks[74]=1;double source=g.campaign.materials[4];g.start(0);enhanced.start(0);seconds(g,1,true);seconds(enhanced,1,true);
        near((enhanced.campaign.materials[4]-source)/(g.campaign.materials[4]-source),1.2,"Target harvest specialist increases actual source yield");
    }
}
