package org.crashlog.sample;

import org.crashlog.crashreport.CrashReport;

import android.app.Application;


/**
 * 
 * MyApplication
 * 
 * @author Liu,Yanjun
 * @see http://www.crashlog.org
 * 
 */
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		CrashReport.init(this);
	}

}
