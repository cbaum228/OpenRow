package org.openrow.probe;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.widget.*;
import java.util.*;
import java.text.DateFormat;

/** Standalone recording and shared workout history. Native buttons, scrollable charts. */
public final class WorkoutDashboardView extends LinearLayout {
    public interface Actions {void back();void resume();void pause();void finish();void original();void connect();void guided();}
    private final WorkoutSessionController controller;private final Actions actions;
    private TextView live;private boolean history;
    private int d(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    public WorkoutDashboardView(Context c,WorkoutSessionController controller,Actions actions){super(c);this.controller=controller;this.actions=actions;setOrientation(VERTICAL);setPadding(d(28),d(16),d(28),d(16));setBackgroundColor(0xff091522);}
    private TextView text(String s,int size){TextView v=new TextView(getContext());v.setText(s);v.setTextSize(size);v.setTextColor(0xffe3efff);v.setPadding(0,d(8),0,d(8));return v;}
    private Button button(String s,Runnable run){Button b=new Button(getContext());b.setText(s);b.setAllCaps(false);b.setOnClickListener(v->run.run());return b;}
    private LinearLayout body(String title){removeAllViews();LinearLayout top=new LinearLayout(getContext());top.addView(text(title,27),new LayoutParams(0,-2,1));top.addView(button("Home",()->actions.back()));addView(top);ScrollView scroll=new ScrollView(getContext());addView(scroll,new LayoutParams(-1,0,1));LinearLayout content=new LinearLayout(getContext());content.setOrientation(VERTICAL);scroll.addView(content);return content;}
    public void showRow(){history=false;LinearLayout l=body("Your row");live=text("",23);l.addView(live);l.addView(text("S4 estimated watts · missing readings stay unavailable. Mechanical energy uses valid samples only.",16));
        LinearLayout row=new LinearLayout(getContext());row.addView(button(controller.hasActive()?"Resume rowing":"Start rowing",()->actions.resume()));row.addView(button("Pause",()->actions.pause()));row.addView(button("Finish row",()->actions.finish()));row.addView(button("Connect S4",()->actions.connect()));l.addView(row);
        if(controller.hasActive()&&!controller.owns("Standalone rowing"))l.addView(button("Resume in original game",()->actions.original()));l.addView(button(controller.guided==null?"Choose a guided workout":"Adjust guided workout",()->actions.guided()));l.addView(button("Workout history & charts",()->showHistory()));refresh();
    }
    private static String value(double v,String unit){return Double.isFinite(v)?String.format(Locale.US,"%.1f %s",v,unit):"—";}
    public void refresh(){if(history||live==null)return;WorkoutSession s=controller.active;RowingTelemetryFrame f=controller.telemetry;
        String now=f==null?"Connect your S4 to see live readings.":"Power  "+value(f.watts,"W")+"     Split  "+(f.speed>0?value(500/f.speed,"s / 500m"):"—")+"     Cadence  "+value(f.cadence,"spm");
        live.setText((s==null?"Ready to start":s.paused()?"Paused · saved":"Recording")+"\n"+now+"\n"+(s==null?"":String.format(Locale.US,"%.0f m     %.0f strokes     %.0f seconds in motion\n%.0f seconds elapsed · %.2f kJ mechanical energy",s.total("distance"),s.total("strokes"),s.total("motionSeconds"),s.number("elapsed"),s.total("energyJ")/1000))+"\nGame input: "+controller.effort.source+(controller.guided==null?"":"\n"+controller.guided.phase+" · "+Math.round(controller.guided.remainingBlockSeconds)+" seconds left · effort "+controller.guided.effort+"/10")+(controller.error==null?"":"\n"+controller.error));
        if(controller.error!=null){live.setOnClickListener(v->{controller.save();refresh();});}
    }
    public void showHistory(){history=true;LinearLayout l=body("Workout history");try{List<Properties> rows=controller.store.history();if(rows.isEmpty())l.addView(text("Your completed workouts will appear here. Earlier workouts keep their existing totals; detailed timelines start with this release.",19));
        for(Properties p:rows){String title=DateFormat.getDateTimeInstance().format(new Date(Long.parseLong(p.getProperty("started","0"))))+" · "+p.getProperty("origin");l.addView(button(title,()->details(p)));}
    }catch(Exception bad){l.addView(text("History could not be read. Original files were kept.",20));}}
    private void details(Properties p){LinearLayout l=body(p.getProperty("origin","Workout"));history=true;try{
        WorkoutSession s=new WorkoutSession(p);double covered=s.total("powerSeconds"),motion=s.total("motionSeconds");
        l.addView(text(String.format(Locale.US,"%.0f m · %.0f strokes · %.0f seconds in motion\nObserved average power: %s · sampled peak: %s\n%.2f kJ mechanical energy · %.0f seconds power coverage (%.0f%% of motion)",s.total("distance"),s.total("strokes"),motion,covered>0?value(s.total("energyJ")/covered,"W"):"—",covered>0?value(s.number("peakPower"),"W"):"—",s.total("energyJ")/1000,covered,motion>0?Math.min(100,covered/motion*100):0),20));
        l.addView(text("Average observed pace: "+(s.total("paceDistance")>0?value(500*s.total("paceSeconds")/s.total("paceDistance"),"s / 500m"):"—")+" · average observed cadence: "+(s.total("cadenceCount")>0?value(s.total("cadenceSum")/s.total("cadenceCount"),"spm"):"—")+"\nProvisional S4 calories: "+("true".equals(p.getProperty("caloriesObserved"))?value(s.total("estimatedCalories"),"kcal"):"Unavailable"),17));
        if(s.total("driveCount")>0)l.addView(text("Detected drive: "+value(s.total("driveSeconds")/s.total("driveCount"),"s")+" · recovery: "+(s.total("recoveryCount")>0?value(s.total("recoverySeconds")/s.total("recoveryCount"),"s"):"—"),17));
        if("true".equals(p.getProperty("legacyPartial")))l.addView(text("Resumed from an earlier version: the timeline covers only newly recorded samples.",17));
        l.addView(button("Optional · how hard did this row feel?",()->{String[] labels=new String[10];for(int i=0;i<10;i++)labels[i]=(i+1)+" / 10";new android.app.AlertDialog.Builder(getContext()).setTitle("Overall perceived effort").setItems(labels,(dialog,which)->{try{p.setProperty("effort",""+(which+1));controller.store.save(new WorkoutSession(p));details(p);}catch(Exception bad){Toast.makeText(getContext(),"Feedback could not be saved. Please retry.",Toast.LENGTH_LONG).show();}}).setNegativeButton("Skip",null).show();}));
        List<String[]> samples=controller.store.samples(s.id());for(int metric=4;metric<=6;metric++){l.addView(text(metric==4?"Power · W":metric==5?"Pace · seconds / 500m":"Cadence · strokes / min",20));l.addView(new Chart(getContext(),samples,metric),new LayoutParams(-1,d(180)));}
        Set<Integer> blocks=new TreeSet<>();for(String key:p.stringPropertyNames())if(key.matches("block\\.[0-9]+\\.motionSeconds"))blocks.add(Integer.parseInt(key.split("\\.")[1]));
        for(int b:blocks){double seconds=s.number("block."+b+".powerSeconds"),meters=s.number("block."+b+".distance"),paceDistance=s.number("block."+b+".paceDistance");String phase="";for(String[] sample:samples)if(sample[1].equals(""+b)){phase=" · "+sample[2].replace('_',' ').toLowerCase(Locale.US);break;}l.addView(text("Interval "+(b+1)+phase+" · "+Math.round(meters)+" m · "+(paceDistance>0?value(500*s.number("block."+b+".paceSeconds")/paceDistance,"s / 500m"):"—")+" · "+(seconds>0?value(s.number("block."+b+".energyJ")/seconds,"W observed"):"power unavailable")+" · "+Math.round(seconds)+" s power coverage",17));}
        l.addView(button("Back to history",()->showHistory()));
    }catch(Exception bad){l.addView(text("This workout could not be read. Original files were kept.",20));}}
    private static final class Chart extends View {
        final List<String[]> rows;final int metric;final Paint paint=new Paint(3);
        Chart(Context c,List<String[]> rows,int metric){super(c);this.rows=rows;this.metric=metric;setContentDescription(metric==4?"Workout power chart":metric==5?"Workout pace chart":"Workout cadence chart");}
        double y(String[] row){try{double v=Double.parseDouble(row[metric]);return metric==5?(v>0?500/v:Double.NaN):v;}catch(Exception bad){return Double.NaN;}}
        @Override protected void onDraw(Canvas c){c.drawColor(0xff15263b);if(rows.isEmpty())return;double max=1,duration=1;for(String[] r:rows){double v=y(r);if(Double.isFinite(v))max=Math.max(max,v);duration=Math.max(duration,Double.parseDouble(r[0]));}
            // One min/max column per pixel. Missing/paused samples interrupt the trace.
            int width=Math.max(1,getWidth()-80);double[] lo=new double[width],hi=new double[width];Arrays.fill(lo,Double.POSITIVE_INFINITY);Arrays.fill(hi,Double.NEGATIVE_INFINITY);
            for(String[] r:rows){double v=y(r);if(!Double.isFinite(v)||r[3].equals("paused"))continue;int x=Math.min(width-1,(int)(Double.parseDouble(r[0])/duration*(width-1)));lo[x]=Math.min(lo[x],v);hi[x]=Math.max(hi[x],v);}
            paint.setColor(0xff70dfd8);paint.setStrokeWidth(2);for(int x=0;x<width;x++)if(Double.isFinite(lo[x])){float bottom=(float)(getHeight()-28-lo[x]/max*(getHeight()-55)),top=(float)(getHeight()-28-hi[x]/max*(getHeight()-55));c.drawLine(60+x,top,60+x,Math.max(top+2,bottom),paint);}
            double previousTime=-1,previousValue=Double.NaN;for(String[] r:rows){double t=Double.parseDouble(r[0]),v=y(r);if(r[3].equals("paused")||!Double.isFinite(v)){previousValue=Double.NaN;continue;}if(Double.isFinite(previousValue)&&t>previousTime&&t-previousTime<=1.5)c.drawLine((float)(60+previousTime/duration*(width-1)),(float)(getHeight()-28-previousValue/max*(getHeight()-55)),(float)(60+t/duration*(width-1)),(float)(getHeight()-28-v/max*(getHeight()-55)),paint);previousTime=t;previousValue=v;}
            paint.setColor(0xffd1e4f5);paint.setTextSize(14*getResources().getDisplayMetrics().scaledDensity);c.drawText(String.format(Locale.US,"%.0f",max),5,24,paint);c.drawText("0",5,getHeight()-28,paint);c.drawText("0 min",60,getHeight()-5,paint);c.drawText(String.format(Locale.US,"%.1f min",duration/60),Math.max(60,getWidth()-90),getHeight()-5,paint);
            String previousBlock=null;paint.setColor(0xff859bb5);paint.setStrokeWidth(1);for(String[] r:rows){if(previousBlock!=null&&!previousBlock.equals(r[1])){float x=(float)(60+Double.parseDouble(r[0])/duration*(width-1));c.drawLine(x,28,x,getHeight()-28,paint);}previousBlock=r[1];}
        }
    }
}
