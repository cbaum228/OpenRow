package org.openrow.probe;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.util.Properties;

/** One atomic commit contains history and the active checkpoint, independent of game saves. */
public final class TrainingStore {
    public final TrainingCore.History history;
    public TrainingCore.Runner active;
    private final AtomicFile file;
    public TrainingStore(Context context)throws IOException {
        file=new AtomicFile(new File(context.getFilesDir(),"training-v1.properties"));
        Properties p=new Properties();
        try(FileInputStream in=file.openRead()){p.load(in);}catch(FileNotFoundException absent){}
        try {
            Properties h=extract(p,"history.");history=h.isEmpty()?new TrainingCore.History():new TrainingCore.History(h);
            Properties r=extract(p,"active.");if(!r.isEmpty())active=TrainingCore.Runner.restore(r);
            if(history.data.containsKey("pendingEnded")&&Long.parseLong(history.data.getProperty("pendingEnded"))<0)throw new IllegalArgumentException("Invalid result date");
        }catch(RuntimeException bad){throw new IOException("Training data could not be read; original retained",bad);}
    }
    private static Properties extract(Properties p,String prefix){Properties out=new Properties();
        for(String k:p.stringPropertyNames())if(k.startsWith(prefix))out.setProperty(k.substring(prefix.length()),p.getProperty(k));return out;}
    private static void include(Properties out,Properties p,String prefix){for(String k:p.stringPropertyNames())out.setProperty(prefix+k,p.getProperty(k));}
    public void save()throws IOException {
        Properties p=new Properties();include(p,history.data,"history.");if(active!=null)include(p,active.save(),"active.");
        FileOutputStream out=null;try{out=file.startWrite();p.store(out,"OpenRow training: experimental foundation policy; game-independent history");file.finishWrite(out);}
        catch(IOException bad){if(out!=null)file.failWrite(out);throw bad;}
    }
    public void record(int effort,int manageable)throws IOException {
        if(active==null||!active.finished)return;
        long ended=Math.max(active.started,Long.parseLong(history.data.getProperty("pendingEnded",""+System.currentTimeMillis())));
        history.put(new TrainingCore.Record(active,ended,effort,manageable),active);
        save();
    }
    public void clearRecorded()throws IOException {
        TrainingCore.Runner previous=active;active=null;
        try{save();}catch(IOException bad){active=previous;throw bad;}
    }
}
