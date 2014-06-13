/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructs;

import java.util.ArrayList;

/**
 *
 * @author bickhart
 */
public class matches {
    public hits forwardHits;
    public hits reverseHits;
    public int totalEdit;
    public callEnum call;
    
    public matches(hits forward, hits reverse, int totaled){
        this.forwardHits = forward;
        this.reverseHits = reverse;
        this.totalEdit = totaled;
        this.call = callEnum.CONCORDANT;
    }
}
