package com.zzoranor.spelldirectory;

import java.util.ArrayList;

import com.zzoranor.spelldirectory.data.Character;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomClassAdapter extends ArrayAdapter<ClassLabel> {
	private LayoutInflater vi;
	ArrayList<ClassLabel> items;
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
	public CustomClassAdapter(Context context, int resource, int textViewResourceId,
			Character character, ArrayList<ClassLabel> objects) {
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
			holder.view = (TextView) v.findViewById(R.id.class_view_id);

			v.setTag(holder);

		} else {
			// Set Viewholder.
			holder = (ViewHolder) v.getTag();
		}

		ClassLabel o = super.getItem(position);

		// Set the view to the correct values
		if (o != null) {
			holder.view.setText(o.getName());
			
			if(character != null && character.getCurrClassId() == o.getId()){
				holder.view.setTextColor(Color.CYAN);
			}else{
				holder.view.setTextColor(Color.LTGRAY);
			}
		}
		return v;
	}

	static class ViewHolder {
		TextView view;
	}

}
