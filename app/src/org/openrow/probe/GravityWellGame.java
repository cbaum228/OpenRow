package org.openrow.probe;

import java.util.*;

/** Deterministic game economy and bounded 60 Hz world. No Android or fitness multipliers. */
public final class GravityWellGame {
    public static final double STEP=1.0/60, CAPACITY=180;
    public static final int METEOROID=0,ASTEROID=1,METAL=2,COMET=3,MOON=4,ICY_MOON=5,CLOUD=6,DWARF=7,
        PLANET=8,GAS_GIANT=9,ICE_GIANT=10,BROWN_DWARF=11,RED_DWARF=12,SUN=13,RED_GIANT=14,WHITE_DWARF=15,NEUTRON=16;
    public static final class Kind {
        public final String id,name,description;public final int material,gate,family;public final double mass,radius,erosion;
        public final boolean boss,stellar,rings;
        Kind(String id,String name,String description,int material,int gate,int family,double mass,double radius,double erosion,boolean boss,boolean stellar,boolean rings){
            this.id=id;this.name=name;this.description=description;this.material=material;this.gate=gate;this.family=family;this.mass=mass;this.radius=radius;this.erosion=erosion;this.boss=boss;this.stellar=stellar;this.rings=rings;
        }
    }
    public static final Kind[] KINDS={
        new Kind("meteoroid","Meteoroid","Fast rock; quick dust and fragments",0,0,0,3,7,1.3,false,false,false),
        new Kind("asteroid","Rocky asteroid","Loose rubble peels into grains",0,0,0,18,13,1,false,false,false),
        new Kind("metal","Metallic asteroid","Dense metal erodes more slowly",0,100,0,26,14,.72,false,false,false),
        new Kind("comet","Comet","Icy nucleus and a flowing vapor tail",1,150,0,22,16,1.5,false,false,false),
        new Kind("moon","Rocky moon","Cratered mantle; resistant remnant",0,250,0,50,23,.95,false,false,false),
        new Kind("icy_moon","Icy moon","Pale ice breaks into cool streams",1,400,0,48,24,1.25,false,false,false),
        new Kind("cloud","Gas cloud","Diffuse gas unravels without a core",1,600,0,35,36,1.9,false,false,false),
        new Kind("dwarf","Dwarf planet","BOSS: compact layered world",1,500,0,180,37,1.05,true,false,false),
        new Kind("planet","Rocky planet","BOSS: mantle and a dense core",3,900,0,300,44,.95,true,false,false),
        new Kind("gas_giant","Gas giant","BOSS: rings first, then flowing gas",2,1500,0,450,53,1.3,true,false,true),
        new Kind("ice_giant","Ice giant","BOSS: blue bands and fragile rings",1,2200,0,420,49,1.15,true,false,true),
        new Kind("brown_dwarf","Brown dwarf","BOSS: dim glowing gas envelope",2,3000,1,580,57,1.05,true,true,false),
        new Kind("red_dwarf","Red dwarf","BOSS: long red plasma streams",2,3500,1,680,63,1.15,true,true,false),
        new Kind("sun","Sun-like star","BOSS: luminous plasma disruption",2,5000,1,850,72,1.1,true,true,false),
        new Kind("red_giant","Red giant","BOSS: broad, fragile gas envelope",2,7500,1,1100,84,1.5,true,true,false),
        new Kind("white_dwarf","White dwarf","BOSS: compact stellar remnant",1,10000,2,600,29,.65,true,true,false),
        new Kind("neutron","Neutron star","BOSS: small, exceptionally dense",1,14000,2,500,18,.5,true,true,false)
    };
    public static Kind kind(Body b){return KINDS[b.kind<0?(b.material==2?GAS_GIANT:b.material==3?PLANET:b.material==1?ICY_MOON:ASTEROID):b.kind];}
    public static boolean hasRings(Body b){return (b.kind<0?b.material==2:kind(b).rings)&&b.fragmentDepth==0;}
    public static final class Body {
        public double x,y,vx,vy,mass,angle,initialMass,orbitRadius,orbitPhase,captureAge,shedClock;
        /** Transient time since visible stripping began; trails rebuild after loading. */
        public double trailAge,flightAge;
        public int material,kind=-1,fragmentDepth;
        /** Transient scene detail, never progression or saved physics state. */
        public int visualStreams=1;
        public double visualShear=1,visualOpacity=1,visualEnergy=.5;
        public long bossId;
        public double erosionScale=1;
        public boolean dust,captured;
        public boolean miniature;
        public double integrity=1;
        private double encounterDuration;
        Body(double x,double y,double vx,double vy,double mass,boolean dust){this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.mass=mass;this.initialMass=mass;this.dust=dust;}
    }
    /** Visual ejecta never grants matter or XP. Bounded separately from capturable bodies. */
    public static final class Particle {
        public double x,y,vx,vy,life,maxLife; public int material;
        Particle(double x,double y,double vx,double vy,double life,int material){this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.life=this.maxLife=life;this.material=material;}
    }
    public final ArrayList<Particle> particles=new ArrayList<>();
    public static final class Node {
        public final String id,title,description; public final int cost,max,gate; public final int[] prerequisites;
        public int branch=-1,tier=0,layer=0,source=-1; public boolean specialist; public double requiredDistance; public int materialCost;
        Node(String id,String title,String description,int cost,int max,int gate,int... prerequisites){this.id=id;this.title=title;this.description=description;this.cost=cost;this.max=max;this.gate=gate;this.prerequisites=prerequisites;}
    }
    private static final Node[] ORIGINAL_NODES={
        new Node("pull","Gravitational reach","Stronger attraction +18% / rank",25,5,0),
        new Node("process","Accretion flow","More conversion per input +20% / rank",25,5,0),
        new Node("value","Cosmic refinement","Processed matter XP +25% / rank",50,4,100,1),
        new Node("capacity","Orbital reservoir","Stored matter capacity +60 / rank",60,4,200,0),
        new Node("rocks","Deep-field discovery","Richer asteroids +35% / rank",100,3,400,0,1),
        new Node("halo","Violet horizon","Unlock a violet accretion halo",150,1,600,2,3),
        new Node("planet_fracture","Planetary fractures","Planet bosses split into 2 / 3 / 4",180,3,400,0,1),
        new Node("stellar_shear","Stellar shearing","Expose 2 / 3 / 4 plasma filaments",400,3,1800,6,4),
        new Node("core_rupture","Remnant rupture","Remnants split into 2 / 3 / 4",800,3,4000,7,3)
    };
    public static final Node[] NODES=catalog();
    public static final int[] TIER_RANKS={0,0,8,23,42,58,76,94,112};
    public static int mainNode(int tier,int branch){if(tier>=10)return 81+(tier-10)/3*24+(tier-10)%3*7+branch;return (tier-1)%3==0?9+(tier-1)/3*7+branch:30+((tier-1)/3*2+(tier-1)%3-1)*7+branch;}
    private static Node[] catalog(){
        Node[] out=Arrays.copyOf(ORIGINAL_NODES,201);
        int[] foundations={0,1,3,2,6,-1,5,5,5};for(int i=0;i<9;i++)out[i].branch=foundations[i];
        String[] ids={"reach","flow","storage","refinement","drift","erosion","yield"};
        String[][] names={
            {"Field reach","Capture grip","Comet approach","Extended reach","Heavy-body coupling","Deep capture","Deep reach","Envelope coupling","Giant-world reach"},
            {"Matter flow","Dust processing","Steady feed","Dense-matter processing","Mixed-material flow","Core processing","Gas processing","Plasma-ready conduits","Giant throughput"},
            {"Deep storage","Reserve buffer","Matter reserves","Planetary storage","Capture buffer","Planetary reserve","Giant reservoir","Surge buffer","Accretion reserve"},
            {"Refined matter","Mineral sorting","Clean accretion","Dense-mineral refinement","Ice separation","Core refinement","Gas separation","Volatile refinement","Giant-matter refinement"},
            {"Inward drift","Orbit damping","Debris retention","Debris routing","Chunk guidance","Core delivery","Giant drift","Envelope guidance","Multi-stream routing"},
            {"Surface tools","Mantle peeling","Polar erosion","Planetary erosion","Exposed-face erosion","Core exposure","Ring tools","Envelope erosion","Giant erosion"},
            {"Material recovery","Mineral survey","Debris sampling","Planetary survey","Core sampling","Planetary recovery","Giant survey","Gas sampling","Rare-material recovery"}};
        String[] effects={"Attraction +8% per rank","Processing +8% per rank","Capacity +30 per rank","XP refinement +10% per rank","Inward drift +8% per rank","Erosion +8% per rank","Material yield +10% per rank"};
        for(int tier=1;tier<=9;tier++)for(int branch=0;branch<7;branch++){
            int i=mainNode(tier,branch),chapter=(tier-1)/3+1;
            String id=(tier-1)%3==0?"exp_"+ids[branch]+"_"+chapter:"tree_"+ids[branch]+"_"+tier;
            Node n=new Node(id,names[branch][tier-1],effects[branch],(tier-1)%3==0?50*chapter:40+25*tier,2,0,tier==1?new int[0]:new int[]{mainNode(tier-1,branch)});
            n.branch=branch;n.tier=chapter;n.layer=tier;if(tier>=6)n.requiredDistance=tier==6?10000:tier==9?25000:15000;
            if(tier>=4){n.source=tier<7?0:1;n.materialCost=1;}out[i]=n;
        }
        String[] specIds={"comet_snare","overflow_recovery","comet_sampling","planetary_anchor","core_extraction","precision_fracturing","ring_routing","envelope_shearing","giant_core_disruption"};
        String[] specNames={"Comet snare","Overflow recovery","Comet sampling","Planetary anchor","Core extraction","Precision fracturing","Ring routing","Envelope shearing","Giant-core disruption"};
        String[] specEffects={"Comet attraction +20%","Storage capacity +60","Comet harvest yield +30%","Rocky boss attraction +20%","Rocky boss harvest yield +30%","Rocky boss erosion +15%","Ringed-world inward drift +15%","Giant envelope erosion +15%","Giant core erosion +25% below half mass"};
        int[] sources={4,3,4,5,6,6,7,8,9};
        for(int j=0;j<9;j++){Node n=new Node("spec_"+specIds[j],specNames[j],specEffects[j],150+100*j,1,0);n.specialist=true;n.branch=7;n.tier=j/3+1;n.layer=j+1;n.source=sources[j];n.materialCost=j<3?2:5;n.requiredDistance=j==3?10000:j==8?25000:0;out[72+j]=n;}
        String[][] chapterNames={
            {"Stellar reach","Plasma flow","Stellar reservoir","Plasma refinement","Stellar drift","Envelope tools","Stellar sampling"},
            {"Solar reach","Solar processing","Solar reservoir","Solar refinement","Solar routing","Photosphere tools","Solar sampling"},
            {"Giant capture","Envelope processing","Envelope reservoir","Giant refinement","Envelope routing","Giant shell tools","Giant sampling"},
            {"Remnant reach","Dense processing","Remnant reservoir","Dense refinement","Remnant routing","Dense-core tools","Remnant sampling"},
            {"Pulsar reach","Neutron processing","Pulsar reservoir","Neutron refinement","Polar routing","Neutron tools","Pulsar sampling"}};
        String[] stepNames={" I"," II"," III"};
        String[][] specialNames={{"Brown-dwarf coupling","Red-dwarf extraction","Plasma collection"},{"Solar coupling","Photosphere peeling","Solar collection"},{"Giant-star coupling","Envelope release","Giant plasma collection"},{"Remnant coupling","Dense-core extraction","Remnant collection"},{"Pulsar coupling","Neutron disruption","Neutron collection"}};
        for(int chapter=4;chapter<=8;chapter++){
            int previousBoss=chapter==4?ICE_GIANT:chapter==5?RED_DWARF:chapter==6?SUN:chapter==7?RED_GIANT:WHITE_DWARF;
            int previousSource=GravityWellCampaign.specialMaterial(previousBoss);
            for(int position=0;position<3;position++)for(int branch=0;branch<7;branch++){
                int layer=(chapter-1)*3+position+1,index=mainNode(layer,branch);
                Node n=new Node("expansion_"+ids[branch]+"_"+chapter+"_"+(position+1),chapterNames[chapter-4][branch]+stepNames[position],effects[branch],200+(chapter-4)*150+position*70,2,0,mainNode(layer-1,branch));
                n.branch=branch;n.tier=chapter;n.layer=layer;n.requiredDistance=(chapter-2)*15000;n.source=previousSource;n.materialCost=1;out[index]=n;
            }
            for(int position=0;position<3;position++){
                int index=81+(chapter-4)*24+21+position;
                int kind=chapter==4?(position==0?BROWN_DWARF:RED_DWARF):chapter+8;
                String effect=position==0?"Target attraction +20%":position==1?"Target erosion +15%":"Target material yield +30%";
                Node n=new Node("spec_stellar_"+chapter+"_"+position,specialNames[chapter-4][position],effect,600+(chapter-4)*200+position*100,1,0);
                n.specialist=true;n.branch=7;n.tier=chapter;n.layer=(chapter-1)*3+position+1;n.source=GravityWellCampaign.specialMaterial(kind);n.materialCost=5;n.requiredDistance=(chapter-2)*15000;out[index]=n;
            }
        }
        HashSet<String> seen=new HashSet<String>();for(Node n:out){if(!seen.add(n.id))throw new IllegalStateException("Duplicate upgrade");for(int parent:n.prerequisites)if(parent<0||parent>=out.length||out[parent].layer>=n.layer&&n.layer>0)throw new IllegalStateException("Invalid prerequisite graph");}return out;
    }
    public int ranksBefore(int tier){int total=0;for(int i=0;i<NODES.length;i++)if(!NODES[i].specialist&&NODES[i].branch>=0&&NODES[i].tier<tier)total+=ranks[i];return total;}
    public int tierRanks(int tier){int total=0;for(int i=9;i<NODES.length;i++)if(!NODES[i].specialist&&NODES[i].tier==tier)total+=ranks[i];return total;}
    public int tierNodes(int tier){int total=0;for(int i=9;i<NODES.length;i++)if(!NODES[i].specialist&&NODES[i].tier==tier&&ranks[i]>0)total++;return total;}
    public String requirements(int i){Node n=NODES[i];if(n.tier==0)return "Foundation tool";
        String text="Tier "+n.tier+": "+ranksBefore(n.tier)+" / "+TIER_RANKS[n.tier]+" earlier ranks.";
        if(n.specialist)text+=" This tier: "+tierRanks(n.tier)+" / 4 ranks across "+tierNodes(n.tier)+" / 2 nodes.";
        else if(n.prerequisites.length>0)text+=" Needs "+NODES[n.prerequisites[0]].title+".";
        return text;
    }
    public GravityWellCampaign campaign;
    private boolean eligibleWork=true,bossWindow=true;
    private double contribution=1,miniClock,bossIntegrity=1;
    private double encounterSeconds;
    public void gameplayContribution(double value){contribution=Double.isFinite(value)?Math.max(0,Math.min(1.25,value)):0;}
    public double bossIntegrity(){return bossIntegrity;}
    private static final class Parked {long id;int kind;double initial,integrity;final ArrayList<Body> bodies=new ArrayList<>();}
    private final Map<String,Parked> parked=new LinkedHashMap<>();
    public Set<String> parkedDestinations(){return Collections.unmodifiableSet(parked.keySet());}
    public void campaignWindow(boolean eligible,boolean mayIntroduceBoss){eligibleWork=eligible;bossWindow=mayIntroduceBoss;}
    public void enableCampaign(boolean synthetic,boolean existing){
        if(campaign!=null){campaign.synthetic=synthetic;return;}campaign=new GravityWellCampaign();campaign.synthetic=synthetic;
        if(existing){for(int k=0;k<KINDS.length;k++)if(distance>=KINDS[k].gate)campaign.legacyMask|=1<<k;
            if(activeBossId>0){bossIntegrity=bossMass()/bossInitialMass;rebaseBossBodies();}}
    }
    public int extraRanks(int branch){if(campaign==null)return 0;int total=0;for(int i=9;i<NODES.length;i++)if(!NODES[i].specialist&&NODES[i].branch==branch)total+=ranks[i];return total;}
    private double attraction(){return 1+.18*ranks[0]+.08*extraRanks(0);}
    public boolean selectDestination(String id){
        if(campaign==null||running)return false;if(id!=null&&id.equals(campaign.selected))return true;
        String previous=campaign.selected;
        if(!campaign.select(id,false,0))return false;
        if(activeBossId>0){Parked p=new Parked();p.id=activeBossId;p.kind=bossKind;p.initial=bossInitialMass;p.integrity=bossIntegrity;
            for(Iterator<Body> it=bodies.iterator();it.hasNext();){Body b=it.next();if(b.bossId==activeBossId){p.bodies.add(b);it.remove();}}
            parked.put(previous,p);activeBossId=0;bossKind=-1;bossInitialMass=0;campaign.clearEncounter();}
        Parked resume=parked.remove(id);if(resume!=null){while(bodies.size()+resume.bodies.size()>150)bodies.remove(0);
            bodies.addAll(resume.bodies);activeBossId=resume.id;bossKind=resume.kind;bossInitialMass=resume.initial;bossIntegrity=resume.integrity;if(campaign.pendingBoss()>=0)campaign.beginBoss(activeBossId);}
        encounterSeconds=0;return true;
    }
    public boolean labCheckpoint(int stage){
        if(campaign==null||!campaign.synthetic||running||stage<0||stage>GravityWellCampaign.BOSSES.length)return false;
        bodies.clear();particles.clear();parked.clear();activeBossId=0;bossInitialMass=0;bossKind=-1;spawnRock=spawnDust=0;bossIntegrity=1;encounterSeconds=0;
        if(!campaign.checkpoint(stage))return false;xp=Math.max(xp,5000);return true;
    }
    public boolean legacyBossAccess(int kind){return campaign!=null&&campaign.legacyAccess(kind)&&KINDS[kind].boss;}
    public final ArrayList<Body> bodies=new ArrayList<>();
    public final int[] ranks=new int[NODES.length];
    public double xp,earned,reservoir,distance,sessionDistance,seconds,sessionSeconds,sessionWork,processed;
    public long strokes,sessionStrokes,captures,sessionCaptures;
    public double field,flash,spawnDust,spawnRock,accumulator,visualPhase,sessionMotionSeconds;
    private boolean separateVisuals;
    private double visualField;
    public double visualField(){return separateVisuals?visualField:field;}
    public boolean running; public int duration=300;
    public double bossClock,bossInitialMass,bossToast;
    public long activeBossId,nextBossId=1,bossesDefeated,sessionBosses;
    public int bossKind=-1;
    private long random=932471;
    private double rand(){random=(random*1664525+1013904223)&0xffffffffL;return random/4294967296.0;}
    private final GravityWellScene particleScene=new GravityWellScene();
    public double capacity(){return CAPACITY+60*ranks[3]+30*extraRanks(2)+(campaign!=null?60*ranks[73]:0);}
    public int cost(int i){return campaign==null?(int)Math.round(NODES[i].cost*Math.pow(1.7,ranks[i])):GravityWellBalance.cost(i,ranks[i]);}
    public int nodeSource(int i){return campaign==null?NODES[i].source:GravityWellBalance.source(i);}
    public int nodeMaterialCost(int i){return campaign==null?NODES[i].materialCost:GravityWellBalance.materialCost(i);}
    public String upgradeEffect(int i){Node n=NODES[i];if(campaign!=null){if(!n.specialist){if(n.branch==3)return "Game XP efficiency; bonuses share a +20% cap";if(n.branch==6&&i!=4)return "Harvest efficiency; bonuses share a +20% cap";}
        if(i==74||i==76||i>=102&&(i-102)%24==2)return "Target material yield +20% (bounded)";}return n.description;}
    public String blocked(int i){
        if(i<0||i>=NODES.length)return "Unknown upgrade";Node n=NODES[i];if(ranks[i]>=n.max)return "MAX RANK";
        if(campaign!=null){
            // Owned nodes remain usable after migration; only new unlocks check topology.
            if(n.tier>0&&ranks[i]==0){
                if(GravityWellBalance.unlockStage(i)>campaign.stage()&&!campaign.complete())return "Discover the next encounter stage";
                if(ranksBefore(n.tier)<TIER_RANKS[n.tier])return "Need "+(TIER_RANKS[n.tier]-ranksBefore(n.tier))+" earlier main ranks";
                if(n.specialist&&(tierRanks(n.tier)<4||tierNodes(n.tier)<2))return "Need 4 ranks in 2 nodes of tier "+n.tier;
                if(n.tier>=4){int previous=n.tier==4?ICE_GIANT:n.tier==5?RED_DWARF:n.tier+7;if(!campaign.discovered(previous))return "First consume "+KINDS[previous].name;}
                if(n.specialist&&n.source>=5){int boss=n.source>=10?n.source+1:n.source==5?DWARF:n.source==6?PLANET:n.source==9?ICE_GIANT:GAS_GIANT;if(!campaign.discovered(boss))return "First consume "+KINDS[boss].name;}
                for(int parent:n.prerequisites)if(ranks[parent]==0)return "Needs "+NODES[parent].title;
            }
            if(i<9&&ranks[i]==0&&GravityWellBalance.unlockStage(i)>campaign.stage()&&!campaign.complete())return "Discover the next encounter stage";
            if(nodeSource(i)>=0&&campaign.materials[nodeSource(i)]<nodeMaterialCost(i))return "Need "+nodeMaterialCost(i)+" "+GravityWellCampaign.MATERIALS[nodeSource(i)];
            if(i>=6&&i<9){int kind=i==6?DWARF:i==7?BROWN_DWARF:WHITE_DWARF;
                if(!campaign.cleared.contains(KINDS[kind].id)&&!campaign.legacyAccess(kind)&&ranks[i]==0)return "First consume "+KINDS[kind].name;}
        }else {if(i>=30)return "Campaign tool";if(distance<n.gate)return String.format(Locale.US,"Row %.0f m more",n.gate-distance);}
        if(i<9)for(int parent:n.prerequisites)if(ranks[parent]==0)return "Needs "+NODES[parent].title;
        if(xp<cost(i))return "Need "+cost(i)+" XP";return "Buy · "+cost(i)+" XP"+(campaign!=null&&nodeSource(i)>=0?" + "+nodeMaterialCost(i)+" "+GravityWellCampaign.MATERIALS[nodeSource(i)]:"");
    }
    public boolean purchase(int i){if(running||i<0||i>=NODES.length||!blocked(i).startsWith("Buy"))return false;
        xp-=cost(i);if(campaign!=null&&nodeSource(i)>=0)campaign.materials[nodeSource(i)]-=nodeMaterialCost(i);ranks[i]++;encounterSeconds=0;return true;}
    private int stellarSpecialist(Body b,int effect){if(campaign==null)return 0;int total=0;for(int chapter=4;chapter<=8;chapter++){int index=81+(chapter-4)*24+21+effect;if(NODES[index].source==GravityWellCampaign.specialMaterial(b.kind))total+=ranks[index];}return total;}
    private double specialistAttraction(Body b){return campaign==null?1:1+.2*stellarSpecialist(b,0)+(b.kind==COMET?.2*ranks[72]:b.kind==DWARF||b.kind==PLANET?.2*ranks[75]:0);}
    private double specialistYield(Body b){return campaign==null?1:1+.3*stellarSpecialist(b,2)+(b.kind==COMET?.3*ranks[74]:b.kind==DWARF||b.kind==PLANET?.3*ranks[76]:0);}
    private double harvestScale(int kind){double target=kind==COMET?.3*ranks[74]:kind==DWARF||kind==PLANET?.3*ranks[76]:0;
        for(int chapter=4;chapter<=8;chapter++){int index=81+(chapter-4)*24+23;if(NODES[index].source==GravityWellCampaign.specialMaterial(kind))target+=.3*ranks[index];}
        return GravityWellBalance.yieldFactor(this)*(1+Math.min(.2,target));}
    private double specialistErosion(Body b){if(campaign==null)return 1;return 1+.15*stellarSpecialist(b,1)+(b.kind==DWARF||b.kind==PLANET?.15*ranks[77]:b.kind==GAS_GIANT||b.kind==ICE_GIANT?.15*ranks[79]+(b.mass<b.initialMass*.5?.25*ranks[80]:0):0);}
    public void start(int duration){this.duration=duration;earned=sessionDistance=sessionSeconds=sessionWork=sessionMotionSeconds=0;sessionStrokes=sessionCaptures=sessionBosses=0;running=true;accumulator=0;encounterSeconds=0;for(Body b:bodies)b.encounterDuration=0;}
    public boolean unlocked(int kind){return kind>=0&&kind<KINDS.length&&(campaign==null?distance>=KINDS[kind].gate:campaign.discovered(kind));}
    /** Bounded, eased rowing response shared by decorative effects. */
    public static double visualEnergy(double field){double f=Double.isFinite(field)?Math.max(0,Math.min(1,field)):0;return f*(1.5-.5*f);}
    public double visualEnergy(){return visualEnergy(visualField());}
    public double visualEmissionRate(){return .55+1.15*visualEnergy();}
    public double stellarFocus(){
        if(activeBossId<=0||bossKind<0||!KINDS[bossKind].stellar)return 0;
        for(Body b:bodies)if(b.bossId==activeBossId){double q=Math.max(0,Math.min(1,b.flightAge/2));return q*q*(3-2*q);}return 0;
    }
    public double bossMass(){double total=0;for(Body b:bodies)if(b.bossId==activeBossId&&activeBossId>0)total+=b.mass;return total;}
    public int bossPieces(){int count=0;for(Body b:bodies)if(b.bossId==activeBossId&&activeBossId>0)count++;return count;}
    public int fractureRank(Body b){return ranks[6+kind(b).family];}
    public void finish(){running=false;accumulator=0;}
    /** Credit only newly observed input. Units are game work for S4, simulated joules for test. */
    public void input(double meters,long count,double work){
        if(!running || !Double.isFinite(meters) || !Double.isFinite(work) || meters<0 || work<0 || count<0)return;
        distance+=meters;sessionDistance+=meters;strokes+=count;sessionStrokes+=count;sessionWork+=work;
        if(work<=0)return;
        // Inexhaustible dust provides a minimum opportunity, but conversion still costs input.
        double wanted=work*.012*(1+.2*ranks[1]+.08*extraRanks(1));
        double fromDust=Math.max(0,wanted-reservoir);
        double fromStored=Math.min(reservoir,wanted);
        reservoir-=fromStored;
        double gain=(fromStored*1.3+fromDust*.45)*(1+.25*ranks[2]+.1*extraRanks(3));
        if(campaign==null){xp+=gain;earned+=gain;}processed+=wanted;flash=Math.min(1,flash+.12);
    }
    public void advance(double dt,double effort,boolean active){advance(dt,effort,active,true);}
    public void advance(double dt,double effort,boolean active,boolean rowing){advanceInputs(dt,effort,active,rowing,effort,false);}
    public void advance(double dt,double effort,boolean active,boolean rowing,double visualInput){advanceInputs(dt,effort,active,rowing,visualInput,true);}
    private void advanceInputs(double dt,double effort,boolean active,boolean rowing,double visualInput,boolean separate){
        if(!running||!Double.isFinite(dt)||dt<=0)return;
        if(!active){field=0;visualField=0;accumulator=0;return;}
        if(separate&&!separateVisuals)visualField=field;separateVisuals=separate;
        accumulator+=Math.min(dt,.25);
        while(accumulator+1e-9>=STEP&&running){accumulator=Math.max(0,accumulator-STEP);step(rowing&&Double.isFinite(effort)?Math.max(0,Math.min(1,effort)):0,rowing,rowing&&Double.isFinite(visualInput)?Math.max(0,Math.min(1,visualInput)):0);}
    }
    private void step(double effort,boolean rowing,double visualInput){
        double remaining=duration==0?STEP:Math.max(0,duration-sessionSeconds);
        double dt=Math.min(STEP,remaining);sessionSeconds+=dt;if(rowing){seconds+=dt;sessionMotionSeconds+=dt;}
        field+=(effort-field)*.018;if(separateVisuals)visualField+=(visualInput-visualField)*.018;else visualField=field;flash*=.96;visualPhase+=(.035+.065*visualField())*dt;
        bossToast=Math.max(0,bossToast-dt);
        double focus=stellarFocus();
        boolean awaitingBoss=campaign!=null&&campaign.pendingBoss()>=0&&activeBossId==0;
        if(rowing&&!awaitingBoss){spawnDust+=dt*(activeBossId>0?.15-.12*focus:1);if(activeBossId==0)spawnRock+=dt;}
        while(spawnDust>=.09){spawnDust-=.09;if(bodies.size()<(activeBossId>0?144:campaign!=null?130:150))spawn(true);}
        if(!awaitingBoss&&activeBossId==0&&spawnRock>=3.5){spawnRock-=3.5;if(bodies.size()<(campaign!=null?130:150))spawn(false);}
        if(campaign!=null){
            campaign.tick(dt,rowing&&eligibleWork);
            if(rowing&&eligibleWork){double gain=dt*contribution*GravityWellBalance.xpFactor(this);xp+=gain;earned+=gain;}
            int pending=campaign.pendingBoss();if(rowing&&eligibleWork&&bossWindow&&activeBossId==0&&pending>=0&&bodies.size()<138){spawnKind(pending,true);campaign.beginBoss(activeBossId);}
            if(rowing&&eligibleWork&&activeBossId==0){miniClock=Math.min(GravityWellBalance.MINI_INTERVAL,miniClock+dt);
                boolean mini=false;for(Body b:bodies)if(b.miniature)mini=true;
                if(!mini&&miniClock>=GravityWellBalance.MINI_INTERVAL&&bodies.size()<144){int k=miniKind();if(k>=0){spawnKind(k,false);Body b=bodies.get(bodies.size()-1);b.miniature=true;b.mass=b.initialMass=KINDS[k].mass*GravityWellBalance.MINI_FRACTION;
                    double radius=Math.hypot(b.x,b.y),nx=b.x/radius,ny=b.y/radius;b.vx=-nx*48-ny*12;b.vy=-ny*48+nx*12;miniClock=0;}}}
            if(activeBossId>0){if(encounterSeconds<=0)encounterSeconds=GravityWellBalance.seconds(this,bossKind);
                double before=bossIntegrity;if(rowing&&eligibleWork)bossIntegrity=Math.max(0,bossIntegrity-dt*contribution/encounterSeconds);
                campaign.harvested(bossKind,before-bossIntegrity,harvestScale(bossKind));
                reservoir+=Math.min(bossInitialMass*(before-bossIntegrity),Math.max(0,capacity()-reservoir));
                for(Body b:bodies)if(b.bossId==activeBossId)b.mass=b.initialMass*bossIntegrity;}
        }
        if(campaign==null&&rowing&&activeBossId==0){bossClock+=dt;if(bossClock>=45){int chosen=-1,count=0;for(int k=0;k<KINDS.length;k++)if(KINDS[k].boss&&unlocked(k)){chosen=k;count++;}
            if(count>1&&nextBossId%2==0){int target=(int)(rand()*count);for(int k=0;k<KINDS.length;k++)if(KINDS[k].boss&&unlocked(k)&&target--==0){chosen=k;break;}}
            if(chosen>=0&&bodies.size()<138){spawnKind(chosen,true);bossClock=0;}}}
        double gravity=(420000+field*1600000)*attraction();
        for(int i=particles.size()-1;i>=0;i--){Particle p=particles.get(i);double r2=p.x*p.x+p.y*p.y;
            double force=gravity/Math.pow(r2+3600,1.5);p.vx-=p.x*force*dt;p.vy-=p.y*force*dt;
            p.x+=p.vx*dt;p.y+=p.vy*dt;p.life-=dt;
            if(p.life<=0||r2<38*38)particles.remove(i);
        }
        particleScene.prepare(this);
        for(int i=bodies.size()-1;i>=0;i--){Body b=bodies.get(i);double r2=b.x*b.x+b.y*b.y,r=Math.sqrt(r2);
            b.flightAge+=dt;
            if(b.captured||b.kind==COMET&&(b.trailAge>0||r<310&&b.x*b.vx+b.y*b.vy<0))b.trailAge+=dt;else b.trailAge=0;
            b.angle+=dt*(b.dust?.12:.22);
            if(!b.dust&&(b.captured||r<(b.bossId>0||b.miniature?260:172))){
                if(b.orbitRadius<=0){
                    boolean first=!b.captured;b.captured=true;b.orbitRadius=b.bossId>0?240:145+Math.min(65,b.initialMass*.7);
                    b.orbitPhase=Math.atan2(b.y/.58,b.x);b.captureAge=0;
                    if(first&&rowing){captures++;sessionCaptures++;}flash=Math.min(1,flash+.2);
                }
                b.captureAge+=dt;
                double captureBlend=Math.max(0,Math.min(1,b.captureAge/2.4));captureBlend=captureBlend*captureBlend*(3-2*captureBlend);
                double ellipseRadius=Math.max(1,Math.hypot(b.x,b.y/.58));
                double incomingRate=(b.x*b.vy/.58-b.y/.58*b.vx)/(ellipseRadius*ellipseRadius);
                double orbitRate=(.18+.09*field)*Math.sqrt(170/b.orbitRadius);
                double phaseRate=Math.max(-1.2,Math.min(1.2,incomingRate))*(1-captureBlend)+orbitRate*captureBlend;
                b.orbitPhase+=dt*phaseRate;
                if(rowing&&b.bossId>0&&b.fragmentDepth==0&&b.captureAge>=4&&b.mass<b.initialMass*.92&&fractureRank(b)>0){
                    int pieces=1+fractureRank(b);if(fragment(b,pieces))continue;
                }
                // Surface-area erosion: splitting the same mass increases total exposed area.
                double materialRate=b.kind<0?1:kind(b).erosion;
                double shed=(b.bossId>0&&!rowing)?0:Math.min(b.mass,.22*materialRate*b.erosionScale*Math.pow(Math.max(.01,b.mass),2.0/3)*(1+.25*field)*(1+.08*extraRanks(5))*specialistErosion(b)*dt);
                if(campaign!=null&&b.bossId>0)shed=0;
                if(campaign!=null&&b.miniature){double before=b.integrity;
                    if(b.encounterDuration<=0)b.encounterDuration=GravityWellBalance.seconds(this,b.kind)*GravityWellBalance.MINI_FRACTION;
                    if(rowing&&eligibleWork)b.integrity=Math.max(0,b.integrity-dt*contribution/b.encounterDuration);
                    shed=b.initialMass*(before-b.integrity);campaign.harvested(b.kind,(before-b.integrity)*GravityWellBalance.MINI_FRACTION,harvestScale(b.kind));}
                b.mass-=shed;if(rowing&&campaign!=null&&!b.dust&&b.bossId==0&&!b.miniature)campaign.stripped(b.kind,shed,harvestScale(b.kind));if(rowing)reservoir+=Math.min(shed,Math.max(0,capacity()-reservoir));
                double fraction=Math.max(0,b.mass/b.initialMass),radius=b.orbitRadius*(.62+.38*Math.sqrt(fraction))/(1+.08*extraRanks(4)+(campaign!=null&&kind(b).rings?.15*ranks[78]:0));
                // Start on the incoming ellipse and settle toward the feeding orbit. A damped
                // velocity handoff preserves momentum instead of teleporting to an orbit target.
                radius=ellipseRadius*(1-captureBlend)+radius*captureBlend;
                double tx=Math.cos(b.orbitPhase)*radius,ty=Math.sin(b.orbitPhase)*radius*.58;
                double targetVx=-Math.sin(b.orbitPhase)*radius*phaseRate,targetVy=Math.cos(b.orbitPhase)*radius*.58*phaseRate;
                double ax=9*(tx-b.x)+6*(targetVx-b.vx),ay=9*(ty-b.y)+6*(targetVy-b.vy);
                double acceleration=Math.hypot(ax,ay),limit=140;
                if(acceleration>limit){ax*=limit/acceleration;ay*=limit/acceleration;}
                b.vx+=ax*dt;b.vy+=ay*dt;b.x+=b.vx*dt;b.y+=b.vy*dt;b.shedClock+=dt*visualEmissionRate();
                double shedInterval=.075*(b.bossId>0&&b.fragmentDepth>0&&kind(b).stellar?Math.max(1,bossPieces()):1)*(b.bossId!=activeBossId?1+4*focus:1);
                while(b.shedClock>=shedInterval){b.shedClock-=shedInterval;emit(b);if(hasRings(b)&&ringRemaining(b)>0)emitRing(b);}
                boolean gone=campaign!=null&&(b.bossId>0||b.miniature)?(b.bossId>0?bossIntegrity<=1e-9:b.integrity<=1e-9):b.mass<=.12;
                if(gone&&(b.bossId==0||rowing&&eligibleWork)){
                    if(rowing&&campaign!=null&&!b.dust){if(b.miniature)campaign.harvested(b.kind,GravityWellBalance.MINI_FRACTION/3,1);else if(b.bossId==0)campaign.stripped(b.kind,b.mass,harvestScale(b.kind));}
                    if(rowing)reservoir+=Math.min(b.mass,Math.max(0,capacity()-reservoir));for(int j=0;j<8;j++)emit(b);bodies.remove(i);}
                continue;
            }
            if(awaitingBoss&&b.dust){b.vx+=(-b.x*.8-b.vx)*.05;b.vy+=(-b.y*.8-b.vy)*.05;}
            double force=gravity*specialistAttraction(b)/Math.pow(r2+2500,1.5);b.vx-=b.x*force*dt;b.vy-=b.y*force*dt;
            if(b.dust&&r<125){b.vx*=.988;b.vy*=.988;}
            b.x+=b.vx*dt;b.y+=b.vy*dt;
            if(b.dust&&r<35){if(rowing)reservoir+=Math.min(b.mass,Math.max(0,capacity()-reservoir));bodies.remove(i);}
            else if(r>760)bodies.remove(i);
        }
        if(activeBossId>0&&bossPieces()==0){if(rowing){bossesDefeated++;sessionBosses++;bossToast=6;if(campaign!=null)campaign.consumed(activeBossId,bossKind);}activeBossId=0;bossClock=0;encounterSeconds=0;}
        if(duration>0 && sessionSeconds>=duration-1e-7)finish();
    }
    /** Shared artistic silhouette: loose mantle peels from the poles/inward face before the core. */
    public static double erosionRadius(Body b,double angle){
        double f=Math.max(0,Math.min(1,b.mass/b.initialMass));
        if(!b.captured)return 1;
        if(b.kind>=0&&kind(b).stellar)return Math.cbrt(f);
        if(b.kind==CLOUD)return Math.cbrt(f)*(1-.15*(1-f)*(1-Math.cos(angle))*.5);
        double core=Math.max(.37,.65*Math.cbrt(f));
        if(f<=.12)return core*Math.cbrt(f/.12);
        if(f<.39)return core;
        double seed=b.initialMass*.37+b.material*1.7;
        double lost=1-f,step=Math.PI*2/(b.fragmentDepth>0?7:12),shift=b.fragmentDepth>0?b.angle:seed*.2;
        double position=(angle+shift)/step,cell=Math.floor(position),t=position-cell;
        double a0=cell*step-shift,a1=a0+step;
        double r0=mantleSector(b,a0,cell,lost,core,seed),r1=mantleSector(b,a1,cell+1,lost,core,seed);
        double delta=t*step;
        double polygon=r0*r1*Math.sin(step)/Math.max(.0001,r1*Math.sin(step-delta)+r0*Math.sin(delta));
        double blend=b.fragmentDepth>0?1:Math.min(1,lost*8);
        return Math.max(core,(1-.08*lost)*(1-blend)+Math.max(core,polygon)*blend);
    }
    private static double mantleSector(Body b,double angle,double sector,double lost,double core,double seed){
        int count=b.fragmentDepth>0?7:12;sector=(sector%count+count)%count;
        double release=Math.max(.015,.06+.25*(1+Math.cos(angle))*.5-.06*Math.abs(Math.sin(angle))+.065*Math.sin(seed+sector*1.8));
        double q=Math.max(0,Math.min(1,(lost-release)/.23)),peel=q*q*(3-2*q);
        double outer=1-.08*lost;if(b.fragmentDepth>0)outer*=.9+.1*Math.sin(seed+sector*2.7);
        return core+Math.max(0,outer-core)*(1-peel);
    }
    public static double visualRadius(Body b){
        double fraction=Math.max(0,Math.min(1,b.mass/b.initialMass));
        if(b.kind>=0)return kind(b).radius*Math.cbrt(b.initialMass/kind(b).mass)
            *(1+Math.max(-.12,Math.min(.12,b.y/1800)))*(.9+.1*Math.cbrt(fraction));
        return ((b.material>=2?15:5)+Math.cbrt(b.initialMass)*2.5)*55/64.0
            *(1+Math.max(-.12,Math.min(.12,b.y/1800)))*(.9+.1*Math.cbrt(fraction));
    }
    public static double tidalStretch(Body b){
        boolean fluid=b.kind==CLOUD;
        double amount=b.kind==CLOUD?.18:.08+.015*(b.visualStreams-1);
        return b.captured&&fluid?1+amount*Math.sin(Math.PI*Math.max(0,Math.min(1,b.mass/b.initialMass))):1;
    }
    public static double inwardStretch(Body b){return b.visualStreams>1&&!kind(b).stellar?1+.18*(1-Math.max(0,Math.min(1,b.mass/b.initialMass))):1;}
    public static double ringRemaining(Body b){return b.captured?Math.max(0,1-b.captureAge/2.4):1;}
    /** Staggered outer mantle, inner mantle and final core fragments; driven by actual mass loss. */
    public static double mantleProgress(Body b,int slot){
        if(!b.captured)return 0;
        int layer=slot/6,sector=slot%6;
        double lost=1-Math.max(0,Math.min(1,b.mass/b.initialMass));
        double release=layer==0?.025+sector*.025:layer==1?.36+sector*.028:.87+sector*.018;
        double duration=layer==2?.09:.25;
        return Math.max(0,Math.min(1,(lost-release)/duration));
    }
    /** Fragment centers in the same outward-facing frame as detached ring bands. */
    public static void mantlePoint(Body b,int slot,float[] out){
        int layer=slot/6,sector=slot%6;
        double q=mantleProgress(b,slot),phase=slot*2.39996+b.initialMass*.17;
        double flow=Math.max(0,Math.min(1,q+q*(1-q)*.45*Math.sin(phase)));
        double pull=flow*flow*(3-2*flow),a=Math.PI+(sector-2.5)*.54;
        double radius=visualRadius(b),origin=radius*(layer==0?.82:layer==1?.56:.24);
        if(sector%2==1)origin*=.76;
        double x=Math.cos(a)*origin,y=Math.sin(a)*origin;
        double distance=Math.hypot(b.x,b.y)/tidalStretch(b);
        out[0]=(float)(x*(1-pull)-distance*pull);
        out[1]=(float)(y*(1-pull)+Math.sin(Math.PI*pull)*radius*(.5+.7*Math.sin(phase)));
        out[2]=(float)q;
    }
    /** Ring coordinates in the body's outward-facing frame, before tidal stretch. */
    public static void ringPoint(Body b,double lane,double a,float[] out,int offset){
        double size=visualRadius(b)*64/55,r=size*(1.4+lane*.54);
        double rotation=-Math.PI/9-Math.atan2(b.y,b.x),rx=Math.cos(a)*r,ry=Math.sin(a)*r*.29;
        double x=Math.cos(rotation)*rx-Math.sin(rotation)*ry,y=Math.sin(rotation)*rx+Math.cos(rotation)*ry;
        double q=1-ringRemaining(b);
        double flow=Math.max(0,Math.min(1,q+q*(1-q)*.65*Math.sin(a*2+lane*4+b.initialMass)));
        double pull=flow*flow*(3-2*flow),distance=Math.hypot(b.x,b.y)/tidalStretch(b);
        out[offset]=(float)(x*(1-pull)-distance*pull);
        out[offset+1]=(float)(y*(1-pull)+Math.sin(Math.PI*pull)*size*(.7+.25*Math.sin(a*3+lane*6)));
    }
    /** Small mantle pits expose breakup on the visible face, away from the protected core. */
    public static void surfacePit(Body b,int index,float[] out){
        if(b.kind>=0&&(kind(b).stellar||b.kind==CLOUD)){out[0]=out[1]=out[2]=0;return;}
        double f=Math.max(0,Math.min(1,b.mass/b.initialMass)),a=Math.PI+(index-1)*.72;
        double core=Math.max(.37,.65*Math.cbrt(f));
        double contour=erosionRadius(b,a),width=Math.max(0,contour-core-.05);
        double center=contour-width*.45,opening=Math.min(width*.29,Math.max(0,(1-f-.025)*.55));
        out[0]=(float)(Math.cos(a)*center);out[1]=(float)(Math.sin(a)*center);out[2]=(float)opening;
    }
    private final float[] visualScratch=new float[3];
    private void emitRing(Body b){
        if(particles.size()>=240)return;
        double phase=b.captureAge*41.3+particles.size()*2.39996;
        ringPoint(b,(Math.sin(phase)*.5+.5),phase,visualScratch,0);
        double facing=Math.atan2(b.y,b.x),edgeX=visualScratch[0],edgeY=visualScratch[1];
        double stretch=tidalStretch(b);edgeX*=stretch;edgeY/=Math.sqrt(stretch);
        double x=b.x+Math.cos(facing)*edgeX-Math.sin(facing)*edgeY,y=b.y+Math.sin(facing)*edgeX+Math.cos(facing)*edgeY;
        double r=Math.max(1,Math.hypot(x,y));
        particles.add(new Particle(x,y,b.vx*.4-x/r*65,b.vy*.4-y/r*65,2.6,2));
    }
    private void emit(Body b){
        if(particles.size()>=240)return;
        if(b.bossId>0&&b.fragmentDepth>0&&kind(b).stellar&&particleScene.stellarBody()!=null)b=particleScene.stellarBody();
        double phase=b.captureAge*37.1+particles.size()*2.39996,radius=visualRadius(b);
        double edgeAngle=Math.PI+1.8*Math.sin(phase),edgeRadius=radius*erosionRadius(b,edgeAngle);
        double edgeX=Math.cos(edgeAngle)*edgeRadius,edgeY=Math.sin(edgeAngle)*edgeRadius;
        if(Math.sin(phase*1.71)>.1){
            surfacePit(b,(int)(Math.abs(phase)*7)%3,visualScratch);
            if(visualScratch[2]>.002){edgeX=visualScratch[0]*radius;edgeY=visualScratch[1]*radius;}
        }
        int fragment=(int)(Math.abs(phase)*3)%15;
        double progress=kind(b).stellar||b.kind==CLOUD?0:mantleProgress(b,fragment);
        if(progress>.06&&progress<.92){mantlePoint(b,fragment,visualScratch);edgeX=visualScratch[0];edgeY=visualScratch[1];}
        double stretch=tidalStretch(b);edgeX*=stretch;if(edgeX<0)edgeX*=inwardStretch(b);edgeY/=Math.sqrt(stretch);
        double angle=Math.atan2(b.y,b.x),nx=Math.cos(angle),ny=Math.sin(angle);
        double x=b.x+nx*edgeX-ny*edgeY,y=b.y+ny*edgeX+nx*edgeY;
        double r=Math.max(1,Math.hypot(x,y));
        particles.add(new Particle(x,y,b.vx*.6-x/r*(12+10*Math.sin(phase)),-Math.cos(phase)*6+b.vy*.6-y/r*18,1.4+.5*Math.sin(phase),b.material));
    }
    /** Hook for later fragmentation upgrades; mass is conserved, exposed surface grows. */
    public boolean fragment(Body parent,int pieces){
        if(parent==null||!parent.captured||parent.dust||pieces<2||pieces>8||!bodies.contains(parent)||bodies.size()+pieces-1>150)return false;
        bodies.remove(parent);
        for(int i=0;i<pieces;i++){Body b=new Body(parent.x,parent.y,parent.vx,parent.vy,parent.mass/pieces,false);b.material=parent.material;b.kind=parent.kind;b.bossId=parent.bossId;b.fragmentDepth=parent.fragmentDepth+1;
            if(campaign!=null)b.initialMass=parent.initialMass/pieces;
            // Dense fragments expose only part of their new area: 12 / 17 / 22% faster total erosion.
            b.erosionScale=parent.bossId>0?parent.erosionScale*(1+.12+.05*(pieces-2))/Math.cbrt(pieces):parent.erosionScale;
            b.captured=true;b.orbitRadius=parent.orbitRadius;b.orbitPhase=parent.orbitPhase+(i-(pieces-1)*.5)*.27;b.captureAge=parent.captureAge;b.flightAge=parent.flightAge;if(kind(parent).stellar)b.trailAge=parent.trailAge;b.angle=parent.angle+i;bodies.add(b);}
        return true;
    }
    private void spawn(boolean dust){if(!dust){int[] candidates=new int[KINDS.length];int count=0;for(int k=0;k<KINDS.length;k++)if(!KINDS[k].boss&&unlocked(k)&&(campaign==null||campaign.ordinaryAllowed(k)))candidates[count++]=k;spawnKind(candidates[(int)(rand()*count)],false);return;}
        double a=rand()*Math.PI*2,r=220+rand()*180;double tangent=4+rand()*8,inward=25;
        int material=dust?0:(int)(rand()*4);double mass=dust?.6:(material>=2?38+rand()*32:10+rand()*16)*(1+.35*ranks[4]);
        Body b=new Body(Math.cos(a)*r,Math.sin(a)*r,-Math.cos(a)*inward-Math.sin(a)*tangent,-Math.sin(a)*inward+Math.cos(a)*tangent,mass,dust);b.material=material;b.angle=a;bodies.add(b);}
    void spawnKind(int type,boolean boss){
        if(type<0||type>=KINDS.length||bodies.size()>=150||boss&&activeBossId>0)return;
        Kind k=KINDS[type];double a=boss?(rand()<.5?.15:Math.PI+.15):rand()*Math.PI*2;
        double tangent=boss?12:type==METEOROID?85:type==COMET?55:15+rand()*35,inward=boss?48:35;
        double mass=k.mass*(.85+rand()*.3)*(1+(type<=METAL?.35:0)*ranks[4]);
        Body b=new Body(Math.cos(a)*510,Math.sin(a)*510,-Math.cos(a)*inward-Math.sin(a)*tangent,-Math.sin(a)*inward+Math.cos(a)*tangent,mass,false);
        b.kind=type;b.material=k.material;b.angle=a;
        if(boss){b.bossId=nextBossId++;activeBossId=b.bossId;bossInitialMass=mass;bossKind=type;bossIntegrity=1;encounterSeconds=0;}
        bodies.add(b);
    }
    private int miniKind(){int best=-1;double score=-1;
        for(int s=0;s<GravityWellCampaign.BOSSES.length;s++){int k=GravityWellCampaign.BOSSES[s];if(!campaign.cleared.contains(KINDS[k].id))continue;
            int source=GravityWellCampaign.specialMaterial(k);double needed=0;
            for(int i=0;i<NODES.length;i++)if(GravityWellBalance.unlockStage(i)<=campaign.stage()&&nodeSource(i)==source)needed+=(NODES[i].max-ranks[i])*nodeMaterialCost(i);
            double value=(needed+1)/(campaign.materials[source]+1);if(value>score){score=value;best=k;}}
        return best;
    }
    private void rebaseBossBodies(){double remaining=bossMass();if(remaining<=0)return;for(Body b:bodies)if(b.bossId==activeBossId)b.initialMass=bossInitialMass*b.mass/remaining;}
    public void save(Properties p){
        if(campaign!=null)campaign.save(p);
        p.setProperty("game.balanceVersion","2");p.setProperty("game.bossIntegrity",""+bossIntegrity);p.setProperty("game.miniClock",""+miniClock);
        p.setProperty("game.parked.count",""+parked.size());int parkedIndex=0;
        for(Map.Entry<String,Parked> entry:parked.entrySet()){String prefix="game.parked."+(parkedIndex++)+".";Parked v=entry.getValue();
            p.setProperty(prefix+"destination",entry.getKey());p.setProperty(prefix+"id",""+v.id);p.setProperty(prefix+"kind",""+v.kind);p.setProperty(prefix+"initial",""+v.initial);p.setProperty(prefix+"integrity",""+v.integrity);p.setProperty(prefix+"count",""+v.bodies.size());
            for(int i=0;i<v.bodies.size();i++)p.setProperty(prefix+"body."+i,bodyState(v.bodies.get(i)));}
        p.setProperty("game.upgradeVersion","1");for(int i=0;i<ranks.length;i++)p.setProperty("game.upgrade."+NODES[i].id,""+ranks[i]);
        p.setProperty("game.visualField",Double.toString(visualField()));p.setProperty("game.separateVisuals",Boolean.toString(separateVisuals));
        p.setProperty("game.version","1");p.setProperty("game.visualPhase",Double.toString(visualPhase));p.setProperty("game.sessionMotion",Double.toString(sessionMotionSeconds));
        double[] values={xp,earned,reservoir,distance,sessionDistance,seconds,sessionSeconds,sessionWork,processed,spawnDust,spawnRock,field,accumulator};
        for(int i=0;i<values.length;i++)p.setProperty("game.v"+i,Double.toString(values[i]));
        p.setProperty("game.counts",strokes+","+sessionStrokes+","+captures+","+sessionCaptures+","+random);
        p.setProperty("game.running",Boolean.toString(running));p.setProperty("game.duration",Integer.toString(duration));
        p.setProperty("game.bossClock",Double.toString(bossClock));p.setProperty("game.bossInitialMass",Double.toString(bossInitialMass));
        p.setProperty("game.activeBossId",Long.toString(activeBossId));p.setProperty("game.nextBossId",Long.toString(nextBossId));p.setProperty("game.bossKind",Integer.toString(bossKind));
        p.setProperty("game.bossesDefeated",Long.toString(bossesDefeated));p.setProperty("game.sessionBosses",Long.toString(sessionBosses));
        for(int i=0;i<ranks.length;i++)p.setProperty("game.rank"+i,Integer.toString(ranks[i]));
        p.setProperty("game.bodies",Integer.toString(bodies.size()));
        for(int i=0;i<bodies.size();i++)p.setProperty("game.body"+i,bodyState(bodies.get(i)));
    }
    private static String bodyState(Body b){return b.x+","+b.y+","+b.vx+","+b.vy+","+b.mass+","+b.dust+","+b.captured+","+b.initialMass+","+b.orbitRadius+","+b.orbitPhase+","+b.captureAge+","+b.shedClock+","+b.angle+","+b.material+","+(b.kind<0?"legacy":kind(b).id)+","+b.bossId+","+b.fragmentDepth+","+b.erosionScale+","+b.miniature+","+b.integrity;}
    public static GravityWellGame restore(Properties p){return restore(p,false);}
    public static GravityWellGame restore(Properties p,boolean synthetic){
        if(!"1".equals(p.getProperty("game.version")))throw new IllegalArgumentException("Unknown save version");
        GravityWellGame g=new GravityWellGame();double[] v=new double[13];for(int i=0;i<v.length;i++){v[i]=Double.parseDouble(p.getProperty("game.v"+i));if(!Double.isFinite(v[i])||v[i]<0)throw new IllegalArgumentException("Invalid save");}
        g.visualPhase=Double.parseDouble(p.getProperty("game.visualPhase","0"));g.sessionMotionSeconds=Double.parseDouble(p.getProperty("game.sessionMotion",Double.toString(v[6])));if(!Double.isFinite(g.visualPhase)||!Double.isFinite(g.sessionMotionSeconds)||g.visualPhase<0||g.sessionMotionSeconds<0)throw new IllegalArgumentException("Invalid clock");
        g.xp=v[0];g.earned=v[1];g.reservoir=v[2];g.distance=v[3];g.sessionDistance=v[4];g.seconds=v[5];g.sessionSeconds=v[6];g.sessionWork=v[7];g.processed=v[8];g.spawnDust=v[9];g.spawnRock=v[10];g.field=v[11];g.accumulator=v[12];
        g.separateVisuals=Boolean.parseBoolean(p.getProperty("game.separateVisuals","false"));g.visualField=Double.parseDouble(p.getProperty("game.visualField",Double.toString(g.field)));if(!Double.isFinite(g.visualField)||g.visualField<0||g.visualField>1)throw new IllegalArgumentException("Invalid visual field");
        String[] c=p.getProperty("game.counts").split(",");g.strokes=Long.parseLong(c[0]);g.sessionStrokes=Long.parseLong(c[1]);g.captures=Long.parseLong(c[2]);g.sessionCaptures=Long.parseLong(c[3]);g.random=Long.parseLong(c[4]);
        g.running=Boolean.parseBoolean(p.getProperty("game.running"));g.duration=Integer.parseInt(p.getProperty("game.duration"));
        boolean stable=p.containsKey("game.upgradeVersion");
        if(stable){if(!"1".equals(p.getProperty("game.upgradeVersion")))throw new IllegalArgumentException("Unknown upgrade version");
            for(String key:p.stringPropertyNames())if(key.startsWith("game.upgrade.")){boolean known=false;for(Node n:NODES)if(key.equals("game.upgrade."+n.id))known=true;if(!known)throw new IllegalArgumentException("Unknown upgrade ID");}}
        for(int i=0;i<g.ranks.length;i++){g.ranks[i]=Integer.parseInt(p.getProperty(stable?"game.upgrade."+NODES[i].id:"game.rank"+i,"0"));if(g.ranks[i]<0||g.ranks[i]>NODES[i].max)throw new IllegalArgumentException("Invalid rank");}
        if(p.containsKey("campaign.version"))g.campaign=GravityWellCampaign.restore(p,synthetic);
        g.bossClock=Double.parseDouble(p.getProperty("game.bossClock","0"));g.bossInitialMass=Double.parseDouble(p.getProperty("game.bossInitialMass","0"));
        g.activeBossId=Long.parseLong(p.getProperty("game.activeBossId","0"));g.nextBossId=Long.parseLong(p.getProperty("game.nextBossId","1"));g.bossKind=Integer.parseInt(p.getProperty("game.bossKind","-1"));
        g.bossesDefeated=Long.parseLong(p.getProperty("game.bossesDefeated","0"));g.sessionBosses=Long.parseLong(p.getProperty("game.sessionBosses","0"));
        if(!Double.isFinite(g.bossClock)||g.bossClock<0||!Double.isFinite(g.bossInitialMass)||g.bossInitialMass<0||g.activeBossId<0||g.nextBossId<=g.activeBossId||g.bossKind< -1||g.bossKind>=KINDS.length||g.bossesDefeated<0||g.sessionBosses<0)throw new IllegalArgumentException("Invalid boss state");
        int n=Integer.parseInt(p.getProperty("game.bodies"));if(n<0||n>150)throw new IllegalArgumentException("Invalid bodies");
        for(int i=0;i<n;i++)g.bodies.add(readBody(p.getProperty("game.body"+i),g.activeBossId));
        if(p.containsKey("game.balanceVersion")&&!"2".equals(p.getProperty("game.balanceVersion")))throw new IllegalArgumentException("Unknown game balance");
        g.bossIntegrity=Double.parseDouble(p.getProperty("game.bossIntegrity",""+(g.activeBossId>0?g.bossMass()/g.bossInitialMass:1)));
        g.miniClock=Double.parseDouble(p.getProperty("game.miniClock","0"));
        if(g.activeBossId>0&&"1".equals(p.getProperty("campaign.version")))g.rebaseBossBodies();
        if(!Double.isFinite(g.bossIntegrity)||g.bossIntegrity<0||g.bossIntegrity>1||!Double.isFinite(g.miniClock)||g.miniClock<0||g.miniClock>GravityWellBalance.MINI_INTERVAL)throw new IllegalArgumentException("Invalid encounter integrity");
        int parkedCount=Integer.parseInt(p.getProperty("game.parked.count","0"));if(parkedCount<0||parkedCount>20||parkedCount>0&&g.campaign==null)throw new IllegalArgumentException("Invalid parked count");
        Set<Long> groups=new HashSet<>();if(g.activeBossId>0)groups.add(g.activeBossId);
        for(int j=0;j<parkedCount;j++){String prefix="game.parked."+j+".";Parked parkedValue=new Parked();String destination=p.getProperty(prefix+"destination");
            parkedValue.id=Long.parseLong(p.getProperty(prefix+"id"));parkedValue.kind=Integer.parseInt(p.getProperty(prefix+"kind"));parkedValue.initial=Double.parseDouble(p.getProperty(prefix+"initial"));parkedValue.integrity=Double.parseDouble(p.getProperty(prefix+"integrity"));int count=Integer.parseInt(p.getProperty(prefix+"count"));
            if(parkedValue.id<=0||parkedValue.id>=g.nextBossId||!groups.add(parkedValue.id)||g.campaign.claimed(parkedValue.id)||parkedValue.kind<0||parkedValue.kind>=KINDS.length||!KINDS[parkedValue.kind].boss||!Double.isFinite(parkedValue.initial)||parkedValue.initial<=0||!Double.isFinite(parkedValue.integrity)||parkedValue.integrity<0||parkedValue.integrity>1||count<1||count>8||destination==null||!(destination.equals("free")||destination.equals("repeat:"+KINDS[parkedValue.kind].id)||GravityWellCampaign.index(destination)>=0&&GravityWellCampaign.DESTINATIONS[GravityWellCampaign.index(destination)].boss==parkedValue.kind))throw new IllegalArgumentException("Invalid parked encounter");
            double mass=0;for(int k=0;k<count;k++){Body body=readBody(p.getProperty(prefix+"body."+k),parkedValue.id);
                if(body.bossId!=parkedValue.id||body.kind!=parkedValue.kind||body.dust||body.miniature)throw new IllegalArgumentException("Parked body mismatch");mass+=body.mass;parkedValue.bodies.add(body);}
            if(Math.abs(mass/parkedValue.initial-parkedValue.integrity)>1e-5)throw new IllegalArgumentException("Parked health mismatch");
            if(g.parked.put(destination,parkedValue)!=null)throw new IllegalArgumentException("Duplicate parked destination");}
        if(g.activeBossId>0&&(g.bossKind<0||!KINDS[g.bossKind].boss||g.bossPieces()==0||g.bossMass()>g.bossInitialMass+.0001))throw new IllegalArgumentException("Invalid boss mass");
        if(g.campaign!=null&&g.activeBossId>0&&Math.abs(g.bossMass()/g.bossInitialMass-g.bossIntegrity)>1e-5)throw new IllegalArgumentException("Boss health mismatch");
        if(g.campaign!=null&&g.campaign.lastRewardGroup>=g.nextBossId)throw new IllegalArgumentException("Invalid reward claim");
        if(g.campaign!=null&&g.campaign.encounterGroup>0&&(g.campaign.encounterGroup!=g.activeBossId||g.campaign.pendingBoss()!=g.bossKind))throw new IllegalArgumentException("Campaign boss mismatch");
        if(g.reservoir>g.capacity()||g.strokes<0||g.sessionStrokes<0||g.duration<0)throw new IllegalArgumentException("Invalid totals");return g;
    }
    private static Body readBody(String data,long active){String[] s=data.split(",");double[] b=new double[5];for(int j=0;j<5;j++){b[j]=Double.parseDouble(s[j]);if(!Double.isFinite(b[j]))throw new IllegalArgumentException("Invalid body");}Body body=new Body(b[0],b[1],b[2],b[3],b[4],Boolean.parseBoolean(s[5]));body.captured=Boolean.parseBoolean(s[6]);
            if(s.length>=14){double[] extra=new double[6];for(int j=0;j<6;j++){extra[j]=Double.parseDouble(s[7+j]);if(!Double.isFinite(extra[j]))throw new IllegalArgumentException("Invalid orbit");}
                body.initialMass=extra[0];body.orbitRadius=extra[1];body.orbitPhase=extra[2];body.captureAge=extra[3];body.shedClock=extra[4];body.angle=extra[5];body.material=Integer.parseInt(s[13]);}
            if(s.length>=18){if(!"legacy".equals(s[14])){for(int k=0;k<KINDS.length;k++)if(KINDS[k].id.equals(s[14]))body.kind=k;if(body.kind<0)throw new IllegalArgumentException("Unknown body class");}
                body.bossId=Long.parseLong(s[15]);body.fragmentDepth=Integer.parseInt(s[16]);body.erosionScale=Double.parseDouble(s[17]);
                if(body.bossId<0||body.bossId>0&&body.bossId!=active||body.fragmentDepth<0||body.fragmentDepth>8||!Double.isFinite(body.erosionScale)||body.erosionScale<=0||body.erosionScale>5)throw new IllegalArgumentException("Invalid fragment state");}
            if(s.length>=20){body.miniature=Boolean.parseBoolean(s[18]);body.integrity=Double.parseDouble(s[19]);if(!Double.isFinite(body.integrity)||body.integrity<0||body.integrity>1||body.miniature&&(body.kind<0||!kind(body).boss||body.bossId>0||Math.abs(body.mass/body.initialMass-body.integrity)>1e-5))throw new IllegalArgumentException("Invalid miniature");}
            if(body.mass<0||body.initialMass<=0||body.orbitRadius<0||body.captureAge<0||body.shedClock<0||body.material<0||body.material>3)throw new IllegalArgumentException("Invalid body state");return body;}
}




