package org.openrow.probe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Profile-local atomic summaries and committed append-only CSV timelines. */
public final class WorkoutStore {
    private final File directory;
    public final Properties state;
    public WorkoutStore(File root)throws IOException{directory=new File(root,"workouts");if(!directory.isDirectory()&&!directory.mkdirs())throw new IOException("Workout storage unavailable");state=read(new File(directory,"state.properties"));if(!state.isEmpty()&&!"1".equals(state.getProperty("version")))throw new IOException("Unknown workout store");state.setProperty("version","1");}
    public static Properties read(File file)throws IOException{
        File backup=new File(file.getPath()+".bak");if(backup.exists()){if(file.exists()&&!file.delete())throw new IOException("Recovery failed");if(!backup.renameTo(file))throw new IOException("Recovery failed");}
        Properties p=new Properties();if(file.exists())try(InputStream in=new FileInputStream(file)){p.load(in);}catch(RuntimeException bad){throw new IOException("Invalid workout data",bad);}return p;
    }
    public static void atomic(File file,Properties data)throws IOException{
        File temp=new File(file.getPath()+".new"),backup=new File(file.getPath()+".bak");
        if(backup.exists())read(file);
        try(FileOutputStream out=new FileOutputStream(temp)){data.store(out,"OpenRow workout v1");out.getFD().sync();}
        if(file.exists()&&!file.renameTo(backup))throw new IOException("Workout backup failed");
        if(!temp.renameTo(file)){if(backup.exists())backup.renameTo(file);throw new IOException("Workout commit failed");}
        if(backup.exists()&&!backup.delete())throw new IOException("Workout cleanup failed");
    }
    private File file(String id,String extension){if(!WorkoutSession.validId(id))throw new IllegalArgumentException("Invalid workout ID");return new File(directory,id+extension);}
    public WorkoutSession active()throws IOException{String id=state.getProperty("active");if(id==null)return null;Properties p=read(file(id,".properties"));if(p.isEmpty())throw new IOException("Missing active workout");try{return new WorkoutSession(p);}catch(RuntimeException bad){throw new IOException("Workout could not be restored",bad);}}
    public void save(WorkoutSession session)throws IOException{
        File samples=file(session.id(),".csv");long committed=(long)session.number("bytes");
        try(RandomAccessFile out=new RandomAccessFile(samples,"rw")){
            if(out.length()<committed)throw new IOException("Workout timeline is incomplete");out.setLength(committed);out.seek(committed);
            for(String row:session.pending)out.write((row+"\n").getBytes(StandardCharsets.UTF_8));out.getFD().sync();
            Properties next=new Properties();next.putAll(session.data);next.setProperty("bytes",""+out.length());
            atomic(file(session.id(),".properties"),next);session.data.putAll(next);session.pending.clear();
        }
        if(session.finished()){if(session.id().equals(state.getProperty("active")))state.remove("active");}else state.setProperty("active",session.id());atomic(new File(directory,"state.properties"),state);
    }
    public void saveState()throws IOException{atomic(new File(directory,"state.properties"),state);}
    public List<Properties> history()throws IOException{
        List<Properties> result=new ArrayList<>();File[] files=directory.listFiles((d,n)->n.endsWith(".properties")&&!n.equals("state.properties"));if(files==null)throw new IOException("History unavailable");
        for(File file:files){Properties p=read(file);if("true".equals(p.getProperty("finished")))result.add(p);}
        result.sort((a,b)->Long.compare(Long.parseLong(b.getProperty("started","0")),Long.parseLong(a.getProperty("started","0"))));return result;
    }
    public List<String[]> samples(String id)throws IOException{
        Properties p=read(file(id,".properties"));long size=Long.parseLong(p.getProperty("bytes","0"));List<String[]> rows=new ArrayList<>();
        try(RandomAccessFile in=new RandomAccessFile(file(id,".csv"),"r")){while(in.getFilePointer()<size){String line=in.readLine();if(line==null)break;if(in.getFilePointer()>size)break;String[] parts=line.split(",",-1);if(parts.length==10)rows.add(parts);}}return rows;
    }
}
