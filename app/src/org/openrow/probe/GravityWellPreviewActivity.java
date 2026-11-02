package org.openrow.probe;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.graphics.*;
import android.view.*;

/** Standalone renderer sample. Never opens a career or athlete file. */
public final class GravityWellPreviewActivity extends Activity {
    private Preview preview;
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().getDecorView().setSystemUiVisibility(5894);showPreview();}
    @Override public void onNewIntent(android.content.Intent intent){super.onNewIntent(intent);setIntent(intent);showPreview();}
    private void showPreview(){if(preview!=null)preview.active=false;preview=null;if(getIntent().getBooleanExtra("audit",false)){setContentView(new GravityWellAuditView(this,getIntent().getIntExtra("page",0),getIntent().getFloatExtra("remaining",1),getIntent().getFloatExtra("age",-1),getIntent().getBooleanExtra("bosses",false)?GravityWellGame.BROWN_DWARF:-1));}else{preview=new Preview();setContentView(preview);}}
    @Override public void onPause(){super.onPause();if(preview!=null)preview.active=false;}
    @Override public void onResume(){super.onResume();if(preview!=null){preview.active=true;preview.last=0;preview.invalidate();}}
    private final class Preview extends View {
        final CelestialRenderer celestial=new CelestialRenderer();
        final SingularityRenderer hole=new SingularityRenderer();
        final TidalStreamRenderer streams=new TidalStreamRenderer();
        final GravityWellScene scene=new GravityWellScene();
        final RenderLoop renderLoop=new RenderLoop(this,"visual-lab");
        final Paint p=new Paint(3);
        GravityWellGame game;long last;boolean active=true;
        final boolean trainerDemo=getIntent().getBooleanExtra("trainer",false);
        final boolean benchmark=getIntent().getBooleanExtra("benchmark",false);
        int athlete=getIntent().getIntExtra("athlete",0);
        TrainingCore.Runner demoRunner;TrainingVisuals.Profile demoProfile;double demoClock,referenceSpeed;
        double effort=Math.max(0,Math.min(1,getIntent().getFloatExtra("effort",.5f)));
        int approach=getIntent().getIntExtra("approach",0);
        int kind=getIntent().getIntExtra("kind",GravityWellGame.SUN);
        Preview(){super(GravityWellPreviewActivity.this);kind=Math.max(0,Math.min(GravityWellGame.KINDS.length-1,kind));StellarTextures.request(kind);celestial.prepareBosses();reset();}
        void reset(){if(trainerDemo)kind=GravityWellGame.NEUTRON;StellarTextures.request(kind);game=new GravityWellGame();game.start(0);game.bodies.clear();game.spawnKind(kind,GravityWellGame.KINDS[kind].boss);game.spawnDust=-1000;game.spawnRock=-1000;game.ranks[7]=3;game.ranks[6]=Math.max(0,Math.min(3,getIntent().getIntExtra("fracture",0)));
            GravityWellGame.Body b=game.bodies.get(0);b.x=kind==GravityWellGame.COMET?390:255;b.y=15;b.vx=-35;b.vy=55;
            if(approach>0){double limit=GravityWellGame.KINDS[kind].boss?260:172,side=approach==1?-1:1;b.x=side*limit*.4;b.y=side*limit*.96;b.vx=-side*55;b.vy=-side*35;}
            else if(kind!=GravityWellGame.COMET){b.mass*=Math.max(.02,Math.min(1,getIntent().getFloatExtra("remaining",1)));b.captured=true;b.orbitRadius=255;b.orbitPhase=.06;}if(getIntent().getBooleanExtra("crowd",false))for(int i=0;i<28;i++){int type=i%5==0?GravityWellGame.GAS_GIANT:i%7;game.spawnKind(type,false);GravityWellGame.Body other=game.bodies.get(game.bodies.size()-1);double a=i*2.39996,r=160+i%4*45;other.x=Math.cos(a)*r;other.y=Math.sin(a)*r*.58;other.captured=true;other.orbitRadius=r;other.orbitPhase=a;other.captureAge=3;other.mass*=.5;}
            if(trainerDemo)prepareTrainingDemo();last=0;invalidate();}
        // In-memory fixtures only. This lab never writes training, career or athlete history.
        void advanceBenchmark(double dt){
            game.field=effort;game.visualPhase+=dt*(.035+.065*effort);
            for(GravityWellGame.Body b:game.bodies){b.flightAge+=dt;b.captureAge=8;b.trailAge+=dt;b.angle+=dt*.22;b.orbitPhase+=dt*.18;b.x=Math.cos(b.orbitPhase)*b.orbitRadius;b.y=Math.sin(b.orbitPhase)*b.orbitRadius*.65;}
        }
        void prepareTrainingDemo(){
            referenceSpeed=athlete%3==0?1.5:athlete%3==1?3:0;java.util.List<TrainingCore.Record> records=new java.util.ArrayList<>();
            long now=System.currentTimeMillis();
            TrainingCore.Plan referencePlan=new TrainingCore.Plan("visual-demo-reference","In-memory test fixture",TrainingCore.Kind.STARTER,TrainingCore.Category.ENDURANCE,TrainingCore.Goal.ENDURANCE,java.util.Arrays.asList(new TrainingCore.Block(TrainingCore.Phase.WORK,TrainingCore.Category.ENDURANCE,180,3)),true);
            if(referenceSpeed>0)for(int i=0;i<3;i++){TrainingCore.Runner r=new TrainingCore.Runner("fixture"+i,referencePlan,now-(i+1)*TrainingCore.DAY,false);r.resume(0);for(int second=1;second<=180;second++){r.observeDistance(referenceSpeed);r.tick(second*1000L,true,true);}records.add(new TrainingCore.Record(r,r.started+180000,3,1));}
            demoProfile=TrainingVisuals.profile(records,now,"");
            TrainingCore.Plan plan=new TrainingCore.Plan("visual-demo-shortened","Shortened software demo, not a workout",TrainingCore.Kind.TRAINING,TrainingCore.Category.ENDURANCE,TrainingCore.Goal.ENDURANCE,java.util.Arrays.asList(
                new TrainingCore.Block(TrainingCore.Phase.WARM_UP,TrainingCore.Category.ENDURANCE,8,2),new TrainingCore.Block(TrainingCore.Phase.WORK,TrainingCore.Category.ENDURANCE,12,3),new TrainingCore.Block(TrainingCore.Phase.RECOVERY,TrainingCore.Category.ENDURANCE,8,1),new TrainingCore.Block(TrainingCore.Phase.WORK,TrainingCore.Category.ENDURANCE,12,3),new TrainingCore.Block(TrainingCore.Phase.COOL_DOWN,TrainingCore.Category.ENDURANCE,8,1)),true);
            demoRunner=new TrainingCore.Runner("visual-demo",plan,now,true);demoRunner.resume(0);demoClock=0;
        }
        void advanceTrainingDemo(double dt){
            GameWorkoutAdapter.Frame frame=new GameWorkoutAdapter.Frame(demoRunner,demoProfile);boolean moving=frame.phase!=TrainingCore.Phase.RECOVERY&&!frame.finished;
            double speed=(referenceSpeed>0?referenceSpeed:2)*(frame.phase==TrainingCore.Phase.WORK?1:.6);
            if(moving){demoRunner.observeDistance(speed*dt);game.input(speed*dt,0,speed*dt*20);}demoClock+=dt;demoRunner.tick(Math.round(demoClock*1000),moving,true);
            game.advance(dt,speed/4,true,moving,TrainingVisuals.input(frame,speed,moving));if(demoRunner.finished)game.finish();
        }
        void text(Canvas c,String s,float x,float y,float size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));c.drawText(s,x,y,p);}
        void button(Canvas c,String s,float x,float w){p.setColor(0xff18324a);c.drawRoundRect(x,625,x+w,675,12,12,p);text(c,s,x+18,657,18,0xffdcecf9);}
        @Override protected void onDraw(Canvas c){renderLoop.begin();int detail=renderLoop.performance.quality;celestial.quality(detail);streams.quality(detail);hole.quality(detail);long now=SystemClock.elapsedRealtime();double dt=last==0?0:Math.min(.1,(now-last)/1000.0);last=now;boolean ready=!GravityWellGame.KINDS[kind].stellar||StellarTextures.get(kind)!=null;if(active&&ready){if(trainerDemo)advanceTrainingDemo(dt);else if(benchmark)advanceBenchmark(dt);else game.advance(dt,effort,true,effort>0);}scene.prepare(game);
            c.save();c.scale(getWidth()/1280f,getHeight()/720f);celestial.background(c);
            text(c,trainerDemo?"GRAVITY WELL / TRAINER VISUAL DEMO":"GRAVITY WELL  /  VISUAL LAB",45,55,26,0xffeaf4ff);text(c,GravityWellGame.KINDS[kind].name+(ready?"  /  simulated rowing "+Math.round(effort*100)+"%":"  /  preparing spherical surface..."),45,86,18,0xff9db7ca);
            if(trainerDemo){p.setColor(0xff050b17);c.drawRect(30,65,1250,103,p);GameWorkoutAdapter.Frame f=new GameWorkoutAdapter.Frame(demoRunner,demoProfile);String name=referenceSpeed>0?"Athlete reference "+referenceSpeed+" m/s":"No recent reference (provisional)";text(c,name+" / "+f.phase+" / effort "+f.effort+" of 10",45,88,18,0xff9db7ca);}
            c.save();c.clipRect(25,105,1255,605);float cx=600,cy=355;
            for(GravityWellGame.Body raw:game.bodies){GravityWellGame.Body b=scene.body(raw);if(b!=null&&!b.dust&&!GravityWellScene.front(b.x,b.y))celestial.body(c,b,cx,cy);}
            streams.draw(c,game,scene,cx,cy,false);celestial.debris(c,game,cx,cy,false);hole.draw(c,cx,cy,43,game.visualPhase,game.field,false,game.flash);celestial.debris(c,game,cx,cy,true);streams.draw(c,game,scene,cx,cy,true);
            for(GravityWellGame.Body raw:game.bodies){GravityWellGame.Body b=scene.body(raw);if(b!=null&&!b.dust&&GravityWellScene.front(b.x,b.y))celestial.body(c,b,cx,cy);}c.restore();
            button(c,"Back",45,130);button(c,"Replay",190,140);button(c,"Previous",345,150);button(c,"Next boss",510,165);button(c,"Comet",690,150);button(c,"Input",850,85);button(c,approach==0?"Capture: rear":approach==1?"Capture: front":"Orbit preview",950,275);
            text(c,benchmark?"Fixed-load benchmark · "+game.bodies.size()+" bodies · no consumption or saved progress":trainerDemo?"Shortened in-memory demo / Input changes athlete / no saved workout or athlete data":"Isolated sample — no career progress or workout credit",45,705,15,0xff859bb1);c.restore();renderLoop.end(active);}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX()*1280/getWidth(),y=e.getY()*720/getHeight();if(y<625||y>675)return true;if(x<175){finish();return true;}if(x>=850&&x<935&&trainerDemo){athlete=(athlete+1)%3;reset();return true;}if(x>=850&&x<935){effort=effort<.1?.15:effort<.3?.5:effort<.8?1:0;invalidate();return true;}if(x>=950){approach=(approach+1)%3;reset();return true;}if(x>=345&&x<495)kind=kind<=GravityWellGame.BROWN_DWARF?GravityWellGame.NEUTRON:kind-1;else if(x>=510&&x<675)kind=kind>=GravityWellGame.NEUTRON||kind<GravityWellGame.BROWN_DWARF?GravityWellGame.BROWN_DWARF:kind+1;else if(x>=690&&x<840)kind=GravityWellGame.COMET;reset();return true;}
    }
}
