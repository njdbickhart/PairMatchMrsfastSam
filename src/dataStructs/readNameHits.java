/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author bickhart
 */
public class readNameHits {
    public HashMap<String, ArrayList<hits>> storage;
    
    public readNameHits(){
        this.storage = new HashMap<>();
    }
    
    public void addToPile(hits hit){
        String readName = getCloneName(hit.readName);
        if(this.storage.containsKey(readName)){
            this.storage.get(readName).add(hit);
        }else{
            this.storage.put(readName, new ArrayList<hits>());
            this.storage.get(readName).add(hit);
        }
    }
    public String getCloneName(String readName){
        String clone;
        String[] nameSplit = readName.split("[/_]");
        clone = nameSplit[0];
        return clone;
    }
    
}
