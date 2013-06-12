package com.zzoranor.spelldirectory.util;

import android.content.Context;
import android.content.res.Configuration;

public class Utility {
	static public String trimSpellName(String s){
		int i = s.indexOf("\t");
		if(i != -1){
			s = s.substring(0, i);
		}
		return s;
	}
	
	static public int getScreenSizeCategory(Context c)
	{
		//Determine screen size
	    if ((c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {     
	        return Configuration.SCREENLAYOUT_SIZE_LARGE;
	    }
	    else if ((c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {     
	    	return Configuration.SCREENLAYOUT_SIZE_NORMAL;

	    } 
	    else if ((c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {     
	    	return Configuration.SCREENLAYOUT_SIZE_SMALL;
	    }
	    else {
	    	return Configuration.SCREENLAYOUT_SIZE_UNDEFINED;
	    }
	}
	
	static public String shortenLongClassNames(String name, int maxLength)
	{
		if(name.length() > maxLength && name.indexOf(" ") == -1)
		{
			return name.substring(0,maxLength);
		}
		return name;
	}
	
}
