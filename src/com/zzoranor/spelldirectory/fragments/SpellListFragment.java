package com.zzoranor.spelldirectory.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.zzoranor.spelldirectory.CustomAdapter;
import com.zzoranor.spelldirectory.OnRefreshListener;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.SpellLabel;
import com.zzoranor.spelldirectory.activity.CharacterList;
import com.zzoranor.spelldirectory.activity.SingleSpell;
import com.zzoranor.spelldirectory.controllers.MainDrawerController;
import com.zzoranor.spelldirectory.data.Character;
import com.zzoranor.spelldirectory.database.DbAdapter;
import com.zzoranor.spelldirectory.fragments.dialogs.PrepareWithMetamagicDialogFragment;
import com.zzoranor.spelldirectory.fragments.dialogs.SpellsLongclickModeDialogFragment;
import com.zzoranor.spelldirectory.services.SqlService;
import com.zzoranor.spelldirectory.util.Constants;
import com.zzoranor.spelldirectory.util.SerializablePair;
import com.zzoranor.spelldirectory.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Fragment to hold and control the list of spells
 *
 * @author morrodin.
 */
public class SpellListFragment extends Fragment implements OnRefreshListener,
        SpellsLongclickModeDialogFragment.SpellLongclickModeCallback,
        PrepareWithMetamagicDialogFragment.PrepareWithMetamagicCallback {

    private Character chosenCharacter;

    private SqlService mSqlService;

    private ArrayList<SpellLabel> spell_labels;
    private boolean all_classes;
    private boolean all_levels;
    private int chosen_level;
    private int currentPosition = -1;
    private DbAdapter sql;

    private MainDrawerController mDrawerController;

    private DrawerLayout mDrawer;
    private CustomAdapter adapter;
    private RelativeLayout headerContainer;
    private TextView header;
    private TextView headerClassField;
    private ImageView search_button;
    private Context context;
    private ListView spellList;
    private TextView view_prepare;
    private TextView view_known;

    private View view;

    private int prepared;
    private int left_today;

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

    /**
     * Constructor
     *
     * @param chosenCharacter
     *          Currently chosen character.
     */
    public SpellListFragment(Character chosenCharacter, SqlService sqlService) {
        this.chosenCharacter = chosenCharacter;
        this.mSqlService = sqlService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.spell_list_fragment, container, false);

        mDrawer = (DrawerLayout) getActivity().findViewById(R.id.tab_management_drawer_layout);
        mDrawer.setFocusable(false);

        mDrawerController = new MainDrawerController(getActivity(), mDrawer);

        setUpHeader();
        wireSearchButton();

        spell_labels = new ArrayList<SpellLabel>();
        sql = mSqlService.getSqlAdapter();

        // Initiate list Adapter for first-time-use.
        initList();
        // Load/Reload data from the database to populate list.
        reloadListFromDB();

        return view;
    }

    /**
     * Initiates the ListAdapter by setting various parameters
     * and connecting onClick and onLongClick listeners.
     */
    private void initList(){

        adapter = new CustomAdapter(getActivity(), R.layout.spell_list_item2,
                R.id.list_view_name, chosenCharacter, spell_labels);
        spellList = (ListView) view.findViewById(R.id.spell_list_view);
        spellList.setOnTouchListener(gestureListener);
        spellList.setAdapter(adapter);

        // Enable search.
        spellList.setTextFilterEnabled(true);

        spellList.setOnItemClickListener(this.listItemOnClickListener());
        spellList.setOnItemLongClickListener(this.listItemOnLongClickListener());
    }

    /**
     * Reloads the list by fetching latest data from the DB.
     */
    private void reloadListFromDB() {
        adapter.setCharacter(chosenCharacter);
        populateListContainerFromDb();
        header.setText(chosenCharacter.getSpanString(headerLines));
        adapter.notifyDataSetChanged();
    }

    /**
     * Clears the current containers holding data for the ListAdapter
     * and repopulates using the sql adapter.
     */
    private void populateListContainerFromDb() {
        sql.populateMetamagicsFromDB();
        spell_labels.clear();

        SpellLabel[] known_spells = sql.getKnownSpells(chosenCharacter.getCharId());
        Arrays.sort(known_spells);

        Log.d("Event", "Spelllist triggered: " + chosenCharacter.getCurrClassId());

        if(chosenCharacter.getCurrClassId() == DbAdapter.KNOWN_SPELLS_CLASS_ID)
        {
            browsingKnownSpells = true;
            // Get spells from dynamic database if it is the known spells class.
            Collections.addAll(spell_labels, known_spells);

        }else {
            browsingKnownSpells = false;

            SpellLabel[] labels = sql.getSpellsFromList(chosenCharacter.getCurrClassId());

            for (SpellLabel l : labels) {
                SerializablePair<Integer, Integer> prep = chosenCharacter.getUsedPrepared(l.getName());
                l.setPrepared(prep);
                int cmp = Arrays.binarySearch(known_spells, l);
                if (cmp >= 0)
                    l.setKnown(true);
                spell_labels.add(l);
            }
        }
    }

    /**
     * Listener for long clicking on a list item
     *
     * @return
     *      boolean
     */
    protected AdapterView.OnItemLongClickListener listItemOnLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                SpellLabel sp = (SpellLabel) parent.getItemAtPosition(position);

                SerializablePair<Integer, Integer> numPrepared = chosenCharacter.getUsedPrepared(sp);
                view_prepare = (TextView) view.findViewById(R.id.list_view_prepare);
                view_known = (TextView) view.findViewById(R.id.list_view_spell_known);
                prepared = numPrepared.second;
                left_today = numPrepared.first;

                switch (longClickMode) {

                    case Constants.LONG_CLICK_PREPARE:
                        prepareSpell(sp);
                        break;

                    case Constants.LONG_CLICK_REMOVE_PREPARED:
                        removePreparedSpell(sp);
                        break;

                    case Constants.LONG_CLICK_METAMAGIC:
                        prepareWithMetamagic(sp, parent);
                        break;

                    case Constants.LONG_CLICK_KNOWN_SPELL:
                        if (!browsingKnownSpells) {    // Not browsing Known spells. This menu button adds spell to known spells.
                           addToKnownSpells(sp);
                        } else {
                            // If we are browsing known spells, we instead remove the spell from known spells.
                            removePreparedSpell(sp);
                        }
                        break;
                }
                adapter.notifyDataSetChanged();
                header.setText(chosenCharacter.getSpanString(headerLines));
                return true;
            }
        };
    }

    /**
     * Handles logic for adding a spell to the list of known spells.
     *
     * @param sp
     *          Spell to add to known spells.
     */
        private void addToKnownSpells(SpellLabel sp) {
                Log.d(tag, "Adding spell to Known Spells: " + sp.getName() + " for character: " + chosenCharacter.getCharId());

                if (sp.getId() > -1) {
                    //sql.addSpellAsKnown(CharacterList.getChosenCharacterId(),sp.getId(), sp.getLvl());
                    sql.addKnownSpell(chosenCharacter.getCharId(), sp.getId(), sp.getLvl());
                } else {
                    Log.e("SpellList", "SpellLabel has id < 0 when adding known spell. ");
                }
                view_known.setText("Known");
                sp.setKnown(true);
        }

    /**
     * Handles the logic for removing a spell from the known spell list.
     *
     * @param sp
     *          Spell to be removed from the known spells list.
     */
    private void removeFromKnownSpells(SpellLabel sp) {
        Log.d(tag, "Removing spell from Known Spells: " + sp.getName() + " for character: " + chosenCharacter.getCharId());

        if (sp.getId() > -1) {
            sql.removeSpellFromKnown(chosenCharacter.getCharId(), sp.getId());
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

        /**
         * Handles the logic for preparing a spell.
         *
         * @param sp
         *          SpellLabel for the spell being prepared.
         */
    private void prepareSpell(SpellLabel sp) {
        // Preparing spells using longclick.
        Log.d("SpellList", "Preparing Spell: " + sp.getName() + " to character: " + chosenCharacter.getCharId());

        prepared++;
        left_today++;
        // Update database.
        sql.addPreparedSpell(chosenCharacter.getCharId(), sp.getId(), sp.getName(), sp.getLvl(),
                chosenCharacter.getCurrClassId(), Constants.SPELL_KNOWN_DEFAULT, left_today, prepared);
        // Update List.
        chosenCharacter.prepareSpell(sp, 1);

        view_prepare.setText("" + left_today + "/" + prepared);
        Log.d("LIST", "Adding: " + sp.getName()
                + " to the prepared spells list.");
    }


    /**
     * Handles the logic for preparing a spell with Metamagic
     *
     * @param sp
     *          Spell to be prepared with metamagic.
     * @param  parent
     *          AdapterView which is holding the corresponding list item.
     */
    private void prepareWithMetamagic(SpellLabel sp, AdapterView parent) {
        if (!metaChosen.equals("")) {
            String metaName = "" + metaChosen;
            metaName = metaChosen.split("\t")[0];

            String metaSpellName = metaName + " " + sp.getName();
            int newlvl = sp.getLvl() + sql.getMetamagicAdjustment(metaName);

            SerializablePair<Integer, Integer> metaPrepPair = chosenCharacter.getUsedPrepared(metaSpellName);
            int metaPrepared = metaPrepPair.second;
            int metaLeftToday = metaPrepPair.first;
            metaPrepared++;
            metaLeftToday++;

            // update database
            sql.addPreparedSpell(chosenCharacter.getCharId(), sp.getId(), metaSpellName,
                    newlvl, chosenCharacter.getCurrClassId(), Constants.SPELL_KNOWN_DEFAULT, metaLeftToday, metaPrepared);

            // Update List
            SpellLabel newSp = new SpellLabel(metaSpellName, sp.getId(), newlvl, sp.getSchool(), metaLeftToday, metaPrepared);
            chosenCharacter.prepareSpell(newSp, 1);

            // Notify that it is prepared.
            Toast.makeText(parent.getContext(), metaSpellName + " prepared.", Toast.LENGTH_SHORT).show();

            longClickMode = prevLongClickMode;
        }
    }


    /**
     * Handles the logic for removing a prepared spell
     *
     * @param sp
     *          Spell which is being removed from the prepared spells.
     */
    private void removePreparedSpell(SpellLabel sp) {
        // Removing spells using longclick.
        if (prepared == 1) {
            view_prepare.setText("");

            chosenCharacter.removeSpell(sp);
        } else if (prepared > 1) {
            prepared--;

            if (prepared < left_today) {
                left_today = prepared;
            }

            chosenCharacter.removeSpell(sp);
            view_prepare.setText("" + left_today + "/" + prepared);

            Log.d("LIST", "Removing: " + sp.getName()
                    + " to the prepared spells list.");
        }
        sql.removePreparedSpell(chosenCharacter.getCharId(), sp.getId(), sp.getName());
    }

    /**
     * Sets up the OnItemClickListener for individual list items.
     * @return
     */
    protected AdapterView.OnItemClickListener listItemOnClickListener() {
        return new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                SpellLabel sp = (SpellLabel) parent.getItemAtPosition(position);

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
//                    TabMain.tabHost.setCurrentTabByTag("prepared_spells");
                } else {
                    //Log.d("EVENT", "-------- SWIPE = 2");
                    swipe = 0;
                    getActivity().finish();
                }
            }
        };
    }

    /**
     * Sets up search button
     */
    private void wireSearchButton() {
        // Search Button
        search_button = (ImageView) view.findViewById(R.id.searchbutton);
        search_button.setFocusable(false);
        search_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) getActivity()
                        .getSystemService(getActivity().INPUT_METHOD_SERVICE);
                inputManager.toggleSoftInput(0, 0);
            }
        });
    }

    //TODO: Find out if this is needed/is called
    @Override
    public void onResume() {
        super.onResume();
        setUpHeader();
    }

    private void setUpHeader() {
        RelativeLayout headerContainer = (RelativeLayout) view.findViewById(R.id.header_container);
        header = (TextView) headerContainer.findViewById(R.id.header_text);
        if(Utility.getScreenSizeCategory(getActivity()) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            headerLines = 3;
        }else
        {
            headerLines = 2;
        }
        header.setText(chosenCharacter.getSpanString(headerLines));
        header.setFocusable(false);

        // Class Name Field
        headerClassField = (TextView) view.findViewById(R.id.headerClassField);
        headerClassField.setText(chosenCharacter.getCurrClassName());
    }


    /**
     * Listener for the Prepare with Metamagic drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Backup link
     */
    protected View.OnClickListener metamagicLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevLongClickMode = longClickMode;
                longClickMode = Constants.LONG_CLICK_METAMAGIC;
                PrepareWithMetamagicDialogFragment dialog =
                        new PrepareWithMetamagicDialogFragment(SpellListFragment.this, sql.getMetamagicList());
                dialog.show(getActivity().getSupportFragmentManager(), "metamagic");
                mDrawer.closeDrawers();
            }
        };
    }


    /**
     * Listener for the Longclick Mode drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Longclick Mode link
     */
    protected View.OnClickListener longclickModeLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //We new this each time in order to show the correct currently selected value
                SpellsLongclickModeDialogFragment dialog =
                        new SpellsLongclickModeDialogFragment(SpellListFragment.this, longClickMode, browsingKnownSpells);
                dialog.show(getActivity().getSupportFragmentManager(), "longclickMode");
                mDrawer.closeDrawers();
            }
        };
    }

    @Override
    public void onRefresh() {
        setUpHeader();
        mDrawerController.initDrawer();
        mDrawerController.setUpSpellListDrawer();

        TextView metamagicLink = (TextView) getActivity().findViewById(R.id.drawer_metamagic_link);
        metamagicLink.setOnClickListener(this.metamagicLinkListener());

        TextView longclickModeLink = (TextView) getActivity().findViewById(R.id.drawer_longclick_link);
        longclickModeLink.setOnClickListener(this.longclickModeLinkListener());

        reloadListFromDB();
        adapter.notifyDataSetChanged();
//        adapter.getFilter().filter(null);
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

    @Override
    public void metamagicConfirmCallback(String item) {
        metaChosen = item;
        Toast.makeText(getActivity(),
                "Long-click on a spell to prepare with Metamagic." +
                        " ", Toast.LENGTH_SHORT).show();
    }
}
