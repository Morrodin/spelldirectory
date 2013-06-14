package com.zzoranor.spelldirectory.activity;


import java.util.ArrayList;
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
import android.support.v4.widget.DrawerLayout;
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

import com.zzoranor.spelldirectory.CustomAdapter;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.SpellLabel;
import com.zzoranor.spelldirectory.TabMain;
import com.zzoranor.spelldirectory.controllers.MainDrawerController;
import com.zzoranor.spelldirectory.database.DbAdapter;
import com.zzoranor.spelldirectory.database.DbAdapterFactory;
import com.zzoranor.spelldirectory.fragments.dialogs.PreparedSpellsLongclickModeDialogFragment;
import com.zzoranor.spelldirectory.util.Constants;
import com.zzoranor.spelldirectory.util.Utility;

public class PreparedList extends FragmentActivity implements PreparedSpellsLongclickModeDialogFragment.PreparedLongclickModeCallback {
	DbAdapter sql;
	//private static Character character;

    private PreparedSpellsLongclickModeDialogFragment mLongclickModeDialog;

	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private MainDrawerController mDrawerController;

	private static final int DIALOG_CONFIRM_CLEAR = 1;

		
	private int swipe = 0;
	private int headerLines;
	private int currentPosition = -1;
	private int longClickMode = Constants.LONG_CLICK_USE_SPELL;
	private int prevLongClickMode = Constants.LONG_CLICK_USE_SPELL;
	

    private DrawerLayout mDrawer;
	private ArrayList<SpellLabel> spell_labels;
	private CustomAdapter adapter;
	private TextView header;
	private TextView headerClassField;
	private ImageView search_button;
    private ListView spellList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spell_list_prepared_activity);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.setFocusable(false);

        mDrawerController = new MainDrawerController(this, mDrawer);

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

		// Loading Spell File.
		sql = DbAdapterFactory.getStaticInstance(this);
		initList();

        //Setup and initialize drawer
        setupDrawer();
	}

    /**
     * Wires the buttons in the navigation drawer for this activity.
     */
    private void setupDrawer() {
        mDrawerController.setupUniversalDrawerLinks();

        TextView resetUsesLink = (TextView) findViewById(R.id.drawer_reset_uses_link);
        resetUsesLink.setOnClickListener(this.resetUsesLinkListener());

        TextView clearListLink = (TextView) findViewById(R.id.drawer_clear_list_link);
        clearListLink.setOnClickListener(this.clearListLinkListener());

        TextView longclickModeLink = (TextView) findViewById(R.id.drawer_longclick_prepared_link);
        longclickModeLink.setOnClickListener(longclickModeLinkListener());
    }


    protected OnClickListener resetUsesLinkListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSpellUses();
                mDrawer.closeDrawers();
            }
        };
    }

    protected OnClickListener clearListLinkListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(DIALOG_CONFIRM_CLEAR);
                mDrawer.closeDrawers();
            }
        };
    }

    protected OnClickListener longclickModeLinkListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                //New this every time so we can show the correct current selection
                PreparedSpellsLongclickModeDialogFragment mLongclickModeDialog =
                        new PreparedSpellsLongclickModeDialogFragment(PreparedList.this, longClickMode);
                mLongclickModeDialog.show(getSupportFragmentManager(), "longclickMode");
                mDrawer.closeDrawers();
            }
        };
    }

    private void resetSpellUses() {
        CharacterList.chosenCharacter.resetAllSpellUses();
        sql.resetAllUses(CharacterList.chosenCharacter.getCharId());
        adapter.notifyDataSetChanged();
        mDrawer.closeDrawers();
    }

	public void initList() {
		// This method prepares the spells from database for the defined
		// character.
		//sql.setCharPreparedspellsFromDB(CharacterList.chosenCharacter);

		

		// "0:  xx  1:  xx  2:  xx  3:  xx  4:  xx  5:  xx  6:  xx  7:  xx  8:  xx  9:  xx  ."
		
		RelativeLayout headerContainer = (RelativeLayout) findViewById(R.id.header_container);		
		
		// Prepared Spells header field. 
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
		headerClassField.setVisibility(View.GONE);
		
		// Search Button 
		search_button = (ImageView) headerContainer.findViewById(R.id.searchbutton);
		search_button.setFocusable(false);
		search_button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				inputManager.toggleSoftInput(0, 0);
			}
		});

		
		// List adapter

		spellList = (ListView) findViewById(R.id.prepared_spells_list_view);
		spellList.setTextFilterEnabled(true);

        adapter = new CustomAdapter(this, R.layout.spell_list_item2,
                R.id.list_view_name, CharacterList.chosenCharacter, spell_labels);
        spellList.setAdapter(adapter);

		// Click listener
		spellList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                SpellLabel sp = (SpellLabel) parent.getItemAtPosition(position);

                Context context = parent.getContext();
                Intent intent;
                if (swipe != 2) {
                    swipe = 0;
                    intent = new Intent().setClass(context, SingleSpell.class);

                    intent.putExtra("single_spell.id", sp.getId());
                    context.startActivity(intent);
                } else {
                    finish();
                }

            }
        });

		// LongClickListener
		spellList.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                SpellLabel sp = (SpellLabel) parent.getItemAtPosition(position);

                Pair<Integer, Integer> numPrepared = CharacterList.chosenCharacter
                        .getUsedPrepared(sp);
                int prepared = numPrepared.second;
                int left_today = numPrepared.first;
                switch (longClickMode) {

                    case Constants.LONG_CLICK_REMOVE_PREPARED:
                        if (prepared == 1) {
                            CharacterList.chosenCharacter.removeSpell(sp);
                            //sql.removePreparedSpellByName(CharacterList.chosenCharacter.getCharId(), sp.getName());
                            int idx = Collections.binarySearch(spell_labels, sp);
                            spell_labels.remove(idx);
                        } else if (prepared > 1) {
                            prepared--;
                            if (prepared < left_today)
                                left_today = prepared;
                            CharacterList.chosenCharacter.removeSpell(sp);
						/*
						sql.updatePreparedSpellByName(CharacterList.chosenCharacter.getCharId(), sp
								.getName(), prepared, left_today, sp.getLvl());
						*/
                            //Log.d("LIST", "Removing: " + sp.getName()+ " from the prepared spells list.");
                        }

                        sql.removePreparedSpell(CharacterList.chosenCharacter.getCharId(), sp.getId(), sp.getName());
                        break;

                    case Constants.LONG_CLICK_USE_SPELL:
                        left_today--;
                        CharacterList.chosenCharacter.useSpell(sp);

                        sql.useSpell(CharacterList.chosenCharacter.getCharId(), sp.getId(), sp.getName());
					/*
					sql.updatePreparedSpellByName(CharacterList.chosenCharacter.getCharId(), sp
							.getName(), prepared, left_today, sp.getLvl());
							*/
                        break;

                }

                header.setText(CharacterList.chosenCharacter.getSpanString(headerLines));
                adapter.notifyDataSetChanged();
                return true;
            }
        });
		spellList.setOnTouchListener(gestureListener);
	}

	private void reloadList()
	{
		CharacterList.chosenCharacter.sortPrepared();
		spell_labels.clear();
		spell_labels.addAll(CharacterList.chosenCharacter.getPrepared_spells());
		header.setText(CharacterList.chosenCharacter.getSpanString(headerLines));
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onPause() {
		currentPosition = spellList.getFirstVisiblePosition();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Log.d("EVENT", "onResume Triggered in PreparedList");
		
		// Open Database. 
		sql.open();
		
		adapter.setCharacter(CharacterList.chosenCharacter);
		
		// Check preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean defToAction = prefs.getBoolean(Constants.PREF_DEFAULT_TO, true);
		if(defToAction)
		{
			// Use default values for longclickmode. 
			String defTo =  prefs.getString(Constants.PREF_PREPLIST, ""+ Constants.LONG_CLICK_USE_SPELL);
			prevLongClickMode = Integer.parseInt(defTo);
			longClickMode = Integer.parseInt(defTo);
		}
		
		reloadList();
		super.onResume();
	}

	public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
	}

	@Override
	public void onBackPressed() {
		TabMain.tabHost.setCurrentTabByTag("class_spells");
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		
		switch (id) {
		
		case DIALOG_CONFIRM_CLEAR:
			// Building an alert dialog.
			builder = new AlertDialog.Builder(this);
			builder.setMessage("Really clear the prepared list?")
					.setCancelable(false).setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									sql.removeAllPreparedSpells(CharacterList.chosenCharacter
											.getCharId());
									CharacterList.chosenCharacter.removeAll();
									reloadList();
								}
							}).setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

    /**
     * Callback method for when "Longclick Remove" is called from the Longclick Mode dialog.
     * Sets the longclick mode to remove the spell from the list.
     */
    @Override
    public void longclickRemoveSelected() {
        longClickMode = Constants.LONG_CLICK_REMOVE_PREPARED;
    }

    /**
     * Callback method for when "Longclick Use" is called from the Longclick Mode dialog.
     * Sets the longclick mode to use one preperation of the spell from the list.
     */
    @Override
    public void longclickUseSelected() {
        longClickMode = Constants.LONG_CLICK_USE_SPELL;
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
					// Toast.makeText(PreparedList.this, "Left Swipe",
					// Toast.LENGTH_SHORT).show();
					/*
					 * Intent intent = new Intent().setClass(PreparedList.this,
					 * PreparedList.class);
					 * PreparedList.this.startActivity(intent);
					 */
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(vy) > SWIPE_THRESHOLD_VELOCITY) {
					Log.d("EVENT", "OnFling ------ Right Fling");
					// Toast.makeText(PreparedList.this, "Right Swipe",
					// Toast.LENGTH_SHORT).show();
					// Intent intent = new
					// Intent().setClass(getApplicationContext(),SpellDir_Test.class);
					// getApplicationContext().startActivity(intent);
					// swipe = 2;
					// finish();
					TabMain.tabHost.setCurrentTabByTag("class_spells");
					return true;
				}
			} catch (Exception e) {
				// nothing
			}
			return false;

		}
	}

}
