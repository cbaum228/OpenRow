package org.openrow.probe;

import android.content.Context;
import android.graphics.*;
import android.view.*;

/** Static contact sheets using production renderers; no progression or save access. */
public final class GravityWellAuditView extends View {
    private final CelestialRenderer renderer=new CelestialRenderer();
    private final Paint p=new Paint(3);
    private final GravityWellGame.Body[] bodies=new GravityWellGame.Body[6];
    private long settledAt;
    private int page,offset;private float remaining;private final float age;
    public GravityWellAuditView(Context c,int page,float remaining,float age,int offset){super(c);this.offset=offset;this.page=Math.max(0,Math.min(2,page));this.remaining=remaining;this.age=age;prepare();}
    private void prepare(){settledAt=0;remaining=Math.max(.01f,Math.min(1,remaining));for(int slot=0;slot<6;slot++){int kind=(offset>=0?offset:page*6)+slot;if(kind>=GravityWellGame.KINDS.length){bodies[slot]=null;continue;}
        GravityWellGame.Kind k=GravityWellGame.KINDS[kind];GravityWellGame.Body b=new GravityWellGame.Body(220,0,0,0,k.mass,false);StellarTextures.request(kind);b.kind=kind;b.material=k.material;b.mass*=remaining;b.captured=remaining<1;b.captureAge=age>=0?age:remaining<1?8:0;b.flightAge=10;b.angle=.5;bodies[slot]=b;}invalidate();}
    private void text(Canvas c,String s,float x,float y,float size,int color){p.setColor(color);p.setTextSize(size);c.drawText(s,x,y,p);}
    @Override protected void onDraw(Canvas c){c.save();c.scale(getWidth()/1280f,getHeight()/720f);renderer.background(c);text(c,"GRAVITY WELL / BODY VISUAL AUDIT",45,55,27,0xffedf4ff);text(c,(offset>=0?"STELLAR BOSSES":"Page "+(page+1)+" / 3")+"  |  "+Math.round(remaining*100)+"% mass remaining  |  production tablet renderer",45,90,18,0xff91aec7);
        for(int slot=0;slot<6;slot++){GravityWellGame.Body b=bodies[slot];if(b==null)continue;float x=45+slot%3*405,y=120+slot/3*240;p.setColor(0xff0d192b);c.drawRoundRect(x,y,x+385,y+225,12,12,p);
            c.save();c.clipRect(x+2,y+35,x+383,y+204);float cx=x+192,cy=y+118;c.translate(cx,cy);c.scale(.85f,.85f);renderer.body(c,b,-220,0);c.restore();
            text(c,GravityWellGame.KINDS[b.kind].name,x+15,y+26,19,0xffe3edf9);text(c,GravityWellGame.KINDS[b.kind].boss?"BOSS":"ORDINARY",x+15,y+214,12,0xff859ab3);}
        text(c,"Previous",55,656,21,0xff69e4e3);text(c,"Change mass",450,656,21,0xff69e4e3);text(c,"Next",1080,656,21,0xff69e4e3);text(c,"Isolated visual fixtures - no career or workout credit",45,704,15,0xff859ab3);c.restore();boolean pending=false;for(GravityWellGame.Body b:bodies)if(b!=null&&GravityWellGame.kind(b).stellar&&StellarTextures.get(b.kind)==null)pending=true;
        for(GravityWellGame.Body b:bodies)if(b!=null&&b.kind>=GravityWellGame.DWARF&&b.kind<=GravityWellGame.ICE_GIANT&&PlanetTextures.get(b.kind)==null)pending=true;
        long now=android.os.SystemClock.uptimeMillis();if(pending)settledAt=0;else if(settledAt==0)settledAt=now;if(pending||now-settledAt<1300)postInvalidateDelayed(100);}
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX()*1280/getWidth(),y=e.getY()*720/getHeight();if(y<610)return true;if(x<350){offset=-1;page=(page+2)%3;}else if(x>850){offset=-1;page=(page+1)%3;}else remaining=remaining>.8?.5f:remaining>.2?.08f:1;prepare();return true;}
}
