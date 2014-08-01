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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;


/**
 * 
 * The utility class provides methods for store crash log.
 * 
 * @author Liu,Yanjun
 * @see http://www.crashlog.org
 *
 */
final class CrashLogStore {
	private CrashLogStore() { }
	
	
	private static String getProcessName(Application app, int pid) {
		ActivityManager am = (ActivityManager) app.getApplicationContext().getSystemService(Activity.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
		Iterator<RunningAppProcessInfo> it = processes.iterator();
		while (it.hasNext()) {
			RunningAppProcessInfo info = it.next();
			if (info.pid == pid) {
				return info.processName;
			}
		}
		return "";
	}
	
	private static long getInternalAvailableSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}
	
	private static long getInternalTotalSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath()); 
		long blockSize = stat.getBlockSize(); 
		long blockCount = stat.getBlockCount();
		return blockCount * blockSize; 
	}
	
	
	private static long getExternalAvailableSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}
	
	private static long getExternalTotalSize(){
		File path = Environment.getExternalStorageDirectory(); 
		StatFs stat = new StatFs(path.getPath()); 
		long blockSize = stat.getBlockSize(); 
		long blockCount = stat.getBlockCount();
		return blockCount * blockSize; 
	}
	
	private static String getVersionName(Application app) {
        String versionName = "";
		try {
			PackageManager pm = app.getPackageManager();  
	        PackageInfo pi = pm.getPackageInfo(app.getPackageName(), 0);
	        versionName = pi.versionName;
		} catch (NameNotFoundException e) {
			Log.i(CrashConfig.TAG, "Unable to get 'VersionName'.", e);
		} 
		return versionName;
	}
	
	private static String getCrashId(String versionName, String cause, String detailMessage, StackTraceElement[] stList) {
		StringBuilder buff = new StringBuilder();
		buff.append(versionName);
		buff.append(cause);
		buff.append(detailMessage);
		if(stList != null) {
			for(int i = 0; i < stList.length; i++) {
				StackTraceElement st = stList[i];
				buff.append(st.toString());
			}
		}
		return MD5Util.toMD5(buff.toString());
	}

	public static void writeBoolean(OutputStream out, boolean val) throws IOException {
		out.write(val ? 1 : 0);
	}
	
	
	public static void writeByte(OutputStream out, byte val) throws IOException {
		out.write(val);
	}
	
	public static void writeInt(OutputStream out, int val) throws IOException {
		byte[] buff = new byte[4];
        buff[0] = (byte) (val >>> 24);  
        buff[1] = (byte) (val >>> 16);  
        buff[2] = (byte) (val >>> 8);  
        buff[3] = (byte) val;  
        
        out.write(buff, 0, 4);  
	}
	
	public static void writeLong(OutputStream out, long val) throws IOException {
    	byte[] buff = new byte[8];
        buff[0] = (byte) (val >>> 56);  
        buff[1] = (byte) (val >>> 48);  
        buff[2] = (byte) (val >>> 40);  
        buff[3] = (byte) (val >>> 32);  
        buff[4] = (byte) (val >>> 24);  
        buff[5] = (byte) (val >>> 16);  
        buff[6] = (byte) (val >>> 8);  
        buff[7] = (byte) val;  
        out.write(buff, 0, 8);  
    }  
	
	
	public static void writeUTF(OutputStream out, String s) throws IOException {
		int utflen = 0;
		byte[] bytes = null;
		if (s != null && s.length() > 0) {
			bytes = s.getBytes("UTF-8");
			utflen = bytes.length;
			if (utflen > 65535) {
				throw new IOException("encoded string too long: " + utflen
						+ " bytes");
			}
		}

		out.write((utflen >>> 8) & 0xff);
		out.write((utflen >>> 0) & 0xff);
		if (bytes != null) {
			out.write(bytes);
		}
	}
	
	public static void writeStackTrace(OutputStream out, Throwable ex) throws IOException {
		StackTraceElement[] elements = ex.getStackTrace();
		test(elements);
		
		int len = elements.length;
		if (len > 65535) {
			throw new IOException("StackTrace elements too much: " + len);
		}
		out.write((len >>> 8) & 0xff);
		out.write((len >>> 0) & 0xff);
		
		for(int i = 0; i < elements.length; i++) {
			StackTraceElement ste = elements[i];
			writeUTF(out, ste.getClassName());
			writeUTF(out, ste.getFileName());
			writeUTF(out, ste.getMethodName());
			writeInt(out, ste.getLineNumber());
			writeBoolean(out, ste.isNativeMethod());
		}
	}
	
	public static void test(StackTraceElement[] stackTraceElements) {
		try {
			for (StackTraceElement element : stackTraceElements) {
				Class<?> clazz = Class.forName(element.getClassName());
				if(!element.getClassName().equals("org.crashlog.sample.ObfuscatedClass")){
					continue;
				}
				
				Log.d(CrashConfig.TAG, "----------" + element.getClassName() + "------------");
				Method[] methods = clazz.getDeclaredMethods();
				for (int i = 0; i < methods.length; i++) {
					Method method = methods[i];
//					if (method.getName().equals(element.getMethodName())) {
						Log.d(CrashConfig.TAG, method.toString());
//					}
				}
			}
			
//			HashMap<String, ArrayList<Method>> methodMap = new HashMap<String, ArrayList<Method>>();
//			for (StackTraceElement element : stackTraceElements) {
//				Class<?> clazz = Class.forName(element.getClassName());
//				ArrayList<Method> methods = new ArrayList<Method>(Arrays.asList(clazz.getDeclaredMethods()));
//				methodMap.put(element.getClassName(), methods);
//				
//				Log.d(CrashConfig.TAG, "L:" + element.getLineNumber() + "\t" + element.getClassName() + "." + element.getMethodName());
//			}
//
//			for (StackTraceElement element : stackTraceElements) {
//				ArrayList<Method> methods = methodMap.get(element.getClassName());
//				for (Method method : methods) {
//					if (method.getName().equals(element.getMethodName())) {
//						Log.d(CrashConfig.TAG, "L:" + element.getLineNumber() + "\t" + method);
//						methods.remove(method);
//						break;
//					}
//				}
//
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeLogData(OutputStream out, File logFile)  throws IOException{
//		if(!logFile.canRead()) {
//			return;
//		}
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(logFile);
			byte[] data = new byte[(int) logFile.length()];
			in.read(data);
			out.write(data); //Write log data
		} finally {
			if(in != null) {
				in.close();
			}
		}
	}
	
	public static void writeDataHeader(Application app, OutputStream out) throws IOException {
		writeInt(out, CrashConfig.MAGIC_NUM);
		writeInt(out, CrashConfig.VERSION);
		writeUTF(out, getVersionName(app));
		writeByte(out, CrashConfig.PLATFORM);
		String model = android.os.Build.MODEL;
		writeUTF(out, model);
		int sdkLevel = android.os.Build.VERSION.SDK_INT;
		writeInt(out, sdkLevel);
		String sdkRelease = android.os.Build.VERSION.RELEASE;
		writeUTF(out, sdkRelease);
		String packageName = app.getPackageName();
		writeUTF(out, packageName);
		
	}
	
	public static void deleteLogFiles(File[] logFiles) {
		for (int i = 0; i < logFiles.length; i++) {
			logFiles[i].delete();
		}
	}
	
	public synchronized static void saveLogToFile(Application app, Throwable throwable, Thread thread) throws IOException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss",
				Locale.getDefault());
		String fileName = CrashConfig.LOG_FILE_PREFIX
				+ dateFormat.format(new Date()) + CrashConfig.LOG_FILE_EXT;
		String dirPath = app.getExternalCacheDir().getPath() + File.separator
				+ CrashConfig.LOG_DIR;

		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, fileName);
		if (!file.exists()) {
			file.createNewFile();
		}

		BufferedOutputStream out = null;
//		FileOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));

			//crash id
			String versionName = getVersionName(app);
			String crashId = getCrashId(versionName, throwable.getClass()
					.getName(), throwable.getLocalizedMessage(),
					throwable.getStackTrace());
			writeUTF(out, crashId);

			// time
			long crashTime = System.currentTimeMillis();
			writeLong(out, crashTime);

			// memory
			Runtime rt = Runtime.getRuntime();
			long allocMem = rt.totalMemory() - rt.freeMemory();
			writeLong(out, allocMem);
			writeLong(out, rt.maxMemory()); // total
			// Log.d(TAG, "allocMem:" + allocMem + " freeMem:" + (rt.maxMemory()
			// - allocMem) + " totalMem:" + rt.maxMemory());

			// storage
			writeLong(out, getInternalAvailableSize());
			writeLong(out, getInternalTotalSize());
			writeLong(out, getExternalAvailableSize());
			writeLong(out, getExternalTotalSize());

			// thread&process
			writeUTF(out, thread.getName());
			writeUTF(out, getProcessName(app, android.os.Process.myPid()));

			//message
			writeUTF(out, throwable.getClass().getName()); //cause
			writeUTF(out, throwable.getLocalizedMessage()); // detail message
			
			// stack trace
//			Log.d(CrashConfig.TAG, "crash message:" + throwable.getMessage());
			writeStackTrace(out, throwable);
			out.flush();

		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}
