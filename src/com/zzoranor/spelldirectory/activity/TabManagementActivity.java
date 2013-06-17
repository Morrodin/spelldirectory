package com.zzoranor.spelldirectory.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import com.viewpagerindicator.TabPageIndicator;
import com.zzoranor.spelldirectory.OnRefreshListener;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.adapters.PagerAdapter;
import com.zzoranor.spelldirectory.controllers.MainDrawerController;
import com.zzoranor.spelldirectory.fragments.ClassListFragment;
import com.zzoranor.spelldirectory.fragments.PreparedListFragment;
import com.zzoranor.spelldirectory.fragments.SpellListFragment;
import com.zzoranor.spelldirectory.data.Character;
import com.zzoranor.spelldirectory.services.SqlService;

import java.util.List;
import java.util.Vector;

/**
 * Holder activity for ClassListFragment, SpellListFragment, and PreparedListFragment
 *
 * @author morrodin
 */
public class TabManagementActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

    private Character chosenCharacter;

    private MainDrawerController mDrawerController;

    private SqlService mSqlService;

    private ClassListFragment classListFragment;
    private SpellListFragment spellListFragment;
    private PreparedListFragment preparedListFragment;

    private PagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_management_activity);


        DrawerLayout mDrawer = (DrawerLayout) findViewById(R.id.tab_management_drawer_layout);
        mDrawerController = new MainDrawerController(this, mDrawer);
        mDrawerController.initDrawer();

        mSqlService = new SqlService(this);
        mSqlService.setupSql();

        //Find our chosen character and wire each of our fragments with it, since they all need access.
        chosenCharacter = (Character) getIntent().getSerializableExtra("chosenChar");
        initializeFragments();

        instantiatePaging();
    }

    /**
     * Sets up our ClassListFragment, SpellListFragment, and PreparedListFragment
     */
    private void initializeFragments() {
        classListFragment = new ClassListFragment(chosenCharacter, mSqlService);
        spellListFragment = new SpellListFragment(chosenCharacter, mSqlService);
        preparedListFragment = new PreparedListFragment(chosenCharacter, mSqlService);
    }

    private void instantiatePaging() {
        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(classListFragment);
        fragments.add(spellListFragment);
        fragments.add(preparedListFragment);
        this.mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);

        mViewPager = (ViewPager) findViewById(R.id.main_content_pager);
        mViewPager.setAdapter(this.mPagerAdapter);

        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.tab_indicator);
        indicator.setViewPager(mViewPager);
        indicator.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        //Not needed
    }

    @Override
    public void onPageSelected(int position) {
        ((OnRefreshListener) mPagerAdapter.getItem(position))
                .onRefresh();
    }

    @Override
    public void onPageScrollStateChanged(int i) {
        //Not neeeded
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerController.getDrawerToggle().onOptionsItemSelected(item)) {
            return true;
        }

        switch(item.getItemId()) {
            case R.id.menu_search:
                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputManager.toggleSoftInput(0, 0);
        }

        return super.onOptionsItemSelected(item);
    }
}
