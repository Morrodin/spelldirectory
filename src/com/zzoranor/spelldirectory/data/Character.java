package com.zzoranor.spelldirectory.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.zzoranor.spelldirectory.SpellLabel;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import com.zzoranor.spelldirectory.util.SerializablePair;

public class Character implements Serializable {

    private static final long serialVersionUID = 678321678145768L;

	private String charName;
	private int char_id = 1;
	private int[] prepSpellLevels;
	private String curr_class_name = "";
	private int curr_class_id = 0;
	
	PreparedSpellList preparedSpells;
	PreparedSpellMap preparedSpellMap;
	
	public Character(int id, String name){
		char_id = id;
		charName = name;
		preparedSpells = new PreparedSpellList(new ArrayList<SpellLabel>());
		preparedSpellMap.setPreparedSpellMap(new HashMap<String, SerializablePair<Integer, Integer>>());
		prepSpellLevels = new int[10];
	}
	
	public Character(String name, ArrayList<SpellLabel> preparedSpells,HashMap<String, SerializablePair<Integer,Integer> > preparedSpellMap){
		charName = name;
		this.preparedSpells.setPreparedSpellList(preparedSpells);
		this.preparedSpellMap.setPreparedSpellMap(preparedSpellMap);
		prepSpellLevels = new int[10];
	}
	
	public Character(int id, String name, String chosen_class, int class_id) {
		char_id = id;
		charName = name;
		curr_class_name = chosen_class;
		curr_class_id = class_id;
        preparedSpells = new PreparedSpellList(new ArrayList<SpellLabel>());
		preparedSpellMap = new PreparedSpellMap(new HashMap<String, SerializablePair<Integer,Integer> >());
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
	public ArrayList<SpellLabel> getPreparedSpells() {
		return preparedSpells.getPreparedSpellList();
	}
	public void setPreparedSpells(ArrayList<SpellLabel> preparedSpells) {
		this.preparedSpells.setPreparedSpellList(preparedSpells);
	}
	public HashMap<String,  SerializablePair<Integer,Integer> > getPreparedSpellMap() {
		return preparedSpellMap.getPreparedSpellMap();
	}
	public void setPreparedSpellMap(HashMap<String, SerializablePair<Integer, Integer>> mapPrepSpells) {
		preparedSpellMap.setPreparedSpellMap(mapPrepSpells);
	}

	public Iterator<Map.Entry<String,SerializablePair<Integer, Integer> > > getEntryIterator(){
		return preparedSpellMap.getPreparedSpellMap().entrySet().iterator();
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
	
	public SerializablePair<Integer,Integer> getUsedPrepared(SpellLabel sp){
		SerializablePair<Integer, Integer> numPrepared = preparedSpellMap.getPreparedSpellMap().get(sp.getName());
		if(numPrepared == null)
			return new SerializablePair<Integer,Integer>(0,0);
		else
			return numPrepared;
	}
	
	public SerializablePair<Integer,Integer> getUsedPrepared(String spell_name){
		SerializablePair<Integer, Integer> numPrepared = preparedSpellMap.getPreparedSpellMap().get(spell_name);
		if(numPrepared == null)
			return new SerializablePair<Integer,Integer>(0,0);
		else
			return numPrepared;
	}

	public void prepareSpell(SpellLabel sp, int numToPrepare){
		prepareSpell(sp,numToPrepare,numToPrepare);
	}
	
	public void prepareSpell(SpellLabel sp,int numUses, int numToPrepare){
		SerializablePair<Integer, Integer> num = getUsedPrepared(sp.getName());
		int lvl = sp.getLvl();
		if(0 <= lvl && lvl < 10){
			prepSpellLevels[lvl] = prepSpellLevels[lvl] + numToPrepare;
		}
		if(((Integer) num.second) <= 0){
			preparedSpells.getPreparedSpellList().add(sp);
			preparedSpellMap.getPreparedSpellMap().put(sp.getName(), new SerializablePair<Integer, Integer>(numUses, numToPrepare));
		}else{
			preparedSpellMap.getPreparedSpellMap().put(sp.getName(),
                    new SerializablePair<Integer, Integer>(((Integer) num.first) + numUses, ((Integer)num.second) + numToPrepare));
		} 
	}
	
	public void resetAllSpellUses(){
		for(SpellLabel sp : preparedSpells.getPreparedSpellList()){
            SerializablePair<Integer,Integer> curr = preparedSpellMap.getPreparedSpellMap().get(sp.getName());
			preparedSpellMap.getPreparedSpellMap().put(sp.getName(), SerializablePair.create(curr.second, curr.second));
		}
	}
	
	public void resetSpellUse(String spell_name){
        SerializablePair<Integer,Integer> curr = preparedSpellMap.getPreparedSpellMap().get(spell_name);
		preparedSpellMap.getPreparedSpellMap().put(spell_name, SerializablePair.create(curr.second, curr.second));
	}
	
	public void resetSpellUse(SpellLabel sp){
        SerializablePair<Integer,Integer> curr = preparedSpellMap.getPreparedSpellMap().get(sp.getName());
		preparedSpellMap.getPreparedSpellMap().put(sp.getName(), SerializablePair.create(curr.second, curr.second));
	}
	public void useSpell(String spell_name){
        SerializablePair<Integer,Integer> curr = preparedSpellMap.getPreparedSpellMap().get(spell_name);
		int left_today = (curr.first-1 >= 0? curr.first-1 : 0);
		preparedSpellMap.getPreparedSpellMap().put(spell_name, SerializablePair.create(left_today, curr.second));
	}
	
	public void useSpell(SpellLabel sp){
		useSpell(sp.getName());
	}
	
	public void removeSpell(SpellLabel sp){
		SerializablePair<Integer, Integer> num = getUsedPrepared(sp.getName());
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
			preparedSpellMap.getPreparedSpellMap().put(sp.getName(), new SerializablePair<Integer, Integer>(left_today, prepared));
		}else if(prepared <= 0){
			preparedSpells.getPreparedSpellList().remove(sp);
			preparedSpellMap.getPreparedSpellMap().remove(sp.getName());
		}
	}
	
	public void removeSpell(String spell_name){	
		SpellLabel sp = new SpellLabel(spell_name);
		removeSpell(sp);
	}
	
	
	public void removeAll(){
		preparedSpells.getPreparedSpellList().clear();
		preparedSpellMap.getPreparedSpellMap().clear();
		for(int i = 0; i < 10; i++){
			prepSpellLevels[i] = 0;
		}
	}

	public void sortPrepared() {
		Collections.sort(preparedSpells.getPreparedSpellList());
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
