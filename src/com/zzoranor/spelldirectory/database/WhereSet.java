package com.zzoranor.spelldirectory.database;

import java.util.ArrayList;

import com.zzoranor.spelldirectory.util.Constants;
import com.zzoranor.spelldirectory.util.Triple;

public class WhereSet {

	//private ArrayList<String> where;
	//private ArrayList<String> args;
	private ArrayList<Triple> list;
	
	public WhereSet() {
		//where = new ArrayList<String>();
		//args = new ArrayList<String>();
		list = new ArrayList<Triple>();
	}
	
	public WhereSet(String field, String value) {
		//where = new ArrayList<String>();
		//args = new ArrayList<String>();
		list = new ArrayList<Triple>();
		add(field, value);
	}

	public WhereSet(String field, int value) {
		//where = new ArrayList<String>();
		//args = new ArrayList<String>();
		list = new ArrayList<Triple>();
		add(field, value);
	}
	
	public int countWhere()
	{
		return list.size();
	}
	
	public int countArgs()
	{
		return list.size();
	}

	public void add(String field, String value) {
		//where.add(field);
		//args.add(value);
		list.add(new Triple(field, Triple.EQ, value));
	}

	public void add(String field, int value) {
		//where.add(field);
		//args.add("" + value);
		list.add(new Triple(field, Triple.EQ, "" + value));
	}
	
	public void add(String field, String operand, String value)
	{
		list.add(new Triple(field, operand, value));
	}
	
	public void add(String field, String operand, int value)
	{
		list.add(new Triple(field, operand, "" + value));
	}

	public void add(Triple triple)
	{
		list.add(new Triple(triple));
	}
	
	public String[] getWhere() {
		/*
		if(where.size() == 0)
			return null;
		String[] s = new String[where.size()];
		return where.toArray(s);
		*/
		
		if(list.size() == 0)
		{
			return null;
		}
		String[] s = new String[list.size()];
		for(int i = 0; i < list.size(); i++)
		{
			s[i] = list.get(i).field;
		}
		return s;
	}

	public String[] getArgs() {
		/*
		if(args.size() == 0)
			return null;
		String[] s = new String[args.size()];
		return args.toArray(s); */
		
		if(list.size() == 0)
			return null;
		
		String[] s = new String[list.size()];
		for(int i = 0; i < list.size();i++)
		{
			s[i] = list.get(i).value;
		}
		return s;
	}
	
	public static WhereSet charId(int id)
	{
		return new WhereSet(Constants.CHAR_ID, id);
	}

	public static WhereSet spellId(boolean shortId, int id)
	{
		if(shortId)
		{
			return new WhereSet(Constants.SHORT_ID, id);
		}else
		{
			return new WhereSet(Constants.SPELL_ID, id);
		}
	}
	
	public String buildWhereSelection()
	{
		StringBuilder whereBuilder = new StringBuilder();

		if (list.size() > 0) {
			for (int i = 0; i < list.size() - 1; i++) {
				Triple t = list.get(i);
				whereBuilder.append(t.field + " " + t.operand + " ? AND ");
			}

			// Add the last fields to the where clause.
			Triple t = list.get(list.size() - 1);
			whereBuilder.append(t.field + " " + t.operand + " ?");
		}else
		{
			return null;
		}

		return whereBuilder.toString();
		
	}
	
	/*
	public static String buildWhereSelection(String[] where) {
		StringBuilder whereBuilder = new StringBuilder();

		if (where != null) {

			if (where.length > 0) {
				for (int i = 0; i < where.length - 1; i++) {
					whereBuilder.append(where[i] + " = ? AND ");
				}

				// Add the last fields to the where clause.
				whereBuilder.append(where[where.length - 1] + " = ?");
			}

			return whereBuilder.toString();
		} else {
			return null;
		}
	}
	*/
	
	@Override
	public String toString() {
		String str = buildWhereSelection();
		StringBuilder builder = new StringBuilder(str);
		int lastIdx = 0;
		for(int i = 0; i < list.size(); i++)
		{
			lastIdx = builder.indexOf("?", lastIdx);
			if(lastIdx != -1)
			{
				builder.replace(lastIdx,lastIdx+1, list.get(i).value);
			}
		}
		return builder.toString();
	}
}
