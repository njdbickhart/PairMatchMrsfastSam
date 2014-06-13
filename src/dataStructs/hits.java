/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructs;

/**
 *
 * @author bickhart
 */
public class hits {
    public String readName;
    public String readQual;
    public String misMatch; //Taken from mapping tag
    public short cloneNum; // whether this is the first read or the second in the insert
    public int chrStart;
    public int chrEnd;
    public int readSize;
    public char orient; //0 is forward (+) and 16 is reverse (-)
    public int eddis; //Taken from edit tag 
    public String editTag; //NM tag from end of Sam record
    public String mappingTag; //MD tag from end of Sam record
    public String chr;
    public String seq;
    public String cigar;
    
    public hits(String samline){
        String[] segs = samline.split("\t");
        this.readName = segs[0];
        this.readQual = segs[10];
        this.chr = segs[2];
        this.cigar = segs[5];
        this.seq = segs[9];
        this.readSize = segs[9].length();
        this.chrStart = Integer.parseInt(segs[3]);
        this.chrEnd = this.chrStart + this.readSize;
        if(segs[1].equals("0")){
            this.orient = '+';
        }else{
            this.orient = '-';
        }
        
        String[] ediTTag = segs[11].split(":");
        this.eddis = Integer.parseInt(ediTTag[2]);
        
        String[] mappinGTag = segs[12].split(":");
        this.misMatch = mappinGTag[2];
        
        this.cloneNum = getCloneNum(readName);
        this.editTag = segs[11];
        this.mappingTag = segs[12];
    }
    private short getCloneNum(String readName){
        short num;
        String[] nameSplit = readName.split("[/_]");
        num = Short.parseShort(nameSplit[nameSplit.length -1]);
        return num;
    }
    public String returnSamString(){
        StringBuilder retStr = new StringBuilder(this.readName);
        if(this.orient == '+'){
            retStr.append("\t").append("0");
        }else{
            retStr.append("\t").append("16");
        }
        retStr.append("\t").append(this.chr).append("\t").append(this.chrStart).append("\t");
        retStr.append("255").append("\t").append(this.cigar).append("\t").append("*");
        retStr.append("\t").append("0").append("\t").append("0").append("\t").append(this.seq);
        retStr.append("\t").append(this.readQual).append("\t").append(this.editTag).append("\t");
        retStr.append(this.mappingTag).append("\n");
        return retStr.toString();
    }
}
