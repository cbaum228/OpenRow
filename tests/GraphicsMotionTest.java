package org.openrow.probe;

import java.util.*;

public final class GraphicsMotionTest {
    private static int checks;
    private static void check(boolean yes,String why){checks++;if(!yes)throw new AssertionError(why);}
    private static void near(double a,double b){check(Math.abs(a-b)<.0001,a+" != "+b);}
    public static void main(String[] args){
        GraphicsPerformance normal=new GraphicsPerformance();for(int i=0;i<600;i++)normal.frame(1000.0/30,5);near(normal.fps(),30);near(normal.percentile(.95),1000.0/30);check(normal.quality==2,"Stable frames can restore detail");int count=normal.samples();normal.frame(4000,2);check(normal.samples()==count,"Background gap is not a presented frame");
        GraphicsPerformance heavy=new GraphicsPerformance();for(int i=0;i<100;i++)heavy.frame(45,10);check(heavy.quality==0,"Slow presentation reduces detail even with low measured CPU");check(heavy.fps()<23,"Actual frame rate reported");
        for(int kind:new int[]{GravityWellGame.GAS_GIANT,GravityWellGame.ICE_GIANT,GravityWellGame.SUN,GravityWellGame.RED_GIANT,GravityWellGame.WHITE_DWARF,GravityWellGame.NEUTRON})for(int stage=0;stage<=20;stage++){
            GravityWellGame g=new GravityWellGame();g.start(0);g.spawnKind(kind,true);GravityWellGame.Body b=g.bodies.get(0);b.captured=true;b.captureAge=8;b.mass=b.initialMass*stage/20.0;
            double r=GravityWellGame.visualRadius(b)*Math.cbrt(stage/20.0),limit=kind==GravityWellGame.GAS_GIANT||kind==GravityWellGame.ICE_GIANT?.04:kind==GravityWellGame.WHITE_DWARF||kind==GravityWellGame.NEUTRON?.01:.08;
            float[] point=new float[2];for(int i=0;i<64;i++){
                double angle=i*Math.PI/32,x=Math.cos(angle)*r,y=Math.sin(angle)*r;GravityWellScene.surfacePoint(b,angle,point);
                check(Math.hypot(point[0],point[1])<=r*(1+limit)+.0001,"Surviving core stays compact throughout consumption");
                near(point[1],y);if(x>=0)near(point[0],x);
                float[] mapped=new float[2];GravityWellScene.fluidPoint(b,x,y,mapped,0);near(point[0],mapped[0]);near(point[1],mapped[1]);
            }
        }
        for(int kind:new int[]{GravityWellGame.PLANET,GravityWellGame.GAS_GIANT,GravityWellGame.ICE_GIANT,GravityWellGame.SUN,GravityWellGame.NEUTRON})for(double mass:new double[]{1,.8,.5,.1})for(int orbit=0;orbit<8;orbit++){
            GravityWellGame g=new GravityWellGame();g.start(0);g.spawnKind(kind,true);GravityWellGame.Body b=g.bodies.get(0);double a=orbit*Math.PI/4;b.x=Math.cos(a)*260;b.y=Math.sin(a)*260;b.captured=true;b.captureAge=8;b.mass=b.initialMass*mass;b.trailAge=8;
            Properties before=new Properties();g.save(before);float[] root=new float[2],p=new float[2];GravityWellScene.surfacePoint(b,Math.PI,root);GravityWellScene.flowPoint(b,0,1,0,0,p);
            near(p[0],b.x+Math.cos(a)*root[0]*.96-Math.sin(a)*root[1]*.96);near(p[1],b.y+Math.sin(a)*root[0]*.96+Math.cos(a)*root[1]*.96);
            double previousX=p[0],previousY=p[1];for(int i=1;i<=32;i++){GravityWellScene.flowPoint(b,0,1,i/32.0,0,p);check(Double.isFinite(p[0])&&Double.isFinite(p[1]),"Finite flow at every stage");check(Math.hypot(p[0]-previousX,p[1]-previousY)<40,"Continuous ribbon geometry");previousX=p[0];previousY=p[1];}check(Math.hypot(p[0],p[1])<30,"Flow finishes beneath the horizon");
            Properties after=new Properties();g.save(after);check(before.equals(after),"Visual geometry cannot change saved economy or physical mass");
        }
        GravityWellGame.Body stream=new GravityWellGame.Body(260,0,0,0,100,false);double length=380,lifetime=length/95;stream.trailAge=lifetime*.1;double early=GravityWellScene.grainPosition(stream,0,0,length);stream.trailAge=lifetime*.2;double earlyEnd=GravityWellScene.grainPosition(stream,0,0,length);stream.trailAge=lifetime*.8;double late=GravityWellScene.grainPosition(stream,0,0,length);stream.trailAge=lifetime*.9;double lateEnd=GravityWellScene.grainPosition(stream,0,0,length);check(lateEnd-late>earlyEnd-early,"Material visibly accelerates toward the well");
        System.out.println("PASS: "+checks+" presentation, flow attachment, acceleration and graphics isolation checks");
    }
}
