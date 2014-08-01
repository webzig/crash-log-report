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


/**
 * 
 * All configurations is defined here.
 * 
 * 
 * @author Liu,Yanjun
 * @see http://www.crashlog.org
 * 
 */
final  class CrashConfig {
	public static final String TAG = "CrashReport"; //For debug log
	
	/**
	 * For report host address.
	 */
	public static final String REPORT_URL = "http://localhost:8080/CrashLog/report";
	
	/**
	 * For report log data.
	 */
	public static final int MAGIC_NUM = 0x0003125B;
	
	/**
	 * SDK version number.
	 */
	public static final int VERSION = 1;
	
	/**
	 * Platform: 1-Android 2-IOS 3-WP 4-Win8
	 */
	public static final byte PLATFORM = 1;
	
	/**
	 * The Log file name prefix. 
	 */
	public static final String LOG_FILE_PREFIX = "crash_";
	
	/**
	 * The log file extension.
	 */
	
	public static final String LOG_FILE_EXT = ".crh";
	
	/**
	 * The log files storage directory.
	 */
	public static final String LOG_DIR = "crash_report";
	
	
	
	/**
	 * When the APP running, how delayed time began to report log files. 
	 */
	public static final int REPORT_LOG_DELAY = 0; //In msec.
	
	/**
	 * Whether to allow report crash log files.
	 * Reported files will be deleted. 
	 */
	public static final boolean IS_ALLOW_REPORT_TO_HOST = true;
	
	private CrashConfig(){};
	
	

}
