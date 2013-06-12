package com.zzoranor.spelldirectory.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Classes {
	
	private static ArrayList<String> classes = null;
	private static HashMap<String, Integer> classMap = null;
	private static int nextClassIdx = -1;

	// Singleton stuff.
	private static Classes instance;
	public static Classes getInstance()
	{
		if(instance == null)
		{
			classes = new ArrayList<String>();
			classMap = new HashMap<String, Integer>();
			instance = new Classes();
		}
		return instance;
	}
	
	private Classes(){}
	
	public int numClasses(){
		return classes.size();
	}
	
	public int addClass(String toAdd,int index){
		if(!classMap.containsKey(toAdd)){
			classes.add(toAdd);
			classMap.put(toAdd, index);
		}
		if(nextClassIdx <= index)
		{
			nextClassIdx = index + 1; 
		}
		return index;
	}
	
	/**
	 * Convenience method to add a new class without having to find an index for it. 
	 * This method simply adds the class to the highest available index. 
	 * @param toAdd The class name to add. 
	 */
	public int addClass(String toAdd)
	{
		return addClass(toAdd, nextClassIdx);
	}
	
	public void removeClass(String toRemove){
		if(classMap.containsKey(toRemove)){
			classes.remove(toRemove);
			classMap.remove(toRemove);
		}
	}
	
	public ArrayList<String> getClassList(){
		return classes;
	}
	
	public HashMap<String, Integer> getClassMap(){
		return classMap;
	}
	
	public Integer get(String key){
		return classMap.get(key);
	}
		
	public Iterator<Map.Entry<String,Integer>> getEntryIterator(){
		
		return classMap.entrySet().iterator();
	}
}
