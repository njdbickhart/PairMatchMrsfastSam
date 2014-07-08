/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pairmatchmrsfastsam;

import dataStructs.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author bickhart
 */
public class binReadsByType {
    private HashMap<String, ArrayList<hits>> storage;
    private commandLineParser cmd;
    private ArrayList<discords> discords;
    private ArrayList<oeaReads> oeaReads;
    private int lower;
    private int upper;
    private int cutoff;
    
    public binReadsByType(readNameHits unsortedHits, commandLineParser cmd){
        this.storage = unsortedHits.storage;
        this.cmd = cmd;
        this.discords = new ArrayList<>();
        this.oeaReads = new ArrayList<>();
    }
    
    public ArrayList<matches> binReadsRetMatches(){
        this.lower = this.cmd.lowEnd;
        this.upper = this.cmd.upperEnd;
        this.cutoff = this.cmd.maxLen;
        ArrayList<matches> matches = new ArrayList<>();
        
        Set<String> cloneNames = this.storage.keySet();
        for(String clone : cloneNames){
            // Assign reads into temporary forward and reverse holders based on clone number
            // forward = 1, reverse = 2
            HashMap<String, ArrayList<hits>> forward = new HashMap<>();
            HashMap<String, ArrayList<hits>> reverse = new HashMap<>();
            
            boolean fContains = false;
            boolean rContains = false;
            
            for(hits h : this.storage.get(clone)){
                if(h.cloneNum == 1){
                    fContains = true;
                    if(!forward.containsKey(h.chr)){
                        forward.put(h.chr, new ArrayList<hits>());
                        forward.get(h.chr).add(h);
                    }else{
                        forward.get(h.chr).add(h);
                    }
                }else{
                    rContains = true;
                    if(!reverse.containsKey(h.chr)){
                        reverse.put(h.chr, new ArrayList<hits>());
                        reverse.get(h.chr).add(h);
                    }else{
                        reverse.get(h.chr).add(h);
                    }
                }
            }
            
            //Check to see if there are any one end anchors right off the bat
            if(!fContains || !rContains){
                if(fContains){
                    addToOEAs(forward);
                }else if(rContains){
                    addToOEAs(reverse);
                }
                continue;
            }
            
            Set<String> fChr = forward.keySet();
            Set<String> rChr = reverse.keySet();
            Set<String> allChr;
            allChr = new HashSet<>(fChr);
            allChr.addAll(rChr);
            
            Set<String> intersectChr = intersectSets(fChr, rChr);
                      
            //<editor-fold defaultstate="collapsed" desc="previous code logic">
            /*//Check for concordants
             * boolean isConcordant = false;
             * for (String chr : intersectChr){
             * for(hits fh : forward.get(chr)){
             * hits bestHit = rankBestAlign(reverse.get(chr), fh.chrStart, fh.chrEnd, fh.orient);
             * if(bestHit != null){
             * isConcordant = true;
             * matches.add(new matches(fh, bestHit, fh.eddis + bestHit.eddis));
             * }
             * }
             * }
             *
             * if(isConcordant){
             * continue; // A concordant map best represents the insert, so we'll go to the next one
             * }*/
            //</editor-fold>
            
            //Now, classify discordants
            boolean hasPerfectConcordant = false;
            for (String chr : allChr){
                if(forward.containsKey(chr) && !(reverse.containsKey(chr))){
                    // Translocation
                }
                if(!forward.containsKey(chr) && reverse.containsKey(chr)){
                    // Translocation
                }
                if(forward.containsKey(chr) && reverse.containsKey(chr)){
                    for(hits fwd : forward.get(chr)){
                        for(hits rev : reverse.get(chr)){
                            int span = max4(fwd.chrStart, fwd.chrEnd, rev.chrStart, rev.chrEnd) - min4(fwd.chrStart, fwd.chrEnd, rev.chrStart, rev.chrEnd) + 1;
                            if(span > this.cutoff){
                                // Maxdist
                                addToDiscords(fwd, rev, callEnum.MAXDIST, span, hasPerfectConcordant);
                            }else if(span < this.lower){
                                /*
                                TODO: I need to add eversion checking here!
                                */
                                if(fwd.orient != rev.orient){
                                    //Insertion
                                    addToDiscords(fwd, rev, callEnum.INSERTION, span, hasPerfectConcordant);
                                }else if(fwd.orient == rev.orient){
                                    //Insinv
                                    addToDiscords(fwd, rev, callEnum.INSINV, span, hasPerfectConcordant);
                                }
                            }else if (span > this.upper){
                                if (((fwd.orient == '+' && rev.orient == '-') && fwd.chrStart < rev.chrStart)
                                        || ((fwd.orient == '-' && rev.orient == '+') && fwd.chrStart > rev.chrStart)){
                                    // Deletion
                                    addToDiscords(fwd, rev, callEnum.DELETION, span, hasPerfectConcordant);
                                }else if (fwd.orient == rev.orient){
                                    // Delinv
                                    addToDiscords(fwd, rev, callEnum.DELINV, span, hasPerfectConcordant);
                                }else{
                                    addToDiscords(fwd, rev, callEnum.EVERSION, span, hasPerfectConcordant);
                                }
                            }else{
                                if (fwd.orient == rev.orient){
                                    // Inversion
                                    addToDiscords(fwd, rev, callEnum.INVERSION, span, hasPerfectConcordant);
                                }else if(((fwd.orient == '+' &&  rev.orient == '-')) 
                                    && (fwd.chrStart > rev.chrStart)){
                                    // Eversion one
                                    addToDiscords(fwd, rev, callEnum.EVERSION, span, hasPerfectConcordant);                                
                                }else if (fwd.orient == '-' && rev.orient == '+' 
                                        && rev.chrStart > fwd.chrStart){
                                    // Eversion two
                                    addToDiscords(fwd, rev, callEnum.EVERSION, span, hasPerfectConcordant);
                                }else{
                                    // Concordant
                                    matches.add(new matches(fwd, rev, fwd.eddis + rev.eddis));
                                    if(fwd.eddis + rev.eddis == 0){
                                        hasPerfectConcordant = true;
                                    }
                                }
                            }                            
                        }
                    }
                }
            }
                      
            // Done classifying this clonename
        }
        
        return matches;   
    }
    public ArrayList<discords> returnDiscords(){
        return this.discords;
    }
    public ArrayList<oeaReads> returnOEAs(){
        return this.oeaReads;
    }
    private void addToOEAs(HashMap<String, ArrayList<hits>> hits){
        /*int bestEdit = 100;
         *hits bestHit = null;*/
         Set<String> chr = hits.keySet();
        
        //<editor-fold defaultstate="collapsed" desc="previous code logic">
        /*for(String c : chr){
         * hits currentHit = rankBestAlign(hits.get(c));
         * if(bestHit != null){
         * if(bestHit.eddis == 0 && currentHit.eddis == 0){
         * return; // Two chromosomes have perfect matches; this will be difficult to resolve
         * }
         * }
         * if(currentHit.eddis < bestEdit){
         * bestEdit = currentHit.eddis;
         * bestHit = currentHit;
         * }
         * }*/
        //</editor-fold>
        
         ArrayList<hits> store = new ArrayList<>();
         for(String c : chr){
             store.addAll(hits.get(c));
         }
        this.oeaReads.add(new oeaReads(store));
    }
    private void addToDiscords(hits forward, hits reverse, callEnum call, int span, boolean concord){
        this.discords.add(new discords(forward, reverse, call, this.lower, span, concord));
    }
    private hits rankBestAlign(ArrayList<hits> hits){
        hits bestHit = null;
        boolean started = false;
        for(hits h: hits){
            if(!started){
                bestHit = h;
                started = true;
            }else{
                if(bestHit.eddis > h.eddis){
                    bestHit = h;
                }
            }
        }
        return bestHit;
    }
    private hits rankBestAlign(ArrayList<hits> hits, int start, int end){
        hits bestHit = null;
        boolean started = false;
        for(hits h: hits){
            if(!started && 
                    (overlap(start + this.upper, end + this.upper, h.chrStart, h.chrEnd) >= 0 ||
                    overlap(start - this.upper, end - this.upper, h.chrStart, h.chrEnd) >= 0)){
                bestHit = h;
                started = true;
            }else{
                if(bestHit.eddis > h.eddis && 
                        (overlap(start + this.upper, end + this.upper, h.chrStart, h.chrEnd) >= 0 ||
                        overlap(start - this.upper, end - this.upper, h.chrStart, h.chrEnd) >= 0)){
                    bestHit = h;
                }
            }
        }
        return bestHit;
    }
    
    private hits rankBestAlign(ArrayList<hits> hits, int start, int end, char orient){
        
        hits bestHit = null;
        boolean started = false;
        for(hits h: hits){
            if(!started &&
                    orient != h.orient &&
                    (overlap(start + this.upper, end + this.upper, h.chrStart, h.chrEnd) >= 0 ||
                    overlap(start - this.upper, end - this.upper, h.chrStart, h.chrEnd) >= 0)){
                bestHit = h;
                started = true;
            }else if(started){
                if(bestHit.eddis > h.eddis && 
                        orient != h.orient &&
                        (overlap(start + this.upper, end + this.upper, h.chrStart, h.chrEnd) >= 0 ||
                        overlap(start - this.upper, end - this.upper, h.chrStart, h.chrEnd) >= 0)){
                    bestHit = h;
                }
            }
        }
        
        return bestHit;
    }
    private Set<String> intersectSets (Set<String> set1, Set<String> set2) {
            Set<String> a;
            Set<String> b;
            Set<String> c = new HashSet<>();
            if (set1.size() <= set2.size()) {
                a = set1;
                b = set2;           
            } else {
                a = set2;
                b = set1;
            }
            for (String e : a) {
                if (b.contains(e)) {
                    c.add(e);
                }           
            }
            return c;
    }
    private Set<String> excludeSets (Set<String> set1, Set<String> set2){
        Set<String> c = new HashSet<>();
        for (String e : set1){
            if(!set2.contains(e)){
                c.add(e);
            }
        }
        return c;
    }
    
    private int least(int a, int b){
        if(a < b) {
            return a;
        }
        else {
            return b;
        }
    }
    private int most(int a, int b){
        if(a > b) {
            return a;
        }
        else {
            return b;
        }
    }
    private int overlap(int s1, int e1, int s2, int e2){
        return (least(e1, e2) - most(s1, s2));
    }
    private int max4(int a, int b, int c, int d){
	if (a >= b && a >= c && a >= d) {
            return a;
        }
	if (b >= a && b >= c && b >= d) {
            return b;
        }
	if (c >= a && c >= b && c >= d) {
            return c;
        }
	if (d >= a && d >= b && d >= c) {
            return d;
        }
        return 0;
    }

    private int min4(int a, int b, int c, int d){
        if (a <= b && a <= c && a <= d) {
            return a;
        }
        if (b <= a && b <= c && b <= d) {
            return b;
        }
        if (c <= a && c <= b && c <= d) {
            return c;
        }
        if (d <= a && d <= b && d <= c) {
            return d;
        }
        return 0;
    }
    
}
