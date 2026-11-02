package org.openrow.probe;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import java.util.Locale;

/** Vsync-aligned ~30 FPS scheduling; simulation and training clocks retain their own owners. */
public final class RenderLoop {
    private final View view;private final String name;private boolean enabled,pending;
    private long start,last,reported;public final GraphicsPerformance performance=new GraphicsPerformance();
    public RenderLoop(View view,String name){this.view=view;this.name=name;if(android.os.Build.VERSION.SDK_INT<=28)performance.maxQuality=1;}
    public void begin(){start=SystemClock.elapsedRealtimeNanos();}
    public void end(boolean animate){
        long now=SystemClock.elapsedRealtimeNanos();if(animate&&enabled&&last>0)performance.frame((start-last)/1e6,(now-start)/1e6);last=animate?start:0;
        if(now-reported>10000000000L&&performance.samples()>=30){Log.i("GravityFrames",String.format(Locale.US,"%s samples=%d fps=%.2f median_ms=%.2f p95_ms=%.2f p99_ms=%.2f quality=%d",name,performance.samples(),performance.fps(),performance.percentile(.5),performance.percentile(.95),performance.percentile(.99),performance.quality));reported=now;}
        enabled=animate;if(enabled&&!pending){pending=true;view.postOnAnimation(request);}
    }
    private final Runnable request=new Runnable(){public void run(){pending=false;if(!enabled||!view.isShown()||view.getWindowVisibility()!=View.VISIBLE)return;
        if(!view.hasWindowFocus()){pending=true;view.postDelayed(this,200);return;}
        if(SystemClock.elapsedRealtimeNanos()-last>=30500000L)view.invalidate();else{pending=true;view.postOnAnimation(this);}
    }};
}
