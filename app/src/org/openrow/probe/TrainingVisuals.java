package org.openrow.probe;

import java.util.*;
import org.openrow.probe.TrainingCore.Record;
import static org.openrow.probe.TrainingCore.*;

/** Athlete-relative artwork estimates. These are never exercise targets or scoring inputs. */
public final class TrainingVisuals {
    private TrainingVisuals(){}
    public static final class Profile {
        private final double[][] speeds;
        private final int[][] samples;
        private Profile(double[][] speeds,int[][] samples){this.speeds=speeds;this.samples=samples;}
        public double speed(Category c,int effort){return c==null||effort<1||effort>6?0:speeds[c.ordinal()][effort];}
        public int samples(Category c,int effort){return c==null||effort<1||effort>6?0:samples[c.ordinal()][effort];}
    }
    public static Profile empty(){return new Profile(new double[Category.values().length][7],new int[Category.values().length][7]);}
    public static Profile profile(List<Record> history,long now,String excludeSession){
        List<Record> records=new State(history).history;
        @SuppressWarnings("unchecked") List<Double>[][] values=new List[Category.values().length][7];
        for(int c=0;c<values.length;c++)for(int e=0;e<7;e++)values[c][e]=new ArrayList<Double>();
        for(int index=records.size()-1;index>=0;index--){Record r=records.get(index);
            if(r.id.equals(excludeSession)||r.ended>now||now-r.ended>14*DAY||!r.success()||r.interruptions>0)continue;
            int prescribed=0;for(Block block:r.plan.blocks)prescribed=Math.max(prescribed,block.effort);
            if(r.plan.kind!=Kind.FREE_ROW&&Math.abs(r.effort-prescribed)>1)continue;
            double[][] meters=new double[values.length][7],motion=new double[values.length][7];
            for(int i=0;i<r.plan.blocks.size();i++){Block b=r.plan.blocks.get(i);int effort=r.plan.kind==Kind.FREE_ROW?r.effort:b.effort;
                if(b.phase!=Phase.WORK||effort<1||effort>6||r.blockRowingSeconds[i]<r.blockSeconds[i]*.7)continue;
                meters[b.category.ordinal()][effort]+=r.blockDistance[i];motion[b.category.ordinal()][effort]+=r.blockRowingSeconds[i];}
            for(int c=0;c<values.length;c++)for(int e=1;e<=6;e++)if(values[c][e].size()<5&&motion[c][e]>=60){double speed=meters[c][e]/motion[c][e];
                if(Double.isFinite(speed)&&speed>0&&speed<=6)values[c][e].add(speed);}
        }
        double[][] speeds=new double[values.length][7];int[][] samples=new int[values.length][7];
        for(int c=0;c<values.length;c++)for(int e=1;e<=6;e++){List<Double> v=values[c][e];Collections.sort(v);int n=v.size();samples[c][e]=n;
            if(n>0)speeds[c][e]=n%2==1?v.get(n/2):(v.get(n/2-1)+v.get(n/2))*.5;}
        return new Profile(speeds,samples);
    }
    public static double ceiling(GameWorkoutAdapter.Frame frame){
        if(frame==null||frame.finished||frame.paused)return 0;
        double intensity=Math.max(.1,Math.min(.95,.1+Math.max(0,frame.effort-1)*.2));
        return frame.phase==Phase.RECOVERY?Math.min(.22,intensity):frame.phase==Phase.COOL_DOWN?Math.min(.25,intensity):frame.phase==Phase.WARM_UP?Math.min(.35,intensity):intensity;
    }
    public static double input(GameWorkoutAdapter.Frame frame,double speed,boolean moving){
        if(frame==null||!frame.telemetryAvailable||!moving||!Double.isFinite(speed)||speed<=0)return 0;
        double cap=ceiling(frame);
        return cap*(frame.phase==Phase.WORK&&frame.visualReferenceSpeed>0?Math.min(1,speed/frame.visualReferenceSpeed):1);
    }
    public static double freeInput(Profile profile,double speed,boolean moving){
        if(!moving||!Double.isFinite(speed)||speed<=0)return 0;
        double reference=profile.speed(Category.ENDURANCE,3);
        return reference>0?Math.max(0,Math.min(1,.5*speed/reference)):.5;
    }
}
