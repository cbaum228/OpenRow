package org.openrow.probe;

import android.graphics.*;

/** Small deterministic surface faults; visual only, driven by the existing mass loss. */
public final class FractureRenderer {
    private final Paint paint=new Paint(3);private final Path fault=new Path(),branch=new Path();
    private int drawn;
    public void beginFrame(){drawn=0;}
    public void draw(Canvas c,GravityWellGame.Body b,float radius){
        if(!b.captured||radius<13||drawn>=8||b.visualOpacity<.6&&radius<24||b.kind==GravityWellGame.CLOUD||b.kind==GravityWellGame.GAS_GIANT||b.kind==GravityWellGame.ICE_GIANT||GravityWellGame.kind(b).stellar)return;
        double remaining=Math.max(0,Math.min(1,b.mass/b.initialMass)),damage=1-remaining;if(damage<.015)return;
        drawn++;
        boolean ice=b.material==1;double visibility=Math.min(1,damage*8)*Math.min(1,remaining*12)*b.visualOpacity;
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStrokeCap(Paint.Cap.ROUND);
        for(int seam=0;seam<7;seam++){
            double seed=b.initialMass*.137+seam*2.39996,a=Math.PI+(seam-3)*.43,extent=Math.min(1,Math.max(0,(damage-seam*.023)*3));if(extent<=0)continue;
            float rootX=(float)(radius*(-.16+.27*Math.cos(seed*1.3))),rootY=(float)(radius*.5*Math.sin(seed*.73));fault.reset();fault.moveTo(rootX,rootY);
            float lastX=rootX,lastY=rootY;
            for(int step=1;step<=6;step++){double t=step/6.0*extent,r=radius*t*.95,wiggle=Math.sin(seed+step*2.7)*radius*.07*t;
                lastX=rootX+(float)(Math.cos(a)*r-Math.sin(a)*wiggle);lastY=rootY+(float)(Math.sin(a)*r+Math.cos(a)*wiggle);fault.lineTo(lastX,lastY);}
            paint.setStrokeWidth(Math.max(1.2f,radius*.045f));paint.setColor(Color.argb((int)(185*visibility),5,9,16));c.drawPath(fault,paint);
            paint.setStrokeWidth(Math.max(.6f,radius*.014f));paint.setColor(ice?Color.argb((int)(200*visibility),142,227,248):Color.argb((int)(200*visibility),255,156,71));c.drawPath(fault,paint);
            float bx=rootX+(lastX-rootX)*.6f,by=rootY+(lastY-rootY)*.6f;branch.reset();branch.moveTo(bx,by);branch.lineTo(bx+radius*.1f,by-radius*.09f);branch.lineTo(bx+radius*.19f,by-radius*.12f);paint.setAlpha((int)(100*visibility));c.drawPath(branch,paint);
        }paint.setStyle(Paint.Style.FILL);paint.setStrokeCap(Paint.Cap.BUTT);paint.setAlpha(255);
    }
}
