package com.zzoranor.spelldirectory.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.zzoranor.spelldirectory.CharacterLabel;
import com.zzoranor.spelldirectory.CustomCharacterAdapter;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.activity.CharacterList;
import com.zzoranor.spelldirectory.activity.TabManagementActivity;
import com.zzoranor.spelldirectory.controllers.MainDrawerController;
import com.zzoranor.spelldirectory.data.Character;
import com.zzoranor.spelldirectory.database.DbAdapter;
import com.zzoranor.spelldirectory.database.DbAdapterFactory;
import com.zzoranor.spelldirectory.services.SqlService;
import com.zzoranor.spelldirectory.util.Constants;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Kenton
 * Date: 6/14/13
 * Time: 11:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class CharacterListFragment extends Fragment implements View.OnClickListener {

    public static boolean EMULATOR = false;

    private MainDrawerController mDrawerController;

    DrawerLayout mDrawer;
    DbAdapter sql;
    private SqlService mSqlService;
    final ArrayList<CharacterLabel> character_labels = new ArrayList<CharacterLabel>();
    Context context;
    CustomCharacterAdapter adapter;
    private com.zzoranor.spelldirectory.data.Character chosenCharacter;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

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

    ListView characterList;

    public CharacterListFragment(SqlService sqlService) {
        this.mSqlService = sqlService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.character_list_fragment, container, false);

        characterList = (ListView) view.findViewById(R.id.character_list);

        mDrawer = (DrawerLayout) getActivity().findViewById(R.id.tab_management_drawer_layout);

        mDrawerController = new MainDrawerController(getActivity(), mDrawer);

        SharedPreferences settings = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        int prefsChosenChar = settings.getInt("chosen_character", -1);
        context = getActivity();


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


        sql = mSqlService.getSqlAdapter();
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
        adapter = new CustomCharacterAdapter(getActivity(), R.layout.character_list_item,
                R.id.character_view_id, chosenCharacter, character_labels);
        characterList.setAdapter(adapter);

        characterList.setTextFilterEnabled(true);

        characterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                CharacterLabel cLabel = (CharacterLabel) parent.getItemAtPosition(position);
                chosenCharacter = sql.getCharacterData(cLabel.getId());
                saveChosenCharacter();
                adapter.setCharacter(chosenCharacter);
                reloadList();

                Intent intent = new Intent(getActivity(), TabManagementActivity.class);
                intent.putExtra("chosenChar", chosenCharacter);
                startActivity(intent);
            }
        });

        characterList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

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

        characterList.setOnTouchListener(gestureListener);

        reloadList();

        return view;
    }


    public void saveChosenCharacter()
    {
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("chosen_character", chosenCharacter.getCharId());
        editor.commit();
    }

    public Character getChosenCharacter()
    {
        return chosenCharacter;
    }

    public int getChosenCharacterId()
    {
        return chosenCharacter.getCharId();
    }

    public void reloadList() {
        character_labels.clear();
        sql.getCharacters(character_labels);
        //adapter.setCharacter(chosenCharacter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    private void editCharacter(CharacterLabel cLabel) {
        getActivity().showDialog(EDIT_DIALOG);
    }


    private void createDefaultCharacter()
    {
        int newCharId = sql.addCharacter(DEFAULT_CHARACTER_NAME);
        // Check if the character was properly added.
        if(newCharId == -1){
            Log.d("CharacterList","Could not add Default character to database.");
            Toast.makeText(getActivity(), "Could not add Default character to database.", Toast.LENGTH_LONG);
        }else
        {
            Log.d("CharacterList", "createDefaultCharacter() char = " + DEFAULT_CHARACTER_NAME + " added on id = " + newCharId);
        }
    }

    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.edit_ok:
                String name = ((EditText) ((CharacterList) getActivity()).getEditDialog().findViewById(R.id.edit_charname)).getEditableText().toString();

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
                    ((CharacterList) getActivity()).getEditDialog().dismiss();
                }else
                {
                    Toast.makeText(getActivity(), "Character name cannot be empty.", Toast.LENGTH_LONG).show();
                }
                break;


            // Creating a new Character
            case R.id.create_ok:
                name = ((EditText)((CharacterList) getActivity()).getCreateDialog().findViewById(R.id.create_charname)).getEditableText().toString();
                if(!name.equals(""))
                {
                    int newCharId = sql.addCharacter(name);
                    // Check if the character was properly added.
                    if(newCharId >= 0){
                        chosenCharacter = sql.getCharacterData(newCharId);
                        character_labels.add(new CharacterLabel(newCharId, name));

                        reloadList();
                        ((CharacterList) getActivity()).getCreateDialog().dismiss();
                    }else
                    {
                        Toast.makeText(getActivity(), "Could not add character to database. ", Toast.LENGTH_LONG).show();
                        ((CharacterList) getActivity()).getCreateDialog().dismiss();
                    }
                }else
                {
                    Toast.makeText(getActivity(), "Character name cannot be empty.", Toast.LENGTH_LONG);
                }
                break;

            case R.id.edit_delete:
                ((CharacterList) getActivity()).getEditDialog().dismiss();
                getActivity().showDialog(DELETE_CHARACTER_CONFIRM_DIALOG);
                break;

            case R.id.edit_cancel:
                ((CharacterList) getActivity()).getEditDialog().cancel();
                break;

            case R.id.create_cancel:
                // Since there exists a onCancel() method, we delegate to that one.
                ((CharacterList) getActivity()).getCreateDialog().cancel();
                break;

            case R.id.backup_save_button:
                String path = ((EditText) ((CharacterList) getActivity()).getBackupDialog().findViewById(R.id.backup_dest_path)).getText().toString();
                String fileName = ((EditText) ((CharacterList) getActivity()).getBackupDialog().findViewById(R.id.backup_dest_name)).getText().toString();
                ((CharacterList) getActivity()).getBackupDialog().dismiss();
                backupCharacters(path, fileName);
                break;

            case R.id.backup_cancel_button:
                ((CharacterList) getActivity()).getBackupDialog().dismiss();
                break;

            case R.id.restore_save_button:
                path = ((EditText) ((CharacterList) getActivity()).getRestoreDialog().findViewById(R.id.restore_dest_path)).getText().toString();
                fileName = ((EditText) ((CharacterList) getActivity()).getRestoreDialog().findViewById(R.id.restore_dest_name)).getText().toString();
                ((CharacterList) getActivity()).getRestoreDialog().dismiss();
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
                ((CharacterList) getActivity()).getRestoreDialog().dismiss();
                break;

        }


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
            Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();

        }else
        {
            Toast.makeText(getActivity(), "Failed to backup Characters. Check path?", Toast.LENGTH_LONG).show();
        }

    }

    public boolean restoreCharacters(String fpath, String fname)
    {
        try
        {
            if(sql == null)
            {
                sql = DbAdapterFactory.getStaticInstance(getActivity());
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

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
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
                     //TODO: Switch tabs once we can do that...
//                    TabMain.tabHost.setCurrentTabByTag("classes");
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


    public CustomCharacterAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(CustomCharacterAdapter adapter) {
        this.adapter = adapter;
    }

}
