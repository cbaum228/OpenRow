package org.openrow.probe;

import java.util.Properties;

public final class FamilyProfilesTest {
    private static int checks;
    private static void check(boolean good){checks++;if(!good)throw new AssertionError("Check "+checks);}
    private static void rejects(Runnable action){boolean rejected=false;try{action.run();}catch(IllegalArgumentException expected){rejected=true;}check(rejected);}
    public static void main(String[] args){
        FamilyProfiles family=new FamilyProfiles();check(family.active().equals("legacy"));check(!family.ready());
        rejects(()->family.configure("legacy"," "));rejects(()->family.add("bad\nname"));rejects(()->family.add(new String(new char[33]).replace('\0','a')));
        family.configure("legacy","  Alex  ");check(family.label("legacy").equals("Alex"));check(family.ready());
        String second=family.add("Sam");check(FamilyProfiles.validId(second));check(!second.equals("legacy"));check(family.active().equals("legacy"));
        family.select(second);check(!family.ready());family.configure(second,"Sam");check(family.ready());
        FamilyProfiles restored=new FamilyProfiles(family.data);check(restored.active().equals(second));check(restored.ids().length==2);
        restored.configure(second,"Alex");check(restored.label("legacy").equals("Alex"));check(restored.ids().length==2); // duplicate names retain distinct IDs
        restored.select("legacy");check(family.active().equals(second));check(restored.ready());
        rejects(()->family.select("missing"));check(!FamilyProfiles.validId("../outside"));check(!FamilyProfiles.validId("C:\\files"));
        Properties bad=new Properties();bad.putAll(family.data);bad.setProperty("ids","legacy,legacy");rejects(()->new FamilyProfiles(bad));
        bad.putAll(family.data);bad.setProperty("active","missing");rejects(()->new FamilyProfiles(bad));
        bad.putAll(family.data);bad.setProperty("version","2");rejects(()->new FamilyProfiles(bad));
        System.out.println("Family profiles: "+checks+" checks passed");
    }
}
