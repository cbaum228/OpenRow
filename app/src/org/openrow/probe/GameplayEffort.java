package org.openrow.probe;

import java.util.*;
import org.openrow.probe.TrainingCore.Record;

/** Separate gameplay reference. It does not infer fitness, watts, or physiological thresholds. */
public final class GameplayEffort {
    private GameplayEffort(){}
    public static double reference(List<Record> history,long now){
        List<Double> speeds=new ArrayList<>();List<Record> records=new TrainingCore.State(history).history;
        for(int i=records.size()-1;i>=0&&speeds.size()<5;i--){Record r=records.get(i);
            if(!r.success()||r.effort<5||r.effort>6||r.ended>now||now-r.ended>28*TrainingCore.DAY)continue;
            double seconds=0,meters=0;for(int j=0;j<r.plan.blocks.size();j++)if(r.plan.blocks.get(j).phase==TrainingCore.Phase.WORK&&r.blockRowingSeconds[j]>=r.blockSeconds[j]*.7){seconds+=r.blockRowingSeconds[j];meters+=r.blockDistance[j];}
            if(seconds>=60&&meters/seconds>0&&meters/seconds<=6)speeds.add(meters/seconds);
        }
        if(speeds.size()<2)return 0;Collections.sort(speeds);return speeds.get(speeds.size()/2);
    }
    public static double contribution(double speed,boolean moving,double reference,GameWorkoutAdapter.Frame frame){
        if(!moving||!Double.isFinite(speed)||speed<=0||frame!=null&&(!frame.telemetryAvailable||frame.paused||frame.finished||frame.phase!=TrainingCore.Phase.WORK))return 0;
        double cap=frame==null?1.25:Math.min(1.25,frame.effort/5.0);
        // Unknown athletes receive a neutral provisional game reference, not a guessed physical capacity.
        return Math.min(cap,reference>0&&Double.isFinite(reference)?speed/reference:1);
    }
}
