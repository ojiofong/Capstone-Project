package com.ojiofong.arounda.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ojiofong.arounda.R;

public class AppManager {

	private Context context;

	public AppManager(Context activityContext) {
		this.context = activityContext;

	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void setStatusBarColorForKitKat(int color) {
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			View statusBar = (View) ((Activity) context).findViewById(R.id.statusBarBackground);
			statusBar.setVisibility(View.VISIBLE);
			Window w = ((Activity) context).getWindow();
			w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			//status bar height
			//int actionBarHeight = getActionBarHeight();
			int statusBarHeight = getStatusBarHeight();
			//action bar height
			statusBar.getLayoutParams().height = statusBarHeight;
			statusBar.setBackgroundColor(color);
		}
	}

	private int getActionBarHeight() {
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
		}
		return actionBarHeight;
	}

	private int getStatusBarHeight() {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

}
