package org.openrow.probe;

import java.util.*;

/** Stable local identities. Display names never become filesystem paths. */
public final class FamilyProfiles {
    public final Properties data=new Properties();
    public FamilyProfiles(){data.setProperty("version","1");data.setProperty("ids","legacy");data.setProperty("active","legacy");data.setProperty("legacy.name","Your profile");}
    public FamilyProfiles(Properties saved){
        data.putAll(saved);
        if(!"1".equals(data.getProperty("version")))throw new IllegalArgumentException("Unknown profile version");
        Set<String> seen=new HashSet<>();
        for(String id:ids()){if(!validId(id)||!seen.add(id))throw new IllegalArgumentException("Invalid profile identity");name(data.getProperty(id+".name"));}
        if(!seen.contains(active()))throw new IllegalArgumentException("Missing active profile");
    }
    public static boolean validId(String id){return "legacy".equals(id)||(id!=null&&id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));}
    private static String name(String value){if(value==null||value.trim().isEmpty()||value.trim().length()>32||value.matches("(?s).*[\\p{Cntrl}].*"))throw new IllegalArgumentException("Enter a name of 1–32 characters");return value.trim();}
    public String[] ids(){return data.getProperty("ids","").split(",");}
    public String active(){return data.getProperty("active");}
    public String label(String id){return data.getProperty(id+".name");}
    public boolean ready(){return Boolean.parseBoolean(data.getProperty(active()+".ready","false"));}
    public void select(String id){if(!Arrays.asList(ids()).contains(id))throw new IllegalArgumentException("Unknown profile");data.setProperty("active",id);}
    public void configure(String id,String label){if(!Arrays.asList(ids()).contains(id))throw new IllegalArgumentException("Unknown profile");data.setProperty(id+".name",name(label));data.setProperty(id+".ready","true");}
    public String add(String label){String clean=name(label),id=UUID.randomUUID().toString();data.setProperty("ids",data.getProperty("ids")+","+id);data.setProperty(id+".name",clean);return id;}
}
