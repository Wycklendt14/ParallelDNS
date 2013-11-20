package com.example.paralleldns;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import android.widget.SeekBar;

public class MainActivity extends Activity {
	
	private Button mSendButton_1;
	private TextView seek1_TextView;
	private int seekInt1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Initialize the send button with a listener that for click events
        mSendButton_1 = (Button) findViewById(R.id.button_sendbn1);
        mSendButton_1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                
            	for(int i=0; i<seekInt1; i++) {
            		DNSQuery tmp = new DNSQuery();
            		tmp.start();
            		//thread.run();
            	}
            }
        });
        
        SeekBar bar = (SeekBar) findViewById( R.id.seekBar1 );
        bar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
        	@Override
        	public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser ) {
        		//Here is where you handle the change in the seekbar e.g.
        		seek1_TextView.setText( Integer.toString(progress) );    
        		seekInt1 = progress;
  	    	}
        	@Override
        	public void onStartTrackingTouch( SeekBar seekBar ) {
        		//No need to do stuff here
  	      	}
        	@Override
  	      	public void onStopTrackingTouch( SeekBar seekBar ) {
        		//No need to do stuff here
        	}
        } );
        
        // make text label for progress value
        seek1_TextView = (TextView)findViewById(R.id.seek1_TextView);
      
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
		 
		Log.e("test", "++ ON START ++");
		
		
	}

}


