package org.openrow.probe;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.util.Log;
import android.view.*;
import java.util.*;

/** Native Canvas renderer: no downloaded art, browser runtime or game-engine dependency. */
public final class CastlebreakerView extends View {
    public interface Controls { void back(); void connect();void campaignProgress(); }
    private final CastlebreakerGame freeGame=new CastlebreakerGame();
    private CastlebreakerGame campaignGame;
    public CastlebreakerGame game=freeGame;
    private CampaignSession campaign;
    private boolean campaignPlay;
    private final Controls controls;
    private final WorkoutSessionController workouts;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final ArrayList<CastlebreakerGame.Block> sorted=new ArrayList<>();
    private boolean connected,demo,active=true;
    private int aimDirection;
    private long lastFrame;
    private double accumulator,pulse;
    private String connection="S4 not connected";
    private static final int CREAM=Color.rgb(249,237,207),DARK=Color.rgb(23,35,39),GOLD=Color.rgb(240,183,77);
    private final RectF back=new RectF(24,18,124,65),connect=new RectF(852,18,1035,65),mode=new RectF(1048,18,1256,65);
    private final RectF left=new RectF(24,644,164,704),fire=new RectF(178,644,388,704),right=new RectF(402,644,542,704);
    private final RectF test=new RectF(558,644,778,704),reset=new RectF(1052,644,1256,704);
    private final RectF previousFort=new RectF(384,97,430,140),nextFort=new RectF(838,97,884,140),advanceFort=new RectF(800,644,1038,704);
    private final WheelShape[] wheels={buildWheel(-51,-56,15,5),buildWheel(51,-56,15,5),buildWheel(-87,13,21,0),buildWheel(87,13,21,0)};
    public CastlebreakerView(Context context,Controls controls,WorkoutSessionController workouts) {
        super(context);this.controls=controls;this.workouts=workouts;setFocusableInTouchMode(true);
        String saved=context.getSharedPreferences("openrow_campaign",Context.MODE_PRIVATE).getString("checkpoint",null);
        if(saved!=null){campaign=CampaignSession.restore(saved);if(campaign!=null)prepareCampaignFort();}
    }
    public CampaignSession campaign(){return campaign;}
    public void saveCheckpoint(){if(campaign!=null)getContext().getSharedPreferences("openrow_campaign",Context.MODE_PRIVATE).edit().putString("checkpoint",campaign.checkpoint()).apply();}
    private void prepareCampaignFort(){campaignGame=new CastlebreakerGame();campaignGame.setChargeScale(campaign.plan.chargeScale());campaignGame.selectFort(campaign.currentFort());}
    public void startCampaign(CampaignSession.Plan plan,boolean test){campaign=new CampaignSession(plan,test);prepareCampaignFort();saveCheckpoint();resumeCampaign();}
    public void resumeCampaign(){campaignPlay=true;game=campaignGame;demo=campaign.simulated;lastFrame=0;accumulator=0;game.resetDistanceBaseline();startRecorder();}
    public boolean continueCampaign(boolean skip){if(campaign==null||!campaign.continueAfterRest(skip))return false;prepareCampaignFort();saveCheckpoint();resumeCampaign();return true;}
    public void freePlay(){campaignPlay=false;game=freeGame;demo=false;lastFrame=0;accumulator=0;game.resetDistanceBaseline();startRecorder();}
    private void startRecorder(){if(!demo&&workouts.start("Castlebreaker",null)){workouts.active.data.setProperty("castle.campaign",""+campaignPlay);workouts.resume();}}
    public boolean recording(){return !demo&&!paused()&&getVisibility()==VISIBLE&&workouts.owns("Castlebreaker");}
    public void resumeRecorded(){if(workouts.owns("Castlebreaker")){boolean campaignSaved=Boolean.parseBoolean(workouts.active.data.getProperty("castle.campaign","false"));if(campaignSaved&&campaign!=null&&campaign.phase==CampaignSession.Phase.PLAYING)resumeCampaign();else if(!campaignSaved)freePlay();workouts.resume();active(true);}}
    public void rowerState(boolean connected) {
        if(this.connected==connected)return;
        game.resetDistanceBaseline();
        if(this.connected && !connected && !demo) game.message="S4 disconnected. Reconnect to resume, or switch to test mode.";
        this.connected=connected;connection=connected?"S4 connected":"S4 not connected";invalidate();
    }
    public void distance(int total) {
        double before=game.distanceRowed;
        boolean changed=game.observeDistance(total,recording()&&workouts.active!=null&&!workouts.active.paused());
        if(campaignPlay)campaign.distance(game.distanceRowed-before);
        if(changed) {
            pulse=1;Log.i("Castlebreaker","S4 distance="+total+"m; charge="+game.charge);invalidate();
        }
    }
    private void testDistance() {
        double meters=game.metersPerFullCharge/4,before=game.distanceRowed;
        if(game.addDistance(meters)){pulse=1;Log.i("Castlebreaker","TEST +"+meters+"m; charge="+game.charge);}
        if(campaignPlay)campaign.distance(game.distanceRowed-before);
    }
    private void fire() {
        double charge=game.charge,aim=game.aim;
        if(game.fire()){if(campaignPlay)campaign.shot();Log.i("Castlebreaker",(demo?"TEST":"S4")+" shot="+game.shots+" charge="+charge+" aim="+aim);}
    }
    public void active(boolean value) { active=value;if(!value&&workouts.owns("Castlebreaker"))workouts.pause();game.resetDistanceBaseline();aimDirection=0;lastFrame=0;invalidate(); }
    private boolean paused() { return !active || (!demo && !connected) || (campaignPlay && campaign.phase!=CampaignSession.Phase.PLAYING); }
    private int rgb(int r,int g,int b){return Color.rgb(r,g,b);}
    private void rect(Canvas c,float x,float y,float w,float h,int color,float radius) {p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRoundRect(x,y,x+w,y+h,radius,radius,p);}
    private void text(Canvas c,String value,float x,float y,float size,int color,boolean bold) {
        p.setColor(color);p.setTypeface(bold?Typeface.create("sans-serif",Typeface.BOLD):Typeface.create("sans-serif",Typeface.NORMAL));p.setTextSize(size);c.drawText(value,x,y,p);
    }
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width) {p.setColor(color);p.setStrokeWidth(width);c.drawLine(x,y,xx,yy,p);}
    private void circle(Canvas c,float x,float y,float r,int color){p.setColor(color);c.drawCircle(x,y,r,p);}
    private float[] project(double x,double y,double z) {
        double depth=(z+16)*Math.cos(.2)-(y-10)*Math.sin(.2);
        return new float[]{(float)(640+x*780/depth),(float)(350-((y-10)*Math.cos(.2)+(z+16)*Math.sin(.2))*780/depth),(float)(780/depth)};
    }
    private void polygon(Canvas c,int color,float[]... points) {
        path.reset();path.moveTo(points[0][0],points[0][1]);for(int i=1;i<points.length;i++)path.lineTo(points[i][0],points[i][1]);path.close();p.setColor(color);c.drawPath(path,p);
    }
    private void button(Canvas c,RectF area,String label,boolean enabled,boolean selected) {
        rect(c,area.left,area.top,area.width(),area.height(),enabled?(selected?GOLD:rgb(47,63,64)):rgb(41,48,48),9);
        p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(18);
        float width=p.measureText(label);text(c,label,area.centerX()-width/2,area.centerY()+6,18,enabled?(selected?DARK:CREAM):rgb(115,128,127),true);
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);float scale=Math.min(getWidth()/1280f,getHeight()/720f);
        c.drawColor(DARK);c.save();c.translate((getWidth()-1280*scale)/2,(getHeight()-720*scale)/2);c.scale(scale,scale);
        long now=SystemClock.elapsedRealtime();
        if(lastFrame!=0 && !paused()) {
            if(campaignPlay)campaign.tick(Math.max(0,now-lastFrame));
            double elapsed=Math.min(.1,(now-lastFrame)/1000.0);accumulator+=elapsed;
            while(accumulator>=1.0/120) {game.aim(game.aim+aimDirection*18/120.0);game.step(1.0/120);accumulator-=1.0/120;}
            pulse=Math.max(0,pulse-elapsed*1.7);
        }
        lastFrame=now;
        if(campaignPlay && campaign.captureFort(game.won()?game.fortIndex:-1)) {
            if(!demo&&campaign.phase==CampaignSession.Phase.COMPLETE&&workouts.owns("Castlebreaker"))workouts.finish();
            saveCheckpoint();aimDirection=0;post(()->controls.campaignProgress());
        }
        drawWorld(c);
        rect(c,0,0,1280,82,DARK,0);
        button(c,back,"‹ Setup",true,false);text(c,"CASTLEBREAKER",146,43,26,CREAM,true);
        text(c,campaignPlay?String.format(Locale.US,"CAMPAIGN  ·  %.0f / %,d m planned  ·  %d shots",campaign.distanceMeters,campaign.plan.plannedMeters(),campaign.shots):"FREE PLAY  /  ROW TO WIND  /  AIM",147,65,11,rgb(163,180,172),false);
        RowingTelemetryFrame telemetry=workouts.telemetry;
        String live=telemetry==null?connection:String.format(Locale.US,"%s W · %s spm",Double.isFinite(telemetry.watts)?String.format(Locale.US,"%.0f",telemetry.watts):"—",Double.isFinite(telemetry.cadence)?String.format(Locale.US,"%.0f",telemetry.cadence):"—");
        text(c,demo?"TEST MODE · simulated distance":live,524,46,16,demo?GOLD:rgb(148,218,182),false);
        button(c,connect,connected?"S4 connected":"Connect S4",!connected,false);
        button(c,mode,campaignPlay?(demo?"Test campaign":"Rowing campaign"):demo?"Use rowing input":"Switch to test mode",!campaignPlay,demo);
        rect(c,374,90,520,57,Color.argb(220,23,35,39),10);
        button(c,previousFort,"‹",!campaignPlay,false);button(c,nextFort,"›",!campaignPlay,false);
        text(c,(game.fortIndex+1)+" / 4  ·  "+CastlebreakerGame.FORT_NAMES[game.fortIndex],449,126,21,CREAM,true);
        drawHud(c);
        if(!demo && !connected) {
            rect(c,340,353,600,112,Color.argb(235,23,35,39),14);
            text(c,"Connect your S4 to power the winch",378,393,24,CREAM,true);
            text(c,"Or choose test mode to try the game without rowing.",378,429,18,rgb(190,205,193),false);
        } else if(game.won()) {
            rect(c,374,368,532,115,Color.argb(240,23,35,39),14);
            text(c,"FORT CAPTURED",432,413,32,GOLD,true);
            text(c,String.format(Locale.US,"%d shots · %.0f m rowed",game.shots,game.distanceRowed),432,450,21,CREAM,false);
        }
        c.restore();
        if(active && getVisibility()==VISIBLE)postInvalidateOnAnimation();
    }
    private void drawWorld(Canvas c) {
        final float horizon=project(0,0,100000)[1];
        p.setShader(new LinearGradient(0,82,0,horizon+22,new int[]{rgb(138,179,177),rgb(209,216,186)},null,Shader.TileMode.CLAMP));
        c.drawRect(0,82,1280,horizon+24,p);p.setShader(null);
        circle(c,1080,138,30,Color.argb(80,255,242,192));
        // Small, hazy silhouettes sit at the true ground-plane horizon, beyond the fort.
        polygon(c,rgb(166,188,173),new float[]{0,horizon+3},new float[]{110,161},new float[]{176,177},new float[]{263,138},new float[]{352,178},new float[]{421,152},new float[]{517,182},new float[]{626,147},new float[]{720,180},new float[]{823,156},new float[]{903,180},new float[]{1018,145},new float[]{1145,176},new float[]{1232,154},new float[]{1280,horizon+3});
        polygon(c,rgb(159,181,161),new float[]{0,horizon+9},new float[]{145,184},new float[]{244,192},new float[]{367,173},new float[]{489,191},new float[]{628,180},new float[]{779,192},new float[]{946,178},new float[]{1110,190},new float[]{1280,179},new float[]{1280,horizon+13});
        p.setShader(new LinearGradient(0,horizon,0,635,new int[]{rgb(175,185,141),rgb(135,152,104),rgb(80,104,75)},new float[]{0,.30f,1},Shader.TileMode.CLAMP));
        c.drawRect(0,horizon+2,1280,635,p);p.setShader(null);
        // A broad road and range marks make perspective/range readable.
        polygon(c,rgb(165,156,112),project(-6,0,0),project(6,0,0),project(1.8,0,450),project(-1.8,0,450));
        for(int z=20;z<=60;z+=10) {
            float[] a=project(-19,0,z),b=project(19,0,z);line(c,a[0],a[1],b[0],b[1],Color.argb(45,242,235,203),1);
            text(c,Integer.toString(z)+" m",b[0]+8,b[1]+4,12,rgb(224,224,185),false);
        }
        // Preview ends at the fort plane, not at a distant ground landing point.
        if(game.charge>0 && !game.won()) {
            for(double t=.04;t<game.timeToFort();t+=.045) {
                double[] point=game.preview(t);if(point[1]<0)break;
                float[] s=project(point[0],point[1],point[2]);circle(c,s[0],s[1],2.5f,Color.argb(160,255,227,151));
            }
        }
        // Compacted earth and contact shadows are projected ON the ground, not screen ellipses.
        polygon(c,rgb(150,151,111),project(-16,0,40.8),project(16,0,40.8),project(16,0,48),project(-16,0,48));
        for(CastlebreakerGame.Block b:game.blocks) {
            if(b.y-b.h/2<.08) {
                polygon(c,Color.argb(35,36,41,25),project(b.x-b.w/2-.15,0,b.z-b.d/2-.12),project(b.x+b.w/2+.7,0,b.z-b.d/2-.12),project(b.x+b.w/2+.7,0,b.z+b.d/2+.8),project(b.x-b.w/2-.15,0,b.z+b.d/2+.8));
            }
        }
        sorted.clear();sorted.addAll(game.blocks);Collections.sort(sorted,(a,b)->Double.compare(b.z,a.z));
        for(CastlebreakerGame.Block b:sorted)drawBlock(c,b);
        if(game.charge>0 && !game.won()) {
            double[] point=game.aimPoint();float[] s=project(point[0],point[1],point[2]);
            p.setColor(GOLD);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawCircle(s[0],s[1],12,p);p.setStyle(Paint.Style.FILL);
            line(c,s[0]-18,s[1],s[0]-7,s[1],GOLD,2);line(c,s[0]+7,s[1],s[0]+18,s[1],GOLD,2);
            line(c,s[0],s[1]-18,s[0],s[1]-7,GOLD,2);line(c,s[0],s[1]+7,s[0],s[1]+18,GOLD,2);
        }
        if(game.impactAge<1.1) {
            float[] s=project(game.impactX,0,game.impactZ);float r=(float)(14+game.impactAge*45);
            for(int i=0;i<7;i++)circle(c,s[0]+(i-3)*r*.35f,s[1]-(float)Math.sin(i*2)*r*.25f,r*.45f,Color.argb((int)(110*(1-game.impactAge/1.1)),211,196,144));
        }
        if(game.flying) {
            float[] shadow=project(game.rockX,0,game.rockZ),rock=project(game.rockX,game.rockY,game.rockZ);
            p.setColor(Color.argb(90,20,29,22));c.drawOval(shadow[0]-rock[2],shadow[1]-rock[2]*.3f,shadow[0]+rock[2],shadow[1]+rock[2]*.3f,p);
            circle(c,rock[0],rock[1],Math.max(4,rock[2]*.65f),rgb(55,62,60));circle(c,rock[0]-2,rock[1]-2,Math.max(1,rock[2]*.2f),rgb(155,159,137));
        }
        drawCatapult(c);
    }
    private void drawBlock(Canvas c,CastlebreakerGame.Block b) {
        if(b.target){drawDefender(c,b);return;}
        float[] s=project(b.x,b.y,b.z);float w=(float)(b.w*s[2]),h=(float)(b.h*s[2]);
        c.save();
        if(b.moving && b.y>b.h/2+.08)c.rotate((float)Math.toDegrees(b.rotation),s[0],s[1]);
        int front=b.reinforced?rgb(125,145,153):(b.moving?rgb(135,131,111):rgb(185,181,151));
        float[] fl=blockCorner(b,-1,-1,-1),fr=blockCorner(b,1,-1,-1),tl=blockCorner(b,-1,1,-1),tr=blockCorner(b,1,1,-1);
        polygon(c,b.reinforced?rgb(182,197,197):rgb(221,211,175),tl,tr,blockCorner(b,1,1,1),blockCorner(b,-1,1,1));
        int side=b.x<0?1:-1;
        polygon(c,b.reinforced?rgb(80,100,112):rgb(127,132,111),blockCorner(b,side,-1,-1),blockCorner(b,side,-1,1),blockCorner(b,side,1,1),blockCorner(b,side,1,-1));
        polygon(c,front,fl,fr,tr,tl);
        line(c,fl[0],fl[1],fr[0],fr[1],rgb(98,105,87),1);
        line(c,fl[0],fl[1],tl[0],tl[1],Color.argb(85,78,88,69),.7f);
        if(b.reinforced && !b.moving) {
            float yy=tl[1]+(fl[1]-tl[1])*.28f;
            line(c,tl[0],yy,tr[0],yy,rgb(70,86,94),2);
            circle(c,tl[0]+3,yy,1.2f,rgb(204,207,189));circle(c,tr[0]-3,yy,1.2f,rgb(204,207,189));
        }
        if(b.health<b.maxHealth && !b.moving) {
            float cx=(tl[0]+tr[0])/2,cy=(tl[1]+fl[1])/2;
            line(c,cx-5,cy-h*.3f,cx+2,cy,rgb(49,62,62),1.6f);line(c,cx+2,cy,cx-3,cy+h*.25f,rgb(49,62,62),1.6f);
            if(b.health<b.maxHealth*.5)line(c,cx+2,cy,cx+8,cy-3,rgb(49,62,62),1.6f);
        }
        c.restore();
    }
    private void drawDefender(Canvas c,CastlebreakerGame.Block b) {
        float[] feet=project(b.x,b.y-b.h/2,b.z),center=project(b.x,b.y,b.z);
        if(b.fallen) {
            float[] ground=project(b.x,.12,b.z);circle(c,ground[0],ground[1],4,b.reinforced?rgb(110,135,151):rgb(69,99,67));return;
        }
        c.save();c.translate(feet[0],feet[1]);
        if(b.moving)c.rotate((float)Math.toDegrees(b.rotation));
        c.scale(center[2],center[2]);
        int cloth=b.reinforced?rgb(62,83,105):rgb(58,100,69),metal=rgb(174,188,188),skin=rgb(224,182,132);
        line(c,-.22f,-.86f,-.28f,-.05f,b.reinforced?metal:rgb(79,62,43),.2f);
        line(c,.22f,-.86f,.28f,-.05f,b.reinforced?metal:rgb(79,62,43),.2f);
        line(c,-.4f,0,-.14f,0,rgb(50,49,39),.16f);line(c,.14f,0,.4f,0,rgb(50,49,39),.16f);
        rect(c,-.39f,-1.96f,.78f,1.15f,cloth,.1f);line(c,-.38f,-.98f,.38f,-.98f,rgb(187,145,75),.12f);
        line(c,-.4f,-1.8f,-.65f,-1.18f,cloth,.2f);line(c,.4f,-1.8f,.65f,-1.15f,cloth,.2f);
        if(b.reinforced) {
            rect(c,-.34f,-2.58f,.68f,.63f,metal,.12f);
            line(c,-.26f,-2.32f,.26f,-2.32f,rgb(37,48,56),.1f);line(c,0,-2.57f,0,-2.03f,rgb(220,221,201),.07f);
            polygon(c,rgb(176,105,59),new float[]{-.05f,-2.59f},new float[]{-.02f,-2.91f},new float[]{.3f,-2.74f},new float[]{.1f,-2.56f});
            polygon(c,rgb(180,148,75),new float[]{-.94f,-1.86f},new float[]{-.37f,-1.86f},new float[]{-.38f,-1.21f},new float[]{-.66f,-.93f},new float[]{-.95f,-1.21f});
            polygon(c,cloth,new float[]{-.86f,-1.77f},new float[]{-.46f,-1.77f},new float[]{-.47f,-1.25f},new float[]{-.66f,-1.06f},new float[]{-.86f,-1.25f});
            line(c,-.66f,-1.7f,-.66f,-1.2f,rgb(216,196,123),.07f);
            line(c,.7f,-.7f,.7f,-2.32f,metal,.09f);line(c,.5f,-1.28f,.88f,-1.28f,rgb(184,148,75),.1f);
        } else {
            circle(c,0,-2.31f,.37f,cloth);rect(c,-.2f,-2.44f,.4f,.35f,skin,.08f);
            line(c,-.5f,-1.3f,-.7f,-2.33f,rgb(98,74,44),.16f);
            for(int i=0;i<3;i++)line(c,-.63f-i*.09f,-2.08f,-.74f-i*.09f,-2.66f,rgb(217,200,145),.035f);
            path.reset();path.moveTo(.65f,-2.43f);path.quadTo(1.37f,-1.57f,.65f,-.69f);p.setColor(rgb(188,138,70));p.setStrokeWidth(.09f);p.setStyle(Paint.Style.STROKE);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
            line(c,.65f,-2.43f,.65f,-.69f,rgb(234,217,175),.035f);
        }
        c.restore();
    }
    private float[] blockCorner(CastlebreakerGame.Block b,int x,int y,int z) {
        double dx=x*b.w/2,dz=z*b.d/2;
        if(b.moving && b.y<=b.h/2+.08) {
            double angle=b.rotation*.35,rotatedX=dx*Math.cos(angle)-dz*Math.sin(angle);
            dz=dx*Math.sin(angle)+dz*Math.cos(angle);dx=rotatedX;
        }
        return project(b.x+dx,b.y+y*b.h/2,b.z+dz);
    }
    private void drawCatapult(Canvas c) {
        float[] at=project(0,0,2);float x=at[0],y=Math.min(at[1],558);
        c.save();c.translate(x,y);c.scale(.90f,.90f);
        // Four wheels, a braced carriage, pivot axle, and a separate winding drum.
        p.setColor(Color.argb(45,10,20,12));c.drawOval(-111,20,111,36,p);
        line(c,-51,-56,51,-56,rgb(62,70,60),6);
        wheel(c,wheels[0]);wheel(c,wheels[1]);
        polygon(c,rgb(117,80,43),new float[]{-79,5},new float[]{79,5},new float[]{45,-68},new float[]{-45,-68});
        for(int i=-2;i<=2;i++)line(c,-65+i*2,i*12-26,65-i*2,i*12-26,Color.argb(110,69,49,30),1);
        line(c,-80,7,-46,-68,rgb(79,53,31),14);line(c,80,7,46,-68,rgb(79,53,31),14);
        line(c,-81,3,-47,-72,rgb(198,150,82),6);line(c,81,3,47,-72,rgb(198,150,82),6);
        line(c,-81,5,81,5,rgb(80,55,31),14);line(c,-47,-67,47,-67,rgb(91,60,32),12);
        line(c,-87,13,87,13,rgb(62,70,60),6);
        wheel(c,wheels[2]);wheel(c,wheels[3]);
        line(c,-61,-6,-23,-96,rgb(78,52,31),15);line(c,61,-6,23,-96,rgb(78,52,31),15);
        line(c,-61,-9,-23,-99,rgb(201,154,88),7);line(c,61,-9,23,-99,rgb(201,154,88),7);
        line(c,-40,-26,40,-26,rgb(104,73,40),9);
        line(c,-28,-96,28,-96,rgb(59,66,58),15);
        circle(c,-28,-96,7,rgb(182,151,94));circle(c,28,-96,7,rgb(182,151,94));
        double pull=game.charge/100.0;
        double angle=game.armRelease<1?(-.15+(game.launchCharge/100.0)*(1-game.armRelease)*1.35):(-.15+pull*1.35);
        float endX=(float)(Math.sin(Math.toRadians(game.aim))*70),endY=(float)(-96-Math.cos(angle)*73);
        line(c,-endX*.25f,-65,endX,endY,rgb(72,47,29),15);line(c,-endX*.25f-2,-68,endX-2,endY,rgb(204,155,83),7);
        line(c,endX*.4f,-96+(endY+96)*.4f,endX*.53f,-96+(endY+96)*.53f,rgb(73,82,72),11);
        line(c,endX,endY+5,0,-15,rgb(83,68,40),4);line(c,endX,endY+5,-1,-15,rgb(222,205,153),1.7f);
        p.setColor(rgb(75,53,31));c.drawOval(endX-18,endY-5,endX+18,endY+8,p);
        p.setColor(rgb(187,143,78));c.drawOval(endX-17,endY-9,endX+17,endY+1,p);
        line(c,endX-17,endY+1,endX+17,endY+1,rgb(67,74,63),3);
        if(game.charge>0){circle(c,endX,endY-10,11,rgb(61,67,61));circle(c,endX-3,endY-14,3,rgb(148,151,132));}
        rect(c,-25,-21,50,19,rgb(84,62,34),3);
        for(int i=-3;i<=3;i++)line(c,i*5,-20,i*5,-3,rgb(210,188,131),2);
        p.setColor(rgb(60,66,56));c.drawOval(-31,-23,-20,1,p);c.drawOval(20,-23,31,1,p);
        float rot=(float)(game.charge*4);c.save();c.rotate(rot,29,-11);
        line(c,29,-11,47,-11,rgb(175,149,93),4);line(c,47,-11,47,-1,rgb(175,149,93),4);circle(c,47,-1,4,rgb(63,54,35));c.restore();
        if(pulse>0){p.setColor(Color.argb((int)(pulse*130),255,210,110));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawCircle(0,-10,(float)(24+(1-pulse)*35),p);p.setStyle(Paint.Style.FILL);}
        c.restore();
    }
    private static final class WheelShape {
        final float[][] near=new float[20][],far=new float[20][],wood=new float[20][];
        float[] hub;
    }
    private WheelShape buildWheel(float x,float y,float radius,double z) {
        // A cylinder with an X axle and circular YZ faces, projected with the scene camera.
        // Cache its mesh: the carriage is stationary, so no per-frame wheel geometry is needed.
        WheelShape shape=new WheelShape();double side=x<0?-1:1;
        float[] sample=project(0,.4,z),up=project(0,.401,z),forward=project(0,.4,z+.001);
        double scale=Math.hypot((up[1]-sample[1])*1000,(forward[1]-sample[1])*1000);
        double r=radius*.9/scale,worldX=x*.9/project(0,r,z)[2];
        float[] origin=project(worldX,r,z);
        double nearX=worldX-side*.09,farX=worldX+side*.09;
        shape.hub=wheelPoint(nearX,r,z,origin,x,y);
        for(int i=0;i<20;i++) {
            double angle=i*Math.PI*2/20;
            shape.near[i]=wheelPoint(nearX,r+Math.sin(angle)*r,z+Math.cos(angle)*r,origin,x,y);
            shape.far[i]=wheelPoint(farX,r+Math.sin(angle)*r,z+Math.cos(angle)*r,origin,x,y);
            shape.wood[i]=wheelPoint(nearX,r+Math.sin(angle)*r*.79,z+Math.cos(angle)*r*.79,origin,x,y);
        }
        return shape;
    }
    private float[] wheelPoint(double x,double y,double z,float[] origin,float cx,float cy) {
        float[] point=project(x,y,z);return new float[]{cx+(point[0]-origin[0])/.9f,cy+(point[1]-origin[1])/.9f};
    }
    private void wheel(Canvas c,WheelShape wheel) {
        polygon(c,rgb(51,56,46),wheel.far);
        for(int i=0;i<20;i++)polygon(c,i<10?rgb(65,66,50):rgb(47,50,41),wheel.near[i],wheel.near[(i+1)%20],wheel.far[(i+1)%20],wheel.far[i]);
        polygon(c,rgb(49,54,45),wheel.near);polygon(c,rgb(155,115,61),wheel.wood);
        for(int i=0;i<20;i+=5)line(c,wheel.hub[0],wheel.hub[1],wheel.wood[i][0],wheel.wood[i][1],rgb(204,163,95),1.6f);
        circle(c,wheel.hub[0],wheel.hub[1],2,rgb(66,76,65));
    }
    private void drawHud(Canvas c) {
        rect(c,24,99,343,97,Color.argb(220,23,35,39),12);
        text(c,String.format(Locale.US,"WINCH  ·  %.0f / %.0f m",game.charge*game.metersPerFullCharge/100,game.metersPerFullCharge),42,126,13,CREAM,true);text(c,String.format(Locale.US,"%.0f%%",game.charge),290,126,18,GOLD,true);
        rect(c,42,140,304,12,rgb(57,73,65),5);if(game.charge>0)rect(c,42,140,(float)(304*game.charge/100),12,GOLD,5);
        text(c,String.format(Locale.US,"%s   ·   Aim %+.0f°",game.charge>=100?"SIEGE SHOT":game.charge>=CastlebreakerGame.MIN_FIRE_CHARGE?"STANDARD SHOT":String.format(Locale.US,"Ready at %.0f m",game.readyMeters()),game.aim),42,178,16,rgb(205,214,188),false);
        rect(c,938,99,318,97,Color.argb(220,23,35,39),12);
        text(c,"DISLODGE THE DEFENDERS",955,126,13,CREAM,true);
        for(int i=0;i<game.totalTargets;i++)circle(c,966+i*22,151,7,i<game.targetsDown?GOLD:rgb(121,84,60));
        text(c,game.targetsDown+" / "+game.totalTargets+"    ·    "+game.shots+" shots",1063,157,17,CREAM,true);
        text(c,"Archers: stone. Knights: reinforced towers.",955,180,12,rgb(205,214,188),false);
        rect(c,0,593,1280,127,DARK,0);
        text(c,game.message,25,622,18,CREAM,false);
        button(c,left,"◀ Aim left",!paused(),aimDirection<0);
        button(c,fire,game.flying?"Stone in flight":game.charge<CastlebreakerGame.MIN_FIRE_CHARGE?String.format(Locale.US,"Row %.0f m to fire",game.readyMeters()):"FIRE",!paused() && !game.flying && game.charge>=CastlebreakerGame.MIN_FIRE_CHARGE && !game.won(),true);
        button(c,right,"Aim right ▶",!paused(),aimDirection>0);
        if(demo)button(c,test,String.format(Locale.US,"+%.0f m test  [W]",game.metersPerFullCharge/4),!game.won(),false);
        else if(!game.won())text(c,"Row to wind · Space to fire",568,680,18,rgb(167,190,175),false);
        if(game.won() && !campaignPlay)button(c,advanceFort,"Next fort  [N]",true,true);
        button(c,reset,campaignPlay?"Pause campaign":"Reset fort  [R]",true,false);
    }
    @Override public boolean onTouchEvent(android.view.MotionEvent event) {
        float s=Math.min(getWidth()/1280f,getHeight()/720f),x=(event.getX()-(getWidth()-1280*s)/2)/s,y=(event.getY()-(getHeight()-720*s)/2)/s;
        int action=event.getActionMasked();
        if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL){aimDirection=0;performClick();return true;}
        if(action==MotionEvent.ACTION_MOVE){if(aimDirection!=0)aimDirection=!paused()&&left.contains(x,y)?-1:!paused()&&right.contains(x,y)?1:0;return true;}
        if(action!=MotionEvent.ACTION_DOWN)return true;
        requestFocus();
        if(back.contains(x,y))controls.back();
        else if(!campaignPlay && previousFort.contains(x,y)){game.selectFort(game.fortIndex-1);aimDirection=0;accumulator=0;}
        else if(!campaignPlay && (nextFort.contains(x,y) || (game.won() && advanceFort.contains(x,y)))){game.selectFort(game.fortIndex+1);aimDirection=0;accumulator=0;}
        else if(connect.contains(x,y) && !connected)controls.connect();
        else if(!campaignPlay && mode.contains(x,y)){demo=!demo;if(demo&&workouts.owns("Castlebreaker"))workouts.pause();else if(!demo)startRecorder();game.resetDistanceBaseline();aimDirection=0;accumulator=0;Log.i("Castlebreaker","Input mode="+(demo?"TEST":"S4"));game.message=demo?"Test mode: tap +5 m or press W. Space fires after 10 m.":"Row 10 m to ready a shot. Your charge stays until you fire.";}
        else if(reset.contains(x,y)){if(campaignPlay)controls.back();else game.reset();accumulator=0;}
        else if(!paused()) {
            if(left.contains(x,y))aimDirection=-1;else if(right.contains(x,y))aimDirection=1;
            else if(fire.contains(x,y))fire();else if(demo && test.contains(x,y))testDistance();
        }
        invalidate();return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    public boolean key(int code,boolean down,int repeats) {
        if(code==KeyEvent.KEYCODE_DPAD_LEFT || code==KeyEvent.KEYCODE_A){aimDirection=down&&!paused()?-1:0;return true;}
        if(code==KeyEvent.KEYCODE_DPAD_RIGHT || code==KeyEvent.KEYCODE_D){aimDirection=down&&!paused()?1:0;return true;}
        if(code==KeyEvent.KEYCODE_SPACE || code==KeyEvent.KEYCODE_ENTER){if(down&&repeats==0&&!paused())fire();return true;}
        if(code==KeyEvent.KEYCODE_W){if(down&&repeats==0&&demo&&!paused())testDistance();return true;}
        if(code==KeyEvent.KEYCODE_R){if(down&&repeats==0){if(campaignPlay)controls.back();else game.reset();}return true;}
        if(code==KeyEvent.KEYCODE_N){if(down&&repeats==0&&!campaignPlay){game.selectFort(game.fortIndex+1);aimDirection=0;accumulator=0;}return true;}
        return false;
    }
}
