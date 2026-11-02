package org.openrow.probe;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import java.util.concurrent.*;
import java.util.*;

/** Longitude-rotating rocky and giant worlds. All surface synthesis is off the UI thread. */
public final class PlanetTextures {
    public static final int FRAMES=16,SIDE=192;
    private static final ConcurrentHashMap<Integer,Bitmap[]> cache=new ConcurrentHashMap<>();
    private static final Set<Integer> pending=Collections.synchronizedSet(new HashSet<Integer>());
    private static final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Planet texture cache");t.setDaemon(true);t.setPriority(Thread.MIN_PRIORITY);return t;});
    private PlanetTextures(){}
    public static Bitmap[] get(int kind){return cache.get(kind);}
    public static void request(int kind){if(kind<GravityWellGame.DWARF||kind>GravityWellGame.ICE_GIANT||cache.containsKey(kind)||!pending.add(kind))return;worker.execute(()->{try{cache.put(kind,generate(kind));}catch(RuntimeException bad){Log.e("PlanetTextures","Surface cache failed",bad);}finally{pending.remove(kind);}});}
    private static double clamp(double n){return Math.max(0,Math.min(1,n));}
    private static double noise(double x,double y,double z){int ix=(int)Math.floor(x),iy=(int)Math.floor(y),iz=(int)Math.floor(z);double a=x-ix,b=y-iy,c=z-iz;a=a*a*(3-2*a);b=b*b*(3-2*b);c=c*c*(3-2*c);double sum=0;
        for(int k=0;k<2;k++)for(int j=0;j<2;j++)for(int i=0;i<2;i++){int n=(ix+i)*374761393+(iy+j)*668265263+(iz+k)*2147483647;n=(n^(n>>>13))*1274126177;double h=((n^(n>>>16))&0x7fffffff)/2147483647.0;sum+=h*(i==0?1-a:a)*(j==0?1-b:b)*(k==0?1-c:c);}return sum;}
    private static double terrain(double x,double y,double z){return noise(x,y,z)*.54+noise(x*2.03+9,y*2.03,z*2.03)*.28+noise(x*4.13,y*4.13+7,z*4.13)*.13+noise(x*8.31,y*8.31,z*8.31)*.05;}
    private static int mix(int a,int b,double q){q=clamp(q);return Color.rgb((int)(Color.red(a)*(1-q)+Color.red(b)*q),(int)(Color.green(a)*(1-q)+Color.green(b)*q),(int)(Color.blue(a)*(1-q)+Color.blue(b)*q));}
    private static int color(int kind,double longitude,double latitude){
        double ring=Math.cos(latitude),x=Math.sin(longitude)*ring,y=Math.sin(latitude),z=Math.cos(longitude)*ring;
        double coarse=terrain(x*4,y*4,z*4),fine=terrain(x*19+8,y*19,z*19);
        if(kind==GravityWellGame.GAS_GIANT||kind==GravityWellGame.ICE_GIANT){
            double curl=terrain(x*8,y*5,z*8),bands=.5+.5*Math.sin(y*43+(curl-.5)*9+Math.sin(longitude*6+y*9)*.9);
            double storm=Math.exp(-((Math.sin(longitude-.5)*ring)*(Math.sin(longitude-.5)*ring)/.035+(y-.18)*(y-.18)/.006))*Math.max(0,Math.cos(longitude-.5));
            double swirl=.5+.5*Math.sin(Math.atan2(y-.18,Math.sin(longitude-.5)*ring)*3+Math.sqrt(Math.pow(Math.sin(longitude-.5)*ring,2)+(y-.18)*(y-.18))*90);
            int base=kind==GravityWellGame.ICE_GIANT?mix(0xff143a70,0xffb0e2de,bands*.6+fine*.4):mix(0xff6b342b,0xffeecb9b,bands*.65+fine*.35);
            return mix(base,kind==GravityWellGame.ICE_GIANT?mix(0xff567fbb,0xffe4f9f0,swirl):mix(0xffa34c2e,0xfff0b376,swirl),storm*.8);
        }
        if(kind==GravityWellGame.DWARF){double ridges=1-Math.abs(coarse-.5)*5;int base=mix(0xff403e4b,0xffb8afa1,coarse*.7+fine*.3);return mix(base,0xffd1e5e7,clamp((Math.abs(y)-.65)*4)+clamp((fine-.65)*4)*.4+clamp(ridges)*.08);}
        double height=coarse+.035*Math.sin(longitude*11+y*15),land=clamp((height-.49)*35),cloud=clamp((terrain(x*8+13,y*8,z*8)-.54)*8);
        int sea=mix(0xff041c35,0xff116a85,clamp((height-.28)*3)),ground=mix(0xff295749,0xffaa9671,clamp((height-.5)*7)+fine*.15);
        double mountains=clamp((fine-.54)*6)*land;ground=mix(ground,0xffc1b5a1,mountains*.55);
        int base=mix(sea,ground,land);base=mix(base,0xffe1edf0,clamp((Math.abs(y)-.78)*6));return mix(base,0xffecf3ef,cloud*.8);
    }
    private static Bitmap[] generate(int kind){
        final int width=512,height=256;int[] atlas=new int[width*height];for(int y=0;y<height;y++)for(int x=0;x<width;x++)atlas[y*width+x]=color(kind,(x/(double)width-.5)*Math.PI*2,(y/(double)(height-1)-.5)*Math.PI);
        float[] lon=new float[SIDE*SIDE],lat=new float[SIDE*SIDE],shade=new float[SIDE*SIDE];int[] alpha=new int[SIDE*SIDE];
        for(int y=0;y<SIDE;y++)for(int x=0;x<SIDE;x++){int i=y*SIDE+x;double nx=(x-SIDE*.5)/(SIDE*55.0/128),ny=(y-SIDE*.5)/(SIDE*55.0/128),r2=nx*nx+ny*ny;if(r2>=1)continue;double nz=Math.sqrt(1-r2);lon[i]=(float)(Math.atan2(nx,nz)/(Math.PI*2)+.5);lat[i]=(float)((Math.asin(ny)/Math.PI+.5)*(height-1));shade[i]=(float)(.72+.28*nz);alpha[i]=(int)(255*clamp((1-r2)*65));}
        Bitmap[] frames=new Bitmap[FRAMES];int[] pixels=new int[SIDE*SIDE];for(int frame=0;frame<FRAMES;frame++){for(int i=0;i<pixels.length;i++){if(alpha[i]==0){pixels[i]=0;continue;}double u=(lon[i]+frame/(double)FRAMES)%1*width;int a=(int)u,y=(int)lat[i];int c=mix(atlas[y*width+a],atlas[y*width+(a+1)%width],u-a);pixels[i]=Color.argb(alpha[i],(int)(Color.red(c)*shade[i]),(int)(Color.green(c)*shade[i]),(int)(Color.blue(c)*shade[i]));}frames[frame]=Bitmap.createBitmap(pixels,SIDE,SIDE,Bitmap.Config.ARGB_8888);}return frames;
    }
}
