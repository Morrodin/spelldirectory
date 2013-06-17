package com.zzoranor.spelldirectory.controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.activity.CharacterList;
import com.zzoranor.spelldirectory.activity.HelpActivity;
import com.zzoranor.spelldirectory.activity.InfoActivity;
import com.zzoranor.spelldirectory.activity.SpellPreferences;
import com.zzoranor.spelldirectory.util.Constants;

import java.util.zip.Inflater;

/**
 * @author morrodin
 *
 * Controller class for universal drawer functions such as wiring links. Any activity using the drawer should
 * include an instance of this.
 */
public class MainDrawerController{

    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    Activity mActivity;

    LinearLayout spellListDrawerView;
    LinearLayout preparedListDrawerView;
    LinearLayout classListDrawerView;

    public MainDrawerController(Activity activity, DrawerLayout drawer) {
        this.mDrawerLayout = drawer;
        this.mActivity = activity;
    }


    public void wireUniversalLinks() {
        TextView showPreferencesLink = (TextView) mActivity.findViewById(R.id.drawer_show_preferences_link);
        showPreferencesLink.setOnClickListener(this.showPreferencesLinkListener());

        TextView showHelpLink = (TextView) mActivity.findViewById(R.id.drawer_help_link);
        showHelpLink.setOnClickListener(this.showHelpLinkListener());

        TextView showLicenseInfoLink = (TextView) mActivity.findViewById(R.id.drawer_license_info_link);
        showLicenseInfoLink.setOnClickListener(this.showLicenseInfoLinkListener());

        TextView reChooseCharacterLink = (TextView) mActivity.findViewById(R.id.drawer_rechoose_character_link);
        reChooseCharacterLink.setOnClickListener(this.reChooseCharacterLinkListener());

    }

    public void initDrawer() {
        wireUniversalLinks();
        initializeDrawerLayouts();
        mDrawerLayout.setDrawerShadow(mActivity.getResources().getDrawable(R.drawable.drawer_shadow),
                Gravity.LEFT);

        mDrawerToggle = new ActionBarDrawerToggle(
                mActivity,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
//                getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
//                getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mActivity.getActionBar().setDisplayHomeAsUpEnabled(true);
        mActivity.getActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Initializes all the separate layouts for different versions of the drawer
     */
    private void initializeDrawerLayouts() {
        spellListDrawerView = (LinearLayout) mActivity.findViewById(R.id.spell_list_drawer_layout);
        preparedListDrawerView = (LinearLayout) mActivity.findViewById(R.id.prepared_list_drawer_layout);
    }

    /**
     * Creates the neccessary view for the class list drawer
     */
    public void setUpClassListDrawer() {
        spellListDrawerView.setVisibility(View.GONE);
        preparedListDrawerView.setVisibility(View.GONE);
    }

    /**
     * Creates the neccessary view for the spell list drawer
     */
    public void setUpSpellListDrawer() {
        spellListDrawerView.setVisibility(View.VISIBLE);
        preparedListDrawerView.setVisibility(View.GONE);
    }

    public void setUpPreparedListDrawer() {
        spellListDrawerView.setVisibility(View.GONE);
        preparedListDrawerView.setVisibility(View.VISIBLE);
    }

    protected View.OnClickListener reChooseCharacterLinkListener() {
       return new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               reChooseCharacter();
           }
       };
    }

    /**
     * Listener for the Show Preferences drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Show Preferences link
     */
    protected View.OnClickListener showPreferencesLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreferences();
            }
        };
    }

    /**
     * Listener for the Help drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for Help link
     */
    protected View.OnClickListener showHelpLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelp();
            }
        };
    }

    /**
     * Listener for the License Info drawer link
     *
     * @return
     *          {@link android.view.View.OnClickListener} for License Info link
     */
    protected View.OnClickListener showLicenseInfoLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLicenseInfo();
            }
        };
    }




    /**
     * Launches a new intent for the app's preferences
     */
    private void showPreferences() {
        Intent intent = new Intent(mActivity, SpellPreferences.class);
        mActivity.startActivityForResult(intent, Constants.SPELL_PREFERENCES);
        mDrawerLayout.closeDrawers();
    }

    /**
     * Launches a new intent for the app's help page
     */
    private void showHelp() {
        Intent intent = new Intent(mActivity, HelpActivity.class);
        mActivity.startActivity(intent);
        mDrawerLayout.closeDrawers();
    }

    private void reChooseCharacter() {
        Intent intent = new Intent(mActivity, CharacterList.class);
        mActivity.startActivity(intent);
        mDrawerLayout.closeDrawers();
    }

    /**
     * Launches a new intent to display the Pathfinder OGC License info
     */
    private void showLicenseInfo(){
        Intent intent = new Intent(mActivity, InfoActivity.class);
        mActivity.startActivity(intent);
        mDrawerLayout.closeDrawers();
    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }

    public void setDrawerToggle(ActionBarDrawerToggle mDrawerToggle) {
        this.mDrawerToggle = mDrawerToggle;
    }
}
