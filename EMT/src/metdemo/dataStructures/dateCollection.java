/**
 * Class to efficiently organize many dates with low overhead of storage
 */
package metdemo.dataStructures;

import java.util.HashMap;

/**
 * Class to organize a collection of dates<P>
 * 
 * Optimized in the following manner:<BR>
 * Assume that each month has 31 days. (12*31 = 372)<BR>
 * have a 372 large int array to keep counts.<BR>
 * if a location is zero, it means we havent seen any thing there, else we have a count.
 * 
 * 
 * @author Shlomo Hershkop
 * June 15 2005
 *
 */
public class dateCollection {
    
    /* map years to integer collections, offset is the day of the year */
    HashMap yearIndex;
    
    
    public dateCollection(){
        yearIndex = new HashMap();
        
    }
    
    /**
     * clear the data structure
     *
     */
    public void clear(){
        yearIndex.clear();
    }
    
    public void addDate(String dateToAdd){
        
        int datesInts[];
        //extract year
        String year = dateToAdd.substring(0,4);
        //check if year exists
        if(yearIndex.containsKey(year)){
            datesInts = (int [])yearIndex.get(year);
        }
        else
        {
            datesInts = new int[372];
            for(int i=0;i<372;i++){
                datesInts[i] =0;
            }
        }
        
        //modify correct int day with count
        int month = Integer.parseInt(dateToAdd.substring(5,7))-1;
        int day = Integer.parseInt(dateToAdd.substring(8));
        int offset = 31 * month + day-1;
        
        datesInts[offset]++;
        yearIndex.put(year,datesInts);
        
        
    }
    
    
    public boolean isSet(String dateString)
    {
        //extract year
        String year = dateString.substring(0,4);
        //check if year exists
        if(!yearIndex.containsKey(year)){
            return false;
        }
        int []datesInts = (int [])yearIndex.get(year);
        
        //modify correct int day with count
        int month = Integer.parseInt(dateString.substring(5,7))-1;
        int day = Integer.parseInt(dateString.substring(8));
        int offset = 31 * month + day-1;
        
       if(datesInts[offset] > 0){
           return true;
       }
       return false;
    }
    
    public int[] getYear(String year){
        if(yearIndex.containsKey(year))
            return (int[])yearIndex.get(year);
        
        int datesInts[] = new int[372];
        for(int i=0;i<372;i++){
            datesInts[i] =0;
        }
        
        return datesInts;
        
    }
    
    

}
