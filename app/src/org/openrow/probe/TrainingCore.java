package org.openrow.probe;

import java.util.*;

/** Game-independent, deterministic foundation training. No physiological threshold inference. */
public final class TrainingCore {
    private TrainingCore() {}
    public static final String POLICY = "foundation-1-experimental";
    public static final long DAY = 86400000L;
    public enum Goal { ENDURANCE, GENERAL_CARDIO }
    public enum Category { ENDURANCE, SUSTAINED, REPEATABILITY }
    public enum Mode { RECOMMENDED, COACH }
    public enum Readiness { READY, TIRED, STOP }
    public enum ReturnChoice { MAINTAIN, LOWER_LITTLE, RAISE_LITTLE, LOWER_LOT }
    public enum Kind { TRAINING, QUICK, BENCHMARK, STARTER, FREE_ROW }
    public enum Phase { WARM_UP, WORK, RECOVERY, COOL_DOWN }
    public enum Outcome { COMPLETED, EARLY_STOP }

    /** These are conservative product defaults awaiting exercise-professional validation. */
    public static final class Policy {
        public static final int MIN_MINUTES=5, MAX_MINUTES=45, DEFAULT_MINUTES=10;
        public static final int PROGRESS_AFTER=3, MAX_STEP_SECONDS=60;
        public static final long HARD_SPACING=2*DAY, EVIDENCE_AGE=28*DAY;
        private Policy() {}
    }
    public static final class Block {
        public final Phase phase;
        public final Category category;
        public final int seconds, effort;
        public Block(Phase phase, Category category, int seconds, int effort) {
            if(phase==null||category==null||seconds<1||seconds>2700||effort<0||effort>6)
                throw new IllegalArgumentException("Invalid training block");
            this.phase=phase;this.category=category;this.seconds=seconds;this.effort=effort;
        }
        public String guidance() {
            return guidanceFor(effort);
        }
    }
    public static final class Plan {
        public final String protocol, reason, policy;
        public final Kind kind;
        public final Category category;
        public final Goal goal;
        public final List<Block> blocks;
        public final boolean provisional;
        public Plan(String protocol,String reason,Kind kind,Category category,Goal goal,List<Block> blocks,boolean provisional) {
            this(protocol,reason,POLICY,kind,category,goal,blocks,provisional);
        }
        private Plan(String protocol,String reason,String policy,Kind kind,Category category,Goal goal,List<Block> blocks,boolean provisional) {
            if(protocol==null||protocol.isEmpty()||reason==null||blocks==null||blocks.isEmpty()||blocks.size()>64)
                throw new IllegalArgumentException("Invalid plan");
            this.protocol=protocol;this.reason=reason;this.policy=Objects.requireNonNull(policy);this.kind=Objects.requireNonNull(kind);
            this.category=Objects.requireNonNull(category);this.goal=Objects.requireNonNull(goal);
            this.blocks=Collections.unmodifiableList(new ArrayList<Block>(blocks));this.provisional=provisional;
            for(Block b:this.blocks)Objects.requireNonNull(b);
            if(seconds()>Policy.MAX_MINUTES*60)throw new IllegalArgumentException("Workout exceeds foundation duration limit");
        }
        public int seconds(){int n=0;for(Block b:blocks)n+=b.seconds;return n;}
        public boolean controlled(){for(Block b:blocks)if(b.effort>=5)return true;return false;}
        public String title(){return kind==Kind.QUICK?"Quick baseline":kind==Kind.BENCHMARK?"Full benchmark":kind==Kind.STARTER?"Starter series · "+categoryName(category):kind==Kind.FREE_ROW?"Free rowing":categoryName(category);}
    }
    public static final class Record {
        public final String id;
        public final Plan plan;
        public final long started, ended;
        public final boolean simulated, adjusted;
        public final Outcome outcome;
        public final double elapsed, rowingSeconds, distance, missingSeconds;
        public final int effort, manageable; // -1 = unanswered; manageable: 0=no, 1=yes
        public final int interruptions;
        public final List<String> timeline;
        public final double[] blockSeconds, blockDistance, blockRowingSeconds;
        public Record(Runner r,long ended,int effort,int manageable) {
            if(!r.finished||ended<r.started||effort< -1||effort>10||manageable< -1||manageable>1)
                throw new IllegalArgumentException("Invalid session result");
            id=r.id;plan=r.plan;started=r.started;this.ended=ended;simulated=r.simulated;adjusted=r.adjusted;
            outcome=r.outcome;elapsed=r.elapsed;rowingSeconds=r.rowingSeconds;distance=r.distance;
            missingSeconds=r.missingSeconds;this.effort=effort;this.manageable=manageable;
            interruptions=r.interruptions;timeline=r.timeline();
            blockSeconds=r.blockSeconds.clone();blockDistance=r.blockDistance.clone();blockRowingSeconds=r.blockRowingSeconds.clone();
        }
        private Record(Properties p) {
            Runner r=Runner.restore(p);if(!r.finished)throw new IllegalArgumentException("Unfinished record");
            id=r.id;plan=r.plan;started=r.started;ended=longNumber(p,"ended",0);
            if(ended<started)throw new IllegalArgumentException("Invalid result date");
            simulated=r.simulated;adjusted=r.adjusted;outcome=r.outcome;elapsed=r.elapsed;
            rowingSeconds=r.rowingSeconds;distance=r.distance;missingSeconds=r.missingSeconds;
            effort=integer(p,"effort",-1,-1,10);manageable=integer(p,"manageable",-1,-1,1);
            interruptions=r.interruptions;timeline=r.timeline();
            blockSeconds=r.blockSeconds;blockDistance=r.blockDistance;blockRowingSeconds=r.blockRowingSeconds;
        }
        public boolean usable(){return !simulated&&rowingSeconds>=60&&effort>=0&&manageable>=0&&missingSeconds<=elapsed*.2;}
        private int expectedEffort(){int maximum=0;for(Block b:plan.blocks)maximum=Math.max(maximum,b.effort);return plan.kind==Kind.FREE_ROW?5:maximum;}
        private boolean workObserved(){double work=0,moving=0;for(int i=0;i<plan.blocks.size();i++)if(plan.blocks.get(i).phase==Phase.WORK){work+=blockSeconds[i];moving+=blockRowingSeconds[i];}return work>0&&moving>=work*.7;}
        public boolean success(){return usable()&&workObserved()&&outcome==Outcome.COMPLETED&&!adjusted&&manageable==1&&effort<=Math.min(6,expectedEffort()+1)&&(plan.kind==Kind.TRAINING||plan.kind==Kind.FREE_ROW||interruptions==0);}
        public boolean difficult(){return usable()&&(manageable==0||effort>=Math.min(8,expectedEffort()+3));}
        public boolean comparable(){return usable()&&workObserved()&&!adjusted&&interruptions==0&&outcome==Outcome.COMPLETED&&(plan.kind==Kind.QUICK||plan.kind==Kind.BENCHMARK||plan.kind==Kind.STARTER);}
        public boolean contributes(Category c){double seconds=0,moving=0;for(int i=0;i<plan.blocks.size();i++)if(plan.blocks.get(i).phase==Phase.WORK&&plan.blocks.get(i).category==c){seconds+=blockSeconds[i];moving+=blockRowingSeconds[i];}return seconds>0&&moving>=seconds*.7;}
        public int categoryDose(Category c){
            if(plan.kind==Kind.TRAINING||plan.kind==Kind.STARTER||plan.kind==Kind.FREE_ROW)return (int)elapsed;
            int seconds=0;for(int i=0;i<plan.blocks.size();i++)if(plan.blocks.get(i).category==c&&
                (plan.blocks.get(i).phase==Phase.WORK||plan.blocks.get(i).phase==Phase.RECOVERY))seconds+=(int)blockSeconds[i];
            // Map observed category work to a short foundation session, not the full composite duration.
            return Math.max(300,seconds+180);
        }
        /** Raw benchmark observation, not a calibrated speed or a physiological score. */
        public double workDistance(Category c){double n=0;for(int i=0;i<plan.blocks.size();i++)
            if(plan.blocks.get(i).phase==Phase.WORK&&plan.blocks.get(i).category==c)n+=blockDistance[i];return n;}
    }
    public static final class State {
        public final List<Record> history;
        public Goal goal=Goal.GENERAL_CARDIO;
        public Mode mode=Mode.RECOMMENDED;
        public long lastActivityAt;
        public State(List<Record> history){
            List<Record> rows=new ArrayList<Record>();Set<String> ids=new HashSet<String>();
            for(Record r:history)if(!r.simulated&&ids.add(r.id))rows.add(r);
            Collections.sort(rows,TrainingCore::compareRecords);this.history=Collections.unmodifiableList(rows);
        }
        public long lastTraining(){long t=lastActivityAt;for(Record r:history)if(r.rowingSeconds>0)t=Math.max(t,r.ended);return t;}
        public boolean returning(long now){return lastTraining()>0&&now-lastTraining()>14*DAY;}
        public int evidence(Category c,long now){int n=0;for(Record r:history)
            if(r.contributes(c)&&r.usable()&&r.ended<=now&&now-r.ended<=Policy.EVIDENCE_AGE)n++;return n;}
        public double confidence(Category c,long now){int n=evidence(c,now);return Math.min(1,n/3.0);}
        public int manageableEvidence(Category c,long now){int n=0;for(Record r:history)if(r.contributes(c)&&r.success()&&r.ended<=now&&now-r.ended<=Policy.EVIDENCE_AGE)n++;return n;}
        /** Relative change within one protocol at similar reported effort; never a population fitness rank. */
        public Double benchmarkChange(Category c,long now){
            Record latest=null,first=null;
            for(int i=history.size()-1;i>=0;i--){Record r=history.get(i);
                if(r.ended>now||now-r.ended>90*DAY||!r.comparable()||!r.contributes(c)||r.workDistance(c)<=0)continue;
                if(latest==null){latest=r;continue;}
                if(r.plan.protocol.equals(latest.plan.protocol)&&Math.abs(r.effort-latest.effort)<=1)first=r;
            }
            return first==null?null:latest.workDistance(c)/first.workDistance(c)-1;
        }
        public int toleranceSeconds(Category c,long now){
            List<Integer> v=new ArrayList<Integer>();int setbacks=0,examined=0;
            for(int i=history.size()-1;i>=0;i--){Record r=history.get(i);if(!r.contributes(c)||!r.usable()||r.ended>now||now-r.ended>Policy.EVIDENCE_AGE)continue;
                if(examined<2&&r.difficult())setbacks++;examined++;
                if(r.success())v.add(r.categoryDose(c));if(v.size()==5)break;}
            int baseline=Policy.DEFAULT_MINUTES*60;
            if(!v.isEmpty()){Collections.sort(v);baseline=v.get(v.size()/2);}
            // Two comparable difficult observations lower current tolerance, not historical records.
            return Math.max(300,baseline-(setbacks>=2?120:0));
        }
        public Category nextCategory(long now){
            // Goal-weighted rotation uses demonstrated tolerance, not a diagnosis of weakness.
            if(history.isEmpty())return Category.ENDURANCE;
            for(Category c:Category.values())if(evidence(c,now)==0)return c;
            Category chosen=Category.ENDURANCE;double lowest=Double.POSITIVE_INFINITY;
            for(Category c:Category.values()) {
                double weight=goal==Goal.ENDURANCE?(c==Category.ENDURANCE?2:c==Category.SUSTAINED?1:.5):1;
                double score=0;for(Record r:history)if(r.contributes(c)&&r.success()&&r.ended<=now&&now-r.ended<=14*DAY)score+=r.categoryDose(c);
                score/=weight;
                Double change=benchmarkChange(c,now);
                if(change!=null&&evidence(c,now)>=3)score*=1+Math.max(-.25,Math.min(.25,change));
                if(score<lowest){lowest=score;chosen=c;}
            }return chosen;
        }
    }
    public static final class Recommendation {
        public final Plan recommended;
        public final List<Plan> alternatives;
        public Recommendation(Plan p,List<Plan> a){recommended=p;alternatives=Collections.unmodifiableList(a);}
    }
    public static Recommendation recommend(State s,int availableMinutes,Readiness readiness,ReturnChoice comeback,long now) {
        Objects.requireNonNull(readiness);Objects.requireNonNull(comeback);
        if(readiness==Readiness.STOP)throw new IllegalStateException("Rest or defer today's training");
        int cap=Math.max(Policy.MIN_MINUTES,Math.min(Policy.MAX_MINUTES,availableMinutes))*60;
        Category c=s.nextCategory(now);int dose=Math.min(cap,s.toleranceSeconds(c,now));
        String reason=s.evidence(c,now)==0?"Build evidence with a balanced foundation session.":"Your goal, recent training balance, and available benchmark trends favor "+categoryName(c)+".";
        boolean recentHard=false,uncertainRecent=false;int successes=0,difficult=0;
        if(!s.history.isEmpty()) {Record last=s.history.get(s.history.size()-1);
            uncertainRecent=last.ended<=now&&now-last.ended<=Policy.EVIDENCE_AGE&&!last.usable();}
        for(int i=s.history.size()-1;i>=0;i--){Record r=s.history.get(i);
            if(r.ended>now)continue;
            if(now-r.ended<Policy.HARD_SPACING&&r.rowingSeconds>0&&(r.plan.controlled()||r.effort>=7))recentHard=true;
            if(!r.contributes(c)||!r.usable()||now-r.ended>Policy.EVIDENCE_AGE)continue;
            if(r.difficult()){difficult++;break;}
            if(r.success())successes++;else break;
            if(successes==Policy.PROGRESS_AFTER)break;
        }
        if(successes>=Policy.PROGRESS_AFTER&&!s.returning(now)&&!uncertainRecent){dose+=Policy.MAX_STEP_SECONDS;reason="Several manageable sessions support one extra minute, within today's available time.";}
        if(uncertainRecent)reason="Recent feedback or telemetry is incomplete: repeat comfortable work and gather current evidence.";
        if(difficult>0){dose-=120;reason="Recent difficulty: ease back and reassess before progressing.";}
        // A difficult session in any category affects today's recommendation without changing ability.
        if(!s.history.isEmpty()) {Record last=s.history.get(s.history.size()-1);
            if(last.ended<=now&&now-last.ended<Policy.HARD_SPACING&&last.difficult())readiness=Readiness.TIRED;}
        if(s.returning(now)){
            int adjustment=comeback==ReturnChoice.LOWER_LOT?-300:comeback==ReturnChoice.LOWER_LITTLE?-120:comeback==ReturnChoice.RAISE_LITTLE?60:0;
            dose+=adjustment;reason="Welcome back: use your check-in and rebuild current evidence.";
            // Older performance remains in history, but does not mandate a long return workout.
            if(now-s.lastTraining()>28*DAY)dose=Math.min(dose,600);
        }
        if(readiness==Readiness.TIRED){dose-=180;c=Category.ENDURANCE;reason="You may need recovery: a shorter, easy session today.";}
        boolean intervals=c==Category.REPEATABILITY&&!recentHard&&!uncertainRecent&&readiness==Readiness.READY&&!s.returning(now)&&s.history.size()>=3&&s.manageableEvidence(c,now)>=2;
        if(c==Category.REPEATABILITY&&!intervals){reason+=" Keep effort comfortable while evidence or recovery is limited.";}
        dose=Math.max(300,Math.min(cap,dose));
        Plan main=foundation(s.goal,c,dose,intervals,reason,s.returning(now)||s.confidence(c,now)<1);
        List<Plan> alternatives=new ArrayList<Plan>();
        alternatives.add(foundation(s.goal,Category.ENDURANCE,Math.max(300,dose-180),false,"An easier option for today.",true));
        alternatives.add(foundation(s.goal,c==Category.ENDURANCE?Category.SUSTAINED:Category.ENDURANCE,dose,false,"A different comfortable foundation session.",true));
        return new Recommendation(main,alternatives);
    }
    public static String categoryName(Category c){return c==Category.ENDURANCE?"comfortable endurance":c==Category.SUSTAINED?"sustained work":"controlled-effort repeatability";}
    public static String guidanceFor(int effort){return effort<=2?"Easy, comfortable conversation":effort<=4?"Comfortable, full sentences":"Controlled effort; ease off if needed";}
    private static void add(List<Block> b,Phase p,Category c,int seconds,int effort){if(seconds>0)b.add(new Block(p,c,seconds,effort));}
    private static Plan foundation(Goal goal,Category c,int seconds,boolean intervals,String reason,boolean provisional){
        List<Block>b=new ArrayList<Block>();int warm=Math.min(180,seconds/5),cool=warm,work=seconds-warm-cool;
        add(b,Phase.WARM_UP,c,warm,2);
        if(c==Category.REPEATABILITY){int count=Math.max(2,work/180),each=work/count,remainder=work%count;
            for(int i=0;i<count;i++){int total=each+(i==count-1?remainder:0);int recovery=total/2;
                add(b,Phase.WORK,c,total-recovery,intervals?5:4);add(b,Phase.RECOVERY,c,recovery,2);}}
        else add(b,Phase.WORK,c,work,c==Category.SUSTAINED?4:3);
        add(b,Phase.COOL_DOWN,c,cool,2);
        return new Plan("foundation-"+c.name().toLowerCase(Locale.US)+"-v1",reason,Kind.TRAINING,c,goal,b,provisional);
    }
    public static Plan assessment(Kind kind,Category starter,Goal goal){
        if(kind==Kind.TRAINING||kind==Kind.FREE_ROW)throw new IllegalArgumentException("Choose assessment type");
        List<Block>b=new ArrayList<Block>();
        if(kind==Kind.QUICK){add(b,Phase.WARM_UP,Category.ENDURANCE,90,2);add(b,Phase.WORK,Category.ENDURANCE,90,3);
            add(b,Phase.WORK,Category.SUSTAINED,60,4);add(b,Phase.COOL_DOWN,Category.ENDURANCE,60,2);}
        else if(kind==Kind.BENCHMARK){add(b,Phase.WARM_UP,Category.ENDURANCE,300,2);
            add(b,Phase.WORK,Category.ENDURANCE,600,3);add(b,Phase.RECOVERY,Category.ENDURANCE,120,2);
            add(b,Phase.WORK,Category.SUSTAINED,360,4);add(b,Phase.RECOVERY,Category.SUSTAINED,120,2);
            for(int i=0;i<4;i++){add(b,Phase.WORK,Category.REPEATABILITY,60,4);add(b,Phase.RECOVERY,Category.REPEATABILITY,60,2);}
            add(b,Phase.COOL_DOWN,Category.ENDURANCE,120,2);}
        else {add(b,Phase.WARM_UP,starter,180,2);
            if(starter==Category.REPEATABILITY)for(int i=0;i<4;i++){add(b,Phase.WORK,starter,60,4);add(b,Phase.RECOVERY,starter,60,2);}
            else add(b,Phase.WORK,starter,480,starter==Category.ENDURANCE?3:4);
            add(b,Phase.COOL_DOWN,starter,120,2);}
        return new Plan(kind.name().toLowerCase(Locale.US)+(kind==Kind.STARTER?"-"+starter.name().toLowerCase(Locale.US):"")+"-v1",
            "Repeat this fixed protocol at comparable effort; this is a provisional performance observation.",kind,
            kind==Kind.STARTER?starter:Category.ENDURANCE,goal,b,true);
    }

    /** Authoritative monotonic clock; call tick from any scheduler, independent of game frames. */
    public static final class Runner {
        public final String id; public final Plan plan; public final long started; public final boolean simulated;
        public boolean paused=true,finished,adjusted;
        public Outcome outcome=Outcome.EARLY_STOP;
        public int block;public double elapsed,within,rowingSeconds,distance,missingSeconds,pauseSeconds;
        public int interruptions;
        public final double[] blockSeconds,blockDistance,blockRowingSeconds;
        public boolean telemetryAvailable;
        private final List<String> events=new ArrayList<String>();
        private long last=-1;
        private int effortReduction;
        public Runner(String id,Plan plan,long wallTime,boolean simulated){
            if(id==null||!id.matches("[A-Za-z0-9_-]{1,80}")||wallTime<0)throw new IllegalArgumentException("Invalid session");
            this.id=id;this.plan=Objects.requireNonNull(plan);started=wallTime;this.simulated=simulated;
            blockSeconds=new double[plan.blocks.size()];blockDistance=new double[plan.blocks.size()];blockRowingSeconds=new double[plan.blocks.size()];
        }
        public Block current(){return plan.blocks.get(Math.min(block,plan.blocks.size()-1));}
        public int effort(){return Math.max(1,current().effort-effortReduction);}
        private void event(String type){events.add(elapsed+"|"+type+"|"+block+"|"+effortReduction);}
        public List<String> timeline(){return Collections.unmodifiableList(new ArrayList<String>(events));}
        public void easier(){if(!finished){effortReduction=Math.min(3,effortReduction+1);adjusted=true;event("easier");}}
        public void resume(long mono){if(!finished&&paused){paused=false;last=mono;event("resume");}}
        public void pause(long mono){boolean wasRunning=!paused&&!finished;tick(mono,false,false);if(wasRunning&&!finished){interruptions++;event("pause");}paused=true;last=mono;}
        public void tick(long mono,boolean moving,boolean telemetryAvailable){
            this.telemetryAvailable=telemetryAvailable&&!paused&&!finished;
            if(mono<0)throw new IllegalArgumentException("Invalid clock");
            if(last<0){last=mono;return;}if(mono<last){last=mono;return;}
            double dt=(mono-last)/1000.0;last=mono;
            if(finished)return;if(paused){pauseSeconds+=dt;return;}
            while(dt>0&&!finished){double step=Math.min(dt,current().seconds-within);dt-=step;elapsed+=step;within+=step;blockSeconds[block]+=step;
                if(!telemetryAvailable)missingSeconds+=step;else if(moving){rowingSeconds+=step;blockRowingSeconds[block]+=step;}
                if(within>=current().seconds-1e-8){within=0;block++;event("transition");if(block==plan.blocks.size()){finished=true;paused=true;outcome=Outcome.COMPLETED;event("completed");}}}
        }
        public void observeDistance(double meters){if(!Double.isFinite(meters)||meters<0||meters>1000)throw new IllegalArgumentException("Invalid meters");
            if(!paused&&!finished){distance+=meters;blockDistance[block]+=meters;}}
        public void stop(long mono){pause(mono);if(!finished){finished=true;outcome=Outcome.EARLY_STOP;event("early-stop");}}
        public void shortenWork(){if(!finished&&current().phase==Phase.WORK){adjusted=true;event("shorten-work");block++;within=0;
            if(block==plan.blocks.size()){finished=true;paused=true;outcome=Outcome.EARLY_STOP;}}}
        // A recovery extension is an explicit pause: it never silently inflates work or rowing.
        public void extendRecovery(long mono){if(current().phase==Phase.RECOVERY){adjusted=true;pause(mono);}}
        public Properties save(){Properties p=new Properties();p.setProperty("version","1");p.setProperty("id",id);p.setProperty("started",""+started);
            p.setProperty("paused",""+paused);
            p.setProperty("simulated",""+simulated);p.setProperty("finished",""+finished);p.setProperty("adjusted",""+adjusted);p.setProperty("outcome",outcome.name());
            p.setProperty("block",""+block);p.setProperty("within",""+within);p.setProperty("elapsed",""+elapsed);p.setProperty("rowing",""+rowingSeconds);
            p.setProperty("distance",""+distance);p.setProperty("missing",""+missingSeconds);p.setProperty("pause",""+pauseSeconds);p.setProperty("reduction",""+effortReduction);
            p.setProperty("interruptions",""+interruptions);p.setProperty("events",""+events.size());for(int i=0;i<events.size();i++)p.setProperty("event."+i,events.get(i));
            p.setProperty("protocol",plan.protocol);p.setProperty("reason",plan.reason);p.setProperty("policy",plan.policy);p.setProperty("kind",plan.kind.name());
            p.setProperty("category",plan.category.name());p.setProperty("goal",plan.goal.name());p.setProperty("provisional",""+plan.provisional);p.setProperty("count",""+plan.blocks.size());
            for(int i=0;i<plan.blocks.size();i++){Block b=plan.blocks.get(i);p.setProperty("b."+i,b.phase+","+b.category+","+b.seconds+","+b.effort);
                p.setProperty("bs."+i,""+blockSeconds[i]);p.setProperty("bd."+i,""+blockDistance[i]);p.setProperty("br."+i,""+blockRowingSeconds[i]);}return p;
        }
        public static Runner restore(Properties p){
            if(!"1".equals(p.getProperty("version")))throw new IllegalArgumentException("Unknown training version");
            int count=integer(p,"count",0,1,64);List<Block>b=new ArrayList<Block>();
            for(int i=0;i<count;i++){String[] v=p.getProperty("b."+i,"").split(",");if(v.length!=4)throw new IllegalArgumentException("Invalid block");
                b.add(new Block(Phase.valueOf(v[0]),Category.valueOf(v[1]),Integer.parseInt(v[2]),Integer.parseInt(v[3])));}
            Plan plan=new Plan(p.getProperty("protocol"),p.getProperty("reason"),p.getProperty("policy"),Kind.valueOf(p.getProperty("kind")),Category.valueOf(p.getProperty("category")),Goal.valueOf(p.getProperty("goal")),b,Boolean.parseBoolean(p.getProperty("provisional")));
            Runner r=new Runner(p.getProperty("id"),plan,longNumber(p,"started",0),Boolean.parseBoolean(p.getProperty("simulated")));
            r.finished=Boolean.parseBoolean(p.getProperty("finished"));r.adjusted=Boolean.parseBoolean(p.getProperty("adjusted"));r.outcome=Outcome.valueOf(p.getProperty("outcome"));
            r.block=integer(p,"block",0,0,count);r.within=number(p,"within",0);r.elapsed=number(p,"elapsed",0);r.rowingSeconds=number(p,"rowing",0);
            r.distance=number(p,"distance",0);r.missingSeconds=number(p,"missing",0);r.pauseSeconds=number(p,"pause",0);r.effortReduction=integer(p,"reduction",0,0,3);
            r.interruptions=integer(p,"interruptions",0,0,Integer.MAX_VALUE);int events=integer(p,"events",0,0,100000);
            for(int i=0;i<events;i++)r.events.add(Objects.requireNonNull(p.getProperty("event."+i)));
            if(!r.finished&&!Boolean.parseBoolean(p.getProperty("paused","true"))){r.interruptions++;r.event("restart");}
            double sum=0;for(int i=0;i<count;i++){r.blockSeconds[i]=number(p,"bs."+i,0);r.blockDistance[i]=number(p,"bd."+i,0);r.blockRowingSeconds[i]=number(p,"br."+i,0);sum+=r.blockSeconds[i];if(r.blockSeconds[i]>b.get(i).seconds||r.blockRowingSeconds[i]>r.blockSeconds[i])throw new IllegalArgumentException("Invalid block time");}
            if((r.block==count&&!r.finished)||(!r.finished&&r.within>=r.current().seconds)||r.elapsed>plan.seconds()||Math.abs(sum-r.elapsed)>.01||r.rowingSeconds+r.missingSeconds>r.elapsed+.01)
                throw new IllegalArgumentException("Invalid training totals");
            return r; // Always paused after process restart; never credit offline time.
        }
    }
    public static final class History {
        public final Properties data=new Properties();
        public History(){data.setProperty("version","1");}
        public History(Properties p){if(!"1".equals(p.getProperty("version")))throw new IllegalArgumentException("Unknown history version");data.putAll(p);state();}
        public void put(Record record,Runner runner){if(record.simulated)return;
            if(!record.id.equals(runner.id))throw new IllegalArgumentException("Mismatched record");
            Properties p=runner.save();p.setProperty("ended",""+record.ended);p.setProperty("effort",""+record.effort);p.setProperty("manageable",""+record.manageable);
            String prefix="r."+record.id+".";for(String k:p.stringPropertyNames())data.setProperty(prefix+k,p.getProperty(k));
        }
        public List<Record> records(){List<Record>rows=new ArrayList<Record>();for(String k:data.stringPropertyNames())if(k.startsWith("r.")&&k.endsWith(".id")){
            String prefix=k.substring(0,k.length()-2);Properties p=new Properties();for(String key:data.stringPropertyNames())if(key.startsWith(prefix))p.setProperty(key.substring(prefix.length()),data.getProperty(key));rows.add(new Record(p));}
            Collections.sort(rows,TrainingCore::compareRecords);return rows;}
        public State state(){State s=new State(records());s.goal=Goal.valueOf(data.getProperty("goal",Goal.GENERAL_CARDIO.name()));s.mode=Mode.valueOf(data.getProperty("mode",Mode.RECOMMENDED.name()));s.lastActivityAt=longNumber(data,"lastActivity",0);return s;}
        public void settings(Goal g,Mode m){data.setProperty("goal",g.name());data.setProperty("mode",m.name());}
    }
    private static double number(Properties p,String k,double fallback){double n=Double.parseDouble(p.getProperty(k,""+fallback));if(!Double.isFinite(n)||n<0)throw new IllegalArgumentException("Invalid "+k);return n;}
    private static int compareRecords(Record a,Record b){int order=Long.compare(a.ended,b.ended);return order!=0?order:a.id.compareTo(b.id);}
    private static long longNumber(Properties p,String k,long fallback){long n=Long.parseLong(p.getProperty(k,""+fallback));if(n<0)throw new IllegalArgumentException("Invalid "+k);return n;}
    private static int integer(Properties p,String k,int fallback,int min,int max){int n=Integer.parseInt(p.getProperty(k,""+fallback));if(n<min||n>max)throw new IllegalArgumentException("Invalid "+k);return n;}
}
