package org.openrow.probe;

import android.graphics.*;

/** One geometry build per frame, reused by both depth passes. Small animated material atlases. */
public final class TidalStreamRenderer {
    private static final int SEGMENTS=16,MAX_SOURCES=6,MAX_RIBBONS=24,GRAINS=20,FRAMES=16;
    private final Paint paint=new Paint(3);private final Path clip=new Path(),horizon=new Path();
    private final Bitmap[][] textures=new Bitmap[2][FRAMES];
    private final GravityWellGame.Body[] sources=new GravityWellGame.Body[MAX_SOURCES];
    private final Ribbon[] ribbons=new Ribbon[MAX_RIBBONS];private int used,quality=1;
    private final float[] point=new float[2];
    private static final class Ribbon {
        final float[] vertices=new float[(SEGMENTS+1)*6],x=new float[SEGMENTS+1],y=new float[SEGMENTS+1],grains=new float[GRAINS*2];
        final Path threads=new Path();int count,frame;boolean cool,hot;float alpha,blend;
    }
    public TidalStreamRenderer(){for(int i=0;i<ribbons.length;i++)ribbons[i]=new Ribbon();}
    public void quality(int level){quality=level;}
    private Bitmap texture(boolean cool,int frame){
        int index=cool?1:0;if(textures[index][frame]!=null)return textures[index][frame];
        for(int f=0;f<FRAMES;f++){int[] pixels=new int[128*32];double phase=f/(double)FRAMES*Math.PI*2;
            for(int y=0;y<32;y++)for(int x=0;x<128;x++){double cross=(y-15.5)/15.5,soft=Math.pow(Math.max(0,1-cross*cross),2.2),a=x/128.0*Math.PI*2-phase;
                double warp=Math.sin(a+y*.12)*1.7+Math.sin(a*3-y*.19)*.6,grain=.50+.19*Math.sin(a*4+y*.65+warp)+.14*Math.sin(a*7-y*.45)+.07*Math.sin(a*11+y*.21);
                double heat=x/127.0,fade=Math.min(1,(128-x)/10.0),core=Math.exp(-cross*cross*15)*(.3+.2*Math.sin(a*5+warp));
                pixels[y*128+x]=Color.argb((int)(Math.min(1,soft*(grain+core))*220*fade),cool?(int)(116+123*heat):255,cool?(int)(181+66*heat):(int)(145+101*heat),cool?255:(int)(69+154*heat));
            }textures[index][f]=Bitmap.createBitmap(pixels,128,32,Bitmap.Config.ARGB_8888);
        }return textures[index][frame];
    }
    private static double priority(GravityWellGame.Body b){return b.mass+(b.bossId>0?10000:b.kind==GravityWellGame.COMET?1000:0);}
    private void prepare(GravityWellGame game,GravityWellScene scene){
        used=0;for(int i=0;i<sources.length;i++)sources[i]=null;int limit=quality==0?3:quality==1?4:6;
        for(GravityWellGame.Body raw:game.bodies){GravityWellGame.Body b=scene.body(raw);if(b==null||b.dust||b.trailAge<=0||!b.captured&&b.kind!=GravityWellGame.COMET||b.mass<.5)continue;
            for(int i=0;i<limit;i++)if(sources[i]==null||priority(b)>priority(sources[i])){for(int j=limit-1;j>i;j--)sources[j]=sources[j-1];sources[i]=b;break;}}
        for(int slot=0;slot<limit;slot++){GravityWellGame.Body b=sources[slot];if(b==null)continue;boolean featured=b.bossId>0&&b.bossId==game.activeBossId;
            double radius=GravityWellGame.visualRadius(b),strength=(.4+.6*scene.energy)*Math.min(1,b.trailAge/.65)*Math.min(1,b.mass/4);
            if(!featured)strength*=1-scene.focus*(slot>1?1:.7);if(strength<.025)continue;
            int branches=featured?Math.max(1,Math.min(4,b.visualStreams)):1;double effective=GravityWellScene.streamCount(b),phase=b.trailAge*2+b.initialMass;
            boolean fluid=GravityWellScene.fluid(b);double liveRadius=radius*(fluid&&b.captured?Math.cbrt(Math.max(0,b.mass/b.initialMass)):1);
            for(int branch=0;branch<branches&&used<ribbons.length;branch++){double weight=GravityWellScene.streamWeight(b,branch);if(weight<.005)continue;Ribbon r=ribbons[used++];
                r.cool=b.material==1||b.material==3;r.hot=fluid&&featured;r.alpha=(float)(strength*weight/Math.sqrt(effective));double animation=b.trailAge*8;r.frame=(int)animation%FRAMES;r.blend=fluid&&featured&&branch==0?(float)(animation-Math.floor(animation)):0;r.count=0;r.threads.reset();
                GravityWellScene.flowPoint(b,branch,branches,0,phase,point);float sx=point[0],sy=point[1];GravityWellScene.flowPoint(b,branch,branches,1,phase,point);
                double length=Math.max(1,Math.hypot(point[0]-sx,point[1]-sy)*1.2),reach=GravityWellScene.trailReach(b,length);
                for(int i=0;i<=SEGMENTS;i++){GravityWellScene.flowPoint(b,branch,branches,reach*i/SEGMENTS,phase,point);r.x[i]=point[0];r.y[i]=point[1];}
                for(int i=0;i<=SEGMENTS;i++){int lo=Math.max(0,i-1),hi=Math.min(SEGMENTS,i+1);double dx=r.x[hi]-r.x[lo],dy=r.y[hi]-r.y[lo],norm=Math.max(1,Math.hypot(dx,dy)),t=reach*i/SEGMENTS;
                    double width=(liveRadius*(fluid?.2:.13)*(1-t)+(fluid?9:5)*t+Math.sin(Math.PI*t)*liveRadius*.08)/Math.sqrt(effective);
                    width*=1+.08*Math.sin(t*16-phase);for(int row=0;row<3;row++){int v=(row*(SEGMENTS+1)+i)*2;r.vertices[v]=r.x[i]+(float)(-dy/norm*width*(row-1));r.vertices[v+1]=r.y[i]+(float)(dx/norm*width*(row-1));}
                    if(r.hot&&branch==0){float offset=(float)(Math.sin(t*17-phase)*width*.22);float x=r.x[i]+(float)(-dy/norm*offset),y=r.y[i]+(float)(dx/norm*offset);if(i==0)r.threads.moveTo(x,y);else r.threads.lineTo(x,y);}}
                int grains=featured?(quality==0?12:20):(quality==0?5:9);
                for(int i=0;i<grains;i++){double u=GravityWellScene.grainPosition(b,i*GRAINS/grains,branch,length);if(u<=0||u>=1||u>reach)continue;double pos=u/Math.max(.001,reach)*SEGMENTS;int a=Math.min(SEGMENTS-1,(int)pos);float q=(float)(pos-a),spread=(float)(Math.sin(i*2.4+phase)*1.2*Math.sin(u*Math.PI));
                    float x=r.x[a]*(1-q)+r.x[a+1]*q+spread,y=r.y[a]*(1-q)+r.y[a+1]*q-spread*.5f;if(x*x+y*y<52*52)continue;r.grains[r.count++]=x;r.grains[r.count++]=y;}
            }
        }
    }
    public void draw(Canvas c,GravityWellGame game,GravityWellScene scene,float cx,float cy,boolean front){
        if(!front)prepare(game,scene);c.save();c.translate(cx,cy);clip.reset();clip.moveTo(-510,108.4f);clip.lineTo(510,-108.4f);clip.lineTo(510,front?245:-245);clip.lineTo(-510,front?245:-245);clip.close();c.clipPath(clip);
        horizon.reset();horizon.addCircle(0,0,50,Path.Direction.CW);c.clipOutPath(horizon);
        for(int i=0;i<used;i++){Ribbon r=ribbons[i];paint.setStyle(Paint.Style.FILL);paint.setColor(Color.WHITE);paint.setAlpha((int)(220*r.alpha*(1-r.blend)));c.drawBitmapMesh(texture(r.cool,r.frame),SEGMENTS,2,r.vertices,0,null,0,paint);
            if(r.blend>0){paint.setAlpha((int)(220*r.alpha*r.blend));c.drawBitmapMesh(texture(r.cool,(r.frame+1)%FRAMES),SEGMENTS,2,r.vertices,0,null,0,paint);}
            if(r.hot){paint.setStyle(Paint.Style.STROKE);paint.setColor(r.cool?0xffdcf6ff:0xffffefcc);paint.setAlpha((int)(165*r.alpha));paint.setStrokeWidth(.8f);c.drawPath(r.threads,paint);paint.setStyle(Paint.Style.FILL);}
            paint.setColor(r.cool?0xffc8f0ff:0xffffe5b6);paint.setAlpha((int)(170*r.alpha));paint.setStrokeWidth(1.3f);c.drawPoints(r.grains,0,r.count,paint);
        }paint.setAlpha(255);c.restore();
    }
}
