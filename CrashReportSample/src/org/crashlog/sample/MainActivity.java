package org.crashlog.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

/**
 * 
 * MainActivity
 * 
 * @author Liu,Yanjun
 * @see http://www.crashlog.org
 */
public class MainActivity extends Activity {

	private Button mThrowExBtn;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mThrowExBtn = (Button)this.findViewById(R.id.btn_throw_ex);
		mThrowExBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new ObfuscatedClass().methodA(MainActivity.this);
			}
		});
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
