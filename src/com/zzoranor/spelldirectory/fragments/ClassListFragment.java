package com.zzoranor.spelldirectory.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.zzoranor.spelldirectory.ClassLabel;
import com.zzoranor.spelldirectory.CustomClassAdapter;
import com.zzoranor.spelldirectory.OnRefreshListener;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.activity.CharacterList;
import com.zzoranor.spelldirectory.activity.TabManagementActivity;
import com.zzoranor.spelldirectory.adapters.PagerAdapter;
import com.zzoranor.spelldirectory.controllers.MainDrawerController;
import com.zzoranor.spelldirectory.services.SqlService;
import com.zzoranor.spelldirectory.data.Character;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Fragment containing the list of Classes
 */
public class ClassListFragment extends Fragment implements OnRefreshListener {

    private Character chosenCharacter;

    private MainDrawerController mDrawerController;
    private SqlService mSqlService;

    private View view;
    private DrawerLayout mDrawer;
    private ArrayList<ClassLabel> class_names;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private int swipe = 0;
    private CustomClassAdapter adapter;

    private ViewPager mViewPager;

    private ListView classList;

    /**
     * Constructor
     *
     * @param chosenCharacter
     *          Currently chosen character.
     */
    public ClassListFragment(Character chosenCharacter, SqlService sqlService) {
        this.chosenCharacter = chosenCharacter;
        this.mSqlService = sqlService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.class_list_fragment, container, false);
        Log.d("START", "Starting Spell list.");

        DrawerLayout mDrawer = (DrawerLayout) getActivity().findViewById(R.id.tab_management_drawer_layout);

        mDrawerController = new MainDrawerController(getActivity(), mDrawer);
        mDrawerController.initDrawer();
        mDrawerController.setUpClassListDrawer();

        class_names = new ArrayList<ClassLabel>();
        classList = (ListView) view.findViewById(R.id.class_list);

        initList();

        mViewPager = (ViewPager) getActivity().findViewById(R.id.main_content_pager);

        return view;
    }


    private void initList() {
        ClassLabel labels[] = mSqlService.getSqlAdapter().getClasses();
        Collections.addAll(class_names, labels);

        adapter = new CustomClassAdapter(getActivity(),
                R.layout.class_list_item, R.id.class_view_id, chosenCharacter, class_names);
        classList.setAdapter(adapter);

        classList.setTextFilterEnabled(true);
        classList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                ClassLabel label = (ClassLabel) parent.getItemAtPosition(position);
                mSqlService.getSqlAdapter().changeChosenClass(chosenCharacter.getCharId(), label.getName(), label.getId());
                chosenCharacter.setCurrentClass(label.getId(), label.getName());

                //Switch tab to Spell List
                adapter.notifyDataSetChanged();
                mViewPager.setCurrentItem(1);

                if (swipe == 0) {
                    //Intent spells_intent = new Intent().setClass(context,
                    //		SpellList.class);
                    //CharacterList.chosenCharacter.curr_class_name = s;
                    //CharacterList.chosenCharacter.curr_class_id = classes.get(s);

//					TabMain.tabHost.setCurrentTabByTag("class_spells");

                    //spells_intent.putExtra("spellList.class_name", s);
                    //spells_intent.putExtra("spellList.class_id", classes.get(s));
                    //context.startActivity(spells_intent);
                } else if (swipe == 1) {
                    swipe = 0;
                    //Intent spells_intent = new Intent().setClass(context,
                    //		PreparedList.class);
                    //context.startActivity(spells_intent);
//					TabMain.tabHost.setCurrentTabByTag("prepared_spells");
                }
            }
        });
        classList.setOnTouchListener(gestureListener);

    }

    public CustomClassAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(CustomClassAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onRefresh() {
        classList.invalidate();

        mDrawerController.initDrawer();
        mDrawerController.setUpClassListDrawer();
    }
}
