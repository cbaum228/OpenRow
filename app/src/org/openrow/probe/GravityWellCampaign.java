package org.openrow.probe;

import java.util.*;

/** Versioned expedition progression; resource provenance and rewards are independent of rendering. */
public final class GravityWellCampaign {
    public static final String[] CHAPTERS={"Debris field","Planetary systems","Giant worlds","First starlight","Solar furnace","Giant’s envelope","Compact remnants","Pulsar frontier"};
    public static final String[] PRESTIGE_REWARDS={"none","violet_accretion","cyan_horizon","legacy_constellation"};
    public static final String[] MATERIALS={"Minerals","Volatiles","Giant gas","Dense minerals","Comet ice","Dwarf cores","Planetary cores","Ring debris","Giant envelopes","Ice condensates","Dwarf plasma","Red plasma","Solar plasma","Giant plasma","Degenerate matter","Neutron matter"};
    public static final String[] SOURCES={"rocky bodies","icy bodies","gas bodies","metallic asteroids","comets","dwarf planets","rocky planets","ringed giants","gas giants","ice giants","brown dwarfs","red dwarfs","sun-like stars","red giants","white dwarfs","neutron stars"};
    public static final int[] BOSSES={7,8,9,10,11,12,13,14,15,16};
    public static final int[] STAGE_CHAPTERS={0,1,2,2,3,3,4,5,6,7};
    /** V1 durations retained solely to migrate completion percentages. */
    public static final int[] PREPARATION_MINUTES={30,40,25,25,60,90,120,150,180,240};
    public static final String[] STAGES={"debris","planetary","gas","ice","brown","red_dwarf","solar","red_giant","white_dwarf","neutron"};
    public static final class Destination {
        public final String id,title; public final int chapter,boss; public final double seconds;
        Destination(String id,String title,int chapter,int boss,double seconds){this.id=id;this.title=title;this.chapter=chapter;this.boss=boss;this.seconds=seconds;}
    }
    public static final Destination[] DESTINATIONS=destinations();
    private static Destination[] destinations(){
        Destination[] out=new Destination[BOSSES.length*4];String[] steps={"Survey","Harvest","Research"};
        for(int stage=0;stage<BOSSES.length;stage++){
            for(int step=0;step<3;step++)out[stage*4+step]=new Destination(STAGES[stage]+"_"+steps[step].toLowerCase(Locale.US),steps[step]+" · "+GravityWellGame.KINDS[BOSSES[stage]].name+" approach",STAGE_CHAPTERS[stage],-1,GravityWellBalance.preparationSeconds(stage));
            out[stage*4+3]=new Destination(STAGES[stage]+"_boss",GravityWellGame.KINDS[BOSSES[stage]].name,STAGE_CHAPTERS[stage],BOSSES[stage],0);
        }return out;
    }
    public int cursor; public double progress,stageEligible; public String selected="free";
    public final double[] materials=new double[MATERIALS.length];
    public final Set<String> cleared=new HashSet<>();
    public final Set<Long> rewarded=new HashSet<>();
    public int legacyMask; public long lastRewardGroup,legacyRewardCeiling,encounterGroup; public String encounter="";
    public boolean synthetic,autoQueue; public double acceleration=1; public int cosmeticPreview,preparationTarget=-1;
    private boolean queuePreferenceExplicit;
    public void setAutoQueue(boolean value){autoQueue=value;queuePreferenceExplicit=true;}
    public void applyCoachDefault(boolean coach){if(!queuePreferenceExplicit)autoQueue=coach;}
    public int chapter(){return complete()?CHAPTERS.length:DESTINATIONS[cursor].chapter+1;}
    public int stage(){return Math.min(9,cursor/4);}
    public boolean complete(){return cursor==DESTINATIONS.length;}
    public Destination next(){return complete()?null:DESTINATIONS[cursor];}
    public String title(){if(selected.equals("free"))return "Free play";if(selected.startsWith("repeat:"))return "Harvest · "+GravityWellGame.KINDS[repeatKind()].name;return DESTINATIONS[index(selected)].title;}
    public boolean selectedComplete(){return !selected.equals("free")&&!selected.startsWith("repeat:")&&index(selected)<cursor;}
    public boolean earlyBoss(){return cursor<3&&stageEligible>=25*60-1e-6;}
    public boolean select(String id,boolean running,long activeBoss){
        if(id==null||running||activeBoss>0)return false;
        if(id.equals("free")){selected=id;preparationTarget=next()!=null&&next().boss<0?cursor:-1;return true;}
        if(id.startsWith("repeat:")){int kind=kindId(id.substring(7));if(kind<0||!GravityWellGame.KINDS[kind].boss||!cleared.contains(GravityWellGame.KINDS[kind].id)&&!legacyAccess(kind))return false;selected=id;preparationTarget=next()!=null&&next().boss<0?cursor:-1;return true;}
        int i=index(id);if(i<0||complete()||i!=cursor&&!(i==3&&earlyBoss()))return false;
        selected=id;preparationTarget=DESTINATIONS[i].boss<0&&next().boss<0?cursor:-1;return true;
    }
    private int repeatKind(){return kindId(selected.substring(7));}
    public int pendingBoss(){if(selected.startsWith("repeat:"))return repeatKind();int i=index(selected);return i==cursor&&i>=0?DESTINATIONS[i].boss:i==3&&earlyBoss()?BOSSES[0]:-1;}
    public void tick(double dt,boolean eligible){
        if(!eligible||!Double.isFinite(dt)||dt<=0||complete())return;
        double amount=dt*(synthetic?acceleration:1);stageEligible+=amount;
        int i=index(selected);if(next().boss>=0||!(i==cursor||preparationTarget==cursor))return;
        progress=Math.min(next().seconds,progress+amount);
        if(progress+1e-6>=next().seconds){materials[next().chapter<3?next().chapter:2]+=1;cursor++;progress=0;preparationTarget=-1;
            if(autoQueue&&next()!=null&&next().boss<0){preparationTarget=cursor;if(i>=0)selected=next().id;}}
    }
    public void beginBoss(long group){encounterGroup=group;encounter=selected;}
    public void clearEncounter(){encounterGroup=0;encounter="";}
    public boolean claimed(long group){return group<=legacyRewardCeiling||rewarded.contains(group);}
    public void consumed(long group,int kind){
        if(group<=0||kind<0||kind>=GravityWellGame.KINDS.length||!GravityWellGame.KINDS[kind].boss)throw new IllegalArgumentException("Invalid boss completion");
        if(claimed(group))return;rewarded.add(group);lastRewardGroup=Math.max(lastRewardGroup,group);
        award(kind,GravityWellBalance.reserve(kind)*.25);
        boolean first=false;
        if(group==encounterGroup){int i=index(encounter);
            if(i>=0&&i/4==cursor/4&&DESTINATIONS[i].boss==kind&&(i==cursor||i==3&&earlyBoss())){first=cleared.add(GravityWellGame.KINDS[kind].id);cursor=i+1;progress=stageEligible=0;preparationTarget=-1;}
            clearEncounter();}
        if(first){award(kind,GravityWellBalance.firstBonus(kind));if(autoQueue&&next()!=null&&next().boss<0){selected=next().id;preparationTarget=cursor;}}
        if(selected.startsWith("repeat:"))selected=preparationTarget==cursor&&next()!=null?next().id:"free";
    }
    private void award(int kind,double amount){materials[material(kind)]+=amount;int source=specialMaterial(kind);if(source>=0)materials[source]+=amount;if(kind==9||kind==10)materials[7]+=amount*.25;}
    public void harvested(int kind,double fraction,double scale){if(!Double.isFinite(fraction)||fraction<0||!Double.isFinite(scale)||scale<0||scale>1.440001)throw new IllegalArgumentException("Invalid harvest");if(fraction>0)award(kind,GravityWellBalance.reserve(kind)*.75*fraction*scale);}
    public static int material(int kind){return kind==3||kind==5||kind==10?1:kind==6||kind==9||kind>=11?2:0;}
    public static int specialMaterial(int kind){return kind==2?3:kind==3?4:kind==7?5:kind==8?6:kind==9?8:kind==10?9:kind>=11&&kind<=16?kind-1:-1;}
    public void stripped(int kind,double mass,double yield){if(mass>0&&Double.isFinite(mass)&&Double.isFinite(yield)&&yield>=0){double amount=mass/100*yield;materials[material(kind)]+=amount;int source=specialMaterial(kind);if(source>=0)materials[source]+=amount;if(kind==9||kind==10)materials[7]+=amount*.25;}}
    public boolean discovered(int kind){if((legacyMask&(1<<kind))!=0)return true;if(GravityWellGame.KINDS[kind].boss)return cleared.contains(GravityWellGame.KINDS[kind].id);return kind<=5||kind==6&&chapter()>=3;}
    public boolean ordinaryAllowed(int kind){if(!discovered(kind))return false;if(selected.equals("free")||pendingBoss()>=0||selectedComplete()||cursor%4!=1)return true;return chapter()==1?kind==2||kind==1:chapter()==2?kind==4||kind==5:kind==6||kind==3;}
    public boolean legacyAccess(int kind){return (legacyMask&(1<<kind))!=0;}
    public static int index(String id){for(int i=0;i<DESTINATIONS.length;i++)if(DESTINATIONS[i].id.equals(id))return i;return -1;}
    private static int kindId(String id){for(int i=0;i<GravityWellGame.KINDS.length;i++)if(GravityWellGame.KINDS[i].id.equals(id))return i;return -1;}
    public boolean checkpoint(int stage){
        if(!synthetic||stage<0||stage>BOSSES.length)return false;cursor=stage==BOSSES.length?DESTINATIONS.length:stage*4+3;progress=stageEligible=0;cleared.clear();clearEncounter();preparationTarget=-1;
        for(int i=0;i<stage;i++)cleared.add(GravityWellGame.KINDS[BOSSES[i]].id);selected=complete()?"free":next().id;for(int i=0;i<MATERIALS.length;i++)materials[i]=20;return true;
    }
    public void save(Properties p){
        p.setProperty("campaign.version","2");p.setProperty("campaign.balanceVersion","2");p.setProperty("campaign.cursor",""+cursor);p.setProperty("campaign.progress",""+progress);p.setProperty("campaign.selected",selected);
        p.setProperty("campaign.stageEligible",""+stageEligible);p.setProperty("campaign.preparationTarget",""+preparationTarget);p.setProperty("campaign.autoQueue",""+autoQueue);
        p.setProperty("campaign.queueExplicit",""+queuePreferenceExplicit);
        p.setProperty("campaign.rewardGroup",""+lastRewardGroup);p.setProperty("campaign.rewardCeiling",""+legacyRewardCeiling);p.setProperty("campaign.rewarded",joinIds(rewarded));
        p.setProperty("campaign.legacyMask",""+legacyMask);p.setProperty("campaign.encounterGroup",""+encounterGroup);p.setProperty("campaign.encounter",encounter);
        for(int i=0;i<MATERIALS.length;i++)p.setProperty("campaign.material."+i,""+materials[i]);p.setProperty("campaign.cleared",String.join(",",new TreeSet<>(cleared)));
        p.setProperty("campaign.cosmeticId",PRESTIGE_REWARDS[synthetic?cosmeticPreview:0]);p.setProperty("campaign.acceleration",""+(synthetic?acceleration:1));p.setProperty("campaign.cosmetic",""+(synthetic?cosmeticPreview:0));
    }
    private static String joinIds(Set<Long> ids){List<String> out=new ArrayList<>();for(long id:new TreeSet<>(ids))out.add(""+id);return String.join(",",out);}
    private static double finite(Properties p,String k,String fallback){double d=Double.parseDouble(p.getProperty(k,fallback));if(!Double.isFinite(d)||d<0)throw new IllegalArgumentException("Invalid "+k);return d;}
    public static GravityWellCampaign restore(Properties p,boolean synthetic){
        String version=p.getProperty("campaign.version");if(!"1".equals(version)&&!"2".equals(version))throw new IllegalArgumentException("Unknown campaign version");
        if("2".equals(version)&&!"2".equals(p.getProperty("campaign.balanceVersion")))throw new IllegalArgumentException("Unknown balance version");
        GravityWellCampaign c=new GravityWellCampaign();c.synthetic=synthetic;c.cursor=Integer.parseInt(p.getProperty("campaign.cursor"));
        if(c.cursor<0||c.cursor>DESTINATIONS.length)throw new IllegalArgumentException("Invalid cursor");
        c.progress=finite(p,"campaign.progress","0");c.selected=p.getProperty("campaign.selected");
        if("1".equals(version)&&!c.complete()&&c.next().boss<0)c.progress=c.progress/(PREPARATION_MINUTES[c.stage()]*60)*c.next().seconds;
        c.stageEligible=finite(p,"campaign.stageEligible",""+(c.complete()?0:(c.cursor%4)*GravityWellBalance.preparationSeconds(c.stage())+c.progress));
        c.preparationTarget=Integer.parseInt(p.getProperty("campaign.preparationTarget",index(c.selected)==c.cursor&&c.next()!=null&&c.next().boss<0?""+c.cursor:"-1"));
        c.autoQueue=Boolean.parseBoolean(p.getProperty("campaign.autoQueue","false"));
        c.queuePreferenceExplicit=Boolean.parseBoolean(p.getProperty("campaign.queueExplicit","false"));
        c.lastRewardGroup=Long.parseLong(p.getProperty("campaign.rewardGroup","0"));c.legacyRewardCeiling=Long.parseLong(p.getProperty("campaign.rewardCeiling","1".equals(version)?""+c.lastRewardGroup:"0"));
        String ids=p.getProperty("campaign.rewarded","");if(!ids.isEmpty())for(String id:ids.split(",")){long n=Long.parseLong(id);if(n<=0||n>c.lastRewardGroup||!c.rewarded.add(n))throw new IllegalArgumentException("Invalid reward ID");}
        c.legacyMask=Integer.parseInt(p.getProperty("campaign.legacyMask","0"));c.encounterGroup=Long.parseLong(p.getProperty("campaign.encounterGroup","0"));c.encounter=p.getProperty("campaign.encounter","");
        c.acceleration=finite(p,"campaign.acceleration","1");c.cosmeticPreview=Integer.parseInt(p.getProperty("campaign.cosmetic","0"));
        if(c.acceleration<1||c.acceleration>120||c.cosmeticPreview<0||c.cosmeticPreview>3||!synthetic&&(c.acceleration!=1||c.cosmeticPreview!=0))throw new IllegalArgumentException("Invalid lab settings");
        if(p.containsKey("campaign.cosmeticId")&&!PRESTIGE_REWARDS[c.cosmeticPreview].equals(p.getProperty("campaign.cosmeticId")))throw new IllegalArgumentException("Unknown cosmetic reward");
        if(c.progress>0&&(c.complete()||c.next().seconds==0||c.progress>=c.next().seconds)||c.legacyMask<0||c.legacyMask>131071||c.encounterGroup<0||c.lastRewardGroup<0||c.legacyRewardCeiling<0||c.legacyRewardCeiling>c.lastRewardGroup||c.preparationTarget< -1||c.preparationTarget>=40)throw new IllegalArgumentException("Invalid campaign state");
        if(c.selected==null||!c.selected.equals("free")&&index(c.selected)<0&&!c.selected.startsWith("repeat:")||index(c.selected)>c.cursor&&!(index(c.selected)==3&&c.earlyBoss()))throw new IllegalArgumentException("Unknown or locked destination");
        for(int i=0;i<MATERIALS.length;i++)c.materials[i]=finite(p,"campaign.material."+i,i<3?null:"0");
        String wins=p.getProperty("campaign.cleared","");if(!wins.isEmpty())for(String id:wins.split(",")){int k=kindId(id),s=GravityWellBalance.stage(k);if(s<0||c.cursor<=s*4+3||!c.cleared.add(id))throw new IllegalArgumentException("Invalid clear");}
        for(int s=0;s<BOSSES.length;s++)if(c.cursor>s*4+3&&!c.cleared.contains(GravityWellGame.KINDS[BOSSES[s]].id))throw new IllegalArgumentException("Missing first clear");
        if(c.selected.startsWith("repeat:")&&(c.repeatKind()<0||!GravityWellGame.KINDS[c.repeatKind()].boss||!c.cleared.contains(c.selected.substring(7))&&!c.legacyAccess(c.repeatKind())))throw new IllegalArgumentException("Locked repeat");
        if(c.encounterGroup>0&&(c.claimed(c.encounterGroup)||!c.encounter.equals(c.selected)||c.pendingBoss()<0)||c.encounterGroup==0&&!c.encounter.isEmpty())throw new IllegalArgumentException("Invalid encounter");return c;
    }
}
