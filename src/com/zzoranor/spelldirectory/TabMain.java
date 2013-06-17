//package com.zzoranor.spelldirectory;
//
//import com.zzoranor.spelldirectory.R;
//import com.zzoranor.spelldirectory.R.drawable;
//import com.zzoranor.spelldirectory.R.layout;
//import com.zzoranor.spelldirectory.activity.CharacterList;
//import com.zzoranor.spelldirectory.activity.ClassList;
//import com.zzoranor.spelldirectory.activity.PreparedList;
//import com.zzoranor.spelldirectory.activity.SpellList;
//
//import android.app.TabActivity;
//import android.content.Intent;
//import android.content.res.Resources;
//import android.os.Bundle;
//import android.widget.TabHost;
//
//public class TabMain extends TabActivity{
//	public static TabHost tabHost;
//
//	public void onCreate(Bundle savedInstanceState) {
//	    super.onCreate(savedInstanceState);
//	    setContentView(R.layout.tab_layout);
//
//	    Resources res = getResources(); // Resource object to get Drawables
//	    tabHost = getTabHost();  // The activity TabHost
//	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
//	    Intent intent;  // Reusable Intent for each tab
//
//	    // Create an Intent to launch an Activity for the tab (to be reused)
//	    intent = new Intent().setClass(this, CharacterList.class);
//
//	    // Initialize a TabSpec for each tab and add it to the TabHost
//	    spec = tabHost.newTabSpec("characters").setIndicator("Characters",
//	                      res.getDrawable(R.drawable.ic_tab_spellbook))
//	                  .setContent(intent);
//	    tabHost.addTab(spec);
//
//	    intent = new Intent().setClass(this, ClassList.class);
//	    // Initialize a TabSpec for each tab and add it to the TabHost
//	    spec = tabHost.newTabSpec("classes").setIndicator("Classes",
//	                      res.getDrawable(R.drawable.ic_tab_spellbook))
//	                  .setContent(intent);
//	    tabHost.addTab(spec);
//
//	    // Do the same for the other tabs
//	    intent = new Intent().setClass(this, SpellList.class);
//	    spec = tabHost.newTabSpec("class_spells").setIndicator("Class Spells",
//	                      res.getDrawable(R.drawable.ic_tab_spellbook))
//	                  .setContent(intent);
//	    tabHost.addTab(spec);
//
//	    // Do the same for the other tabs
//	    intent = new Intent().setClass(this, PreparedList.class);
//	    spec = tabHost.newTabSpec("prepared_spells").setIndicator("Prepared Spells",
//	                      res.getDrawable(R.drawable.ic_tab_spellbook))
//	                  .setContent(intent);
//	    tabHost.addTab(spec);
//
//
//	    tabHost.setCurrentTabByTag("characters");
//
//	    ChangeLog cl = new ChangeLog(this);
//	    if (cl.firstRun()) {
//	    	cl.getLogDialog().show();
//		}
//	}
//
//}
