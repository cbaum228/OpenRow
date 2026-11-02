package org.openrow.probe;
import android.graphics.*;

/** Small cached vapor wake that is bent into the accretion flow near the well. */
public final class CometTailRenderer {
    private final Paint paint=new Paint(3);
    private final Path shadow=new Path();
    private final float[] centers=new float[18],mesh=new float[54],point=new float[2];
    private Bitmap texture;
    public void draw(Canvas c,GravityWellGame.Body b,float cx,float cy){
        if(b.flightAge<=0||b.captured&&b.captureAge>=2.2)return;
        if(texture==null){int[] pixels=new int[128*32];for(int y=0;y<32;y++)for(int x=0;x<128;x++){
            double cross=(y-15.5)/15.5,soft=Math.pow(Math.max(0,1-cross*cross),2.5),fade=Math.pow(1-x/127.0,1.5);
            pixels[y*128+x]=Color.argb((int)(165*soft*fade),167,221,246);}
            texture=Bitmap.createBitmap(pixels,128,32,Bitmap.Config.ARGB_8888);}
        c.save();c.translate(cx,cy);shadow.reset();shadow.addCircle(0,0,50,Path.Direction.CW);c.clipOutPath(shadow);
        for(int i=0;i<=8;i++){GravityWellScene.cometTailPoint(b,i/8.0,point);centers[i*2]=point[0];centers[i*2+1]=point[1];}
        for(int i=0;i<=8;i++){int before=Math.max(0,i-1)*2,after=Math.min(8,i+1)*2;
            double dx=centers[after]-centers[before],dy=centers[after+1]-centers[before+1],length=Math.max(1,Math.hypot(dx,dy));
            double width=(2+GravityWellGame.visualRadius(b)*.34*(1-i/8.0))*Math.min(1,b.flightAge);
            for(int row=0;row<3;row++){int v=(row*9+i)*2;mesh[v]=centers[i*2]+(float)(-dy/length*width*(row-1));mesh[v+1]=centers[i*2+1]+(float)(dx/length*width*(row-1));}}
        paint.setColor(Color.WHITE);paint.setAlpha((int)(b.visualOpacity*(b.captured?255*Math.max(0,1-b.captureAge/2.2):255)));
        c.drawBitmapMesh(texture,8,2,mesh,0,null,0,paint);paint.setAlpha(255);c.restore();
    }
}
