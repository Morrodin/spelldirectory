package com.zzoranor.spelldirectory.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.zzoranor.spelldirectory.CharacterLabel;
import com.zzoranor.spelldirectory.CustomCharacterAdapter;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.TabMain;
import com.zzoranor.spelldirectory.controllers.MainDrawerController;
import com.zzoranor.spelldirectory.data.Character;
import com.zzoranor.spelldirectory.database.DbAdapter;
import com.zzoranor.spelldirectory.database.DbAdapterFactory;
import com.zzoranor.spelldirectory.util.Constants;

public class CharacterList extends ListActivity implements OnClickListener, android.content.DialogInterface.OnCancelListener{
	
	public static boolean EMULATOR = false;

    private MainDrawerController mDrawerController;

	DbAdapter sql;
	final ArrayList<CharacterLabel> character_labels = new ArrayList<CharacterLabel>();
	Context context;
	CustomCharacterAdapter adapter;
	public static Character chosenCharacter;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	
	private Dialog create_dialog;
	private Dialog edit_dialog;
	private Dialog backup_dialog;
	private Dialog restore_dialog;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final String DEFAULT_CHARACTER_NAME = "Default";
	
	private static final int EDIT_DIALOG = 0;
	private static final int CREATE_DIALOG = 1;
	private static final int DELETE_CHARACTER_CONFIRM_DIALOG = 2;
	private static final int BACKUP_DIALOG = 3;
	private static final int RESTORE_FROM_FILE_DIALOG = 4;
	
	private int swipe = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.character_list_layout);
		
		mDrawerController = new MainDrawerController(this);
		
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		int prefsChosenChar = settings.getInt("chosen_character", -1);
		context = this;
		
		
		if(Build.FINGERPRINT.startsWith("generic"))
		{
			EMULATOR = true;
			Log.d("CharacterList", "Running on Emulator phone.");
		}else
		{
			EMULATOR = false;
			Log.d("CharacterList", "Running on Physical phone.");
		}
		
		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
		
		
		sql = DbAdapterFactory.getStaticInstance(this);
		sql.open();
		

		sql.getCharacters(character_labels);
		
		
		// The value does not exist, we need to do something here. 
		if(prefsChosenChar == -1 || character_labels.size() == 0 || !sql.isCharInDB(prefsChosenChar)){
			
			// No characters found in the database. Need to prompt to create character!
			if(character_labels.size() == 0){
				createDefaultCharacter();
				chosenCharacter = sql.getFirstAvailCharacter();
			}
			else	// At least one character exists in the database. Set default character to first of those.
			{
				prefsChosenChar = character_labels.get(0).getId();
			}		
		}
		
		chosenCharacter = sql.getCharacterData(prefsChosenChar);
		adapter = new CustomCharacterAdapter(this, R.layout.character_list_item, 
				R.id.character_view_id, chosenCharacter, character_labels);
		setListAdapter(adapter);
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				CharacterLabel cLabel = (CharacterLabel) parent.getItemAtPosition(position);
				chosenCharacter = sql.getCharacterData(cLabel.getId());
				saveChosenCharacter();
				adapter.setCharacter(chosenCharacter);
				//selectCharacterClick(view);
				TabMain.tabHost.setCurrentTabByTag("classes");
			}
		});
		
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				CharacterLabel cLabel = (CharacterLabel) parent.getItemAtPosition(position);
				chosenCharacter = sql.getCharacterData(cLabel.getId());
				saveChosenCharacter();
				adapter.setCharacter(chosenCharacter);
				reloadList();
				editCharacter(cLabel);
				return true;
			}
		});
		
		lv.setOnTouchListener(gestureListener);

        setupDrawer();

		reloadList();
	}

    private void setupDrawer() {
        TextView createCharacterLink = (TextView) findViewById(R.id.drawer_create_character_link);
        createCharacterLink.setOnClickListener(this.createCharacterLinkListener());

        TextView backupLink = (TextView) findViewById(R.id.drawer_backup_link);
        backupLink.setOnClickListener(this.backupLinkListener());

        TextView restoreLink = (TextView) findViewById(R.id.drawer_restore_link);
        restoreLink.setOnClickListener(this.restoreLinkListener());

        //Wire universal links through controller
        mDrawerController.setupUniversalDrawerLinks();
    }

    /**
     * Listener for the Create Character drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Create Character link
     */
    protected OnClickListener createCharacterLinkListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                createCharacter();
            }
        };
    }

    /**
     * Listener for the Backup drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Backup link
     */
    protected OnClickListener backupLinkListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(BACKUP_DIALOG);
            }
        };
    }

    /**
     * Listener for the Restore drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Restore link
     */
    protected OnClickListener restoreLinkListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(RESTORE_FROM_FILE_DIALOG);
            }
        };
    }
	
	public void saveChosenCharacter()
	{
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("chosen_character", chosenCharacter.getCharId());
		editor.commit();
	}
	
	public static Character getChosenCharacter()
	{
		return chosenCharacter;
	}
	
	public static int getChosenCharacterId()
	{
		return chosenCharacter.getCharId();
	}

	private void reloadList() {
		character_labels.clear();
		sql.getCharacters(character_labels);
		//adapter.setCharacter(chosenCharacter);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onResume() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		adapter.notifyDataSetChanged();
		super.onResume();
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}
	
	public void backupCharacters(String path, String name) {
		
		int[] ids = new int[character_labels.size()];
		
		for(int i = 0; i < character_labels.size(); i++)
		{
			ids[i] = character_labels.get(i).getId();
		}
			
		
		
		String result = sql.backupCharactersToFile(ids, false, path, name);
		
		if(!result.equals(""))
		{
			Toast.makeText(this, result, Toast.LENGTH_LONG).show();
			
		}else
		{
			Toast.makeText(this, "Failed to backup Characters. Check path?", Toast.LENGTH_LONG).show();
		}
				
	}

	public boolean restoreCharacters(String fpath, String fname)
	{
		try
		{
			if(sql == null)
			{
				sql = DbAdapterFactory.getStaticInstance(this);
				sql.open();
			}
			boolean res = sql.readBackupFile(fpath, fname);
			character_labels.clear();
			sql.getCharacters(character_labels);
			
			// Just use the first character. 
			if(character_labels.size() > 0)
			{
				int chosenChar = character_labels.get(0).getId();
				chosenCharacter = sql.getCharacterData(chosenChar);
				reloadList();
			}
			return res;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private void delCharacter() {
		sql.deleteCharacter(chosenCharacter.getCharId());
		chosenCharacter = sql.getFirstAvailCharacter();
		if(chosenCharacter == null)
		{
			createDefaultCharacter();
		}
		chosenCharacter = sql.getFirstAvailCharacter();
		adapter.setCharacter(chosenCharacter);
		reloadList();
	}

	private void editCharacter(CharacterLabel cLabel) {
		showDialog(EDIT_DIALOG);
	}

	private void createCharacter() {
		showDialog(CREATE_DIALOG);	
	}


	
	private void createDefaultCharacter()
	{
		int newCharId = sql.addCharacter(DEFAULT_CHARACTER_NAME);
		// Check if the character was properly added. 
		if(newCharId == -1){
			Log.d("CharacterList","Could not add Default character to database.");
			Toast.makeText(this, "Could not add Default character to database.", Toast.LENGTH_LONG);
		}else
		{
			Log.d("CharacterList", "createDefaultCharacter() char = " + DEFAULT_CHARACTER_NAME + " added on id = " + newCharId);
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id)
		{
		case EDIT_DIALOG:
			EditText etext = (EditText) dialog.findViewById(R.id.edit_charname);
			etext.setText(chosenCharacter.getCharName());
			break;
		
		case CREATE_DIALOG:
			EditText ctext = (EditText) dialog.findViewById(R.id.create_charname);
			ctext.setText(chosenCharacter.getCharName());
			break;
		}
		super.onPrepareDialog(id, dialog);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch(id){
			case EDIT_DIALOG:
				edit_dialog = new Dialog(context);
				edit_dialog.setTitle("Edit Character");
				edit_dialog.setContentView(R.layout.edit_dialog);
				EditText etext = (EditText) edit_dialog.findViewById(R.id.edit_charname);
				etext.setText(chosenCharacter.getCharName());
				edit_dialog.findViewById(R.id.edit_ok).setOnClickListener(this);
				edit_dialog.findViewById(R.id.edit_delete).setOnClickListener(this);
				edit_dialog.findViewById(R.id.edit_cancel).setOnClickListener(this);
				dialog = edit_dialog;
				break;
				
			case CREATE_DIALOG:
				create_dialog = new Dialog(context);
				create_dialog.setTitle("Create New Character");
				create_dialog.setContentView(R.layout.create_dialog);
				etext = (EditText) create_dialog.findViewById(R.id.create_charname);
				//TODO: Fix some default character name. 
				etext.setText("");
				create_dialog.findViewById(R.id.create_ok).setOnClickListener(this);
				create_dialog.findViewById(R.id.create_cancel).setOnClickListener(this);
				create_dialog.setOnCancelListener(this);
				dialog = create_dialog;
				break;
			
			case DELETE_CHARACTER_CONFIRM_DIALOG:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Are you sure you want to delete this character?")
				       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							delCharacter();
						}
				       })
				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				dialog = alert;
			break;
			
			case BACKUP_DIALOG:
				dialog = new Dialog(context);

				dialog.setContentView(R.layout.backup_char_dialog);
				dialog.setTitle("File destination:");
				EditText fpath = (EditText) dialog.findViewById(R.id.backup_dest_path);
				if(EMULATOR)
				{
					fpath.setText("/mnt/sdcard");
				}else
				{
					fpath.setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
				}
				
				EditText fname = (EditText) dialog.findViewById(R.id.backup_dest_name);
				fname.setText("spelldir_backup.xml");
				Button saveButton = (Button) dialog.findViewById(R.id.backup_save_button);
				saveButton.setOnClickListener(this);
				Button cancelButton = (Button) dialog.findViewById(R.id.backup_cancel_button);
				cancelButton.setOnClickListener(this);
				backup_dialog = dialog;
				break;
				
			case RESTORE_FROM_FILE_DIALOG:
				dialog = new Dialog(context);

				dialog.setContentView(R.layout.restore_char_dialog);
				dialog.setTitle("File to Restore:");
				fpath = (EditText) dialog.findViewById(R.id.restore_dest_path);
				if(EMULATOR)
				{
					fpath.setText("/mnt/sdcard");
				}else
				{
					fpath.setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
				}
				
				fname = (EditText) dialog.findViewById(R.id.restore_dest_name);
				fname.setText("spelldir_backup.xml");
				saveButton = (Button) dialog.findViewById(R.id.restore_save_button);
				saveButton.setOnClickListener(this);
				cancelButton = (Button) dialog.findViewById(R.id.restore_cancel_button);
				cancelButton.setOnClickListener(this);
				restore_dialog = dialog;
				break;
		}
		return dialog;
	}
	
	public void onClick(View v) {
		
		switch(v.getId())
		{
		case R.id.edit_ok:
			String name = ((EditText)edit_dialog.findViewById(R.id.edit_charname)).getEditableText().toString();
			
			if(!name.equals(""))
			{
				// TODO: This could probably be done a bit better...
				// Editing a character	
				sql.renameCharacter(chosenCharacter.getCharId(), name);
				for(int i = 0; i < character_labels.size();i++){
					CharacterLabel cl = character_labels.get(i);
					// Grab the correct label for the character that is to change name. 
					if(cl.getId() == chosenCharacter.getCharId()){
						cl.setName(name);
					}
				}
				chosenCharacter.setCharName(name);
				reloadList();
				edit_dialog.dismiss();
			}else
			{
				Toast.makeText(this, "Character name cannot be empty.", Toast.LENGTH_LONG).show();
			}
			break;
				
			
			// Creating a new Character
		case R.id.create_ok:
			name = ((EditText)create_dialog.findViewById(R.id.create_charname)).getEditableText().toString();
			if(!name.equals(""))
			{
				int newCharId = sql.addCharacter(name);
				// Check if the character was properly added. 
				if(newCharId >= 0){
					chosenCharacter = sql.getCharacterData(newCharId);
					character_labels.add(new CharacterLabel(newCharId, name));
					
					reloadList();
					create_dialog.dismiss();
				}else
				{
					Toast.makeText(this, "Could not add character to database. ", Toast.LENGTH_LONG).show();
					create_dialog.dismiss();
				}
			}else
			{
				Toast.makeText(this, "Character name cannot be empty.", Toast.LENGTH_LONG);
			}
			break;
			
		case R.id.edit_delete:
			edit_dialog.dismiss();
			showDialog(DELETE_CHARACTER_CONFIRM_DIALOG);
			break;
			
		case R.id.edit_cancel:
			edit_dialog.cancel();
			break;
			
		case R.id.create_cancel:
			// Since there exists a onCancel() method, we delegate to that one. 
			create_dialog.cancel();
			break;
			
		case R.id.backup_save_button:
			String path = ((EditText) backup_dialog.findViewById(R.id.backup_dest_path)).getText().toString();
			String fileName = ((EditText) backup_dialog.findViewById(R.id.backup_dest_name)).getText().toString();
			backup_dialog.dismiss();
			backupCharacters(path, fileName);
			break;
			
		case R.id.backup_cancel_button:
			backup_dialog.dismiss();
			break;
			
		case R.id.restore_save_button:
			path = ((EditText) restore_dialog.findViewById(R.id.restore_dest_path)).getText().toString();
			fileName = ((EditText) restore_dialog.findViewById(R.id.restore_dest_name)).getText().toString();
			restore_dialog.dismiss();
			boolean res = restoreCharacters(path, fileName);
			if(res)
			{
				Toast.makeText(context, "Characters restored successfully", Toast.LENGTH_LONG).show();
			}else
			{
				Toast.makeText(context, "Failed to restore characters from file. ", Toast.LENGTH_LONG).show();
			}
			break;
			
		case R.id.restore_cancel_button:
			restore_dialog.dismiss();
			break;
			
		}
		
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("CharacterList", "onActivityResult() requestCode = " + requestCode + " resultCode = " + resultCode);
		switch(requestCode)
		{
		case Constants.SPELL_PREFERENCES:
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			Log.d("CharacterList", "<Preferences Start>");
			Log.d("CharacterList","\t" + Constants.PREF_CLERIC_ORACLE + " = " + prefs.getBoolean(Constants.PREF_CLERIC_ORACLE, false));
			Log.d("CharacterList","\t" + Constants.PREF_DEFAULT_TO + " = " + prefs.getBoolean(Constants.PREF_DEFAULT_TO, false));
			Log.d("CharacterList","\t" + Constants.PREF_PREPLIST + " = " + prefs.getString(Constants.PREF_PREPLIST, ""));
			Log.d("CharacterList","\t" + Constants.PREF_SPELLLIST + " = " + prefs.getString(Constants.PREF_SPELLLIST, ""));
			Log.d("CharacterList", "<Preferences End>");
			break;
		
		}
	}
	
	private class MyGestureDetector extends SimpleOnGestureListener {
		public boolean onFling(MotionEvent e1, MotionEvent e2, float vx,
				float vy) {
			Log.d("EVENT", "OnFling");
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(vx) > SWIPE_THRESHOLD_VELOCITY) {
					Log.d("EVENT", "OnFling ------ Left Fling");
					TabMain.tabHost.setCurrentTabByTag("classes");
					return true;
					// swipe = 1;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(vy) > SWIPE_THRESHOLD_VELOCITY) {
					Log.d("EVENT", "OnFling ------ Right Fling");
					
					return true;
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}
	}

	public void onCancel(DialogInterface dialog) {
		create_dialog.dismiss();
		if(!sql.hasCharacters())
		{
			createDefaultCharacter();
			chosenCharacter = sql.getFirstAvailCharacter();
		}
	}
}
