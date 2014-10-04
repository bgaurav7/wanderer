package com.gb.pro.nLocation;

import java.util.HashMap;

public class Suggester {

	DataAnalyser da;
	POI poi;
	
	public Suggester(DataAnalyser da, POI poi) {
		this.da = da;
		this.poi = poi;
	}
	
	public Loc_Tag sugg(double lat, double lng) {
		int nP = poi.nearestPOI(lat, lng);
		
        return searchSuggestions(nP);
        
    }
    
    private Loc_Tag searchSuggestions(int nP) {
        HashMap<Integer, Integer> map = da.getMap(nP);

        Loc_Tag next = null;
        double pro = -1;
        if(map != null) {
        	//Toast.makeText(m, "Reach: " + map.toString() + "", Toast.LENGTH_LONG).show();
    		
            for(int key : map.keySet()) {
            	Loc_Tag t = poi.keys.get(nP);
            	if(probability(nP, key) > pro) {
            		next = t;
            	}
            	//t.prob = probability(nP, key);
                //ltQ.add(t);
            }
        }
        
        return next;
    }
    
    private double probability(int i1, int i2) {
        double prob = 1 + da.getCount(i1, i2);
        prob /= da.getCount(i1) + da.getTokenCount(i1);
        return (prob);
    }
}
