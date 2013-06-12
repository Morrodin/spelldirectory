package com.zzoranor.spelldirectory.util;

public abstract class Constants {
	
	// SharedPreferences constants. 
	public static final String PREFS_NAME = "SpellDir";
	public static final String PREFS_HAS_SEEN_RECENT_CHANGES = "hasSeenRecentChanges";

	//////////////////
	// DB Constants //
	//////////////////
	
	// Defaults
	public static final int SPELL_KNOWN_DEFAULT = 0;
	
	// Data
	public static final String SHORT_ID = "_id";
	public static final String SPELL_ID = "spell_id";
	public static final String NAME = "spell_name";
	public static final String SCHOOL = "spell_school";
	public static final String SUBSCHOOL = "spell_subschool";
	public static final String DESCRIPTOR = "spell_descriptor";
	public static final String CASTTIME = "spell_casttime";
	public static final String COMPONENTS = "spell_components";
	public static final String RANGE = "spell_range";
	public static final String AREA = "spell_area";
	public static final String TARGET = "spell_target";
	public static final String DURATION = "spell_duration";
	public static final String SAVINGTHROW = "spell_savingthrow";
	public static final String RESISTANCE = "spell_resistance";
	public static final String SOURCE = "spell_source";
	public static final String DESCRIPTION = "spell_description";
	public static final String EFFECT = "spell_effect";
	
	// Character
	public static final String CHAR_ID = "char_id";
	public static final String CHAR_NAME = "char_name";
	public static final String CHAR_CHOSEN_CLASS_NAME = "chosen_class";
	
	// Class
	public static final String CLASS_ID = "class_id";
	public static final String CLASS_NAME = "class_name";
	
	// Meta
	public static final String META_NAME = "meta_name";
	public static final String META_ADJUST = "meta_adjust";
	public static final String META_PREREQ = "meta_prereq";
	public static final String META_BENEFIT = "meta_benefit";
	public static final String META_SOURCE = "meta_source";
	
	// Prepared
	public static final String PREP_SPELL_KNOWN = "spell_known";	// Not really used...
	public static final String PREP_NUM_PREP = "spell_prepared";
	public static final String PREP_LVL_PREP = "spell_prepared_lvl";
	public static final String PREP_USES_LEFT = "spell_used";
	
	// Lvl
	public static final String SPELL_LVL = "spell_lvl";
	
	// Known
	public static final String KNOWN_TYPE = "known_type";
	
	// Request codes
	public static final int SPELL_PREFERENCES = 40;
	
	// LongClick
	// Change in Strings should these values be changed.. 
	public static final int LONG_CLICK_PREPARE = 1;
	public static final int LONG_CLICK_REMOVE_PREPARED = 2;
	public static final int LONG_CLICK_USE_SPELL = 3;
	public static final int LONG_CLICK_METAMAGIC = 4;
	public static final int LONG_CLICK_KNOWN_SPELL = 5;
	public static final int LONG_CLICK_REMOVE_KNOWN_SPELL = 6;
	
	//Preferences
	public static final String PREF_CLERIC_ORACLE = "checkbox_clericOracle";
	public static final String PREF_DEFAULT_TO = "checkbox_defaultToLongclickAction";
	public static final String PREF_PREPLIST = "preplist_defaultLongclickAction";
	public static final String PREF_SPELLLIST = "spelllist_defaultLongclickAction";
	
	
}
