package org.openrow.probe;

import android.graphics.*;

/** Cached textured mantle chips pulled and sheared along the same flow as the detached rings. */
public final class MantleFragmentRenderer {
    private final Bitmap[][] chips=new Bitmap[GravityWellGame.KINDS.length+4][3];
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Path horizon=new Path();
    private final RectF fragmentBounds=new RectF();
    private final float[] center=new float[3],quad=new float[8];
    private int drawn;
    public void beginFrame(){drawn=0;}
    public void body(Canvas c,GravityWellGame.Body b,Bitmap surface,float size){
        int variant=Math.floorMod((int)Math.round(b.angle-b.flightAge*.22),3);Bitmap texture=chip(b.kind<0?b.material:b.kind+4,variant,surface);
        c.save();c.rotate((float)Math.toDegrees(b.angle));c.scale(1,(float)(.65+.35*Math.abs(Math.cos(b.flightAge*.5+variant))));paint.setColor(Color.WHITE);paint.setAlpha((int)(255*b.visualOpacity));fragmentBounds.set(-size,-size,size,size);c.drawBitmap(texture,null,fragmentBounds,paint);paint.setAlpha(255);c.restore();
    }

    private Bitmap chip(int material,int variant,Bitmap surface){
        if(chips[material][variant]!=null)return chips[material][variant];
        Bitmap bitmap=Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bitmap);
        Path shape=new Path();
        for(int i=0;i<7;i++){double a=i*Math.PI*2/7,r=22+4*Math.sin(i*2.71+variant);float x=32+(float)(Math.cos(a)*r),y=27+(float)(Math.sin(a)*r*.82);
            if(i==0)shape.moveTo(x,y);else shape.lineTo(x,y);}
        shape.close();c.save();c.translate(2,9);paint.setShader(new LinearGradient(0,15,0,62,material==GravityWellGame.PLANET+4?0xffe98c3b:0xff647b87,0xff101623,Shader.TileMode.CLAMP));c.drawPath(shape,paint);paint.setShader(null);c.restore();
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.3f);paint.setColor(0xff24303c);for(int i=0;i<4;i++){float yy=37+i*5;c.drawLine(15,yy,49,yy+3,paint);}paint.setStyle(Paint.Style.FILL);
        c.save();c.clipPath(shape);paint.setColor(Color.WHITE);
        int x=22+variant*17,y=26+variant*11;
        c.drawBitmap(surface,new Rect(x*surface.getWidth()/128,y*surface.getHeight()/128,(x+40)*surface.getWidth()/128,(y+40)*surface.getHeight()/128),new RectF(0,0,64,54),paint);
        paint.setShader(new LinearGradient(0,0,64,64,0x22fff0d0,0xa501050e,Shader.TileMode.CLAMP));c.drawPaint(paint);paint.setShader(null);c.restore();
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.2f);paint.setColor(0x9ce8d7b3);c.drawPath(shape,paint);paint.setStyle(Paint.Style.FILL);
        chips[material][variant]=bitmap;return bitmap;
    }

    public void draw(Canvas c,GravityWellGame.Body b,Bitmap surface,float radius,boolean front){
        if(!b.captured||drawn>=48)return;
        if(b.kind>=0&&(GravityWellGame.kind(b).stellar||b.kind==GravityWellGame.CLOUD||b.kind==GravityWellGame.GAS_GIANT||b.kind==GravityWellGame.ICE_GIANT))return;
        double stretch=GravityWellGame.tidalStretch(b),distance=Math.hypot(b.x,b.y),angle=Math.atan2(b.y,b.x);
        float hx=(float)(-distance/stretch),rx=(float)(50/stretch),ry=(float)(50*Math.sqrt(stretch));
        c.save();horizon.reset();horizon.addOval(hx-rx,-ry,hx+rx,ry,Path.Direction.CW);c.clipOutPath(horizon);
        for(int slot=0;slot<15&&drawn<48;slot++){
            double progress=GravityWellGame.mantleProgress(b,slot);if(progress<=0||progress>=1)continue;
            GravityWellGame.mantlePoint(b,slot,center);
            double viewY=Math.sin(angle)*center[0]*stretch+Math.cos(angle)*center[1]/Math.sqrt(stretch);
            if((viewY>=0)!=front)continue;
            double width=radius*(slot<6?.39:slot<12?.32:.16)*(1-.68*progress);
            double length=width*1.22,rotation=slot*.87+progress*(3.1+slot%3*.4);
            double cos=Math.cos(rotation),sin=Math.sin(rotation);
            for(int corner=0;corner<4;corner++){double px=corner%2==0?-length:length,py=corner<2?-width:width;
                quad[corner*2]=center[0]+(float)(px*cos-py*sin);quad[corner*2+1]=center[1]+(float)(px*sin+py*cos);}
            Bitmap texture=chip(b.kind<0?b.material:b.kind+4,slot%3,surface);
            paint.setColor(Color.WHITE);paint.setAlpha((int)(255*b.visualOpacity*Math.min(1,progress*15)*Math.min(1,(1-progress)*6)));
            c.drawBitmapMesh(texture,1,1,quad,0,null,0,paint);drawn++;
        }
        paint.setAlpha(255);c.restore();
    }
}
