package com.zzoranor.spelldirectory.activity;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.zzoranor.spelldirectory.ClassLabel;
import com.zzoranor.spelldirectory.CustomClassAdapter;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.TabMain;
import com.zzoranor.spelldirectory.data.Classes;
import com.zzoranor.spelldirectory.database.DbAdapter;
import com.zzoranor.spelldirectory.database.DbAdapterFactory;

public class ClassList extends ListActivity {
	DbAdapter sql;
	//HashMap<String, Integer> classes;
	//Classes classes;
	//private final int DATABASE_DIALOG = 1;
	//private static final int PROGRESS_INCREMENT = 10;
	//private static final int PROGRESS_MAX = 1250;
	//public static Character currCharacter;
	
	private ArrayList<ClassLabel> class_names;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	
	private int swipe = 0;
	CustomClassAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("START", "Starting Spell list.");
		
		//classes = Classes.getInstance();
				
			
			//new Character("Zach");
		
		class_names = new ArrayList<ClassLabel>();
		
		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
		sql = DbAdapterFactory.getStaticInstance(this);
		sql.open();

		initList();
		//currCharacter = sql.getCharacterData(1);
	}

	private void initList() {
		//classes = sql.populateClassMap();
		
		//sql.setClassesFromDB(classes);
		//classes.getClassList();
		ClassLabel labels[] = sql.getClasses();
		for(int i = 0; i < labels.length;i++)
		{
			class_names.add(labels[i]);
		}
		
		
		adapter = new CustomClassAdapter(this, 
				R.layout.class_list_item, R.id.class_view_id, CharacterList.chosenCharacter, class_names);
		/*
				
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
				R.layout.class_list_item, R.id.class_view_id, class_names);
		*/
		// new CustomAdapter(this, R.layout.class_list_item,
		// R.id.list_view_name,class_names);
		setListAdapter(adapter);
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ClassLabel label = (ClassLabel) parent.getItemAtPosition(position);
				sql.changeChosenClass(CharacterList.getChosenCharacterId(), label.getName(), label.getId());
				CharacterList.chosenCharacter.setCurrentClass(label.getId(), label.getName());
				
				if (swipe == 0) {
					//Intent spells_intent = new Intent().setClass(context,
					//		SpellList.class);
					//CharacterList.chosenCharacter.curr_class_name = s;
					//CharacterList.chosenCharacter.curr_class_id = classes.get(s);
					
					TabMain.tabHost.setCurrentTabByTag("class_spells");
					
					//spells_intent.putExtra("spellList.class_name", s);
					//spells_intent.putExtra("spellList.class_id", classes.get(s));
					//context.startActivity(spells_intent);
				} else if (swipe == 1) {
					swipe = 0;
					//Intent spells_intent = new Intent().setClass(context,
					//		PreparedList.class);
					//context.startActivity(spells_intent);
					TabMain.tabHost.setCurrentTabByTag("prepared_spells");
				}
			}
		});
		lv.setOnTouchListener(gestureListener);

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.classmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.toPreparedList:
			/*
			Intent prepared_intent = new Intent().setClass(this,
					PreparedList.class);
			startActivity(prepared_intent);
			*/
			TabMain.tabHost.setCurrentTabByTag("prepared_spells");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	

	@Override
	protected void onResume() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		sql.open();
		if(adapter != null){
			adapter.setCharacter(CharacterList.chosenCharacter);
			adapter.notifyDataSetChanged();
		}
		//getListView().setSelection(currentPosition);
		super.onResume();
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}
	
	@Override
	public void onBackPressed() {
		TabMain.tabHost.setCurrentTabByTag("characters");
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
					//Toast.makeText(SpellDir_Test.this, "Left Swipe",
						//	Toast.LENGTH_SHORT).show();
					//Intent intent = new Intent().setClass(
					//		SpellDir_Test.this, PreparedList.class);
					//SpellDir_Test.this.startActivity(intent);
					TabMain.tabHost.setCurrentTabByTag("class_spells");
					return true;
					// swipe = 1;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(vy) > SWIPE_THRESHOLD_VELOCITY) {
					Log.d("EVENT", "OnFling ------ Right Fling");
					//Toast.makeText(SpellDir_Test.this, "Right Swipe",
						//	Toast.LENGTH_SHORT).show();
					// Intent intent = new
					// Intent().setClass(getApplicationContext(),SpellDir_Test.class);
					// getApplicationContext().startActivity(intent);
					return true;
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}
	}
}