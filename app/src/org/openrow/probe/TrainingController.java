package org.openrow.probe;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.graphics.*;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import static org.openrow.probe.TrainingCore.*;
import org.openrow.probe.TrainingCore.Record;

/** Native training controls; scheduling and persistence never depend on drawing a game frame. */
public final class TrainingController {
    public static final int EXPORT_REQUEST=481;
    public interface Host extends GameWorkoutAdapter {
        boolean trainingAvailable();
        boolean telemetryAvailable();
        boolean rowingNow();
        void beginTraining(String sessionId);
        void endTraining();
    }
    private final Context context;
    private final Host host;
    private final Handler handler=new Handler();
    private TrainingStore store;
    private TrainingVisuals.Profile visualProfile;
    private String visualSession;
    private double gameplayReference;
    private String error;
    private long saved;
    private boolean foreground, feedbackOpen;
    private int minutes=10,trainingPage=0;
    private Readiness readiness=Readiness.READY;
    private ReturnChoice comeback=ReturnChoice.MAINTAIN;
    private AlertDialog panel;
    public TrainingController(Context context,Host host){this.context=context;this.host=host;
        try{store=new TrainingStore(context);
            // Import only the date of prior real activity, never fabricate old assessments.
            try{long last=(long)AthleteStore.read(context).value("last.ended");
                if(last>store.history.state().lastTraining())store.history.data.setProperty("lastActivity",""+last);
            }catch(IOException ignored){}
        }catch(IOException e){error=e.getMessage();}}
    public Runner runner(){return store==null?null:store.active;}
    public void configure(Goal goal,Mode mode)throws IOException{if(store==null)throw new IOException("Training unavailable");store.history.settings(goal,mode);store.save();}
    public boolean running(){return runner()!=null&&!runner().finished;}
    public boolean hasSession(){return runner()!=null;}
    private final Runnable pulse=new Runnable(){public void run(){
        if(!foreground)return;
        Runner r=runner();if(r!=null&&!r.finished){r.tick(SystemClock.elapsedRealtime(),host.rowingNow(),host.telemetryAvailable());
            if(!host.telemetryAvailable()&&!r.paused&&!r.finished)r.pause(SystemClock.elapsedRealtime());publish();
            if(r.finished)completed();else if(SystemClock.elapsedRealtime()-saved>=3000)persist();}
        handler.postDelayed(this,250);
    }};
    private TrainingVisuals.Profile visualProfile(){
        String id=running()?runner().id:"free";
        if(visualProfile==null||!id.equals(visualSession)){visualProfile=store==null?TrainingVisuals.empty():TrainingVisuals.profile(store.history.records(),System.currentTimeMillis(),running()?runner().id:"");gameplayReference=store==null?0:GameplayEffort.reference(store.history.records(),System.currentTimeMillis());visualSession=id;}
        return visualProfile;
    }
    public double visualInput(double speed,boolean moving,boolean guided,GameWorkoutAdapter.Frame frame){return guided?TrainingVisuals.input(frame,speed,moving):TrainingVisuals.freeInput(visualProfile(),speed,moving);}
    public double gameplayInput(double speed,boolean moving,boolean guided,GameWorkoutAdapter.Frame frame){visualProfile();return GameplayEffort.contribution(speed,moving,gameplayReference,guided?frame:null);}
    public boolean coachMode(){return store!=null&&store.history.state().mode==Mode.COACH;}
    public boolean provisionalGameplay(){visualProfile();return gameplayReference<=0;}
    private void publish(){if(runner()!=null)host.workoutChanged(new GameWorkoutAdapter.Frame(runner(),visualProfile()));}
    public void foreground(boolean value){if(value)visualProfile=null;foreground=value;handler.removeCallbacks(pulse);
        if(!value)pause();else {handler.post(pulse);if(runner()!=null&&runner().finished)handler.post(()->feedback());}}
    public void pause(){if(running()){runner().pause(SystemClock.elapsedRealtime());publish();persist();}}
    public void resume(){if(running()&&error==null&&host.telemetryAvailable()){
        runner().resume(SystemClock.elapsedRealtime());publish();}}
    public void distance(double meters){if(running())runner().observeDistance(meters);}
    /** Ordinary free play supplies self-selected endurance evidence after optional feedback. */
    public void freeRow(String id,long ended,double elapsed,double motion,double distance){
        if(store==null||error!=null||hasSession()||elapsed<1||elapsed>2700||motion<=0)return;
        Plan p=new Plan("observed-free-row-v1","Self-selected rowing; no prescribed intensity or benchmark result.",Kind.FREE_ROW,Category.ENDURANCE,store.history.state().goal,
            Arrays.asList(new Block(Phase.WORK,Category.ENDURANCE,(int)Math.ceil(elapsed),0)),true);
        Runner r=new Runner(id,p,Math.max(0,ended-(long)(elapsed*1000)),false);
        r.elapsed=elapsed;r.blockSeconds[0]=elapsed;r.rowingSeconds=Math.min(elapsed,motion);r.blockRowingSeconds[0]=r.rowingSeconds;r.distance=distance;r.blockDistance[0]=distance;
        r.finished=true;r.outcome=Outcome.COMPLETED;store.active=r;store.history.data.setProperty("pendingEnded",""+ended);
        try{store.record(-1,-1);visualProfile=null;}catch(IOException e){error="Free-row training record could not be saved. Retry in Training.";}
        if(foreground&&error==null)handler.post(()->feedback());
    }
    public void stop(){if(running()){runner().stop(SystemClock.elapsedRealtime());completed();}}
    public void flush(){persist();}
    private boolean persist(){if(store==null)return false;
        try{store.save();saved=SystemClock.elapsedRealtime();return true;}
        catch(IOException e){error="Training save failed. Session paused; use Retry save in Training.";
            if(running())runner().pause(SystemClock.elapsedRealtime());publish();return false;}}
    private void completed(){
        store.history.data.setProperty("pendingEnded",""+Math.max(runner().started,System.currentTimeMillis()));
        try{store.record(-1,-1);visualProfile=null;}catch(IOException e){error="Training result could not be saved. Retry in Training.";}
        publish();host.endTraining();if(foreground&&error==null)feedback();
    }
    private static final int INK=0xffedf4ff,SOFT=0xff9cacc2,TEAL=0xff70dfd8;
    private int dp(float value){return Math.round(value*context.getResources().getDisplayMetrics().density);}
    private LinearLayout column(){LinearLayout l=new LinearLayout(context);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(24),dp(12),dp(24),dp(16));return l;}
    private void text(LinearLayout l,String s){TextView t=new TextView(context);t.setText(s);t.setTextColor(SOFT);t.setTextSize(17);t.setLineSpacing(dp(3),1);t.setPadding(0,dp(6),0,dp(10));l.addView(t);}
    private void heading(LinearLayout l,String s){TextView t=new TextView(context);t.setText(s);t.setTextColor(INK);t.setTextSize(23);t.setTypeface(null,Typeface.BOLD);t.setPadding(0,dp(12),0,dp(8));l.addView(t);}
    private void button(LinearLayout l,String title,Runnable action){action(l,title,false,action);}
    private void primary(LinearLayout l,String title,Runnable action){action(l,title,true,action);}
    private void action(LinearLayout l,String title,boolean primary,Runnable action){Button b=new Button(context);b.setText(title);b.setTextColor(primary?TEAL:INK);b.setTextSize(17);b.setAllCaps(false);b.setMinHeight(dp(52));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(primary?0xff20535b:0xff1c2d45);bg.setCornerRadius(dp(10));b.setBackground(bg);b.setOnClickListener(v->action.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));l.addView(b,lp);}
    private AlertDialog dialog(String title,LinearLayout content){ScrollView scroll=new ScrollView(context){@Override protected void onMeasure(int width,int height){int limit=(int)(getResources().getDisplayMetrics().heightPixels*.66);super.onMeasure(width,View.MeasureSpec.makeMeasureSpec(Math.min(limit,View.MeasureSpec.getSize(height)),View.MeasureSpec.AT_MOST));}};scroll.setFillViewport(false);scroll.addView(content);
        AlertDialog.Builder builder=new AlertDialog.Builder(context).setTitle(title).setView(scroll).setNegativeButton("Back",null);
        if(title.equals("Workout preview"))builder.setPositiveButton("Start workout",null);
        AlertDialog d=builder.create();d.show();
        android.view.Window window=d.getWindow();if(window!=null){android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(0xff101c2f);bg.setCornerRadius(dp(16));window.setBackgroundDrawable(bg);
            android.util.DisplayMetrics metrics=context.getResources().getDisplayMetrics();window.setLayout(Math.min(dp(940),(int)(metrics.widthPixels*.94)),android.view.ViewGroup.LayoutParams.WRAP_CONTENT);}
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TEAL);d.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);if(d.getButton(AlertDialog.BUTTON_POSITIVE)!=null){d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TEAL);d.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);}return d;
    }
    private void tabs(LinearLayout l){LinearLayout row=new LinearLayout(context);String[] titles={"Workouts","Progress","Preferences","Assessments"};for(int i=0;i<4;i++){final int page=i;Button b=new Button(context);b.setAllCaps(false);b.setText(titles[i]);b.setTextSize(16);b.setTextColor(trainingPage==i?TEAL:SOFT);android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(trainingPage==i?0xff20535b:0xff17283e);bg.setCornerRadius(dp(8));b.setBackground(bg);b.setOnClickListener(v->{trainingPage=page;show();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(50),1);lp.setMargins(dp(3),0,dp(3),0);row.addView(b,lp);}l.addView(row);}
    public void openPage(int page){trainingPage=page>=0&&page<=3?page:0;show();}
    public void show(){
        pause();if(panel!=null)panel.dismiss();LinearLayout l=column();
        if(error!=null){heading(l,"Let's save your progress");text(l,error);if(store!=null)primary(l,"Try saving again",()->{if(persist()){error=null;show();}});panel=dialog("Training",l);return;}
        tabs(l);
        if(trainingPage==1){historyContent(l);}
        else if(trainingPage==2){preferences(l);}
        else if(trainingPage==3&&!hasSession()){assessmentContent(l);}
        else if(hasSession()){
            Runner r=runner();heading(l,r.finished?"Workout complete":"Your workout is paused");text(l,r.plan.title()+" · "+duration(r.elapsed)+" of "+duration(r.plan.seconds()));
            text(l,r.finished?"Your row is saved. A quick check-in helps shape the next recommendation.":phaseName(r.current().phase)+" · "+duration(r.current().seconds-r.within)+" left · effort "+r.effort()+"/10");
            if(r.finished)primary(l,"Review your workout",()->{panel.dismiss();feedback();});
            else {primary(l,"Resume workout",()->{panel.dismiss();host.beginTraining(r.id);resume();});button(l,"Lower the effort",()->{r.easier();persist();publish();show();});
                if(r.current().phase==Phase.WORK)button(l,"Shorten this work interval",()->{r.shortenWork();persist();publish();show();});
                if(r.current().phase==Phase.RECOVERY)text(l,"Need more recovery? Leave the workout paused until you're ready.");
                button(l,"End workout…",()->new AlertDialog.Builder(context).setTitle("End this workout?").setMessage("Your rowing and campaign progress will be saved.").setPositiveButton("End and save",(d,w)->{panel.dismiss();stop();}).setNegativeButton("Keep paused",null).show());}
        }else {
            State state=store.history.state();heading(l,"Make time for a row");
            text(l,"Choose your available time. We'll suggest a workout, then show the plan before you start.");
            button(l,"Available time · "+minutes+" minutes",()->timePicker());
            text(l,"Goal: "+(state.goal==Goal.ENDURANCE?"Endurance":"General cardio")+" · "+(state.mode==Mode.COACH?"One suggested workout":"A recommendation with alternatives"));
            primary(l,"Find my workout",()->{panel.dismiss();checkIn(null);});
            heading(l,"Find your starting point");text(l,"Assessments help you compare similar rows over time. They're optional.");button(l,"Browse assessments",()->assessments());
        }
        panel=dialog("OpenRow training",l);
    }
    private void timePicker(){final int[] times={5,10,15,20,30,45};String[] labels=new String[times.length];int selected=0;for(int i=0;i<times.length;i++){labels[i]=times[i]+" minutes";if(times[i]==minutes)selected=i;}
        new AlertDialog.Builder(context).setTitle("How much time do you have?").setSingleChoiceItems(labels,selected,(d,i)->{minutes=times[i];d.dismiss();show();}).setNegativeButton("Back",null).show();}
    private void assessments(){openPage(3);}
    private void assessmentContent(LinearLayout l){
        heading(l,"Choose your assessment");text(l,"New here? Start with the quick baseline. Assessments are optional and help compare similar rows over time.");
        String[] titles={"Quick baseline · 5 min","Full benchmark · 35 min","Endurance starter · 13 min","Steady-work starter · 13 min","Controlled-interval starter · 13 min"};
        String[] details={"A short first assessment to establish a starting point.","A longer assessment to repeat under similar conditions.","Explore a manageable continuous rowing effort.","Explore a manageable sustained effort.","Explore repeated efforts with recovery between them."};
        State state=store.history.state();for(int i=0;i<titles.length;i++){final int choice=i;button(l,titles[i],()->{panel.dismiss();checkIn(assessment(choice==0?Kind.QUICK:choice==1?Kind.BENCHMARK:Kind.STARTER,choice<2?Category.ENDURANCE:Category.values()[choice-2],state.goal));});text(l,details[i]);}
        text(l,"You'll review the effort and timing before starting. The rowing session currently uses the Gravity Well workout display.");
    }
    private void preferences(LinearLayout l){State state=store.history.state();heading(l,"Make training yours");
        button(l,"Goal · "+(state.goal==Goal.ENDURANCE?"Endurance":"General cardio"),()->new AlertDialog.Builder(context).setTitle("Your training goal").setSingleChoiceItems(new String[]{"Build endurance","General cardio"},state.goal==Goal.ENDURANCE?0:1,(d,i)->{store.history.settings(i==0?Goal.ENDURANCE:Goal.GENERAL_CARDIO,state.mode);persist();d.dismiss();show();}).setNegativeButton("Back",null).show());
        button(l,"Workout choices · "+(state.mode==Mode.COACH?"One suggestion":"Show alternatives"),()->new AlertDialog.Builder(context).setTitle("How would you like to choose?").setSingleChoiceItems(new String[]{"Give me one suggested workout","Show a recommendation and alternatives"},state.mode==Mode.COACH?0:1,(d,i)->{store.history.settings(state.goal,i==0?Mode.COACH:Mode.RECOMMENDED);persist();d.dismiss();show();}).setNegativeButton("Back",null).show());
        button(l,"About effort and recommendations",()->new AlertDialog.Builder(context).setTitle("About guided rowing").setMessage("Effort uses a 0–10 scale: 0 is rest and 10 is your hardest effort. You can lower the effort, pause or stop at any time.\n\nThese foundation templates are experimental and await exercise-professional validation. They do not measure VO₂ max or prescribe automatic power targets. Game upgrades never change your workout targets.").setPositiveButton("Got it",null).show());
        heading(l,"Your data");button(l,"Export training history",()->export());button(l,"Delete training history…",()->deleteHistory());}
    private void deleteHistory(){new AlertDialog.Builder(context).setTitle("Delete training history?").setMessage("Removes assessments, training estimates and session history. Your lifetime rowing totals and game career are kept. Finish any active workout first.")
        .setPositiveButton("Delete history",(d,w)->{if(hasSession()){toast("Finish and review your workout first.");return;}Properties backup=new Properties();backup.putAll(store.history.data);store.history.data.clear();visualProfile=null;store.history.data.setProperty("version","1");if(!persist())store.history.data.putAll(backup);show();}).setNegativeButton("Keep history",null).show();}
    private static String categoryLabel(Category c){return c==Category.ENDURANCE?"Endurance":c==Category.SUSTAINED?"Steady work":"Controlled intervals";}
    private static String duration(double seconds){int total=(int)Math.ceil(Math.max(0,seconds));return total%60==0?total/60+" min":total>=60?total/60+" min "+total%60+" sec":total+" sec";}
    private static String phaseName(Phase phase){return phase==Phase.WARM_UP?"Warm-up":phase==Phase.COOL_DOWN?"Cool-down":phase==Phase.RECOVERY?"Recovery":"Work";}
    private void checkIn(Plan selected){
        new AlertDialog.Builder(context).setTitle("How are you feeling today?")
            .setItems(new String[]{"Ready to row","Tired — keep it easy","Rest / defer today"},(d,which)->{
                readiness=Readiness.values()[which];
                if(readiness==Readiness.STOP){toast("Take the time you need. Your progress is saved.");return;}
                State s=store.history.state();
                if(s.returning(System.currentTimeMillis()))new AlertDialog.Builder(context).setTitle("Welcome back — choose your starting point")
                    .setItems(new String[]{"About where I left off","Ease back a little","Ready for a little more","Ease back a lot"},(x,n)->{comeback=ReturnChoice.values()[n];choose(selected);}).setNegativeButton("Back",(cancelDialog,cancelWhich)->show()).show();
                else {comeback=ReturnChoice.MAINTAIN;choose(selected);}
            }).setNegativeButton("Back",(cancelDialog,cancelWhich)->show()).show();
    }
    private void choose(Plan selected){
        State s=store.history.state();
        long at=System.currentTimeMillis();store.history.data.setProperty("input.recommendedAt",""+at);
        Recommendation rec=recommend(s,minutes,readiness,comeback,at);
        if(selected!=null){
            if(readiness!=Readiness.READY||s.returning(System.currentTimeMillis())){
                new AlertDialog.Builder(context).setTitle("Use an easy return session today")
                    .setMessage("Your check-in suggests an easier session. Keep benchmark protocols unchanged so future results remain comparable.")
                    .setPositiveButton("Review easy workout",(d,w)->preview(rec.recommended)).setNegativeButton("Back",(cancelDialog,cancelWhich)->show()).show();
            }else preview(selected);
            return;
        }
        if(s.mode==Mode.COACH){preview(rec.recommended);return;}
        List<Plan> plans=new ArrayList<Plan>();plans.add(rec.recommended);plans.addAll(rec.alternatives);
        String[] names=new String[plans.size()];for(int i=0;i<names.length;i++)names[i]=(i==0?"Recommended: ":"Alternative: ")+categoryLabel(plans.get(i).category)+" · "+plans.get(i).seconds()/60+" min";
        new AlertDialog.Builder(context).setTitle("Today's training").setItems(names,(d,i)->preview(plans.get(i))).setNegativeButton("Back",(cancelDialog,cancelWhich)->show()).show();
    }
    private void preview(Plan plan){LinearLayout l=column();heading(l,plan.title());
        int work=0,recovery=0;for(Block b:plan.blocks){if(b.phase==Phase.WORK)work+=b.seconds;if(b.phase==Phase.RECOVERY)recovery+=b.seconds;}
        text(l,duration(plan.seconds())+" total · "+duration(work)+" work"+(recovery>0?" · "+duration(recovery)+" recovery":""));
        l.addView(new WorkoutStrip(context,plan),new LinearLayout.LayoutParams(-1,dp(32)));
        for(Block b:plan.blocks){LinearLayout row=new LinearLayout(context);row.setPadding(0,dp(8),0,dp(8));TextView phase=new TextView(context);phase.setText(phaseName(b.phase)+" · "+duration(b.seconds));phase.setTextColor(INK);phase.setTextSize(18);row.addView(phase,new LinearLayout.LayoutParams(0,-2,1));TextView effort=new TextView(context);effort.setText("Effort "+b.effort+"/10");effort.setTextColor(b.phase==Phase.WORK?TEAL:SOFT);effort.setTextSize(17);row.addView(effort);l.addView(row);}
        text(l,"0 = rest · 10 = your hardest effort. You can ease off, pause or end the workout at any time.");
        text(l,plan.reason);text(l,plan.provisional?"A starting recommendation that will adapt as you log manageable sessions.":"Based on your recent manageable sessions.");
        if(plan.kind!=Kind.TRAINING)text(l,"For useful comparisons, repeat this assessment with the same rower setup and similar conditions. Modified or interrupted results stay in history but are excluded from comparisons.");
        AlertDialog d=dialog("Workout preview",l);d.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v->{d.dismiss();show();});d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(startWorkout(plan))d.dismiss();});
    }
    private boolean startWorkout(Plan plan){
        if(error!=null)return false;if(!host.trainingAvailable()){toast("End your current rowing session before starting a guided workout.");return false;}
        Runner r=new Runner(UUID.randomUUID().toString(),plan,System.currentTimeMillis(),false);
        store.history.data.setProperty("input.readiness",readiness.name());store.history.data.setProperty("input.return",comeback.name());
        store.history.data.setProperty("audit."+r.id+".readiness",readiness.name());store.history.data.setProperty("audit."+r.id+".return",comeback.name());
        store.history.data.setProperty("audit."+r.id+".availableMinutes",""+minutes);store.history.data.setProperty("audit."+r.id+".mode",store.history.state().mode.name());
        store.history.data.setProperty("audit."+r.id+".recommendedAt",store.history.data.getProperty("input.recommendedAt",""+r.started));
        store.active=r;if(!persist())return false;host.beginTraining(r.id);resume();return true;
    }
    private static final class WorkoutStrip extends View {
        private final Plan plan;private final Paint paint=new Paint(3);WorkoutStrip(Context c,Plan p){super(c);plan=p;setContentDescription("Workout phases, "+p.seconds()/60+" minutes total");}
        @Override protected void onDraw(Canvas canvas){float x=0;for(Block b:plan.blocks){float width=getWidth()*b.seconds/(float)plan.seconds();paint.setColor(b.phase==Phase.WORK?0xff58c4bd:b.phase==Phase.RECOVERY?0xff507491:0xff354b68);canvas.drawRect(x,5,x+Math.max(1,width-3),getHeight()-5,paint);x+=width;}}
    }
    private void feedback(){if(!hasSession()||!runner().finished||feedbackOpen||error!=null)return;feedbackOpen=true;
        LinearLayout l=column();text(l,"How did that row feel? Rate the whole workout from 0 (rest) to 10 (your hardest effort).");
        Spinner effort=new Spinner(context);String[] values=new String[12];values[0]="Prefer not to answer";for(int i=0;i<=10;i++)values[i+1]=""+i;
        effort.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_spinner_dropdown_item,values));l.addView(effort);
        text(l,"Did the planned effort feel manageable?");Spinner manageable=new Spinner(context);
        manageable.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_spinner_dropdown_item,new String[]{"Prefer not to answer","Yes","No"}));l.addView(manageable);
        text(l,"This helps shape your next recommendation. Your row is saved even if you skip.");
        AlertDialog d=new AlertDialog.Builder(context).setTitle("How was your row?").setView(l).setPositiveButton("Save",null)
            .setNegativeButton("Skip for now",(x,w)->{try{store.record(-1,-1);visualProfile=null;store.clearRecorded();}catch(IOException e){error="Feedback save failed. Retry in Training.";toast(error);}}).create();
        d.setOnDismissListener(x->feedbackOpen=false);d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            try{store.record(effort.getSelectedItemPosition()-1,manageable.getSelectedItemPosition()==0?-1:manageable.getSelectedItemPosition()==1?1:0);visualProfile=null;store.clearRecorded();d.dismiss();toast("Training saved.");}
            catch(IOException e){error="Feedback save failed. Retry in Training.";toast(error);}}));d.show();
    }
    private void historyContent(LinearLayout l){List<Record> rows=store.history.records();State state=store.history.state();
        heading(l,"Your training progress");if(rows.isEmpty()){text(l,"No workouts yet. Start a guided row or try a short baseline to build your history.");button(l,"Browse assessments",()->assessments());return;}
        text(l,"These charts show the duration of recent manageable sessions. They are training estimates, not a fitness score. Categories without enough history will stay unestimated.");
        for(Category c:Category.values()){
            int evidence=state.evidence(c,System.currentTimeMillis());Double trend=state.benchmarkChange(c,System.currentTimeMillis());
            text(l,categoryLabel(c)+": "+(evidence==0?"unknown":state.toleranceSeconds(c,System.currentTimeMillis())/60+" min estimated tolerance")+" · "+evidence+" recent observations"+(trend==null?"":String.format(Locale.US," · benchmark work-distance change %+.0f%%",trend*100)));
            if(evidence>0)l.addView(new Trend(context,rows,c),new LinearLayout.LayoutParams(-1,dp(130)));
        }
        text(l,"Benchmark observations below are measured distance, elapsed time, and reported effort. Compare the same protocol at similar effort. Adjusted, interrupted, or poor-quality results are excluded from comparisons.");
        if(rows.isEmpty())text(l,"No training sessions yet.");
        for(int i=rows.size()-1;i>=0;i--){Record r=rows.get(i);
            text(l,DateFormat.getDateInstance().format(new Date(r.ended))+" · "+r.plan.title()+"\n"+(r.outcome==Outcome.COMPLETED?"Completed":"Stopped early")+" · "+(int)r.elapsed+" sec · "+Math.round(r.distance)+" m · effort "+(r.effort<0?"unanswered":r.effort+"/10")+"\n"+r.plan.reason);
            if(r.comparable())for(Category c:Category.values()){
                double observed=r.workDistance(c);if(observed<=0||!r.contributes(c))continue;
                Record previous=null;for(int j=i-1;j>=0;j--){Record p=rows.get(j);if(p.comparable()&&p.contributes(c)&&p.plan.protocol.equals(r.plan.protocol)&&Math.abs(p.effort-r.effort)<=1){previous=p;break;}}
                text(l,categoryLabel(c)+": "+Math.round(observed)+" work meters"+(previous==null?" · first comparable result":" · change "+Math.round(observed-previous.workDistance(c))+" m"));
            }
        }
    }
    private void export(){try{
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain").putExtra(Intent.EXTRA_TITLE,"openrow-training.properties");
        Context owner=context;while(owner instanceof android.content.ContextWrapper&&!(owner instanceof Activity))owner=((android.content.ContextWrapper)owner).getBaseContext();
        ((Activity)owner).startActivityForResult(intent,EXPORT_REQUEST);
    }catch(ActivityNotFoundException e){toast("This tablet has no file picker available for export.");}}
    public void exportTo(Uri uri){if(store==null||uri==null)return;
        try(OutputStream stream=context.getContentResolver().openOutputStream(uri,"wt")){
            if(stream==null)throw new IOException("No export destination");
            store.history.data.store(stream,"OpenRow training export v1; includes policy versions and recommendation inputs");toast("Training history exported.");
        }catch(IOException|SecurityException e){toast("Export could not be saved.");}}
    private void toast(String message){Toast.makeText(context,message,Toast.LENGTH_LONG).show();}
    private static final class Trend extends View {
        private final List<Record> points=new ArrayList<Record>();private final Paint paint=new Paint(3);
        private final Category category;
        Trend(Context context,List<Record> rows,Category c){super(context);category=c;for(Record r:rows)if(r.contributes(c)&&r.success())points.add(r);
            setContentDescription(categoryLabel(c)+" manageable-session duration chart, "+points.size()+" observations");}
        @Override protected void onDraw(Canvas canvas){canvas.drawColor(0xff15263b);paint.setColor(0xff6ce1d0);paint.setStrokeWidth(3);
            if(points.isEmpty()){paint.setTextSize(24);canvas.drawText("Not enough evidence yet",15,65,paint);return;}
            double max=300;for(Record r:points)max=Math.max(max,r.categoryDose(category));float px=0,py=0;
            for(int i=0;i<points.size();i++){float x=70+(getWidth()-90)*i/(float)Math.max(1,points.size()-1);float y=(float)(getHeight()-35-(getHeight()-60)*points.get(i).categoryDose(category)/max);
                if(i>0)canvas.drawLine(px,py,x,y,paint);canvas.drawCircle(x,y,5,paint);px=x;py=y;}
            paint.setTextSize(20);paint.setColor(0xffd1e2eb);canvas.drawText((int)(max/60)+" min",5,22,paint);
            canvas.drawText("Earlier sessions → recent sessions",70,getHeight()-8,paint);
        }
    }
}
