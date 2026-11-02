package org.openrow.probe;

import android.graphics.*;

/** Cached illustrative light bending and turbulent gas, with a small animated foreground. */
public final class SingularityRenderer {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Path filament=new Path(),shadow=new Path();
    private final RectF bounds=new RectF();
    private final Bitmap[] layers=new Bitmap[2];
    private final float[] diskMesh=new float[17*9*2];
    private final float[] sparks=new float[192];
    private Bitmap feedingGlow;
    private int quality=1;
    public void quality(int level){quality=level;}
    private static double clamp(double v){return Math.max(0,Math.min(1,v));}

    private Bitmap layer(boolean violet){
        int index=violet?1:0;if(layers[index]!=null)return layers[index];
        final int side=640;int[] pixels=new int[side*side];
        for(int y=0;y<side;y++)for(int x=0;x<side;x++){
            double px=(x-side/2.0)/(side/10.0),py=(y-side/2.0)/(side/10.0);
            double spherical=Math.hypot(px,py);
            if(spherical<1.015){pixels[y*side+x]=0xff010208;continue;}
            double diskR=Math.hypot(px,py/.29),diskA=Math.atan2(py/.29,px);
            double disk=0,heat=0;
            if(diskR>1.35&&diskR<4.55){
                double inner=clamp((diskR-1.35)/.32),outer=clamp((4.55-diskR)/1.65);
                double bands=.58+.22*Math.sin(diskR*19+diskA*4+2*Math.sin(diskA*3+diskR*3))
                    +.14*Math.sin(diskR*47-diskA*7+Math.sin(diskA*5)*3)+.09*Math.sin(diskR*83+diskA*17);
                long hash=((x*73856093L)^(y*19349663L))&1023;
                double grain=.78+.22*hash/1023.0;
                double approaching=.65+.35*(1-px/diskR);
                disk=inner*outer*Math.pow(1.7/diskR,1.2)*bands*grain*approaching;
                heat=clamp((3.7-diskR)/2.2);
            }
            // Broad upper image of the rear disk, with a thinner underside echo.
            double a=Math.atan2(py,px),lensR=Math.hypot(px,py/(py<0?1.08:.88));
            double bandCenter=1.18+.012*Math.sin(a*3),width=py<0?.17:.065;
            double delta=(lensR-bandCenter)/width;
            double lens=Math.exp(-delta*delta*2)*(py<0?.62:.22)
                *(.75-.25*px/Math.max(1,spherical))*(.82+.18*Math.sin(a*43+lensR*61));
            double photon=Math.exp(-Math.pow((spherical-1.035)/.022,2))*.60;
            double glow=Math.exp(-Math.pow((spherical-1.1)/.25,2))*.045;
            double total=disk+lens+photon+glow;
            if(total<.002)continue;
            double hot=clamp((disk*heat+(lens+photon)*.95)/Math.max(.001,total));
            int red=violet?(int)(146+105*hot):255;
            int green=violet?(int)(95+137*hot):(int)(100+139*hot);
            int blue=violet?255:(int)(35+172*hot);
            pixels[y*side+x]=Color.argb((int)(clamp(total)*245),red,green,blue);
        }
        Bitmap bitmap=Bitmap.createBitmap(pixels,side,side,Bitmap.Config.ARGB_8888);layers[index]=bitmap;return bitmap;
    }

    public void draw(Canvas c,float x,float y,float radius,double phase,double effort,boolean violet){
        draw(c,x,y,radius,phase,effort,violet,0);
    }
    public void draw(Canvas c,float x,float y,float radius,double phase,double effort,boolean violet,double feeding){
        Bitmap base=layer(violet);float drive=(float)clamp(effort);
        c.save();c.translate(x,y);c.rotate(-12);
        bounds.set(-radius*5,-radius*5,radius*5,radius*5);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);
        // Low-resolution UV flow moves the cached turbulent disk without spinning its plane.
        for(int row=0;row<=8;row++)for(int col=0;col<=16;col++){double px=(col/8.0-1)*5,py=(row/4.0-1)*5,r=Math.hypot(px,py/.29),envelope=Math.exp(-Math.pow((r-2.9)/1.6,2));
            double wave=Math.sin(r*4+Math.atan2(py/.29,px)*3-phase*2.7)*envelope;
            double protect=Math.max(0,Math.min(1,(Math.hypot(px,py)-1.22)*3));int i=(row*17+col)*2;
            diskMesh[i]=(float)((px+.043*wave*protect)*radius);diskMesh[i+1]=(float)((py+.018*Math.cos(r*5-phase*3)*envelope*protect)*radius);}
        c.drawBitmapMesh(base,16,8,diskMesh,0,null,0,p);
        p.setColor(0xff010208);c.drawCircle(0,0,radius*1.018f,p);p.setColor(Color.WHITE);
        shadow.reset();shadow.addCircle(0,0,radius*1.018f,Path.Direction.CW);
        c.save();c.clipOutPath(shadow);
        // Effort lights the gas without lifting the black level inside the shadow.
        p.setAlpha((int)(drive*45));c.drawBitmap(base,null,bounds,p);p.setAlpha(255);
        p.setStyle(Paint.Style.STROKE);
        for(int i=0;i<(quality==0?4:7);i++){
            filament.reset();double lane=1.65+i*.34,start=i*2.39996+phase*(1.5-i*.12);
            for(int j=0;j<=28;j++){
                double a=start+j*.031,r=radius*(lane+.10*Math.sin(a*3+phase)+.055*Math.sin(a*7+i)+j*.003);
                float px=(float)(Math.cos(a)*r),py=(float)(Math.sin(a)*r*.29);
                if(j==0)filament.moveTo(px,py);else filament.lineTo(px,py);
            }
            p.setStrokeWidth(radius*.055f);p.setColor(violet?0x18bfa2ef:0x18ffc17b);c.drawPath(filament,p);
            p.setStrokeWidth(radius*.016f);p.setColor(violet?Color.argb(35+(int)(drive*30),220,196,255):Color.argb(35+(int)(drive*30),255,219,166));c.drawPath(filament,p);
        }
        int count=0;
        for(int i=0;i<(quality==0?48:96);i++){
            double lane=(i*.61803398875)%1,r=(1.5+lane*2.55)*radius,a=i*2.399963+phase*(1.6-lane);
            float px=(float)(Math.cos(a)*r),py=(float)(Math.sin(a)*r*.29);
            if(px*px+py*py<radius*radius*1.05)continue;sparks[count++]=px;sparks[count++]=py;
        }
        p.setColor(violet?0x70d4bbff:0x70ffdfab);p.setStrokeWidth(.9f);c.drawPoints(sparks,0,count,p);
        if(feeding>.015&&quality>0){
            if(feedingGlow==null){int[] pixels=new int[96*40];for(int sy=0;sy<40;sy++)for(int sx=0;sx<96;sx++){double dx=(sx-48)/48.0,dy=(sy-20)/20.0,soft=Math.exp(-(dx*dx*3+dy*dy*5));pixels[sy*96+sx]=Color.argb((int)(110*soft),255,219,160);}feedingGlow=Bitmap.createBitmap(pixels,96,40,Bitmap.Config.ARGB_8888);}
            double a=phase*2.2+.8,ring=radius*2.1;float gx=(float)(Math.cos(a)*ring),gy=(float)(Math.sin(a)*ring*.29);bounds.set(gx-radius*.85f,gy-radius*.28f,gx+radius*.85f,gy+radius*.28f);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setAlpha((int)(Math.min(.65,feeding)*160));c.drawBitmap(feedingGlow,null,bounds,p);p.setAlpha(255);p.setStyle(Paint.Style.STROKE);
        }
        // Rear-disk lens arcs are visible above the shadow; their flow is deliberately asymmetric.
        for(int arc=0;arc<3;arc++){
            filament.reset();for(int j=0;j<=26;j++){double a=Math.PI+(j/26.0)*Math.PI,r=radius*(1.13+arc*.045+.009*Math.sin(a*11-phase*2+arc));float px=(float)(Math.cos(a)*r),py=(float)(Math.sin(a)*r*1.09);if(j==0)filament.moveTo(px,py);else filament.lineTo(px,py);}
            p.setColor(violet?0xffd2b7ff:0xffffe8bd);p.setAlpha((int)((25+drive*18)*(1-arc*.22)));p.setStrokeWidth(Math.max(.55f,radius*.012f));c.drawPath(filament,p);
        }p.setAlpha(255);
        p.setStyle(Paint.Style.FILL);c.restore();c.restore();
    }
}
