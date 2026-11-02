package org.openrow.probe;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.util.Properties;

/** Game checkpoint commits first; replayable source counters make interrupted imports safe. */
public final class AthleteStore {
    private static AtomicFile file(Context c){return new AtomicFile(new File(c.getFilesDir(),"athlete-history-v1.properties"));}
    public static AthleteHistory read(Context c)throws IOException{
        Properties p=new Properties();try(FileInputStream in=file(c).openRead()){p.load(in);return new AthleteHistory(p);}catch(FileNotFoundException absent){return new AthleteHistory();}catch(RuntimeException bad){throw new IOException("Athlete history could not be read",bad);}
    }
    private static void write(Context c,AthleteHistory history)throws IOException{
        AtomicFile file=file(c);FileOutputStream out=null;try{out=file.startWrite();history.data.store(out,"OpenRow shared athlete history");file.finishWrite(out);}catch(IOException bad){if(out!=null)file.failWrite(out);throw bad;}
    }
    public static synchronized AthleteHistory sync(Context c,Properties source)throws IOException{
        AthleteHistory h=read(c);try{h.sync(source,false);}catch(RuntimeException bad){throw new IOException("Invalid rowing source",bad);}write(c,h);return h;
    }
    public static synchronized AthleteHistory refresh(Context c)throws IOException{
        Properties recorder=WorkoutStore.read(new File(c.getFilesDir(),"workouts/state.properties"));
        if(Boolean.parseBoolean(recorder.getProperty("migrated","false")))return read(c);
        Properties source=new Properties();try(FileInputStream in=new AtomicFile(new File(c.getFilesDir(),"gravity-real-v1.properties")).openRead()){source.load(in);}catch(FileNotFoundException absent){return read(c);}
        // Older builds saved one most recent session but no completed-workout receipt.
        try{if(!Boolean.parseBoolean(source.getProperty("game.running","true"))&&Double.parseDouble(source.getProperty("game.v4","0"))>0&&!source.containsKey("last.id")){
            source.setProperty("last.id","legacy-"+source.getProperty("game.v3","0"));source.setProperty("last.distance",source.getProperty("game.v4","0"));source.setProperty("last.seconds",source.getProperty("game.v6","0"));
            String[] counts=source.getProperty("game.counts","0,0").split(",");source.setProperty("last.strokes",counts.length>1?counts[1]:"0");
        }}catch(RuntimeException bad){throw new IOException("Invalid saved workout",bad);}
        return sync(c,source);
    }
    public static synchronized AthleteHistory reset(Context c)throws IOException{AthleteHistory h=refresh(c);h.resetTotals(System.currentTimeMillis());write(c,h);return h;}
    public static synchronized void syncSession(Context c,WorkoutSession session)throws IOException{AthleteHistory h=read(c);Properties receipt=session.credit();for(String m:AthleteHistory.METRICS)receipt.setProperty("full."+m,""+session.total(m));h.syncSession(receipt);write(c,h);}
}
