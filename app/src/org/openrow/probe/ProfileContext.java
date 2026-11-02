package org.openrow.probe;

import android.content.*;
import java.io.File;

/** Immutable storage scope: even callbacks from an old screen retain their owner. */
public final class ProfileContext extends ContextWrapper {
    private final String id;
    public ProfileContext(Context base,String id){super(base);if(!FamilyProfiles.validId(id))throw new IllegalArgumentException("Invalid profile");this.id=id;}
    @Override public File getFilesDir(){
        if(id.equals("legacy"))return super.getFilesDir();
        File directory=new File(new File(super.getFilesDir(),"profiles"),id);
        if(!directory.isDirectory()&&!directory.mkdirs())throw new IllegalStateException("Profile storage unavailable");return directory;
    }
    @Override public SharedPreferences getSharedPreferences(String name,int mode){return super.getSharedPreferences(id.equals("legacy")?name:"profile_"+id+"_"+name,mode);}
}
