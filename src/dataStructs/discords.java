/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructs;

import utils.calcAvgPhred;
import utils.probBasedPhred;

/**
 *
 * @author bickhart
 */
public class discords {
    public hits forwardHit;
    public hits reverseHit;
    public double probScore;
    public double avgPhred;
    public callEnum type;
    public int span;
    public boolean hasPerfectConcordant = false; // This is just for filtration later in vh hunter
    
    public discords(hits forward, hits reverse, callEnum type, int lower, int span, boolean concord){
        this.forwardHit = forward;
        this.reverseHit = reverse;
        this.type = type;
        this.span = span;
        this.hasPerfectConcordant = concord;
        calcProbScore(lower);
        calcAvgPhred();
    }
    
    private void calcProbScore(int lower){
        double score1 = probBasedPhred.calculateScore(this.forwardHit.misMatch, this.forwardHit.readQual, lower);
        double score2 = probBasedPhred.calculateScore(this.reverseHit.misMatch, this.reverseHit.readQual, lower);
        this.probScore = score1 * score2;
    }
    private void calcAvgPhred(){
        this.avgPhred = calcAvgPhred.calcAvgPhred(this.forwardHit.readQual, this.reverseHit.readQual);
    }
    public String retClone (){
        String clone = getCloneName(this.forwardHit.readName);
        return clone;
    }
    private String getCloneName(String readName){
        String clone;
        String[] nameSplit = readName.split("[/_]");
        clone = nameSplit[0];
        return clone;
    }
    public String divetString(Integer perfCon){
        String fOrientStr = returnOrient(this.forwardHit.orient);
        String rOrientStr = returnOrient(this.reverseHit.orient);
        int totalEdit = this.forwardHit.eddis + this.reverseHit.eddis;
        if(this.type == callEnum.MAXDIST){
            return "";
        }
        String event = returnEvent(this.type);
        String probS = String.format("%.20f", this.probScore);
        int hasperfscore = 0;
        
        // This is my change to the divet file that allows me to filter away discordants that may be perfectly concordant in other locations
        if(perfCon == 1){
            hasperfscore = 1;
        }
        
        StringBuilder retStr = new StringBuilder(getCloneName(this.forwardHit.readName));
        retStr.append("\t").append(this.forwardHit.chr).append("\t").append(this.forwardHit.chrStart);
        retStr.append("\t").append(this.forwardHit.chrEnd).append("\t").append(fOrientStr).append("\t");
        retStr.append(this.reverseHit.chrStart).append("\t").append(this.reverseHit.chrEnd).append("\t");
        retStr.append(rOrientStr).append("\t").append(event).append("\t").append(totalEdit).append("\t").append(this.avgPhred);
        retStr.append("\t").append(probS).append("\t").append(hasperfscore).append("\n");
        
        return retStr.toString();
    }
    private String returnOrient(char oc){
        if(oc == '+'){
            return "F";
        }else{
            return "R";
        }
    }
    private String returnEvent(callEnum type){
        switch(type){
            case INSERTION :
                return "insertion";
            case DELETION :
                return "deletion";
            case INSINV :
                return "insinv";
            case DELINV :
                return "delinv";
            case EVERSION :
                return "eversion";
            case INVERSION :
                return "inversion";
            default :
                return "ERROR";
        }
    }
}
