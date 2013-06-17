package com.zzoranor.spelldirectory;

import java.io.Serializable;
import java.util.Comparator;

import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import com.zzoranor.spelldirectory.util.SerializablePair;

public class SpellLabel implements Comparable<SpellLabel>, Serializable {

    private static final long serialVersionUID = 68732567123L;
	private String spell_name;
	private int spell_id;
	private int spell_lvl;
	private String spell_school;
	private SerializablePair<Integer, Integer> numPrepared;
	private boolean known = false;
	private static Comparator<SpellLabel> lvlNameComparator;

	public SpellLabel() {
	}

	public SpellLabel(String name) {
		this.spell_name = name;
	}

	public SpellLabel(String name, int id, int lvl, String school)
	{
		this.spell_name = name;
		this.spell_id = id;
		this.spell_lvl = lvl;
		this.spell_school = school;
		this.numPrepared = new SerializablePair<Integer, Integer>(0,0);
	}
	
	public SpellLabel(String str, int id, int lvl, String school,
			int left_today, int prepared) {
		this.spell_name = str;
		this.spell_id = id;
		this.spell_lvl = lvl;
		this.spell_school = school;
		this.numPrepared = new SerializablePair<Integer, Integer>(left_today, prepared);
	}

	public SpellLabel(View v) {
		TextView view_name = (TextView) v.findViewById(R.id.list_view_name);
		TextView view_prepare = (TextView) v
				.findViewById(R.id.list_view_prepare);
		TextView view_lvl = (TextView) v.findViewById(R.id.list_view_spell_lvl);
		TextView view_school = (TextView) v
				.findViewById(R.id.list_view_spell_school);

		spell_id = -1;
		spell_name = (String) view_name.getText();
		spell_lvl = Integer.valueOf((String) view_lvl.getText());
		spell_school = (String) view_school.getText();
		String text = (String) view_prepare.getText();
		int prepared;
		int left_today;
		if (text.length() == 0) {
			prepared = 0;
			left_today = 0;
		} else {
			String[] tokens = text.split("/");
			if (tokens.length == 1) { // No /, means we have a number.
				prepared = Integer.parseInt(text);
				left_today = prepared;
			} else {
				left_today = Integer.parseInt(tokens[0]);
				prepared = Integer.parseInt(tokens[1]);
			}
		}
		numPrepared = new SerializablePair<Integer, Integer>(left_today, prepared);
	}

	public int getLvl() {
		return spell_lvl;
	}

	public void setLvl(int lvl) {
		this.spell_lvl = lvl;
	}

	public String getSchool() {
		return spell_school;
	}

	public void setSchool(String school) {
		this.spell_school = school;
	}

	public void setId(int id) {
		this.spell_id = id;
	}

	public int getId() {
		return spell_id;
	}

	public void setName(String str) {
		this.spell_name = str;
	}

	public String getName() {
		return spell_name;
	}

	public boolean isPrepared() {
		return numPrepared.second > 0;
	}

	public int numPrepared() {
		return numPrepared.second;
	}

	public void setPrepared(SerializablePair<Integer, Integer> prepared) {
		this.numPrepared = prepared;
	}

	public void setKnown(boolean known) {
		this.known = known;
	}

	public boolean isKnown() {
		return known;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || o.getClass() != this.getClass())
			return false;

		SpellLabel obj = (SpellLabel) o;

		return spell_name.equals(obj.spell_name);
	}

	@Override
	public String toString() {
		return spell_name;
	}

	public int compareTo(SpellLabel another) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		
		if (this == another) 
			return EQUAL;
		
		SpellLabel a = (SpellLabel) another;
		
		// Same lvl.
		if(this.getLvl() == a.getLvl())
		{
			int cmp = this.getName().compareTo(a.getName());
			
			if(cmp < 0)
				return BEFORE;
			else if(cmp > 0)
				return AFTER;
			else
				return EQUAL;
		}else if(this.getLvl() < a.getLvl())
		{
			return BEFORE;
		}else
		{
			return AFTER;
		}
	}
}
