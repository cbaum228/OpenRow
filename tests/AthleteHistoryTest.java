package org.openrow.probe;
import java.util.Properties;
public final class AthleteHistoryTest {
    private static int checks;
    private static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    private static Properties source(double distance,int strokes,double seconds,int workouts,String id){Properties p=new Properties();p.setProperty("athlete.distance",""+distance);p.setProperty("athlete.strokes",""+strokes);p.setProperty("athlete.motionSeconds",""+seconds);p.setProperty("athlete.workouts",""+workouts);p.setProperty("last.id",id);p.setProperty("last.distance",""+distance);p.setProperty("last.strokes",""+strokes);p.setProperty("last.seconds",""+seconds);p.setProperty("last.ended","12345");return p;}
    public static void main(String[] args){
        AthleteHistory h=new AthleteHistory();Properties row=source(1200,300,600,1,"a");h.sync(row,true);check(h.value("total.distance")==0&&!h.data.containsKey("last.id"),"Test data excluded");
        h.sync(row,false);check(h.value("total.distance")==1200,"Import existing real totals");check(h.value("total.workouts")==1,"Workout counted");check(h.earned("first")&&h.earned("km1"),"Actual achievements earned");check(!h.earned("hour"),"Unearned achievements locked");
        for(int i=0;i<20;i++)h.sync(row,false);check(h.value("total.distance")==1200&&h.value("total.workouts")==1,"Save replay cannot duplicate counters");
        h.data.setProperty("trophy.test","true");h.data.setProperty("milestone.test","true");h.resetTotals(5000);for(String metric:AthleteHistory.METRICS)check(h.value("total."+metric)==0,"Reset "+metric);check(!h.earned("km1")&&!h.data.containsKey("last.id")&&!h.data.containsKey("trophy.test")&&!h.data.containsKey("milestone.test"),"Complete athlete reset clears awards and last workout");
        h=new AthleteHistory(h.data);h.sync(row,false);check(h.value("total.distance")==0&&!h.data.containsKey("last.id")&&!h.earned("first"),"Restart does not reimport reset totals or awards or last workout");
        h.sync(source(1500,360,720,2,"b"),false);check(h.value("total.distance")==300&&h.value("total.strokes")==60&&h.value("total.motionSeconds")==120&&h.value("total.workouts")==1,"Only post-reset increments count");
        h.sync(source(1000,200,400,1,"b"),false);check(h.value("total.distance")==300,"Rollback does not subtract or recredit");h.sync(source(1500,360,720,2,"b"),false);check(h.value("total.distance")==300,"Recovery after rollback is idempotent");
        AthleteHistory.Hold hold=new AthleteHistory.Hold();check(!hold.complete(10000),"No touch cannot reset");hold.start(100);check(!hold.complete(5099),"4.999 seconds is too short");check(hold.complete(5100),"Continuous 5 seconds completes");check(!hold.complete(11000),"Completes only once");
        hold.start(20000);hold.cancel();check(!hold.complete(40000)&&hold.progress(40000)==0,"Release cancels");hold.start(40000);check(!hold.complete(44999),"A second hold starts from zero");hold.cancel();hold.start(50000);check(!hold.complete(54999),"Interrupted holds do not accumulate");
        check(row.getProperty("athlete.distance").equals("1200.0"),"Reset never changes game source");
        System.out.println("PASS: "+checks+" athlete history and hold-reset checks");
    }
}
