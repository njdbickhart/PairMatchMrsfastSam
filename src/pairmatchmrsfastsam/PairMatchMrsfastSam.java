/*
 * This Program is designed to take sam output from Mrsfast, identify one end anchors,
 * matches and other interesting features and then output broken fastqs, divets and the like.
 * Improvement over splitread: uses discordant read pairs as anchors for finding split reads
 */
package pairmatchmrsfastsam;

import dataStructs.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bickhart
 */
public class PairMatchMrsfastSam {
    protected static Charset charset = Charset.forName("UTF-8");
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        commandLineParser cmd = new commandLineParser(args);
        
        if(!cmd.minInfo){
            System.out.println("Command line argument error!");
            System.exit(0);
        }
        // read in sam files; generate readname map of hits
        Path sam1 = Paths.get(cmd.inputSam1);
        Path sam2 = Paths.get(cmd.inputSam2);
        readNameHits hitStorage = new readNameHits();
        
        try {
            String line; 
            
            try (BufferedReader inSam1 = Files.newBufferedReader(sam1, charset)) {
                while((line = inSam1.readLine()) != null){
                    line = line.replaceAll("\n", "");
                    hits sample = new hits(line);
                    hitStorage.addToPile(sample);
                }
            }
            
            try (BufferedReader inSam2 = Files.newBufferedReader(sam2, charset)){
                while((line = inSam2.readLine()) != null){
                    line = line.replace("\n", "");
                    hits sample = new hits(line);
                    hitStorage.addToPile(sample);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PairMatchMrsfastSam.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Cycle through readnamehits and associate reads with appropriate containers (matches) discordants, oeas
        binReadsByType binner = new binReadsByType(hitStorage, cmd);
        ArrayList<matches> matches = binner.binReadsRetMatches();
        
        // Output to appropriate files
        Path singles = Paths.get(cmd.outputBase + ".single.txt");
        Path divets = Paths.get(cmd.outputBase + ".divet.vh");
        Path filterDisc = Paths.get(cmd.outputBase + ".concordant.vh");
        
        try(BufferedWriter singWriter = Files.newBufferedWriter(singles, charset)){
            ArrayList<oeaReads> reads = binner.returnOEAs();
            for(oeaReads r : reads){
                for(hits anc : r.anchor){
                    String samLine = anc.returnSamString();
                    singWriter.write(samLine);
                }
            }
        } catch(IOException ex){
            Logger.getLogger(PairMatchMrsfastSam.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        HashMap<String, Integer> concords = new HashMap<>();
        ArrayList<discords> tempd = binner.returnDiscords();
        for(discords d : tempd){
            String readName = d.retClone();
            if(!concords.containsKey(readName)){
                    concords.put(readName, 0);
            }
            if(d.hasPerfectConcordant){
                concords.put(readName, 1);
            }
        }
        
        try(BufferedWriter divWriter = Files.newBufferedWriter(divets, charset)){
            ArrayList<discords> discs = binner.returnDiscords();
            for(discords d : discs){
                String readName = d.retClone();
                String samLine = d.divetString(concords.get(readName));
                divWriter.write(samLine);
            }
        }catch(IOException ex){
            Logger.getLogger(PairMatchMrsfastSam.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Printing a file containing discordant read names that have perfect concordant matches
        try(BufferedWriter conWriter = Files.newBufferedWriter(filterDisc, charset)){
            Set<String> readName = concords.keySet();
            List<String> readList = asSortedList(readName);
            for(String r : readList){
                if(concords.get(r) == 1){
                    conWriter.write(r + "\t" + concords.get(r) + "\n");
                }
            }
        }catch(IOException ex){
            Logger.getLogger(PairMatchMrsfastSam.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Pull reads that do not map out of the fastq files and then break them into splits
        // Using threads slowed it down to a crawl!
        HashMap<String, Short> unmappedReads = new HashMap<>();
        HashMap<String, Integer> anchorClones = new HashMap<>();
        for (oeaReads r : binner.returnOEAs()){
            unmappedReads.put(r.unmappedReads, (short)1);
            anchorClones.put(getCloneName(r.anchor.get(0).readName), getCloneNum(r.anchor.get(0).readName));
        }
        try {
            FileOutputStream file = new FileOutputStream(cmd.outputBase + ".split.fq", false);
            file.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PairMatchMrsfastSam.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex){
            Logger.getLogger(PairMatchMrsfastSam.class.getName()).log(Level.SEVERE, null, ex);
        }
        outputBreakFastq fq1Worker = new outputBreakFastq(cmd.inputFq1, cmd.outputBase + ".split.fq", unmappedReads, anchorClones);
        outputBreakFastq fq2Worker = new outputBreakFastq(cmd.inputFq2, cmd.outputBase + ".split.fq", unmappedReads, anchorClones);
        
    }
    public static
        <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
          List<T> list = new ArrayList<>(c);
          java.util.Collections.sort(list);
          return list;
    }
    private static String getCloneName(String readName){
        String clone;
        String[] nameSplit = readName.split("[/_]");
        clone = nameSplit[0];
        return clone;
    }
    private static int getCloneNum(String readName){
        int num;
        String[] nameSplit = readName.split("[/_]");
        num = Short.parseShort(nameSplit[1]);
        return num;
    }
}
