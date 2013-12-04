package com.test.paralleldns;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.xbill.DNS.ResolverConfig;

import com.test.paralleldns.MainActivity.statusReceiver;

import android.content.res.AssetManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;


public class DNSTest extends IntentService {
	private String[] servers = {"198.153.192.1",
	                            "8.26.56.26",
	                            "209.244.0.3",
	                            "4.2.2.2",
	                            "208.76.50.50",
	                            "208.67.222.222",
	                            "156.154.70.1",
	                            "8.8.8.8"};
	String[] local_servers;
	
	public static final String MESSAGE = "MESSAGE";
	
	public DNSTest() {
		super("DNSTest");
	}

	InputStream input;
	AssetManager manager;
	PowerManager pm;
	WakeLock wakeLock;
	int running;
	String running_string;
	FileOutputStream outputStream;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// prevent the phone from sleeping while background task is running
		pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
		wakeLock.acquire();
		
		running = MainActivity.running;
		running_string = MainActivity.running_state[MainActivity.running];
		
        ResolverConfig con = new ResolverConfig();
		local_servers = con.servers();
        
		String all_servers = "Starting " + running_string + " test with servers: ";
		for(int i=0; i<local_servers.length; i++) {
			all_servers = all_servers + local_servers[i] + " ";
		}
		for(int j=0; j<servers.length; j++) {
			all_servers = all_servers + servers[j] + " ";
		}
		
		try {
			outputStream = openFileOutput("Output_"+running_string, Context.MODE_PRIVATE);
			outputStream.write(all_servers.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Log.v("test"," " + doc);
		Log.v("ParallelDNS",all_servers);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {

		for(int i=0; i<10000; i++) {
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	        int network_type = tm.getNetworkType();
			
			List<DNSQuery> threads = new ArrayList<DNSQuery>();
			String line = getTarget();
			
			for(int j=0; j<local_servers.length; j++) {
				if(line!=null) {
					DNSQuery tmp = new DNSQuery(line, local_servers[j]);
					tmp.start();
					threads.add(tmp);
				}
			}
			for(int j=0; j<servers.length; j++) {
				if(line!=null) {
					DNSQuery tmp = new DNSQuery(line, servers[j]);
					tmp.start();
					threads.add(tmp);
				}
			}
			
			
			String times = " ";
			for (int n=0; n<threads.size(); n++)
			{
				try {
					DNSQuery tmp = (DNSQuery)threads.get(n);
					tmp.join(1000000);
					times = times + tmp.delta + " ";
					//Log.v("time",tmp.server + " " + tmp.target + " " + tmp.delta);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			String out = "run " + i + " " + running_string + " network_type: " + network_type + " " + line + " " + getStrength() + times;
			try {
				outputStream.write((out+"\n").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.v("ParallelDNS", out);
			
			
			DecimalFormat df = new DecimalFormat("0.00");
			df.setMaximumFractionDigits(2);
			String percent_complete = df.format((((float)i/10000.0)*100.0));
			String message = running_string+" test: "+percent_complete+"% complete.";

			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(statusReceiver.PROCESS_RESPONSE);
	        broadcastIntent.putExtra(MESSAGE, message);
	        sendBroadcast(broadcastIntent);
	    }
	}
	
	@Override
	public void onDestroy () {
		// Save completion settings
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		if(running == 1) {
			editor.putBoolean("completeWiFi", true);
		}
		else if(running == 2) {
			editor.putBoolean("completeLTE", true);
		}
		else if(running == 3) {
			editor.putBoolean("complete3G", true);
		}
		editor.putInt("running", 0);
		editor.apply();
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(statusReceiver.PROCESS_RESPONSE);
        broadcastIntent.putExtra(MESSAGE, "Destroy");
        sendBroadcast(broadcastIntent);
        
        
        
        
        // Let the tool go back to sleep
        wakeLock.release();
        
        try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Choose a random website to send to DNS servers
	public String getTarget(){
		AssetManager manager = getAssets();
		try {
			input = manager.open("top-1m.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	Random r = new Random();
    	String line = "null";
    	
    	int randomInt;
    	try {
    		randomInt = r.nextInt((input.available()-100));
    		input.skip(randomInt);
    		byte[] buffer = new byte[100];
    		input.read(buffer);
    		String tmp = new String(buffer, "UTF-8");
    		BufferedReader bufReader = new BufferedReader(new StringReader(tmp));
    		bufReader.readLine();
    		line = bufReader.readLine();
    		input.reset();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	String delims = "[,]";
		String[] tokens = line.split(delims);
		
		
    	return tokens[1];
	}   
	
	// Get the strength of the wifi network
	private String getStrength() {
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);    

    	if (mWifi.isConnected()) {
    		return "WiFi rssi " + wifi.getConnectionInfo().getRssi();
    	}
    	else
    		return MainActivity.strength;
    }
	
	
	    
}