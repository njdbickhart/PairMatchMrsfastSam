/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pairmatchmrsfastsam;

/**
 *
 * @author bickhart
 */
public class commandLineParser {
    public String inputSam1 = null;
    public String inputSam2 = null;
    public String inputFq1 = null;
    public String inputFq2 = null;
    public String outputBase = null;
    public int maxLen = -1;
    public int lowEnd = -1;
    public int upperEnd = -1;
    public boolean minInfo = false;
    
    public commandLineParser(String[] args){
        for(int i = 0; i < args.length; i++){
            try{
                switch(args[i]){
                    case "-i1":
                        this.inputSam1 = args[i + 1]; break;
                    case "-i2":
                        this.inputSam2 = args[i + 1]; break;
                    case "-f1":
                        this.inputFq1 = args[i + 1]; break;
                    case "-f2":
                        this.inputFq2 = args[i + 1]; break;
                    case "-o":
                        this.outputBase = args[i + 1]; break;
                    case "-m":
                        this.maxLen = Integer.parseInt(args[i+1]); break;
                    case "-u":
                        this.upperEnd = Integer.parseInt(args[i + 1]); break;
                    case "-l":
                        this.lowEnd = Integer.parseInt(args[i+1]); break;
                }
            }catch(NumberFormatException ex){
                ex.printStackTrace();
            }
        }
        if(inputSam1 == null || inputSam2 == null || outputBase == null || maxLen == -1 || lowEnd == -1 || upperEnd == -1){
            minInfo = false;
        }else{
            minInfo = true;
        }
    }
}
