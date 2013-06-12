package com.zzoranor.spelldirectory.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.zzoranor.spelldirectory.CharacterLabel;
import com.zzoranor.spelldirectory.ClassLabel;
import com.zzoranor.spelldirectory.SpellLabel;
import com.zzoranor.spelldirectory.data.Character;
import com.zzoranor.spelldirectory.data.Metamagics;
import com.zzoranor.spelldirectory.data.Spell;
import com.zzoranor.spelldirectory.util.Constants;
import com.zzoranor.spelldirectory.util.FileHelper;
import com.zzoranor.spelldirectory.util.Triple;
import com.zzoranor.spelldirectory.xml.XMLCharacterData;
import com.zzoranor.spelldirectory.xml.XMLParser;
import com.zzoranor.spelldirectory.xml.XMLSpellData;
import com.zzoranor.spelldirectory.xml.XMLSpellKnownData;

/**
 * Database Adapter class, wrapping the SQL code and providing a host of functionality
 * used by the whole application. 
 * @author Zoranor
 *
 */
public class DbAdapter {
	
	

	private static final String TAG = "SQL";
	private static DatabaseHelper mDbHelper;
	private static DatabaseHelper mDynDHelper;
	private static SQLiteDatabase db; // Static
	private static SQLiteDatabase dyndb; // Dynamic
	private Context context;

	private static final boolean DEBUG = false;
	
	private static boolean dbIsOpen = false;
	private static final String DYNAMIC_DATABASE_NAME = "dynamic_data.db3";
	private static final String STATIC_DATABASE_NAME = "spells.db3";
	private static final String COMPILED_DATABASE_NAME = "spells_core_magic_combat.mp3";
	private static final int DATABASE_VERSION = 14;

	//////////////////
	// Table Names  //
	//////////////////
	
	// Static
	//private static final String STA_SPELL_DATA_NAME = "spell_data";
	private static final int STA_SPELL_DATA = 0;
	//private static final String STA_SPELL_LVLS_NAME = "spell_lvls";
	private static final int STA_SPELL_LVLS = 1;
	//private static final String STA_CLASS_NAMES_NAME = "class_names";
	private static final int STA_CLASS_NAMES = 2;
	//private static final String STA_METAMAGIC_NAME = "metamagic";
	private static final int STA_METAMAGIC = 3;
	private static final String[] STA_NAMES = new String[]{"spell_data","spell_lvls","class_names","metamagic"};
	
	// Dynamic
	//private static final String DYN_SPELLS_KNOWN_NAME = "spells_known";
	private static final int DYN_SPELLS_KNOWN = 10;
	//private static final String DYN_CHARACTERS_NAME = "characters";
	private static final int DYN_CHARACTERS = 11;
	//private static final String DYN_PREPARED_SPELLS_NAME = "character_spells";
	private static final int DYN_PREP_SPELLS = 12;
	private static final String[] DYN_NAMES = new String[]{"spells_known","characters", "character_spells"};
	
	private static final String FOLDER_NAME = "spelldirectory";

	public static final int NAME_EXISTS_ERROR = -2;
	public static final int KNOWN_SPELLS_CLASS_ID = 99999;

	public static boolean DB_ON_SDCARD = false;

	private static final boolean emulator = false;

	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	Handler progressHandler;
	//Classes classes;
	Metamagics metamagics;
		
	/*
	 * private static final String CREATE_SPELL_DATA_TABLE =
	 * "CREATE TABLE spell_data (_id INTEGER PRIMARY KEY ASC, spell_name TEXT, spell_school TEXT,"
	 * +
	 * "spell_subschool TEXT, spell_descriptor TEXT, spell_casttime TEXT, spell_components TEXT, spell_range TEXT, spell_area TEXT, spell_target TEXT, "
	 * +
	 * "spell_duration TEXT, spell_savingthrow TEXT, spell_resistance TEXT, spell_description TEXT, spell_effect TEXT);"
	 * ; private static final String CREATE_CLASS_NAMES_TABLE =
	 * "CREATE TABLE class_names (_id INTEGER PRIMARY KEY ASC, class_name TEXT);"
	 * ; private static final String CREATE_SPELL_LVLS_TABLE = "" +
	 * "CREATE TABLE spell_lvls (" + "spell_id INTEGER," + "spell_lvl INTEGER,"
	 * + "class_id INTEGER," +
	 * "FOREIGN KEY(spell_id) REFERENCES spell_data(spell_id)," +
	 * "FOREIGN KEY(class_id) REFERENCES class_names(class_id))";
	 */
	private static final String CREATE_CHARCTER_SPELLS = ""
			+ "CREATE TABLE character_spells (" 
			+ "char_id INTEGER,"
			+ "spell_id INTEGER," 
			+ "spell_name TEXT," 
			+ "class_id INTEGER, "
			+ "spell_known INTEGER," 
			+ "spell_prepared INTEGER,"
			+ "spell_prepared_lvl INTEGER," 
			+ "spell_used INTEGER, "
			+ "FOREIGN KEY(char_id) REFERENCES characters(char_id))";

	private static final String CREATE_CHARACTER_TABLE = ""
			+ "CREATE TABLE characters (" 
			+ " char_id INTEGER,"
			+ " char_name TEXT," 
			+ " chosen_class TEXT," 
			+ " class_id TEXT)";
	
	private static final String CREATE_SPELLS_KNOWN_TABLE = ""
			+ "CREATE TABLE spells_known (" 
			+ " char_id INTEGER,"
			+ " spell_id INTEGER, "
			+ " spell_lvl INTEGER,"
			+ " known_type INTEGER)";

	private static class DatabaseHelper extends SQLiteOpenHelper {
		boolean dynamic;

		DatabaseHelper(Context context, String dbfile, boolean dynamic) {
			super(context, dbfile, null, DATABASE_VERSION);
			this.dynamic = dynamic;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (dynamic) {
				db.execSQL(CREATE_CHARACTER_TABLE);
				db.execSQL(CREATE_CHARCTER_SPELLS);
				db.execSQL(CREATE_SPELLS_KNOWN_TABLE);
			}

		}

		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ".");
			
			// Switch works as follows: 
			//	As new database versions are added, add those below the previous version.
			// Important: Do not break between the cases. 
			// Eg:
			//	old = 10;
			//	new = 14;
			//	switch(old)
			//	{
			//		case 10:
			//			// Changes for version 10=>11.
			//		case 11:
			//			// Changes for version 11=>12
			//		case 12:
			//			// Changes for version 12=>13
			//		case 13:
			//			// Changes for version 13=>14
			//		break;	// Break to avoid the default case where the chain of update does not hold. 
			//
			//	}
			
			if(newVersion > 13)
			{
				switch (oldVersion) {
				case 13:
					Log.w(TAG, "Upgrading database 13 => 14. Add SPELLS_KNOWN table. ");
					db.execSQL(CREATE_SPELLS_KNOWN_TABLE);
					break;
	
					
				default:
					Log.w(TAG, "Upgrading between widely differing databases. New: " + newVersion + " Old: " + oldVersion);
					Log.w(TAG, "All data will be deleted. ");
					db.execSQL("DROP TABLE IF EXISTS character_spells");
					db.execSQL("DROP TABLE IF EXISTS characters");
					onCreate(db);
					break;
				}
			}
		}
		
	}

	public DbAdapter(Context ctx) {
		this.context = ctx;
		DB_ON_SDCARD = isInstalledOnSDCard();
		//classes = Classes.getInstance();
		metamagics = new Metamagics();
	}

	public DbAdapter(Context ctx, HashMap<String, Integer> map) {
		DB_ON_SDCARD = isInstalledOnSDCard();
		this.context = ctx;
		//classes = Classes.getInstance();
		metamagics = new Metamagics();
	}
	
	


	public void initStaticDB() {
		File sdcard = null;
		String dbfileName = "";
		AssetManager assets = context.getAssets();
		try {

			InputStream is = assets.open("db" + File.separator
					+ COMPILED_DATABASE_NAME);

			if (DB_ON_SDCARD) {
				Log.d("SQL", "Database should be on SD card. ");
				sdcard = Environment.getExternalStoragePublicDirectory(FOLDER_NAME);
				Log.d("SQL",sdcard.getAbsolutePath());
				//sdcard = context.getExternalFilesDir(null);// Environment.getExternalStorageDirectory();

				if (!sdcard.exists()) {
					sdcard.mkdir();
				}

				dbfileName = sdcard.getAbsolutePath() + File.separator + STATIC_DATABASE_NAME;
				File dbFile = new File(dbfileName);
				if (dbFile.exists() && dbFile.canWrite()) {
					Log.d("SQL", "STATIC database: " + dbfileName
							+ " EXISTS and can be WRITTEN TO. OK");
				}
				if (!dbFile.exists()) {
					Log.d("SQL", "STATIC database: " + dbfileName
							+ " does not exist, create it. ");
					dbFile.createNewFile();
				}

				Log.d("SQL",
						"STATIC DATABASE should be fine now. dbFile.exists() = "
								+ dbFile.exists());

				Log.d("SQL", "Trying to copy STATIC database to: "
						+ dbfileName);
				OutputStream os = new FileOutputStream(dbfileName);
				FileHelper.copyFile(is, os);

				mDbHelper = new DatabaseHelper(context, dbfileName, false);
				db = mDbHelper.getWritableDatabase();
			} else {
				Log.d("SQL", "Database should be in internal memory");
				
				File path = context.getDir(FOLDER_NAME, 0);
				File dbFile = new File(path, STATIC_DATABASE_NAME);
				dbfileName = path.getAbsolutePath() + File.separator
						+ STATIC_DATABASE_NAME;
				if (dbFile.exists() && dbFile.canWrite()) {
					Log.d("SQL", "STATIC database: " + dbfileName
							+ " EXISTS and can be WRITTEN TO. OK");
				}
				if (!dbFile.exists()) {
					Log.d("SQL", "STATIC database: " + dbfileName
							+ " does not exist, create it. ");
					dbFile.createNewFile();
				}

				Log.d("SQL",
						"STATIC DATABASE should be fine now. dbFile.exists() = "
								+ dbFile.exists());

				OutputStream os = new FileOutputStream(dbFile);
				FileHelper.copyFile(is, os);

				Log.d("SQL", "dbfileName = " + dbfileName);
				mDbHelper = new DatabaseHelper(context, dbfileName, false);
				db = mDbHelper.getWritableDatabase();
			}
			
			addKnownSpellsClass();
		} catch (FileNotFoundException e) {
			Log.d("SQL", "The following file was not found: " + dbfileName);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public DbAdapter open() throws SQLException {
		
		File storageDir = null;
		String dbfile = "";
		
		if(!dbIsOpen)
		{
			initStaticDB();
			
			if (DB_ON_SDCARD) {
				storageDir = Environment.getExternalStoragePublicDirectory("spelldirectory");//Environment.getExternalStorageDirectory();
			} else {
				storageDir = context.getDir(FOLDER_NAME, 0);
			}

			if (!storageDir.exists()) {
				storageDir.mkdir();
			}

			dbfile = storageDir.getAbsolutePath() + File.separator
					+ DYNAMIC_DATABASE_NAME;
			Log.d("SQL", "Trying to create DYNAMIC database at: " + dbfile);
			mDynDHelper = new DatabaseHelper(context, dbfile, true);

			dyndb = mDynDHelper.getWritableDatabase();
			
			dbIsOpen = true;
			Log.d("SQL", "DYNAMIC DATABASE has version: " + dyndb.getVersion());
			Log.d("SQL", "STATIC DATABASE has version: " + db.getVersion());
		}

		return this;
	}

	// Close databases. 
	public void close() {
		if (mDbHelper != null)
			mDbHelper.close();
		if (mDynDHelper != null)
			mDynDHelper.close();
		if (db != null)
			db.close();
		if (dyndb != null)
			dyndb.close();
	}
	
	public void addKnownSpellsClass()
	{
		
		SpellDBSet set = getFieldsFromTable(db, getTableName(STA_CLASS_NAMES), new WhereSet(Constants.SHORT_ID, KNOWN_SPELLS_CLASS_ID), null, null);

		if(set.getNumRows() == 0)	// Check if the known_spells class exists already. 
		{
			ContentValues cv = new ContentValues();
			cv.put(Constants.SHORT_ID, KNOWN_SPELLS_CLASS_ID);
			cv.put(Constants.CLASS_NAME, "KNOWN SPELLS");
			insertIntoTable(db, getTableName(STA_CLASS_NAMES), cv);
		}
	}

	

	/**
	 * Returns a list of spell-labels for use in a ListActivity. These
	 * SpellLabels have no prepared/used values, these needs to be added
	 * by the calling function by using SpellLabel.setPrepared(Pair<Integer,Integer>);
	 * @param classId
	 * @return
	 */
	public SpellLabel[] getSpellsFromList(int classId)
	{
		return getSpellsFromList(classId, null);
	}
	
	public SpellLabel[] getSpellsFromList(int classId, ArrayList<Triple> extraFilters)
	{
		WhereSet where = new WhereSet();
		where.add(Constants.CLASS_ID, classId);
		if(extraFilters != null)
		{
			for(int i = 0; i < extraFilters.size();i++)
			{
				where.add(extraFilters.get(i));
			}
		}
		
		SpellDBSet set = getFieldsFromMultipleTables(db, "spell_data inner join spell_lvls on spell_data._id = spell_lvls.spell_id",
				where, new String[]{addPrefix(STA_SPELL_DATA,Constants.SHORT_ID), Constants.NAME, Constants.SPELL_LVL, Constants.SCHOOL},
				Constants.SPELL_LVL + " ASC, " + Constants.NAME + " ASC");
		
		//set.printToLog(10);
		SpellLabel[] labels = new SpellLabel[set.getNumRows()];
		for(int i = 0; i < labels.length;i++)
		{
			labels[i] = new SpellLabel(set.get(Constants.NAME, i),set.getInt(Constants.SHORT_ID,i), set.getInt(Constants.SPELL_LVL, i), set.get(Constants.SCHOOL,i));
		}
		return labels;
	}
	
	public boolean hasCharacters() {
		SpellDBSet set = getFieldsFromTable(dyndb, getTableName(DYN_CHARACTERS), null, new String[]{Constants.CHAR_ID}, null, 1);
		if (set.getNumRows() > 0) {
			return true;
		} else {
			return false;
		}
	}
		
	
	public void resetAllUses(int charId) {
		//dyndb.execSQL("update character_spells set spell_used = spell_prepared where char_id = "+ char_id);
		removeFromTable(dyndb, DYN_PREP_SPELLS, WhereSet.charId(charId));
	}
	 
	
	public Spell getSpellData(int spellId)
	{
		SpellDBSet set = getFieldsFromTable(db, getTableName(STA_SPELL_DATA), WhereSet.spellId(true, spellId), null, null, 1);
		SpellDBSet lvlSet = getFieldsFromMultipleTables(db, "spell_lvls inner join class_names on spell_lvls.class_id = class_names._id"
				, WhereSet.spellId(false, spellId), new String[]{Constants.CLASS_NAME, Constants.SPELL_LVL}, Constants.SPELL_LVL + " ASC");
		Spell sp = new Spell(set, lvlSet);
		return sp;
	}
	
	public ClassLabel[] getClasses()
	{
		SpellDBSet set = getFieldsFromTable(db, getTableName(STA_CLASS_NAMES), null, new String[]{Constants.CLASS_NAME, Constants.SHORT_ID}, Constants.CLASS_NAME);
		ClassLabel[] labels = new ClassLabel[set.getNumRows()];
		for(int i = 0; i < set.getNumRows(); i++)
		{
			labels[i] = new ClassLabel(set.getInt(Constants.SHORT_ID, i),set.get(Constants.CLASS_NAME, i));
		}
		return labels;
	}
	
	/**
	 * Returns a matrix containing the spell data for prepared spells. The
	 * format is: String[rows+1][cols+1], with the first row being populated
	 * with column names for convenience.
	 * 
	 * @param char_id
	 * @return
	 */
	public String[][] getPreparedSpellDBData(int char_id) {
		Cursor c = dyndb.rawQuery(
				"select * from character_spells where char_id = " + char_id,
				null);

		c.moveToFirst();

		String[] colNames = c.getColumnNames();
		String[][] res;

		int rows = c.getCount();
		int cols = c.getColumnCount();

		res = new String[rows + 1][cols];

		// Add Column Names to Top Column.
		for (int i = 0; i < cols; i++) {
			res[0][i] = colNames[i];
		}

		for (int i = 1; i < rows + 1; i++) {
			for (int j = 0; j < cols; j++) {
				res[i][j] = c.isNull(j) ? "" : c.getString(j);
			}
			c.moveToNext();
		}

		c.close();
		return res;

	}
	
	public String[][] getKnownSpellDBData(int char_id)
	{
		Cursor c = dyndb.rawQuery(
				"select * from spells_known where char_id = " + char_id,
				null);

		c.moveToFirst();

		String[] colNames = c.getColumnNames();
		String[][] res;

		int rows = c.getCount();
		int cols = c.getColumnCount();

		res = new String[rows + 1][cols];

		// Add Column Names to Top Column.
		for (int i = 0; i < cols; i++) {
			res[0][i] = colNames[i];
		}

		for (int i = 1; i < rows + 1; i++) {
			for (int j = 0; j < cols; j++) {
				res[i][j] = c.isNull(j) ? "" : c.getString(j);
			}
			c.moveToNext();
		}

		c.close();
		return res;
	}
	

	public void populateMetamagicsFromDB() {
		SpellDBSet set = getFieldsFromTable(db, getTableName(STA_METAMAGIC), null, new String[]{Constants.META_NAME, Constants.META_ADJUST}, null);
		for(int i = 0; i < set.getNumRows(); i++)
		{
			metamagics.addMetamagic(set.get(Constants.META_NAME, i), set.getInt(Constants.META_ADJUST, i));
		}
	}
	
	

	public ArrayList<String> getMetamagicList() {
		return metamagics.getMetamagicList();

	}

	public int getMetamagicAdjustment(String name) {
		return metamagics.get(name);
	}

	/**
	 * Returns a matrix containing the spell data for prepared spells. The
	 * format is: String[rows+1][cols+1], with the first row being populated
	 * with column names for convenience.
	 * 
	 * @param char_id
	 * @return
	 */
	public String[][] getCharDBData(int char_id) {
		Cursor c = dyndb.rawQuery("select * from characters where char_id = "
				+ char_id, null);

		c.moveToFirst();

		String[] colNames = c.getColumnNames();
		String[][] res;

		int rows = c.getCount();
		int cols = c.getColumnCount();

		res = new String[rows + 1][cols];

		// Add Column Names to Top Column.
		for (int i = 0; i < cols; i++) {
			res[0][i] = colNames[i];
		}

		for (int i = 1; i < rows + 1; i++) {
			for (int j = 0; j < cols; j++) {

				String s = c.isNull(j) ? "" : c.getString(j);

				res[i][j] = s;
			}
			c.moveToNext();
		}

		c.close();
		return res;
	}
	
	
	public Character getFirstAvailCharacter()
	{
		Character ch = null;
		SpellDBSet set = getFieldsFromTable(dyndb, getTableName(DYN_CHARACTERS), null, new String[]{Constants.CHAR_ID, Constants.CHAR_NAME, Constants.CHAR_CHOSEN_CLASS_NAME, Constants.CLASS_ID}, null, 1);
		if(set.getNumRows() > 0)
		{
			ch = new Character(set.getInt(Constants.CHAR_ID), set.get(Constants.CHAR_NAME), set.get(Constants.CHAR_CHOSEN_CLASS_NAME), set.getInt(Constants.CLASS_ID));
			setCharPreparedSpellsFromDB(ch);
			Log.d(TAG, "getFirstAvailCharacter() returning char: " + ch);
			return ch;
		}else
		{
			return null;
		}
	}

	
	public Character getCharacterData(int charId)
	{
		Character ch = null;
		String[] selection = new String[]{Constants.CHAR_NAME, Constants.CHAR_CHOSEN_CLASS_NAME, Constants.CLASS_ID};
		SpellDBSet set = getFieldsFromTable(dyndb, getTableName(DYN_CHARACTERS), WhereSet.charId(charId), selection, null, 1);
		if(set.getNumRows() > 0)
		{
			ch = new Character(charId, set.get(Constants.CHAR_NAME), set.get(Constants.CHAR_CHOSEN_CLASS_NAME), set.getInt(Constants.CLASS_ID));
			setCharPreparedSpellsFromDB(ch);
		}
		return ch;
	}
	
	
	public void changeChosenClass(int charId, String chosenClass, int classId)
	{
		ContentValues cv = new ContentValues();
		cv.put(Constants.CHAR_CHOSEN_CLASS_NAME, chosenClass);
		cv.put(Constants.CLASS_ID, classId);
		updateTableFields(dyndb, getTableName(DYN_CHARACTERS), cv, WhereSet.charId(charId));
	}

	public int addCharacter(String charName)
	{
		SpellDBSet firstClass = getFieldsFromTable(db, getTableName(STA_CLASS_NAMES), null, new String[]{Constants.SHORT_ID, Constants.CLASS_NAME}, null, 1);
		String returnField = "max("+Constants.CHAR_ID+")";
		SpellDBSet maxIdSet = getFieldsFromTable(dyndb, getTableName(DYN_CHARACTERS), null, new String[]{returnField},null);
		
		int id;
		if(maxIdSet.getNumRows() > 0)
		{
			id = maxIdSet.getInt(returnField) + 1;
		}else
		{	// Empty Character database. We just pick 1 as charId then. 
			id = 1;
		}
		
		if(firstClass.getNumRows() > 0)
		{
			return addCharacter(id, charName, firstClass.getInt(Constants.SHORT_ID), firstClass.get(Constants.CLASS_NAME));
		}else
		{
			Log.w(TAG, "addCharacter() Trying to add a character when no classes exist. Aborting. ");
			return -1;
		}
	}
	
	public int addCharacter(int charId, String charName, int classId, String className)
	{
			ContentValues cv = new ContentValues();
			cv.put(Constants.CHAR_ID, charId);
			cv.put(Constants.CHAR_NAME, charName);
			cv.put(Constants.CHAR_CHOSEN_CLASS_NAME, className);
			cv.put(Constants.CLASS_ID, classId);
			return (int) insertIntoTable(dyndb, getTableName(DYN_CHARACTERS), cv);
	}
	
	
	public boolean renameCharacter(int charId, String newName)
	{
		ContentValues cv = new ContentValues();
		cv.put(Constants.CHAR_NAME, newName);
		return 0 < updateTableFields(dyndb, getTableName(DYN_CHARACTERS), cv, WhereSet.charId(charId));
	}

	public void useSpell(int charId, int spellId, String spellName)
	{
		String table = getTableName(DYN_PREP_SPELLS);
		WhereSet where = WhereSet.charId(charId);
		where.add(Constants.SPELL_ID, spellId);
		where.add(Constants.NAME, spellName);
		SpellDBSet set = getFieldsFromTable(dyndb, table, where, new String[]{Constants.PREP_USES_LEFT}, null, 1);
		if(set.getNumRows() > 0 && set.getInt(Constants.PREP_USES_LEFT) > 0)
		{
			ContentValues cv = new ContentValues();
			int usesLeft = set.getInt(Constants.PREP_USES_LEFT) -1;
			cv.put(Constants.PREP_USES_LEFT, usesLeft);
			updateTableFields(dyndb, table, cv, where);		   
		}
	}
	
	public boolean addPreparedSpell(int charId, int spellId, String spellName, int spellLvl, int classId, int spellKnown, int numUsesLeft, int numPrepared)
	{
		String table = getTableName(DYN_PREP_SPELLS);
		WhereSet where = WhereSet.charId(charId);
		where.add(Constants.SPELL_ID, spellId);
		where.add(Constants.NAME, spellName);
		SpellDBSet set = getFieldsFromTable(dyndb, table, where, new String[]{Constants.SPELL_ID}, null);
		ContentValues cv = new ContentValues();
		cv.put(Constants.PREP_USES_LEFT, numUsesLeft);
		cv.put(Constants.PREP_NUM_PREP, numPrepared);
		
		if(set.getNumRows() > 0)
		{
			// Spell exists in table, we should update. 
			return 0 != updateTableFields(dyndb, table, cv, where);	// return false if updateTableFields returns 0 updated rows. 
		}else
		{
			cv.put(Constants.CHAR_ID, charId);
			cv.put(Constants.SPELL_ID, spellId);
			cv.put(Constants.NAME, spellName);
			cv.put(Constants.CLASS_ID, classId);
			cv.put(Constants.PREP_LVL_PREP, spellLvl);
			cv.put(Constants.PREP_SPELL_KNOWN, Constants.SPELL_KNOWN_DEFAULT);
			cv.put(Constants.PREP_USES_LEFT, numUsesLeft);
			cv.put(Constants.PREP_NUM_PREP, numPrepared);
			return -1 != insertIntoTable(dyndb, table, cv);		// return false if insertIntoTable returns -1 as new id.   	
		}
	}
	
	
	public boolean addKnownSpell(int charId, int spellId, int spellLvl, int knownType)
	{
		boolean ret = true;
		WhereSet where = WhereSet.charId(charId);
		where.add(Constants.SPELL_ID, spellId);
		String tableName = getTableName(DYN_SPELLS_KNOWN);
		boolean exists = existsInTable(dyndb, tableName, where);
		
		ContentValues cv = new ContentValues();
		cv.put(Constants.CHAR_ID, charId);
		cv.put(Constants.SPELL_ID, spellId);
		cv.put(Constants.SPELL_LVL, spellLvl);
		cv.put(Constants.KNOWN_TYPE, knownType);
		if(exists)
		{
			updateTableFields(dyndb, tableName, cv, where);
		}else
		{
			insertIntoTable(dyndb, tableName, cv);
		}
		
		return ret;
	}
	
	public boolean addKnownSpell(int charId, int spellId, int spellLvl)
	{
		return addKnownSpell(charId, spellId, spellLvl, Constants.SPELL_KNOWN_DEFAULT);
	}
	
	
	
	public void removeAllPreparedSpells(int charId)
	{
		if(charId < 0)
		{
			Log.w(TAG, "removePreparedSpell() charId =" + charId + " < 0");
			return;
		}
		
		WhereSet where = WhereSet.charId(charId);
		removeFromTable(dyndb, DYN_PREP_SPELLS, where);
	}
	
	public void removePreparedSpell(int charId, int spellId, String spellName)
	{
		if(charId < 0)
		{
			Log.w(TAG, "removePreparedSpell() charId =" + charId + " < 0");
			return;
		}else if(spellId < 0)
		{
			Log.w(TAG, "removePreparedSpell() spellId =" + spellId + " < 0");
			return;
		}
		
		String table = getTableName(DYN_PREP_SPELLS);
		WhereSet where = WhereSet.charId(charId);
		where.add(Constants.SPELL_ID, spellId);
		where.add(Constants.NAME, spellName);
		SpellDBSet set = getFieldsFromTable(dyndb, table, where, new String[]{Constants.SPELL_ID, Constants.PREP_NUM_PREP, Constants.PREP_USES_LEFT}, null, 1);
		
		if(set.getNumRows() > 0)
		{
			if(set.getInt(Constants.PREP_NUM_PREP) > 1)
			{
				ContentValues cv = new ContentValues();
				cv.put(Constants.PREP_USES_LEFT, set.getInt(Constants.PREP_USES_LEFT));
				cv.put(Constants.PREP_NUM_PREP, set.getInt(Constants.PREP_NUM_PREP));
				updateTableFields(dyndb, table, cv, where);	
			}else
			{
				removeFromTable(dyndb, DYN_PREP_SPELLS, where);
			}
		}
	}
	
	public void removeSpellFromKnown(int charId, int spellId)
	{
		WhereSet where = WhereSet.charId(charId);
		where.add(Constants.SPELL_ID, spellId);
		removeFromTable(dyndb, DYN_SPELLS_KNOWN, where);
	}
	
	/**
	 * Removes the character from all dynamic tables. 
	 * @param charId
	 * @return
	 */
	public void deleteCharacter(int charId)
	{
		WhereSet where = WhereSet.charId(charId);
		removeFromTable(dyndb, DYN_CHARACTERS, where);
		removeFromTable(dyndb, DYN_PREP_SPELLS, where);
		removeFromTable(dyndb, DYN_SPELLS_KNOWN, where);
	}
	
	public int removeFromTable(SQLiteDatabase database, int table, WhereSet where)
	{
		int numRemoved = database.delete(getTableName(table), where.buildWhereSelection(), where.getArgs());
		
		Log.d(TAG, "removeItem() "+ numRemoved +" item(s) removed from table: " + getTableName(table) + " where " + where);
		return numRemoved;
	}
	
	
	/**
	 * Updates the table with the values as specified by the newValues parameter. 
	 * @param database 
	 * @param table
	 * @param newValues
	 * @param where
	 */
	public int updateTableFields(SQLiteDatabase database, String table, ContentValues newValues, WhereSet where)
	{
		int numUpdated;
		if(where == null)
		{
			where = new WhereSet();
		}
		
		numUpdated = database.update(table, newValues, where.buildWhereSelection(), where.getArgs());
		Log.d(TAG, "updateTable() "+ numUpdated +" item(s) updated in table: " + table + " where " + where);
		return numUpdated;
	}
	
	public long insertIntoTable(SQLiteDatabase database, String table, ContentValues values)
	{
		long idOfInserted = database.insert(table, null, values);

		Log.d(TAG, "insertIntoTable() to table: " +table+ " insert ID: " + idOfInserted);
		return idOfInserted;
	}
	
	
	/**
	 * Shorthand method for checking if a value exists in a single table. 
	 * @param database
	 * @param table
	 * @param where
	 * @return true if the value exists, false otherwise. 
	 */
	public boolean existsInTable(SQLiteDatabase database, String table, WhereSet where)
	{
		boolean exists;
		if(where == null || where.countWhere() == 0)
		{
			Log.e(TAG, "existsInTable() with empty or null WhereSet. Returning false. table: " + table);
			return false;
		}
		
		// Use the first where value as single selector to avoid returning a full row.
		String[] selector = new String[]{where.getWhere()[0]};
		
		Cursor c = database.query(table, selector,where.buildWhereSelection(), where.getArgs(), null, null, null);

		exists = c.moveToFirst();
		c.close();
		return exists;
	}
	
	
	public boolean isCharInDB(int charId)
	{
		return existsInTable(dyndb, getTableName(DYN_CHARACTERS), WhereSet.charId(charId));
	}
	
	/**
	 * New method to retrieve data from the database. One or a number of rows can be retrieved and saved in the SpellDBSet that
	 * is returned. 
	 * @param database
	 * @param table
	 * @param whereFields 
	 * @param whereArgs
	 * @param returnFields null to return all fields in the table. 
	 * @param orderBy
	 * @param limit A negative limit constitutes no limit clause. 
	 * @return A SpellDBSet containing the set returned by the query. 
	 */
	private SpellDBSet getFieldsFromTable(SQLiteDatabase database, String table, WhereSet where, String[] returnFields, String orderBy, int limit)
	{
		if(where == null)
		{
			where = new WhereSet();
		}
		
		String lim = limit < 1 ? null : "" + limit;
		
		Cursor c = database.query(table, returnFields, where.buildWhereSelection(), where.getArgs(), null, null, orderBy, lim);

		// Create a set, to return!
		SpellDBSet returnSet = new SpellDBSet(c);
		c.close();
		return returnSet;
	}
	
	private SpellDBSet getFieldsFromTable(SQLiteDatabase database, String table, WhereSet where, String[] returnFields, String orderBy)
	{
		return getFieldsFromTable(database, table, where, returnFields, orderBy, -1);
	}
	
	
	/**
	 * New method to retrieve data from the database. One or a number of rows can be retrieved and saved in the SpellDBSet that is returned. This
	 * method uses the rawQuery() method to allow a custom table clause for retrieving joined tables.  
	 * @param database
	 * @param tablesWithOnClause For example the string: "spells_known left outer join character_spells on spells_known.spell_id = character_spells.spell_id"
	 * @param where
	 * @param returnFields
	 * @param orderBy
	 * @return a SpellDBSet containing the returned set. 
	 */
	private SpellDBSet getFieldsFromMultipleTables(SQLiteDatabase database, String tablesWithOnClause, WhereSet where, String[] returnFields, String orderBy, int limit)
	{
		
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("select ");
		
		if(returnFields != null)
		{
			for(int i = 0; i < returnFields.length-1;i++)
			{
				sqlQuery.append(returnFields[i] + ", ");
			}
			sqlQuery.append(returnFields[returnFields.length-1]);
		}else
		{
			sqlQuery.append("* ");
		}
		
		sqlQuery.append(" from ");
		sqlQuery.append(tablesWithOnClause);	// Add the tables and on clause to be used. 
		if(where.getWhere() != null)
		{
			sqlQuery.append(" where ");
			sqlQuery.append(where.buildWhereSelection());
		}
		
		if(orderBy != null)
		{
			sqlQuery.append(" order by ");
			sqlQuery.append(orderBy);
		}
		
		if(limit != -1)
		{
			sqlQuery.append(" limit " + limit);
		}
		
		Log.d(TAG, "getFieldsFromMultipleTables() sqlQuery = " + sqlQuery.toString());
		String str = ""; 
		
		//Log.d(TAG, "getFieldsFromMultipleTables() where = " + );
		
		
		Cursor c = database.rawQuery(sqlQuery.toString(), where.getArgs());
		
		
		// Create a set, to return!
		SpellDBSet returnSet = new SpellDBSet(c);
		c.close();
		return returnSet;

	}
	
	public SpellDBSet getFieldsFromMultipleTables(SQLiteDatabase database, String tablesWithOnClause, WhereSet where, String[] returnFields, String orderBy)
	{
		return getFieldsFromMultipleTables(database, tablesWithOnClause, where, returnFields, orderBy, -1);
	}
	
	private String getTableName(int id)
	{
		if(id < 10)
		{
			return STA_NAMES[id];
		}else
		{
			return DYN_NAMES[id-10];
		}
	}
	
	private String addPrefix(int table, String field)
	{
		return getTableName(table) + "." + field;
	}
	
	public SpellLabel[] getKnownSpells(int charId)
	{
		WhereSet where = new WhereSet(addPrefix(DYN_SPELLS_KNOWN, Constants.CHAR_ID), charId);
		String[] returnFields = new String[]{
				addPrefix(DYN_SPELLS_KNOWN,Constants.SPELL_ID), 
				addPrefix(DYN_SPELLS_KNOWN,Constants.SPELL_LVL), 
				addPrefix(DYN_PREP_SPELLS, Constants.PREP_NUM_PREP), 
				addPrefix(DYN_PREP_SPELLS, Constants.PREP_USES_LEFT)};
		
		SpellDBSet set = getFieldsFromMultipleTables(dyndb, "spells_known left outer join character_spells " +
				"on spells_known.spell_id = character_spells.spell_id", where, returnFields, null);
		
		SpellLabel[] labels = new SpellLabel[set.getNumRows()];
		for(int i = 0; i < set.getNumRows(); i++)
		{
			int spell_id = set.getInt(Constants.SPELL_ID, i);
			labels[i] = new SpellLabel(getSpellField(Constants.NAME,spell_id),spell_id, set.getInt(Constants.SPELL_LVL,i),getSpellField(Constants.SCHOOL, spell_id), set.getInt(Constants.PREP_USES_LEFT, i), set.getInt(Constants.PREP_NUM_PREP, i));
		}
		return labels;
	}
	
	public SpellDBSet getPreparedSpells(int charId)
	{
		WhereSet where = WhereSet.charId(charId);
		SpellDBSet set = getFieldsFromTable(dyndb, getTableName(DYN_PREP_SPELLS), where, null, null);
		//set.printToLog();
		return set;
	}
	
	public void setCharPreparedSpellsFromDB(Character character)
	{
		character.removeAll();
		SpellDBSet set = getPreparedSpells(character.getCharId());
		if(set.getNumRows() > 0)
		{
			Log.d(TAG, "_setCharPreparedSpellsFromDB() preparing " + set.getNumRows() + " spells for character: " + character);
			for(int i = 0; i < set.getNumRows(); i++)
			{
				int id = set.getInt(Constants.SPELL_ID,i);
				WhereSet where = new WhereSet(Constants.SHORT_ID, id);
				// Get school from the static database.
				SpellDBSet schoolSet = getFieldsFromTable(db, getTableName(STA_SPELL_DATA), where, new String[]{Constants.SCHOOL}, null, 1);
				//schoolSet.printToLog();
				String school = schoolSet.get(Constants.SCHOOL);
				
				// Create label to be used in the list. 
				SpellLabel label = new SpellLabel(set.get(Constants.NAME, i), id, set.getInt(Constants.PREP_LVL_PREP,i),school,
						set.getInt(Constants.PREP_USES_LEFT, i), set.getInt(Constants.PREP_NUM_PREP, i));
				
				// Prepare the spell. 
				character.prepareSpell(label, set.getInt(Constants.PREP_USES_LEFT, i), set.getInt(Constants.PREP_NUM_PREP, i));
			}
		}
	}
	
	public SpellLabel[] getPreparedSpellsFromList(int charId, ArrayList<Triple> extraFilters)
	{
		SpellDBSet set = getPreparedSpells(charId);
		SpellLabel[] labels = new SpellLabel[set.getNumRows()];
		if(set.getNumRows() > 0)
		{
			Log.d(TAG, "getPreparedSpellsFromList() preparing " + set.getNumRows() + " spells for charId: " + charId);
			for(int i = 0; i < set.getNumRows(); i++)
			{
				int id = set.getInt(Constants.SPELL_ID,i);
				WhereSet where = new WhereSet(Constants.SHORT_ID, id);
				// Get school from the static database.
				SpellDBSet schoolSet = getFieldsFromTable(db, getTableName(STA_SPELL_DATA), where, new String[]{Constants.SCHOOL}, null, 1);
				//schoolSet.printToLog();
				String school = schoolSet.get(Constants.SCHOOL);
				
				// Create label to be used in the list. 
				SpellLabel label = new SpellLabel(set.get(Constants.NAME, i), id, set.getInt(Constants.PREP_LVL_PREP,i),school,
						set.getInt(Constants.PREP_USES_LEFT, i), set.getInt(Constants.PREP_NUM_PREP, i));
				labels[i] = label;
			}
		}
		
		return labels;

	}
	
	
	/**
	 * Returns a field from the SPELL_DATA table. 
	 * @param field
	 * @param spellId
	 * @return
	 */
	public String getSpellField(String field, int spellId) {
		WhereSet where = new WhereSet(Constants.SHORT_ID, spellId);
		SpellDBSet set = getFieldsFromTable(db, getTableName(STA_SPELL_DATA),where, new String[]{field}, null);
		return set.get(field);
	}
		
	public void getCharacters(ArrayList<CharacterLabel> list) {
		SpellDBSet charSet = getFieldsFromTable(dyndb, getTableName(DYN_CHARACTERS), null, null, null);
		for(int i = 0; i < charSet.getNumRows(); i++)
		{
			CharacterLabel cl = new CharacterLabel(charSet.getInt(Constants.CHAR_ID, i),charSet.get(Constants.CHAR_NAME, i));
			list.add(cl);
		}
	}
	
	public void clearDynamicDatabase() {
		for(int i = 0; i < DYN_NAMES.length;i++)
		{
			dyndb.delete(DYN_NAMES[i], null, null);
		}
	}
	
	
	
	public boolean isInstalledOnSDCard() {
		PackageManager pm = context.getPackageManager();
		boolean res;
		try {
			PackageInfo pi = pm.getPackageInfo("com.zzoranor.spelldirectory",
					0);
			ApplicationInfo ai = pi.applicationInfo;
			// this only works on API level 8 and higher (check that first)
			// Toast.makeText(context, "Value of FLAG_EXTERNAL_STORAGE:" +
			// ((ai.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) ==
			// ApplicationInfo.FLAG_EXTERNAL_STORAGE),Toast.LENGTH_LONG).show();
			res = ((ai.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE);
			Log.d("SQL", "Value of FLAG_EXTERNAL_STORAGE:" + res);
			return res;
		} catch (NameNotFoundException e) {
			// do something
		}
		return false;
	}
	
	public boolean readBackupFile(String filePath, String fileName) {
		boolean success = false;
		updateExternalStorageState();
		
		if (mExternalStorageAvailable) {
			File path = new File(filePath);

			if (path.canRead()) {
				File file = new File(path, fileName);
				XMLParser parser = new XMLParser();
				parser.parseFile(file);
				ArrayList<XMLCharacterData> list = parser.getCharacterData();

				// Clear dynamic database.
				clearDynamicDatabase();

				for (int i = 0; i < list.size(); i++) {
					// Character Information
					XMLCharacterData character = list.get(i);
					addCharacter(character.char_id, character.char_name, character.class_id, character.chosen_class);

					// Prepared Spells
					for (int j = 0; j < character.preparedSpells.length; j++) {
						XMLSpellData sp = character.preparedSpells[j];
						// TODO: Fix Prefix addition for metadata.
						addPreparedSpell(sp.char_id, sp.spell_id, sp.spell_name, sp.spell_prepared_lvl,
								sp.class_id, sp.spell_known, sp.spell_used, sp.spell_prepared);
						//addPreparedSpellByName(sp.char_id, sp.class_id, "", sp.spell_name, sp.spell_prepared, sp.spell_prepared_lvl, sp.spell_used);
					}
					// Known Spells
					if(character.knownSpells != null)
					{
						for(int j = 0; j < character.knownSpells.length;j++)
						{
							XMLSpellKnownData sp = character.knownSpells[j];
							//addSpellAsKnown(sp.char_id, sp.spell_id, sp.spell_lvl, sp.known_type);
							addKnownSpell(sp.char_id, sp.spell_id, sp.spell_lvl, sp.known_type);
						}
					}
				}
				success = true;
			} else {
				success = false;
			}
		} else {
			success = false;

		}
		return success;
	}
	
	void updateExternalStorageState() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}

	public String backupCharactersToFile(int[] char_ids, boolean append,
			String filePath, String fileName) {
		String result = "";

		Log.d("XML", "Trying to Backup Characters to file: " + filePath + "/"
				+ fileName);
		updateExternalStorageState();
		if (mExternalStorageWriteable) {

			File path = new File(filePath);
			/*
			 * if(emulator) { path = new File("/mnt/sdcard"); }else { path =
			 * Environment
			 * .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS
			 * ); }
			 */
			if (!path.exists()) {
				path.mkdirs();
			}

			if (path.canWrite()) {
				File file = new File(path, fileName);
				try {

					FileWriter fw = new FileWriter(file, append);
					BufferedWriter bw = new BufferedWriter(fw);

					bw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
					bw.write("<character_list>\n");

					for (int k = 0; k < char_ids.length; k++) {
						int char_id = char_ids[k];

						String[][] charData = getCharDBData(char_id);
						String[][] spellData = getPreparedSpellDBData(char_id);
						String[][] knownSpellData = getKnownSpellDBData(char_id);

						bw.write("\t<character>\n");
						bw.write("\t\t<char_data ");
						for (int i = 1; i < charData.length; i++) {

							for (int j = 0; j < charData[0].length; j++) {
								// Attribute Start.
								bw.write("" + charData[0][j] + " = \"");
								// The Value.
								bw.write("" + charData[i][j]);
								// Attribute end.
								bw.write("\" ");
							}
						}
						bw.write(" />\n");
						// Start prepared Spells.
						bw.write("\t\t<prepared_spells>\n");
						for (int i = 1; i < spellData.length; i++) {
							bw.write("\t\t\t<spell ");
							for (int j = 0; j < spellData[0].length; j++) {
								bw.write("" + spellData[0][j] + " = \"");
								bw.write("" + spellData[i][j]);
								bw.write("\" ");
							}

							bw.write(" />\n");
						}
						bw.write("\t\t</prepared_spells>\n");
						// End Prepared Spells.
						
						
						// Start Known Spells
						bw.write("\t\t<known_spells>\n");
						for (int i = 1; i < knownSpellData.length; i++) {
							bw.write("\t\t\t<kspell ");
							for (int j = 0; j < knownSpellData[0].length; j++) {
								bw.write("" + knownSpellData[0][j] + " = \"");
								bw.write("" + knownSpellData[i][j]);
								bw.write("\" ");
							}

							bw.write(" />\n");
						}
						bw.write("\t\t</known_spells>\n");
						// End Known Spells.
						bw.write("\t</character>\n");

						result = file.getPath();

						bw.flush();
					}

					bw.write("</character_list>\n");
					bw.flush();
					fw.close();

				} catch (IOException e) {
					result = "";
					e.printStackTrace();
				}
			} else {
				result = "";
			}
		} else {
			Log.d("XML", "mExternalStorageWriteable = "
					+ mExternalStorageWriteable);
			Log.d("XML", "mExternalStorageAvailable = "
					+ mExternalStorageAvailable);
			result = "";

		}

		return result;
	}
}
