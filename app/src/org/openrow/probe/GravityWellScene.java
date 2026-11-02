package org.openrow.probe;

/** Visual grouping only: physical stellar fragments share one remnant and several plasma flows. */
public final class GravityWellScene {
    /** Same projected plane as the disk's -12 degree tilt. */
    public static boolean front(double x,double y){return y+x*.21255656>=0;}
    public static boolean fluid(GravityWellGame.Body b){return GravityWellGame.kind(b).stellar||b.kind==GravityWellGame.GAS_GIANT||b.kind==GravityWellGame.ICE_GIANT;}
    public static double fluidPull(GravityWellGame.Body b){double strength=b.kind==GravityWellGame.NEUTRON?.5:b.kind==GravityWellGame.WHITE_DWARF?.72:1;return b.captured?Math.sin(Math.PI*Math.max(0,Math.min(1,b.mass/b.initialMass)))*Math.min(1,b.captureAge/2)*strength:0;}
    /** Keep the surviving body compact; detached streams carry the large tidal extension. */
    public static double fluidStretch(GravityWellGame.Body b){
        double bulge=b.kind==GravityWellGame.NEUTRON||b.kind==GravityWellGame.WHITE_DWARF?.01:b.kind==GravityWellGame.GAS_GIANT||b.kind==GravityWellGame.ICE_GIANT?.04:.08;
        return 1+bulge*fluidPull(b);
    }
    /** Local coordinates face away from the well along +X. Shared by surface, glow and inlet. */
    public static void fluidPoint(GravityWellGame.Body b,double x,double y,float[] out,int index){
        out[index]=(float)(x<0?x*fluidStretch(b):x);out[index+1]=(float)y;
    }
    /** Shared local silhouette for the material inlet and its deforming source. */
    public static void surfacePoint(GravityWellGame.Body b,double angle,float[] out){
        double live=GravityWellGame.visualRadius(b),r=live*(fluid(b)&&b.captured?Math.cbrt(Math.max(0,b.mass/b.initialMass)):GravityWellGame.erosionRadius(b,angle));
        double x=Math.cos(angle)*r,y=Math.sin(angle)*r;
        if(fluid(b)){fluidPoint(b,x,y,out,0);x=out[0];y=out[1];}else if(x<0)x*=GravityWellGame.inwardStretch(b);
        double stretch=GravityWellGame.tidalStretch(b);out[0]=(float)(x*stretch);out[1]=(float)(y/Math.sqrt(stretch));
    }
    private final GravityWellGame.Body stellar=new GravityWellGame.Body(0,0,0,0,1,false);
    private GravityWellGame.Body first;
    private long group;
    public double focus,energy;

    public void prepare(GravityWellGame game){
        first=null;group=0;focus=game.stellarFocus();energy=game.visualEnergy();
        if(game.activeBossId<=0||game.bossKind<0||!GravityWellGame.KINDS[game.bossKind].stellar)return;
        double mass=0,x=0,y=0,vx=0,vy=0;int count=0;
        for(GravityWellGame.Body b:game.bodies)if(b.bossId==game.activeBossId){
            if(first==null)first=b;mass+=b.mass;x+=b.x*b.mass;y+=b.y*b.mass;vx+=b.vx*b.mass;vy+=b.vy*b.mass;count++;
        }
        if(first==null||mass<=0){first=null;return;}
        group=game.activeBossId;stellar.kind=game.bossKind;stellar.material=GravityWellGame.KINDS[game.bossKind].material;
        stellar.mass=mass;stellar.initialMass=game.bossInitialMass;stellar.x=x/mass;stellar.y=y/mass;stellar.vx=vx/mass;stellar.vy=vy/mass;
        stellar.captured=first.captured;stellar.captureAge=first.captureAge;stellar.trailAge=first.trailAge;stellar.angle=first.angle;stellar.flightAge=first.flightAge;stellar.orbitRadius=first.orbitRadius;
        stellar.bossId=group;stellar.fragmentDepth=0;stellar.visualEnergy=energy;stellar.visualStreams=Math.max(count,Math.min(4,1+game.fractureRank(first)));
        // The same visual remnant exists on both sides of the physical split.
        double growth=Math.max(0,Math.min(1,Math.min((stellar.captureAge-2.5)/3,(1-mass/game.bossInitialMass-.02)/.14)));
        stellar.visualShear=growth*growth*(3-2*growth);
    }
    public GravityWellGame.Body body(GravityWellGame.Body physical){
        if(group>0&&physical.bossId==group)return physical==first?stellar:null;
        physical.visualOpacity=1-.68*focus;physical.visualEnergy=energy;return physical;
    }
    public GravityWellGame.Body stellarBody(){return first==null?null:stellar;}
    public static double streamWeight(GravityWellGame.Body b,int branch){return branch==0?1:b.visualShear;}
    public static double streamCount(GravityWellGame.Body b){return 1+(Math.max(1,Math.min(4,b.visualStreams))-1)*b.visualShear;}
    public static double streamSpread(GravityWellGame.Body b,int branch){return branch==0?0:(branch==1?-.5:branch==2?.5:.24)*b.visualShear;}
    public static double trailReach(GravityWellGame.Body b,double length){return Math.max(0,Math.min(1,b.trailAge*95/Math.max(1,length)));}
    public static double grainPosition(GravityWellGame.Body b,int grain,int branch,double length){
        double lifetime=Math.max(.14,length/95),first=grain*lifetime/20+branch*.035;
        double elapsed=b.trailAge-first;if(elapsed<0)return -1;
        double u=(elapsed-Math.floor(elapsed/lifetime)*lifetime)/lifetime;
        return Math.min(trailReach(b,length),u*(.45+.55*u));
    }
    /** A comet keeps a wake on approach; that wake bends inward and is stripped after capture. */
    public static void cometTailPoint(GravityWellGame.Body b,double t,float[] out){
        double r=Math.max(1,Math.hypot(b.x,b.y)),speed=Math.max(1,Math.hypot(b.vx,b.vy));
        double tailX=speed>1?-b.vx/speed:b.x/r,tailY=speed>1?-b.vy/speed:b.y/r;
        double shed=b.captured?1:Math.max(0,Math.min(1,(310-r)/140));
        double remaining=b.captured?Math.max(0,1-b.captureAge/2.2):1;
        double length=Math.min(125,b.flightAge*65)*remaining;
        double curl=Math.sin(Math.PI*t)*12*(1-shed);
        out[0]=(float)(b.x+length*t*(tailX*(1-shed)-b.x/r*shed)-tailY*curl);
        out[1]=(float)(b.y+length*t*(tailY*(1-shed)-b.y/r*shed)+tailX*curl);
    }
    /** Flow starts at the inward-facing surface and ends underneath the opaque event horizon. */
    public static void flowPoint(GravityWellGame.Body b,int branch,int branches,double t,double phase,float[] out){
        double angle=Math.atan2(b.y,b.x),spread=branches==1?0:streamSpread(b,branch);
        double localAngle=Math.PI+spread*.8;surfacePoint(b,localAngle,out);double localX=out[0]*.96,localY=out[1]*.96;
        double startX=b.x+Math.cos(angle)*localX-Math.sin(angle)*localY,startY=b.y+Math.sin(angle)*localX+Math.cos(angle)*localY;
        double endAngle=angle+1.25+spread*.15,endX=Math.cos(endAngle)*28,endY=Math.sin(endAngle)*28;
        double inward=Math.min(1,Math.hypot(startX,startY)*.75/Math.max(1,Math.hypot(endX,endY)));endX*=inward;endY*=inward;
        double distance=Math.hypot(startX-endX,startY-endY);
        double tangent=Math.min(80,distance*.38),c1x=startX+(-Math.cos(angle)*.72-Math.sin(angle)*.7)*tangent,c1y=startY+(-Math.sin(angle)*.72+Math.cos(angle)*.7)*tangent;
        double c2x=Math.cos(endAngle-.55)*Math.min(85,Math.hypot(startX,startY)*.65),c2y=Math.sin(endAngle-.55)*Math.min(65,Math.hypot(startX,startY)*.5);
        double u=1-t,wave=Math.sin(t*11-phase+branch*.9)*Math.sin(Math.PI*t)*1.4;
        out[0]=(float)(u*u*u*startX+3*u*u*t*c1x+3*u*t*t*c2x+t*t*t*endX-Math.sin(angle)*wave);
        out[1]=(float)(u*u*u*startY+3*u*u*t*c1y+3*u*t*t*c2y+t*t*t*endY+Math.cos(angle)*wave);
    }
}
