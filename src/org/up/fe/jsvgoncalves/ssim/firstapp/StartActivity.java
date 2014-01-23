package org.up.fe.jsvgoncalves.ssim.firstapp;

import org.up.fe.jsvgoncalves.ssim.firstapp.utils.Utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class StartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		TextView t = (TextView) findViewById(R.id.ipTextView);
		t.setText(Utils.getIPAddress(true));
	}
	
	/**
	 *  Called when the user touches the button
	 * @param view
	 */
	public void startClicked(View view) {
	    // Check if the user has net
		if(checkNetwork()) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			finish();
		}
	}

	private boolean checkNetwork() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
		    // fetch data
			return true;
		} else {
		    // display error
			Toast.makeText(this, getString(R.string.nonetwork), Toast.LENGTH_LONG).show();
			return false;
		}
	}

}
