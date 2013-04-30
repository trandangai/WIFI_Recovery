package com.vorsk.wifirecovery;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vorsk.wifirecovery.network.Network;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

//extend ListActivity in place of activity
public class HomeActivity extends SherlockListActivity {
	private static final String TAG = "WIFI_Recovery Activity";
	private static final boolean DEBUG = false;

	private static final int REFRESH_RESULT = 5;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "onCreate");
		
		// use my custom list with buttons and empty list support
		setContentView(R.layout.home_network_list);
		
		setTitle(R.string.home_title);
		overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

		// start up the parser
		ParserTask.loadNetworks(this);
	}

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (DEBUG)
			Log.d(TAG, "clicked!");
		Network network = (Network) getListAdapter().getItem(position);
		if (DEBUG)
			Log.d(TAG, "got network");

		infoPopUp(network).show();
	}

	public AlertDialog infoPopUp(final Network network) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(network.getDetails())
				.setCancelable(true)
				.setNegativeButton(R.string.button_dismiss,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss(); // or cancel?, who knows, it
													// works.
							}
						})
				.setPositiveButton(R.string.button_share,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// does this share the data?
								Intent share = new Intent(
										android.content.Intent.ACTION_SEND);
								share.setType("text/plain");
								share.putExtra(
										android.content.Intent.EXTRA_SUBJECT,
										"Wireless Settings");
								share.putExtra(
										android.content.Intent.EXTRA_TEXT,
										network.getDetails());
								startActivity(share);

							}
						});

		AlertDialog alert = builder.create();

		return alert;
	}

	private void refresh() {
		ParserTask.refresh(this);
	}

	// make the menu work
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//actionbar menu
		getSupportMenuInflater().inflate(R.menu.home_action, menu);
		return true;
	}

	// when a user selects a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_backup:
			startActivityForResult(new Intent(this, BackupActivity.class), HomeActivity.REFRESH_RESULT);
			return true;
		case R.id.menu_refresh:
			this.refresh();
			return true;
		case R.id.menu_about:
			showAboutView(this);
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			return true;
			// possibly add more menu items here
		default:
			return false;
		}
	}

	/**
	 * This is called when the backup task finishes and returns
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// networks may be changed, refresh
		if (requestCode == HomeActivity.REFRESH_RESULT
				&& resultCode == SherlockListActivity.RESULT_OK) {
			if (DEBUG)
				Log.d(TAG, "got back the activity");
			if (data.getBooleanExtra("refresh", false)) {
				if (DEBUG)
					Log.d(TAG, "updating networks");
				this.refresh();
			}
		}
	}
	
	
	public static void showAboutView(Activity act)
	{
		LayoutInflater inflater = (LayoutInflater) act.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.about, null);
		
		final PopupWindow pw = new PopupWindow(layout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		pw.setTouchable(true);
		pw.setFocusable(true);
		
		Button ok = (Button) pw.getContentView().findViewById(R.id.about_close);
	    ok.setOnClickListener(new View.OnClickListener()
	    {
	        @Override
	        public void onClick(View v)
	        {   
	            pw.dismiss(); 
	        }

	    });
		
		pw.showAtLocation(act.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
		
		TextView version = (TextView) pw.getContentView().findViewById(R.id.about_version); 
		try {
			version.append(act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e1) {

			version.append("Unknown");
		}
	}


}