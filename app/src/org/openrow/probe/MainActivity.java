package org.openrow.probe;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.usb.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/** A deliberately small diagnostic app for the WaterRower S4 USB CDC interface. */
public class MainActivity extends Activity {
    private static final String TAG = "OpenRow", PERMISSION = "org.openrow.probe.USB_PERMISSION";
    private static final int INK = Color.rgb(230, 238, 247), MUTED = Color.rgb(158, 176, 196);
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final ConcurrentLinkedQueue<String> commands = new ConcurrentLinkedQueue<>();
    private final StringBuilder screenLog = new StringBuilder();
    private UsbManager manager;
    private volatile boolean cancelled = true, busy, destroyed;
    private volatile UsbDevice activeDevice;
    private TextView status, detail, logView, bytesView, handshakeView, versionView;
    private TextView distanceView, strokesView, cadenceView, cadenceDetail, cadenceState, channelsView;
    private final RowingMetrics metrics = new RowingMetrics();
    private final Handler ui = new Handler();
    private LinearLayout diagnostics;
    private LinearLayout dashboard;
    private CastlebreakerView castlebreaker;
    private GravityWellView gravityWell;
    private HomeDashboardView home;
    private CampaignSetupView campaignSetup;
    private ProfileStore profileStore;
    private FamilyProfiles profiles;
    private Context profileContext;
    private WorkoutSessionController workouts;
    private WorkoutDashboardView workoutDashboard;
    private final Runnable refresh = new Runnable() {
        @Override public void run() { if(!destroyed) { refreshWorkouts();renderMetrics(); ui.postDelayed(this,250); } }
    };
    private Button connect;
    private ScrollView scroll;
    private long received;
    private BufferedWriter journal;
    private String sessionFile;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (PERMISSION.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null && !cancelled) {
                    startConnection(device);
                } else {
                    busy = false;
                    update("USB permission not granted", "Tap Connect to try again.");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (activeDevice != null && activeDevice.equals(device)) {
                    cancelled = true;
                    log("USB", "S4 cable disconnected");
                    update("S4 disconnected", "Reconnect the cable, then tap Connect.");
                }
            }
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        manager = (UsbManager)getSystemService(USB_SERVICE);
        sessionFile = "s4-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + ".log";
        try { journal = new BufferedWriter(new FileWriter(new File(getFilesDir(), sessionFile))); }
        catch (IOException e) { Log.e(TAG, "Cannot open diagnostic log", e); }
        profileStore=new ProfileStore(this);
        try{profiles=profileStore.read();profileContext=new ProfileContext(this,profiles.active());}
        catch(IOException bad){new android.app.AlertDialog.Builder(this).setTitle("Profiles unavailable").setMessage("Your saved data has been kept. Close and reopen OpenRow to retry.").setPositiveButton("Close",(d,w)->finish()).setCancelable(false).show();}
        if(profiles!=null){if(profiles.ready())buildScreen();else{TextView welcome=text("Welcome to OpenRow",32,INK);welcome.setBackgroundColor(Color.rgb(12,22,35));setContentView(welcome);configureProfile(profiles.active(),false);}}
        ui.post(refresh);
        IntentFilter filter = new IntentFilter(PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(receiver, filter);
        log("INFO", "OpenRow " + appVersion() + " | Android " + android.os.Build.VERSION.RELEASE);
        log("INFO", "Session log: " + sessionFile);
        update("Ready to find your S4", "Keep the S4 powered on and its USB cable connected.");
    }

    private String appVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(android.content.pm.PackageManager.NameNotFoundException absent){return "unknown";}}
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); return view;
    }
    private GradientDrawable panel(int color) {
        GradientDrawable shape = new GradientDrawable(); shape.setColor(color); shape.setCornerRadius(dp(14)); return shape;
    }
    private void buildScreen() {
        try{workouts=new WorkoutSessionController(profileContext);}catch(Exception bad){new android.app.AlertDialog.Builder(this).setTitle("Workout data unavailable").setMessage("Original records have been kept. Close and reopen OpenRow to retry.").setPositiveButton("Close",(d,w)->finish()).setCancelable(false).show();return;}
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(38),dp(24),dp(38),dp(20)); root.setBackgroundColor(Color.rgb(12,22,35));
        dashboard=root;
        FrameLayout host=new FrameLayout(this);
        host.addView(root,new FrameLayout.LayoutParams(-1,-1));
        castlebreaker=new CastlebreakerView(profileContext,new CastlebreakerView.Controls() {
            public void back(){showCampaignSetup();}
            public void connect(){ findS4(); }
            public void campaignProgress(){showCampaignProgress();}
        },workouts);
        castlebreaker.setVisibility(View.GONE);host.addView(castlebreaker,new FrameLayout.LayoutParams(-1,-1));
        campaignSetup=new CampaignSetupView(profileContext,new CampaignSetupView.Actions(){
            public void start(CampaignSession.Plan plan,boolean test){castlebreaker.startCampaign(plan,test);showGame();}
            public void resume(){if(castlebreaker.campaign().phase==CampaignSession.Phase.PLAYING){castlebreaker.resumeCampaign();showGame();}else showCampaignProgress();}
            public void freePlay(){castlebreaker.freePlay();showGame();}
            public void rowing(){showDashboard();}
            public void setup(){showCampaignSetup();}
            public void continueRest(boolean skip){if(castlebreaker.continueCampaign(skip))showGame();}
        });
        campaignSetup.setVisibility(View.GONE);host.addView(campaignSetup,new FrameLayout.LayoutParams(-1,-1));
        gravityWell=new GravityWellView(profileContext,new GravityWellView.Controls(){public void openWorkout(){if(gravityWell.isStandalone())showStandalone();else showGravityWell();}public void back(){showDashboard();}public void connect(){findS4();}},workouts);
        gravityWell.setVisibility(View.GONE);host.addView(gravityWell,new FrameLayout.LayoutParams(-1,-1));setContentView(host);
        TextView title = text("OpenRow", 32, INK); title.setTypeface(null,Typeface.BOLD); root.addView(title);
        TextView subtitle = text("Live rowing • WaterRower S4",17,MUTED);
        subtitle.setPadding(0,dp(5),0,dp(18)); root.addView(subtitle);
        status = text("",24,Color.rgb(81,216,191)); root.addView(status);
        detail = text("",16,MUTED); detail.setPadding(0,dp(5),0,dp(12)); root.addView(detail);
        LinearLayout cards = new LinearLayout(this);
        distanceView = metricCard(cards,"DISTANCE", "m", "Total reported by your S4");
        strokesView = metricCard(cards,"STROKES", "strokes", "Total reported by your S4");
        cadenceView = metricCard(cards,"STROKE RATE", "strokes / min", "Calculated from stroke starts");
        LinearLayout cadenceBox=(LinearLayout)cadenceView.getParent();
        cadenceDetail=text("Latest interval: —",19,INK); cadenceDetail.setPadding(0,dp(20),0,dp(8)); cadenceBox.addView(cadenceDetail);
        cadenceState=text("Connect to begin",16,MUTED); cadenceBox.addView(cadenceState);
        root.addView(cards,new LinearLayout.LayoutParams(-1,dp(300)));
        ScrollView channelScroll=new ScrollView(this); channelsView=text("",18,INK); channelsView.setPadding(dp(20),dp(12),dp(20),dp(12)); channelScroll.addView(channelsView); root.addView(channelScroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout buttons = new LinearLayout(this); buttons.setPadding(0,dp(14),0,dp(12));
        connect = button(buttons,"Connect to S4", v -> findS4());
        button(buttons,"Disconnect", v -> { cancelled = true;
            update(busy ? "Disconnecting…" : "Disconnected", busy ? "Releasing the USB connection." : "Tap Connect to start a test."); });
        button(buttons,"Show diagnostics", v -> {
            boolean show=diagnostics.getVisibility()!=View.VISIBLE;
            diagnostics.setVisibility(show?View.VISIBLE:View.GONE);
            ((Button)v).setText(show?"Hide diagnostics":"Show diagnostics");
        });
        button(buttons,"Back to home",v -> showDashboard());
        button(buttons,"Enable S4 watts",v -> {if(metrics.isConnected())commands.add("DIWA");else Toast.makeText(this,"Connect the S4 first",Toast.LENGTH_SHORT).show();});
        root.addView(buttons);
        diagnostics=new LinearLayout(this); diagnostics.setOrientation(LinearLayout.VERTICAL);
        diagnostics.setVisibility(View.GONE); root.addView(diagnostics,new LinearLayout.LayoutParams(-1,dp(290)));
        LinearLayout debugHeader=new LinearLayout(this);
        handshakeView=card(debugHeader,"HANDSHAKE","Waiting");
        versionView=card(debugHeader,"MONITOR VERSION","—");
        bytesView=card(debugHeader,"BYTES RECEIVED","0"); diagnostics.addView(debugHeader);
        button(debugHeader,"Clear log",v -> { screenLog.setLength(0); logView.setText(""); });
        scroll = new ScrollView(this); scroll.setBackground(panel(Color.rgb(19,34,51)));
        logView = text("",15,INK); logView.setTypeface(Typeface.MONOSPACE); logView.setTextIsSelectable(true);
        logView.setPadding(dp(18),dp(12),dp(18),dp(12)); scroll.addView(logView);
        diagnostics.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView foot = text("S4 estimates are provisional. For watts, select normal WATTS on the S4. Scroll for all channels.",14,MUTED);
        foot.setPadding(0,dp(12),0,0); root.addView(foot);
        home=new HomeDashboardView(profileContext,new HomeDashboardView.Actions(){public void row(){chooseWorkout();}public void history(){showWorkoutHistory();}public void castle(){if(workouts.hasActive()&&!workouts.owns("Castlebreaker")){chooseWorkout();return;}showCampaignSetup();}public void profiles(){showProfiles();}public void training(int page){gravityWell.standalone(false);gravityWell.openTraining(page);}public void play(){if(workouts.hasActive()&&!workouts.owns("Gravity Well")){chooseWorkout();return;}gravityWell.standalone(false);showGravityWell();}public void connection(){home.cancelHold();home.setVisibility(View.GONE);dashboard.setVisibility(View.VISIBLE);renderMetrics();}public void pin(){pinHomeIcon();}});
        home.setProfileName(profiles.label(profiles.active()));
        host.addView(home,new FrameLayout.LayoutParams(-1,-1));dashboard.setVisibility(View.GONE);home.setWorkoutActive(workouts.hasActive());home.showHome();
        workoutDashboard=new WorkoutDashboardView(profileContext,workouts,new WorkoutDashboardView.Actions(){public void back(){showDashboard();}public void connect(){findS4();}public void original(){resumeOriginal();}public void pause(){workouts.pause();gravityWell.standaloneActive(false);workoutDashboard.refresh();}public void resume(){if(!workouts.hasActive()&&!workouts.start("Standalone rowing",null))return;if(!metrics.isConnected()){findS4();return;}workouts.resume();gravityWell.standaloneActive(true);workoutDashboard.refresh();}public void finish(){finishWorkout();}public void guided(){if(workouts.hasActive()&&workouts.guided==null){Toast.makeText(MainActivity.this,"Finish this row before choosing a new workout.",Toast.LENGTH_LONG).show();return;}gravityWell.standalone(true);gravityWell.openTraining(0);}});
        workoutDashboard.setVisibility(View.GONE);host.addView(workoutDashboard,new FrameLayout.LayoutParams(-1,-1));
    }
    private void chooseWorkout(){
        if(!workouts.hasActive()){showStandalone();return;}
        new android.app.AlertDialog.Builder(this).setTitle("Your saved workout").setItems(new String[]{"Resume in original game","Resume standalone rowing","Finish workout"},(d,w)->{if(w==0)resumeOriginal();else if(w==1)showStandalone();else finishWorkout();}).setNegativeButton("Keep paused",null).show();
    }
    private void showStandalone(){showDashboard();home.setVisibility(View.GONE);gravityWell.standalone(true);workoutDashboard.setVisibility(View.VISIBLE);workoutDashboard.showRow();}
    private void showWorkoutHistory(){showDashboard();home.setVisibility(View.GONE);workoutDashboard.setVisibility(View.VISIBLE);workoutDashboard.showHistory();}
    private void resumeOriginal(){
        if(workouts.owns("Gravity Well")){gravityWell.standalone(false);showDashboard();gravityWell.standalone(false);showGravityWell();gravityWell.resumeRecorded();}
        else if(workouts.owns("Castlebreaker")){showDashboard();showGame();castlebreaker.resumeRecorded();}
        else showStandalone();
    }
    private void finishWorkout(){
        if(!workouts.hasActive())return;
        gravityWell.finishFromDashboard();if(workouts.error!=null){Toast.makeText(this,workouts.error,Toast.LENGTH_LONG).show();return;}
        gravityWell.standaloneActive(false);showWorkoutHistory();
    }
    private void refreshWorkouts(){
        if(workouts==null||gravityWell==null)return;
        RowingTelemetryFrame frame=RowingTelemetryFrame.from(metrics,SystemClock.elapsedRealtime());
        if(workouts.hasActive()&&!workouts.active.paused()){
            boolean standalone=workoutDashboard!=null&&workoutDashboard.getVisibility()==View.VISIBLE&&gravityWell.isStandalone();
            if(!standalone&&!gravityWell.recording()&&!castlebreaker.recording())workouts.pause();
        }
        workouts.tick(frame);if(workoutDashboard!=null&&workoutDashboard.getVisibility()==View.VISIBLE)workoutDashboard.refresh();
    }
    private void showProfiles(){
        String[] ids=profiles.ids(),items=new String[ids.length+3];
        for(int i=0;i<ids.length;i++)items[i]=profiles.label(ids[i])+(ids[i].equals(profiles.active())?" · current":"");
        items[ids.length]="Add family member";items[ids.length+1]="Edit my profile";items[ids.length+2]="Home-screen icon";
        new android.app.AlertDialog.Builder(this).setTitle("Who's rowing?").setItems(items,(d,which)->{
            if(which==ids.length)configureProfile(null,true);
            else if(which==ids.length+1)configureProfile(profiles.active(),true);
            else if(which==ids.length+2)pinHomeIcon();
            else if(!ids[which].equals(profiles.active())){try{checkpointWorkout();gravityWell.checkpointProfile();castlebreaker.active(false);castlebreaker.saveCheckpoint();FamilyProfiles next=new FamilyProfiles(profiles.data);next.select(ids[which]);profileStore.save(next);recreate();}catch(Exception bad){Toast.makeText(this,"Could not save and switch profiles. Please retry.",Toast.LENGTH_LONG).show();}}
        }).setNegativeButton("Back",null).show();
    }
    private void configureProfile(String existing,boolean cancellable){
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(dp(24),dp(8),dp(24),dp(8));
        TextView intro=text(existing==null?"Everyone gets their own career, workouts, and coaching settings.":"Set up your local profile. Existing progress stays with this profile.",18,MUTED);form.addView(intro);
        EditText name=new EditText(this);name.setSingleLine(true);name.setHint("Your name");name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(32)});
        if(existing!=null&&profiles.ready())name.setText(profiles.label(existing));form.addView(name);
        Spinner goal=new Spinner(this),mode=new Spinner(this);
        goal.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Build endurance","General cardio"}));
        mode.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"One suggested workout","Recommendation with alternatives"}));
        form.addView(text("Training goal",16,MUTED));form.addView(goal);form.addView(text("Workout choices",16,MUTED));form.addView(mode);
        if(existing!=null)try{TrainingCore.State state=new TrainingStore(new ProfileContext(this,existing)).history.state();goal.setSelection(state.goal==TrainingCore.Goal.ENDURANCE?0:1);mode.setSelection(state.mode==TrainingCore.Mode.COACH?0:1);}catch(IOException ignored){}
        form.addView(text("Connect your WaterRower S4 from Home when ready. You can choose an optional assessment or start gently; no fitness test is required. Profiles are stored on this tablet.",16,MUTED));
        ScrollView scroll=new ScrollView(this);scroll.addView(form);
        android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(this).setTitle(existing==null?"Add a profile":"Welcome · your rowing profile").setView(scroll).setPositiveButton("Save & continue",null).setNegativeButton(cancellable?"Cancel":"Close",(d,w)->{if(!cancellable)finish();}).setCancelable(cancellable).create();
        dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(b->{try{
            FamilyProfiles next=new FamilyProfiles(profiles.data);String id=existing==null?next.add(name.getText().toString()):existing;next.configure(id,name.getText().toString());
            if(gravityWell!=null){checkpointWorkout();gravityWell.checkpointProfile();castlebreaker.active(false);castlebreaker.saveCheckpoint();}
            TrainingCore.Goal chosenGoal=goal.getSelectedItemPosition()==0?TrainingCore.Goal.ENDURANCE:TrainingCore.Goal.GENERAL_CARDIO;
            TrainingCore.Mode chosenMode=mode.getSelectedItemPosition()==0?TrainingCore.Mode.COACH:TrainingCore.Mode.RECOMMENDED;
            if(gravityWell!=null&&id.equals(profiles.active()))gravityWell.configureTraining(chosenGoal,chosenMode);
            else{TrainingStore training=new TrainingStore(new ProfileContext(this,id));training.history.settings(chosenGoal,chosenMode);training.save();}
            next.select(id);profileStore.save(next);dialog.dismiss();recreate();
        }catch(Exception bad){name.setError(bad instanceof IllegalArgumentException?bad.getMessage():"Setup could not be saved. Your data was kept; retry.");}}));dialog.show();
    }
    private void pinHomeIcon(){
        android.content.pm.ShortcutManager shortcuts=getSystemService(android.content.pm.ShortcutManager.class);
        if(shortcuts==null||!shortcuts.isRequestPinShortcutSupported()){Toast.makeText(this,"Open the app drawer, then hold OpenRow and drag it onto Home.",Toast.LENGTH_LONG).show();return;}
        for(android.content.pm.ShortcutInfo item:shortcuts.getPinnedShortcuts())if(item.getId().equals("openrow-home")){getPreferences(MODE_PRIVATE).edit().putBoolean("homeIconRequested",true).apply();Toast.makeText(this,"OpenRow is already on your home screen",Toast.LENGTH_SHORT).show();return;}
        Intent launch=new Intent(this,MainActivity.class).setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int icon=getResources().getIdentifier("openrow_icon","drawable",getPackageName());
        android.content.pm.ShortcutInfo shortcut=new android.content.pm.ShortcutInfo.Builder(this,"openrow-home").setShortLabel("OpenRow").setLongLabel("OpenRow · Rowing & games").setIcon(android.graphics.drawable.Icon.createWithResource(this,icon)).setIntent(launch).build();
        if(shortcuts.requestPinShortcut(shortcut,null))getPreferences(MODE_PRIVATE).edit().putBoolean("homeIconRequested",true).apply();
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);if(home!=null)showDashboard();}
    private void checkpointRest(){if(castlebreaker.campaign()!=null && castlebreaker.campaign().phase!=CampaignSession.Phase.PLAYING)castlebreaker.saveCheckpoint();}
    private void showCampaignSetup(){
        home.setVisibility(View.GONE);
        checkpointRest();castlebreaker.active(false);castlebreaker.setVisibility(View.GONE);dashboard.setVisibility(View.GONE);
        campaignSetup.showSetup(castlebreaker.campaign());campaignSetup.setVisibility(View.VISIBLE);campaignSetup.active(true);
    }
    private void showCampaignProgress(){
        castlebreaker.active(false);castlebreaker.setVisibility(View.GONE);dashboard.setVisibility(View.GONE);
        campaignSetup.showProgress(castlebreaker.campaign());campaignSetup.setVisibility(View.VISIBLE);campaignSetup.active(true);
    }
    private void showGame(){
        checkpointRest();campaignSetup.setVisibility(View.GONE);campaignSetup.active(false);dashboard.setVisibility(View.GONE);
        castlebreaker.setVisibility(View.VISIBLE);castlebreaker.active(true);castlebreaker.rowerState(metrics.isConnected());castlebreaker.requestFocus();
        if(!metrics.isConnected() && !busy)findS4();
    }
    private void showDashboard(){
        if(workouts!=null)workouts.pause();if(workoutDashboard!=null)workoutDashboard.setVisibility(View.GONE);
        gravityWell.standaloneActive(false);gravityWell.standalone(false);
        gravityWell.active(false);gravityWell.setVisibility(View.GONE);
        checkpointRest();castlebreaker.active(false);castlebreaker.setVisibility(View.GONE);campaignSetup.setVisibility(View.GONE);campaignSetup.active(false);dashboard.setVisibility(View.GONE);home.setWorkoutActive(workouts.hasActive());home.showHome();home.setVisibility(View.VISIBLE);renderMetrics();
    }
    private void showGravityWell(){
        if(workoutDashboard!=null)workoutDashboard.setVisibility(View.GONE);
        home.cancelHold();home.setVisibility(View.GONE);
        checkpointRest();castlebreaker.active(false);castlebreaker.setVisibility(View.GONE);campaignSetup.active(false);campaignSetup.setVisibility(View.GONE);dashboard.setVisibility(View.GONE);
        gravityWell.setVisibility(View.VISIBLE);gravityWell.active(true);gravityWell.rowerState(metrics.isConnected());gravityWell.requestFocus();
    }
    private TextView metricCard(LinearLayout row,String label,String unit,String caption) {
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(30),dp(24),dp(30),dp(24)); box.setBackground(panel(Color.rgb(24,43,62)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1); p.setMargins(0,0,dp(14),0); row.addView(box,p);
        box.addView(text(label,18,MUTED)); TextView value=text("—",60,INK); value.setTypeface(null,Typeface.BOLD); box.addView(value);
        box.addView(text(unit,22,Color.rgb(81,216,191)));
        TextView note=text(caption,15,MUTED); note.setPadding(0,dp(16),0,0); box.addView(note);
        if(!label.equals("STROKE RATE")) {
            TextView spacer=text(" ",19,INK); spacer.setPadding(0,dp(20),0,dp(8)); box.addView(spacer);
            box.addView(text(" ",16,MUTED));
        }
        return value;
    }
    private void renderMetrics() {
        if(castlebreaker==null||gravityWell==null)return;
        castlebreaker.rowerState(metrics.isConnected());
        gravityWell.rowerState(metrics.isConnected());
        if(dashboard.getVisibility()!=View.VISIBLE)return;
        long now=SystemClock.elapsedRealtime();
        distanceView.setText(metrics.distance()<0?"—":String.format(Locale.US,"%,d",metrics.distance()));
        strokesView.setText(metrics.strokes()<0?"—":String.format(Locale.US,"%,d",metrics.strokes()));
        double rate=metrics.cadence(now), immediate=metrics.immediateCadence(now);
        cadenceView.setText(Double.isNaN(rate)?"—":String.format(Locale.US,"%.1f",rate));
        cadenceDetail.setText(Double.isNaN(immediate)?"Latest interval: —":String.format(Locale.US,"Latest interval: %.1f spm",immediate));
        cadenceState.setText(metrics.cadenceState(now));
        cadenceView.setTextColor(Double.isNaN(rate)?MUTED:INK);
        renderChannels(now);
    }
    private String val(double v,String unit){return Double.isFinite(v)?String.format(Locale.US,"%.1f %s",v,unit):"—";}
    private void renderChannels(long now){
        S4Channels c=metrics.channels;
        StringBuilder s=new StringBuilder();
        s.append(metrics.isConnected()?"S4 USB · "+(c.knownFirmware()?"firmware 2.10":"unverified firmware · raw channels only"):"Disconnected · live channels unavailable");
        if(metrics.isConnected()){
            s.append("\nEstimated power: ").append(val(c.watts(now),"W")).append("     Speed: ").append(val(c.speed(now),"m/s"));
            s.append("     Split: ").append(val(c.pace(now),"sec / 500m"));
            s.append("\nMonitor calorie estimate: ").append(val(c.calories(now),"kcal (provisional)"));
            s.append("     Heart rate: ").append(val(c.heartRate(now),"bpm"));
            s.append("\nMonitor display clock: ").append(val(c.clock(now),"s (may count down)"));
            s.append("     S4 cadence: ").append(metrics.monitorCadence()<0?"—":metrics.monitorCadence()+" spm");
            s.append("\nDrive: ").append(val(c.driveSeconds(),"s")).append("     Recovery: ").append(val(c.recoverySeconds(),"s"));
            s.append("     Pulses: ").append(c.pulses()).append("     Latest pulse rate: ").append(val(c.pulseRate(now),"edges/s"));
            s.append("\nConfigured tank volume: ").append(val(c.tankLiters(now),"L (monitor setting)"));
            s.append("\nPower needs normal WATTS mode. Speed/calorie scaling needs a normal-water check.");
            s.append("\n\nRAW CHANNELS · value / age · scroll to inspect");
            for(java.util.Map.Entry<String,S4Channels.Sample> entry:c.observations().entrySet()){
                S4Channels.Sample v=entry.getValue();s.append("\n").append(entry.getKey()).append("  0x").append(v.payload).append("  (").append(v.raw).append(")   ").append(Math.max(0,now-v.at)).append(" ms");
            }
        }
        channelsView.setText(s.toString());
    }
    private void receiveMetric(String line,long timestamp) {
        runOnUiThread(() -> {
            if(destroyed) return;
            boolean stroke=metrics.accept(line,timestamp);
            double previous=workouts!=null&&workouts.hasActive()?workouts.active.total("distance"):0;
            if(workouts!=null)workouts.observe(RowingTelemetryFrame.from(metrics,timestamp),stroke);
            if(workouts!=null&&workouts.hasActive())gravityWell.standaloneDistance(Math.max(0,workouts.active.total("distance")-previous));
            if(stroke) {
                gravityWell.stroke();
                double rate=metrics.cadence(timestamp);
                log("CADENCE",Double.isNaN(rate)?"First stroke; waiting for next start":String.format(Locale.US,"Calculated %.1f spm; latest %.1f spm",rate,metrics.immediateCadence(timestamp)));
            }
            gravityWell.telemetry(metrics.channels,line,timestamp);
            if(line.matches("IDD055[0-9A-Fa-f]{4}")){castlebreaker.distance(metrics.distance());gravityWell.distance(metrics.distance());}
            // The 250 ms UI refresh presents telemetry; pulse packets must not redraw native panels.
        });
    }
    private TextView card(LinearLayout row, String label, String value) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(1); box.setPadding(dp(20),dp(14),dp(20),dp(14));
        box.setBackground(panel(Color.rgb(24,43,62)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,-2,1); p.setMargins(0,0,dp(12),0); row.addView(box,p);
        box.addView(text(label,12,MUTED)); TextView output=text(value,27,INK); box.addView(output); return output;
    }
    private Button button(LinearLayout row, String title, View.OnClickListener action) {
        Button b=new Button(this); b.setText(title); b.setAllCaps(false); b.setTextSize(16); b.setOnClickListener(action);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(54)); p.setMargins(0,0,dp(12),0); row.addView(b,p); return b;
    }
    private void update(String headline, String description) {
        runOnUiThread(() -> { if (!destroyed&&status!=null) {status.setText(headline); detail.setText(description); connect.setEnabled(!busy);} });
    }
    private synchronized void log(String kind, String message) {
        String line = new SimpleDateFormat("HH:mm:ss.SSS",Locale.US).format(new Date()) + "  " + kind + "  " + message;
        if(!message.matches("P[0-9A-Fa-f]{2}"))Log.i(TAG,line);
        try { if (journal != null) {journal.write(line); journal.newLine(); journal.flush();} }
        catch (IOException e) { Log.e(TAG,"Log write failed",e); }
        if(message.matches("P[0-9A-Fa-f]{2}"))return;
        runOnUiThread(() -> {
            if (destroyed) return;
            screenLog.append(line).append('\n');
            if (screenLog.length()>18000) screenLog.delete(0,screenLog.indexOf("\n",screenLog.length()-14000)+1);
            if(dashboard!=null&&dashboard.getVisibility()==View.VISIBLE && diagnostics.getVisibility()==View.VISIBLE) {
                logView.setText(screenLog); scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            }
        });
    }
    private void findS4() {
        if (busy) return;
        UsbDevice found=null;
        for (UsbDevice device:manager.getDeviceList().values()) {
            log("USB",String.format(Locale.US,"%04X:%04X %s",device.getVendorId(),device.getProductId(),device.getProductName()));
            if (device.getVendorId()==0x04d8 && device.getProductId()==0x000a) found=device;
        }
        if (found==null) { update("S4 not found", "Check monitor power and the existing S4 USB cable, then retry."); return; }
        activeDevice=found; busy=true; cancelled=false;
        commands.clear();
        metrics.reset(); renderMetrics();
        received=0; bytesView.setText("0"); handshakeView.setText("Waiting"); versionView.setText("—");
        update("Opening S4…", "Requesting access to the monitor.");
        if (manager.hasPermission(found)) startConnection(found);
        else {
            Intent intent=new Intent(PERMISSION).setPackage(getPackageName());
            manager.requestPermission(found,PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }
    private void startConnection(UsbDevice device) { io.execute(() -> communicate(device)); }

    private void communicate(UsbDevice device) {
        UsbDeviceConnection connection=null;
        UsbInterface control=null,data=null;
        boolean controlClaimed=false,dataClaimed=false;
        try {
            UsbEndpoint input=null,output=null;
            for(int i=0;i<device.getInterfaceCount();i++) {
                UsbInterface candidate=device.getInterface(i);
                if(candidate.getInterfaceClass()==UsbConstants.USB_CLASS_COMM) control=candidate;
                if(candidate.getInterfaceClass()==UsbConstants.USB_CLASS_CDC_DATA) {
                    data=candidate;
                    for(int j=0;j<candidate.getEndpointCount();j++) {
                        UsbEndpoint endpoint=candidate.getEndpoint(j);
                        if(endpoint.getType()==UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            if(endpoint.getDirection()==UsbConstants.USB_DIR_IN) input=endpoint; else output=endpoint;
                        }
                    }
                }
            }
            if(control==null || data==null || input==null || output==null) throw new IOException("S4 CDC interfaces missing");
            connection=manager.openDevice(device);
            if(connection==null) throw new IOException("Cannot open USB device. Check Android USB permission.");
            controlClaimed=connection.claimInterface(control,true);
            dataClaimed=connection.claimInterface(data,true);
            if(!controlClaimed || !dataClaimed) throw new IOException("USB interface busy. Close the Ergatta app and retry.");
            // CDC SET_LINE_CODING: 115200 baud, one stop bit, no parity, eight data bits.
            byte[] coding={(byte)0x00,(byte)0xc2,(byte)0x01,0,0,0,8};
            int configured=connection.controlTransfer(0x21,0x20,0,control.getId(),coding,coding.length,1000);
            if(configured!=7) throw new IOException("CDC line configuration failed: "+configured);
            int lines=connection.controlTransfer(0x21,0x22,3,control.getId(),null,0,1000);
            if(lines<0) throw new IOException("CDC control lines failed: "+lines);
            log("USB","Claimed CDC interfaces; 115200 baud, 8N1, DTR/RTS on");
            update("USB connected — checking monitor", "Waiting for the S4 handshake reply.");
            write(connection,output,"USB");
            long started=SystemClock.elapsedRealtime(),lastQuery=0,lastRx=started;
            int retries=0;
            S4Channels polling=new S4Channels();
            boolean handshake=false,notified=false;
            
            StringBuilder pending=new StringBuilder();
            byte[] buffer=new byte[512];
            while(!cancelled) {
                int count=connection.bulkTransfer(input,buffer,buffer.length,25);
                long now=SystemClock.elapsedRealtime();
                if(count>0) {
                    received+=count; lastRx=now; final long total=received;
                    runOnUiThread(() -> {if(dashboard.getVisibility()==View.VISIBLE && diagnostics.getVisibility()==View.VISIBLE)bytesView.setText(Long.toString(total));});
                    for(int i=0;i<count;i++) {
                        int b=buffer[i]&255;
                        if(b==10 || b==13) {
                            if(pending.length()>0) {
                                String line=pending.toString(); pending.setLength(0); log("RX",line);
                                if(line.contains("_WR_")&&!handshake) {
                                    handshake=true;
                                    write(connection,output,"IV?");
                                    commands.add("DIWA");
                                    lastQuery=now;
                                    runOnUiThread(() -> { handshakeView.setText("Confirmed"); metrics.setConnected(true); renderMetrics(); });
                                    update("Connected to S4", "Ready to row. Cadence appears after two stroke starts.");
                                }
                                if(line.startsWith("IV") && !line.equals("IV?")) {
                                    final String version=line.substring(2); runOnUiThread(() -> versionView.setText(version));
                                }
                                receiveMetric(line,SystemClock.elapsedRealtime());
                            }
                        } else if(b>=32 && b<=126) pending.append((char)b);
                        else pending.append(String.format(Locale.US,"<%02X>",b));
                        if(pending.length()>2048) {log("RX partial",pending.toString());pending.setLength(0);}
                    }
                }
                if(!handshake && now-started>3000L*(retries+1) && retries<2) {write(connection,output,"USB");retries++;}
                if(!handshake && now-started>10000 && !notified) {
                    notified=true; log("WARN","No handshake after 10 seconds");
                    update("USB open — no handshake yet", "Check that the S4 is awake; disconnect and retry if necessary.");
                }
                if(handshake && now-lastQuery>=50) {String requested=commands.poll();write(connection,output,requested==null?polling.nextQuery():requested); lastQuery=now;}
                if(handshake && now-lastRx>10000) throw new IOException("S4 stopped responding for 10 seconds. Check monitor power/cable and retry.");
            }
            write(connection,output,"EXIT");
            if(pending.length()>0) log("RX partial",pending.toString());
            update("Disconnected", "USB released. Tap Connect to start another test.");
        } catch(Exception error) {
            log("ERROR",error.toString()); update("Connection needs attention",error.getMessage());
        } finally {
            if(connection!=null) {
                if(dataClaimed) connection.releaseInterface(data);
                if(controlClaimed) connection.releaseInterface(control);
                connection.close();
            }
            busy=false; cancelled=true; activeDevice=null;
            runOnUiThread(() -> { if(!destroyed) { connect.setEnabled(true); metrics.setConnected(false); renderMetrics(); } });
            log("USB","Connection closed");
        }
    }
    private void write(UsbDeviceConnection connection,UsbEndpoint output,String command) throws IOException {
        byte[] bytes=(command+"\r\n").getBytes(StandardCharsets.US_ASCII);
        int sent=connection.bulkTransfer(output,bytes,bytes.length,1000);
        if(sent!=bytes.length) throw new IOException("USB write failed: "+sent+" / "+bytes.length);
        log("TX",command);
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if(home==null)return super.dispatchKeyEvent(event);
        if(gravityWell!=null&&gravityWell.getVisibility()==View.VISIBLE&&event.getKeyCode()==KeyEvent.KEYCODE_BACK){if(event.getAction()==KeyEvent.ACTION_UP)gravityWell.key(KeyEvent.KEYCODE_BACK,true);return true;}
        if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){if(event.getAction()==KeyEvent.ACTION_UP){if(home.getVisibility()==View.VISIBLE){if(!home.back())moveTaskToBack(true);}else showDashboard();}return true;}
        if(gravityWell!=null&&gravityWell.getVisibility()==View.VISIBLE&&gravityWell.key(event.getKeyCode(),event.getAction()==KeyEvent.ACTION_DOWN))return true;
        if(castlebreaker!=null && castlebreaker.getVisibility()==View.VISIBLE) {
            if(event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
                if(event.getAction()==KeyEvent.ACTION_UP)showCampaignSetup();return true;
            }
            if(castlebreaker.key(event.getKeyCode(),event.getAction()==KeyEvent.ACTION_DOWN,event.getRepeatCount()))return true;
        }
        if(campaignSetup!=null && campaignSetup.getVisibility()==View.VISIBLE && event.getKeyCode()==KeyEvent.KEYCODE_BACK){if(event.getAction()==KeyEvent.ACTION_UP)showDashboard();return true;}
        return super.dispatchKeyEvent(event);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==TrainingController.EXPORT_REQUEST&&resultCode==RESULT_OK&&data!=null&&gravityWell!=null)gravityWell.exportTraining(data.getData());}
    private void checkpointWorkout()throws IOException{if(workouts!=null){workouts.pause();if(workouts.error!=null)throw new IOException(workouts.error);}}
    @Override protected void onPause(){super.onPause();if(workouts!=null)workouts.pause();if(home!=null)home.cancelHold();if(gravityWell!=null){gravityWell.standaloneActive(false);gravityWell.active(false);}if(castlebreaker!=null)castlebreaker.active(false);if(campaignSetup!=null)campaignSetup.active(false);if(castlebreaker!=null)checkpointRest();}
    @Override protected void onResume(){super.onResume();if(gravityWell!=null)gravityWell.active(gravityWell.getVisibility()==View.VISIBLE);if(castlebreaker!=null)castlebreaker.active(true);if(campaignSetup!=null)campaignSetup.active(true);}
    @Override protected void onDestroy() {
        destroyed=true; cancelled=true; ui.removeCallbacks(refresh); unregisterReceiver(receiver);
        io.execute(() -> { synchronized(MainActivity.this) {
            try { if(journal!=null) journal.close(); } catch(IOException ignored) {} journal=null;
        }});
        io.shutdown();
        super.onDestroy();
    }
}
