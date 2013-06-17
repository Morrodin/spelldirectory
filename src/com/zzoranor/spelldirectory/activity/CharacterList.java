package com.zzoranor.spelldirectory.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.zzoranor.spelldirectory.CharacterLabel;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.controllers.MainDrawerController;
import com.zzoranor.spelldirectory.data.Character;
import com.zzoranor.spelldirectory.fragments.CharacterListFragment;
import com.zzoranor.spelldirectory.services.SqlService;
import com.zzoranor.spelldirectory.util.Constants;
//import pl.verdigo.libraries.drawer.Drawer;

public class CharacterList extends FragmentActivity implements android.content.DialogInterface.OnCancelListener{
	
	public static boolean EMULATOR = false;

    private MainDrawerController mDrawerController;
    private SqlService mSqlService;

    private DrawerLayout mDrawer;
	final ArrayList<CharacterLabel> character_labels = new ArrayList<CharacterLabel>();
	private Context context;
	private Character chosenCharacter;
	
	private Dialog create_dialog;
	private Dialog edit_dialog;
	private Dialog backup_dialog;
	private Dialog restore_dialog;

	private static final String DEFAULT_CHARACTER_NAME = "Default";
	
	private static final int EDIT_DIALOG = 0;
	private static final int CREATE_DIALOG = 1;
	private static final int DELETE_CHARACTER_CONFIRM_DIALOG = 2;
	private static final int BACKUP_DIALOG = 3;
	private static final int RESTORE_FROM_FILE_DIALOG = 4;

    private CharacterListFragment characterListFragment;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.character_list_activity);

        mDrawer = (DrawerLayout) findViewById(R.id.tab_management_drawer_layout);
        mDrawerController = new MainDrawerController(this, mDrawer);
        mSqlService = new SqlService(this);
        mSqlService.setupSql();
        context = this;

        characterListFragment = new CharacterListFragment(mSqlService);

        getFragmentManager().beginTransaction()
                .add(R.id.character_list_fragment_container, characterListFragment).commit();

        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        int prefsChosenChar = settings.getInt("chosen_character", -1);

        chosenCharacter = mSqlService.getSqlAdapter().getCharacterData(prefsChosenChar);

        setupDrawer();

	}

    private void setupDrawer() {
        TextView createCharacterLink = (TextView) findViewById(R.id.drawer_create_character_link);
        createCharacterLink.setOnClickListener(this.createCharacterLinkListener());

        TextView backupLink = (TextView) findViewById(R.id.drawer_backup_link);
        backupLink.setOnClickListener(this.backupLinkListener());

        TextView restoreLink = (TextView) findViewById(R.id.drawer_restore_link);
        restoreLink.setOnClickListener(this.restoreLinkListener());

        //Wire universal links through controller
        mDrawerController.initDrawer();
    }

    /**
     * Listener for the Create Character drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Create Character link
     */
    protected View.OnClickListener createCharacterLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCharacter();
                mDrawer.closeDrawers();
            }
        };
    }

    /**
     * Listener for the Backup drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Backup link
     */
    protected View.OnClickListener backupLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(BACKUP_DIALOG);
                mDrawer.closeDrawers();
            }
        };
    }

    /**
     * Listener for the Restore drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Restore link
     */
    protected View.OnClickListener restoreLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(RESTORE_FROM_FILE_DIALOG);
                mDrawer.closeDrawers();
            }
        };
    }

    private void createCharacter() {
        showDialog(CREATE_DIALOG);
    }
	
	public int getChosenCharacterId() {
		return chosenCharacter.getCharId();
	}

	
	@Override
	protected void onResume() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		characterListFragment.getAdapter().notifyDataSetChanged();
		super.onResume();
	}
	
//	public boolean onTouchEvent(MotionEvent event) {
//		if (gestureDetector.onTouchEvent(event))
//			return true;
//		else
//			return false;
//	}


	private void delCharacter() {
		mSqlService.getSqlAdapter().deleteCharacter(characterListFragment.getChosenCharacter().getCharId());
		chosenCharacter = mSqlService.getSqlAdapter().getFirstAvailCharacter();
		if(chosenCharacter == null)
		{
			createDefaultCharacter();
		}
		chosenCharacter = mSqlService.getSqlAdapter().getFirstAvailCharacter();
		characterListFragment.getAdapter().setCharacter(chosenCharacter);
		characterListFragment.reloadList();
	}

	private void createDefaultCharacter()
	{
		int newCharId = mSqlService.getSqlAdapter().addCharacter(DEFAULT_CHARACTER_NAME);
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
			etext.setText(characterListFragment.getChosenCharacter().getCharName());
			break;
		
		case CREATE_DIALOG:
			EditText ctext = (EditText) dialog.findViewById(R.id.create_charname);
			ctext.setText(characterListFragment.getChosenCharacter().getCharName());
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
				edit_dialog.findViewById(R.id.edit_ok).setOnClickListener(characterListFragment);
				edit_dialog.findViewById(R.id.edit_delete).setOnClickListener(characterListFragment);
				edit_dialog.findViewById(R.id.edit_cancel).setOnClickListener(characterListFragment);
				dialog = edit_dialog;
				break;
				
			case CREATE_DIALOG:
				create_dialog = new Dialog(context);
				create_dialog.setTitle("Create New Character");
				create_dialog.setContentView(R.layout.create_dialog);
				etext = (EditText) create_dialog.findViewById(R.id.create_charname);
				//TODO: Fix some default character name. 
				etext.setText("");
				create_dialog.findViewById(R.id.create_ok).setOnClickListener(characterListFragment);
				create_dialog.findViewById(R.id.create_cancel).setOnClickListener(characterListFragment);
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
				saveButton.setOnClickListener(characterListFragment);
				Button cancelButton = (Button) dialog.findViewById(R.id.backup_cancel_button);
				cancelButton.setOnClickListener(characterListFragment);
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
				saveButton.setOnClickListener(characterListFragment);
				cancelButton = (Button) dialog.findViewById(R.id.restore_cancel_button);
				cancelButton.setOnClickListener(characterListFragment);
				restore_dialog = dialog;
				break;
		}
		return dialog;
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

	public void onCancel(DialogInterface dialog) {
		create_dialog.dismiss();
		if(!mSqlService.getSqlAdapter().hasCharacters())
		{
			createDefaultCharacter();
			chosenCharacter = mSqlService.getSqlAdapter().getFirstAvailCharacter();
		}
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerController.getDrawerToggle().syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerController.getDrawerToggle().onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerController.getDrawerToggle().onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    public Dialog getCreateDialog() {
        return create_dialog;
    }

    public void setCreateDialog(Dialog create_dialog) {
        this.create_dialog = create_dialog;
    }

    public Dialog getEditDialog() {
        return edit_dialog;
    }

    public void setEditDialog(Dialog edit_dialog) {
        this.edit_dialog = edit_dialog;
    }

    public Dialog getBackupDialog() {
        return backup_dialog;
    }

    public void setBackupDialog(Dialog backup_dialog) {
        this.backup_dialog = backup_dialog;
    }

    public Dialog getRestoreDialog() {
        return restore_dialog;
    }

    public void setRestoreDialog(Dialog restore_dialog) {
        this.restore_dialog = restore_dialog;
    }

    public SqlService getSqlService() {
        return mSqlService;
    }

    public void setSqlService(SqlService mSqlService) {
        this.mSqlService = mSqlService;
    }
}
