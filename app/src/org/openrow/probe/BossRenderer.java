package org.openrow.probe;
import android.graphics.*;

/** High-detail cached spherical surfaces and bounded animated coronas for the encounter focus. */
public final class BossRenderer {
    private final Bitmap[][] photos=new Bitmap[GravityWellGame.KINDS.length][2];
    private final Bitmap[] halos=new Bitmap[GravityWellGame.KINDS.length];
    private final boolean[] waiting=new boolean[GravityWellGame.KINDS.length];
    private final long[] readyAt=new long[GravityWellGame.KINDS.length];
    private Bitmap beam;
    public void prepare(){for(int kind=GravityWellGame.DWARF;kind<=GravityWellGame.ICE_GIANT;kind++)PlanetTextures.request(kind);for(int kind=GravityWellGame.BROWN_DWARF;kind<=GravityWellGame.NEUTRON;kind++)StellarTextures.request(kind);}
    private final Paint p=new Paint(3);
    private final RectF rect=new RectF();
    private final Path loop=new Path(),shadow=new Path();
    private final float[] pulledSurface=new float[7*5*2];
    private final float[] coronaMesh=new float[13*9*2],glowPoint=new float[2];
    private int quality=1;
    public void quality(int level){quality=level;}
    private static double clamp(double x){return Math.max(0,Math.min(1,x));}
    private static double hash(int x,int y,int z){int n=x*374761393+y*668265263+z*2147483647;n=(n^(n>>>13))*1274126177;return ((n^(n>>>16))&0x7fffffff)/2147483647.0;}
    private static double noise(double x,double y,double z){
        int ix=(int)Math.floor(x),iy=(int)Math.floor(y),iz=(int)Math.floor(z);double a=x-ix,b=y-iy,c=z-iz;
        a=a*a*(3-2*a);b=b*b*(3-2*b);c=c*c*(3-2*c);double sum=0;
        for(int k=0;k<2;k++)for(int j=0;j<2;j++)for(int i=0;i<2;i++)sum+=hash(ix+i,iy+j,iz+k)*(i==0?1-a:a)*(j==0?1-b:b)*(k==0?1-c:c);
        return sum;
    }
    private static double fbm(double x,double y,double z){return noise(x,y,z)*.57+noise(x*2.07+8,y*2.07,z*2.07)*.28+noise(x*4.13,y*4.13+9,z*4.13)*.15;}
    private static int mix(int a,int b,double t){t=clamp(t);return Color.rgb((int)(Color.red(a)*(1-t)+Color.red(b)*t),(int)(Color.green(a)*(1-t)+Color.green(b)*t),(int)(Color.blue(a)*(1-t)+Color.blue(b)*t));}
    private static boolean blue(int kind){return kind==GravityWellGame.WHITE_DWARF||kind==GravityWellGame.NEUTRON;}
    public Bitmap surface(GravityWellGame.Body b,int variant){
        Bitmap[] planet=PlanetTextures.get(b.kind);if(planet!=null)return planet[0];PlanetTextures.request(b.kind);
        if(GravityWellGame.kind(b).stellar){
            StellarTextures.Frames ready=StellarTextures.get(b.kind);if(ready!=null)return ready.images[0];
            StellarTextures.request(b.kind);waiting[b.kind]=true;
            if(photos[b.kind][0]==null){Bitmap fallback=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);Canvas f=new Canvas(fallback);p.setColor(Color.WHITE);boolean cool=blue(b.kind)||b.kind==GravityWellGame.NEUTRON;p.setShader(new android.graphics.RadialGradient(128,128,110,new int[]{cool?0xffb0ddf7:0xffffba60,cool?0xff2b3c89:0xff792b16},null,android.graphics.Shader.TileMode.CLAMP));f.drawCircle(128,128,110,p);p.setShader(null);photos[b.kind][0]=fallback;}
            return photos[b.kind][0];
        }
        if(photos[b.kind][variant]!=null)return photos[b.kind][variant];
        int[] pixels=new int[256*256];
        for(int y=0;y<256;y++)for(int x=0;x<256;x++){
            double nx=(x-128)/110.0,ny=(y-128)/110.0,r2=nx*nx+ny*ny;if(r2>=1)continue;
            double nz=Math.sqrt(1-r2),offset=variant*1.7,coarse=fbm(nx*5+offset,ny*5,nz*5);
            int color;
                if(b.kind==GravityWellGame.GAS_GIANT||b.kind==GravityWellGame.ICE_GIANT){
                    double band=.5+.5*Math.sin(ny*31+(coarse-.5)*5),storm=Math.exp(-((nx-.3)*(nx-.3)/.06+(ny-.2)*(ny-.2)/.012));
                    color=b.kind==GravityWellGame.ICE_GIANT?mix(0xff164d8c,0xffaadbe0,band*.8+coarse*.2):mix(0xff844431,0xffedc994,band*.75+coarse*.25);color=mix(color,0xffe49858,storm*.7);
                }else if(b.kind==GravityWellGame.PLANET){color=coarse>.51?mix(0xff28655b,0xffb3a068,(coarse-.51)*5):mix(0xff073253,0xff268aae,coarse*1.6);double cloud=clamp((fbm(nx*9+6,ny*9,nz*9)-.55)*5);color=mix(color,0xffe8f3ed,cloud*.8);}
                else color=mix(0xff4e4a55,0xffc8b7a0,coarse);
            pixels[y*256+x]=(color&0xffffff)|((int)(255*clamp((1-r2)*70))<<24);
        }
        photos[b.kind][variant]=Bitmap.createBitmap(pixels,256,256,Bitmap.Config.ARGB_8888);return photos[b.kind][variant];
    }
    private Bitmap halo(GravityWellGame.Body b){
        if(halos[b.kind]!=null)return halos[b.kind];boolean star=GravityWellGame.kind(b).stellar,cool=blue(b.kind)||!star&&b.material!=2;
        int[] pixels=new int[384*384];
        for(int y=0;y<384;y++)for(int x=0;x<384;x++){
            double nx=(x-192)/80.0,ny=(y-192)/80.0,r=Math.hypot(nx,ny);if(r<.95)continue;
            double a=Math.atan2(ny,nx),inner=Math.exp(-Math.pow((r-1.03)/(star?.12:.045),2));
            double rays=.48+.23*Math.sin(a*7+Math.sin(a*3)*2.3)+.18*Math.sin(a*19+Math.sin(a*11));
            double outer=star?Math.exp(-Math.max(0,r-1)*(b.kind==GravityWellGame.RED_GIANT?2.7:3.7))*rays*(b.kind==GravityWellGame.BROWN_DWARF?.14:.32):0;
            int alpha=(int)(255*clamp(inner*(star?.65:.23)+outer));
            int color=b.kind==GravityWellGame.NEUTRON?mix(0xff7139c7,0xffa3eaff,inner):cool?mix(0xff3d91de,0xffd9f6ff,inner):b.kind==GravityWellGame.BROWN_DWARF?mix(0xff6f2545,0xffe0a15f,inner):mix(0xffed5f0a,0xffffe2a1,inner);
            pixels[y*384+x]=(color&0xffffff)|(alpha<<24);
        }
        halos[b.kind]=Bitmap.createBitmap(pixels,384,384,Bitmap.Config.ARGB_8888);return halos[b.kind];
    }
    public void drawHalo(Canvas c,GravityWellGame.Body b,float cx,float cy,float radius){
        boolean star=GravityWellGame.kind(b).stellar,fluid=GravityWellScene.fluid(b);double atmosphere=fluid||!b.captured?1:clamp((b.mass/b.initialMass-.94)/.06);if(atmosphere<=0||b.visualOpacity<.5&&quality==0)return;float x=cx+(float)b.x,y=cy+(float)b.y;
        float live=radius*(float)(fluid&&b.captured?Math.cbrt(Math.max(0,b.mass/b.initialMass)):GravityWellGame.erosionRadius(b,Math.PI*.5));Bitmap halo=halo(b);
        c.save();c.translate(x,y);shadow.reset();shadow.addCircle(cx-x,cy-y,50,Path.Direction.CW);c.clipOutPath(shadow);
        double pull=GravityWellScene.fluidPull(b);
        p.setColor(Color.WHITE);p.setAlpha((int)((225+25*Math.sin(b.flightAge*.28))*atmosphere*(.55+.45*b.visualEnergy)*b.visualOpacity));rect.set(-live*2.4f,-live*2.4f,live*2.4f,live*2.4f);
        if(fluid&&pull>.01){for(int row=0;row<=8;row++)for(int col=0;col<=12;col++){float px=(col/6f-1)*live*2.4f,py=(row/4f-1)*live*2.4f;mapFluid(b,px,py,live*64/55,coronaMesh,(row*13+col)*2);}c.drawBitmapMesh(halo,12,8,coronaMesh,0,null,0,p);}else c.drawBitmap(halo,null,rect,p);p.setAlpha(255);
        if(b.kind==GravityWellGame.NEUTRON){drawBeams(c,b,live);}
        else if(star){p.setStyle(Paint.Style.STROKE);int loops=b.kind==GravityWellGame.BROWN_DWARF?2:b.kind==GravityWellGame.WHITE_DWARF?3:b.kind==GravityWellGame.RED_GIANT?6:5;
            for(int i=0;i<loops;i++){double seed=hash(i,b.kind,13),a=seed*Math.PI*2+b.flightAge*(.008+seed*.01),activity=Math.pow(.5+.5*Math.sin(b.flightAge*(.26+seed*.13)+i*2.7),3);
                double h=(b.kind==GravityWellGame.RED_GIANT?.3:.12)+(.12+seed*.22)*activity,start=a-.07-seed*.05,end=a+.08+seed*.08;loop.reset();
                mapFluid(b,(float)(Math.cos(start)*live),(float)(Math.sin(start)*live),live*64/55,glowPoint,0);loop.moveTo(glowPoint[0],glowPoint[1]);
                mapFluid(b,(float)(Math.cos(a-.05)*live*(1+h)),(float)(Math.sin(a-.05)*live*(1+h)),live*64/55,coronaMesh,0);
                mapFluid(b,(float)(Math.cos(a+.1)*live*(1+h)),(float)(Math.sin(a+.1)*live*(1+h)),live*64/55,coronaMesh,2);
                mapFluid(b,(float)(Math.cos(end)*live),(float)(Math.sin(end)*live),live*64/55,coronaMesh,4);loop.cubicTo(coronaMesh[0],coronaMesh[1],coronaMesh[2],coronaMesh[3],coronaMesh[4],coronaMesh[5]);
                double opacity=(.15+.85*activity)*(.55+.45*b.visualEnergy)*(b.kind==GravityWellGame.BROWN_DWARF?.5:1);
                p.setStrokeWidth(Math.max(1,live*.07f));p.setColor(blue(b.kind)?0xff5dbbf4:0xffff8b27);p.setAlpha((int)(50*opacity));c.drawPath(loop,p);
                p.setStrokeWidth(Math.max(.7f,live*.015f));p.setColor(blue(b.kind)?0xffd8f7ff:0xffffe2a3);p.setAlpha((int)(220*opacity));c.drawPath(loop,p);
            }p.setStyle(Paint.Style.FILL);p.setAlpha(255);
        }c.restore();
    }
    private void drawBeams(Canvas c,GravityWellGame.Body b,float live){
        if(beam==null){int[] pixels=new int[128*256];for(int y=0;y<256;y++)for(int x=0;x<128;x++){double distance=(255-y)/255.0,width=4+distance*24,cross=Math.abs(x-64)/width;double alpha=Math.pow(Math.max(0,1-cross),2)*Math.sin(Math.PI*Math.min(.99,.08+distance*.9))*(1-distance);pixels[y*128+x]=Color.argb((int)(230*alpha),177,221,255);}beam=Bitmap.createBitmap(pixels,128,256,Bitmap.Config.ARGB_8888);}
        for(int pair=0;pair<(quality==0?1:3);pair++){
            c.save();c.rotate((float)(-25+pair*61+b.flightAge*(pair==0?37:pair==1?-23:17)));
            float width=live*(pair==0?.8f:pair==1?.59f:.45f)*(float)(.75+.45*b.visualEnergy);
            float length=live*(pair==0?5.5f:pair==1?4.8f:4.1f)*(float)(.8+.35*b.visualEnergy);
            double pulse=.85+.15*Math.sin(b.flightAge*(1.4+pair*.23)+pair*2.1),weight=pair==0?1:pair==1?.65:.45;
            p.setColor(Color.WHITE);p.setAlpha((int)((95+140*b.visualEnergy)*pulse*weight));
            for(int i=0;i<2;i++){rect.set(-width,-length,width,0);c.drawBitmap(beam,null,rect,p);c.rotate(180);}p.setAlpha(255);c.restore();
        }
        c.save();c.rotate((float)(-25+b.flightAge*37));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(.6f,live*.025f));p.setColor(0x709b8cfa);c.drawOval(-live*1.65f,-live*.5f,live*1.65f,live*.5f,p);p.setStyle(Paint.Style.FILL);c.restore();
    }

    private static double period(int kind){return kind==GravityWellGame.BROWN_DWARF?54:kind==GravityWellGame.RED_GIANT?82:kind==GravityWellGame.WHITE_DWARF?33:kind==GravityWellGame.NEUTRON?18:55;}
    public static double stellarPull(GravityWellGame.Body b){return GravityWellScene.fluidPull(b);}
    private static void mapFluid(GravityWellGame.Body b,float x,float y,float size,float[] out,int index){
        double distance=Math.max(1,Math.hypot(b.x,b.y)),dx=b.x/distance,dy=b.y/distance;
        GravityWellScene.fluidPoint(b,x*dx+y*dy,-x*dy+y*dx,out,index);
        double localX=out[index],localY=out[index+1];out[index]=(float)(localX*dx-localY*dy);out[index+1]=(float)(localX*dy+localY*dx);
    }
    private void stellarFrame(Canvas c,Bitmap texture,GravityWellGame.Body b,float size){
        double pull=stellarPull(b);if(pull<.01){c.drawBitmap(texture,null,rect,p);return;}
        for(int row=0;row<5;row++)for(int col=0;col<7;col++)mapFluid(b,(col/3f-1)*size,(row/2f-1)*size,size,pulledSurface,(row*7+col)*2);c.drawBitmapMesh(texture,6,4,pulledSurface,0,null,0,p);
    }
    public boolean drawPlanetSurface(Canvas c,GravityWellGame.Body b,float size){
        Bitmap[] frames=PlanetTextures.get(b.kind);if(frames==null)return false;
        double phase=(b.flightAge/(b.kind==GravityWellGame.GAS_GIANT?48:72)+.13)*PlanetTextures.FRAMES;int first=(int)phase%PlanetTextures.FRAMES;double q=phase-Math.floor(phase);
        boolean fluid=GravityWellScene.fluid(b);if(fluid&&b.captured)size*=(float)Math.cbrt(Math.max(0,b.mass/b.initialMass));
        rect.set(-size,-size,size,size);p.setColor(Color.WHITE);p.setAlpha((int)(255*b.visualOpacity));if(fluid)stellarFrame(c,frames[first],b,size);else c.drawBitmap(frames[first],null,rect,p);
        if(quality>0&&b.visualOpacity>=.6){p.setAlpha((int)(255*q*b.visualOpacity));if(fluid)stellarFrame(c,frames[(first+1)%PlanetTextures.FRAMES],b,size);else c.drawBitmap(frames[(first+1)%PlanetTextures.FRAMES],null,rect,p);}p.setAlpha(255);return true;
    }
    public void drawStellarSurface(Canvas c,GravityWellGame.Body b,float size){
        if(b.captured)size*=(float)Math.cbrt(Math.max(0,Math.min(1,b.mass/b.initialMass)));
        rect.set(-size,-size,size,size);p.setColor(Color.WHITE);
        StellarTextures.Frames frames=StellarTextures.get(b.kind);
        if(frames==null){c.drawBitmap(surface(b,0),null,rect,p);return;}
        double fade=1;
        if(waiting[b.kind]){if(readyAt[b.kind]==0)readyAt[b.kind]=android.os.SystemClock.uptimeMillis();fade=Math.min(1,(android.os.SystemClock.uptimeMillis()-readyAt[b.kind])/1000.0);if(fade<1)c.drawBitmap(photos[b.kind][0],null,rect,p);}
        double phase=(b.flightAge/period(b.kind)+.17)*StellarTextures.FRAMES;int first=(int)phase%StellarTextures.FRAMES;double blend=phase-Math.floor(phase);
        p.setAlpha((int)(255*fade));stellarFrame(c,frames.images[first],b,size);p.setAlpha((int)(255*fade*blend));stellarFrame(c,frames.images[(first+1)%StellarTextures.FRAMES],b,size);p.setAlpha(255);
        if(b.kind!=GravityWellGame.BROWN_DWARF&&b.kind!=GravityWellGame.NEUTRON){
            float live=size*110/128;p.setStyle(Paint.Style.STROKE);
            for(int i=0;i<3;i++){double longitude=i*2.1+.2-b.flightAge/period(b.kind)*Math.PI*2-.17*Math.PI*2,visible=Math.cos(longitude);if(visible<.2)continue;
                double latitude=(i-1)*.31,nx=Math.sin(longitude)*Math.cos(latitude),ny=Math.sin(latitude),activity=(.1+.9*Math.pow(.5+.5*Math.sin(b.flightAge*.43+i*2.5),4))*visible*(.55+.45*b.visualEnergy);
                float x=(float)(nx*live),y=(float)(ny*live);loop.reset();loop.moveTo(x-live*.13f,y);loop.cubicTo(x-live*.05f,y-live*.18f,x+live*.15f,y-live*.15f,x+live*.17f,y+live*.06f);
                p.setColor(blue(b.kind)?0xffa6e6ff:0xffffecac);p.setStrokeWidth(Math.max(1,live*.045f));p.setAlpha((int)(34*activity));c.drawPath(loop,p);p.setStrokeWidth(Math.max(.6f,live*.012f));p.setAlpha((int)(145*activity));c.drawPath(loop,p);
            }p.setStyle(Paint.Style.FILL);p.setAlpha(255);
        }
    }
}
