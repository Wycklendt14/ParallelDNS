package com.test.paralleldns;

import java.net.UnknownHostException;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import android.os.NetworkOnMainThreadException;
import android.util.Log;

public class DNSQuery extends Thread {
	public String target;
	public String server;
	public long delta;
	
	DNSQuery(String target, String server) {
	    this.target = target;
	    this.server = server;
	 }
	
    public void run() {
    	try {
    		
    		//Log.v("test","Send!" );
    		
    		SimpleResolver res = new SimpleResolver(server);
			res.setTimeout(2, 0);
				
			Lookup loo = new Lookup(target, Type.A);
			loo.setResolver(res);
				
			long start = System.nanoTime();
			loo.run();
			//Record [] rec = loo.getAnswers();
			loo.getAnswers();
			delta = System.nanoTime() - start;
			
			//Log.v("test",target);
			//if(rec!=null) {
				//for (int i = 0; i < rec.length; i++) {
				//	Record tmp = rec[i];
					//Log.v("test",tmp.rdataToString() );
				//}
			//	Log.v("test",rec[0].rdataToString() );
			//}
				
		} catch (UnknownHostException e) {
			Log.e("ParallelDNS","host");
		} catch (TextParseException e) {
			Log.e("ParallelDNS","parse");
	 	} catch (IllegalArgumentException e) {
	 		Log.e("ParallelDNS","arg");
	 	} catch (IllegalStateException e) {
	 		Log.e("ParallelDNS",e.toString());
	 	} catch (IllegalMonitorStateException e) {
	 		Log.e("ParallelDNS",e.toString());
		}  catch (NetworkOnMainThreadException e) {
			Log.e("ParallelDNS", "netowrk");
		}
    }
}