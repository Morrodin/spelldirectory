package com.zzoranor.spelldirectory.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.zzoranor.spelldirectory.R;

public class InfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_layout);
	}
	
	@Override
	protected void onResume() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onResume();
	}
}
