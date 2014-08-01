/* The MIT License (MIT)

Copyright (c) 2014 Liu,Yanjun

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. */

package org.crashlog.crashreport;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.util.Log;

/**
 * 
 * Create a CrashReport instance, for listens uncaught exception and report to Host.
 * When APP is running will find crash log files, if there is then reported.
 * 
 * @author Liu,Yanjun
 * @see http://www.crashlog.org
 *
 */
public class CrashReport implements UncaughtExceptionHandler {	
	private static CrashReport mInstance;
	
	private UncaughtExceptionHandler mUncaughtExHandler;
	private Application mApp;
	
	private CrashReport(Application app) {
		this.mApp = app;
		mUncaughtExHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);

		//Report crash logs when APP is running.
		if(CrashConfig.IS_ALLOW_REPORT_TO_HOST) {
			CrashLogSender cls = new CrashLogSender(app);
			cls.start();
		}
	} 
	
	/**
	 * Initializes an instance of CrashReport and listens uncaught exception.
	 * 
	 * @param app
	 * @return
	 */
	public static final CrashReport init(Application app) {
		if (mInstance == null) {
			mInstance = new CrashReport(app);
		}
		return mInstance;
	}
	

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.d(CrashConfig.TAG, ex.getLocalizedMessage() + "\n" + ex.getMessage());
		
		try {
			CrashLogStore.saveLogToFile(mApp, ex, thread);
		} catch (IOException e) {
			Log.d(CrashConfig.TAG, "Save crash log failed. ", e);
		}
//		Log.d(TAG, this.getProcessName(android.os.Process.myPid()) + "/" + thread.getName() + "-" + thread.getId());
		
		if (mUncaughtExHandler != null) {
			mUncaughtExHandler.uncaughtException(thread, ex);
		}
	}
}
