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
public class oeaReads {
    public ArrayList<hits> anchor;
    public String unmappedReads;
    
    public oeaReads(ArrayList<hits> anchor){
        this.anchor = anchor;
        determineUnmappedReads();
    }

    private void determineUnmappedReads(){
            String cloneName = getCloneName(anchor.get(0).readName);
            short cloneNum;
            if(anchor.get(0).cloneNum == 1){
                cloneNum = 2;
            }else{
                cloneNum = 1;
            }
            String fullName = cloneName + "/" + cloneNum;
            this.unmappedReads = fullName;
    }
    private String getCloneName(String readName){
        String clone;
        String[] nameSplit = readName.split("[/_]");
        clone = nameSplit[0];
        return clone;
    }
    
}
