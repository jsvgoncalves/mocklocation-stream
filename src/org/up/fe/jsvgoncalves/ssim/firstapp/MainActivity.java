package org.up.fe.jsvgoncalves.ssim.firstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;

import org.json.JSONObject;
import org.up.fe.jsvgoncalves.ssim.firstapp.utils.JSONHelper;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

public class MainActivity extends Activity implements
	ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	// Logtag used in the debugger.
	public static final String LOG_TAG = "mylog";

	// The name used for all mock locations
    public static final String LOCATION_PROVIDER = "fused";
    
    // The location manager used to update the location.
	LocationManager locationManager;
	
	// Statistics
	private float totalDistance = 0;
	private float speed = 0, avgspeed = 0;

	// Update times.
	private long lastTime = 0;
	private float totalTime = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout.
        setContentView(R.layout.activity_main);
        
        // Start the socket communication
        new RetrievePositionTask().execute();

        // Setup the location manager.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	String mLocationProvider = LocationManager.GPS_PROVIDER;
    	locationManager.addTestProvider(mLocationProvider, false, false,
    			false, false, true, false, false, 0, 5);
    	locationManager.setTestProviderEnabled(mLocationProvider, true);
    	locationManager.requestLocationUpdates(mLocationProvider, 0, 0, this);
    }
    
	@Override
	public void onDestroy() {
        super.onDestroy();
	}
    
	/**
     * Called by the network async task when a comm is received.
     * @param str The JSON string with all the data.
     */
    public void positionUpdated(String str) {
    	// Create the JSON object and parse the string.
    	JSONObject json = JSONHelper.string2JSON(str);
    	
    	// Get latitude and longitude values.
    	String lat = JSONHelper.getValue(json, "latitude");
    	String lon = JSONHelper.getValue(json, "longitude");
    	
    	// Set the new device location.
    	setMockLocation(lat,lon);
    	
    	// TODO: Set the other parameters such as speed limit.
    	// setSpeedLimit(speedLimit);
    } 	
    
    /**
     * Overrides the current position with the given latitude and longitude.
     * TODO: Pass in all the parameters.
     * @param lat The new latitude.
     * @param lon The new longitude.
     */
    private void setMockLocation(String lat, String lon) {
    	// Parse the values.
    	double latitude = Double.parseDouble(lat);
    	double longitude = Double.parseDouble(lon);
    	
    	// Get the previous location.
    	Location previousLocation = locationManager.getLastKnownLocation(LOCATION_PROVIDER);
    	
    	// Create a new location
    	Location newLocation = new Location(LocationManager.GPS_PROVIDER);
    	
    	// Create the times.
    	long elapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
        long currentTime = System.currentTimeMillis();
        
        // Set everything into the new location.
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        newLocation.setElapsedRealtimeNanos(elapsedTimeNanos);
        newLocation.setTime(currentTime);
        newLocation.setAccuracy(16f);
        newLocation.setAltitude(0d);
        
        // Calculate the bearing with the previous location.
        if(previousLocation != null) {
        	float bearing = previousLocation.bearingTo(newLocation);
        	newLocation.setBearing(bearing);
        } else {
        	newLocation.setBearing(0f);
        }
        	
		
		// Provide the new location.
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
		
		// Calculate distances and speeds
		if(previousLocation != null) {
			float newDistance = previousLocation.distanceTo(newLocation)/1000;
			Log.d(LOG_TAG, "This distance: " + newDistance + "m");
			totalDistance  += newDistance;
			Log.d(LOG_TAG, "Speed: " + newLocation.getSpeed());
			
			if(lastTime != 0){
				float timeSpan = currentTime - lastTime;
	        	speed =  newDistance / ((timeSpan)/(1000*3600));
	        	avgspeed = totalDistance / (totalTime/(1000*3600));
	        	totalTime += timeSpan;
	        }
	        lastTime = currentTime;
		}
		
		// Calls the GUI updater.
		updateGUI();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * Updates the GUI with the new values.
	 * Updates average speed, current speed and traveled distance.
	 */
	private void updateGUI() {
		TextView distance = (TextView) findViewById(R.id.distance);
		DecimalFormat df = new DecimalFormat("#.##");
		distance.setText(df.format(totalDistance) + " Km");
		
		TextView avgspeedView = (TextView) findViewById(R.id.avgspeed);
		DecimalFormat df2 = new DecimalFormat("#.#");
		avgspeedView.setText(df2.format(avgspeed) + " Km / h");
		
		TextView instaSpeedView = (TextView) findViewById(R.id.currentSpeedTextView);
		instaSpeedView.setText(df2.format(speed) + " Km / h");
		
	}
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "The location has changed: "  + location.getLatitude() + " , " + location.getLongitude());
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
	
	/**
     * AssyncTask that handles the socket communication.
     * Calls positionUpdated() when a new string of data is received.
     * @author João
     *
     */
	class RetrievePositionTask extends AsyncTask<String, String, String> {
		ServerSocket socket;
		
		/**
		 * Main loop.
		 */
    	protected String doInBackground(String... urls) {
    		try {
    			socket = new ServerSocket(5173);
		    	Socket clientSocket = socket.accept();
		    	PrintWriter out =
		    		new PrintWriter(clientSocket.getOutputStream(), true);                   
		    	BufferedReader in = new BufferedReader(
		    		new InputStreamReader(clientSocket.getInputStream()));
		    	String inputLine;
		    	while ((inputLine = in.readLine()) != null) {
		    		publishProgress(inputLine);
		    		out.println("I got your position  (" + inputLine + ") via my Nexus 7");
		    	}
		    	return "na";
    		} catch (Exception e) {
    			return null;
    		}
    	}
    	
    	/**
    	 * Used to update the position instead of the progress.
    	 */
    	protected void onProgressUpdate(String... msg) {
    		positionUpdated(msg[0]);
		}
    	
    	@Override
    	protected void onPostExecute(String result) {
    		super.onPostExecute(result);
    		try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
}

