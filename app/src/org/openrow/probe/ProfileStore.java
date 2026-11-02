package org.openrow.probe;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.util.Properties;

public final class ProfileStore {
    private final AtomicFile file;
    public ProfileStore(Context context){file=new AtomicFile(new File(context.getFilesDir(),"family-profiles-v1.properties"));}
    public FamilyProfiles read()throws IOException{
        Properties p=new Properties();try(FileInputStream in=file.openRead()){p.load(in);return new FamilyProfiles(p);}
        catch(FileNotFoundException absent){return new FamilyProfiles();}
        catch(RuntimeException bad){throw new IOException("Profiles could not be read; original retained",bad);}
    }
    public void save(FamilyProfiles profiles)throws IOException{
        FileOutputStream out=null;try{out=file.startWrite();profiles.data.store(out,"OpenRow local family profiles");file.finishWrite(out);}
        catch(IOException bad){if(out!=null)file.failWrite(out);throw bad;}
    }
}
