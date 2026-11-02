package org.openrow.probe;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.Locale;

/** Native campaign planning, rest-stop and summary screens. */
public final class CampaignSetupView extends LinearLayout {
    public interface Actions {
        void start(CampaignSession.Plan plan,boolean test);void resume();void freePlay();void rowing();void setup();void continueRest(boolean skip);
    }
    private final Actions actions;
    private boolean byTime,test,active=true,progress,settingsInitialized;
    private int goalIndex=1,pace=150,rest;
    private CampaignSession session;
    private TextView estimate,paceLabel,countdown;
    private Button continueButton;
    private final Handler handler=new Handler();
    private long lastRestFrame;
    private static final int INK=Color.rgb(239,237,218),MUTED=Color.rgb(166,185,177),BG=Color.rgb(23,35,39),GOLD=Color.rgb(240,183,77);
    private final Runnable tick=new Runnable(){public void run(){
        if(active && progress && getVisibility()==VISIBLE && session!=null && session.phase==CampaignSession.Phase.REST){
            long now=SystemClock.elapsedRealtime();if(lastRestFrame!=0)session.tick(now-lastRestFrame);lastRestFrame=now;refreshRest();
        }else lastRestFrame=0;
        if(isAttachedToWindow())handler.postDelayed(this,250);
    }};
    public CampaignSetupView(Context context,Actions actions){
        super(context);this.actions=actions;setOrientation(VERTICAL);setBackgroundColor(BG);
        android.content.SharedPreferences prefs=context.getSharedPreferences("openrow_campaign",Context.MODE_PRIVATE);
        if(prefs.contains("setup_goal")){settingsInitialized=true;byTime=prefs.getBoolean("setup_time",false);test=prefs.getBoolean("setup_test",false);goalIndex=Math.max(0,Math.min(2,prefs.getInt("setup_goal",1)));pace=Math.max(90,Math.min(300,prefs.getInt("setup_pace",150)));rest=prefs.getInt("setup_rest",0);if(rest!=30&&rest!=60)rest=0;}
    }
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();handler.post(tick);}
    @Override protected void onDetachedFromWindow(){handler.removeCallbacks(tick);super.onDetachedFromWindow();}
    public void active(boolean value){active=value;lastRestFrame=0;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView label(String text,int size,int color){TextView t=new TextView(getContext());t.setText(text);t.setTextSize(size);t.setTextColor(color);return t;}
    private GradientDrawable panel(){GradientDrawable d=new GradientDrawable();d.setColor(Color.rgb(39,56,58));d.setCornerRadius(dp(13));return d;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(getContext());l.setOrientation(VERTICAL);return l;}
    private void space(LinearLayout parent,int h){parent.addView(new View(getContext()),new LinearLayout.LayoutParams(1,dp(h)));}
    private Button button(LinearLayout parent,String text,Runnable click,boolean selected){
        Button b=new Button(getContext());b.setText(text);b.setTextSize(17);b.setAllCaps(false);b.setTextColor(selected?BG:INK);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selected?GOLD:Color.rgb(54,74,74)));
        b.setOnClickListener(v -> click.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(58));p.setMargins(0,0,dp(10),0);parent.addView(b,p);return b;
    }
    private void title(String text,String sub){removeAllViews();setPadding(dp(38),dp(26),dp(38),dp(24));TextView heading=label(text,32,INK);heading.setTypeface(null,Typeface.BOLD);addView(heading);space(this,5);addView(label(sub,17,MUTED));space(this,20);}
    private CampaignSession.Plan plan(){int distance=new int[]{500,1000,2000}[goalIndex],minutes=new int[]{15,30,45}[goalIndex];return new CampaignSession.Plan(byTime,distance,minutes,pace,rest);}
    private void section(LinearLayout parent,String heading){space(parent,14);TextView t=label(heading,16,MUTED);parent.addView(t);space(parent,6);}
    public void showSetup(CampaignSession current){
        if(!settingsInitialized && current!=null){byTime=current.plan.byTime;pace=current.plan.paceSeconds;rest=current.plan.restSeconds;test=current.simulated;int goal=byTime?current.plan.minutes:current.plan.meters;int[] choices=byTime?new int[]{15,30,45}:new int[]{500,1000,2000};for(int i=0;i<3;i++)if(choices[i]==goal)goalIndex=i;}
        settingsInitialized=true;
        progress=false;session=current;lastRestFrame=0;
        title("Castlebreaker campaign",current==null?"Plan your rowing session • Four forts in order • Rest between encounters":String.format(Locale.US,"Saved %s campaign: %,d m • Resume keeps that plan. Choices below start a new campaign.",current.simulated?"TEST":"rowing",current.plan.plannedMeters()));
        LinearLayout columns=new LinearLayout(getContext());addView(columns,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout settings=column();settings.setPadding(dp(22),dp(8),dp(22),dp(16));settings.setBackground(panel());
        LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-1,1);left.setMargins(0,0,dp(22),0);columns.addView(settings,left);
        section(settings,"PLAN BY");LinearLayout type=new LinearLayout(getContext());settings.addView(type);
        button(type,"Distance",()->{byTime=false;showSetup(session);},!byTime);button(type,"Time estimate",()->{byTime=true;showSetup(session);},byTime);
        section(settings,byTime?"ESTIMATED ROWING TIME":"PLANNED ROWING DISTANCE");LinearLayout goals=new LinearLayout(getContext());settings.addView(goals);
        String[] values=byTime?new String[]{"15 min","30 min","45 min"}:new String[]{"500 m","1,000 m","2,000 m"};
        for(int i=0;i<3;i++){final int choice=i;button(goals,values[i],()->{goalIndex=choice;showSetup(session);},goalIndex==i);}
        section(settings,"PLANNING PACE / 500 M");LinearLayout paceRow=new LinearLayout(getContext());paceRow.setGravity(Gravity.CENTER_VERTICAL);settings.addView(paceRow);
        button(paceRow,"−",()->{pace=Math.max(90,pace-15);refreshEstimate();},false);paceLabel=label("",24,INK);paceRow.addView(paceLabel,new LinearLayout.LayoutParams(dp(145),-2));
        button(paceRow,"+",()->{pace=Math.min(300,pace+15);refreshEstimate();},false);
        section(settings,"BETWEEN FORTS");LinearLayout rests=new LinearLayout(getContext());settings.addView(rests);
        for(int seconds:new int[]{0,30,60})button(rests,seconds==0?"As needed":seconds+" sec",()->{rest=seconds;showSetup(session);},rest==seconds);
        section(settings,"INPUT FOR A NEW CAMPAIGN");LinearLayout inputs=new LinearLayout(getContext());settings.addView(inputs);
        button(inputs,"S4 rowing",()->{test=false;showSetup(session);},!test);button(inputs,"Test mode",()->{test=true;showSetup(session);},test);
        LinearLayout route=column();columns.addView(route,new LinearLayout.LayoutParams(0,-1,1));
        for(int i=0;i<4;i++){
            LinearLayout card=column();card.setPadding(dp(20),dp(12),dp(20),dp(12));card.setBackground(panel());
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,0,1);cp.setMargins(0,0,0,dp(10));route.addView(card,cp);
            String marker=current!=null&&i<current.completedForts?"✓":""+(i+1);
            card.addView(label(marker+"   "+CastlebreakerGame.FORT_NAMES[i],22,INK));card.addView(label(CastlebreakerGame.FORT_HINTS[i],15,MUTED));
        }
        estimate=label("",17,INK);estimate.setPadding(0,dp(12),0,dp(10));route.addView(estimate);refreshEstimate();
        space(this,16);
        addView(label("Clear all four forts to finish. Aiming, misses and rests add time. Saved between forts; an unfinished fort restarts if the app closes.",16,MUTED));
        space(this,10);LinearLayout buttons=new LinearLayout(getContext());addView(buttons);
        if(current!=null)button(buttons,current.phase==CampaignSession.Phase.COMPLETE?"View last results":current.simulated?"Resume TEST campaign":"Resume campaign",actions::resume,false);
        button(buttons,current==null?"Start campaign":"Start new campaign",()->actions.start(plan(),test),true);
        button(buttons,"Free play",actions::freePlay,false);button(buttons,"‹ Rowing",actions::rowing,false);
    }
    private void refreshEstimate(){
        CampaignSession.Plan p=plan();paceLabel.setText(String.format(Locale.US,"%d:%02d",pace/60,pace%60));
        getContext().getSharedPreferences("openrow_campaign",Context.MODE_PRIVATE).edit().putBoolean("setup_time",byTime).putBoolean("setup_test",test).putInt("setup_goal",goalIndex).putInt("setup_pace",pace).putInt("setup_rest",rest).apply();
        estimate.setText(String.format(Locale.US,"Plan: %,d m • about %.0f min rowing\nStandard shot: %.0f m · Siege shot: %.0f m\nYour plan scales the winch; aiming and shot physics stay the same.",p.plannedMeters(),p.estimatedRowingMinutes(),10*p.chargeScale(),20*p.chargeScale()));
    }
    public void showProgress(CampaignSession current){
        progress=true;session=current;lastRestFrame=0;
        boolean complete=current.phase==CampaignSession.Phase.COMPLETE;
        title(complete?"Campaign complete":"Fort captured — take a breather",current.simulated?"TEST CAMPAIGN • Distance and results are simulated":"Castlebreaker campaign • Your checkpoint is saved");
        LinearLayout metrics=new LinearLayout(getContext());addView(metrics,new LinearLayout.LayoutParams(-1,dp(175)));
        stat(metrics,"FORTS CAPTURED",current.completedForts+" / 4");stat(metrics,"DISTANCE ROWED",String.format(Locale.US,"%,.0f m",current.distanceMeters));stat(metrics,"SHOTS FIRED",""+current.shots);stat(metrics,"ACTIVE PLAY TIME",clock(current.activeMillis));
        space(this,22);
        LinearLayout route=column();addView(route,new LinearLayout.LayoutParams(-1,0,1));
        for(int i=0;i<4;i++){TextView item=label((i<current.completedForts?"✓  ":"     ")+CastlebreakerGame.FORT_NAMES[i],24,i<current.completedForts?GOLD:MUTED);item.setPadding(dp(12),dp(6),0,dp(6));route.addView(item);}
        addView(label(String.format(Locale.US,"Planned: %,d m • %.0f min estimated rowing. Active play includes aiming; rests are excluded.",current.plan.plannedMeters(),current.plan.estimatedRowingMinutes()),17,MUTED));space(this,14);
        countdown=label("",22,INK);addView(countdown);space(this,12);
        LinearLayout buttons=new LinearLayout(getContext());addView(buttons);
        if(!complete){continueButton=button(buttons,"Continue",()->actions.continueRest(false),true);if(current.plan.restSeconds>0)button(buttons,"Skip rest",()->actions.continueRest(true),false);}
        else continueButton=null;
        button(buttons,"Campaign setup",actions::setup,false);button(buttons,"‹ Rowing",actions::rowing,false);refreshRest();
    }
    private void stat(LinearLayout parent,String name,String value){LinearLayout box=column();box.setGravity(Gravity.CENTER_VERTICAL);box.setPadding(dp(20),dp(18),dp(20),dp(18));box.setBackground(panel());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);p.setMargins(0,0,dp(12),0);parent.addView(box,p);box.addView(label(name,14,MUTED));box.addView(label(value,38,INK));}
    private void refreshRest(){
        if(countdown==null || session==null)return;
        if(session.phase==CampaignSession.Phase.COMPLETE){countdown.setText("All four forts captured. Your results are saved on this tablet.");return;}
        countdown.setText(session.restMillis>0?"Rest: "+clock(session.restMillis+999):"Rest as long as you like. Continue when you are ready.");
        if(continueButton!=null){boolean ready=session.restMillis==0;continueButton.setEnabled(ready);continueButton.setAlpha(ready?1f:.45f);continueButton.setText(ready?"Continue to "+CastlebreakerGame.FORT_NAMES[session.currentFort()]:"Continue in "+clock(session.restMillis+999));}
    }
    private static String clock(long millis){long seconds=millis/1000;return String.format(Locale.US,"%d:%02d",seconds/60,seconds%60);}
}
