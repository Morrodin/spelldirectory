package com.zzoranor.spelldirectory.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.zzoranor.spelldirectory.SpellLabel;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;

public class Character {
	private String charName;
	private int char_id = 1;
	private int[] prepSpellLevels;
	private String curr_class_name = "";
	private int curr_class_id = 0;
	
	ArrayList<SpellLabel> prepared_spells;
	HashMap<String,  Pair<Integer,Integer> > map_prep_spells;
	
	public Character(int id, String name){
		char_id = id;
		charName = name;
		prepared_spells = new ArrayList<SpellLabel>();
		map_prep_spells = new HashMap<String, Pair<Integer,Integer> >();
		prepSpellLevels = new int[10];
	}
	
	public Character(String name, ArrayList<SpellLabel> prepared_spells,HashMap<String, Pair<Integer,Integer> > map_prep_spells){
		charName = name;
		this.prepared_spells = prepared_spells;
		this.map_prep_spells = map_prep_spells;
		prepSpellLevels = new int[10];
	}
	
	public Character(int id, String name, String chosen_class, int class_id) {
		char_id = id;
		charName = name;
		curr_class_name = chosen_class;
		curr_class_id = class_id;
		prepared_spells = new ArrayList<SpellLabel>();
		map_prep_spells = new HashMap<String, Pair<Integer,Integer> >();
		prepSpellLevels = new int[10];
	}

	public int getCharId(){
		return char_id;
	}
	
	public String getCurrClassName()
	{
		return curr_class_name;
	}
	
	public void setCurrClassName(String className)
	{
		curr_class_name = className;
	}
	
	public int getCurrClassId()
	{
		return curr_class_id;
	}
	
	public void setCurrClassId(int classId)
	{
		curr_class_id = classId;
	}
	
	
	public String getCharName() {
		return charName;
	}
	public void setCharName(String charName) {
		this.charName = charName;
	}
	public ArrayList<SpellLabel> getPrepared_spells() {
		return prepared_spells;
	}
	public void setPrepared_spells(ArrayList<SpellLabel> preparedSpells) {
		prepared_spells = preparedSpells;
	}
	public HashMap<String,  Pair<Integer,Integer> > getMap_prep_spells() {
		return map_prep_spells;
	}
	public void setMap_prep_spells(HashMap<String,  Pair<Integer,Integer> > mapPrepSpells) {
		map_prep_spells = mapPrepSpells;
	}

	public Iterator<Map.Entry<String,Pair<Integer, Integer> > > getEntryIterator(){
		return map_prep_spells.entrySet().iterator();
	}
	
	private static int[][] lineConfig = new int[][]{{10}, {5, 5}, {4,3,3}, {3,3,2,2}};
	
	public SpannableString getSpanString(int lines){
		StringBuilder sb = new StringBuilder();
		
		// start on lvl 0.
		int lvl = 0; 
		int twoLetterNumberOffset = 0; 
		// For each Line
		for(int i = 0; i < lines;i++)
		{
			// Go through the number of lvls defines in the lineConfig for line i, j times.  
			for(int j = 0; j < lineConfig[lines-1][i];j++)
			{
				sb.append(lvl);
				sb.append(": ");
				sb.append(prepSpellLevels[lvl]);
				if(prepSpellLevels[lvl] < 10)
				{
					sb.append("   ");
					
				}else
				{
					sb.append(" ");
					twoLetterNumberOffset++;
				}
				lvl++;
			}
			sb.append("\n");
			
		}
		
		/*
		String s = "";
		for(int i = 0; i < 5;i++){
			s += i + ": " + prepSpellLevels[i] + (prepSpellLevels[i] < 10 ? "  " : " ");
		}
		s +="\n";
		for(int i = 5; i < 9;i++){
			s += i + ": " + prepSpellLevels[i] + (prepSpellLevels[i] < 10 ? "  " : " ");
		}
		s += "9: " + prepSpellLevels[9] + (prepSpellLevels[9] < 10 ? "  " : " ");
		*/
		//"0:  xx  1:  xx  2:  xx  3:  xx  4:  xx  5:  xx  6:  xx  7:  xx  8:  xx  9:  xx  ."
		SpannableString str = new SpannableString(sb.toString());
		int numCharsPerLvl = 7;
		int numColoredPerLvl = 3;
		int offSetToFirstColored = 2;
		int lvlSplitOffset = 0;
		lvl = 0; 
		
		String fullString = sb.toString();
		int startAt = 0;
		for(int i = 0; i < 10;i++)
		{
			int start = fullString.indexOf(":", startAt) + 1;	// Find starting position. 
			if(start > 0)
			{
				int end = fullString.indexOf(" ", start+1);	// Find next space. +2 to avoid the first space after : 
				if(end > 0)
				{
					str.setSpan(new ForegroundColorSpan(Color.CYAN), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					startAt = end;
				}
			}
		}
		return str;
		
	}
	
	public Pair<Integer,Integer> getUsedPrepared(SpellLabel sp){
		Pair<Integer, Integer> numPrepared = map_prep_spells.get(sp.getName());
		if(numPrepared == null)
			return new Pair<Integer,Integer>(0,0);
		else
			return numPrepared;
	}
	
	public Pair<Integer,Integer> getUsedPrepared(String spell_name){
		Pair<Integer, Integer> numPrepared = map_prep_spells.get(spell_name);
		if(numPrepared == null)
			return new Pair<Integer,Integer>(0,0);
		else
			return numPrepared;
	}

	public void prepareSpell(SpellLabel sp, int numToPrepare){
		prepareSpell(sp,numToPrepare,numToPrepare);
	}
	
	public void prepareSpell(SpellLabel sp,int numUses, int numToPrepare){
		Pair<Integer, Integer> num = getUsedPrepared(sp.getName());
		int lvl = sp.getLvl();
		if(0 <= lvl && lvl < 10){
			prepSpellLevels[lvl] = prepSpellLevels[lvl] + numToPrepare;
		}
		if(num.second <= 0){
			prepared_spells.add(sp);
			map_prep_spells.put(sp.getName(), new Pair<Integer,Integer>(numUses,numToPrepare));
		}else{
			map_prep_spells.put(sp.getName(), new Pair<Integer,Integer>(num.first + numUses,num.second + numToPrepare));
		} 
	}
	
	public void resetAllSpellUses(){
		for(SpellLabel sp : prepared_spells){
			Pair<Integer,Integer> curr = map_prep_spells.get(sp.getName());
			map_prep_spells.put(sp.getName(), Pair.create(curr.second, curr.second));
		}
	}
	
	public void resetSpellUse(String spell_name){
		Pair<Integer,Integer> curr = map_prep_spells.get(spell_name);
		map_prep_spells.put(spell_name, Pair.create(curr.second, curr.second));
	}
	
	public void resetSpellUse(SpellLabel sp){
		Pair<Integer,Integer> curr = map_prep_spells.get(sp.getName());
		map_prep_spells.put(sp.getName(), Pair.create(curr.second, curr.second));
	}
	public void useSpell(String spell_name){
		Pair<Integer,Integer> curr = map_prep_spells.get(spell_name);
		int left_today = (curr.first-1 >= 0? curr.first-1 : 0);
		map_prep_spells.put(spell_name, Pair.create(left_today, curr.second));
	}
	
	public void useSpell(SpellLabel sp){
		useSpell(sp.getName());
	}
	
	public void removeSpell(SpellLabel sp){
		Pair<Integer, Integer> num = getUsedPrepared(sp.getName());
		int prepared = num.second-1;
		int left_today = num.first; 
		int lvl = sp.getLvl();
		if(0 <= lvl && lvl < 10 && prepSpellLevels[lvl] > 0){
			prepSpellLevels[lvl]--;
		}
		// Prefer to remove used prepared spells to unused ones. 
		// Leaving the number of spells left on a particular day unchanged as far as possible.
		if(prepared < left_today)
			left_today = prepared;
		
		if(prepared > 0){
			map_prep_spells.put(sp.getName(),  new Pair<Integer,Integer>(left_today,prepared));
		}else if(prepared <= 0){
			prepared_spells.remove(sp);
			map_prep_spells.remove(sp.getName());
		}
	}
	
	public void removeSpell(String spell_name){	
		SpellLabel sp = new SpellLabel(spell_name);
		removeSpell(sp);
	}
	
	
	public void removeAll(){
		prepared_spells.clear();
		map_prep_spells.clear();
		for(int i = 0; i < 10; i++){
			prepSpellLevels[i] = 0;
		}
	}

	public void sortPrepared() {
		Collections.sort(prepared_spells);
	}

	public void setCurrentClass(int id, String name) {
		curr_class_name = name;
		curr_class_id = id;
		
	}
	
	@Override
	public String toString() {
		return charName + " (" + char_id +  ") " + curr_class_name + "(" + curr_class_id + ")";
	}
}
