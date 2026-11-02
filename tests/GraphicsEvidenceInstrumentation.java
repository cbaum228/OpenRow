package org.openrow.probe.telemetrytests;

import android.app.Instrumentation;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import java.io.*;

/** Debug-only extraction of native video time sequences; never opens a career or athlete store. */
public final class GraphicsEvidenceInstrumentation extends Instrumentation {
    private String clip;
    @Override public void onCreate(Bundle args){super.onCreate(args);clip=args.getString("clip","");start();}
    @Override public void onStart(){Bundle result=new Bundle();MediaMetadataRetriever decoder=new MediaMetadataRetriever();try{
        if(!clip.matches("openrow-021-[a-z-]+"))throw new IllegalArgumentException("Use a named graphics fixture");
        decoder.setDataSource(new File(getTargetContext().getExternalFilesDir(null),"graphics-input/"+clip+".mp4").getAbsolutePath());File directory=new File(getTargetContext().getExternalFilesDir(null),"graphics-review");if(!directory.isDirectory()&&!directory.mkdirs())throw new IOException("Cannot create evidence output");
        long duration=Long.parseLong(decoder.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));result.putLong("durationMs",duration);if(duration<1000)throw new IOException("Invalid clip duration");java.util.Set<Long> hashes=new java.util.HashSet<>();
        for(int i=0;i<8;i++){long time=Math.min(duration-100,500+i*1500);Bitmap frame=decoder.getFrameAtTime(time*1000,MediaMetadataRetriever.OPTION_CLOSEST);if(frame==null)throw new IOException("Missing video frame");Bitmap small=Bitmap.createScaledBitmap(frame,1280,720,true);long hash=17;for(int y=0;y<720;y+=7)for(int x=0;x<1280;x+=7)hash=hash*31+small.getPixel(x,y);hashes.add(hash);try(FileOutputStream out=new FileOutputStream(new File(directory,clip+"-"+i+".png"))){small.compress(Bitmap.CompressFormat.PNG,100,out);}if(small!=frame)small.recycle();frame.recycle();}
        if(hashes.size()<2)throw new IOException("Decoder returned duplicate frames; do not use as motion evidence. Capture direct timed screenshots instead.");
        result.putString("stream","PASS: extracted eight timestamped frames from "+clip+"; spacing 1.5 seconds, first at 0.5 seconds\n");finish(-1,result);
    }catch(Throwable bad){result.putString("stream",android.util.Log.getStackTraceString(bad));finish(0,result);}finally{try{decoder.release();}catch(IOException ignored){}}}
}
