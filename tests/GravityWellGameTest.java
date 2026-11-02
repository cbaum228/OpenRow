package org.openrow.probe;
import java.util.*;
public final class GravityWellGameTest {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static void near(double a,double b,String message){check(Math.abs(a-b)<.0001,message+" "+a+" / "+b);}
    public static void main(String[] args){
        for(int type:new int[]{GravityWellGame.PLANET,GravityWellGame.SUN,GravityWellGame.COMET})for(int side:new int[]{-1,1}){
            GravityWellGame entry=new GravityWellGame();entry.start(0);
            boolean boss=GravityWellGame.KINDS[type].boss;entry.spawnKind(type,boss);GravityWellGame.Body body=entry.bodies.get(0);
            body.x=0;body.y=side*(boss?259:171);body.vx=-side*80;body.vy=-side*35;
            double x=body.x,y=body.y,vx=body.vx,vy=body.vy;
            entry.advance(GravityWellGame.STEP,.5,true,true);
            check(body.captured,"Rear and front approaches both capture");
            check(body.vx*vx+body.vy*vy>0,"Capture preserves the incoming heading");
            check(Math.hypot(body.vx-vx,body.vy-vy)<=140*GravityWellGame.STEP+.0001,"Capture acceleration is bounded rather than replacing velocity");
            check((body.x-x)*vx+(body.y-y)*vy>0,"First captured frame continues along the incoming path");
            double previousVx=body.vx,previousVy=body.vy;
            for(int frame=0;frame<240;frame++){
                entry.advance(GravityWellGame.STEP,.5,true,true);
                check(Math.hypot(body.vx-previousVx,body.vy-previousVy)<=140*GravityWellGame.STEP+.0001,"Orbit settling has no later velocity snap");
                check(Double.isFinite(body.x)&&Double.isFinite(body.y)&&Math.hypot(body.x,body.y)<400,"Capture remains finite and bound near the well");
                previousVx=body.vx;previousVy=body.vy;
            }
            double settledRadius=body.orbitRadius*(.62+.38*Math.sqrt(body.mass/body.initialMass));
            check(Math.hypot(body.x-Math.cos(body.orbitPhase)*settledRadius,body.y-Math.sin(body.orbitPhase)*settledRadius*.58)<30,"Incoming motion settles onto the feeding orbit");
            Properties inFlight=new Properties();entry.save(inFlight);GravityWellGame reloaded=GravityWellGame.restore(inFlight);
            entry.advance(.1,.5,true,true);reloaded.advance(.1,.5,true,true);
            near(body.x,reloaded.bodies.get(0).x,"Capture handoff survives saving without a positional snap");near(body.vy,reloaded.bodies.get(0).vy,"Capture velocity survives saving");
        }

        GravityWellGame g=new GravityWellGame();g.start(300);
        for(int i=0;i<1000;i++)g.advance(.1,1,false);
        near(g.xp,0,"No inactive XP");near(g.seconds,0,"No inactive time");check(g.bodies.isEmpty(),"No inactive matter");
        g.input(10,4,200);check(g.xp>0,"Easy input earns dust XP");near(g.distance,10,"Exact distance");check(g.strokes==4,"Exact strokes");
        double xp=g.xp;for(int i=0;i<100;i++)g.advance(.1,1,true);near(g.xp,xp,"Physics cannot create XP without input");
        Properties p=new Properties();g.save(p);GravityWellGame restored=GravityWellGame.restore(p);near(g.distance,restored.distance,"Save distance");near(g.xp,restored.xp,"Save XP");check(g.bodies.size()==restored.bodies.size(),"World saved");
        for(int i=0;i<100;i++){g.advance(.05,.5,true);restored.advance(.05,.5,true);}near(g.reservoir,restored.reservoir,"Seeded restore");
        double prior=g.distance;g.input(Double.NaN,1,100);near(prior,g.distance,"Invalid input rejected");
        g.finish();xp=g.xp;g.input(100,100,50000);g.advance(.1,1,true);near(xp,g.xp,"No finished earnings");
        g.xp=1000;g.distance=1000;double fitness=g.distance;check(g.purchase(0),"Buy root");check(!g.purchase(5),"Prerequisites enforced");near(fitness,g.distance,"Upgrade cannot multiply meters");g.start(0);check(!g.purchase(1),"No in-session purchases");
        GravityWellGame longRun=run(1800,false),split=run(1800,true);near(longRun.distance,split.distance,"Equal split distance");near(longRun.xp,split.xp,"Equal split reward");check(longRun.bodies.size()<=150,"Bounded bodies");
        check(longRun.captures>0,"Ordinary asteroids are catchable at steady input");
        for(GravityWellGame.Body b:longRun.bodies)check(Double.isFinite(b.x)&&Double.isFinite(b.y),"Stable trajectories");
        GravityWellGame timed=new GravityWellGame();timed.start(300);for(int i=0;i<18001;i++)timed.advance(GravityWellGame.STEP,.5,true);check(!timed.running,"Five-minute completion");near(300,timed.seconds,"Precise session limit");
        GravityWellGame coast=new GravityWellGame();coast.start(300);coast.input(10,3,200);coast.advance(.1,1,true,true);
        double earned=coast.xp,stored=coast.reservoir,motion=coast.seconds,phase=coast.visualPhase,elapsed=coast.sessionSeconds;
        for(int i=0;i<100;i++)coast.advance(.1,0,true,false);
        near(earned,coast.xp,"Coasting cannot earn XP");near(stored,coast.reservoir,"Coasting cannot earn stored matter");near(motion,coast.seconds,"Coasting is not rowing time");near(elapsed+10,coast.sessionSeconds,"Coasting advances session clock");check(coast.visualPhase>phase,"Coasting continues visual rotation");
        phase=coast.visualPhase;elapsed=coast.sessionSeconds;coast.advance(.1,0,false,false);near(phase,coast.visualPhase,"Explicit pause freezes rotation");near(elapsed,coast.sessionSeconds,"Explicit pause freezes clock");
        Properties coastSave=new Properties();coast.save(coastSave);GravityWellGame coastRestore=GravityWellGame.restore(coastSave);near(coast.sessionMotionSeconds,coastRestore.sessionMotionSeconds,"Restore separate motion time");near(coast.visualPhase,coastRestore.visualPhase,"Restore continuous phase");
        GravityWellGame orbit=new GravityWellGame();orbit.start(0);GravityWellGame.Body planet=new GravityWellGame.Body(150,0,-20,20,60,false);planet.material=3;orbit.bodies.add(planet);
        orbit.advance(.1,.5,true);check(planet.captured&&orbit.captures==1,"Planet captured once on entry");double mass=planet.mass;check(mass<60&&mass>59,"Captured planet erodes gradually");
        double angle=planet.orbitPhase;for(int i=0;i<100;i++)orbit.advance(.1,.5,true);check(planet.orbitPhase>angle&&orbit.bodies.contains(planet),"Planet remains in orbit while shedding");check(!orbit.particles.isEmpty()&&orbit.particles.size()<=240,"Bounded visible ejecta");near(0,orbit.xp,"Shedding alone never earns XP");
        double bank=orbit.reservoir;mass=planet.mass;angle=planet.orbitPhase;for(int i=0;i<30;i++)orbit.advance(.1,0,true,false);near(bank,orbit.reservoir,"Coasting shedding cannot bank matter");check(planet.mass<mass&&planet.orbitPhase>angle,"Coasting continues orbit and erosion");
        mass=planet.mass;angle=planet.orbitPhase;orbit.advance(.1,1,false);near(mass,planet.mass,"Explicit pause freezes erosion");near(angle,planet.orbitPhase,"Explicit pause freezes orbit");
        Properties orbitSave=new Properties();orbit.save(orbitSave);GravityWellGame orbitCopy=GravityWellGame.restore(orbitSave);GravityWellGame.Body copy=orbitCopy.bodies.get(0);near(planet.mass,copy.mass,"Persist stripped mass");near(planet.orbitPhase,copy.orbitPhase,"Persist orbit phase");check(copy.material==3,"Persist surface type");
        for(int i=0;i<30;i++){orbit.advance(.1,.5,true);orbitCopy.advance(.1,.5,true);}near(orbit.reservoir,orbitCopy.reservoir,"Orbital accounting deterministic after restore");near(planet.x,copy.x,"Orbital position deterministic after restore");
        GravityWellGame whole=new GravityWellGame(),fragments=new GravityWellGame();whole.start(0);fragments.start(0);GravityWellGame.Body a=new GravityWellGame.Body(160,0,0,0,60,false),b=new GravityWellGame.Body(160,0,0,0,60,false);a.captured=b.captured=true;a.orbitRadius=b.orbitRadius=170;whole.bodies.add(a);fragments.bodies.add(b);check(fragments.fragment(b,4),"Fragmentation hook accepts captured body");double total=0;for(GravityWellGame.Body part:fragments.bodies)total+=part.mass;near(60,total,"Fragmentation conserves mass");check(!fragments.fragment(b,4),"Removed parent cannot be fragmented twice");
        for(int i=0;i<10;i++){whole.advance(.1,.5,true);fragments.advance(.1,.5,true);}check(fragments.reservoir>whole.reservoir,"More exposed fragment surface sheds faster");
        GravityWellGame vanish=new GravityWellGame();vanish.start(0);GravityWellGame.Body pebble=new GravityWellGame.Body(100,0,0,0,1,false);vanish.bodies.add(pebble);for(int i=0;i<300;i++)vanish.advance(.1,0,true,false);check(!vanish.bodies.contains(pebble),"Exhausted body disappears");check(vanish.reservoir==0&&vanish.xp==0,"Idle erosion grants no progress");
        Properties legacy=new Properties();new GravityWellGame().save(legacy);legacy.setProperty("game.bodies","1");legacy.setProperty("game.body0","150,0,0,0,20,false,true");GravityWellGame old=GravityWellGame.restore(legacy);old.start(0);old.advance(.1,.5,true);check(old.bodies.get(0).orbitRadius>0,"Legacy captured bodies migrate to orbit");
        GravityWellGame.Body stretching=new GravityWellGame.Body(170,0,0,0,60,false);near(GravityWellGame.tidalStretch(stretching),1,"Uncaptured worlds remain spherical");
        stretching.captured=true;near(GravityWellGame.tidalStretch(stretching),1,"Fresh captures start unstretched");stretching.mass=30;near(GravityWellGame.tidalStretch(stretching),1,"Solid remnants stay compact instead of stretching into ovals");
        check(GravityWellGame.erosionRadius(stretching,Math.PI/2)<=GravityWellGame.erosionRadius(stretching,0),"Polar mantle does not outlast outward mantle");
        check(GravityWellGame.erosionRadius(stretching,Math.PI)>.37,"Core remains behind the exposed face");
        for(int direction=0;direction<8;direction++){double previous=2;for(int i=100;i>=0;i--){stretching.mass=stretching.initialMass*i/100.0;double r=GravityWellGame.erosionRadius(stretching,direction*Math.PI/4);check(r<=previous+1e-8&&r>=0&&r<=1,"All contour directions recede without regrowing");previous=r;}}
        stretching.mass=stretching.initialMass*.12;near(GravityWellGame.erosionRadius(stretching,0),.37,"Stripped mantle leaves a compact core");stretching.mass=0;near(GravityWellGame.erosionRadius(stretching,1),0,"Core eventually disappears");
        GravityWellGame edgeGame=new GravityWellGame();edgeGame.start(0);GravityWellGame.Body half=new GravityWellGame.Body(170,0,0,0,60,false);half.mass=30;half.captured=true;half.orbitRadius=170;edgeGame.bodies.add(half);
        edgeGame.advance(.2,.5,true);check(!edgeGame.particles.isEmpty(),"Eroding edge emits fragments at low initial intensity");
        double outwardX=half.x/Math.hypot(half.x,half.y),outwardY=half.y/Math.hypot(half.x,half.y),radius=GravityWellGame.visualRadius(half);
        for(GravityWellGame.Particle particle:edgeGame.particles){double dx=particle.x-half.x,dy=particle.y-half.y;check(dx*outwardX+dy*outwardY<radius*.3,"Debris sheds from inward face and detached mantle");check(Double.isFinite(particle.x)&&Double.isFinite(particle.y)&&Math.hypot(dx,dy)<Math.hypot(half.x,half.y)+radius*2,"Surface and detached-mantle debris stays within the inward flow");}
        GravityWellGame ringGame=new GravityWellGame();ringGame.start(0);GravityWellGame.Body ringed=new GravityWellGame.Body(170,0,0,0,60,false);ringed.material=2;ringGame.bodies.add(ringed);
        near(GravityWellGame.ringRemaining(ringed),1,"Flyby planets retain their rings");
        for(int i=0;i<12;i++)ringGame.advance(.1,.5,true);check(GravityWellGame.ringRemaining(ringed)>0&&GravityWellGame.ringRemaining(ringed)<.6,"Rings strip rapidly during capture");check(!ringGame.particles.isEmpty(),"Ring capture produces bounded visual debris");
        Properties ringSave=new Properties();ringGame.save(ringSave);GravityWellGame restoredRings=GravityWellGame.restore(ringSave);near(GravityWellGame.ringRemaining(ringed),GravityWellGame.ringRemaining(restoredRings.bodies.get(0)),"Ring consumption survives save and restore");
        for(int i=0;i<13;i++)ringGame.advance(.1,.5,true);near(GravityWellGame.ringRemaining(ringed),0,"Rings are gone within 2.4 seconds");check(ringed.mass>ringed.initialMass*.8,"Planet remains mostly intact after ring consumption");near(ringGame.xp,0,"Decorative ring debris cannot grant XP");check(ringGame.particles.size()<=240,"Ring debris shares existing particle cap");
        GravityWellGame.Body ringShape=new GravityWellGame.Body(180,0,0,0,60,false);ringShape.material=2;ringShape.captured=true;float[] pointA=new float[3],pointB=new float[3];
        GravityWellGame.ringPoint(ringShape,.5,0,pointA,0);GravityWellGame.ringPoint(ringShape,.5,Math.PI,pointB,0);near(pointA[0]+pointB[0],0,"Fresh ring is centered on its planet");
        ringShape.captureAge=1.2;GravityWellGame.ringPoint(ringShape,.5,0,pointA,0);GravityWellGame.ringPoint(ringShape,.5,Math.PI,pointB,0);check((pointA[0]+pointB[0])*.5<-45,"Whole ring detaches and translates inward");check(Math.abs(pointA[1]+pointB[1])>1,"Detached ring is sheared away from a centered ellipse");
        ringShape.captureAge=2.4;GravityWellGame.ringPoint(ringShape,.5,0,pointA,0);GravityWellGame.ringPoint(ringShape,.5,Math.PI,pointB,0);near(pointA[0],pointB[0],"Ring sectors converge into the well");near(pointA[1],0,"Ring flow finishes at the well");
        GravityWellGame.surfacePit(ringShape,1,pointA);near(pointA[2],0,"Fresh planet has no exposed surface pits");ringShape.mass=54;
        for(int i=0;i<3;i++){GravityWellGame.surfacePit(ringShape,i,pointA);check(pointA[2]>0,"Visible inward-facing surface opens during erosion");check(Math.hypot(pointA[0],pointA[1])-pointA[2]>.37,"Surface pits preserve the robust core");}
        ringShape.mass=6;GravityWellGame.surfacePit(ringShape,1,pointA);near(pointA[2],0,"Core is not punctured by mantle pits");
        GravityWellGame.Body mantle=new GravityWellGame.Body(180,0,0,0,60,false);mantle.captured=true;
        near(GravityWellGame.mantleProgress(mantle,0),0,"Intact mantle has no detached fragments");
        mantle.mass=54;check(GravityWellGame.mantleProgress(mantle,0)>0,"Outer mantle begins peeling early");near(GravityWellGame.mantleProgress(mantle,6),0,"Inner mantle holds while outer layer detaches");near(GravityWellGame.mantleProgress(mantle,12),0,"Core holds while mantle detaches");
        GravityWellGame.mantlePoint(mantle,0,pointA);double initialX=pointA[0];mantle.mass=48;GravityWellGame.mantlePoint(mantle,0,pointA);check(pointA[0]<initialX,"Detached chunk travels inward as more mass is consumed");
        mantle.mass=36;check(GravityWellGame.mantleProgress(mantle,6)>0,"Inner layers peel after outer mantle");near(GravityWellGame.mantleProgress(mantle,12),0,"Core still holds after inner mantle begins peeling");
        mantle.mass=6;check(GravityWellGame.mantleProgress(mantle,12)>0,"Core finally breaks into small chunks");
        mantle.mass=0;for(int i=0;i<15;i++){near(GravityWellGame.mantleProgress(mantle,i),1,"Exhausted body retains no detached fragments");GravityWellGame.mantlePoint(mantle,i,pointA);near(pointA[0],-180,"Fragments converge into the well");near(pointA[1],0,"Fragments finish on the well plane");}
        p.setProperty("game.v0","NaN");try{GravityWellGame.restore(p);throw new AssertionError("Bad save accepted");}catch(IllegalArgumentException expected){checks++;}
        System.out.println("PASS: "+checks+" Gravity Well checks; 30-minute XP="+longRun.xp+", bodies="+longRun.bodies.size());
    }
    static GravityWellGame run(int seconds,boolean split){GravityWellGame g=new GravityWellGame();g.start(0);for(int i=0;i<seconds*60;i++){if(split&&i>0&&i%(300*60)==0){g.finish();Properties p=new Properties();g.save(p);g=GravityWellGame.restore(p);g.start(0);}g.input(2.5/60,0,120.0/60);g.advance(GravityWellGame.STEP,.5,true);}return g;}
}


