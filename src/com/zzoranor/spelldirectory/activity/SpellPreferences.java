package com.zzoranor.spelldirectory.activity;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.zzoranor.spelldirectory.R;

public class SpellPreferences extends PreferenceActivity implements OnPreferenceChangeListener {
	
	private CheckBoxPreference clericOracle;
	private CheckBoxPreference defaultToLongClickAction;
	private ListPreference prepList;
	private ListPreference spellList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		clericOracle = (CheckBoxPreference) getPreferenceManager().findPreference("checkbox_clericOracle");
		defaultToLongClickAction = (CheckBoxPreference) getPreferenceManager().findPreference("checkbox_defaultToLongclickAction");
		defaultToLongClickAction.setOnPreferenceChangeListener(this);
		prepList = (ListPreference) getPreferenceManager().findPreference("preplist_defaultLongclickAction");
		spellList = (ListPreference) getPreferenceManager().findPreference("spelllist_defaultLongclickAction");
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals("checkbox_defaultToLongclickAction"))
		{
			boolean value = (Boolean) newValue;
			if(value)
			{
				prepList.setEnabled(true);
				spellList.setEnabled(true);
			}else
			{
				prepList.setEnabled(false);
				spellList.setEnabled(false);
			}
		}
		return true;
	}
	
	
}
