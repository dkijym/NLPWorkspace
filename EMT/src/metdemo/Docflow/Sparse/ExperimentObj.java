/*
 * ExperimentObj.java
 *
 * Created on May 25, 2006, 12:29 PM
 *
 */

package metdemo.Docflow.Sparse;

import java.io.Serializable;
import org.apache.poi.poifs.storage.*;

/**
 *  cook the parsed data a little bit and call bloomfilter for training and
 *  testing
 *
 * @author Wei-Jen
 */
public class ExperimentObj implements Serializable{
    
    /** bloomfilter for ngram lower than 6 */
    CUBloomFilter bf1 = null;
    /** extra bloomfilter for ngram higher than 7 */
    CUBloomFilter[] bf2 = null;
    /** ngram */
    int gram = -1;
    /** a name for the bloomfilter */
    String key = null;
    /** for the extra bf array size */
    int extra = 8;
    
    /** if need to split the bloomfilter*/
    int spsize = 7;
    
    /** ignore zero bytes*/
    boolean ignorezero = false;//I put it global because I'm lazy
    
    /**
     *  use two bloomfilters if gram > 6
     *
     *  @param g size of gram
     *  @param k the section name
     *  @param b ignore zero bytes
     */
    
    public ExperimentObj(int g, String k, boolean b) {
        gram = g;
        key = k;
        ignorezero = b;
        try{
            //the bit size should be 22 if use 6 or 7-gram
            if(gram<=spsize)bf1 = new CUBloomFilter(key, 27, gram, null);
            
            //this is tricky.... use reasonable memory and increase accuracy
            else{
                int g1 = spsize;
                int g2 = gram - g1;
                bf1 = new CUBloomFilter(key, 26, g1, null);
                
                //use a list of smaller bf...
                extra = getextra();//length of extra array
                int bfsize = getbflen();//size of each bf in this array
                bf2 = new CUBloomFilter[extra];
                for(int i=0;i<extra;++i)
                    bf2[i] = new CUBloomFilter(key, bfsize, g2, null);
            }
        }
        catch(Exception e){
            System.out.println("Experiment:"+e);
        }
    }
    
    /*
     *  some extra things to add accuracy and use reasonable memory
     */
    
    /**
     *  get a proper size of bloomfilter
     *  to save some memory
     *
     *  @return the size of bloomfilter
     */
    public int getbflen(){
        switch(gram){
            case 7: return 12;
            case 8: return 17;
            case 9: return 21;
            case 10: return 22;
            case 11: return 24;
            case 12: return 24;
            default: return 0;
        }
        
    }
    
    /**
     *  get a proper size of bloomfilter
     *  to save some memory
     *
     *  @return the number of extra bloomfilters
     */
    public int getextra(){
        switch(gram){
            case 7: return 256;
            case 8: return 256;
            case 9: return 128;
            case 10: return 128;
            case 11: return 4;
            case 12: return 4;
//            case 11: return 64;
//            case 12: return 32;
            default: return 0;
        }
    }
            
    /**
     *  update the bloomfilter
     *
     *  @param data the parsed data
     *  @param other sometimes we may remove common ngrams from another bf
     */
    public void update(ListManagedBlock[] data, ExperimentObj other){
        if(gram<=spsize)lowGram(data, other);
        else highGram(data, other);
    }
    
    /**
     *  update the bloomfilter
     *  for 6-gram or smaller
     *
     *  @param data the parsed data
     *  @param other sometimes we may remove common ngrams from another bf
     */
    public void lowGram(ListManagedBlock[] data, ExperimentObj other){
        try{
            byte[] seq = new byte[gram];
            if(other == null){
                for(int i=0;i<data.length;++i){
                    byte[] block = data[i].getData();
                    for(int j=0;j<block.length-gram;++j){
                        boolean allzero = true;//check if 0 byte
                        for(int k=0;k<gram;++k){
                            seq[k] = block[j+k];
                            if(seq[k] != 0)allzero = false;
                        }
                        if(!allzero || !ignorezero)//don't add if all zero
                            bf1.insert(seq);
                    }
                }
            }
            //don't add ngram if it's in "other
            else{
                for(int i=0;i<data.length;++i){
                    byte[] block = data[i].getData();
                    for(int j=0;j<block.length-gram;++j){
                        boolean allzero = true;//check if 0 byte
                        for(int k=0;k<gram;++k){
                            seq[k] = block[j+k];
                            if(seq[k] != 0)allzero = false;
                        }
                        if(!allzero || !ignorezero)//don't add if all zero
                            if(!checkBF(seq, null, other))
                                bf1.insert(seq);
                    }
                }
            }
        }
        catch(Exception e){
            System.out.println("exp.lowGram:"+e);
        }
    }
    
    /**
     *  update the bloomfilter
     *  for 7-gram or bigger
     *
     *  @param data the parsed data
     *  @param other sometimes we may remove common ngrams from another bf
     */
    public void highGram(ListManagedBlock[] data, ExperimentObj other){
         try{
             int g1 = spsize;
             int g2 = gram - g1;
             byte[] seq1 = new byte[g1];
             byte[] seq2 = new byte[g2];
             if(other == null){
                 for(int i=0;i<data.length;++i){
                    byte[] block = data[i].getData();
                    for(int j=0;j<block.length-gram;++j){
                        boolean allzero = true;//check if 0 byte
                        
                        for(int k=0;k<g1;++k){
                            seq1[k] = block[j+k];
                            if(seq1[k] != 0)allzero = false;
                        }

                        for(int k=0;k<g2;++k){
                            seq2[k] = block[j+g1+k];
                            if(seq2[k] != 0)allzero = false;
                        }
                        
                        if(!allzero || !ignorezero){//don't add if all zero
                            int index = ((int)seq1[0]+128) % extra;
                            bf1.insert(seq1);
                            bf2[index].insert(seq2);
                        }
                    }
                 }
             }
             
             //don't add ngram if it's in "other"
            else{
                 for(int i=0;i<data.length;++i){
                    byte[] block = data[i].getData();
                    for(int j=0;j<block.length-gram;++j){
                         boolean allzero = true;//check if 0 byte
                         
                        for(int k=0;k<g1;++k){
                            seq1[k] = block[j+k];
                            if(seq1[k] != 0)allzero = false;
                        }

                        for(int k=0;k<g2;++k){
                            seq2[k] = block[j+g1+k];
                            if(seq2[k] != 0)allzero = false;
                        }
                        
                        if(!allzero || !ignorezero){//don't add if all zero
                            if(!checkBF(seq1, seq2, other)){
                                int index = ((int)seq1[0]+128) % extra;
                                bf1.insert(seq1);
                                bf2[index].insert(seq2);
                            }
                        }
                    }
                 }
            }
            
        }
        catch(Exception e){
            System.out.println("exp.highGram:"+e);
        }
    }
    
    /**
     *  check how many n-gram exist in the current bloombilter
     *
     *  @param data parsed data
     *  @return the number of matching ngrams and total length
     */
    public double[] check(ListManagedBlock[] data){
        if(gram<=spsize)return checkLowGram(data);
        else return checkHighGram(data);
    }
    
    /**
     *  check how many n-gram exist in the current bloombilter
     *  for 6-gram or smaller
     *
     *  @param data parsed data
     *  @return the number of matching ngrams and total length
     */
    public double[] checkLowGram(ListManagedBlock[] data){
        double rate = 0;
        double len = 0;
        try{
            byte[] seq = new byte[gram];
            for(int i=0;i<data.length;++i){
                byte[] block = data[i].getData();
                for(int j=0;j<block.length-gram;++j){
                    boolean allzero = true;//check if 0 byte
                    
                    for(int k=0;k<gram;++k){
                        seq[k] = block[j+k];
                        if(seq[k] != 0)allzero = false;
                    }
                    if(!allzero || !ignorezero){//don't add if all zero
                        if(bf1.check(seq))++rate;
                        ++len;
                    }
                }
            }
        }
        catch(Exception e){
            System.out.println("exp.checklowGram:"+e);
            return null;
        }
        if(len<=0)return null;
        return new double[]{rate, len};
    }
    
    /**
     *  check how many n-gram exist in the current bloombilter
     *  for 7-gram or bigger
     *
     *  @param data parsed data
     *  @return the number of matching ngrams and total length
     */
    public double[] checkHighGram(ListManagedBlock[] data){
        double rate = 0;
        double len = 0;
         try{
            int g1 = spsize;
            int g2 = gram - g1;
            byte[] seq1 = new byte[g1];
            byte[] seq2 = new byte[g2];
            for(int i=0;i<data.length;++i){
                byte[] block = data[i].getData();
                for(int j=0;j<block.length-gram;++j){
                    boolean allzero = true;//check if 0 byte
                    
                    for(int k=0;k<g1;++k){
                        seq1[k] = block[j+k];
                        if(seq1[k] != 0)allzero = false;
                    }
                    bf1.insert(seq1);

                    for(int k=0;k<g2;++k){
                        seq2[k] = block[j+g1+k];
                        if(seq2[k] != 0)allzero = false;
                    }

                    if(!allzero || !ignorezero){//don't add if all zero
                        int index = ((int)seq1[0]+128) % extra;
                        if(bf1.check(seq1)
                            && bf2[index].check(seq2))
                            ++rate;
                        ++len;
                    }
                }
            }
        }
        catch(Exception e){
            System.out.println("exp.checkhighGram:"+e);
            return null;
        }
        if(len<=0)return null;
        return new double[]{rate, len};
    }
    
    /**
     *  check the size of used bloomfilters
     */
    public void bfsize(){
        System.out.println("bf used size");
        if(bf1!=null)
            System.out.println(bf1.getUsedSize());
//            System.out.println("bf1:"+bf1.getUsedSize()+" "+bf1.getSize());
        if(bf2!=null){
            double avg = 0; 
            double std = 0;
            System.out.println("total size:"+bf2[0].getSize());
            for(int i=0;i<(int)extra;++i){
                avg += bf2[i].getUsedSize();
            }
            avg /= extra;
            for(int i=0;i<(int)extra;++i){
                std += ((avg - bf2[i].getUsedSize()) * (avg - bf2[i].getUsedSize()));
            }
            std = Math.sqrt(std/extra);
            System.out.println("average:"+avg +" std:"+std);
//            System.out.println("bf2["+i+"]:"+bf2[i].getUsedSize()+" "+bf2[i].getSize());
        }
    }
    
    /**
     *  return the size of used bloomfilters
     */
    public int getbfsize(){
        if(bf1!=null)
            return bf1.getUsedSize();
        if(bf2!=null){
            double avg = 0; 
            double std = 0;
            for(int i=0;i<(int)extra;++i){
                avg += bf2[i].getUsedSize();
            }
            avg /= extra;
            for(int i=0;i<(int)extra;++i){
                std += ((avg - bf2[i].getUsedSize()) * (avg - bf2[i].getUsedSize()));
            }
            std = Math.sqrt(std/extra);
            return (int)Math.round(avg);
        }
        return 0;
    }
    
    public void bffullsize(){
        System.out.println("bf full size");
        if(bf1!=null)
            System.out.println(bf1.getSize());
        if(bf2!=null)
            System.out.println(bf2[0].getSize());
    }
    
    /***************************************************************/
    
    /**
     *  update the bloomfilter
     *
     *  @param data the parsed data
     *  @param other sometimes we may remove common ngrams from another bf
     */
    public void update(byte[] data, ExperimentObj other){
        if(gram<=spsize)lowGram(data, other);
        else highGram(data, other);
    }
    
    /**
     *  update the bloomfilter
     *  for 6-gram or smaller
     *
     *  @param data the parsed data
     *  @param other sometimes we may remove common ngrams from another bf
     */
    public void lowGram(byte[] data, ExperimentObj other){
        try{
            byte[] seq = new byte[gram];
            if(other == null){
                for(int i=0;i<data.length-gram;++i){   
                    boolean allzero = true;//check if 0 byte
                    for(int j=0;j<gram;++j){
                        seq[j] = data[i+j];
                    }
                    if(!allzero || !ignorezero){//don't add if all zero
                            //System.out.println("***");
                        bf1.insert(seq);
                            //System.out.println("*****");
                    }
                }
            }
            
            //don't add ngram if it's in "other"
            else{
                for(int i=0;i<data.length-gram;++i){   
                    boolean allzero = true;//check if 0 byte
                    
                    for(int j=0;j<gram;++j){
                        seq[j] = data[i+j];
                        if(seq[j] != 0)allzero = false;
                    }
                    
                    if(!allzero || !ignorezero){//don't add if all zero
                        if(!checkBF(seq, null, other))bf1.insert(seq);
                    }
                }
            }
        }
        catch(Exception e){
            System.out.println("exp.lowGramFull:"+e);
        }
        
    }
    
    /**
     *  update the bloomfilter
     *  for 7-gram or bigger
     *
     *  @param data the parsed data
     *  @param other sometimes we may remove common ngrams from another bf
     */
    public void highGram(byte[] data, ExperimentObj other){
         try{
             int g1 = spsize;
             int g2 = gram - g1;
             byte[] seq1 = new byte[g1];
             byte[] seq2 = new byte[g2];
             if(other == null){
                for(int i=0;i<data.length-gram;++i){ 
                    boolean allzero = true;//check if 0 byte
                    
                    for(int j=0;j<g1;++j){
                        seq1[j] = data[i+j];
                        if(seq1[j] != 0)allzero = false;
                    }
                    bf1.insert(seq1);

                    for(int j=0;j<g2;++j){
                        seq2[j] = data[i+g1+j];
                        if(seq2[j] != 0)allzero = false;
                    }

                    if(!allzero || !ignorezero){//don't add if all zero
                        int index = ((int)seq1[0]+128) % extra;
                        bf2[index].insert(seq2);
                    }
                }
             }
             
             //don't add ngram if it's in "other"
             else{
                 for(int i=0;i<data.length-gram;++i){ 
                     boolean allzero = true;//check if 0 byte
                     
                    for(int j=0;j<g1;++j){
                        seq1[j] = data[i+j];
                        if(seq1[j] != 0)allzero = false;
                    }

                    for(int j=0;j<g2;++j){
                        seq2[j] = data[i+g1+j];
                        if(seq1[j] != 0)allzero = false;
                    }

                     if(!allzero || !ignorezero){//don't add if all zero
                        if(!checkBF(seq1, seq2, other)){
                            int index = ((int)seq1[0]+128) % extra;
                            bf1.insert(seq1);
                            bf2[index].insert(seq2);
                        }
                     }
                }
             }
        }
        catch(Exception e){
            System.out.println("exp.highGramFull:"+e);
        }
    }
    
    /**
     *  check how many n-gram exist in the current bloombilter
     *
     *  @param data parsed data
     *  @return the number of matching ngrams and total length
     */
    public double[] check(byte[] data){
        if(gram<=spsize)return checkLowGram(data);
        else return checkHighGram(data);
    }
    
    /**
     *  check how many n-gram exist in the current bloombilter
     *  for 6-gram or smaller
     *
     *  @param data parsed data
     *  @return the number of matching ngrams and total length
     */
    public double[] checkLowGram(byte[] data){
        double rate = 0;
        try{
            byte[] seq = new byte[gram];
            for(int i=0;i<data.length-gram;++i){   
                boolean allzero = true;//check if 0 byte
                
                for(int j=0;j<gram;++j){
                    seq[j] = data[i+j];
                    if(seq[j] != 0)allzero = false;
                }
                
                if(!allzero || !ignorezero){//don't add if all zero
                    if(bf1.check(seq))
                        ++rate;
                }
            }
        }
        catch(Exception e){
            System.out.println("exp.checkLowGramFull:"+e);
            return null;
        }
        return new double[]{rate, (double)(data.length-gram)};
    }
    
    /**
     *  check how many n-gram exist in the current bloombilter
     *  for 7-gram or bigger
     *
     *  @param data parsed data
     *  @return the number of matching ngrams and total length
     */
    public double[] checkHighGram(byte[] data){
        double rate = 0;
         try{
            int g1 = spsize;
            int g2 = gram - g1;
            byte[] seq1 = new byte[g1];
            byte[] seq2 = new byte[g2];
            for(int i=0;i<data.length-gram;++i){ 
                boolean allzero = true;//check if 0 byte
                
                for(int j=0;j<g1;++j){
                    seq1[j] = data[i+j];
                    if(seq1[j] != 0)allzero = false;
                }

                for(int j=0;j<g2;++j){
                    seq2[j] = data[i+g1+j];
                    if(seq2[j] != 0)allzero = false;
                }
                
                if(!allzero || !ignorezero){//don't add if all zero
                    int index = ((int)seq1[0]+128) % extra;
                    if(bf1.check(seq1)
                        && bf2[index].check(seq2))
                        ++rate;
                }
            }
        }
        catch(Exception e){
            System.out.println("exp.checkHighGramFull:"+e);
            return null;
        }
        return new double[]{rate, (double)(data.length-gram)};
    }
    
    
    /**********************************************/
    
    /**
     *  check if the parsed n-gram exists in "other"
     *
     *  @param data1 the first half of n-gram
     *  @param data2 the second half of n-gram
     *  @param other the bloomfilter
     *  @return exists or not
     */
    public boolean checkBF(byte[] data1, byte[] data2, ExperimentObj other){
        if(gram<=spsize)return checkLowBF(data1, other);
        else return checkHighBF(data1, data2, other);
    }
    
    /**
     *  check if the parsed n-gram exists in "other"
     *  for 6-gram or smaller
     *
     *  @param data the n-gram
     *  @param other the bloomfilter
     *  @return exists or not
     */
    public boolean checkLowBF(byte[] data, ExperimentObj other){
        return other.bf1.check(data);
    }
    
    /**
     *  check if the parsed n-gram exists in "other"
     *  for 7-gram or bigger
     *
     *  @param data1 the first half of n-gram
     *  @param data2 the second half of n-gram
     *  @param other the bloomfilter
     *  @return exists or not
     */
    public boolean checkHighBF(byte[] data1, byte[] data2, ExperimentObj other){
        int index = ((int)data1[0]+128) % extra;
        if(other.bf1.check(data1)
            && other.bf2[index].check(data2))return true;
        else return false;
    }
    
    /**
     *  reset the bloomfilters
     *
     */
    public void reset(){
        if(bf1 != null){
            bf1.reset();
        }
        
        if(bf2 != null){
            for(int i=0;i<bf2.length;++i)bf2[i].reset();
        }
    }
    
    /**
     *  for the extra result... check if a byte sequence exist in the model
     *
     *  @param data the byte sequence to check
     *  @return exist or not
     */
    public boolean checkSingle(byte[] data){
        if(gram<=spsize)return checkBF(data, null, this);
        else{
            byte[] seq1 = new byte[spsize];
            byte[] seq2 = new byte[gram-spsize];
            System.arraycopy(data, 0, seq1, 0, spsize);
            System.arraycopy(data, spsize, seq2, 0, gram-spsize);
            return checkBF(seq1, seq2, this);
        }
    }

    public boolean bfcheck(byte[] b){
        return bf1.check(b);
    }
}
