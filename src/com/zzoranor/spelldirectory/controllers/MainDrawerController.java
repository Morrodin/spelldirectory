package com.zzoranor.spelldirectory.controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.TextView;
import com.zzoranor.spelldirectory.R;
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

    DrawerLayout drawer;
    Activity mActivity;

    public MainDrawerController(Activity activity, DrawerLayout drawer) {
        this.drawer = drawer;
        this.mActivity = activity;
    }


    public void setupUniversalDrawerLinks() {
        TextView showPreferencesLink = (TextView) mActivity.findViewById(R.id.drawer_show_preferences_link);
        showPreferencesLink.setOnClickListener(this.showPreferencesLinkListener());

        TextView showHelpLink = (TextView) mActivity.findViewById(R.id.drawer_help_link);
        showHelpLink.setOnClickListener(this.showHelpLinkListener());

        TextView showLicenseInfoLink = (TextView) mActivity.findViewById(R.id.drawer_license_info_link);
        showLicenseInfoLink.setOnClickListener(this.showLicenseInfoLinkListener());
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
        drawer.closeDrawers();
    }

    /**
     * Launches a new intent for the app's help page
     */
    private void showHelp() {
        Intent intent = new Intent(mActivity, HelpActivity.class);
        mActivity.startActivity(intent);
        drawer.closeDrawers();
    }

    /**
     * Launches a new intent to display the Pathfinder OGC License info
     */
    private void showLicenseInfo(){
        Intent intent = new Intent(mActivity, InfoActivity.class);
        mActivity.startActivity(intent);
        drawer.closeDrawers();
    }


}
