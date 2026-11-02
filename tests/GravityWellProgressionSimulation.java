package org.openrow.probe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Long-running audit, deliberately separate from the fast regression suite. No Android/device files. */
public final class GravityWellProgressionSimulation {
    private final String name,strategy;
    private final double input,speed;
    private final boolean guided;
    private final Path output;
    private GravityWellGame game=new GravityWellGame();
    private double clock,movingClock,eligibleClock,prep,bossTime,spent,earned,lastXp,routeEligible;
    private int sessions,purchases,maxBodies,maxParticles,checks;
    private final List<String> stageRows=new ArrayList<>();
    private final List<String> rows=new ArrayList<>(),bossRows=new ArrayList<>(),purchaseRows=new ArrayList<>();
    private final long began=System.nanoTime();
    private GravityWellProgressionSimulation(String[] args)throws IOException{
        name=args[0];input=Double.parseDouble(args[1]);speed=Double.parseDouble(args[2]);strategy=args[3];guided=name.contains("guided");output=Paths.get(args[4]);Files.createDirectories(output);
        game.enableCampaign(false,false); // Exercise ordinary gates/economy: no lab acceleration or checkpoint grants.
        game.campaign.autoQueue=guided;
        if(name.contains("seed2")||name.contains("seed3")){Properties p=new Properties();game.save(p);p.setProperty("game.counts","0,0,0,0,"+(name.contains("seed2")?1234567:987654321));game=GravityWellGame.restore(p);}
        rows.add("scenario,destination,eligible_minutes,boss_seconds,distance_m,xp_earned,xp_unspent,xp_spent,ranks,capacity,reach_factor,flow_factor,refinement_factor,drift_factor,erosion_factor,yield_factor,bodies_peak,particles_peak");
        bossRows.add("scenario,boss,seconds,ranks,capacity,xp_gained,distance_m,wall_seconds");
        purchaseRows.add("scenario,minute,node,rank,xp_cost,material_source,material_cost");
        stageRows.add("scenario,stage,eligible_hours,wall_hours,main_owned,main_available,xp_earned,xp_spent,xp_balance");
    }
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(name+": "+message);}
    private int ranks(){int n=0;for(int r:game.ranks)n+=r;return n;}
    private void second(){
        second(true,true,true);
    }
    private void second(boolean moving,boolean eligible,boolean bossWindow){
        game.campaignWindow(eligible,bossWindow);
        game.gameplayContribution(moving&&eligible?1:0);
        // Match the native fixed-step simulation, with S4-style distance-derived game work.
        for(int step=0;step<4;step++){
            if(moving)game.input(speed*.25,step==0&&((long)clock)%3==0?1:0,speed*20*.25);
            game.advance(.25,input,true,moving);
            maxBodies=Math.max(maxBodies,game.bodies.size());maxParticles=Math.max(maxParticles,game.particles.size());
        }
        clock++;if(moving)movingClock++;if(moving&&eligible)eligibleClock++;check(Double.isFinite(game.xp)&&game.xp>=0,"Finite XP");
        check(game.reservoir>=0&&game.reservoir<=game.capacity()+1e-7,"Reservoir bound");
        check(game.bodies.size()<=150&&game.particles.size()<=240,"World budgets");
    }
    private boolean buy(int node){double price=game.cost(node);GravityWellGame.Node n=GravityWellGame.NODES[node];
        if(!game.purchase(node))return false;spent+=price;purchases++;
        purchaseRows.add(String.format(Locale.US,"%s,%.3f,%s,%d,%.0f,%d,%d",name,clock/60,n.id,game.ranks[node],price,game.nodeSource(node),game.nodeMaterialCost(node)));return true;}
    private void shopping(){
        if(strategy.equals("none"))return;
        // Reference route funds main gameplay tools first. Optional completion is measured afterward.
        while(true){int best=-1;for(int i=0;i<game.ranks.length;i++)if(wanted(i)&&GravityWellBalance.main(i)&&game.blocked(i).startsWith("Buy")&&(best<0||game.cost(i)<game.cost(best)))best=i;
            if(best<0)break;buy(best);}
        if(game.campaign.complete())for(int i=0;i<game.ranks.length;i++)if(wanted(i)&&!GravityWellBalance.main(i))while(buy(i)){}
    }
    private void endSession(){
        game.finish();sessions++;earned+=game.xp-lastXp;lastXp=game.xp;
        double distance=game.distance;shopping();lastXp=game.xp;
        Properties p=new Properties();game.save(p);GravityWellGame restored=GravityWellGame.restore(p,false);
        check(Arrays.equals(game.ranks,restored.ranks),"Purchases survive checkpoint");
        check(game.campaign.cursor==restored.campaign.cursor&&game.activeBossId==restored.activeBossId,"Campaign and unfinished boss survive checkpoint");
        check(Math.abs(game.xp-restored.xp)<1e-7&&Math.abs(distance-restored.distance)<1e-7,"No checkpoint credit");
        check(Arrays.equals(game.campaign.materials,restored.campaign.materials),"Materials survive checkpoint");
        game=restored;
    }
    private void snapshot(String destination,double seconds){
        rows.add(String.format(Locale.US,"%s,%s,%.3f,%.0f,%.1f,%.3f,%.3f,%.0f,%d,%.0f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%d,%d",name,destination,prep/60,seconds,game.distance,earned,game.xp,spent,ranks(),game.capacity(),
            1+.18*game.ranks[0]+.08*game.extraRanks(0),1+.2*game.ranks[1]+.08*game.extraRanks(1),GravityWellBalance.xpFactor(game),1+.08*game.extraRanks(4),1+.08*game.extraRanks(5),GravityWellBalance.yieldFactor(game),maxBodies,maxParticles));
    }
    private void campaign()throws IOException{
        double stageBegan=0,stageEarned=0,stageSpent=0,stageEligible=0;int sessionLimit=guided?1800:900;
        while(!game.campaign.complete()){
            GravityWellCampaign.Destination d=game.campaign.next();int cursor=game.campaign.cursor;
            check(game.selectDestination(d.id),"Select next destination "+d.id);double elapsed=0,xpAt=earned,objectiveSeconds=-1,eligibleFight=0,objectiveEligibleAt=0,objectiveWallAt=0;
            if(d.boss>=0&&strategy.equals("balanced")){int owned=0,available=0;for(int i=0;i<game.ranks.length;i++)if(GravityWellBalance.main(i)&&GravityWellBalance.unlockStage(i)<=cursor/4){owned+=game.ranks[i];available+=GravityWellGame.NODES[i].max;}
                check(owned==available,"Normal preparation funds all main upgrades BEFORE boss "+d.id+": "+owned+"/"+available);}
            while(game.campaign.cursor==cursor){
                game.start(0);game.campaignWindow(true,true);int time=0;
                TrainingCore.Runner trainer=guided?guidedRunner():null;
                while((guided||game.campaign.cursor==cursor)&&time<sessionLimit){
                    if(trainer==null){if(d.boss>=0&&game.campaign.cursor==cursor)eligibleFight++;second();}else{
                        GameWorkoutAdapter.Frame f=new GameWorkoutAdapter.Frame(trainer);boolean moving=f.phase!=TrainingCore.Phase.RECOVERY;
                        if(d.boss>=0&&game.campaign.cursor==cursor&&f.campaignWorkEligible(moving))eligibleFight++;
                        second(moving,f.campaignWorkEligible(moving),f.campaignBossWindow(moving));trainer.tick((time+1)*1000L,moving,true);
                    }
                    time++;elapsed++;if(game.campaign.cursor!=cursor&&objectiveSeconds<0){objectiveSeconds=elapsed;objectiveEligibleAt=eligibleClock;objectiveWallAt=clock;}
                    if(d.boss>=0&&game.campaign.cursor==cursor)check(elapsed<GravityWellBalance.UNPREPARED_MINUTES[cursor/4]*60*4,"Boss stalled: "+d.id+" mass="+game.bossMass());
                }
                if(trainer!=null)check(trainer.finished&&game.running,"Trainer finishes independently of campaign objective");
                endSession();
            }
            if(d.boss<0){prep+=guided?d.seconds:elapsed;if(!guided)check(Math.abs(elapsed-d.seconds)<1.1,"Normal preparation budget");}
            else {bossTime+=eligibleFight;bossRows.add(String.format(Locale.US,"%s,%s,%.0f,%d,%.0f,%.3f,%.1f,%.0f",name,GravityWellGame.KINDS[d.boss].id,eligibleFight,ranks(),game.capacity(),earned-xpAt,game.distance,objectiveSeconds));
                if(strategy.equals("balanced"))check(eligibleFight>=240&&eligibleFight<=360,"Prepared boss is 4–6 eligible minutes");}
            snapshot(d.id,d.boss<0?0:objectiveSeconds);
            if(d.boss>=0){int stage=cursor/4;int owned=0,available=0;for(int i=0;i<game.ranks.length;i++)if(GravityWellBalance.main(i)&&GravityWellBalance.unlockStage(i)<=stage){owned+=game.ranks[i];available+=GravityWellGame.NODES[i].max;}
                double stageHours=(objectiveEligibleAt-stageEligible)/3600;
                if(strategy.equals("balanced"))check(stageHours>=GravityWellBalance.HOURS[stage]*.8&&stageHours<=GravityWellBalance.HOURS[stage]*1.2,"Stage stays within 20% of its budget");
                stageRows.add(String.format(Locale.US,"%s,%s,%.4f,%.4f,%d,%d,%.2f,%.2f,%.2f",name,d.id,stageHours,(objectiveWallAt-stageBegan)/3600,owned,available,earned-stageEarned,spent-stageSpent,game.xp));
                stageBegan=objectiveWallAt;stageEligible=objectiveEligibleAt;routeEligible=objectiveEligibleAt;stageEarned=earned;stageSpent=spent;}
            System.out.printf(Locale.US,"%s %s: %.0f min, ranks=%d XP=%.0f cap=%.0f%n",name,d.id,clock/60,ranks(),game.xp,game.capacity());System.out.flush();
            write();
        }
        check(game.campaign.cleared.size()==10,"All ten first clears recorded");
        double expected=0;for(int stage=0;stage<10;stage++)expected+=GravityWellBalance.preparationSeconds(stage)*3;
        check(Math.abs(prep-expected)<10,"Full preparation budget matches all authored stages");
        check(Math.abs(game.distance-movingClock*speed)<1e-4,"Skill tree never multiplies input distance");
        check(Math.abs(earned-spent-game.xp)<.01,"Earned XP reconciles to spend plus balance");
        if(strategy.equals("balanced"))check(routeEligible/3600>=43&&routeEligible/3600<=53,"Reference route within 43–53 eligible hours");
        Properties snapshot=new Properties();game.save(snapshot);Files.createDirectories(output);
        try(OutputStream out=Files.newOutputStream(output.resolve(name+"-completed.properties"))){snapshot.store(out,"In-memory simulation only; never import into a real athlete career");}
        String summary=String.format(Locale.US,"%s complete: route_hours=%.3f prep=%.1f min bosses=%.1f min sessions=%d ranks=%d spent=%.0f earned=%.0f balance=%.0f checks=%d wall=%.1fs%n",name,routeEligible/3600,prep/60,bossTime/60,sessions,ranks(),spent,earned,game.xp,checks,(System.nanoTime()-began)/1e9);
        Files.write(output.resolve(name+"-summary.txt"),summary.getBytes(StandardCharsets.UTF_8));System.out.print(summary);write();
        if(!strategy.equals("none"))postCampaign();
    }
    private TrainingCore.Runner guidedRunner(){
        List<TrainingCore.Block>b=new ArrayList<>();TrainingCore.Category c=TrainingCore.Category.ENDURANCE;
        b.add(new TrainingCore.Block(TrainingCore.Phase.WARM_UP,c,180,2));
        for(int i=0;i<2;i++){b.add(new TrainingCore.Block(TrainingCore.Phase.WORK,c,600,5));b.add(new TrainingCore.Block(TrainingCore.Phase.RECOVERY,c,120,1));}
        b.add(new TrainingCore.Block(TrainingCore.Phase.COOL_DOWN,c,180,2));
        TrainingCore.Plan p=new TrainingCore.Plan("campaign-audit-30m-v1","Simulation fixture; not an exercise prescription.",TrainingCore.Kind.TRAINING,c,TrainingCore.Goal.ENDURANCE,b,true);
        TrainingCore.Runner r=new TrainingCore.Runner("audit-"+sessions,p,1000,true);r.resume(0);r.telemetryAvailable=true;return r;
    }
    private boolean wanted(int i){return i<9||strategy.equals("balanced")||!GravityWellGame.NODES[i].specialist&&(GravityWellGame.NODES[i].branch==0||GravityWellGame.NODES[i].branch==1||GravityWellGame.NODES[i].branch==5);}
    private void postCampaign()throws IOException{
        int target=0;for(int i=0;i<game.ranks.length;i++)if(wanted(i))target+=GravityWellGame.NODES[i].max;
        double beganAt=clock;int repeats=0,ordinary=0;
        while(ranks()<target){
            int before;do{before=ranks();shopping();}while(ranks()>before);lastXp=game.xp;
            if(ranks()==target)break;
            int missing=-1,priority=Integer.MAX_VALUE;for(int i=0;i<game.ranks.length;i++){GravityWellGame.Node n=GravityWellGame.NODES[i];
                // Harvest deterministic boss sources before rare ordinary drops, building reach first.
                int candidate=(n.source>=5?0:100)+n.layer;
                if(wanted(i)&&game.ranks[i]<n.max&&game.nodeSource(i)>=0&&game.campaign.materials[game.nodeSource(i)]<game.nodeMaterialCost(i)&&candidate<priority){missing=i;priority=candidate;}}
            int source=missing<0?-1:game.nodeSource(missing);
            int boss=source==5?7:source==6?8:source==7||source==8?9:source==9?10:source>=10?source+1:-1;
            String destination=boss<0?"free":"repeat:"+GravityWellGame.KINDS[boss].id;
            check(game.selectDestination(destination),"Select required material harvest "+destination);
            game.start(0);game.campaignWindow(true,true);int sec=0;
            do{second();sec++;check(sec<=3600,"Post-campaign harvest stalled");}
            while(boss>=0?!game.campaign.selected.equals("free"):sec<300);
            endSession();if(boss>=0)repeats++;else ordinary++;
            check(repeats+ordinary<1000,"Tree must not require endless harvesting");
        }
        snapshot("post_campaign_max",clock-beganAt);write();
        String result=String.format(Locale.US,"%s maxed selected build: ranks=%d/%d extra=%.2f min boss_repeats=%d ordinary_harvests=%d spent=%.0f balance=%.0f%n",name,ranks(),target,(clock-beganAt)/60,repeats,ordinary,spent,game.xp);
        Files.write(output.resolve(name+"-post-campaign.txt"),result.getBytes(StandardCharsets.UTF_8));System.out.print(result);
        Properties p=new Properties();game.save(p);try(OutputStream out=Files.newOutputStream(output.resolve(name+"-maxed.properties"))){p.store(out,"In-memory simulation: organically harvested maxed build");}
    }
    private void write()throws IOException{
        Files.write(output.resolve(name+"-destinations.csv"),rows,StandardCharsets.UTF_8);
        Files.write(output.resolve(name+"-bosses.csv"),bossRows,StandardCharsets.UTF_8);
        Files.write(output.resolve(name+"-purchases.csv"),purchaseRows,StandardCharsets.UTF_8);
        Files.write(output.resolve(name+"-stages.csv"),stageRows,StandardCharsets.UTF_8);
    }
    public static void main(String[] args)throws Exception{
        if(args.length<5||args.length>6)throw new IllegalArgumentException("name input speed balanced|focused|none output-directory [post-only]");
        GravityWellProgressionSimulation simulation=new GravityWellProgressionSimulation(args);
        if(args.length==5)simulation.campaign();else{
            Properties p=new Properties();try(InputStream in=Files.newInputStream(simulation.output.resolve(simulation.name+"-completed.properties"))){p.load(in);}
            simulation.game=GravityWellGame.restore(p,false);simulation.clock=Math.rint(simulation.game.distance/simulation.speed);
            simulation.rows.clear();simulation.rows.addAll(Files.readAllLines(simulation.output.resolve(simulation.name+"-destinations.csv"),StandardCharsets.UTF_8));
            simulation.bossRows.clear();simulation.bossRows.addAll(Files.readAllLines(simulation.output.resolve(simulation.name+"-bosses.csv"),StandardCharsets.UTF_8));
            simulation.purchaseRows.clear();simulation.purchaseRows.addAll(Files.readAllLines(simulation.output.resolve(simulation.name+"-purchases.csv"),StandardCharsets.UTF_8));
            for(int i=1;i<simulation.purchaseRows.size();i++)simulation.spent+=Double.parseDouble(simulation.purchaseRows.get(i).split(",")[4]);
            simulation.earned=simulation.spent+simulation.game.xp;simulation.lastXp=simulation.game.xp;
            for(int minutes:GravityWellCampaign.PREPARATION_MINUTES)simulation.prep+=minutes*3*60;
            simulation.postCampaign();
        }
    }
}


