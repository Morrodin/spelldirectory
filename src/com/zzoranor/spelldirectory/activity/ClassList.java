//package com.zzoranor.spelldirectory.activity;
//
//import android.content.pm.ActivityInfo;
//import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.widget.DrawerLayout;
//import android.util.Log;
//import android.view.GestureDetector;
//import android.view.GestureDetector.SimpleOnGestureListener;
//import android.view.MotionEvent;
//import android.view.View;
//import com.zzoranor.spelldirectory.R;
//import com.zzoranor.spelldirectory.controllers.MainDrawerController;
//import com.zzoranor.spelldirectory.fragments.ClassListFragment;
//import com.zzoranor.spelldirectory.services.SqlService;
//
//public class ClassList extends FragmentActivity {
//
//    private GestureDetector gestureDetector;
//	private static final int SWIPE_MIN_DISTANCE = 120;
//	private static final int SWIPE_MAX_OFF_PATH = 250;
//	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
//
//    private ClassListFragment classListFragment;
//
//    private SqlService mSqlService;
//
//
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		Log.d("START", "Starting Spell list.");
//        setContentView(R.layout.class_list_activity);
//
//        mSqlService = new SqlService(this);
//        mSqlService.setupSql();
//
//        gestureDetector = new GestureDetector(new MyGestureDetector());
//        View.OnTouchListener gestureListener = new View.OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                return gestureDetector.onTouchEvent(event);
//            }
//        };
//
////        classListFragment = new ClassListFragment();
//
//        getFragmentManager().beginTransaction()
//                .add(R.id.class_list_fragment_container, classListFragment).commit();
//
//        DrawerLayout mDrawer = (DrawerLayout) findViewById(R.id.tab_management_drawer_layout);
//
//        MainDrawerController mDrawerController = new MainDrawerController(this, mDrawer);
//        mDrawerController.initDrawer();
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//	}
//
//
//	@Override
//	protected void onResume() {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		mSqlService.openDB();
//		if(classListFragment.getAdapter() != null){
//			classListFragment.getAdapter().setCharacter(CharacterList.chosenCharacter);
//			classListFragment.getAdapter().notifyDataSetChanged();
//		}
//		//getListView().setSelection(currentPosition);
//		super.onResume();
//	}
//
//	public boolean onTouchEvent(MotionEvent event) {
//        return gestureDetector.onTouchEvent(event);
//	}
//
//	@Override
//	public void onBackPressed() {
//        //TODO: Switch tabs once we have those implemented....
////		TabMain.tabHost.setCurrentTabByTag("characters");
//	}
//
//	private class MyGestureDetector extends SimpleOnGestureListener {
//		public boolean onFling(MotionEvent e1, MotionEvent e2, float vx,
//				float vy) {
//			Log.d("EVENT", "OnFling");
//			try {
//				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
//					return false;
//				// right to left swipe
//				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
//						&& Math.abs(vx) > SWIPE_THRESHOLD_VELOCITY) {
//					Log.d("EVENT", "OnFling ------ Left Fling");
//					//Toast.makeText(SpellDir_Test.this, "Left Swipe",
//						//	Toast.LENGTH_SHORT).show();
//					//Intent intent = new Intent().setClass(
//					//		SpellDir_Test.this, PreparedList.class);
//					//SpellDir_Test.this.startActivity(intent);
////					TabMain.tabHost.setCurrentTabByTag("class_spells");
//					return true;
//					// swipe = 1;
//				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
//						&& Math.abs(vy) > SWIPE_THRESHOLD_VELOCITY) {
//					Log.d("EVENT", "OnFling ------ Right Fling");
//					//Toast.makeText(SpellDir_Test.this, "Right Swipe",
//						//	Toast.LENGTH_SHORT).show();
//					// Intent intent = new
//					// Intent().setClass(getApplicationContext(),SpellDir_Test.class);
//					// getApplicationContext().startActivity(intent);
//					return true;
//				}
//			} catch (Exception e) {
//				// nothing
//			}
//			return false;
//		}
//	}
//
//    public SqlService getSqlService() {
//        return mSqlService;
//    }
//
//    public void setSqlService(SqlService mSqlService) {
//        this.mSqlService = mSqlService;
//    }
//}