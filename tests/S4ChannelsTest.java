package org.openrow.probe;
import java.util.*;
public final class S4ChannelsTest {
    private static int checks;
    private static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    private static void close(double expected,double actual,String why){check(Math.abs(expected-actual)<.00001,why+": "+actual);}
    private static void row(S4Channels c,String line,long at){c.accept(line,at,line.equals("SS"));}
    private static void power(S4Channels c,long t,int w){row(c,"IDS00D00",t);row(c,"IDS04110",t);row(c,String.format("IDD088%04X",w),t);}
    private static double n(Properties p,String k){return Double.parseDouble(p.getProperty(k,"0"));}
    public static void main(String[] args){
        S4Channels c=new S4Channels();power(c,100,200);check(Double.isNaN(c.watts(100)),"Unknown firmware cannot publish watts");
        row(c,"IV40210",100);close(200,c.watts(100),"Known firmware watts");check(Double.isNaN(c.watts(1601)),"Stale power is missing");
        row(c,"IDS04120",200);check(Double.isNaN(c.watts(200)),"Calorie mode cannot become watts");
        row(c,"IDS04190",200);check(Double.isNaN(c.watts(200)),"Average display excluded");
        row(c,"IDS04110",200);row(c,"IDS00D40",200);check(Double.isNaN(c.watts(200)),"Menu data excluded");
        power(c,300,4001);check(Double.isNaN(c.watts(300)),"Implausible power excluded");
        row(c,"IDT08A010000",400);close(65.536,c.calories(400),"All 24 calorie bits decoded, cal to kcal");
        row(c,"IDD08A0001",410);close(65536,c.get("08A").raw,"Wrong width does not corrupt calorie counter");
        row(c,"IDD14800FA",400);close(2.5,c.speed(400),"Speed cm/s to m/s");close(200,c.pace(400),"500m split");
        row(c,"IDD1480000",500);check(Double.isNaN(c.pace(500)),"No infinite split at rest");
        row(c,"IDT1E1012359",400);close(5039,c.clock(400),"BCD clock uses decimal digits and correct order");
        row(c,"IDT1E1012360",400);check(Double.isNaN(c.clock(400)),"Invalid seconds rejected");
        row(c,"IDS1A000",400);check(Double.isNaN(c.heartRate(400)),"No HR sensor is missing, not zero bpm");row(c,"IDS1A096",400);close(150,c.heartRate(400),"HR single-byte");
        row(c,"PING",500);row(c,"P0A",500);row(c,"P02",525);close(12,c.pulses(),"PING is not a pulse");close(80,c.pulseRate(525),"Pulse count represents 25ms");check(Double.isNaN(c.pulseRate(1026)),"Pulse rate expires");
        row(c,"SS",1000);row(c,"SE",1800);close(.8,c.driveSeconds(),"Drive from event timestamps");row(c,"SS",3500);close(1.7,c.recoverySeconds(),"Recovery from events");
        RowingMetrics m=new RowingMetrics();m.setConnected(true);m.accept("IDD0550064",0);m.accept("IDD05703E8",10);close(100,m.distance(),"Canonical workout distance independent of display countdown");
        Properties p=new Properties();RowingStats stats=new RowingStats();stats.begin(p);power(c,10000,100);stats.accept(c,"IDD0880064",10000,p,true);power(c,11000,300);stats.accept(c,"IDD088012C",11000,p,true);
        close(200,n(p,"athlete.energyJ"),"Trapezoidal watts integration");close(1,n(p,"athlete.powerSeconds"),"Coverage recorded separately");
        stats.accept(c,"IDD088012C",11000,p,true);close(200,n(p,"athlete.energyJ"),"Duplicate timestamp cannot add energy");
        stats.accept(c,"IDD088broken",11500,p,true);close(200,n(p,"athlete.energyJ"),"Malformed reply cannot integrate a cached value");
        power(c,14000,100);stats.accept(c,"IDD0880064",14000,p,true);close(200,n(p,"athlete.energyJ"),"Missing samples not bridged");
        stats.interrupt();power(c,14500,100);stats.accept(c,"IDD0880064",14500,p,true);close(200,n(p,"athlete.energyJ"),"Pause/resume starts baseline");
        row(c,"IDS04120",14600);stats.accept(c,"IDS04120",14600,p,true);power(c,15000,100);stats.accept(c,"IDD0880064",15000,p,true);close(200,n(p,"athlete.energyJ"),"Mode changes break integration");
        power(c,15500,100);stats.accept(c,"IDD0880064",15500,p,false);close(200,n(p,"athlete.energyJ"),"Menus and test career excluded");
        row(c,"IDT08A0003E8",16000);stats.accept(c,"IDT08A0003E8",16000,p,true);row(c,"IDT08A0005DC",17000);stats.accept(c,"IDT08A0005DC",17000,p,true);close(.5,n(p,"athlete.estimatedCalories"),"Calorie differences only");
        row(c,"IDT08A000000",18000);stats.accept(c,"IDT08A000000",18000,p,true);close(.5,n(p,"athlete.estimatedCalories"),"Reset cannot subtract or wrap calories");
        row(c,"SS",19000);stats.accept(c,"SS",19000,p,true);row(c,"SE",19800);stats.accept(c,"SE",19800,p,true);stats.accept(c,"SE",19850,p,true);close(1,n(p,"athlete.driveCount"),"Duplicate ends do not double count");
        stats.complete(p);close(300,n(p,"last.peakPower"),"Last workout sampled peak saved");
        AthleteHistory h=new AthleteHistory();h.sync(p,false);h.sync(p,false);close(200,h.value("total.energyJ"),"Energy import replay is idempotent");h.resetTotals(20000);h.sync(p,false);close(0,h.value("total.energyJ"),"Reset keeps energy watermark");
        RowingStats restored=new RowingStats();power(c,21000,100);restored.accept(c,"IDD0880064",21000,p,true);close(200,n(p,"athlete.energyJ"),"Process restart cannot integrate offline time");
        Set<String> queries=new HashSet<>();for(int i=0;i<102;i++)queries.add(c.nextQuery());check(queries.contains("IRT08A")&&queries.contains("IRS1A0")&&queries.contains("IRT1E1"),"Polling includes correct widths");
        power(c,22000,200);row(c,"IDS00D02",22000);close(200,c.watts(22000),"Normal mode permits its submode nibble");
        row(c,"IDS0A9B4",22000);close(18,c.tankLiters(22000),"Tank setting is tenths of liters");
        row(c,"P02",22000);stats.accept(c,"P02",22000,p,true);close(2,n(p,"athlete.pulses"),"First eligible pulse packet is retained");
        c.reset();check(c.observations().isEmpty()&&c.pulses()==0&&!c.knownFirmware(),"Connection reset clears telemetry");
        System.out.println("PASS: "+checks+" S4 channel and rowing-stat checks");
    }
}
