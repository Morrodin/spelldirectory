package com.zzoranor.spelldirectory.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Metamagics {

	private static ArrayList<String> metamagicList = null;
	private static HashMap<String, Integer> metamagicMap = null;
		
	public Metamagics(){
		if(metamagicList == null)
			metamagicList = new ArrayList<String>();
		if(metamagicMap == null)
			metamagicMap = new HashMap<String, Integer>();
	}
		
		public int numMetamagics(){
			return metamagicList.size();
		}
		
		public void addMetamagic(String toAdd, int adj){
			if(!metamagicMap.containsKey(toAdd)){
				metamagicList.add(toAdd + "	(+" + adj + ")");
				//metamagicList.add(toAdd);
				metamagicMap.put(toAdd, adj);
			}
		}
		
		public void removeMetamagic(String toRemove){
			if(metamagicMap.containsKey(toRemove)){
				metamagicList.remove(toRemove);
				metamagicMap.remove(toRemove);
			}
		}
		
		public ArrayList<String> getMetamagicList(){
			return metamagicList;
		}
		
		public HashMap<String, Integer> getMetamagicMap(){
			return metamagicMap;
		}
		
		public Integer get(String key){
			return metamagicMap.get(key);
		}
			
		public Iterator<Map.Entry<String,Integer>> getEntryIterator(){
			
			return metamagicMap.entrySet().iterator();
		}
	}

