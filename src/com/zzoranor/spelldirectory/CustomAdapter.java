package com.zzoranor.spelldirectory;

import java.util.ArrayList;

import com.zzoranor.spelldirectory.data.Character;

import android.content.Context;
import android.graphics.Color;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.zzoranor.spelldirectory.util.SerializablePair;

public class CustomAdapter extends ArrayAdapter<SpellLabel> {
	private LayoutInflater vi;
	ArrayList<SpellLabel> items;
	// ItemsFilter mFilter;
	Context context;
	int resource;
	boolean showPrepared;
	Character character = null;

	/**
	 * This constructor should be used when Prepared spells should be visible. 
	 * It uses data from the character provided to show what spells are prepared. 
	 * 
	 * @param context
	 * @param resource
	 * @param textViewResourceId
	 * @param character
	 */
	public CustomAdapter(Context context, int resource, int textViewResourceId,
			Character character, ArrayList<SpellLabel> objects) {
		super(context, resource, textViewResourceId, objects);
		this.items = objects;
		this.character = character;
		init(context, resource, textViewResourceId);

	}
	
	/**
	 * Function run by all constructors to initiate some values.  
	 */
	private void init(Context context, int resource, int textViewResourceId){
		this.context = context;
		this.resource = resource;
		vi = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setCharacter(Character _character){
		this.character = _character;
	}
	
	public Character getCharacter(){
		return character;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		View v = convertView;
		if (v == null) {
			v = vi.inflate(resource, null);

			holder = new ViewHolder();
			holder.view_name = (TextView) v.findViewById(R.id.list_view_name);
			holder.view_prepare = (TextView) v
					.findViewById(R.id.list_view_prepare);
			holder.view_lvl = (TextView) v
					.findViewById(R.id.list_view_spell_lvl);
			holder.view_school = (TextView) v
					.findViewById(R.id.list_view_spell_school);
			holder.view_known = (TextView) v
					.findViewById(R.id.list_view_spell_known);
			v.setTag(holder);

		} else {
			holder = (ViewHolder) v.getTag();
		}

		SpellLabel o = super.getItem(position);

		if (o != null) {
			holder.view_name.setText(o.getName());
			holder.view_lvl.setText("" + o.getLvl());
			holder.view_school.setText(o.getSchool());
            SerializablePair<Integer, Integer> numPrepared = character.getUsedPrepared(o);
			if(numPrepared.second == 0){
				holder.view_prepare.setText("");
			}else{
				if(numPrepared.first == 0){
					holder.view_prepare.setTextColor(Color.RED);
				}else{
					holder.view_prepare.setTextColor(Color.CYAN);
				}
				holder.view_prepare.setText(""+numPrepared.first+"/" + numPrepared.second);
			}
			if (o.isKnown())
				holder.view_known.setText("Known");
			else{
				holder.view_known.setText("");
			}
			
			
			
		}
		return v;
	}

	static class ViewHolder {
		TextView view_name;
		TextView view_prepare;
		TextView view_lvl;
		TextView view_school;
		TextView view_known;
	}

}
