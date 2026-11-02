package org.openrow.probe;

import java.util.*;

/** S4 v2 register observations. Values retain their original payload and monotonic timestamp. */
public final class S4Channels {
    public static final class Sample {
        public final int raw; public final long at; public final String payload;
        Sample(int raw,long at,String payload){this.raw=raw;this.at=at;this.payload=payload;}
    }
    private final Map<String,Sample> samples=new LinkedHashMap<>();
    private String firmware="";
    private long start=-1,end=-1,pulseAt=-1;
    private double drive=Double.NaN,recovery=Double.NaN;
    private long pulses;
    // Each slot has distance and power; slow channels rotate without starving game input.
    private static final String[] SLOW={"IRD140","IRS1A9","IRD148","IRD14A","IRS1A0","IRT08A","IRT1E1","IRS0A9","IRS083","IRS084","IRD1A1","IRD1A5","IRD1A7","IRS142","IRS143","IRD057","IRS049"};
    private int slot;
    public String nextQuery(){int n=slot++;switch(n%6){case 0:return "IRS00D";case 1:return "IRS041";case 2:return "IRD088";case 3:return "IRD055";case 4:return SLOW[(n/6)%SLOW.length];default:return "IRS041";}}
    public void reset(){samples.clear();firmware="";start=end=pulseAt=-1;pulses=0;drive=recovery=Double.NaN;slot=0;}
    public boolean knownFirmware(){return "IV40210".equals(firmware);}
    public String firmware(){return firmware;}
    public Sample get(String address){return samples.get(address);}
    public boolean fresh(String address,long now,long age){Sample s=get(address);return s!=null&&now>=s.at&&now-s.at<=age;}
    public Map<String,Sample> observations(){return Collections.unmodifiableMap(samples);}
    public void accept(String line,long now,boolean acceptedStart){
        if(line.matches("IV[45][0-9]{4}"))firmware=line;
        if(acceptedStart){if(end>=0&&now>end&&now-end<15000)recovery=(now-end)/1000.0;else recovery=Double.NaN;start=now;end=-1;}
        if(line.equals("SE")&&start>=0&&end<0&&now>start&&now-start<10000){drive=(now-start)/1000.0;end=now;}
        if(line.matches("P[0-9A-Fa-f]{2}")){int p=Integer.parseInt(line.substring(1),16);pulses+=p;pulseAt=now;samples.put("pulse",new Sample(p,now,line.substring(1)));return;}
        if(!line.matches("ID[SDT][0-9A-Fa-f]{3}[0-9A-Fa-f]+"))return;
        int width=line.charAt(2)=='S'?2:line.charAt(2)=='D'?4:6;
        if(line.length()!=6+width)return;
        String key=line.substring(3,6).toUpperCase(Locale.US),payload=line.substring(6);
        // Reject a wrong-width response instead of silently replacing the channel.
        String expected=width(key);if(expected==null||expected.charAt(0)!=line.charAt(2))return;
        samples.put(key,new Sample(Integer.parseInt(payload,16),now,payload));
    }
    private String width(String a){if(Arrays.asList("055","057","088","140","148","14A","1A1","1A5","1A7").contains(a))return "D";if(a.equals("08A")||a.equals("1E1"))return "T";if(Arrays.asList("00D","041","049","0A9","083","084","1A0","1A9","142","143").contains(a))return "S";return null;}
    public double watts(long now){
        // 088 is shared kcal/watts. Require fresh, normal-screen, non-average watts flags.
        if(!knownFirmware()||!fresh("088",now,1500)||!fresh("041",now,1500)||!fresh("00D",now,1500))return Double.NaN;
        if((get("00D").raw&0xf0)!=0||(get("041").raw&0xb0)!=0x10)return Double.NaN;
        int value=get("088").raw;return value<=3000?value:Double.NaN;
    }
    public double speed(long now){return knownFirmware()&&fresh("148",now,10000)&&get("148").raw<=1500?get("148").raw/100.0:Double.NaN;}
    public double pace(long now){double s=speed(now);return s>0?500/s:Double.NaN;}
    public double heartRate(long now){return knownFirmware()&&fresh("1A0",now,10000)&&get("1A0").raw>=30&&get("1A0").raw<=240?get("1A0").raw:Double.NaN;}
    public double calories(long now){return knownFirmware()&&fresh("08A",now,10000)?get("08A").raw/1000.0:Double.NaN;}
    public double clock(long now){
        if(!knownFirmware()||!fresh("1E1",now,10000))return Double.NaN;
        String s=get("1E1").payload;if(!s.matches("[0-9]{6}"))return Double.NaN;
        int h=Integer.parseInt(s.substring(0,2)),m=Integer.parseInt(s.substring(2,4)),sec=Integer.parseInt(s.substring(4,6));
        return h<=9&&m<60&&sec<60?h*3600+m*60+sec:Double.NaN;
    }
    public long pulses(){return pulses;}
    public double pulseRate(long now){return pulseAt>=0&&now>=pulseAt&&now-pulseAt<=500?get("pulse").raw*40.0:Double.NaN;}
    public double driveSeconds(){return drive;}
    public long lastEnd(){return end;}
    public double recoverySeconds(){return recovery;}
    public long lastStart(){return start;}
    public double tankLiters(long now){return knownFirmware()&&fresh("0A9",now,10000)?get("0A9").raw/10.0:Double.NaN;}
    public void disconnected(){start=end=pulseAt=-1;drive=recovery=Double.NaN;}
}
