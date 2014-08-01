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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 
 * Start a thread for report crash logs.
 * 
 * @author Liu,Yanjun
 * 
 * @see http://www.crashlog.org
 *
 */
class CrashLogSender {
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int NETWORK_CLASS_3_G = 2;
    public static final int NETWORK_CLASS_4_G = 3;
    
    
	private Application mApp;
	private Thread mReportThread;
	
	public CrashLogSender (Application app) {
		this.mApp = app;
		
		mReportThread = new Thread(mReportRunnable);
		mReportThread.setName("CrashLogReport");
		mReportThread.setPriority(Thread.MIN_PRIORITY);
	}
	
	
	private Runnable mReportRunnable =  new Runnable() {

		@Override
		public void run() {
			try {
				Thread.sleep(CrashConfig.REPORT_LOG_DELAY);
				
				reportCrashLogs();
			} catch (Exception ex) {
				Log.d(CrashConfig.TAG, "Report crash logs failed.", ex);
			}
		}
		
	};
	
	private void sendHttpRequest(byte[] data) throws IOException {
		HttpURLConnection conn = null;
		OutputStream out = null;
		try{
			conn = (HttpURLConnection)new URL(CrashConfig.REPORT_URL).openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.connect();
			
			out = conn.getOutputStream();
			out.write(data);
			out.flush();
			
			if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("HTTP Response code not is 200 OK.");
			}
		} finally {
			if(out != null) {
				out.close();
			}
			if(conn != null) {
				conn.disconnect();
			}
		}
	}


    /**
     * Return general class of network type, such as "3G" or "4G". 
     *
     */
    private static int getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }
	
	private boolean isAllowConnectNetwork() {
		ConnectivityManager connectMgr = (ConnectivityManager) mApp.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
		if(netInfo == null) {
			return false;
		}
		
		if(netInfo.getType() == ConnectivityManager.TYPE_WIFI
				&& netInfo.isConnected()) {
			return true;
		}
		
		TelephonyManager telMgr = (TelephonyManager) mApp.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		int netClass = getNetworkClass(telMgr.getNetworkType());
		if(netInfo.getType() == ConnectivityManager.TYPE_MOBILE
				&& netInfo.isConnected()
				&&  (netClass == NETWORK_CLASS_3_G || netClass == NETWORK_CLASS_4_G)) {
			return true;
		}
	
		return false;
	}
	
	private void reportCrashLogs() throws IOException {
		String dirPath = mApp.getExternalCacheDir().getPath() + File.separator + CrashConfig.LOG_DIR;
		
		File dir = new File(dirPath);
		File[] logs = dir.listFiles();
		if(logs == null || logs.length == 0) {
			return;
		}
		
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream(102400);
			
			//Write header
			CrashLogStore.writeDataHeader(mApp, baos);
	
			//Write number of logs , Allowed to report maximum of 128 logs.
			int len = Math.min(logs.length, 128);
			baos.write(len);
			
			//Write logs data
			for(int i = 0; i < logs.length; i++) {
				File logFile = logs[i];
				CrashLogStore.writeLogData(baos, logFile);
			}
		} finally {
			if(baos != null) {
				baos.close();
			}
		}
		this.sendHttpRequest(baos.toByteArray());

		//Delete log files after successful reported. 
		CrashLogStore.deleteLogFiles(logs);
	}
	
	public void start() {
		//Check network type, only WIFI,4G or 3G  to HTTP request.
		if(isAllowConnectNetwork() 
				&& mReportThread.getState() == Thread.State.NEW) {
			mReportThread.start();
		}
	}
	
}
