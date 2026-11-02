package org.openrow.probe;

import java.nio.file.*;
import java.util.*;

public final class RowingMetricsTest {
    private static int assertions;
    private static void check(boolean value,String message) {
        assertions++; if(!value) throw new AssertionError(message);
    }
    private static void close(double expected,double actual,String message) {
        check(Math.abs(expected-actual)<0.001,message+": "+actual);
    }
    private static RowingMetrics ready() { RowingMetrics m=new RowingMetrics();m.setConnected(true);return m; }
    public static void main(String[] args) throws Exception {
        RowingMetrics m=ready();
        check(Double.isNaN(m.cadence(0)),"No cadence before strokes");
        m.accept("SS",1000); check(Double.isNaN(m.cadence(1000)),"One start cannot establish cadence");
        m.accept("SE",1700); check(Double.isNaN(m.cadence(1700)),"End events do not count as starts");
        m.accept("SS",3000); close(30,m.cadence(3000),"Two starts give immediate cadence");
        check(!m.accept("SS",3000),"Same-timestamp duplicate ignored");
        close(30,m.cadence(3000),"Same-timestamp duplicate preserves cadence");
        check(!m.accept("SS",3100),"Duplicate start rejected");
        m.accept("SS",6000); close(24,m.cadence(6000),"Mean of two intervals"); close(20,m.immediateCadence(6000),"Latest interval differs from smoothed");
        m.accept("SS",9000); m.accept("SS",12000); close(20,m.cadence(12000),"Only last three intervals used");
        check(m.isIdle(19500),"Idle boundary is inclusive");
        check(Double.isNaN(m.cadence(19500)),"Idle clears displayed cadence");
        m.accept("SS",22000); check(Double.isNaN(m.cadence(22000)),"Pause does not become a cadence interval");
        m.accept("SS",24000); close(30,m.cadence(24000),"Resume starts a new average");
        m.accept("IDD057000F",24000); m.accept("IDD1400005",24000); m.accept("IDS1A900",24000);
        check(m.distance()==15 && m.strokes()==5,"Hex monitor totals decoded");
        close(30,m.cadence(24000),"Zero S4 cadence cannot override calculated cadence");
        m.accept("IDD057ZZZZ",24000); m.accept("IDD14000",24000); m.accept("IDD057000Fextra",24000);
        check(m.distance()==15 && m.strokes()==5,"Malformed replies ignored");
        m.accept("IDD1400000",24000); check(Double.isNaN(m.cadence(24000)),"S4 reset clears interval history");
        m.accept("SS",25000); m.accept("SS",27000); m.setConnected(false);
        check(Double.isNaN(m.cadence(27000)),"Disconnected cadence unavailable");
        check(!m.accept("SS",29000),"Disconnected events ignored");
        check(m.distance()==15,"Last totals retained when disconnected");
        m.reset(); check(m.distance()==-1 && m.strokes()==-1,"New connection clears old totals");
        m=ready(); m.accept("SS",0); m.accept("SS",6000); close(10,m.cadence(6000),"Slow initial cadence supported");
        m.accept("SS",12000); close(10,m.cadence(12000),"Adaptive idle window supports slow rowing");
        m.accept("SS",11000); check(Double.isNaN(m.cadence(11000)),"Clock discontinuity clears history");
        if(args.length>0) replay(args[0]);
        System.out.println("PASS: "+assertions+" assertions");
    }
    private static void replay(String file) throws Exception {
        RowingMetrics m=ready(); int starts=0; double lastCadence=Double.NaN;
        for(String line:Files.readAllLines(Paths.get(file))) {
            int rx=line.indexOf("  RX  "); if(rx<0) continue;
            String[] t=line.substring(0,12).split("[:.]");
            long timestamp=((Long.parseLong(t[0])*60+Long.parseLong(t[1]))*60+Long.parseLong(t[2]))*1000+Long.parseLong(t[3]);
            String message=line.substring(rx+6).trim();
            if(m.accept(message,timestamp)) {starts++;lastCadence=m.cadence(timestamp);}
        }
        check(m.distance()==15 && m.strokes()==5,"Real S4 recording decodes 15m and five strokes");
        check(starts>=4 && lastCadence>15 && lastCadence<60,"Real stroke events produce plausible cadence");
        System.out.printf(Locale.US,"Recorded S4 replay: %d start events, final moving cadence %.1f spm%n",starts,lastCadence);
    }
}
