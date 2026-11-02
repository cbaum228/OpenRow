package org.openrow.probe;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.util.AtomicFile;
import android.view.*;
import java.io.*;
import java.util.*;

/** Native tablet vertical slice. Test power never contributes to the athlete ledger. */
public final class GravityWellView extends View {
    public interface Controls {void back();void connect();void openWorkout();}
    private final Controls controls;
    private final Paint paint=new Paint(3);
    private final SingularityRenderer singularity=new SingularityRenderer();
    private final CelestialRenderer celestial=new CelestialRenderer();
    private final TidalStreamRenderer tidalStreams=new TidalStreamRenderer();
    private final RenderLoop renderLoop=new RenderLoop(this,"gameplay");
    private final GravityWellScene scene=new GravityWellScene();
    private final float[] dustPoints=new float[300];
    private GravityWellGame game=new GravityWellGame();
    private final Properties ledger=new Properties();
    private final RowingStats rowingStats=new RowingStats();
    private final TrainingController training;
    private final WorkoutSessionController workouts;
    private boolean standalone;
    private GameWorkoutAdapter.Frame workoutFrame;
    private boolean test=false,foreground=false,paused=true,connected=false,loaded=false,saveError=false;
    private int screen=0,minutes=5,testWatts=120,baseline=-1,atlasPage=0,mapStage=0,labStage=0,upgradeBranch=0,selectedUpgrade=0,harvestPage=0;
    private long lastFrame,lastMotion,lastDistanceAt,lastSave,lastTelemetryAt;
    private double speed=0,testStrokeFraction;
    private String notice="";
    private static final int WHITE=0xffedf4ff,MUTED=0xff8497b8,CYAN=0xff69e4e3,GOLD=0xffffc687;
    public GravityWellView(Context c,Controls controls,WorkoutSessionController workouts){super(c);this.controls=controls;this.workouts=workouts;setFocusableInTouchMode(true);load();
        training=new TrainingController(c,new TrainingController.Host(){
            public boolean trainingAvailable(){return !test&&!game.running&&!workouts.hasActive()&&loaded&&!saveError;}
            public boolean telemetryAvailable(){return !test&&connected&&foreground&&!paused&&!saveError&&(standalone?workouts.telemetry!=null&&workouts.telemetry.distanceAvailable():screen==1&&lastTelemetryAt>0&&SystemClock.elapsedRealtime()-lastTelemetryAt<=5000);}
            public boolean rowingNow(){return standalone?workouts.active!=null&&workouts.active.moving(SystemClock.elapsedRealtime()):lastMotion>0&&SystemClock.elapsedRealtime()-lastMotion<1600;}
            public void beginTraining(String id){
                if(test)return;
                if(!game.running&&!standalone)recordCompletion();
                if(!workouts.start(standalone?"Standalone rowing":"Gravity Well",id)){notice="Finish the active workout first.";return;}
                controls.openWorkout();
                game.campaign.applyCoachDefault(training.coachMode());
                if(!game.running&&!standalone){game.start(0);rowingStats.begin(ledger);
                    ledger.setProperty("session.id",id);ledger.setProperty("session.recorded","false");}
                ledger.setProperty("training.session",id);save();resume();
            }
            public void endTraining(){if(!test&&trainingMatches()){game.finish();paused=true;screen=2;completeRecorded();save();invalidate();}}
            public void workoutChanged(GameWorkoutAdapter.Frame frame){workoutFrame=frame;workouts.guided=frame;workouts.guidedBlock=training==null||training.runner()==null?-1:training.runner().block;if(frame.paused){paused=true;workouts.pause();}invalidate();}
        });
        if(trainingMatches()&&training.runner().finished&&game.running){game.finish();save();}
        if(game.activeBossId>0)StellarTextures.request(game.bossKind);celestial.prepareBosses();}
    private boolean trainingMatches(){return training!=null&&training.runner()!=null&&!test&&training.runner().id.equals(ledger.getProperty("training.session"));}
    public void openTraining(int page){if(test){save();test=false;load();}paused=true;training.openPage(page);}
    public void exportTraining(android.net.Uri uri){training.exportTo(uri);}
    public void configureTraining(TrainingCore.Goal goal,TrainingCore.Mode mode)throws IOException{training.configure(goal,mode);}
    public void checkpointProfile()throws IOException{active(false);if(loaded&&saveError)throw new IOException("Career could not be saved");}
    private AtomicFile file(){return new AtomicFile(new File(getContext().getFilesDir(),test?"gravity-test-v1.properties":"gravity-real-v1.properties"));}
    private void load(){
        ledger.clear();game=new GravityWellGame();loaded=true;saveError=false;notice="";
        try(FileInputStream in=file().openRead()){ledger.load(in);game=GravityWellGame.restore(ledger,test);notice=game.running?"Saved session ready to resume":"Career restored";}
        catch(FileNotFoundException empty){}
        catch(Exception bad){loaded=false;notice="Save could not be read. Original retained; restart app to retry.";}
        game.enableCampaign(test,ledger.containsKey("game.version"));
        paused=true;rowingStats.interrupt();baseline=-1;lastMotion=lastFrame=lastDistanceAt=0;
    }
    private double number(String key){try{return Double.parseDouble(ledger.getProperty(key,"0"));}catch(Exception e){return 0;}}
    private void add(String key,double value){ledger.setProperty(key,Double.toString(number(key)+value));}
    public void save(){
        if(!test&&paused&&!standalone&&workouts.owns("Gravity Well"))workouts.pause();
        if(!loaded)return;recordCompletion();game.save(ledger);FileOutputStream out=null;
        try{out=file().startWrite();ledger.store(out,"OpenRow Gravity Well: game.* and athlete.* have independent ownership");file().finishWrite(out);saveError=false;}
        catch(Exception e){if(out!=null)file().failWrite(out);saveError=true;paused=true;notice="Save failed. Session paused; reopen to retry.";}
    }
    private void recordCompletion(){
        if(test||game.running||Boolean.parseBoolean(ledger.getProperty("session.recorded","false"))||(game.sessionDistance<=0&&game.sessionStrokes<=0))return;
        boolean dated=ledger.containsKey("session.id");
        ledger.setProperty("last.id",ledger.getProperty("session.id",UUID.randomUUID().toString()));
        ledger.setProperty("last.ended",dated?Long.toString(System.currentTimeMillis()):"0");
        ledger.setProperty("last.distance",Double.toString(game.sessionDistance));ledger.setProperty("last.strokes",Long.toString(game.sessionStrokes));ledger.setProperty("last.seconds",Double.toString(game.sessionMotionSeconds));
        ledger.setProperty("session.recorded","true");
        if(!standalone&&workouts.owns("Gravity Well"))completeRecorded();
    }
    public void active(boolean value){training.foreground(value);rowingStats.interrupt();foreground=value;lastFrame=lastMotion=lastDistanceAt=0;baseline=-1;testStrokeFraction=0;if(!value){paused=true;rowingStats.interrupt();save();}invalidate();}
    public void rowerState(boolean value){
        if(connected==value)return;
        if(!value)training.pause();rowingStats.interrupt();connected=value;baseline=-1;lastMotion=lastDistanceAt=0;
        if(!value&&!test){paused=true;rowingStats.interrupt();save();}
        else if(value&&!paused){
            // Starting before USB connects lets the recording safety gate pause the recorder.
            // Complete that initial handoff together; a later disconnect sets paused above.
            if(!test&&!standalone&&foreground&&screen==1&&game.running&&workouts.owns("Gravity Well")&&workouts.active.paused())workouts.resume();
            if(trainingMatches())training.resume();
        }
    }
    public void telemetry(S4Channels c,String line,long now){}
    public void stroke(){if(canRow()&&!test)game.input(0,1,0);}
    public void distance(int total){
        if(total<0)return;long now=SystemClock.elapsedRealtime();lastTelemetryAt=now;int previous=baseline;baseline=total;
        if(trainingMatches()&&training.runner().paused&&!paused&&foreground&&screen==1)training.resume();
        if(!canRow()||test){lastDistanceAt=now;return;}
        if(previous>=0&&total>previous){int delta=total-previous;double elapsed=lastDistanceAt==0?1:Math.max(.25,(now-lastDistanceAt)/1000.0);
            if(delta<=Math.max(20,elapsed*15)){speed=Math.min(6,delta/elapsed);game.input(delta,0,delta*20);lastMotion=now;if(trainingMatches())training.distance(delta);}
        }
        lastDistanceAt=now;
    }
    private boolean canRow(){return !standalone&&foreground&&getVisibility()==VISIBLE&&screen==1&&!paused&&game.running&&(test||connected)&&loaded&&!saveError&&(!trainingMatches()||!training.runner().paused);}
    private void resume(){if(!loaded||saveError)return;if(!test){String origin=standalone&&workouts.active!=null?workouts.active.data.getProperty("origin"):"Gravity Well";if(!workouts.start(origin,!standalone&&game.running?ledger.getProperty("session.id"):null)){notice="Finish the active workout before starting this row.";return;}workouts.resume();}rowingStats.interrupt();paused=false;if(standalone){foreground=true;training.foreground(true);}screen=1;lastFrame=lastMotion=lastDistanceAt=0;baseline=-1;requestFocus();if(!test&&!connected)controls.connect();if(trainingMatches())training.resume();invalidate();}
    private void start(){if(training.hasSession()&&!test){paused=true;training.show();return;}if(game.running){resume();return;}recordCompletion();if(!test&&!workouts.start("Gravity Well",null)){notice="Finish the active workout first.";return;}ledger.remove("training.session");workoutFrame=null;workouts.guided=null;workouts.guidedBlock=0;game.start(minutes*60);rowingStats.begin(ledger);ledger.setProperty("session.id",test?UUID.randomUUID().toString():workouts.active.id());ledger.setProperty("session.recorded","false");save();resume();}
    private void finish(){if(trainingMatches()&&training.running()){training.stop();return;}game.finish();paused=true;screen=2;completeRecorded();save();invalidate();}
    public boolean isStandalone(){return standalone;}
    public boolean recording(){return !test&&canRow();}
    public void resumeRecorded(){if(test){save();test=false;load();}standalone=false;if(game.running)resume();}
    public void standalone(boolean value){standalone=value;}
    public void standaloneActive(boolean value){foreground=value;paused=!value;training.foreground(value);if(value&&trainingMatches())training.resume();else if(!value)training.pause();}
    public void standaloneDistance(double meters){if(standalone&&trainingMatches())training.distance(meters);}
    public void finishFromDashboard(){if(test){save();test=false;load();}if(trainingMatches()&&training.running())training.stop();else{if(workouts.owns("Gravity Well"))game.finish();completeRecorded();save();}}
    private void completeRecorded(){
        if(test||!workouts.hasActive())return;
        WorkoutSession done=workouts.active;boolean guidedRow=workouts.guided!=null;workouts.finish();
        if(workouts.error==null&&!guidedRow&&training!=null)training.freeRow(done.id(),System.currentTimeMillis(),done.number("elapsed"),done.total("motionSeconds"),done.total("distance"));
    }
    @Override protected void onDraw(Canvas c){
        renderLoop.begin();int detail=renderLoop.performance.quality;celestial.quality(detail);tidalStreams.quality(detail);singularity.quality(detail);
        super.onDraw(c);long now=SystemClock.elapsedRealtime();double dt=lastFrame==0?0:Math.min(.1,(now-lastFrame)/1000.0);lastFrame=now;
        if(canRow()){
            boolean moving=test?testWatts>0:lastMotion>0&&now-lastMotion<1600;
            {double actual=game.duration==0?dt:Math.min(dt,Math.max(0,game.duration-game.sessionSeconds));
                if(test&&moving){testStrokeFraction+=actual*24/60;long s=(long)testStrokeFraction;testStrokeFraction-=s;game.input(actual*2.5,s,testWatts*actual);}
                boolean eligible=!trainingMatches()||workoutFrame!=null&&workoutFrame.campaignWorkEligible(moving);
                boolean bossWindow=eligible&&(!trainingMatches()||workoutFrame.campaignBossWindow(moving));
                game.campaignWindow(eligible,bossWindow);
                game.gameplayContribution(test?(moving?1:0):workouts.effort.contribution);
                double before=game.seconds;double physicsInput=test?testWatts/240.0:speed/4.0;double visualInput=test?physicsInput:Math.min(TrainingVisuals.ceiling(trainingMatches()?workoutFrame:null),workouts.effort.contribution*.5);if(!test&&!trainingMatches())visualInput=workouts.effort.contribution*.5;game.advance(actual,physicsInput,true,moving,visualInput);
                if(!game.running){screen=2;paused=true;rowingStats.interrupt();completeRecorded();save();}
            }
            if(now-lastSave>3000){save();lastSave=now;}
        }
        c.save();c.scale(getWidth()/1280f,getHeight()/720f);c.drawColor(0xff070d20);
        stars(c);if(screen==1)play(c,now);else if(screen==3)upgrades(c);else if(screen==4)atlas(c);else if(screen==5)campaignMap(c);else overview(c);
        if(saveError)label(c,"SAVE FAILED — progression paused",640,680,18,0xffff9090,true);
        c.restore();renderLoop.end(foreground&&getVisibility()==VISIBLE&&screen==1);if(foreground&&getVisibility()==VISIBLE&&screen!=1)postInvalidateDelayed(250);
    }
    private void stars(Canvas c){celestial.background(c);}
    private void box(Canvas c,float x,float y,float w,float h,int color){paint.setColor(color);paint.setStyle(Paint.Style.FILL);c.drawRoundRect(x,y,x+w,y+h,14,14,paint);}
    private void label(Canvas c,String s,float x,float y,float size,int color,boolean center){paint.setColor(color);paint.setStyle(Paint.Style.FILL);paint.setTextSize(size);paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));paint.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);c.drawText(s,x,y,paint);}
    private void button(Canvas c,String s,int x,int y,int w,boolean accent){box(c,x,y,w,48,accent?0xff22585f:0xff17243c);label(c,s,x+w/2,y+30,17,accent?CYAN:WHITE,true);}
    private String fmt(double n){return String.format(Locale.US,"%,.0f",n);}
    private String clock(double n){int v=(int)n;return String.format(Locale.US,"%02d:%02d",v/60,v%60);}
    private void header(Canvas c,String subtitle){label(c,"GRAVITY WELL",46,62,29,WHITE,false);label(c,subtitle,46,91,15,MUTED,false);button(c,"OpenRow",1110,35,125,false);}
    private void hole(Canvas c,float x,float y,float radius,double t){
        singularity.draw(c,x,y,radius,game.visualPhase,game.visualField(),game.ranks[5]>0||game.campaign!=null&&game.campaign.cosmeticPreview==1,game.flash);
        if(game.campaign!=null&&game.campaign.cosmeticPreview==2){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(radius*.035f);paint.setColor(0x886be4ed);c.drawCircle(x,y,radius*1.07f,paint);paint.setStyle(Paint.Style.FILL);}
        if(game.campaign!=null&&game.campaign.cosmeticPreview==3){paint.setColor(GOLD);for(int i=0;i<8;i++){double a=i*Math.PI/4;float px=x+(float)Math.cos(a)*radius*4.6f,py=y+(float)Math.sin(a)*radius*1.7f;c.drawCircle(px,py,2.4f,paint);}label(c,"GALAXY LEGACY",x,y+radius*1.55f,13,GOLD,true);}
    }
    private void overview(Canvas c){boolean summary=screen==2;header(c,test?"SYNTHETIC LAB  /  separate test career":"YOUR SINGULARITY  /  local career");
        hole(c,940,340,60,game.seconds);
        label(c,summary?"Session complete":"Where will you row today?",55,168,32,WHITE,false);
        label(c,summary?"Captured matter waits for your next row.":"Explore the campaign or settle into a free row.",55,205,18,MUTED,false);
        box(c,45,240,620,170,0xff111d34);
        label(c,summary?"THIS SESSION":"CAREER",68,270,13,CYAN,false);
        label(c,fmt(summary?game.earned:game.xp)+" XP",68,315,35,WHITE,false);
        label(c,fmt(summary?game.sessionDistance:game.distance)+" m",290,315,35,WHITE,false);
        label(c,clock(summary?game.sessionMotionSeconds:game.seconds),490,315,35,WHITE,false);
        label(c,summary?fmt(game.sessionCaptures)+(game.sessionCaptures==1?" body captured":" bodies captured"):"Spendable cosmic XP",68,355,16,MUTED,false);
        label(c,test?"Simulated distance":"Measured S4 distance",290,355,15,MUTED,false);
        label(c,"Motion time",490,355,15,MUTED,false);
        label(c,fmt(game.reservoir)+" / "+fmt(game.capacity())+" matter stored",68,389,16,GOLD,false);
        if(summary){button(c,"Upgrade singularity",55,445,260,true);button(c,"Next session",330,445,260,false);}
        else {button(c,minutes==5?"5 MIN  · selected":"5 minutes",55,445,180,minutes==5);button(c,minutes==30?"30 MIN  · selected":"30 minutes",250,445,180,minutes==30);button(c,game.running?"Resume saved row":"Start rowing",55,510,375,true);button(c,"Upgrade tree",450,510,180,false);
            button(c,test?"Input: TEST POWER":"Input: S4 USB",55,575,375,false);button(c,"Body atlas",450,575,180,false);}
        if(!summary){button(c,"Campaign map",760,445,325,true);label(c,game.campaign.title(),770,512,15,GOLD,false);}
        if(!test&&!summary)button(c,"Guided workout",760,625,325,true);
        label(c,test?"Test input cannot change your real fitness record.":"Your rowing drives the game. Upgrades never change your recorded distance.",55,663,16,MUTED,false);
        label(c,notice,55,694,14,MUTED,false);
        label(c,"ROWING IN THIS CAREER",770,540,15,CYAN,false);
        label(c,test?"TEST DATA ONLY":fmt(game.distance)+" real meters  ·  "+fmt(game.strokes)+" strokes",770,570,19,WHITE,false);
        label(c,"Recorded separately from game upgrades.",770,600,14,MUTED,false);
    }
    private void play(Canvas c,long now){
        scene.prepare(game);
        header(c,test?"TEST POWER  /  simulated joules and distance":training.provisionalGameplay()?"PERSONAL GAME PACE · provisional reference":"PERSONAL GAME PACE · based on recent reported effort");
        c.save();c.clipRect(245,110,1240,595);
        float cx=745,cy=350;
        int dustCount=0;
        for(GravityWellGame.Body physical:game.bodies){GravityWellGame.Body b=scene.body(physical);if(b==null)continue;
            if(b.dust){dustPoints[dustCount++]=cx+(float)b.x;dustPoints[dustCount++]=cy+(float)b.y;}
            else if(!GravityWellScene.front(b.x,b.y))celestial.body(c,b,cx,cy);
        }
        paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(0x187cbccc);paint.setStrokeWidth(5);c.drawPoints(dustPoints,0,dustCount,paint);paint.setColor(0xa8c8e3e5);paint.setStrokeWidth(1.4f);c.drawPoints(dustPoints,0,dustCount,paint);paint.setStrokeCap(Paint.Cap.BUTT);
        tidalStreams.draw(c,game,scene,cx,cy,false);
        celestial.debris(c,game,cx,cy,false);
        hole(c,cx,cy,41+(float)Math.min(6,game.reservoir/32),game.seconds);
        celestial.debris(c,game,cx,cy,true);
        tidalStreams.draw(c,game,scene,cx,cy,true);
        for(GravityWellGame.Body physical:game.bodies){GravityWellGame.Body b=scene.body(physical);if(b!=null&&!b.dust&&GravityWellScene.front(b.x,b.y))celestial.body(c,b,cx,cy);}
        c.restore();
        if(game.activeBossId>0){
            box(c,280,110,925,58,0xe8111d34);
            label(c,"BOSS  /  "+GravityWellGame.KINDS[game.bossKind].name.toUpperCase(Locale.US),298,133,15,GOLD,false);
            int pieces=game.bossPieces();GravityWellGame.Body star=scene.stellarBody();String stage=star!=null&&star.visualStreams>1&&star.visualShear>.05?star.visualStreams+" plasma filaments · exposed area":pieces>1?pieces+" chunks · increased exposed area":"Row to strip its layers";
            label(c,stage,950,133,14,WHITE,true);
            box(c,298,145,790,6,0xff27334a);box(c,298,145,(float)(790*Math.max(0,Math.min(1,game.bossMass()/game.bossInitialMass))),6,GOLD);
            label(c,String.format(Locale.US,"%.0f%%",game.bossMass()/game.bossInitialMass*100),1110,154,14,GOLD,false);
        }else if(game.bossToast>0)label(c,"BOSS CONSUMED · matter still requires rowing to process",745,140,16,GOLD,true);
        box(c,35,130,205,420,0xff111d34);label(c,"SESSION TIME",55,161,13,MUTED,false);label(c,trainingMatches()?clock(training.runner().elapsed)+" / "+clock(training.runner().plan.seconds()):clock(game.sessionSeconds)+" / "+clock(game.duration),55,200,25,WHITE,false);
        label(c,"DISTANCE",55,248,13,MUTED,false);label(c,fmt(game.sessionDistance)+" m",55,286,31,WHITE,false);
        label(c,"COSMIC XP",55,333,13,MUTED,false);label(c,"+"+fmt(game.earned),55,372,31,CYAN,false);
        label(c,"CAPTURED MATTER",55,419,13,MUTED,false);label(c,fmt(game.reservoir)+" / "+fmt(game.capacity()),55,453,24,GOLD,false);
        box(c,55,477,160,7,0xff27334a);box(c,55,477,(float)(160*game.reservoir/game.capacity()),7,CYAN);
        label(c,test?testWatts+" simulated W":trainingMatches()?"Workout-guided effects":"Rowing-linked effects",55,524,15,MUTED,false);
        String message=paused?"PAUSED — your singularity is saved":test&&testWatts==0?"Coasting · timer running · XP paused":!test&&!connected?"Connect the S4, then resume":!test&&(lastMotion==0||now-lastMotion>=1600)?"Coasting · row to earn XP":"Capturing worlds  ·  stripping matter into the well";
        if(trainingMatches()&&workoutFrame!=null){
            String phase=workoutFrame.phase==TrainingCore.Phase.RECOVERY?"RECOVERY · no hard rowing needed":workoutFrame.phase==TrainingCore.Phase.WORK?"WORK":workoutFrame.phase.toString().replace('_',' ');
            label(c,phase+" · "+clock(workoutFrame.remainingBlockSeconds)+" · effort "+workoutFrame.effort+"/10",745,557,18,CYAN,true);
            message=paused?"PAUSED · resume when ready":workoutFrame.guidance;
        }
        label(c,message,745,580,20,paused?GOLD:CYAN,true);
        if(game.campaign!=null){GravityWellCampaign cp=game.campaign;label(c,cp.selectedComplete()?"DESTINATION COMPLETE · choose the next after your workout":cp.title()+(cp.next()!=null&&cp.next().seconds>0&&!cp.selected.equals("free")?" · "+clock(cp.progress)+" / "+clock(cp.next().seconds):""),745,607,15,GOLD,true);}
        button(c,paused?"Resume":"Pause",35,625,160,true);button(c,"End session",210,625,185,false);
        if(test){button(c,"− 40 W",530,625,130,false);button(c,testWatts==0?"Start input":"Stop input",675,625,155,false);button(c,"+ 40 W",845,625,130,false);}
        else button(c,connected?"S4 connected":"Connect S4",530,625,200,false);
        if(trainingMatches())button(c,"Adjust workout",800,625,230,false);
        RowingTelemetryFrame telemetry=workouts.telemetry;
        String live=telemetry==null?"Waiting for S4 readings":String.format(Locale.US,"%s W · %s s/500m · %s spm · game: %s",Double.isFinite(telemetry.watts)?fmt(telemetry.watts):"—",telemetry.speed>0?fmt(500/telemetry.speed):"—",Double.isFinite(telemetry.cadence)?fmt(telemetry.cadence):"—",workouts.effort.source);
        label(c,test?"Space: pause  ·  arrows: power  ·  no real fitness credit":live,40,702,14,MUTED,false);
    }
    private static final String[] BRANCHES={"All branches","Attraction","Processing","Storage","Refinement","Orbital flow","Disruption","Discovery"};
    private float treeLimit(){return 480+300*(GravityWellCampaign.CHAPTERS.length-3);}
    private float treeScroll,touchStartX,treeStartScroll; private boolean treeDragging;
    private int branchFor(int i){return GravityWellGame.NODES[i].branch+1;}
    private float nodeX(int i){return (i>=9?490+100*(GravityWellGame.NODES[i].layer-1):i==6?250:i==7?315:i==8?380:315)-treeScroll;}
    private float nodeY(int i){return i==5?593:220+41*(branchFor(i)-1);}
    private void wrapped(Canvas c,String message,float x,float y,int size,int color,float width){
        paint.setTextSize(size);String line="";for(String word:message.split(" ")){String next=line.isEmpty()?word:line+" "+word;if(paint.measureText(next)>width&&!line.isEmpty()){label(c,line,x,y,size,color,false);y+=size+7;line=word;}else line=next;}if(!line.isEmpty())label(c,line,x,y,size,color,false);
    }
    private void upgrades(Canvas c){
        header(c,test?"TEST LAB / UPGRADE TREE":"UPGRADE TREE");
        label(c,fmt(game.xp)+" XP · "+materials(),45,145,17,CYAN,false);
        button(c,"←",530,110,70,false);button(c,"Jump to chapter",610,110,230,false);button(c,"→",850,110,70,false);
        c.save();c.clipRect(220,170,930,618);
        label(c,"FOUNDATION",315-treeScroll,185,13,MUTED,true);for(int tier=1;tier<=GravityWellCampaign.CHAPTERS.length;tier++){float left=450+300*(tier-1)-treeScroll;box(c,left,170,280,432,0x122f6575);label(c,"TIER "+tier+" · CHAPTER "+tier,left+140,185,13,CYAN,true);}
        // Branch tracks group parallel tools; only the smaller prerequisite links mean requirements.
        for(int branch=1;branch<8;branch++){
            float y=220+41*(branch-1);boolean focus=upgradeBranch==0||upgradeBranch==branch;
            paint.setColor(focus?0xff263d52:0xff172336);paint.setStrokeWidth(2);c.drawLine(315-treeScroll,y,1290-treeScroll,y,paint);

        }

        for(int i=0;i<GravityWellGame.NODES.length;i++)for(int parent:GravityWellGame.NODES[i].prerequisites){paint.setColor(i==selectedUpgrade?GOLD:0x354e858e);paint.setStrokeWidth(1);c.drawLine(nodeX(parent),nodeY(parent),nodeX(i),nodeY(i),paint);}
        for(int i=0;i<GravityWellGame.NODES.length;i++){
            float x=nodeX(i),y=nodeY(i);if(x<199||x>951)continue;boolean focus=upgradeBranch==0||branchFor(i)==upgradeBranch,owned=game.ranks[i]>0,available=game.blocked(i).startsWith("Buy");
            paint.setStyle(Paint.Style.FILL);paint.setColor(!focus?0xff101c2c:owned?0xff22585f:available?0xff253f51:0xff172237);c.drawCircle(x,y,19,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(i==selectedUpgrade?3:1.5f);paint.setColor(i==selectedUpgrade?GOLD:owned?CYAN:available?0xff6795a7:0xff354456);c.drawCircle(x,y,19,paint);paint.setStyle(Paint.Style.FILL);
            label(c,game.ranks[i]+"/"+GravityWellGame.NODES[i].max,x,y+5,14,focus?WHITE:MUTED,true);
        }
        c.restore();
        for(int branch=1;branch<8;branch++)label(c,BRANCHES[branch],45,226+41*(branch-1),18,upgradeBranch==0||upgradeBranch==branch?WHITE:MUTED,false);
        label(c,"Specialists",45,513,18,CYAN,false);label(c,"Appearance",45,599,18,MUTED,false);
        box(c,950,175,290,450,0xff111e32);GravityWellGame.Node n=GravityWellGame.NODES[selectedUpgrade];
        label(c,selectedUpgrade<9?"FOUNDATION TOOL":(n.specialist?"SPECIALIST · ":"MAIN TOOL · ")+"TIER "+n.tier,970,205,13,CYAN,false);
        wrapped(c,n.title,970,242,23,WHITE,245);
        label(c,"Rank "+game.ranks[selectedUpgrade]+" of "+n.max,970,302,17,GOLD,false);
        wrapped(c,game.upgradeEffect(selectedUpgrade),970,339,18,WHITE,245);
        wrapped(c,game.requirements(selectedUpgrade),970,400,14,MUTED,245);
        wrapped(c,game.running?"Finish your session to buy upgrades.":game.blocked(selectedUpgrade),970,477,14,GOLD,245);
        int source=game.nodeSource(selectedUpgrade);if(source>=0){label(c,String.format(Locale.US,"Have %.1f / %d %s",game.campaign.materials[source],game.nodeMaterialCost(selectedUpgrade),GravityWellCampaign.MATERIALS[source]),970,535,13,CYAN,false);label(c,"Source: "+GravityWellCampaign.SOURCES[source],970,555,13,MUTED,false);}
        button(c,game.ranks[selectedUpgrade]>=n.max?"Fully upgraded":game.running?"Session in progress":game.blocked(selectedUpgrade).startsWith("Buy")?"Upgrade · "+game.cost(selectedUpgrade)+" XP":"Not available yet",970,575,250,!game.running&&game.blocked(selectedUpgrade).startsWith("Buy"));
        button(c,"Back",45,625,180,false);button(c,upgradeBranch==0?"Focus a branch":"Show all branches",250,625,255,false);
        label(c,"Swipe the tree · tap to inspect · ranks unlock tiers",530,654,16,MUTED,false);
    }
    private String materials(){GravityWellCampaign cp=game.campaign;return String.format(Locale.US,"%.1f minerals · %.1f volatiles · %.1f gas",cp.materials[0],cp.materials[1],cp.materials[2]);}
    private void campaignMap(Canvas c){
        GravityWellCampaign cp=game.campaign;
        header(c,test?"SYNTHETIC EXPEDITION LAB":"YOUR EXPEDITION");
        label(c,"Chapter "+(GravityWellCampaign.STAGE_CHAPTERS[mapStage]+1)+" · "+GravityWellCampaign.CHAPTERS[GravityWellCampaign.STAGE_CHAPTERS[mapStage]],45,143,27,WHITE,false);
        button(c,"Choose region",850,110,180,false);button(c,"Region →",1040,110,195,false);
        for(int step=0;step<4;step++){int i=mapStage*4+step,y=190+step*65;GravityWellCampaign.Destination d=GravityWellCampaign.DESTINATIONS[i];
            paint.setColor(i<cp.cursor?CYAN:i==cp.cursor?GOLD:MUTED);paint.setStrokeWidth(2);if(step<3)c.drawLine(70,y+17,70,y+82,paint);c.drawCircle(70,y+17,8,paint);
            label(c,d.title,95,y+21,22,i<=cp.cursor?WHITE:MUTED,false);
            label(c,i<cp.cursor?"COMPLETE":i==cp.cursor?d.boss>=0?"Ready to encounter":clock(cp.progress)+" / "+clock(d.seconds)+" active work":"Undiscovered",95,y+43,15,i==cp.cursor?GOLD:MUTED,false);
        }
        label(c,cp.complete()?"Galaxy complete · all ten bosses discovered":cp.selectedComplete()?"Destination complete. Choose your next expedition.":"Selected: "+cp.title(),45,485,18,CYAN,false);
        int encounterKind=game.activeBossId>0?game.bossKind:cp.pendingBoss();
        if(encounterKind>=0)label(c,"Estimated work at reference effort: "+(int)Math.ceil(GravityWellBalance.seconds(game,encounterKind)*(game.activeBossId>0?game.bossIntegrity():1)/60)+" min · damage stays saved",45,455,16,MUTED,false);
        if(!game.parkedDestinations().isEmpty())label(c,"Saved encounters: "+game.parkedDestinations().size(),650,485,16,GOLD,false);
        button(c,cp.complete()?"Campaign complete":"Choose next expedition",45,505,470,!cp.complete());button(c,"Choose free play",45,565,285,false);
        if(cp.earlyBoss())button(c,"Try first boss",650,505,180,true);
        label(c,materials(),650,202,17,CYAN,false);label(c,"HARVEST AGAIN · "+(harvestPage+1)+" / 3",850,254,15,GOLD,false);
        for(int slot=0;slot<4;slot++){int stage=harvestPage*4+slot;if(stage>=GravityWellCampaign.BOSSES.length)break;int kind=GravityWellCampaign.BOSSES[stage];boolean unlocked=cp.cleared.contains(GravityWellGame.KINDS[kind].id)||cp.legacyAccess(kind);button(c,(unlocked?"Harvest ":"Locked · ")+GravityWellGame.KINDS[kind].name,850,280+slot*60,385,unlocked);}
        button(c,"← Harvests",850,510,180,false);button(c,"Harvests →",1040,510,195,false);
        if(test){button(c,"Rate: "+(int)cp.acceleration+"×",650,565,170,false);button(c,"Boss checkpoint",835,565,205,false);button(c,"Cosmetic "+cp.cosmeticPreview,1055,565,180,false);}
        else {button(c,cp.autoQueue?"Preparation queue: ON":"Preparation queue: OFF",650,565,550,false);}
        button(c,"Back to career",45,625,225,false);button(c,game.running?"Resume session":"Start rowing",300,625,285,true);
        if(!test)button(c,"Guided workout",625,625,325,true);
        label(c,notice,45,698,14,MUTED,false);
    }
    private void atlas(Canvas c){header(c,"BODY ATLAS / YOUR DISCOVERIES");
        label(c,fmt(game.distance)+" career meters  ·  "+fmt(game.bossesDefeated)+" bosses consumed",45,137,22,CYAN,false);
        for(int slot=0;slot<6;slot++){int i=atlasPage*6+slot;if(i>=GravityWellGame.KINDS.length)break;GravityWellGame.Kind k=GravityWellGame.KINDS[i];int x=45+(slot%3)*405,y=175+(slot/3)*205;
            box(c,x,y,385,185,0xff13203a);label(c,k.name,x+18,y+38,24,game.unlocked(i)?WHITE:MUTED,false);label(c,k.description,x+18,y+77,15,MUTED,false);
            label(c,game.unlocked(i)?"Discovered":"Discover in the campaign",x+18,y+116,16,game.unlocked(i)?CYAN:GOLD,false);
            label(c,k.boss?"Tap here to select a discovered harvest":"Ordinary encounter",x+18,y+151,15,MUTED,false);
        }
        button(c,"Back to career",45,625,225,true);button(c,"← Previous",825,625,180,false);button(c,"Next →",1020,625,215,false);label(c,"Page "+(atlasPage+1)+" / 3 · Bosses persist between sessions.",300,656,17,MUTED,false);
    }
    private boolean hit(float x,float y,int left,int top,int width){return x>=left&&x<=left+width&&y>=top&&y<=top+48;}
    @Override public boolean onTouchEvent(MotionEvent event){if(screen==3){float tx=event.getX()*1280/getWidth(),ty=event.getY()*720/getHeight();
            if(event.getAction()==MotionEvent.ACTION_DOWN){touchStartX=tx;treeStartScroll=treeScroll;treeDragging=tx>=220&&tx<930&&ty>=170&&ty<618;return true;}
            if(event.getAction()==MotionEvent.ACTION_MOVE&&treeDragging){treeScroll=Math.max(0,Math.min(treeLimit(),treeStartScroll+touchStartX-tx));invalidate();return true;}
            if(event.getAction()==MotionEvent.ACTION_CANCEL){treeDragging=false;return true;}
            if(event.getAction()==MotionEvent.ACTION_UP&&treeDragging&&Math.abs(tx-touchStartX)>10){treeDragging=false;return true;}}
        if(event.getAction()!=MotionEvent.ACTION_UP)return true;float x=event.getX()*1280/getWidth(),y=event.getY()*720/getHeight();
        if(hit(x,y,1110,35,125)){active(false);controls.back();return true;}
        if(screen==1){if(hit(x,y,35,625,160)){if(paused)resume();else{training.pause();paused=true;rowingStats.interrupt();save();}}else if(hit(x,y,210,625,185))finish();else if(!test&&trainingMatches()&&hit(x,y,800,625,230)){training.pause();paused=true;save();training.show();}else if(test){if(hit(x,y,530,625,130))testWatts=Math.max(0,testWatts-40);if(hit(x,y,675,625,155))testWatts=testWatts==0?120:0;if(hit(x,y,845,625,130))testWatts=Math.min(400,testWatts+40);}else if(hit(x,y,530,625,200))controls.connect();}
        else if(screen==5){
            GravityWellCampaign cp=game.campaign;
            if(hit(x,y,45,625,225))screen=0;
            else if(hit(x,y,850,110,180)){String[] regions=new String[GravityWellCampaign.BOSSES.length];for(int i=0;i<regions.length;i++)regions[i]="Chapter "+(GravityWellCampaign.STAGE_CHAPTERS[i]+1)+" · "+GravityWellGame.KINDS[GravityWellCampaign.BOSSES[i]].name;new android.app.AlertDialog.Builder(getContext()).setTitle("Expedition regions").setItems(regions,(d,which)->{mapStage=which;invalidate();}).setNegativeButton("Back",null).show();}
            else if(hit(x,y,1040,110,195))mapStage=(mapStage+1)%GravityWellCampaign.BOSSES.length;
            else if(hit(x,y,300,625,285))start();
            else if(!test&&hit(x,y,625,625,325)){paused=true;training.show();}
            else if(hit(x,y,45,505,470)&&!cp.complete()){choose(cp.next().id);mapStage=Math.min(GravityWellCampaign.BOSSES.length-1,cp.cursor/4);}
            else if(cp.earlyBoss()&&hit(x,y,650,505,180))choose("debris_boss");
            else if(hit(x,y,45,565,285))choose("free");
            else if(!test&&hit(x,y,650,565,550)){cp.setAutoQueue(!cp.autoQueue);save();}
            else if(test&&hit(x,y,650,565,170)){cp.acceleration=cp.acceleration==1?30:cp.acceleration==30?120:1;save();}
            else if(test&&hit(x,y,835,565,205)){String[] checkpoints=new String[GravityWellCampaign.BOSSES.length+1];for(int i=0;i<checkpoints.length-1;i++)checkpoints[i]=GravityWellGame.KINDS[GravityWellCampaign.BOSSES[i]].name+" ready";checkpoints[checkpoints.length-1]="Galaxy complete";new android.app.AlertDialog.Builder(getContext()).setTitle("Synthetic boss checkpoint").setItems(checkpoints,(d,which)->{if(game.labCheckpoint(which)){mapStage=Math.min(GravityWellCampaign.BOSSES.length-1,cp.cursor/4);notice="Synthetic checkpoint loaded";save();}else notice="Finish the synthetic session before loading a checkpoint";invalidate();}).setNegativeButton("Back",null).show();}
            else if(test&&hit(x,y,1055,565,180)){cp.cosmeticPreview=(cp.cosmeticPreview+1)%4;save();}
            else if(hit(x,y,850,510,180))harvestPage=(harvestPage+2)%3;
            else if(hit(x,y,1040,510,195))harvestPage=(harvestPage+1)%3;
            else for(int slot=0;slot<4;slot++){int stage=harvestPage*4+slot;if(stage<GravityWellCampaign.BOSSES.length&&hit(x,y,850,280+slot*60,385))choose("repeat:"+GravityWellGame.KINDS[GravityWellCampaign.BOSSES[stage]].id);}
        }
        else if(screen==3){
            if(hit(x,y,45,625,180))screen=0;
            else if(hit(x,y,250,625,255))upgradeBranch=upgradeBranch==0?branchFor(selectedUpgrade):0;
            else if(hit(x,y,530,110,70))treeScroll=Math.max(0,treeScroll-300);
            else if(hit(x,y,850,110,70))treeScroll=Math.min(treeLimit(),treeScroll+300);
            else if(hit(x,y,610,110,230)){String[] titles=new String[GravityWellCampaign.CHAPTERS.length];for(int i=0;i<titles.length;i++)titles[i]="Chapter "+(i+1)+" · "+GravityWellCampaign.CHAPTERS[i];new android.app.AlertDialog.Builder(getContext()).setTitle("Upgrade tiers").setItems(titles,(d,which)->{treeScroll=Math.min(treeLimit(),which*300);invalidate();}).setNegativeButton("Back",null).show();}
            else if(hit(x,y,970,575,250)){if(game.purchase(selectedUpgrade)){notice="Upgraded: "+GravityWellGame.NODES[selectedUpgrade].title;save();}}
            else {boolean picked=false;for(int i=0;i<GravityWellGame.NODES.length;i++)if(x>=220&&x<=930&&Math.abs(x-nodeX(i))<=21&&Math.abs(y-nodeY(i))<=20){selectedUpgrade=i;if(upgradeBranch!=0)upgradeBranch=branchFor(i);picked=true;break;}
                if(!picked&&x>=35&&x<=205&&y>=196&&y<=530)upgradeBranch=Math.max(1,Math.min(8,(int)((y-199)/41)+1));}
        }
        else if(screen==4){if(hit(x,y,45,625,225))screen=0;else if(hit(x,y,825,625,180))atlasPage=(atlasPage+2)%3;else if(hit(x,y,1020,625,215))atlasPage=(atlasPage+1)%3;else for(int slot=0;slot<6;slot++){int i=atlasPage*6+slot;if(i<GravityWellGame.KINDS.length&&GravityWellGame.KINDS[i].boss&&hit(x,y,45+(slot%3)*405,294+(slot/3)*205,385)){choose("repeat:"+GravityWellGame.KINDS[i].id);}}}
        else if(screen==2){if(hit(x,y,55,445,260))screen=3;else if(hit(x,y,330,445,260))screen=0;}
        else{if(hit(x,y,760,445,325)){screen=5;mapStage=Math.min(GravityWellCampaign.BOSSES.length-1,game.campaign.cursor/4);}else if(!test&&hit(x,y,760,625,325)){paused=true;training.show();}else if(hit(x,y,55,445,180))minutes=5;else if(hit(x,y,250,445,180))minutes=30;else if(hit(x,y,55,510,375))start();else if(hit(x,y,450,510,180))screen=3;else if(hit(x,y,450,575,180))screen=4;else if(hit(x,y,55,575,375)){if(training.hasSession()){notice="Finish and review your training workout before switching inputs.";}else{save();test=!test;load();}}}
        invalidate();return true;
    }
    private void choose(String id){if(game.selectDestination(id)){notice="Selected: "+game.campaign.title()+" · unfinished encounters stay saved";save();}else notice="Finish your session before choosing another destination.";}
    public boolean key(int key,boolean down){if(!down)return false;if(key==KeyEvent.KEYCODE_BACK){if(screen==3||screen==4||screen==5||screen==2){screen=0;invalidate();}else{active(false);controls.back();}return true;}if(screen!=1)return false;if(key==KeyEvent.KEYCODE_SPACE){if(paused)resume();else{training.pause();paused=true;rowingStats.interrupt();save();}return true;}if(test&&(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT)){testWatts=Math.max(0,Math.min(400,testWatts+(key==KeyEvent.KEYCODE_DPAD_RIGHT?40:-40)));return true;}return false;}
}



