package com.example.paralleldns;

import java.net.UnknownHostException;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import android.os.NetworkOnMainThreadException;
import android.util.Log;

public class DNSQuery extends Thread {

    public void run() {
    	try {
    		SimpleResolver res = new SimpleResolver("8.8.8.8");
			res.setTimeout(2, 0);
				
			Lookup loo = new Lookup("milwaukeetool.com", Type.A);
			loo.setResolver(res);
				
			loo.run();
			Record [] rec = loo.getAnswers();
				
			for (int i = 0; i < rec.length; i++) {
				Record tmp = rec[i];
				Log.v("test",tmp.rdataToString() );
			}
				
		} catch (UnknownHostException e) {
			Log.e("test","host");
		} catch (TextParseException e) {
			Log.e("test","parse");
	 	} catch (IllegalArgumentException e) {
	 		Log.e("test","arg");
	 	} catch (IllegalStateException e) {
	 		Log.e("test",e.toString());
	 	} catch (IllegalMonitorStateException e) {
	 		Log.e("test",e.toString());
		}  catch (NetworkOnMainThreadException e) {
			Log.e("test", "netowrk");
		}
    }
}