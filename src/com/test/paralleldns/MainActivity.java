package com.test.paralleldns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	Intent i;
	public static int running; //1=WiFi, 2=LTE, 4=3G
	public static String strength;
	public static String[] running_state = {"Not Running","WiFi","LTE","3G"};
	
	// Store what tests are complete
	public static boolean complete3G;
	public static boolean completeLTE;
	public static boolean completeWiFi;
	
	// Textviews for statuses of tests
	private static TextView text_Wifi;
	private static TextView text_LTE;
	private static TextView text_3G;
	private static TextView current_status;
	
	// Location of stored statuses
	private static SharedPreferences prefs;
	
	// Receiver for updates form background task 
	private statusReceiver receiver;
	
	// Get status of radio
	TelephonyManager        Tel;
	MyPhoneStateListener    MyListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.e("test","Start!" );
		
		// Not running on create
        running = 0;

        // get listener for phone state
        MyListener = new MyPhoneStateListener();
        Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        
        strength = "null";

        // Set status of testing
        current_status = (TextView)findViewById(R.id.current_status);
        if(completeLTE & complete3G & completeWiFi)
        	current_status.setText( "All done Testing. Thanks!" );
        else
        	current_status.setText( "Please Select Test." );
        
        // initialize variables from stored preferences
        getStatus();
        
        // Set up the reciever for updates from background task
        IntentFilter filter = new IntentFilter(statusReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new statusReceiver();
        registerReceiver(receiver, filter);
        
        
        
        
	}

	// Save state back to not running and unregister receiver
	public void onDestroy() {
        this.unregisterReceiver(receiver);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("running", 0);
		editor.apply();
        
        super.onDestroy();
    }
	
	// Start the WiFi test
	public void onClick_WiFi(View v) {
        
    	Log.v("test","Click!" );
    	if(completeWiFi) {
    		Toast.makeText(getApplicationContext(), "Already Ran Test! Thanks!", Toast.LENGTH_SHORT).show();

    		return;

    	}
    	
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
    	
    	// If the wifi is connected and there is no test running start wifi test
    	if (mWifi.isConnected()  && (networkInfo != null) ) {
    		if(running == 0) {
    			running = 1;
    			
        		// Start test service
        		i = new Intent(this, DNSTest.class);
        		startService(i); 
        		
        		MainActivity.text_Wifi.setText( "Running..." );  
    			MainActivity.text_Wifi.setTextColor(Color.YELLOW);
    			
    			SharedPreferences.Editor editor = prefs.edit();
    			editor.putInt("running", 1);
    			editor.apply();
        	}
        	else {
        		Toast.makeText(getApplicationContext(), "Currently running " + running_state[running] + " test.", Toast.LENGTH_SHORT).show();
        	}
    	}
    	else
    		Toast.makeText(getApplicationContext(), "Connect to a WiFi Network please.", Toast.LENGTH_SHORT).show();
    	
    	
    }
	
	public void onClick_LTE(View v) {
        
    	Log.v("test","Click!" );
    	if(completeLTE) {
    		Toast.makeText(getApplicationContext(), "Already Ran Test! Thanks!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
    	
    	// If the wifi is not connected and there is no test running and LTE is the network type start wifi test
    	if (!mWifi.isConnected() && (networkInfo != null) && !completeLTE) {
    		if(running == 0) {
        		
    			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	        if(tm.getNetworkType()==TelephonyManager.NETWORK_TYPE_LTE) {
    	        	running = 2;
    	        	
    	        	// Start test service
    	        	i = new Intent(this, DNSTest.class);
    	        	startService(i); 
    	        	
    	        	MainActivity.text_LTE.setText( "Running..." );  
        			MainActivity.text_LTE.setTextColor(Color.YELLOW);
        			
        			SharedPreferences.Editor editor = prefs.edit();
        			editor.putInt("running", 2);
        			editor.apply();
    	        }
    	        else
    	        	Toast.makeText(getApplicationContext(), "Not in 4G LTE mode.  Please turn on 4G.", Toast.LENGTH_SHORT).show();
        	}
    		else {
        		Toast.makeText(getApplicationContext(), "Currently running " + running_state[running] + " test.", Toast.LENGTH_SHORT).show();
        	}
    	}
    	else
    		Toast.makeText(getApplicationContext(), "Turn off WiFi please.", Toast.LENGTH_SHORT).show();
    	
    	
    }

	public void onClick_3G(View v) {
    
		Log.v("test","Click!" );
		if(complete3G) {
    		Toast.makeText(getApplicationContext(), "Already Ran Test! Thanks!", Toast.LENGTH_SHORT).show();
    		return;
    	}
	
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		if (!mWifi.isConnected() && (networkInfo != null) && !complete3G) {
			if(running == 0) {	        
    	        if((tm.getNetworkType()!=TelephonyManager.NETWORK_TYPE_LTE)&&networkInfo.isConnected()) {
    	        	// Start test service
    	        	running = 3;
    	        	
    	        	i = new Intent(this, DNSTest.class);
    	        	startService(i); 
    	        	
    	        	MainActivity.text_3G.setText( "Running..." );  
        			MainActivity.text_3G.setTextColor(Color.YELLOW);
        			
        			SharedPreferences.Editor editor = prefs.edit();
        			editor.putInt("running", 3);
        			editor.apply();
    	        }
    	        else
    	        	Toast.makeText(getApplicationContext(), "Not in 3G mode.  Please turn off 4G.", Toast.LENGTH_SHORT).show();
			}
			else {
        		Toast.makeText(getApplicationContext(), "Currently running " + running_state[running] + " test.", Toast.LENGTH_SHORT).show();
        	}
		}
		else
    		Toast.makeText(getApplicationContext(), "Turn off WiFi please.", Toast.LENGTH_SHORT).show();
		
}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
	public void onStart() {
		super.onStart();

		getStatus();
	}

	
	@Override
	public void onResume() {
		super.onStart();
		
		getStatus();
	}
	
	private class MyPhoneStateListener extends PhoneStateListener
    {
      /* Get the Signal strength from the provider, each tiome there is an update */
      @Override
      public void onSignalStrengthsChanged(SignalStrength signalStrength)
      {
         super.onSignalStrengthsChanged(signalStrength);
         
         String ssignal = signalStrength.toString();
         //String[] parts = ssignal.split(" ");
         //case TelephonyManager.NETWORK_TYPE_LTE:
         //strength = "LTE " + parts[8] +" "+parts[9]+" "+ parts[10]+" "+ parts[11];
         strength = running_state[running]+ " " + ssignal;

      }

    };
    
    public static void set_current_status(String input){
    	current_status.setText(input);
    }
    
    public static void set_destroy(){
    	
		if(running == 1) {
			completeWiFi = true;
			text_Wifi.setText( "Done!" );  
			text_Wifi.setTextColor(Color.GREEN);
		}
		else if(running == 2) {
			completeLTE = true;
			text_LTE.setText( "Done!" );  
			text_LTE.setTextColor(Color.GREEN);
		}
		else if(running == 3) {
			complete3G = true;
			text_3G.setText( "Done!" );  
			text_3G.setTextColor(Color.GREEN);
		}
		
        if(completeLTE & complete3G & completeWiFi)
        	current_status.setText( "All done Testing. Thanks!" );
        else
        	current_status.setText( "Please Select Test." );
		
		running = 0;
    }
    
    public class statusReceiver extends BroadcastReceiver{
    	 
        public static final String PROCESS_RESPONSE = "test";
 
        @Override
        public void onReceive(Context context, Intent intent) {   
        	String message = intent.getStringExtra(DNSTest.MESSAGE);

            if(message.equals("Destroy"))
            	set_destroy();
            else
            	current_status.setText(message);

        }
 
    }

    private void getStatus() {
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
        complete3G = prefs.getBoolean("complete3G", false);
    	completeLTE = prefs.getBoolean("completeLTE", false);
    	completeWiFi = prefs.getBoolean("completeWiFi", false);
    	running = prefs.getInt("running",0);
    	
        text_Wifi = (TextView)findViewById(R.id.text_WiFi);
        if(completeWiFi) {
        	text_Wifi.setText( "Done!" );  
        	text_Wifi.setTextColor(Color.GREEN);
        }
        else if (running==1) {
        	MainActivity.text_3G.setText( "Running..." );  
			MainActivity.text_3G.setTextColor(Color.YELLOW);
        }
        else {
        	text_Wifi.setText( "Incomplete." );
        	text_Wifi.setTextColor(Color.RED);
        }
        
        text_LTE = (TextView)findViewById(R.id.text_LTE);
        if(completeLTE) {
        	text_LTE.setText( "Done!" );  
        	text_LTE.setTextColor(Color.GREEN);
        }
        else if (running==2) {
        	MainActivity.text_LTE.setText( "Running..." );  
			MainActivity.text_LTE.setTextColor(Color.YELLOW);
        }
        else {
        	text_LTE.setText( "Incomplete." );
        	text_LTE.setTextColor(Color.RED);
        }
        
        text_3G = (TextView)findViewById(R.id.text_3G);
        if(complete3G) {
        	text_3G.setText( "Done!" );  
        	text_3G.setTextColor(Color.GREEN);
        }
        else if (running==3) {
        	MainActivity.text_3G.setText( "Running..." );  
			MainActivity.text_3G.setTextColor(Color.YELLOW);
        }
        else {
        	text_3G.setText( "Incomplete." );
        	text_3G.setTextColor(Color.RED);
        }
        
        if(completeLTE & complete3G & completeWiFi)
        	current_status.setText( "All done Testing. Thanks!" );
        else
        	current_status.setText( "Please Select Test." );
        
    }
    
    
    
	void emailData() {
	    try {
	        // Make sure the Pictures directory exists.
	    	File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

	    	path.mkdirs();
	    	File file = new File(path, "test.txt");
	    	Log.v("test", file.toString());
	    	Toast.makeText(getApplicationContext(), file.toString(), Toast.LENGTH_SHORT).show();
	    	
	        // Very simple code to copy a picture from the application's
	        // resource into the external file.  Note that this code does
	        // no error checking, and assumes the picture is small (does not
	        // try to copy it in chunks).  Note that if external storage is
	        // not currently mounted this will silently fail.
	        FileInputStream is = new FileInputStream(getFileStreamPath("Output_WiFi"));
	        OutputStream os = new FileOutputStream(file);
	        byte[] data = new byte[is.available()];
	        is.read(data);
	        os.write(data);
	        is.close();
	        os.close();
	        
	        //File file = getFileStreamPath("test.txt");
	        // Tell the media scanner about the new file so that it is
	        // immediately available to the user.
	        MediaScannerConnection.scanFile(this,
	                new String[] { file.toString() }, null,
	                new MediaScannerConnection.OnScanCompletedListener() {
	            public void onScanCompleted(String path, Uri uri) {
	                Log.i("ExternalStorage", "Scanned " + path + ":");
	                Log.i("ExternalStorage", "-> uri=" + uri);
	            }
	        });
	        
	        //Email file
	        Intent intent = new Intent(Intent.ACTION_SEND);
	        intent.setType("text/plain");
	        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"matt.wycklendt@gmail.com"});
	        intent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
	        intent.putExtra(Intent.EXTRA_TEXT, "body text");
	        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
	        startActivity(Intent.createChooser(intent, "Send email..."));
	        
	    } catch (IOException e) {
	        // Unable to create file, likely because external storage is
	        // not currently mounted.
	        Log.w("ExternalStorage", "Error writing ",e);//+file, e);
	    }
	}
}


