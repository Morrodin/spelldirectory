package com.zzoranor.spelldirectory.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Pair;
import android.view.*;
import android.widget.*;
import com.zzoranor.spelldirectory.CustomAdapter;
import com.zzoranor.spelldirectory.OnRefreshListener;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.SpellLabel;
import com.zzoranor.spelldirectory.activity.SingleSpell;
import com.zzoranor.spelldirectory.controllers.MainDrawerController;
import com.zzoranor.spelldirectory.data.Character;
import com.zzoranor.spelldirectory.database.DbAdapter;
import com.zzoranor.spelldirectory.fragments.dialogs.ConfirmClearPreparedSpellListDialogFragment;
import com.zzoranor.spelldirectory.fragments.dialogs.PreparedSpellsLongclickModeDialogFragment;
import com.zzoranor.spelldirectory.services.SqlService;
import com.zzoranor.spelldirectory.util.Constants;
import com.zzoranor.spelldirectory.util.SerializablePair;
import com.zzoranor.spelldirectory.util.Utility;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Fragment to hold and control the list of prepared spells.
 *
 * @author morrodin
 */
public class PreparedListFragment extends Fragment implements OnRefreshListener,
        PreparedSpellsLongclickModeDialogFragment.PreparedLongclickModeCallback,
        ConfirmClearPreparedSpellListDialogFragment.ConfirmListClearCallback {

    private Character chosenCharacter;
    private SqlService mSqlService;

    private DbAdapter sql;

    private PreparedSpellsLongclickModeDialogFragment mLongclickModeDialog;

    private MainDrawerController mDrawerController;

    private static final int DIALOG_CONFIRM_CLEAR = 1;

    private int left_today;
    private int prepared;

    private int swipe = 0;
    private int headerLines;
    private int currentPosition = -1;
    private int longClickMode = Constants.LONG_CLICK_USE_SPELL;
    private int prevLongClickMode = Constants.LONG_CLICK_USE_SPELL;


    private View view;
    private DrawerLayout mDrawer;
    private ArrayList<SpellLabel> spell_labels;
    private CustomAdapter adapter;
    private TextView header;
    private TextView headerClassField;
    private ImageView search_button;
    private ListView spellList;

    /**
     * Constructor
     *
     * @param chosenCharacter
     *          Currently chosen character.
     */
    public PreparedListFragment(Character chosenCharacter, SqlService sqlService) {
        this.chosenCharacter = chosenCharacter;
        this.mSqlService = sqlService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.spell_list_prepared_fragment, container, false);

        mDrawer = (DrawerLayout) getActivity().findViewById(R.id.tab_management_drawer_layout);
        mDrawer.setFocusable(false);

        mDrawerController = new MainDrawerController(getActivity(), mDrawer);
        mDrawerController.initDrawer();

        spell_labels = new ArrayList<SpellLabel>();

        // Loading Spell File.
        sql = mSqlService.getSqlAdapter();

        setUpHeader();
        initList();

        //Setup and initialize drawer
        setupDrawer();

        return view;
    }


    /**
     * Wires the buttons in the navigation drawer for this activity.
     */
    private void setupDrawer() {
        mDrawerController.initDrawer();

        TextView resetUsesLink = (TextView) getActivity().findViewById(R.id.drawer_reset_uses_link);
        resetUsesLink.setOnClickListener(this.resetUsesLinkListener());

        TextView clearListLink = (TextView) getActivity().findViewById(R.id.drawer_clear_list_link);
        clearListLink.setOnClickListener(this.clearListLinkListener());

        TextView longclickModeLink = (TextView) getActivity().findViewById(R.id.drawer_longclick_prepared_link);
        longclickModeLink.setOnClickListener(longclickModeLinkListener());
    }


    protected View.OnClickListener resetUsesLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSpellUses();
                mDrawer.closeDrawers();
            }
        };
    }

    protected View.OnClickListener clearListLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmClearPreparedSpellListDialogFragment dialog =
                        new ConfirmClearPreparedSpellListDialogFragment(PreparedListFragment.this);
                dialog.show(getActivity().getSupportFragmentManager(), "confirmListClear");
                mDrawer.closeDrawers();
            }
        };
    }

    protected View.OnClickListener longclickModeLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //New this every time so we can show the correct current selection
                PreparedSpellsLongclickModeDialogFragment mLongclickModeDialog =
                        new PreparedSpellsLongclickModeDialogFragment(PreparedListFragment.this, longClickMode);
                mLongclickModeDialog.show(getActivity().getSupportFragmentManager(), "longclickMode");
                mDrawer.closeDrawers();
            }
        };
    }

    private void resetSpellUses() {
        chosenCharacter.resetAllSpellUses();
        sql.resetAllUses(chosenCharacter.getCharId());
        adapter.notifyDataSetChanged();
        mDrawer.closeDrawers();
    }

    /**
     * Initializes the header with the chosen character's class name, as well as setting up the prepared spells
     * header.
     */
    private void setUpHeader() {
        // Prepared Spells header field.
        header = (TextView) view.findViewById(R.id.header_text);
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
        headerClassField.setVisibility(View.GONE);
    }

    /**
     * This method prepares the spells from database for the defined
     * character.
     */
    private void initList() {

        RelativeLayout headerContainer = (RelativeLayout) view.findViewById(R.id.header_container);

        // List adapter

        spellList = (ListView) view.findViewById(R.id.prepared_spells_list_view);
        spellList.setTextFilterEnabled(true);

        adapter = new CustomAdapter(getActivity(), R.layout.spell_list_item2,
                R.id.list_view_name, chosenCharacter, spell_labels);
        spellList.setAdapter(adapter);

        spellList.setOnItemClickListener(this.preparedSpellListItemClickListener());
        spellList.setOnItemLongClickListener(this.preparedSpellListItemLongClickListener());
    }

    /**
     * Sets up and returns an {@link android.widget.AdapterView.OnItemClickListener}
     * for the prepared spell list.
     *
     * @return
     *          {@link android.widget.AdapterView.OnItemClickListener}
     */
    private AdapterView.OnItemClickListener preparedSpellListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
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
                    getActivity().finish();
                }

            }
        };
    }

    /**
     * Sets up and returns an {@link android.widget.AdapterView.OnItemLongClickListener}
     * for the prepared spell list.
     *
     * @return
     *          {@link android.widget.AdapterView.OnItemLongClickListener}
     */
    private AdapterView.OnItemLongClickListener preparedSpellListItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                SpellLabel sp = (SpellLabel) parent.getItemAtPosition(position);

                SerializablePair<Integer, Integer> numPrepared = chosenCharacter
                        .getUsedPrepared(sp);
                prepared = numPrepared.second;
                left_today = numPrepared.first;
                switch (longClickMode) {

                    case Constants.LONG_CLICK_REMOVE_PREPARED:
                        removePreparedSpell(sp);
                        break;

                    case Constants.LONG_CLICK_USE_SPELL:
                        usePreparedSpell(sp);
                        break;

                }

                header.setText(chosenCharacter.getSpanString(headerLines));
                adapter.notifyDataSetChanged();
                return true;
            }
        };
    }

    /**
     * Removes one instance of a prepared spell from the list,
     * and updates both the database and view accordingly.
     *
     * @param sp
     *          Spell to be removed.
     */
    private void removePreparedSpell(SpellLabel sp) {
        if (prepared == 1) {
            chosenCharacter.removeSpell(sp);
            int idx = Collections.binarySearch(spell_labels, sp);
            spell_labels.remove(idx);
        } else if (prepared > 1) {
            prepared--;
            if (prepared < left_today)
                left_today = prepared;
            chosenCharacter.removeSpell(sp);
        }
        sql.removePreparedSpell(chosenCharacter.getCharId(), sp.getId(), sp.getName());
    }


    /**
     * Uses one preparation of a prepared spell. Updates the DB and the view accordingly.
     *
     * @param sp
     *          Spell to be used.
     */
    private void usePreparedSpell(SpellLabel sp) {
        left_today--;
        chosenCharacter.useSpell(sp);
        sql.useSpell(chosenCharacter.getCharId(), sp.getId(), sp.getName());
    }

    /**
     * Reloads the list to reflect any changes to the chosen character's
     * prepared spells.
     */
    private void reloadList() {
        chosenCharacter.sortPrepared();
        spell_labels.clear();
        spell_labels.addAll(chosenCharacter.getPreparedSpells());
        header.setText(chosenCharacter.getSpanString(headerLines));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        setUpHeader();

        spellList.invalidate();
        mDrawerController.initDrawer();
        mDrawerController.setUpPreparedListDrawer();

        reloadList();
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

    @Override
    public void listClearCallback() {
        sql.removeAllPreparedSpells(chosenCharacter
                .getCharId());
        chosenCharacter.removeAll();
        reloadList();
    }
}
