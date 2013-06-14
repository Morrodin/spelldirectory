package com.zzoranor.spelldirectory.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zzoranor.spelldirectory.CustomAdapter;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.SpellLabel;
import com.zzoranor.spelldirectory.TabMain;
import com.zzoranor.spelldirectory.controllers.MainDrawerController;
import com.zzoranor.spelldirectory.database.DbAdapter;
import com.zzoranor.spelldirectory.database.DbAdapterFactory;
import com.zzoranor.spelldirectory.fragments.dialogs.SpellsLongclickModeDialogFragment;
import com.zzoranor.spelldirectory.util.Constants;
import com.zzoranor.spelldirectory.util.Utility;

public class SpellList extends FragmentActivity implements SpellsLongclickModeDialogFragment.SpellLongclickModeCallback {

	private ArrayList<SpellLabel> spell_labels;
	private boolean all_classes;
	private boolean all_levels;
	private int chosen_level;
	private int currentPosition = -1;
	private DbAdapter sql;
	
	private CustomAdapter adapter;
	private TextView header;
	private TextView headerClassField;
	private ImageView search_button;
	private Context context;
    private ListView spellList;

	private GestureDetector gestureDetector;
	private View.OnTouchListener gestureListener;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	private static final int MAX_LENGTH_CHAR_NAME = 8;
	
	private int swipe = 0;
	private int headerLines;
	
	private static final String tag = "SpellList";
		
	private boolean browsingKnownSpells = false;
	private int longClickMode = Constants.LONG_CLICK_PREPARE;
	private int prevLongClickMode = Constants.LONG_CLICK_PREPARE;
	

	private static final int DIALOG_METAMAGIC = 2;
	
	private String metaChosen = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spell_list_activity);

		RelativeLayout headerContainer = (RelativeLayout) findViewById(R.id.header_container);	
		
		// Prepared Spells Header
		header = (TextView) headerContainer.findViewById(R.id.header_text);
		if(Utility.getScreenSizeCategory(this) == Configuration.SCREENLAYOUT_SIZE_SMALL)
		{
			headerLines = 3;
		}else
		{
			headerLines = 2;
		}
		header.setText(CharacterList.chosenCharacter.getSpanString(headerLines));
		header.setFocusable(false);
		
		// Class Name Field
		headerClassField = (TextView) headerContainer.findViewById(R.id.headerClassField);
		headerClassField.setText(CharacterList.chosenCharacter.getCurrClassName());

		// Search Button 
		search_button = (ImageView) headerContainer.findViewById(R.id.searchbutton);
		search_button.setFocusable(false);
		search_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				InputMethodManager inputManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
				inputManager.toggleSoftInput(0, 0);	
			}
		});

		spell_labels = new ArrayList<SpellLabel>();

		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				//Log.d("EVENT", "---- OnTouch");
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
		sql = DbAdapterFactory.getStaticInstance(this);

		sql.open();
		
		// Initiate list Adapter for first-time-use.
		initList();
		// Load/Reload data from the database to populate list. 
		reloadListFromDB();

        setupDrawer();
	}


    /**
     * Wires up the buttons in the navigation drawer.
     */
    private void setupDrawer() {
        MainDrawerController mDrawerController = new MainDrawerController(this);
        mDrawerController.setupUniversalDrawerLinks();

        TextView metamagicLink = (TextView) findViewById(R.id.drawer_metamagic_link);
        metamagicLink.setOnClickListener(this.metamagicLinkListener());

        TextView longclickModeLink = (TextView) findViewById(R.id.drawer_longclick_link);
        longclickModeLink.setOnClickListener(this.longclickModeLinkListener());
    }

    /**
     * Listener for the Prepare with Metamagic drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Backup link
     */
    protected OnClickListener metamagicLinkListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                prevLongClickMode = longClickMode;
                longClickMode = Constants.LONG_CLICK_METAMAGIC;
                showDialog(DIALOG_METAMAGIC);
            }
        };
    }


    /**
     * Listener for the Longclick Mode drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Longclick Mode link
     */
    protected OnClickListener longclickModeLinkListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                //We new this each time in order to show the correct currently selected value
                SpellsLongclickModeDialogFragment dialog =
                        new SpellsLongclickModeDialogFragment(SpellList.this, longClickMode, browsingKnownSpells);
                dialog.show(getSupportFragmentManager(), "longclickMode");
            }
        };
    }

	/**
	 * Clears the current containers holding data for the ListAdapter 
	 * and repopulates using the sql adapter. 
	 */
	private void populateListContainerFromDb()
	{
		
		sql.populateMetamagicsFromDB();
		spell_labels.clear();

		SpellLabel[] known_spells = sql.getKnownSpells(CharacterList.chosenCharacter.getCharId());
		Arrays.sort(known_spells);
		
		Log.d("Event", "Spelllist triggered: " + CharacterList.chosenCharacter.getCurrClassId());

		if(CharacterList.chosenCharacter.getCurrClassId() == DbAdapter.KNOWN_SPELLS_CLASS_ID)
		{
			browsingKnownSpells = true;
			// Get spells from dynamic database if it is the known spells class. 
			

			for(SpellLabel label : known_spells)
			{
				spell_labels.add(label);
				//spell_ids.add(label.getId());
			}
			
		}else
		{
			browsingKnownSpells = false;
			
			
			SpellLabel[] labels = sql.getSpellsFromList(CharacterList.chosenCharacter.getCurrClassId());

            for (SpellLabel l : labels) {
                Pair<Integer, Integer> prep = CharacterList.chosenCharacter.getUsedPrepared(l.getName());
                l.setPrepared(prep);
                int cmp = Arrays.binarySearch(known_spells, l);
                if (cmp >= 0)
                    l.setKnown(true);
                spell_labels.add(l);
            }
		}
	}
	
	/**
	 * Initiates the ListAdapter by setting various parameters 
	 * and connecting onClick and onLongClick listeners. 
	 */
	private void initList(){
	
		adapter = new CustomAdapter(this, R.layout.spell_list_item2,
				R.id.list_view_name, CharacterList.chosenCharacter, spell_labels);
        spellList = (ListView) findViewById(R.id.spell_list_view);
		spellList.setOnTouchListener(gestureListener);
		spellList.setAdapter(adapter);
		
		// Enable search. 
		spellList.setTextFilterEnabled(true);

		spellList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                SpellLabel sp = (SpellLabel) parent.getItemAtPosition(position);

                //Log.d("EVENT", "OnItemClick");
                //Log.d("LIST", "View: " + view.toString());
                RelativeLayout ll = (RelativeLayout) view;
                View v = ll.findViewById(R.id.list_view_name);
                Context context = parent.getContext();
                Intent intent;
                if (swipe == 0) {
                    //Log.d("EVENT", "-------- SWIPE = 0");
                    intent = new Intent().setClass(context, SingleSpell.class);
                    String s = (String) ((TextView) v).getText();
                    //Log.d("LIST", "1:" + s);

                    //intent.putExtra("single_spell.name", s);
                    intent.putExtra("single_spell.id", sp.getId());
                    context.startActivity(intent);
                } else if (swipe == 1) {
                    //Log.d("EVENT", "-------- SWIPE = 1");
                    swipe = 0;
                    TabMain.tabHost.setCurrentTabByTag("prepared_spells");
                } else {
                    //Log.d("EVENT", "-------- SWIPE = 2");
                    swipe = 0;
                    finish();
                }
            }
        });

		spellList.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                SpellLabel sp = (SpellLabel) parent.getItemAtPosition(position);

                Pair<Integer, Integer> numPrepared = CharacterList.chosenCharacter.getUsedPrepared(sp);
                TextView view_prepare = (TextView) view.findViewById(R.id.list_view_prepare);
                TextView view_known = (TextView) view.findViewById(R.id.list_view_spell_known);
                int prepared = numPrepared.second;
                int left_today = numPrepared.first;

                switch (longClickMode) {

                    case Constants.LONG_CLICK_PREPARE:
                        // Preparing spells using longclick.
                        Log.d("SpellList", "Preparing Spell: " + sp.getName() + " to character: " + CharacterList.chosenCharacter.getCharId());

                        prepared++;
                        left_today++;
                        // Update database.
                        sql.addPreparedSpell(CharacterList.chosenCharacter.getCharId(), sp.getId(), sp.getName(), sp.getLvl(),
                                CharacterList.chosenCharacter.getCurrClassId(), Constants.SPELL_KNOWN_DEFAULT, left_today, prepared);
                        // Update List.
                        CharacterList.chosenCharacter.prepareSpell(sp, 1);

                        view_prepare.setText("" + left_today + "/" + prepared);
                        Log.d("LIST", "Adding: " + sp.getName()
                                + " to the prepared spells list.");
                        break;

                    case Constants.LONG_CLICK_REMOVE_PREPARED:
                        // Removing spells using longclick.
                        if (prepared == 1) {
                            view_prepare.setText("");

                            //sql.removePreparedSpellByName(CharacterList.chosenCharacter.getCharId(), sp.getName());
                            CharacterList.chosenCharacter.removeSpell(sp);
                        } else if (prepared > 1) {
                            prepared--;

                            if (prepared < left_today) {
                                left_today = prepared;
                            }

                            CharacterList.chosenCharacter.removeSpell(sp);
                            view_prepare.setText("" + left_today + "/" + prepared);

                            Log.d("LIST", "Removing: " + sp.getName()
                                    + " to the prepared spells list.");
                        }

                        sql.removePreparedSpell(CharacterList.chosenCharacter.getCharId(), sp.getId(), sp.getName());

                        break;


                    case Constants.LONG_CLICK_METAMAGIC:
                        if (!metaChosen.equals("")) {
                            String metaName = "" + metaChosen;
                            metaName = metaChosen.split("\t")[0];

                            String metaSpellName = metaName + " " + sp.getName();
                            int newlvl = sp.getLvl() + sql.getMetamagicAdjustment(metaName);

                            Pair<Integer, Integer> metaPrepPair = CharacterList.chosenCharacter.getUsedPrepared(metaSpellName);
                            int metaPrepared = metaPrepPair.second;
                            int metaLeftToday = metaPrepPair.first;
                            metaPrepared++;
                            metaLeftToday++;

                            // update database
                            sql.addPreparedSpell(CharacterList.chosenCharacter.getCharId(), sp.getId(), metaSpellName,
                                    newlvl, CharacterList.chosenCharacter.getCurrClassId(), Constants.SPELL_KNOWN_DEFAULT, metaLeftToday, metaPrepared);

                            // Update List
                            SpellLabel newSp = new SpellLabel(metaSpellName, sp.getId(), newlvl, sp.getSchool(), metaLeftToday, metaPrepared);
                            CharacterList.chosenCharacter.prepareSpell(newSp, 1);

                            // Notify that it is prepared.
                            Toast.makeText(parent.getContext(), metaSpellName + " prepared.", Toast.LENGTH_SHORT).show();

                            longClickMode = prevLongClickMode;
                        }
                        break;

                    case Constants.LONG_CLICK_KNOWN_SPELL:
                        if (!browsingKnownSpells) {    // Not browsing Known spells. This menu button adds spell to known spells.
                            Log.d(tag, "Adding spell to Known Spells: " + sp.getName() + " for character: " + CharacterList.getChosenCharacterId());

                            if (sp.getId() > -1) {
                                //sql.addSpellAsKnown(CharacterList.getChosenCharacterId(),sp.getId(), sp.getLvl());
                                sql.addKnownSpell(CharacterList.getChosenCharacterId(), sp.getId(), sp.getLvl());
                            } else {
                                Log.e("SpellList", "SpellLabel has id < 0 when adding known spell. ");
                            }
                            view_known.setText("Known");
                            sp.setKnown(true);
                        } else {
                            // Browsing Known Spells. This menu button removes the spell from known spells.
                            Log.d(tag, "Removing spell from Known Spells: " + sp.getName() + " for character: " + CharacterList.getChosenCharacterId());

                            if (sp.getId() > -1) {
                                sql.removeSpellFromKnown(CharacterList.getChosenCharacterId(), sp.getId());
                                if (browsingKnownSpells) {

                                    int idx = Collections.binarySearch(spell_labels, sp);
                                    if (idx >= 0) {
                                        spell_labels.remove(idx);
                                        //spell_ids.remove(idx);
                                    } else {
                                        Log.w("SpellList", "Trying to remove a spell that is not found. ");
                                    }
                                }
                            } else {
                                Log.e("SpellList", "SpellLabel has id < 0 when removing known spell. ");
                            }

                            view_known.setText("");
                            sp.setKnown(false);
                        }
                        break;
                }

                adapter.notifyDataSetChanged();
                header.setText(CharacterList.chosenCharacter.getSpanString(headerLines));
                return true;
            }
        });
	}

	private void reloadListFromDB() {
		adapter.setCharacter(CharacterList.chosenCharacter);
		populateListContainerFromDb();
		header.setText(CharacterList.chosenCharacter.getSpanString(headerLines));
		adapter.notifyDataSetChanged();
	}

	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder;
		
		switch(id)
		{
		case DIALOG_METAMAGIC:
			
			final ArrayList<String> list = sql.getMetamagicList();
			
			String[] arr = new String[list.size()];			
			arr = list.toArray(arr);

			
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Add Metamagic");
			builder.setItems(arr, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	metaChosen = list.get(item);
			    	Toast.makeText(SpellList.this, "Long-click on a spell to prepare with Metamagic. ", Toast.LENGTH_SHORT).show();
			    }
			});
			dialog = builder.create();
			
			break;
		}
		
		return dialog;
	}
	

	@Override
	protected void onPause() {
		currentPosition = spellList.getFirstVisiblePosition();
		super.onPause();
	}

	@Override
	protected void onResume() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Log.d("HANDLER","class_id: " + CharacterList.chosenCharacter.getCurrClassId());
		Log.d("HANDLER","class_name: " + CharacterList.chosenCharacter.getCurrClassName());
		sql.open();
		
		headerClassField.setText(Utility.shortenLongClassNames(CharacterList.chosenCharacter.getCurrClassName(), MAX_LENGTH_CHAR_NAME));
		
		reloadListFromDB();	
		spellList.setSelection(currentPosition);
				
		// Check preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean defToAction = prefs.getBoolean(Constants.PREF_DEFAULT_TO, true);
		if(defToAction)
		{
			// Use default values for longclickmode. 
			String defTo =  prefs.getString(Constants.PREF_SPELLLIST, ""+ Constants.LONG_CLICK_PREPARE);
			prevLongClickMode = Integer.parseInt(defTo);
			longClickMode = Integer.parseInt(defTo);
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		TabMain.tabHost.setCurrentTabByTag("classes");
	}

	public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
	}

    @Override
    public void longclickPrepareSelected() {
        longClickMode = Constants.LONG_CLICK_PREPARE;
    }

    @Override
    public void longclickRemoveSelected() {
        longClickMode = Constants.LONG_CLICK_REMOVE_PREPARED;
    }

    @Override
    public void longclickAddToKnownSelected() {
        longClickMode = Constants.LONG_CLICK_KNOWN_SPELL;
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
					 //Toast.makeText(SpellList.this, "Left Swipe",
					 //Toast.LENGTH_SHORT).show();
					//Intent intent = new Intent().setClass(SpellList.this,
					//		PreparedList.class);
					//SpellList.this.startActivity(intent);
					TabMain.tabHost.setCurrentTabByTag("prepared_spells");
					Log.d("EVENT", "OnFling ------ Left Fling Return");
					return true;
					// swipe = 1;

				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(vy) > SWIPE_THRESHOLD_VELOCITY) {
					Log.d("EVENT", "OnFling ------ Right Fling");
					 //Toast.makeText(SpellList.this, "Right Swipe",
					 //Toast.LENGTH_SHORT).show();
					//Intent intent = new Intent().setClass(SpellList.this,
					//		SpellDir_Test.class);
					//SpellList.this.startActivity(intent);
					TabMain.tabHost.setCurrentTabByTag("classes");
					// swipe = 2;
					Log.d("EVENT", "OnFling ------ Right Fling Return");
					return true;
					// finish();
				}
			} catch (Exception e) {
				// nothing
			}
			return false;

		}
	}
}