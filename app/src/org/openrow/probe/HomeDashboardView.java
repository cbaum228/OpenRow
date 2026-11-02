package org.openrow.probe;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import java.text.DateFormat;
import java.util.*;

/** Athlete home, detailed totals and earned awards. Native scrolling and accessible buttons. */
public final class HomeDashboardView extends LinearLayout {
    public interface Actions {void play();void connection();void pin();void training(int page);void profiles();void row();void history();void castle();}
    private boolean workoutActive;
    public void setWorkoutActive(boolean value){workoutActive=value;}
    private String profileName="";
    public void setProfileName(String name){profileName=name;}
    private static final int INK=0xffedf4ff,MUTED=0xff9cafc7,ACCENT=0xff6ce1d0,PANEL=0xff15263b;
    private final Actions actions;
    private AthleteHistory history=new AthleteHistory();
    private String error;
    private int page;
    private HoldReset reset;
    public HomeDashboardView(Context context,Actions actions){super(context);this.actions=actions;setOrientation(VERTICAL);setBackgroundColor(0xff091522);setPadding(d(30),d(20),d(30),d(20));}
    private int d(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size,int color){TextView t=new TextView(getContext());t.setText(s);t.setTextColor(color);t.setTextSize(size);return t;}
    private GradientDrawable shape(int color){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(18));return g;}
    private Button button(String title,Runnable action){Button b=new Button(getContext());b.setText(title);b.setAllCaps(false);b.setTextColor(INK);b.setTextSize(18);b.setBackground(shape(0xff24455a));b.setOnClickListener(v->action.run());return b;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(getContext());l.setOrientation(VERTICAL);return l;}
    private LinearLayout card(String title){LinearLayout l=column();l.setPadding(d(26),d(20),d(26),d(20));l.setBackground(shape(PANEL));TextView heading=text(title,17,ACCENT);heading.setTypeface(null,Typeface.BOLD);l.addView(heading);return l;}
    private void gap(LinearLayout l,int height){l.addView(new View(getContext()),new LayoutParams(1,d(height)));}
    private String n(double v){return String.format(Locale.US,"%,.0f",v);}
    private String time(double seconds){long s=(long)seconds;return String.format(Locale.US,"%dh %02dm %02ds",s/3600,(s/60)%60,s%60);}
    private void read(){try{history=AthleteStore.refresh(getContext());error=null;}catch(Exception e){error="Saved athlete data could not be read. Return home to retry.";}}
    public void showHome(){cancelHold();page=0;read();render();}
    public void cancelHold(){if(reset!=null)reset.cancel();}
    public boolean back(){if(page==0)return false;showHome();return true;}
    private void navigate(int p){cancelHold();page=p;read();render();}
    private void header(String title){LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);TextView name=text(title,32,INK);name.setTypeface(null,Typeface.BOLD);row.addView(name,new LayoutParams(0,d(64),1));
        if(page==0){Button pin=button(profileName+" · Profiles",()->actions.profiles());row.addView(pin,new LayoutParams(d(250),d(48)));View space=new View(getContext());row.addView(space,new LayoutParams(d(12),1));row.addView(button("Live Rowing Stats",()->actions.connection()),new LayoutParams(d(210),d(48)));}
        else row.addView(button("Back to home",()->showHome()),new LayoutParams(d(200),d(48)));
        addView(row);gap(this,14);
    }
    private void render(){
        removeAllViews();reset=null;header(page==0?"OpenRow":page==1?"Your lifetime rowing":"Milestones & Trophies");
        if(error!=null){addView(text(error,22,0xffffb1a8));return;}
        if(page==1){details();return;}if(page==2){awards();return;}
        LinearLayout columns=new LinearLayout(getContext());addView(columns,new LayoutParams(-1,0,1));
        LinearLayout left=column();LayoutParams lp=new LayoutParams(0,-1,1);lp.setMargins(0,0,d(24),0);columns.addView(left,lp);
        LinearLayout last=card("LAST WORKOUT");LayoutParams top=new LayoutParams(-1,0,4);top.setMargins(0,0,0,d(14));left.addView(last,top);
        if(history.data.containsKey("last.id")){
            gap(last,12);last.addView(text(history.data.getProperty("last.game","Gravity Well"),26,INK));
            long ended=(long)history.value("last.ended");last.addView(text(ended>0?DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(new Date(ended)):"Previous saved workout",15,MUTED));
            gap(last,22);metricRow(last,new String[]{n(history.value("last.distance"))+" m",n(history.value("last.strokes")),time(history.value("last.seconds"))},new String[]{"Distance","Strokes","Motion time"});
        }else{gap(last,26);last.addView(text("Your next row starts here.",27,INK));gap(last,14);last.addView(text("Finish a real rowing session to see its summary here.",18,MUTED));gap(last,10);last.addView(text("Test sessions stay in the test career.",16,MUTED));}
        LinearLayout lifetime=card("LIFETIME STATS");LayoutParams middle=new LayoutParams(-1,0,4);middle.setMargins(0,0,0,d(14));left.addView(lifetime,middle);gap(lifetime,28);
        metricRow(lifetime,new String[]{n(history.value("total.distance"))+" m",n(history.value("total.strokes")),n(history.value("total.workouts"))},new String[]{"Distance","Strokes","Finished workouts"});gap(lifetime,24);
        lifetime.addView(text(time(history.value("total.motionSeconds"))+"  in rowing motion",23,INK));gap(lifetime,12);lifetime.addView(text(history.value("resetAt")>0?"Totals since your last reset":"Your recorded real rowing, across sessions",16,MUTED));
        LinearLayout links=new LinearLayout(getContext());links.setGravity(Gravity.CENTER_VERTICAL);left.addView(links,new LayoutParams(-1,0,2));
        LayoutParams b=new LayoutParams(0,d(88),1);b.setMargins(0,0,d(12),0);links.addView(button("Detailed Lifetime Stats",()->navigate(1)),b);links.addView(button("Milestones & Trophies",()->navigate(2)),new LayoutParams(0,d(88),1));
        LinearLayout right=column();columns.addView(right,new LayoutParams(0,-1,1));right.addView(text("YOUR ROWING",18,ACCENT));gap(right,14);
        ScrollView games=new ScrollView(getContext());games.setFillViewport(true);right.addView(games,new LayoutParams(-1,0,1));LinearLayout list=column();games.addView(list);
        LinearLayout recording=card("YOUR WORKOUTS");list.addView(recording);recording.addView(button(workoutActive?"Resume saved workout":"Start Row · without a game",()->actions.row()),new LayoutParams(-1,d(58)));gap(recording,8);recording.addView(button("Workout history & charts",()->actions.history()),new LayoutParams(-1,d(58)));gap(list,18);
        LinearLayout trainingCard=card("TRAINING & ASSESSMENTS");list.addView(trainingCard,new LayoutParams(-1,-2));gap(trainingCard,8);
        trainingCard.addView(text("Choose a guided workout, establish a baseline, or review your progress.",18,MUTED));gap(trainingCard,8);
        trainingCard.addView(button("Assessments · find your starting point",()->actions.training(3)),new LayoutParams(-1,d(58)));gap(trainingCard,10);
        LinearLayout trainingLinks=new LinearLayout(getContext());LayoutParams trainingLink=new LayoutParams(0,d(54),1);trainingLink.setMargins(0,0,d(10),0);
        trainingLinks.addView(button("Guided workouts",()->actions.training(0)),trainingLink);trainingLinks.addView(button("Training progress",()->actions.training(1)),new LayoutParams(0,d(54),1));trainingCard.addView(trainingLinks);gap(list,18);
        LinearLayout gameCard=card("GRAVITY WELL");list.addView(gameCard,new LayoutParams(-1,-2));gap(gameCard,12);gameCard.addView(new GravityArt(getContext()),new LayoutParams(-1,d(130)));gap(gameCard,18);
        gameCard.addView(text("Build your own singularity",29,INK));gap(gameCard,14);gameCard.addView(text("Draw in dust and capture orbiting worlds. Strip away their matter, earn XP, and grow your gravity well.",20,MUTED));gap(gameCard,20);gameCard.addView(text("5 or 30 minutes  •  Saved progression",17,ACCENT));gap(gameCard,25);gameCard.addView(button("Play Gravity Well",()->actions.play()),new LayoutParams(-1,d(64)));gap(list,20);list.addView(button("Play Castlebreaker",()->actions.castle()),new LayoutParams(-1,d(64)));
    }
    private void metricRow(LinearLayout parent,String[] values,String[] names){LinearLayout row=new LinearLayout(getContext());for(int i=0;i<values.length;i++){LinearLayout cell=column();TextView value=text(values[i],i==2&&values[i].contains("h")?22:30,INK);cell.addView(value);gap(cell,8);cell.addView(text(names[i],16,MUTED));row.addView(cell,new LayoutParams(0,-2,1));}parent.addView(row);}
    private void details(){
        ScrollView scroll=new ScrollView(getContext());addView(scroll,new LayoutParams(-1,0,1));LinearLayout content=column();scroll.addView(content);LinearLayout totals=card("RECORDED ACTIVITY");content.addView(totals);
        row(totals,"Rowing distance",n(history.value("total.distance"))+" meters");row(totals,"Accepted stroke starts",n(history.value("total.strokes"))+" strokes");row(totals,"Rowing motion time",time(history.value("total.motionSeconds")));row(totals,"Finished workouts",n(history.value("total.workouts")));
        row(totals,"S4 estimated mechanical energy",String.format(Locale.US,"%.2f kJ",history.value("total.energyJ")/1000));
        row(totals,"Average over valid power samples",history.value("total.powerSeconds")>0?String.format(Locale.US,"%.1f W",history.value("total.energyJ")/history.value("total.powerSeconds")):"No valid power samples yet");
        row(totals,"Power sample coverage",time(history.value("total.powerSeconds")));
        row(totals,"S4 estimated calories (provisional)",String.format(Locale.US,"%.2f kcal",history.value("total.estimatedCalories")));
        row(totals,"Recorded sensor pulses",n(history.value("total.pulses")));
        row(totals,"Average detected drive duration",history.value("total.driveCount")>0?String.format(Locale.US,"%.2f s",history.value("total.driveSeconds")/history.value("total.driveCount")):"No completed drives yet");
        gap(content,18);content.addView(text("S4 power and calories are monitor estimates. Energy integrates valid watts only; missing samples and earlier workouts are excluded. Calorie scaling is provisional until compared with your monitor. Watts are never added together as a lifetime total.",17,MUTED));
        if(history.data.containsKey("last.sensor")){gap(content,18);LinearLayout recent=card("LAST WORKOUT · S4 CHANNELS");content.addView(recent);row(recent,"Estimated energy",String.format(Locale.US,"%.2f kJ",history.value("last.energyJ")/1000));row(recent,"Power coverage",time(history.value("last.powerSeconds")));row(recent,"Peak sampled power",history.value("last.powerSeconds")>0?n(history.value("last.peakPower"))+" W":"—");row(recent,"Calorie estimate",String.format(Locale.US,"%.2f kcal (provisional)",history.value("last.estimatedCalories")));row(recent,"Peak received heart rate",history.value("last.peakHeartRate")>0?n(history.value("last.peakHeartRate"))+" bpm":"No HR received");}
        gap(content,18);
        content.addView(text("Motion time is estimated from recent S4 distance updates. Pauses, menus and test sessions are excluded. Recorded workouts cover games and standalone rowing. Older unrecorded Castlebreaker sessions are not imported.",17,MUTED));
        if(history.value("resetAt")>0){gap(content,12);content.addView(text("Last reset: "+DateFormat.getDateTimeInstance().format(new Date((long)history.value("resetAt"))),17,MUTED));}
        gap(this,18);LinearLayout resetBox=card("RESET LIFETIME STATS");addView(resetBox,new LayoutParams(-1,-2));gap(resetBox,10);resetBox.addView(text("Clears all athlete totals, last workout, milestones, badges and trophies. Gravity Well career progress is kept.",17,MUTED));gap(resetBox,12);
        reset=new HoldReset(getContext(),()->{try{history=AthleteStore.reset(getContext());Toast.makeText(getContext(),"Athlete stats and awards reset",Toast.LENGTH_SHORT).show();render();}catch(Exception e){Toast.makeText(getContext(),"Reset could not be saved. Your stats were kept.",Toast.LENGTH_LONG).show();}});resetBox.addView(reset,new LayoutParams(-1,d(70)));
    }
    private void row(LinearLayout l,String name,String value){LinearLayout r=new LinearLayout(getContext());r.setPadding(0,d(17),0,d(17));r.addView(text(name,20,MUTED),new LayoutParams(0,-2,1));r.addView(text(value,22,INK),new LayoutParams(0,-2,1));l.addView(r);}
    private void awards(){ScrollView scroll=new ScrollView(getContext());addView(scroll,new LayoutParams(-1,0,1));LinearLayout content=column();scroll.addView(content);int earned=0;for(String[] b:AthleteHistory.BADGES)if(history.earned(b[0]))earned++;content.addView(text(earned+" earned  •  Your rowing achievements",23,INK));gap(content,20);
        for(String[] b:AthleteHistory.BADGES){boolean unlocked=history.earned(b[0]);LinearLayout card=card((unlocked?"EARNED  ·  ":"UPCOMING  ·  ")+b[1]);content.addView(card,new LayoutParams(-1,-2));gap(card,10);card.addView(text(b[2],21,INK));gap(card,8);card.addView(text(unlocked?"Achievement unlocked":n(Math.min(history.value("total."+b[3]),Double.parseDouble(b[4])))+" / "+n(Double.parseDouble(b[4])),17,unlocked?ACCENT:MUTED));gap(content,14);}
        content.addView(text("Game trophies will appear here as games add trophy challenges.",17,MUTED));
    }
    private static final class GravityArt extends View {
        final Paint p=new Paint(3);GravityArt(Context c){super(c);setContentDescription("A black hole surrounded by a glowing accretion disk");}
        private final SingularityRenderer singularity=new SingularityRenderer();
        @Override protected void onDraw(Canvas c){float x=getWidth()*.5f,y=getHeight()*.5f,r=Math.min(getWidth(),getHeight())*.23f;for(int i=0;i<70;i++){p.setColor(0xff4c6784);c.drawCircle((i*137%997)/997f*getWidth(),(i*i*31%419)/419f*getHeight(),i%8==0?2:1,p);}singularity.draw(c,x,y,r,0,.3,false);}
    }
    private static final class HoldReset extends View {
        final AthleteHistory.Hold hold=new AthleteHistory.Hold();final Paint p=new Paint(3);final Runnable action;boolean touching;int pointer=-1;
        final Runnable tick=new Runnable(){public void run(){if(!touching)return;if(hold.complete(SystemClock.elapsedRealtime())){touching=false;pointer=-1;action.run();return;}invalidate();postDelayed(this,40);}};
        HoldReset(Context c,Runnable action){super(c);this.action=action;setContentDescription("Hold continuously for five seconds to reset lifetime stats. Release or slide away to cancel.");}
        void cancel(){touching=false;pointer=-1;hold.cancel();removeCallbacks(tick);invalidate();}
        @Override protected void onDetachedFromWindow(){cancel();super.onDetachedFromWindow();}
        @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(!focus)cancel();}
        @Override protected void onDraw(Canvas c){double progress=hold.progress(SystemClock.elapsedRealtime());p.setColor(0xff462c38);c.drawRoundRect(0,0,getWidth(),getHeight(),14,14,p);p.setColor(0xff8d4352);c.drawRoundRect(0,0,(float)(getWidth()*progress),getHeight(),14,14,p);p.setColor(0xffffe6e6);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(20*getResources().getDisplayMetrics().scaledDensity);c.drawText(touching?"Keep holding · "+(int)Math.ceil(5-progress*5)+" seconds":"Hold 5 seconds to reset lifetime stats",getWidth()/2f,getHeight()/2f-p.ascent()/2-p.descent()/2,p);}
        @Override public boolean onTouchEvent(MotionEvent e){int a=e.getActionMasked();if(a==MotionEvent.ACTION_DOWN){pointer=e.getPointerId(0);touching=true;hold.start(SystemClock.elapsedRealtime());getParent().requestDisallowInterceptTouchEvent(true);post(tick);return true;}if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_CANCEL||a==MotionEvent.ACTION_POINTER_DOWN){cancel();return true;}if(a==MotionEvent.ACTION_MOVE){int index=e.findPointerIndex(pointer);if(index<0||e.getX(index)<0||e.getY(index)<0||e.getX(index)>getWidth()||e.getY(index)>getHeight())cancel();return true;}return true;}
    }
}
