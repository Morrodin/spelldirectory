package com.zzoranor.spelldirectory.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.zzoranor.spelldirectory.fragments.ClassListFragment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Kenton
 * Date: 6/16/13
 * Time: 1:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    private static final String[] CONTENT = new String[] {"Classes", "Spell List", "Prepared Spells"};

    private int currentFragment;
    private List<Fragment> fragments;

    public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        currentFragment = position;
        return this.fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return CONTENT[position % CONTENT.length].toUpperCase();
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    public int getCurrentFragment() {
        return currentFragment;
    }

    public void setCurrentFragment(int currentFragment) {
        this.currentFragment = currentFragment;
    }
}
