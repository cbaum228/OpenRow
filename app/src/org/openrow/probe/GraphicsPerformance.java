package org.openrow.probe;

import java.util.Arrays;

/** Presented-view frame intervals, separate from Android GPU pipeline latency. */
public final class GraphicsPerformance {
    private final double[] intervals=new double[300];private int count,next,windowCount,goodWindows;private double windowTime,windowCpu;
    public int quality=1,maxQuality=2;
    public void frame(double milliseconds,double cpuMilliseconds){
        if(!Double.isFinite(milliseconds)||milliseconds<=0||milliseconds>250){windowCount=0;windowTime=windowCpu=0;return;}
        intervals[next]=milliseconds;next=(next+1)%intervals.length;count=Math.min(count+1,intervals.length);windowCount++;windowTime+=milliseconds;windowCpu+=cpuMilliseconds;
        if(windowTime>=1000){double average=windowTime/windowCount,cpu=windowCpu/windowCount;
            if(average>36.5||cpu>24){quality=Math.max(0,quality-1);goodWindows=0;}
            else if(average<34.5&&cpu<14){if(++goodWindows>=15){quality=Math.min(maxQuality,quality+1);goodWindows=0;}}
            else goodWindows=0;windowCount=0;windowTime=windowCpu=0;
        }
    }
    public double fps(){double sum=0;for(int i=0;i<count;i++)sum+=intervals[i];return sum>0?1000*count/sum:0;}
    public double percentile(double p){if(count==0)return 0;double[] copy=Arrays.copyOf(intervals,count);Arrays.sort(copy);return copy[Math.min(count-1,(int)Math.ceil(p*count)-1)];}
    public int samples(){return count;}
}
