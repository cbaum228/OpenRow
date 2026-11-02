package org.openrow.probe;

import android.graphics.Bitmap;
import android.util.Log;
import java.util.*;
import java.util.concurrent.*;

/** Immutable spherical rotation frames. Generation runs once, off the animation thread. */
public final class StellarTextures {
    public static final int FRAMES=24,SIDE=256;
    public static final class Frames {
        public final Bitmap[] images;
        private Frames(Bitmap[] images){this.images=images;}
    }
    private static final ConcurrentHashMap<Integer,Frames> cache=new ConcurrentHashMap<>();
    private static final Set<Integer> pending=Collections.synchronizedSet(new HashSet<Integer>());
    private static final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Stellar texture cache");t.setDaemon(true);t.setPriority(Thread.MIN_PRIORITY);return t;});
    private StellarTextures(){}
    public static Frames get(int kind){return cache.get(kind);}
    public static void request(final int kind){
        if(kind<GravityWellGame.BROWN_DWARF||kind>GravityWellGame.NEUTRON||cache.containsKey(kind)||!pending.add(kind))return;
        worker.execute(()->{long start=System.nanoTime();try{cache.put(kind,generate(kind));Log.i("StellarTextures","Cached "+GravityWellGame.KINDS[kind].name+" in "+((System.nanoTime()-start)/1000000)+"ms");}catch(RuntimeException e){Log.e("StellarTextures","Surface cache failed",e);}finally{pending.remove(kind);}});
    }
    private static double clamp(double x){return Math.max(0,Math.min(1,x));}
    private static double hash(int x,int y,int z){int n=x*374761393+y*668265263+z*2147483647;n=(n^(n>>>13))*1274126177;return ((n^(n>>>16))&0x7fffffff)/2147483647.0;}
    private static double noise(double x,double y,double z){
        int ix=(int)Math.floor(x),iy=(int)Math.floor(y),iz=(int)Math.floor(z);double a=x-ix,b=y-iy,c=z-iz;
        a=a*a*(3-2*a);b=b*b*(3-2*b);c=c*c*(3-2*c);double sum=0;
        for(int k=0;k<2;k++)for(int j=0;j<2;j++)for(int i=0;i<2;i++)sum+=hash(ix+i,iy+j,iz+k)*(i==0?1-a:a)*(j==0?1-b:b)*(k==0?1-c:c);
        return sum;
    }
    private static double fbm(double x,double y,double z){return noise(x,y,z)*.55+noise(x*2.03+8,y*2.03,z*2.03)*.28+noise(x*4.11,y*4.11+9,z*4.11)*.17;}
    private static int mix(int a,int b,double q){q=clamp(q);return 0xff000000|((int)(((a>>16)&255)*(1-q)+((b>>16)&255)*q)<<16)|((int)(((a>>8)&255)*(1-q)+((b>>8)&255)*q)<<8)|(int)((a&255)*(1-q)+(b&255)*q);}
    private static int palette(double heat,int dark,int warm,int hot){return heat<.55?mix(dark,warm,heat/.55):mix(warm,hot,(heat-.55)/.45);}
    private static int sample(int kind,double x,double y,double z,double longitude){
        double warp=fbm(x*3+13,y*3,z*3),broad=fbm(x*5+(warp-.5)*2,y*5,z*5),fine=noise(x*53,y*53+7,z*53),mid=fbm(x*15,y*15,z*15);
        if(kind==GravityWellGame.BROWN_DWARF){
            double bands=.5+.5*Math.sin(y*29+(warp-.5)*9+Math.sin(longitude*4+y*6)*1.8);
            double embers=clamp((mid-.47)*5),storm=Math.exp(-((x-.48)*(x-.48)/.035+(y+.12)*(y+.12)/.012+(z-.86)*(z-.86)/.12));
            double heat=clamp(.12+.26*bands+.36*broad+.28*embers+.25*storm);
            int color=palette(heat,0xff100e20,0xff985032,0xffffc57b);
            return mix(color,0xff211225,clamp((warp-.51)*6)*.72);
        }
        if(kind==GravityWellGame.NEUTRON){
            double pole=Math.pow(Math.abs(y),6),magnetic=.5+.5*Math.sin(longitude*5+y*11+(warp-.5)*8);
            int color=palette(clamp(.16+.35*broad+.18*magnetic+.18*fine),0xff100e52,0xff5a3bd0,0xff68dbff);
            return mix(color,0xffe8fdff,clamp((pole-.35)*1.6));
        }
        if(kind==GravityWellGame.WHITE_DWARF){
            double wisps=clamp(1-Math.abs(mid-.5)*13),curl=.5+.5*Math.sin(longitude*8+y*16+(warp-.5)*14);
            return palette(clamp(.43+.29*broad+.12*wisps+.13*curl+.04*fine),0xff175aa0,0xff69c7ed,0xfff3fdff);
        }
        double filaments=clamp(1-Math.abs(mid-.51)*18);
        if(kind==GravityWellGame.RED_GIANT){
            double cells=fbm(x*3.4+warp,y*3.4,z*3.4),trenches=clamp(1-Math.abs(cells-.49)*21);
            double heat=clamp(.24+.65*cells+.21*mid+.07*fine-.28*trenches);
            int color=palette(heat,0xff39091a,0xffdc4a1a,0xffffd394);
            return mix(color,0xffffdca3,filaments*clamp((broad-.5)*6)*.56);
        }
        double heat=clamp(.12+.61*broad+.26*mid+.16*fine+.16*filaments*clamp((broad-.44)*5));
        int color=kind==GravityWellGame.SUN?palette(heat,0xff8c240b,0xffffb627,0xfffff5c2):palette(heat,0xff2a071a,0xffca351d,0xffffbd71);
        double spot=Math.exp(-((x+.32)*(x+.32)/.012+(y-.18)*(y-.18)/.007+(z-.93)*(z-.93)/.08));
        double spot2=Math.exp(-((x+.18)*(x+.18)/.004+(y-.23)*(y-.23)/.003+(z-.96)*(z-.96)/.06));
        return mix(color,kind==GravityWellGame.SUN?0xff4c160d:0xff2b0919,Math.max(spot,spot2)*.9);
    }
    private static Frames generate(int kind){
        final int width=768,height=384;int[] atlas=new int[width*height];
        for(int v=0;v<height;v++){double latitude=(v/(double)(height-1)-.5)*Math.PI,y=Math.sin(latitude),ring=Math.cos(latitude);
            for(int u=0;u<width;u++){double longitude=(u/(double)width-.5)*Math.PI*2;atlas[v*width+u]=sample(kind,Math.sin(longitude)*ring,y,Math.cos(longitude)*ring,longitude);}}
        float[] longitude=new float[SIDE*SIDE],latitude=new float[SIDE*SIDE],shade=new float[SIDE*SIDE];int[] alpha=new int[SIDE*SIDE];
        for(int y=0;y<SIDE;y++)for(int x=0;x<SIDE;x++){int i=y*SIDE+x;double nx=(x-128)/110.0,ny=(y-128)/110.0,r2=nx*nx+ny*ny;if(r2>=1)continue;
            double nz=Math.sqrt(1-r2);longitude[i]=(float)(Math.atan2(nx,nz)/(Math.PI*2)+.5);latitude[i]=(float)((Math.asin(ny)/Math.PI+.5)*(height-1));
            shade[i]=(float)(kind==GravityWellGame.BROWN_DWARF?.28+.72*Math.pow(nz,.55):kind==GravityWellGame.WHITE_DWARF?.5+.5*Math.pow(nz,.42):.34+.66*Math.pow(nz,.45));alpha[i]=(int)(255*clamp((1-r2)*70));}
        Bitmap[] frames=new Bitmap[FRAMES];int[] pixels=new int[SIDE*SIDE];
        for(int frame=0;frame<FRAMES;frame++){
            for(int i=0;i<pixels.length;i++){if(alpha[i]==0){pixels[i]=0;continue;}double u=(longitude[i]+frame/(double)FRAMES)%1*width;int x=(int)u,y=(int)latitude[i];double q=u-x;
                int color=mix(atlas[y*width+x],atlas[y*width+(x+1)%width],q);double light=shade[i];pixels[i]=(alpha[i]<<24)|((int)(((color>>16)&255)*light)<<16)|((int)(((color>>8)&255)*light)<<8)|(int)((color&255)*light);}
            frames[frame]=Bitmap.createBitmap(pixels,SIDE,SIDE,Bitmap.Config.ARGB_8888);
        }
        return new Frames(frames);
    }
}
