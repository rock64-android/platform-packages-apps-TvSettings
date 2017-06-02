package com.android.tv.settings.util;

public class JniCall {
	static {
		System.loadLibrary("tvsettings-jni");
	}

	//public static native boolean test();
	//最大亮度，最小亮度曲线
	public static native int[] get(double x, double y);
	//亮度，饱和度曲线
	public static native int[] getOther(double x, double y);
	//电视是否支持HDR
	public static native boolean isSupportHDR();
	//设置电视HDR是否可用
	public static native void setHDREnable(int enable);
}
