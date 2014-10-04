package com.gb.pro.nLocation;

import java.util.ArrayList;
import java.util.HashMap;

public class DataAnalyser {

    private final HashMap<Integer, Integer> uniG;
    private final HashMap<Integer , HashMap<Integer, Integer>> biG;
    
    public DataAnalyser() {
        uniG = new HashMap<Integer, Integer>();
        biG = new HashMap<Integer , HashMap<Integer, Integer>>();
    }
    
    public void addLoc_Tag(ArrayList<Loc_Tag> t) {
    	if(t == null || t.size() <= 0) return;
    	
        Loc_Tag prev;
        Loc_Tag curr = null;
        
        for(Loc_Tag Loc_Tag : t) {
            prev = curr;
            curr = Loc_Tag;
            
            /**
             *  Insert uni-grams in Hashmap
            **/
            if(prev != null) {
                int v = 0;
                if(uniG.containsKey(curr.index)) {
                    v = uniG.get(curr.index);
                }
                uniG.put(curr.index, v);
            }
            
            /**
             *  Insert bi-grams in Hashmap
            **/
            if(prev != null && curr != null)  {
                HashMap <Integer, Integer> tmp;
                if(biG.containsKey(prev.index)) {
                    tmp = biG.get(prev.index);
                    if(tmp.containsKey(curr.index)) {
                        tmp.put(curr.index, tmp.get(curr.index) + 1);
                    } else {
                        tmp.put(curr.index, 1);
                    }
                } else {
                    tmp = new HashMap<Integer, Integer>();
                    tmp.put(curr.index, 1);
                    biG.put(prev.index, tmp);
                }
            }
        }
        
        /**
         *  Add last token that is not added
        **/
        prev = t.get(t.size() - 1);
        int v = 0;
        if(uniG.containsKey(prev.index))
            v = uniG.get(prev.index);
        uniG.put(prev.index, v + 1);
    }
    
    public HashMap<Integer, Integer> getMap(int i) {
        if(i >= 0) {
            if(biG.containsKey(i))
                return biG.get(i);
        }
        return null;
    }
    
    public int getCount(int i) {
        if(i >= 0) {
            if(uniG.containsKey(i))
                return uniG.get(i);
        }
        
        return 0;
    }
    
    public int getCount(int i1, int i2) {
        HashMap<Integer , Integer> tmp = getMap(i1);
        if(tmp != null && tmp.containsKey(i2))
            return tmp.get(i2);
        return 0;
    }
    
    public int getTokenCount() {
        return uniG.size();
    }
    
    public int getTokenCount(int i) {
        int c = 0;
        
        for(Integer tmp : biG.get(i).values()) {
            c += tmp;
        }
        
        return c;
    }
}
