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
import org.up.fe.jsvgoncalves.ssim.firstapp.utils.Utils;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity implements
	ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener, LocationListener {

	public static final String LOG_TAG = "mylog";

	// The name used for all mock locations
    public static final String LOCATION_PROVIDER = "fused";
    
    // A request to connect to Location Services
    private LocationRequest mLocationRequest;
    	
	LocationClient mLocationClient;
	LocationListener locationListener;
	LocationManager locationManager;
	
	// Intent to send to SendMockLocationService. Contains the type of test to run
    private Intent mRequestIntent;

	private double update = 0;

	private float totalDistance = 0;

	private long lastTime = 0;

	private float speed = 0, avgspeed = 0;

	private float totalTime = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Start the socket communication
        new RetrievePositionTask().execute();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	String mocLocationProvider = LocationManager.GPS_PROVIDER;
    	locationManager.addTestProvider(mocLocationProvider, false, false,
    			false, false, true, false, false, 0, 5);
    	locationManager.setTestProviderEnabled(mocLocationProvider, true);
    	locationManager.requestLocationUpdates(mocLocationProvider, 0, 0, this);
    	
        try {
//	        mLocationRequest = LocationRequest.create();
//	        mLocationRequest.setInterval(1000);
//	        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//	        mLocationRequest.setFastestInterval(1000);
//
//	        mLocationClient = new LocationClient(this, this, this);
//	        mLocationClient.connect();
        } catch (Exception e) {
        	e.printStackTrace();
        }
       
    }
	/**
     * Method called by the network async task when a comm is received.
     * @param pos
     */
    public void positionUpdated(String str) {
    	JSONObject json = JSONHelper.string2JSON(str);
//    	t.setText(str + " Km/h");

    	TextView t = (TextView) findViewById(R.id.currentSpeedTextView);
    	String lat = JSONHelper.getValue(json, "latitude");
    	String lon = JSONHelper.getValue(json, "longitude");
    	t.setText(lat + ", " + lon);
    	//setCustomLocation(lat, lon);
    	setMockLocation(lat,lon);
    	Log.d(LOG_TAG, "setted temp location");
    } 	
    
    
    
    private void setMockLocation(String lat, String lon) {
    	double latitude = Double.parseDouble(lat);
    	double longitude = Double.parseDouble(lon);

    	Location location = new Location(LocationManager.GPS_PROVIDER);
    	long elapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
        long currentTime = System.currentTimeMillis();
        
        location.setElapsedRealtimeNanos(elapsedTimeNanos);
        location.setTime(currentTime);
        location.setAccuracy(16f);
        location.setAltitude(0d);
        location.setBearing(0f);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		
		// provide the new location
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
	}



	class RetrievePositionTask extends AsyncTask<String, String, String> {

    	protected String doInBackground(String... urls) {
    		try {
    			ServerSocket socket = new ServerSocket(5173);
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
    }

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		 try {
			 if(mLocationClient.isConnected()) {
				 mLocationClient.setMockMode(true);
				 mLocationClient.requestLocationUpdates(mLocationRequest, this);
//				 setCustomLocation();
			 }
			Log.d(LOG_TAG, "Mocks connected.");
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setCustomLocation(String latitude, String longitude) {
		
		Location previousLoc = mLocationClient.getLastLocation();
		
		
		Location newLocation = new Location(LOCATION_PROVIDER);

		// Convert the string parameters to double.
		double lat = Double.parseDouble(latitude);
		double lon = Double.parseDouble(longitude);
		// Time values to put into the mock Location
        long elapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
        long currentTime = System.currentTimeMillis();
        
		newLocation.setElapsedRealtimeNanos(elapsedTimeNanos);
		newLocation.setTime(currentTime);
		newLocation.setLatitude(lat);
		newLocation.setLongitude(lon);
		newLocation.setAccuracy(16f);
		newLocation.setAltitude(0d);
		newLocation.setBearing(0f);
		mLocationClient.setMockLocation(newLocation);
		Log.d(LOG_TAG, "Setting mock location " + newLocation.getLatitude() + " , " + newLocation.getLongitude());
		
		/** 
		 * Calculate distances and speeds
		 */
		if(previousLoc != null) {
			float newDistance = previousLoc.distanceTo(newLocation)/1000;
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
		updateGUI();
	}

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
	public void onDestroy() {
		// Turn mock mode off
        mLocationClient.setMockMode(false);

        // Disconnect from Location Services
        mLocationClient.disconnect();
        
        super.onDestroy();

        // remove it from the location manager
    	try {
    		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//    		locationManager.removeTestProvider(MockGpsProvider.GPS_MOCK_PROVIDER);
    	}
    	catch (Exception e) {}
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
	   
}

