package com.renegadeware.m8.math;

public class Math {
	public static final float PI = (float)java.lang.Math.PI;
	public static final float PI_HALF = PI*0.5f;
	public static final float PI_2 = (float)(java.lang.Math.PI*2.0);

	public static final float degreeToRadian(float degree) {
		return degree*(PI/180.0f);
	}
	
	public static final float hermite(float v1, float t1, float v2, float t2, float s) {
		float s2 = s*s;
		float s3 = s*s*s;
		
		float h1 =  2*s3 - 3*s2 + 1;
		float h2 = -2*s3 + 3*s2;
		float h3 =    s3 - 2*s2 + s;
		float h4 =    s3 - s2;
		
		return h1*v1 + h2*v2 + h3*t1 + h4*t2;
	}
}
