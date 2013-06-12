package com.zzoranor.spelldirectory.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.database.Cursor;
import android.util.Pair;

import com.zzoranor.spelldirectory.database.SpellDBSet;
import com.zzoranor.spelldirectory.util.Constants;

public class Spell {
	
	private int spell_id = -1;
	
	/*
	private String name;
	private String school = null;
	private String sub_school = null;
	private String descriptor = null;
	private String level = null;
	private String castingTime = null;
	private String components = null;
	private String range = null;
	private String area = null;
	private String target = null;
	private String duration = null;
	private String savingthrow = null;
	private String spellresistance = null;
	private String description = null;
	private String effect = null;
	private boolean prepared = false;
	*/
	private ArrayList<Pair<String,Integer> > classLevels;
	//private ArrayList<Pair<String,String> > data;
	private HashMap<String,String> data;
	
	//public static final String _ID = "_id";
	public static final String ID = "_id";
	public static final String NAME = "spell_name";
	public static final String LEVEL = "spell_level";	
	public static final String SCHOOL = "spell_school";
	public static final String SUBSCHOOL = "spell_subschool";
	public static final String DESCRIPTOR = "spell_descriptor";
	public static final String CASTING_TIME = "spell_casttime";
	public static final String COMPONENTS = "spell_components";
	public static final String RANGE = "spell_range";
	public static final String AREA = "spell_area";
	public static final String TARGET = "spell_target";
	public static final String DURATION = "spell_duration";
	public static final String SAVINGTHROW = "spell_savingthrow";
	public static final String SPELL_RESISTANCE = "spell_resistance";
	public static final String SPELL_SOURCE = "spell_source";
	public static final String DESCRIPTION = "spell_description";
	public static final String EFFECT = "spell_effect";
		
	
	public int getId(){	
		return spell_id;
	}
	public void setId(int id){
		spell_id = id;
	}
	/*
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getCastingTime() {
		return castingTime;
	}
	public void setCastingTime(String castingTime) {
		this.castingTime = castingTime;
	}
	public String getComponents() {
		return components;
	}
	
	*/
	public ArrayList<Pair<String, Integer> > getClassLevels(){
		return classLevels;	
	}
	
	public void setClassLevels(ArrayList<Pair<String, Integer> > arr){
		classLevels = arr;	
	}
	
	/*
	public void setComponents(String components) {
		this.components = components;
	}
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getSavingthrow() {
		return savingthrow;
	}
	public void setSavingthrow(String savingthrow) {
		this.savingthrow = savingthrow;
	}
	public String getSpellresistance() {
		return spellresistance;
	}
	public void setSpellresistance(String spellresistance) {
		this.spellresistance = spellresistance;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setPrepared(boolean prep){
		prepared = true;
	}
	
	public boolean getPrepared(){
		return prepared;
	}
	
	public void setSubSchool(String tmp) {
		sub_school = tmp;
	}
	
	public void setDescriptor(String tmp) {
		descriptor = tmp;
	}
	
	public String getSubSchool() {
		return sub_school;
	}
	
	public String getDescriptor() {
		return descriptor;
	}
	*/
	
	public void setData(HashMap<String,String> str){
		data = str;
	}
	public HashMap<String, String> getDataMap(){
		return data;
	}
	
	public Iterator<Map.Entry<String,String>> getEntryIterator(){
		
		return data.entrySet().iterator();
	}
	
	/*
	public void parseClassLevels(String levels){
		if(levels != null && levels.length() > 0){
			String[] tokens = levels.split(",");

			for(int i = 0; i < tokens.length; i++){
				tokens[i] = tokens[i].trim().toUpperCase();
				// Retrieve the number
				String[] t = tokens[i].split(" ");
				int lvl = -1;
				if(t.length == 2){
					lvl = Integer.parseInt(t[1]);
				}
			
				t = t[0].split("/");
				
				for(int j = 0; j < t.length;j++){
					Pair<String, Integer> p = new Pair<String, Integer>(t[j], lvl);
					classLevels.add(p);
				}				
			}
		}
	}
	*/

	/*
	public String getEffect() {
		return effect;
	}
	public void setEffect(String effect) {
		this.effect = effect;
	}
	
	*/
	
	public void addToData(String col,String val){
		data.put(col, val);
	}
	
	public String getData(String col){
		return data.get(col);
	}
	
	public Spell(){
		classLevels = new ArrayList<Pair<String, Integer> >();
		data = new HashMap<String, String>();
	}
	
	public Spell(Cursor c){
		classLevels = new ArrayList<Pair<String, Integer> >();
		data = new HashMap<String, String>();
		c.moveToFirst();
		
		for(int i = 1; i < c.getColumnCount();i++){
			data.put(c.getColumnName(i), c.getString(i));
		}
	}
	
	public Spell(SpellDBSet dataSet, SpellDBSet levels)
	{
		classLevels = new ArrayList<Pair<String, Integer> >();
		data = new HashMap<String, String>();
		Iterator<String> it = dataSet.getFieldIterator();
		while(it.hasNext())
		{
			String field = it.next();
			data.put(field, dataSet.get(field));
		}

		for(int i = 0; i< levels.getNumRows();i++)
		{
			classLevels.add(new Pair<String, Integer>(levels.get(Constants.CLASS_NAME,i), levels.getInt(Constants.SPELL_LVL,i)));
		}
		
	}
		
	@Override
	public String toString() {
		String s = "";
		Iterator it = getEntryIterator();
		while(it.hasNext()){
			Map.Entry<String, String> p = (Map.Entry<String, String>) it.next();
			s += "\t" + p.getKey() + " = " + p.getValue() + " \n";
			
		}
		
		return s;
	}

	
	
	
}
