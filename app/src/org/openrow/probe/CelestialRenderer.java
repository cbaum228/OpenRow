package org.openrow.probe;

import android.graphics.*;

/** Cached procedural surfaces and nebula. No bitmap/shader allocation during animation. */
public final class CelestialRenderer {
    public void prepareBosses(){bosses.prepare();}
    private final BossRenderer bosses=new BossRenderer();
    private final CometTailRenderer cometTails=new CometTailRenderer();
    private final Paint p=new Paint(3);
    private final MantleFragmentRenderer mantle=new MantleFragmentRenderer();
    private final FractureRenderer fractures=new FractureRenderer();
    private int quality=1;
    public void quality(int level){quality=level;bosses.quality(level);}
    private final RectF rect=new RectF();
    private final Path erosionMask=new Path();
    private final Path ringFlow=new Path(),ringHorizon=new Path();
    private final float[] ringPoint=new float[2],pit=new float[3];
    private final float[] stellarMesh=new float[18];
    private final Bitmap[] surfaces=new Bitmap[4];
    private final Bitmap[] classSurfaces=new Bitmap[GravityWellGame.KINDS.length];
    private final float[] points=new float[480];
    private Bitmap warmGlow,coolGlow;
    private Bitmap sphereLight;
    private final Bitmap[] ringLayers=new Bitmap[2];
    private Bitmap background;
    public void background(Canvas c){
        mantle.beginFrame();fractures.beginFrame();
        if(background==null){background=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);Canvas b=new Canvas(background);b.drawColor(0xff050b17);
            for(int i=0;i<15;i++){float x=330+i*65,y=310+(float)Math.sin(i*.73)*125,r=150+i%4*50;
                p.setShader(new RadialGradient(x,y,r,new int[]{i%3==0?0x153b7299:0x102a466e,0x00122349},null,Shader.TileMode.CLAMP));b.drawCircle(x,y,r,p);}
            p.setShader(null);for(int i=0;i<280;i++){float x=(i*643%1279),y=(i*i*31%719);p.setColor(i%17==0?0xffa6c1dc:i%5==0?0xff576b8b:0xff243349);b.drawCircle(x,y,i%17==0?1.2f:.55f,p);if(i%47==0){p.setColor(0x405a93be);b.drawRect(x-4,y-.3f,x+4,y+.3f,p);b.drawRect(x-.3f,y-4,x+.3f,y+4,p);}}
        }
        p.setShader(null);p.setColor(Color.WHITE);c.drawBitmap(background,0,0,p);
    }
    private Bitmap surface(int type){
        if(surfaces[type]!=null)return surfaces[type];
        Bitmap bitmap=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bitmap);c.save();
        Path silhouette=new Path();
        if(type==0){for(int i=0;i<17;i++){double a=i*Math.PI*2/17;float r=52+(float)Math.sin(i*7.1)*5;float x=64+(float)Math.cos(a)*r,y=64+(float)Math.sin(a)*r;if(i==0)silhouette.moveTo(x,y);else silhouette.lineTo(x,y);}silhouette.close();}
        else silhouette.addCircle(64,64,55,Path.Direction.CW);
        c.clipPath(silhouette);
        int[] base={0xff81776e,0xff7cabbc,0xffb78364,0xff236889};p.setColor(base[type]);c.drawPaint(p);
        if(type==2){for(int i=0;i<20;i++){float y=9+i*6;p.setColor(i%4==0?0xffe0b58b:i%3==0?0xff885b53:0xffbd9274);c.drawOval(-60,y-4,184,y+8,p);}p.setColor(0xffa8715b);c.drawOval(60,65,99,82,p);p.setColor(0xffd5ad88);c.drawOval(64,69,92,78,p);}
        else if(type==3){for(int i=0;i<16;i++){float x=10+(i*37%100),y=10+(i*53%105);p.setColor(i%2==0?0xff508f80:0xff2d776e);c.drawOval(x-9,y-5,x+14,y+6,p);c.drawOval(x-3,y-8,x+7,y+12,p);}for(int i=0;i<8;i++){p.setColor(0x708bc7c9);float y=22+i*12;c.drawOval(-15+i*6,y,110+i*3,y+4,p);}}
        else {for(int i=0;i<37;i++){float x=8+i*37%113,y=8+i*61%111,r=2+i%6;p.setColor(type==0?0xff554e4b:0xff527c91);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.8f);p.setColor(type==0?0xffac9b87:0xffacdae0);c.drawArc(x-r,y-r,x+r,y+r,25,145,false,p);p.setStyle(Paint.Style.FILL);}}
        // Surface detail rotates independently from the light facing the accretion disk.
        if(type==0){p.setShader(new RadialGradient(38,31,100,new int[]{0x00ffffff,0x15051422,0xf0020713},new float[]{0,.5f,1},Shader.TileMode.CLAMP));c.drawPaint(p);p.setShader(null);}
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(type==0?1:2);p.setColor(type==3?0x904fcfe1:0x706e95b4);c.drawCircle(64,64,54,p);p.setStyle(Paint.Style.FILL);c.restore();
        surfaces[type]=bitmap;return bitmap;
    }
    private Bitmap sphereLight(){
        if(sphereLight==null){sphereLight=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sphereLight);
            p.setColor(Color.WHITE);p.setShader(new RadialGradient(20,64,102,new int[]{0x28ffe4b3,0x00000000,0x9c020611,0xf9020611},new float[]{0,.28f,.72f,1},Shader.TileMode.CLAMP));
            c.drawCircle(64,64,55,p);p.setShader(null);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setColor(0xa0ffe5ba);c.drawArc(9,9,119,119,135,90,false,p);p.setStyle(Paint.Style.FILL);
        }return sphereLight;
    }
    private Bitmap classSurface(GravityWellGame.Body body){
        if(body.kind<0)return surface(body.material);
        GravityWellGame.Kind kind=GravityWellGame.kind(body);
        if(!kind.stellar&&body.kind!=GravityWellGame.CLOUD&&body.kind!=GravityWellGame.METAL&&body.kind!=GravityWellGame.ICE_GIANT)return surface(body.material);
        if(classSurfaces[body.kind]!=null)return classSurfaces[body.kind];
        Bitmap bitmap=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
        if(body.kind==GravityWellGame.METAL||body.kind==GravityWellGame.ICE_GIANT){Bitmap base=surface(body.kind==GravityWellGame.METAL?0:2);Canvas c=new Canvas(bitmap);p.setColor(Color.WHITE);
            p.setColorFilter(new LightingColorFilter(body.kind==GravityWellGame.METAL?0xffa7b6c5:0xff659ddc,0x00101720));c.drawBitmap(base,0,0,p);p.setColorFilter(null);
        }else{
            int[] pixels=new int[128*128];int tint=body.kind==GravityWellGame.BROWN_DWARF?0xffb96b42:body.kind==GravityWellGame.RED_DWARF||body.kind==GravityWellGame.RED_GIANT?0xffff7540:body.kind==GravityWellGame.SUN?0xffffd382:0xffc5e8ff;
            for(int y=0;y<128;y++)for(int x=0;x<128;x++){
                double dx=(x-64)/55.0,dy=(y-64)/55.0,r2=dx*dx+dy*dy;if(r2>=1)continue;
                long grain=((x*73856093L)^(y*19349663L))&1023;
                double mottled=.8+.05*Math.sin(x*.27+Math.sin(y*.13)*3)+.04*Math.sin(y*.31+x*.17)+.1*(grain/1023.0-.5);
                if(body.kind==GravityWellGame.CLOUD){double opacity=Math.pow(1-r2,1.8)*mottled*(.65+.35*Math.sin(x*.09+y*.13));pixels[y*128+x]=Color.argb((int)(150*opacity),120,174,209);}
                else{double light=(.64+.36*Math.sqrt(1-r2))*mottled;pixels[y*128+x]=Color.argb((int)(255*Math.min(1,(1-r2)*35)),(int)(Color.red(tint)*light),(int)(Color.green(tint)*light),(int)(Color.blue(tint)*light));}
            }
            bitmap.setPixels(pixels,0,128,0,0,128,128);
        }
        classSurfaces[body.kind]=bitmap;return bitmap;
    }
    private void rings(Canvas c,float x,float y,float size,boolean front,float opacity){
        if(opacity<=0)return;
        int index=front?1:0;
        if(ringLayers[index]==null){Bitmap layer=Bitmap.createBitmap(256,128,Bitmap.Config.ARGB_8888);Canvas ring=new Canvas(layer);ring.rotate(-20,128,64);p.setStyle(Paint.Style.STROKE);
            for(int i=0;i<7;i++){float r=58*(1.4f+i*.09f);rect.set(128-r,64-r*.29f,128+r,64+r*.29f);
                p.setStrokeWidth(58*.07f);p.setColor(i==3?0x207a878e:Color.argb(front?115:65,190+i*7,168+i*6,139+i*7));ring.drawArc(rect,front?0:180,180,false,p);}
            p.setStyle(Paint.Style.FILL);ringLayers[index]=layer;
        }
        rect.set(x-size*128/58,y-size*64/58,x+size*128/58,y+size*64/58);p.setColor(Color.WHITE);p.setAlpha((int)(255*opacity));c.drawBitmap(ringLayers[index],null,rect,p);p.setAlpha(255);
    }
    public void body(Canvas c,GravityWellGame.Body b,float cx,float cy){
        if(b.dust)return;
        float x=cx+(float)b.x,y=cy+(float)b.y;
        float radius=(float)GravityWellGame.visualRadius(b),size=radius*64/55;
        float facing=(float)Math.toDegrees(Math.atan2(b.y,b.x));
        GravityWellGame.Kind kind=GravityWellGame.kind(b);
        boolean gaseous=b.kind>=0&&(kind.stellar||b.kind==GravityWellGame.CLOUD),fluid=GravityWellScene.fluid(b);
        if(b.kind==GravityWellGame.COMET)cometTails.draw(c,b,cx,cy);
        boolean hero=b.kind>=0&&kind.boss;
        if(hero&&b.fragmentDepth==0)bosses.drawHalo(c,b,cx,cy,radius);
        Bitmap texture=hero?bosses.surface(b,0):classSurface(b),light=b.material==0||gaseous?null:sphereLight();
        c.save();c.translate(x,y);c.rotate(facing);
        float stretch=(float)GravityWellGame.tidalStretch(b);c.scale(stretch,1/(float)Math.sqrt(stretch));
        if(GravityWellGame.hasRings(b))strippingRings(c,b,size,facing,false);
        mantle.draw(c,b,texture,radius,false);
        c.save();
        float shadeScale=1;
        if(b.captured&&b.kind!=GravityWellGame.CLOUD){
            shadeScale=0;
            erosionMask.reset();
            erosionMask.setFillType(Path.FillType.EVEN_ODD);
            int contourPoints=quality==0?40:64;
            for(int i=0;i<contourPoints;i++){double a=i*Math.PI*2/contourPoints,contour=fluid?Math.cbrt(Math.max(0,b.mass/b.initialMass)):GravityWellGame.erosionRadius(b,a),r=radius*contour;shadeScale=Math.max(shadeScale,(float)contour);float px=(float)(Math.cos(a)*r),py=(float)(Math.sin(a)*r);if(fluid){GravityWellScene.fluidPoint(b,px,py,pit,0);px=pit[0];py=pit[1];}else if(px<0)px*=GravityWellGame.inwardStretch(b);
                if(i==0)erosionMask.moveTo(px,py);else erosionMask.lineTo(px,py);}
            erosionMask.close();
            for(int scar=0;scar<3;scar++){
                GravityWellGame.surfacePit(b,scar,pit);if(pit[2]<=.002)continue;
                for(int i=0;i<12;i++){double a=i*Math.PI*2/12,r=radius*pit[2]*(1+.15*Math.sin(i*2.4+scar));
                    float px=pit[0]*radius+(float)(Math.cos(a)*r),py=pit[1]*radius+(float)(Math.sin(a)*r);
                    if(i==0)erosionMask.moveTo(px,py);else erosionMask.lineTo(px,py);}
                erosionMask.close();
            }
            c.clipPath(erosionMask);
        }
        p.setColor(Color.argb((int)(255*b.visualOpacity),255,255,255));p.setFilterBitmap(true);
        if(hero&&kind.stellar){c.save();c.rotate(-facing);bosses.drawStellarSurface(c,b,size);c.restore();}
        else if(b.fragmentDepth>0&&b.kind!=GravityWellGame.GAS_GIANT&&b.kind!=GravityWellGame.ICE_GIANT){mantle.body(c,b,texture,size);}
        else if(hero&&PlanetTextures.get(b.kind)!=null){c.save();c.rotate(-facing-(fluid?0:12));bosses.drawPlanetSurface(c,b,size);c.restore();}
        else if(b.visualStreams>1){
            float nose=(float)GravityWellGame.inwardStretch(b);
            for(int row=0;row<3;row++)for(int col=0;col<3;col++){int i=(row*3+col)*2;float px=(col-1)*size;stellarMesh[i]=px<0?px*nose:px;stellarMesh[i+1]=(row-1)*size;}
            c.drawBitmapMesh(texture,2,2,stellarMesh,0,null,0,p);
        }else{c.save();c.rotate((float)(b.angle*28)-facing);float textureSize=b.kind==GravityWellGame.CLOUD&&b.captured?size*(float)Math.cbrt(Math.max(0,b.mass/b.initialMass)):size;rect.set(-textureSize,-textureSize,textureSize,textureSize);c.drawBitmap(texture,null,rect,p);c.restore();}
        if(light!=null&&b.fragmentDepth==0&&!fluid){float litSize=size*shadeScale;rect.set(-litSize,-litSize,litSize,litSize);p.setColor(Color.argb((int)(255*b.visualOpacity),255,255,255));c.drawBitmap(light,null,rect,p);}
        if(b.fragmentDepth==0)fractures.draw(c,b,radius);
        c.restore();
        mantle.draw(c,b,texture,radius,true);
        if(GravityWellGame.hasRings(b))strippingRings(c,b,size,facing,true);
        c.restore();
        if(!hero&&b.kind!=GravityWellGame.CLOUD&&(b.captured||b.kind>=0&&kind.stellar)){boolean cool=b.material==1||b.material==3;Bitmap glow=cool?coolGlow:warmGlow;if(glow==null){glow=Bitmap.createBitmap(96,96,Bitmap.Config.ARGB_8888);Canvas g=new Canvas(glow);p.setAlpha(255);p.setShader(new RadialGradient(48,48,48,new int[]{cool?0x45419abc:0x45ed9d58,0x00000000},null,Shader.TileMode.CLAMP));g.drawCircle(48,48,48,p);p.setShader(null);if(cool)coolGlow=glow;else warmGlow=glow;}float glowSize=size*shadeScale;rect.set(x-glowSize*2,y-glowSize*2,x+glowSize*2,y+glowSize*2);p.setColor(Color.argb((int)(255*b.visualOpacity),255,255,255));p.setAlpha((int)(255*b.visualOpacity*Math.min(1,b.mass/2)));c.drawBitmap(glow,null,rect,p);p.setAlpha(255);}
    }
    private void strippingRings(Canvas c,GravityWellGame.Body b,float size,float facing,boolean front){
        double remaining=GravityWellGame.ringRemaining(b);if(remaining<=0)return;
        if(!b.captured){c.save();c.rotate(-facing);rings(c,0,0,size,front,1);c.restore();return;}
        c.save();double stretch=GravityWellGame.tidalStretch(b),distance=Math.hypot(b.x,b.y),q=1-remaining;
        float hx=(float)(-distance/stretch),rx=(float)(50/stretch),ry=(float)(50*Math.sqrt(stretch));
        ringHorizon.reset();ringHorizon.addOval(hx-rx,-ry,hx+rx,ry,Path.Direction.CW);c.clipOutPath(ringHorizon);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(.65f,size*.035f));
        double angle=Math.atan2(b.y,b.x);
        for(int lane=0;lane<7;lane++){
            ringFlow.reset();boolean open=false;
            for(int i=0;i<=48;i++){
                double a=i*Math.PI*2/48;
                GravityWellGame.ringPoint(b,lane/6.0,a,ringPoint,0);
                double viewY=Math.sin(angle)*ringPoint[0]*stretch+Math.cos(angle)*ringPoint[1]/Math.sqrt(stretch);
                boolean gap=q>.18&&Math.sin(a*13+lane*4+b.initialMass)>1-q*.9;
                if((viewY>=0)!=front||gap){open=false;continue;}
                if(open)ringFlow.lineTo(ringPoint[0],ringPoint[1]);else{ringFlow.moveTo(ringPoint[0],ringPoint[1]);open=true;}
            }
            p.setColor(Color.argb((int)(125*remaining*b.visualOpacity),220+lane*4,183+lane*6,132+lane*7));c.drawPath(ringFlow,p);
        }
        p.setStyle(Paint.Style.FILL);c.restore();
    }
    public void debris(Canvas c,GravityWellGame game,float cx,float cy,boolean front){
        p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.ROUND);
        for(int color=0;color<2;color++)for(int fade=0;fade<3;fade++){int n=0;
            for(GravityWellGame.Particle s:game.particles){boolean cool=s.material==1||s.material==3;if(GravityWellScene.front(s.x,s.y)!=front||(cool?1:0)!=color||s.x*s.x+s.y*s.y<55*55||Math.min(2,(int)(s.life/.2))!=fade)continue;points[n++]=cx+(float)s.x;points[n++]=cy+(float)s.y;}
            if(n==0)continue;boolean cool=color==1;int alpha=45+fade*65;
            p.setStrokeWidth(5);p.setColor(Color.argb(alpha/8,cool?100:255,cool?201:174,cool?255:108));c.drawPoints(points,0,n,p);
            p.setStrokeWidth(1.4f);p.setColor(Color.argb(alpha,cool?175:255,cool?228:207,cool?255:157));c.drawPoints(points,0,n,p);}
        p.setStrokeCap(Paint.Cap.BUTT);
    }
}
