package org.openrow.probe;

/** Shared game input. A reference is frozen for the lifetime of a workout. */
public final class RowingEffortFrame {
    public static double powerReference(java.util.List<java.util.Properties> sessions,java.util.List<TrainingCore.Record> records,long now){
        java.util.List<Double> values=new java.util.ArrayList<>();
        for(java.util.Properties p:sessions){if(values.size()>=5)break;TrainingCore.Record found=null;for(TrainingCore.Record r:records)if(r.id.equals(p.getProperty("id"))){found=r;break;}
            long ended=Long.parseLong(p.getProperty("ended","0"));int effort=Integer.parseInt(p.getProperty("effort",found==null?"-1":""+found.effort));
            if(!"true".equals(p.getProperty("finished"))||Boolean.parseBoolean(p.getProperty("simulated","false"))||found!=null&&!found.success()||effort<5||effort>6||ended>now||now-ended>28*TrainingCore.DAY)continue;
            double seconds=0,joules=0,motion=0;
            if(found!=null&&found.plan.kind!=TrainingCore.Kind.FREE_ROW){for(int b=0;b<found.plan.blocks.size();b++)if(found.plan.blocks.get(b).phase==TrainingCore.Phase.WORK){seconds+=n(p,"block."+b+".powerSeconds");joules+=n(p,"block."+b+".energyJ");motion+=found.blockRowingSeconds[b];}}
            else{seconds=n(p,"athlete.powerSeconds");joules=n(p,"athlete.energyJ");motion=n(p,"athlete.motionSeconds");}
            if(seconds>=60&&motion>=60&&seconds>=motion*.7&&joules>0)values.add(joules/seconds);
        }if(values.size()<2)return 0;java.util.Collections.sort(values);int count=values.size();return count%2==1?values.get(count/2):(values.get(count/2-1)+values.get(count/2))*.5;
    }
    private static double n(java.util.Properties p,String key){return Double.parseDouble(p.getProperty(key,"0"));}
    public final double contribution;public final String source;public final boolean provisional,moving;
    RowingEffortFrame(double value,String source,boolean provisional,boolean moving){contribution=value;this.source=source;this.provisional=provisional;this.moving=moving;}
    public static final class Resolver {
        private final double powerReference,paceReference;private final boolean validated;
        private long last=-1,lastPower=-1;private int validSamples;private double smoothed;private boolean usingPower;
        public Resolver(double power,double pace,boolean validated){powerReference=power;paceReference=pace;this.validated=validated;}
        public RowingEffortFrame resolve(RowingTelemetryFrame f,boolean moving,GameWorkoutAdapter.Frame workout){
            boolean eligible=moving&&f.connected&&(workout==null||workout.campaignWorkEligible(true));
            double cap=workout==null?1.25:Math.min(1.25,workout.effort/5.0);
            if(f.powerAvailable()){if(lastPower!=f.powerAt){validSamples++;lastPower=f.powerAt;}}else{validSamples=0;usingPower=false;lastPower=-1;}
            if(validated&&powerReference>0&&validSamples>=3)usingPower=true;
            double target=usingPower?f.watts/powerReference:GameplayEffort.contribution(f.speed,moving,paceReference,workout);
            if(!Double.isFinite(target))target=paceReference>0?0:1;
            target=Math.max(0,Math.min(cap,target));
            double dt=last<0?0:Math.max(0,Math.min(1,(f.at-last)/1000.0));last=f.at;
            if(!eligible||usingPower&&f.watts==0)smoothed=0;else smoothed+=Math.min(1,dt/2)*(target-smoothed);
            return new RowingEffortFrame(eligible?Math.min(cap,smoothed):0,usingPower?"Power":paceReference>0?"Pace":"Provisional pace",usingPower?false:paceReference<=0,eligible);
        }
    }
}
