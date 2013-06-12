package com.zzoranor.spelldirectory;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomListView extends TextView{
	SpellLabel s;
	
	Context context;
	public CustomListView(Context context, int height,int width){
		super(context);
		initCustomListView(context);
	}
	
	public CustomListView(Context context) {
		super(context);
		initCustomListView(context);
	}

	public CustomListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCustomListView(context);
	}
	
	private void initCustomListView(Context context){
		this.context = context;
	}
	
	public void setSpellLabel(SpellLabel sp){
		s = sp;
	}
	public SpellLabel getSpellLabel(){
		return s;
	}
	
}
