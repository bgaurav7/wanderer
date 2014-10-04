package com.gb.pro.nLocation;

import java.util.ArrayList;

public class POI {
	public ArrayList<Loc_Tag> keys;
	
	public POI(ArrayList<Loc_Tag> t) {
		keys = t;
	}
	
	public void addPOI(Loc_Tag t) {
		keys.add(t);
	}
	
	public boolean containsPOI(Loc_Tag b) {
        for(Loc_Tag t : keys) {
            if(t.isSame(b, 50))
            	return true;
        }
        return false;
    }
    
	public int nearestPOIIndex(Loc_Tag b) {
        int i = -1;
        double d = 100;
        for(Loc_Tag t : keys) {
            if(t.distance(b) <= d)
            	i = keys.indexOf(t);
        }
        return i;
    }
	
    public int nearestPOI(double lat, double lng) {
        Loc_Tag b = new Loc_Tag(lat, lng);
        
        int i = -1;
        double d = Double.MAX_VALUE;
        for(Loc_Tag t : keys) {
            if(t.distance(b) < d)
            	i = keys.indexOf(t);
        }
        return i;
    }
}
