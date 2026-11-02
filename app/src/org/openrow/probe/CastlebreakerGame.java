package org.openrow.probe;

import java.util.ArrayList;
import java.util.List;

/** Small deterministic siege simulation, independent of Android and USB timing. */
public final class CastlebreakerGame {
    public static final double GRAVITY=9.81, ANGLE=Math.toRadians(6);
    public static final double METERS_PER_FULL_CHARGE=20, MIN_FIRE_CHARGE=50;
    public static final double FORT_Z=44, LAUNCH_HEIGHT=4.2;
    public static final String[] FORT_NAMES={"The Watchpost","Iron Gate","Split Rampart","The Citadel"};
    public static final String[] FORT_HINTS={"Archers guard ordinary stone towers.","The knight's blue-gray keep resists light shots.","Four towers: archers outside, knights inside.","Three reinforced towers. Save for siege shots."};
    public static final class Block {
        public double x,y,z,vx,vy,vz,rotation,spin;
        public final double homeX,homeY,homeZ,w,h,d,maxHealth;
        public double health;
        public final boolean target,reinforced;
        public boolean moving,fallen;
        Block(double x,double y,double z,double w,double h,double d,boolean target,boolean reinforced) {
            this.x=homeX=x;this.y=homeY=y;this.z=homeZ=z;this.w=w;this.h=h;this.d=d;this.target=target;this.reinforced=reinforced;
            health=maxHealth=reinforced?2.8:.65;
        }
    }
    public final List<Block> blocks=new ArrayList<>();
    public double charge,aim,rockX,rockY,rockZ,rockVX,rockVY,rockVZ;
    public double impactX,impactZ,impactAge=100,armRelease=1,launchCharge;
    public boolean flying;
    public int shots,targetsDown,totalTargets,fortIndex;
    public double distanceRowed;
    public double metersPerFullCharge=METERS_PER_FULL_CHARGE;
    private int previousDistance=-1;
    public String message="Row to wind the catapult. Aim for the three red beacons.";
    private double flightAge;

    public CastlebreakerGame() { reset(); }
    public void setChargeScale(double scale){if(!Double.isFinite(scale)||scale<.1||scale>1000)throw new IllegalArgumentException("Invalid charge scale");metersPerFullCharge=METERS_PER_FULL_CHARGE*scale;}
    public double readyMeters(){return metersPerFullCharge*MIN_FIRE_CHARGE/100;}
    private String meters(double value){return String.format(java.util.Locale.US,"%.0f m",value);}
    public void selectFort(int index) {fortIndex=Math.floorMod(index,FORT_NAMES.length);reset();}
    public void reset() {
        blocks.clear(); charge=aim=distanceRowed=0;shots=targetsDown=totalTargets=0;flying=false;impactAge=100;armRelease=1;previousDistance=-1;
        message=FORT_HINTS[fortIndex]+" "+meters(readyMeters())+": standard. "+meters(metersPerFullCharge)+": siege.";
        if(fortIndex==0) {
            addTower(-9,3,false);addTower(0,3,false);addTower(9,3,false);
            for(int side:new int[]{-1,1})for(int col=0;col<2;col++)addWall(side*(3.6+col*2.3),2.8,false);
        } else if(fortIndex==1) {
            addTower(-9,3,false);addTower(0,4,true);addTower(9,3,false);
            for(int side:new int[]{-1,1})for(int col=0;col<2;col++)addWall(side*(3.6+col*2.3),3.3,col==0);
        } else if(fortIndex==2) {
            addTower(-12,2,false);addTower(-4,3,true);addTower(4,3,true);addTower(12,2,false);
            addWall(-8,2.6,false);addWall(8,2.6,false);
        } else {
            addTower(-10,3,true);addTower(0,4,true);addTower(10,3,true);
            for(int side:new int[]{-1,1})for(int col=0;col<3;col++)addWall(side*(3.5+col*1.8),3.2,true);
        }
    }
    private void addTower(double x,int levels,boolean knight) {
        for(int level=0;level<levels;level++)for(int col:new int[]{-1,1})
            blocks.add(new Block(x+col*1.05,1.1+level*2.2,FORT_Z,2,2.2,3.2,false,knight));
        double top=levels*2.2;
        blocks.add(new Block(x,top+.65,FORT_Z,4.5,1.3,3.5,false,knight));
        blocks.add(new Block(x,top+1.3+1.35,FORT_Z,1.8,2.7,1.5,true,knight));totalTargets++;
    }
    private void addWall(double x,double height,boolean reinforced) {
        blocks.add(new Block(x,height/2,FORT_Z,1.8,height,2.5,false,reinforced));
    }
    public boolean won() { return totalTargets>0 && targetsDown==totalTargets; }
    public void resetDistanceBaseline() { previousDistance=-1; }
    /** Baseline every reading, even while inactive, so old monitor totals never grant charge. */
    public boolean observeDistance(int total,boolean charging) {
        if(total<0 || total>65535) return false;
        int before=previousDistance;previousDistance=total;
        if(before<0 || total<=before || !charging)return false;
        return addDistance(total-before);
    }
    public boolean addDistance(double meters) {
        if(!Double.isFinite(meters) || meters<=0 || won())return false;
        distanceRowed+=meters;
        double before=charge;
        charge=Math.min(100,charge+meters*100/metersPerFullCharge);
        if(charge>before)message=charge>=100?"Siege shot ready! Strong enough to break reinforced stone.":
            charge>=MIN_FIRE_CHARGE?"Standard shot ready. Wind to "+meters(metersPerFullCharge)+" for reinforced towers.":"Keep rowing — "+meters(readyMeters())+" readies a shot. Your charge is held.";
        return charge>before;
    }
    public void aim(double degrees) { aim=Math.max(-22,Math.min(22,degrees)); }
    private double speed() {
        return 36+charge*0.12;
    }
    public double landingRange() {
        double v=speed(), vy=v*Math.sin(ANGLE), horizontal=v*Math.cos(ANGLE);
        return 2+horizontal*(vy+Math.sqrt(vy*vy+2*GRAVITY*LAUNCH_HEIGHT))/GRAVITY;
    }
    public double[] preview(double t) {
        double v=speed(),yaw=Math.toRadians(aim),h=v*Math.cos(ANGLE);
        return new double[]{Math.sin(yaw)*h*t,LAUNCH_HEIGHT+v*Math.sin(ANGLE)*t-GRAVITY*t*t/2,2+Math.cos(yaw)*h*t};
    }
    public double timeToFort() { return (FORT_Z-2)/(speed()*Math.cos(ANGLE)*Math.cos(Math.toRadians(aim))); }
    public double[] aimPoint() { return preview(timeToFort()); }
    public boolean fire() {
        if(flying || won() || charge<MIN_FIRE_CHARGE) return false;
        double v=speed(),yaw=Math.toRadians(aim);
        rockX=0;rockY=LAUNCH_HEIGHT;rockZ=2;launchCharge=charge;
        rockVX=v*Math.cos(ANGLE)*Math.sin(yaw);rockVY=v*Math.sin(ANGLE);rockVZ=v*Math.cos(ANGLE)*Math.cos(yaw);
        charge=0;shots++;flying=true;flightAge=0;armRelease=0;
        message="Stone away! You can wind the next shot while it flies.";
        return true;
    }
    /** Called only at the fixed 120 Hz simulation timestep. */
    public void step(double dt) {
        impactAge+=dt;armRelease=Math.min(1,armRelease+dt*3);
        if(flying) {
            flightAge+=dt;rockVY-=GRAVITY*dt;
            rockX+=rockVX*dt;rockY+=rockVY*dt;rockZ+=rockVZ*dt;
            for(Block b:blocks) if(!b.fallen && overlapsRock(b)) { impact(true);break; }
            if(flying && (rockY<=0.55 || flightAge>12 || rockZ>110)) impact(false);
        }
        for(Block b:blocks) {
            if(!b.moving && b.y-b.h/2>0.05 && !supported(b)) {
                b.moving=true; b.vx=(b.homeX<0?-0.25:0.25);b.spin=0.4;
            }
            if(!b.moving) continue;
            b.vy-=GRAVITY*dt;b.x+=b.vx*dt;b.y+=b.vy*dt;b.z+=b.vz*dt;b.rotation+=b.spin*dt;
            if(b.y<b.h/2) {
                b.y=b.h/2;
                b.vy=Math.abs(b.vy)>1?-b.vy*0.18:0;
                double friction=Math.exp(-4*dt);b.vx*=friction;b.vz*=friction;b.spin*=friction;
            }
            if(b.target && !b.fallen && (b.y<b.homeY-1.5 || Math.abs(b.x-b.homeX)>2.5 || Math.abs(b.z-b.homeZ)>2.5)) {
                b.fallen=true;targetsDown++;
                message=won()?"Fort captured! All defenders dislodged.":"Defender dislodged! "+targetsDown+" / "+totalTargets+". Aim for the remaining towers.";
            }
        }
    }
    private boolean supported(Block b) {
        for(Block other:blocks) if(other!=b && !other.moving && !other.fallen &&
            Math.abs((b.y-b.h/2)-(other.y+other.h/2))<0.12 &&
            Math.abs(b.x-other.x)<(b.w+other.w)/2-0.1 && Math.abs(b.z-other.z)<(b.d+other.d)/2-0.1) return true;
        return false;
    }
    private boolean overlapsRock(Block b) {
        double dx=Math.max(0,Math.abs(rockX-b.x)-b.w/2),dy=Math.max(0,Math.abs(rockY-b.y)-b.h/2),dz=Math.max(0,Math.abs(rockZ-b.z)-b.d/2);
        return dx*dx+dy*dy+dz*dz<=0.65*0.65;
    }
    private void impact(boolean hit) {
        flying=false;impactAge=0;impactX=rockX;impactZ=rockZ;
        if(hit) {
            int broken=0;boolean armorHit=false;
            for(Block b:blocks) {
                double dx=b.x-rockX,dy=b.y-rockY,dz=b.z-rockZ;
                double distance=Math.sqrt(dx*dx+dy*dy+dz*dz);
                if(distance<5.5 && !b.moving) {
                    double damage=4.5*Math.pow(launchCharge/100,2)*(.4+.6*(1-distance/5.5));
                    b.health=Math.max(0,b.health-damage);armorHit|=b.reinforced;
                    if(b.health>0)continue;
                    broken++;
                    double force=(1-distance/5.5)*7*(0.7+launchCharge*0.006);
                    b.moving=true;b.vx+=rockVX*0.22+dx*force*0.4;b.vy+=force+1;
                    b.vz+=rockVZ*0.35+dz*force*0.3;b.spin=(dx>=0?1:-1)*(1+force*0.2);
                }
            }
            message=broken>0?"Masonry broken! Watch for the collapse.":armorHit?"Reinforced stone cracked. Charge to "+meters(metersPerFullCharge)+" for a siege shot.":"Stone damaged. Another hit will weaken it further.";
        } else {
            message=rockZ<FORT_Z-5?"Short of the fort. Wind a little further for your next shot.":
                rockZ>FORT_Z+6?"Beyond the fort. Try less charge on your next shot.":"Close! Adjust left or right and try again.";
        }
    }
}
