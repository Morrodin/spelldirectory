package com.zzoranor.spelldirectory.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.data.Spell;
import com.zzoranor.spelldirectory.database.DbAdapter;
import com.zzoranor.spelldirectory.database.DbAdapterFactory;

public class SingleSpell extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_spell);
		int id = this.getIntent().getIntExtra("single_spell.id", -1);
		String tag = "SingleSpell";
		String int_name = "";
		if(id == -1){
			int_name = this.getIntent().getStringExtra("single_spell.name");
		}
		
		
		String str;
		
        //SQLWrapper sql = new SQLWrapper(this);
        //sql.setDatabase();
        DbAdapter sql = DbAdapterFactory.getStaticInstance(this);
        sql.open();
        
        // Spell now has spell data info. 
        Spell sp;
        
    	sp = sql.getSpellData(id);
    	Log.d(tag,"Retrieving spell data from ID.");
        /*else
        {
        	sp = sql.getSpellDataByName(int_name);
        	Log.d(tag,"Retrieving spell data from ID.");
        }
         */
        
        // Print Spell to Log for debug info. 
        Log.d("SQL",sp.toString());
        
        // Name
        TextView v = (TextView) findViewById(R.id.spell_name);
		v.setText(sp.getData(Spell.NAME));
        
		// School
		v = (TextView) findViewById(R.id.spell_school);
		v.setText(sp.getData(Spell.SCHOOL));
		
		// Subschool
		v = (TextView) findViewById(R.id.spell_sub_school);
		str = sp.getData(Spell.SUBSCHOOL);
		if(!calcVisibility(R.id.spell_sub_school, str)){
			v.setText(str);
		}
		
		// Descriptor
		v = (TextView) findViewById(R.id.spell_descriptor);
		str = sp.getData(Spell.DESCRIPTOR);
		if(!calcVisibility(R.id.spell_descriptor, str)){
			v.setText(str);
		}
		
        // Lvl
		ArrayList<Pair<String, Integer> > arr = sp.getClassLevels();
		v = (TextView) findViewById(R.id.spell_lvl);
		str = ""; 
		for(int i = 0; i < arr.size();i++){
			Pair<String, Integer> p = arr.get(i);
			str += p.first + " " + p.second + ",";
			if((i+1) % 3 == 0){
				str += "\n";
			}
		}
		str = str.substring(0,str.length()-1);
		str = str.toLowerCase();
		v.setText(str);
			        
        // Cast Time
		v = (TextView) findViewById(R.id.spell_casting_time);
		v.setText(sp.getData(Spell.CASTING_TIME));
        
		// Components
		v = (TextView) findViewById(R.id.spell_components);
		v.setText(sp.getData(Spell.COMPONENTS));
        
		// Range
        v = (TextView) findViewById(R.id.spell_range);
		v.setText(sp.getData(Spell.RANGE));
        
		// Area
		v = (TextView) findViewById(R.id.spell_area);
		str = sp.getData(Spell.AREA);
		if(!calcVisibility(R.id.spell_area, str)){
			v.setText(str);
		}
		// Target
		v = (TextView) findViewById(R.id.spell_target);
		str = sp.getData(Spell.TARGET);
		if(!calcVisibility(R.id.spell_target, str)){
			v.setText(str);
		}
        
		// Duration
		v = (TextView) findViewById(R.id.spell_duration);
		v.setText(sp.getData(Spell.DURATION));
        
		// Save
		v = (TextView) findViewById(R.id.spell_save);
		str = sp.getData(Spell.SAVINGTHROW);
		if(!calcVisibility(R.id.spell_save, str)){
			v.setText(str);
		}
        
		// Spell Resist
		v = (TextView) findViewById(R.id.spell_spell_resist);
		str = sp.getData(Spell.SPELL_RESISTANCE);
		if(!calcVisibility(R.id.spell_spell_resist, str)){
			v.setText(str);
		}
		
		// Spell Source
		v = (TextView) findViewById(R.id.spell_source);
		str = sp.getData(Spell.SPELL_SOURCE);
		if(!calcVisibility(R.id.spell_source, str)){
			v.setText(str);
		}
		
		// Effect
		v = (TextView) findViewById(R.id.spell_effect);
		str = sp.getData(Spell.EFFECT);
		if(!calcVisibility(R.id.spell_effect, str)){
			v.setText(str);
		}
		
		// Description
        v = (TextView) findViewById(R.id.spell_description);
		v.setText(sp.getData(Spell.DESCRIPTION));
	}
		
	
	@Override
	protected void onResume() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onResume();
	}
	
	/**
	 * 
	 * @param id Id of the specified 
	 * @param str The string to check against. 
	 * @return true if Layout was turned invisible(View.GONE). False if it was turned visible. 
	 */
	private boolean calcVisibility(int id, String str){
		View v = findViewById(id);
		LinearLayout ll = (LinearLayout) v.getParent();
		if(str == null || str.equals("") || str.equals("null") || str.equals("NULL")){
			ll.setVisibility(View.GONE);
			ll.invalidate();
			return true;
		}else{
			ll.setVisibility(View.VISIBLE);
			ll.invalidate();
			return false;
		}
	}
}
