package org.openrow.probe;

/** Authored encounter/economy policy. Game contribution is not a fitness measurement. */
public final class GravityWellBalance {
    public static final int VERSION=2;
    public static final double[] HOURS={1,4,4.5,4.5,5,5,6,6,6,6};
    public static final int[] UNPREPARED_MINUTES={40,60,90,120,180,240,300,360,420,480};
    public static final int[] LAST_LAYER={3,6,8,9,11,12,15,18,21,24};
    public static final double PREPARED_SECONDS=300, MINI_FRACTION=.1, MINI_INTERVAL=90;
    private GravityWellBalance(){}
    public static int stage(int kind){for(int i=0;i<GravityWellCampaign.BOSSES.length;i++)if(GravityWellCampaign.BOSSES[i]==kind)return i;return -1;}
    public static double preparationSeconds(int stage){return (HOURS[stage]*3600-PREPARED_SECONDS)/3;}
    public static int unlockStage(int index){
        GravityWellGame.Node n=GravityWellGame.NODES[index];
        if(index<6)return 0;if(index==6)return 1;if(index==7)return 5;if(index==8)return 9;
        int s=0;while(s<10&&n.layer>LAST_LAYER[s])s++;
        if(n.specialist&&n.source>=5){int k=n.source==5?7:n.source==6?8:n.source==7||n.source==8?9:n.source==9?10:n.source+1;s=Math.max(s,stage(k)+1);}
        return s;
    }
    public static int source(int index){GravityWellGame.Node n=GravityWellGame.NODES[index];
        if(index<9||n.specialist)return n.source;int s=unlockStage(index);return s==0?-1:GravityWellCampaign.specialMaterial(GravityWellCampaign.BOSSES[s-1]);}
    public static int materialCost(int index){return source(index)<0?0:GravityWellGame.NODES[index].materialCost;}
    public static boolean main(int i){return !GravityWellGame.NODES[i].specialist&&i!=5;}
    public static double materialDemand(int stage,int source){double count=0;for(int i=0;i<GravityWellGame.NODES.length;i++)if(main(i)&&unlockStage(i)==stage&&source(i)==source)count+=GravityWellGame.NODES[i].max*materialCost(i);return count;}
    public static double reserve(int kind){int s=stage(kind),source=GravityWellCampaign.specialMaterial(kind);return Math.max(2,materialDemand(s+1,source)*.04);}
    public static double firstBonus(int kind){int s=stage(kind);return Math.max(5,materialDemand(s+1,GravityWellCampaign.specialMaterial(kind))*.5-reserve(kind));}
    public static int cost(int index,int rank){GravityWellGame.Node n=GravityWellGame.NODES[index];
        if(!main(index))return (int)Math.round(n.cost*Math.pow(1.7,rank)*3);
        int stage=unlockStage(index);double base=0;
        for(int i=0;i<GravityWellGame.NODES.length;i++)if(main(i)&&unlockStage(i)==stage)for(int r=0;r<GravityWellGame.NODES[i].max;r++)base+=GravityWellGame.NODES[i].cost*Math.pow(1.7,r);
        double budget=(HOURS[stage]*3600)*.935;
        return Math.max(1,(int)Math.round(n.cost*Math.pow(1.7,rank)*budget/base));
    }
    public static double xpFactor(GravityWellGame g){return 1+Math.min(.2,.025*g.ranks[2]+g.extraRanks(3)/480.0);}
    public static double yieldFactor(GravityWellGame g){return 1+Math.min(.2,g.extraRanks(6)/240.0);}
    private static double coverage(GravityWellGame g,int stage,int branch){double have=0,total=0;
        for(int i=0;i<g.ranks.length;i++){GravityWellGame.Node n=GravityWellGame.NODES[i];if(!main(i)||n.branch!=branch||i>=6&&i<9)continue;
            double weight=i<9?2:1;have+=g.ranks[i]*weight;if(unlockStage(i)<=stage)total+=n.max*weight;}
        return total==0?0:have/total;
    }
    public static double readiness(GravityWellGame g,int kind){int s=stage(kind);if(s<0)return 0;
        double reach=coverage(g,s,0),flow=coverage(g,s,1),erosion=coverage(g,s,5);
        int fracture=6+GravityWellGame.KINDS[kind].family;
        boolean available=unlockStage(fracture)<=s;
        return available?.2*reach+.25*flow+.4*erosion+.15*g.ranks[fracture]/GravityWellGame.NODES[fracture].max:(.2*reach+.25*flow+.4*erosion)/.85;
    }
    public static double seconds(GravityWellGame g,int kind){int s=stage(kind);if(s<0)return 300;double readiness=readiness(g,kind),bounded=Math.min(1,readiness);
        double bare=UNPREPARED_MINUTES[s]*60.0;
        double seconds=bare*Math.pow(PREPARED_SECONDS/bare,bounded*bounded);
        double specialist=(kind==7||kind==8)?.15*g.ranks[77]:(kind==9||kind==10)?.15*g.ranks[79]+.125*g.ranks[80]:0;
        for(int chapter=4;chapter<=8;chapter++){int i=81+(chapter-4)*24+22;if(GravityWellGame.NODES[i].source==GravityWellCampaign.specialMaterial(kind))specialist+=.15*g.ranks[i];}
        return seconds/Math.min(4,Math.max(1,readiness))/ (1+Math.min(.2,specialist));
    }
    public static void referenceLoadout(GravityWellGame g,int stage){for(int i=0;i<g.ranks.length;i++)if(main(i)&&unlockStage(i)<=stage)g.ranks[i]=GravityWellGame.NODES[i].max;}
}
