/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pairmatchmrsfastsam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bickhart
 */
public class outputBreakFastq{
    private String fqFile;
    private String outputFile;
    private HashMap<String, Short> unmap;
    private HashMap<String, Integer> anchor;
    private Charset charset = Charset.forName("UTF-8");

    public outputBreakFastq(String fqFile, String outputFile, HashMap<String, Short> unmap, HashMap<String, Integer> anchor){
        this.fqFile = fqFile;
        this.outputFile = outputFile;
        this.unmap = unmap;
        this.anchor = anchor;
        run();
    }
    

    private void run() {
        try(BufferedReader fq = Files.newBufferedReader(Paths.get(fqFile), charset)){
            String h, s, p, q; // Line variables for the fastq
            
            try(BufferedWriter out = Files.newBufferedWriter(Paths.get(outputFile), charset, StandardOpenOption.APPEND)){
                while((h = fq.readLine()) != null){
                    s = fq.readLine();
                    p = fq.readLine();
                    q = fq.readLine();
                    h = h.replaceAll("@", "");
                    
                    h = h.replaceAll("_", "/");
                    h = h.trim();
                    String clone = getCloneName(h);
                    int cNum = getCloneNum(h);
                                        
                    if(this.anchor.containsKey(clone)){
                        if(this.unmap.containsKey(h)){
                            // This is the unmapped read that needs to be split
                            String[] seq = readBreaker(s);
                            String[] qual = readBreaker(q);
                            String h1 = "@" + clone + "/" + cNum + "1\n";
                            String h2 = "@" + clone + "/" + cNum + "2\n";
                            out.write(h1 + seq[0] + "\n" + p + "\n" + qual[0] + "\n");
                            out.write(h2 + seq[1] + "\n" + p + "\n" + qual[1] + "\n");
                        }else{
                            // This is the anchor read
                            
                        }                        
                    }
                }
            }
        }catch(IOException ex){
            Logger.getLogger(PairMatchMrsfastSam.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String[] readBreaker (String s){
        String[] split = new String[2];
        int len = s.length();
        if(len % 2 == 0){
            split[0] = s.substring(0, len /2);
            split[1] = s.substring(len / 2, len);
        }else{
            len--;
            split[0] = s.substring(0, len /2);
            split[1] = s.substring(len / 2, len);
        }
        return split;
    }
    private String getCloneName(String readName){
        String clone;
        String[] nameSplit = readName.split("[/_]");
        clone = nameSplit[0];
        return clone;
    }
    private int getCloneNum(String readName){
        int num;
        String[] nameSplit = readName.split("[/_]");
        num = Short.parseShort(nameSplit[1]);
        return num;
    }
}
