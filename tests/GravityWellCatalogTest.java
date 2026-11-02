package org.openrow.probe;
import java.util.*;

/** Encounter progression, physical fragmentation and migration independent of Android rendering. */
public final class GravityWellCatalogTest {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static void near(double a,double b,String message){check(Math.abs(a-b)<.0001,message+" "+a+" / "+b);}
    static GravityWellGame boss(int kind){GravityWellGame g=new GravityWellGame();g.start(0);g.spawnKind(kind,true);GravityWellGame.Body b=g.bodies.get(0);b.x=240;b.y=0;b.captured=true;b.orbitRadius=240;b.captureAge=5;return g;}
    public static void main(String[] args){
        check(GravityWellGame.KINDS.length==17,"Complete initial body catalog");
        double previousEnergy=-1;for(int input=0;input<=100;input++){double energy=GravityWellGame.visualEnergy(input/100.0);check(energy>=previousEnergy&&energy>=0&&energy<=1,"Rowing visual response is monotonic and bounded");previousEnergy=energy;}
        near(GravityWellGame.visualEnergy(-1),0,"Negative effort cannot drive effects");near(GravityWellGame.visualEnergy(5),1,"Effects saturate at their fixed maximum");near(GravityWellGame.visualEnergy(Double.NaN),0,"Invalid effort produces a safe visual response");
        GravityWellGame easyEffects=boss(GravityWellGame.NEUTRON),hardEffects=boss(GravityWellGame.NEUTRON);easyEffects.field=.1;hardEffects.field=1;
        for(int frame=0;frame<60;frame++){easyEffects.advance(GravityWellGame.STEP,.1,true,true);hardEffects.advance(GravityWellGame.STEP,1,true,true);}
        check(hardEffects.particles.size()>easyEffects.particles.size()*1.5,"Harder rowing produces visibly denser decorative particles");check(hardEffects.particles.size()<=240,"Hard rowing preserves the global particle cap");
        GravityWellScene responseScene=new GravityWellScene();responseScene.prepare(hardEffects);near(responseScene.stellarBody().visualEnergy,hardEffects.visualEnergy(),"Boss beams and corona receive the shared rowing response");
        double oldEnergy=hardEffects.visualEnergy();hardEffects.advance(GravityWellGame.STEP,0,true,false);check(hardEffects.visualEnergy()<oldEnergy&&hardEffects.visualEnergy()>.9,"Easing off decays effects smoothly without an immediate blackout");
        int pausedParticles=hardEffects.particles.size();hardEffects.advance(.2,1,false,true);check(hardEffects.particles.size()==pausedParticles,"Paused effects cannot spawn additional particles");

        GravityWellGame clearVisuals=boss(GravityWellGame.SUN),saturatedVisuals=boss(GravityWellGame.SUN);
        for(int i=0;i<240;i++)saturatedVisuals.particles.add(new GravityWellGame.Particle(500,100,0,0,10,2));
        for(int frame=0;frame<120;frame++){clearVisuals.advance(GravityWellGame.STEP,1,true,true);saturatedVisuals.advance(GravityWellGame.STEP,1,true,true);}
        near(clearVisuals.bossMass(),saturatedVisuals.bossMass(),"Decorative particle capacity cannot change consumed mass");near(clearVisuals.reservoir,saturatedVisuals.reservoir,"Decorative particle density cannot create stored matter");near(clearVisuals.xp,saturatedVisuals.xp,"Decorative effects do not award additional XP");check(saturatedVisuals.particles.size()<=240,"A saturated effect scene preserves its particle cap");
        GravityWellGame focused=boss(GravityWellGame.SUN);GravityWellGame.Body featured=focused.bodies.get(0);GravityWellGame.Body passer=new GravityWellGame.Body(380,30,-5,3,50,false);passer.kind=GravityWellGame.MOON;focused.bodies.add(passer);
        GravityWellScene focusedScene=new GravityWellScene();double lastFocus=-1;
        for(int frame=0;frame<=120;frame++){featured.flightAge=frame/60.0;focusedScene.prepare(focused);focusedScene.body(passer);double strength=focused.stellarFocus();check(strength>=lastFocus&&strength<=1,"Stellar focus ramps smoothly and stays bounded");near(passer.visualOpacity,1-.68*strength,"Background bodies fade with encounter focus");near(focusedScene.body(featured).visualOpacity,1,"The featured stellar body retains full opacity");lastFocus=strength;}
        near(lastFocus,1,"Encounter reaches full focus after two seconds");near(passer.mass,50,"Visual focus cannot consume background mass");
        focused.activeBossId=0;focusedScene.prepare(focused);focusedScene.body(passer);near(passer.visualOpacity,1,"Background objects regain full visibility after the encounter");
        GravityWellGame planetaryFocus=boss(GravityWellGame.PLANET);planetaryFocus.bodies.get(0).flightAge=20;near(planetaryFocus.stellarFocus(),0,"Existing planetary encounter presentation is unchanged");
        GravityWellGame quietStar=boss(GravityWellGame.SUN);quietStar.bodies.get(0).flightAge=3;for(int frame=0;frame<600;frame++)quietStar.advance(GravityWellGame.STEP,.5,true,true);int visibleDust=0;for(GravityWellGame.Body body:quietStar.bodies)if(body.dust)visibleDust++;check(visibleDust<=4,"Stellar encounters greatly reduce new ambient dust");

        for(double length:new double[]{150,300,600}){
            GravityWellGame.Body ribbon=new GravityWellGame.Body(400,0,0,0,50,false);ribbon.trailAge=length/95*2.3;
            double nearest=1,farthest=0;for(int grain=0;grain<20;grain++){double u=GravityWellScene.grainPosition(ribbon,grain,0,length);nearest=Math.min(nearest,u);farthest=Math.max(farthest,u);}
            check(nearest<.15&&farthest>.85,"Established grains cover the entire stream including long comet inlets");
            ribbon.trailAge=.1;for(int grain=0;grain<20;grain++){double u=GravityWellScene.grainPosition(ribbon,grain,0,length);check(u<0||u<=GravityWellScene.trailReach(ribbon,length),"Long streams still grow from their source without prefilled grains");}
        }
        for(int stellarKind:new int[]{GravityWellGame.SUN,GravityWellGame.RED_GIANT})for(int age=0;age<40;age++){
            GravityWellGame ejecta=boss(stellarKind);GravityWellGame.Body origin=ejecta.bodies.get(0);origin.mass*=.5;origin.captureAge=age*.4;origin.shedClock=.074;
            ejecta.advance(GravityWellGame.STEP,.5,true,true);check(ejecta.particles.size()==1,"Stellar fixture emits one surface particle");GravityWellGame.Particle particle=ejecta.particles.get(0);
            check(Math.hypot(particle.x-origin.x,particle.y-origin.y)<=GravityWellGame.visualRadius(origin)+1,"Stellar ejecta originates on the star rather than an invisible rocky mantle path");
        }

        for(int stellarKind:new int[]{GravityWellGame.SUN,GravityWellGame.RED_GIANT})for(int rank=1;rank<=3;rank++){
            GravityWellGame continuity=boss(stellarKind);continuity.ranks[7]=rank;GravityWellGame.Body parent=continuity.bodies.get(0);parent.mass*=.9;parent.captureAge=4.2;parent.trailAge=4.2;
            GravityWellScene visual=new GravityWellScene();visual.prepare(continuity);GravityWellGame.Body before=visual.stellarBody();
            check(before!=null&&before.visualStreams==rank+1,"Stellar stream layout exists before the physical split");
            double beforeShear=before.visualShear;check(beforeShear>0&&beforeShear<1,"Extra filaments fade in gradually around shearing");
            float[] points=new float[(rank+1)*6],sample=new float[2];
            for(int branch=0;branch<=rank;branch++)for(int part=0;part<3;part++){GravityWellScene.flowPoint(before,branch,rank+1,part*.5,0,sample);int index=branch*6+part*2;points[index]=sample[0];points[index+1]=sample[1];}
            continuity.fragment(parent,rank+1);visual.prepare(continuity);GravityWellGame.Body after=visual.stellarBody();near(after.visualShear,beforeShear,"Physical splitting cannot change cloud opacity");
            for(int branch=0;branch<=rank;branch++)for(int part=0;part<3;part++){GravityWellScene.flowPoint(after,branch,rank+1,part*.5,0,sample);int index=branch*6+part*2;near(sample[0],points[index],"Filaments do not jump at the split");near(sample[1],points[index+1],"Filament geometry stays continuous");}
            for(int step=0;step<=20;step++){after.visualShear=step/20.0;double total=0;for(int branch=0;branch<=rank;branch++)total+=GravityWellScene.streamWeight(after,branch)/GravityWellScene.streamCount(after);near(total,1,"Filaments share a constant combined brightness budget");}
            GravityWellGame unsplit=boss(stellarKind),split=boss(stellarKind);split.ranks[7]=rank;GravityWellGame.Body fracture=split.bodies.get(0);split.fragment(fracture,rank+1);
            unsplit.spawnDust=split.spawnDust=-1000;for(int step=0;step<60;step++){unsplit.advance(GravityWellGame.STEP,.5,true,true);split.advance(GravityWellGame.STEP,.5,true,true);}
            check(Math.abs(unsplit.particles.size()-split.particles.size())<=4,"Shearing shares the particle emission budget across physical pieces");
        }

        GravityWellGame quiet=boss(GravityWellGame.SUN);quiet.spawnDust=0;quiet.spawnRock=2;
        for(int i=0;i<100;i++)quiet.advance(.1,.5,true,true);
        int ordinary=0,dust=0;for(GravityWellGame.Body b:quiet.bodies){if(b.dust)dust++;else if(b.bossId==0)ordinary++;}
        check(ordinary==0,"Ordinary object spawning pauses during a boss");
        near(quiet.spawnRock,2,"Boss time cannot accumulate an ordinary spawn backlog");
        check(dust<=17,"Boss encounter sharply reduces dust spawning");
        GravityWellGame.Body wake=new GravityWellGame.Body(400,0,-30,40,22,false);wake.kind=GravityWellGame.COMET;float[] tail=new float[2];
        GravityWellScene.cometTailPoint(wake,1,tail);near(tail[0],400,"New comet has no preformed tail");
        wake.flightAge=3;GravityWellScene.cometTailPoint(wake,1,tail);check(tail[0]>400&&tail[1]<0,"Approaching comet keeps a wake behind its motion");
        wake.x=170;GravityWellScene.cometTailPoint(wake,1,tail);check(tail[0]<170,"Tail turns toward the well near capture");
        wake.captured=true;wake.captureAge=2.2;GravityWellScene.cometTailPoint(wake,1,tail);near(tail[0],170,"Captured comet sheds its old tail completely");

        for(int k=0;k<GravityWellGame.KINDS.length;k++){
            GravityWellGame g=new GravityWellGame();GravityWellGame.Kind kind=GravityWellGame.KINDS[k];
            g.distance=kind.gate;check(g.unlocked(k),"Class unlock at career distance");if(kind.gate>0){g.distance--;check(!g.unlocked(k),"Class remains locked before its gate");}
            g.spawnKind(k,kind.boss);GravityWellGame.Body b=g.bodies.get(0);check(b.kind==k&&b.material==kind.material,"Spawn class and material independent");check(GravityWellGame.visualRadius(b)>0,"Readable body size");
            Properties p=new Properties();g.save(p);GravityWellGame copy=GravityWellGame.restore(p);check(copy.bodies.get(0).kind==k,"Stable class id round trip");near(b.mass,copy.bodies.get(0).mass,"Class mass survives save");
            if(kind.boss){check(copy.activeBossId==b.bossId&&copy.bossKind==k,"Boss identity survives save");g.spawnKind(k,true);check(g.bossPieces()==1,"One boss at a time");}
        }
        GravityWellGame locked=new GravityWellGame();locked.start(0);for(int i=0;i<1000;i++)locked.advance(.1,.5,true,true);
        check(locked.activeBossId==0,"No locked bosses at zero career distance");for(GravityWellGame.Body b:locked.bodies)if(!b.dust)check(b.kind<=GravityWellGame.ASTEROID,"Only unlocked ordinary classes spawn");
        GravityWellGame schedule=new GravityWellGame();schedule.distance=900;schedule.start(0);
        for(int i=0;i<500;i++)schedule.advance(.1,0,true,false);near(schedule.bossClock,0,"Coasting does not advance encounter director");
        for(int i=0;i<451;i++)schedule.advance(.1,.5,true,true);check(schedule.activeBossId>0&&schedule.bossKind==GravityWellGame.PLANET,"Highest unlocked boss arrives after rowing");
        double clock=schedule.bossClock,mass=schedule.bossMass(),xp=schedule.xp;for(int i=0;i<200;i++)schedule.advance(.1,0,true,false);
        near(schedule.bossMass(),mass,"Boss progress waits for rowing while coasting");near(schedule.xp,xp,"Coasting boss creates no XP");near(schedule.bossClock,clock,"No queued bosses during encounter");
        Properties midway=new Properties();schedule.save(midway);GravityWellGame restored=GravityWellGame.restore(midway);near(restored.bossMass(),mass,"Boss health restores");near(restored.bossClock,clock,"Encounter clock restores");
        restored.advance(.1,.5,false);near(restored.bossMass(),mass,"Explicit pause freezes boss");
        for(int rank=1;rank<=3;rank++){
            GravityWellGame g=boss(GravityWellGame.PLANET);GravityWellGame.Body parent=g.bodies.get(0);parent.mass*=.9;double before=parent.mass;g.ranks[6]=rank;
            g.advance(GravityWellGame.STEP,.5,true,true);check(g.bossPieces()==rank+1,"Planetary upgrade creates correct chunk count");near(g.bossMass(),before,"Automatic split conserves exact remaining mass");
            for(GravityWellGame.Body child:g.bodies)if(child.bossId>0)check(child.fragmentDepth==1&&!GravityWellGame.hasRings(child),"Chunks cannot recursively split or grow rings");
            Properties p=new Properties();g.save(p);GravityWellGame copy=GravityWellGame.restore(p);check(copy.bossPieces()==rank+1,"Chunk count and boss group persist");near(g.bossMass(),copy.bossMass(),"Fragment mass round trip");
            double fitness=g.distance;g.advance(.1,.5,true,true);near(g.distance,fitness,"Physical fragmentation cannot fabricate rowing distance");check(g.bossPieces()==rank+1,"Upgrade does not split fragments again");
            GravityWellGame whole=boss(GravityWellGame.PLANET),split=boss(GravityWellGame.PLANET);GravityWellGame.Body original=split.bodies.get(0);double initial=original.mass;
            check(split.fragment(original,rank+1),"Boss can fragment");whole.advance(GravityWellGame.STEP,.5,true,true);split.advance(GravityWellGame.STEP,.5,true,true);
            double wholeLoss=initial-whole.bossMass(),splitLoss=initial-split.bossMass();near(splitLoss/wholeLoss,1.12+.05*(rank-1),"Bounded surface-area erosion benefit");
        }
        GravityWellGame star=boss(GravityWellGame.SUN);GravityWellGame.Body sun=star.bodies.get(0);sun.mass*=.9;star.ranks[6]=3;star.advance(GravityWellGame.STEP,.5,true,true);check(star.bossPieces()==1,"Planet upgrade cannot split stars");star.ranks[7]=1;star.advance(GravityWellGame.STEP,.5,true,true);check(star.bossPieces()==2,"Stellar upgrade splits stars");
        GravityWellGame remnant=boss(GravityWellGame.NEUTRON);GravityWellGame.Body neutron=remnant.bodies.get(0);neutron.mass*=.9;remnant.ranks[7]=3;remnant.advance(GravityWellGame.STEP,.5,true,true);check(remnant.bossPieces()==1,"Stellar upgrade cannot split remnants");remnant.ranks[8]=1;remnant.advance(GravityWellGame.STEP,.5,true,true);check(remnant.bossPieces()==2,"Remnant upgrade splits dense remnants");
        GravityWellGame noSplit=boss(GravityWellGame.GAS_GIANT);noSplit.bodies.get(0).mass*=.9;noSplit.ranks[6]=3;noSplit.advance(.1,.5,true,false);check(noSplit.bossPieces()==1,"No upgrade splitting while coasting");
        GravityWellGame finished=boss(GravityWellGame.DWARF);finished.bodies.get(0).mass=.1;double earned=finished.xp;finished.advance(GravityWellGame.STEP,.5,true,true);check(finished.activeBossId==0&&finished.bossesDefeated==1&&finished.sessionBosses==1,"Boss completion counted once");near(finished.xp,earned,"Boss completion cannot create unprocessed XP");finished.advance(.1,.5,true,true);check(finished.bossesDefeated==1,"No repeated completion bonus");
        GravityWellGame buy=new GravityWellGame();buy.distance=20000;buy.xp=10000;check(!buy.purchase(6),"Fragmentation prerequisites enforced");buy.ranks[0]=buy.ranks[1]=1;check(buy.purchase(6),"Planet upgrade purchasable between sessions");buy.start(0);check(!buy.purchase(6),"No upgrade purchases during encounters");
        Properties legacy=new Properties();new GravityWellGame().save(legacy);for(int i=6;i<9;i++)legacy.remove("game.rank"+i);legacy.setProperty("game.bodies","1");legacy.setProperty("game.body0","150,0,0,0,20,false,true,20,170,0,4,0,0,2");GravityWellGame old=GravityWellGame.restore(legacy);check(old.bodies.get(0).kind==-1&&GravityWellGame.hasRings(old.bodies.get(0)),"Legacy bodies retain appearance");check(old.ranks[6]==0&&old.ranks[7]==0&&old.ranks[8]==0,"New upgrade ranks default to zero");
        legacy.setProperty("game.body0",legacy.getProperty("game.body0")+",unknown_class,0,0,1");try{GravityWellGame.restore(legacy);throw new AssertionError("Unknown class accepted");}catch(IllegalArgumentException expected){checks++;}
        for(int k=0;k<GravityWellGame.KINDS.length;k++)if(GravityWellGame.KINDS[k].boss){
            GravityWellGame g=boss(k);g.ranks[6+GravityWellGame.KINDS[k].family]=3;
            for(int i=0;i<6000&&g.activeBossId>0;i++)g.advance(.1,.5,true,true);
            check(g.activeBossId==0&&g.bossesDefeated==1,"Every boss class and its fragments can finish");near(g.xp,0,"No boss class bypasses rowing-input XP processing");check(g.bodies.size()<=150&&g.particles.size()<=240,"Boss completion stays within world caps");
        }
        GravityWellGame display=boss(GravityWellGame.RED_GIANT);display.ranks[7]=3;GravityWellGame.Body giant=display.bodies.get(0);giant.mass*=.9;double physicalMass=display.bossMass();giant.trailAge=5;giant.flightAge=12;check(display.fragment(giant,4),"Prepare fragmented stellar encounter");
        GravityWellScene scene=new GravityWellScene();scene.prepare(display);int visible=0;GravityWellGame.Body remnantView=null;
        for(GravityWellGame.Body piece:display.bodies){GravityWellGame.Body view=scene.body(piece);if(view!=null){visible++;remnantView=view;}}
        check(visible==1&&remnantView!=null,"Stellar fragments render as one connected remnant");check(remnantView.visualStreams==4,"Upgrade renders plasma filaments rather than four little stars");near(remnantView.trailAge,5,"Stellar shearing preserves the established stream instead of restarting it");near(remnantView.flightAge,12,"Stellar animation remains continuous through fragmentation");near(remnantView.mass,physicalMass,"Remnant displays all surviving mass");near(display.bossMass(),physicalMass,"Visual grouping cannot change physical mass");check(display.bossPieces()==4,"Visual grouping preserves exposed-area mechanics");
        Properties visualSave=new Properties();display.save(visualSave);GravityWellGame savedDisplay=GravityWellGame.restore(visualSave);GravityWellScene savedScene=new GravityWellScene();savedScene.prepare(savedDisplay);check(savedScene.stellarBody()!=null&&savedScene.stellarBody().visualStreams==4,"Saved stellar fragments immediately use the new presentation");
        for(int i=display.bodies.size()-1;i>0;i--)display.bodies.remove(i);scene.prepare(display);check(scene.stellarBody()!=null&&scene.stellarBody().initialMass==display.bossInitialMass,"Final physical fragment retains the shared remnant scale");
        GravityWellGame rocky=boss(GravityWellGame.PLANET);rocky.fragment(rocky.bodies.get(0),4);scene.prepare(rocky);visible=0;for(GravityWellGame.Body piece:rocky.bodies)if(scene.body(piece)!=null)visible++;check(visible==4,"Solid planets still show separate physical chunks");
        float[] flow=new float[2];
        for(int type:new int[]{GravityWellGame.SUN,GravityWellGame.RED_GIANT})for(int direction=0;direction<32;direction++){
            GravityWellGame.Body b=boss(type).bodies.get(0);double a=direction*Math.PI/16;b.x=240*Math.cos(a);b.y=240*Math.sin(a);b.mass*=.25;b.visualStreams=4;
            near(GravityWellGame.tidalStretch(b),1,"Stellar cores do not flatten during consumption");near(GravityWellGame.inwardStretch(b),1,"Stellar upgrades stretch plasma rather than the core");
            near(GravityWellGame.erosionRadius(b,a),Math.cbrt(.25),"Star shrinks evenly with remaining mass");
            for(int branch=0;branch<4;branch++){GravityWellScene.flowPoint(b,branch,4,1,0,flow);check(Math.hypot(flow[0],flow[1])<30,"Every stellar branch reaches beneath the horizon throughout an orbit");}
        }
        GravityWellGame persistent=new GravityWellGame();persistent.start(0);persistent.spawnKind(GravityWellGame.COMET,false);GravityWellGame.Body activeComet=persistent.bodies.get(0);activeComet.x=330;activeComet.y=0;activeComet.vx=25;activeComet.vy=55;activeComet.trailAge=1;
        persistent.advance(.1,.5,true,true);check(activeComet.trailAge>1,"Established comet stream keeps growing on the outward leg of an orbit");

        for(boolean captured:new boolean[]{false,true})for(int direction=0;direction<8;direction++)for(double range:new double[]{80,200,500}){
            double angle=direction*Math.PI/4;GravityWellGame.Body comet=new GravityWellGame.Body(Math.cos(angle)*range,Math.sin(angle)*range,0,0,22,false);comet.kind=GravityWellGame.COMET;comet.material=1;comet.captured=captured;
            GravityWellScene.flowPoint(comet,0,1,0,0,flow);double startX=flow[0],startY=flow[1],startRadius=Math.hypot(startX,startY);
            for(int i=1;i<=16;i++){GravityWellScene.flowPoint(comet,0,1,i/16.0,0,flow);check(Double.isFinite(flow[0])&&Double.isFinite(flow[1])&&Math.hypot(flow[0],flow[1])<startRadius+1,"Comet tail stays inside its nucleus orbit while flowing inward");}
            check(Math.hypot(flow[0],flow[1])<startRadius,"Comet tail tip is closer to the well than its root");check(Math.hypot(flow[0],flow[1])<30,"Comet inlet terminates beneath the event horizon at every orbit angle");
        }
        GravityWellGame birthGame=new GravityWellGame();birthGame.start(0);birthGame.spawnKind(GravityWellGame.COMET,false);GravityWellGame.Body incoming=birthGame.bodies.get(0);incoming.x=500;incoming.y=0;incoming.vx=-30;incoming.vy=0;
        near(incoming.trailAge,0,"Newly spawned comet has no prebuilt trail");birthGame.advance(.1,0,true,true);near(incoming.trailAge,0,"Distant flyby does not seed an inward trail");
        incoming.x=280;incoming.vx=-30;birthGame.advance(GravityWellGame.STEP,0,true,true);check(incoming.trailAge>0,"Visible stripping starts inside the attraction region");
        check(GravityWellScene.trailReach(incoming,200)>0&&GravityWellScene.trailReach(incoming,200)<.02,"First trail segment begins at the source");check(GravityWellScene.grainPosition(incoming,10,0,200)<0,"No grains are prefilled far down the trail");
        incoming.trailAge=.5;double shortReach=GravityWellScene.trailReach(incoming,200);incoming.trailAge=1;check(GravityWellScene.trailReach(incoming,200)>shortReach&&GravityWellScene.trailReach(incoming,200)<1,"Leading front advances over time instead of appearing at full length");
        for(int grain=0;grain<20;grain++){double u=GravityWellScene.grainPosition(incoming,grain,0,200);check(u<0||u<=GravityWellScene.trailReach(incoming,200),"Grains cannot get ahead of emitted material");}
        Properties trailSave=new Properties();birthGame.save(trailSave);GravityWellGame loadedTrail=GravityWellGame.restore(trailSave);near(loadedTrail.bodies.get(0).trailAge,0,"Loading rebuilds visual trails without fabricated history");near(loadedTrail.bodies.get(0).mass,incoming.mass,"Trail rebuild preserves physical progress");
        double age=incoming.trailAge;birthGame.advance(.1,0,false,false);near(incoming.trailAge,age,"Explicit pause freezes trail development");
        incoming.trailAge=31;incoming.captured=true;incoming.orbitRadius=170;birthGame.advance(.1,0,true,false);near(incoming.trailAge,31.1,"Established trails keep advecting after their leading front is complete");
        for(int k=0;k<GravityWellGame.KINDS.length;k++)if(!GravityWellGame.KINDS[k].stellar&&k!=GravityWellGame.CLOUD){GravityWellGame.Body solid=new GravityWellGame.Body(180,0,0,0,60,false);solid.kind=k;solid.material=GravityWellGame.KINDS[k].material;solid.captured=true;solid.mass=12;
            near(GravityWellGame.tidalStretch(solid),1,"Planetary remnants do not become stretched ovals");double smallest=2,largest=0;
            for(int direction=0;direction<24;direction++){double r=GravityWellGame.erosionRadius(solid,direction*Math.PI/12);smallest=Math.min(smallest,r);largest=Math.max(largest,r);}
            check(largest/smallest<1.15,"Late solid remnant stays compact in all directions");near(GravityWellGame.erosionRadius(solid,0),GravityWellGame.erosionRadius(solid,Math.PI*2),"Faceted outline has no wrapping seam");
        }
        System.out.println("PASS: "+checks+" body catalog, boss, fragmentation, scene and migration checks");
    }
}
