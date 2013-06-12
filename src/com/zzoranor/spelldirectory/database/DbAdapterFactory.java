package com.zzoranor.spelldirectory.database;

import java.util.HashMap;

import android.content.Context;



/**
 * This Factory is to be used to create the static DbAdapters. This provides
 * a layer of abstraction between the Activities using the adapter, and the adapter 
 * itself, allowing fore future improvements to DbAdapter code layout. 
 * @author Zoranor
 *
 */
public class DbAdapterFactory {


	private static DbAdapter staticInstance = null;
	public static DbAdapter getStaticInstance(Context context)
	{
		if(staticInstance == null)
		{
			staticInstance = new DbAdapter(context);
		}
		
		return staticInstance;
	}
	
	public static DbAdapter getStaticInstance(Context context, HashMap<String, Integer> map)
	{
		if(staticInstance == null)
		{
			staticInstance = new DbAdapter(context, map);
		}
		
		return staticInstance;
	}
}
