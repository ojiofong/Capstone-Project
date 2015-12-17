package com.ojiofong.arounda.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.ojiofong.arounda.R;


public class AlertDialogManager {
	/**
	 * Function to display simple Alert Dialog
	 * 
	 * @param context
	 *            - application context
	 * @param title
	 *            - alert dialog title
	 * @param message
	 *            - alert message
	 * @param status
	 *            - success/failure (used to set icon) - pass null if you don't
	 *            want icon
	 * */

	public void showAlertDialog(Context context, String title, String message, Boolean status) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		if (status != null) {
			//builder.setIcon((status) ? R.drawable.alert_dark_frame: R.drawable.alert_dark_frame);
		}

		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public void showGPSWarningAlert(final Context mContext){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.enable_gps_optional);
		builder.setMessage(R.string.enable_gps_message);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.okay, null);
		builder.show();
		
		//Ensure this shows only once
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		editor.putString("gpsAlert", "OFF");
		editor.apply();
	}

	public void showGPSWarningAlert(final Context mContext, ViewGroup root) {

		LayoutInflater inflater = LayoutInflater.from(mContext);

		View v = inflater.inflate(R.layout.vertical_dialog, root);

		final Dialog dialog = new Dialog(mContext);
		dialog.setTitle(mContext.getString(R.string.enable_gps_optional));
		dialog.setContentView(v);
		dialog.setCancelable(false);
		dialog.show();

		Button locSetting, remind, doNotRemind;
		locSetting = (Button) v.findViewById(R.id.locSetting_b);
		remind = (Button) v.findViewById(R.id.remind_b);
		doNotRemind = (Button) v.findViewById(R.id.doNotRemind_b);

		locSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});
		remind.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		doNotRemind.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();

				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
				editor.putString("gpsAlert", "OFF");
				editor.commit();
			}
		});

	}

}
