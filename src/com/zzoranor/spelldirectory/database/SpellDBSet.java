package com.zzoranor.spelldirectory.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.database.Cursor;
import android.util.Log;

public class SpellDBSet {
	
	HashMap<String, Integer> strNameMap;
	int numRows;
	int numFields;
	ArrayList<String>[] map;
	String tag = "SpellDBSet";
	
	public SpellDBSet(String[] fields)
	{
		strNameMap = new HashMap<String, Integer>();
		for(int i = 0; i < fields.length; i++)
		{
			strNameMap.put(fields[i], i);
		}
		numFields = strNameMap.size();
	}
	
	public SpellDBSet(Cursor c)
	{
		strNameMap = new HashMap<String, Integer>();
		String[] fields = c.getColumnNames();
		
		for(int i = 0; i < fields.length; i++)
		{
			strNameMap.put(fields[i], i);
		}
		numFields = strNameMap.size();
		
		if(c.moveToFirst())
		{
			map = new ArrayList[c.getCount()];
			for(int i = 0; i < c.getCount(); i++)
			{
				map[i] = new ArrayList<String>();
			}
			
			for(int i = 0; i < c.getCount(); i++)
			{
				for(int j = 0; j < fields.length; j++)
				{
					String s = c.getString(j);
					if(s == null)
					{
						s = "" + c.getInt(j);
					}
					map[i].add(s);
				}
				c.moveToNext();
			}
			
		}
		
		numRows = c.getCount();
		
	}
	
	public Iterator<String> getFieldIterator()
	{
		return strNameMap.keySet().iterator();
	}
	
	public void addField(String field)
	{
		addField(field, "");
	}
	
	public void addField(String field, String defValue)
	{
		if(!strNameMap.containsKey(field))
		{
			strNameMap.put(field, numFields++);
			for(int i = 0; i < numRows; i++)
			{
				map[i].add("");
			}
		}
	}
	
	
	public void set(String field, int row, String value)
	{
		Integer idx = strNameMap.get(field);
		if(idx != null)
		{
			map[row].set(idx, value);
		}else
		{
			Log.e(tag, "set() field does not exist in the map. ");
		}
	}
	
	/**
	 * Short Hand for returning the specific field from the first row in the set.
	 * @param field
	 * @return
	 */
	public String get(String field)
	{
		if(numRows > 0)
		{
			return get(field, 0);
		}else
		{
			Log.e(tag, "get() no rows in set when trying to retrieve field: " + field);
			return "";
		}
	}
	
	/** 
	 * Returns the specific field, at the specific row. 
	 * @param field
	 * @param row
	 * @return
	 */
	public String get(String field, int row)
	{
		Integer idx = strNameMap.get(field);
		if(idx != null)
		{
			return map[row].get(idx);
		}else
		{
			Log.e(tag, "get() field="+field+ " does not exist in the map. ");
			return "";
		}
	}
	
	/**
	 * Convenience method where value is parsed as an int. This 
	 * method takes the first row in the set.
	 * @param field
	 * @return
	 */
	public int getInt(String field)
	{
		if(numRows > 0)
		{
			return getInt(field, 0);
		}else
		{
			Log.e(tag, "getInt() no rows in set when trying to retrieve field: " + field);
			return -1;
		}
	}
	
	/**
	 * Convenience method where value is parsed as an int. 
	 * @param field
	 * @param row
	 * @return
	 */
	public int getInt(String field, int row)
	{
		String s = get(field, row);
		try
		{
			if(s == null)
			{
				Log.e(tag, "getInt() saved value is null, returning -1");
				return -1;
			}else
			{
				Integer i = Integer.parseInt(s);
				return i;
			}
			
		}catch(NumberFormatException e)
		{
			Log.e(tag, "getInt() cannot parse string:" + s + " as integer. Returning -1");
			return -1;
		}
	}
	
	public int getNumRows()
	{
		return numRows;
	}
	
	public int getNumFields()
	{
		return numFields;
	}
	
	
	public void printToLog(int maxRows)
	{
		
		String[] fields = new String[strNameMap.size()];
		fields = strNameMap.keySet().toArray(fields);
		Log.d(tag, "*** Start SpellDBSet ***");
		for(int i = 0; i < numRows && i < maxRows; i++)
		{
			StringBuilder sb = new StringBuilder();
			for(int j = 0; j < numFields; j++)
			{
				sb.append(fields[j] + ": " + get(fields[j], i) + " ");
			}
			Log.d(tag, sb.toString());
		}
		Log.d(tag, "*** End SpellDBSet ***");
	}
	
}
