package org.crashlog.sample;

import android.content.Context;
import android.content.Intent;

/**
 * 
 * This class will be ProGuard obfuscated for testing ReTrace. 
 * See more about ReTrace: http://proguard.sourceforge.net/index.html#manual/retrace/introduction.html
 * 
 * @author Liu,Yanjun
 * 
 */
public class ObfuscatedClass {
	public void methodC(Context context, float a) {
		Intent intent = new Intent("org.crashlog.UndefineActivity");
		context.startActivity(intent);
	}
	
	public void methodB(Context context, int a) {
		this.methodC(context, 0f);
	}
	
	public void methodA(Context context) {
		this.methodB(context, 0);
	}
}
