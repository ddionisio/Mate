package com.renegadeware.m8.gfx;

public class Color {
	public static final Color WHITE = new Color();
	public static final Color BLACK = new Color(0.0f,0.0f,0.0f);
	public static final Color GREEN = new Color(0.0f,1.0f,0.0f);
	public static final Color RED = new Color(1.0f,0.0f,0.0f);
	public static final Color YELLOW = new Color(1.0f,1.0f,0.0f);
	
	public float red;
	public float green;
	public float blue;
	public float alpha;
	
	public Color() {
		reset();
	}
	
	public Color(float r, float g, float b) {
		set(r,g,b);
	}
	
	public Color(float r, float g, float b, float a) {
		set(r,g,b,a);
	}
	
	public Color(int val) {
		set(val);
	}
	
	public Color(int r, int g, int b) {
		set(((float)r)/255.0f, ((float)g)/255.0f, ((float)b)/255.0f);
	}
	
	public Color(int r, int g, int b, int a) {
		set(((float)r)/255.0f, ((float)g)/255.0f, ((float)b)/255.0f, ((float)a)/255.0f);
	}
	
	public Color(Color c) {
		set(c);
	}
	
	public void set(Color c) {
		if(c == null) {
			red = green = blue = alpha = 1.0f;
		}
		else {
			red = c.red;
			green = c.green;
			blue = c.blue;
			alpha = c.alpha;
		}
	}
	
	public void setLerp(Color src, Color dst, float t) {
		red = src.red + t*(dst.red-src.red);
		green = src.green + t*(dst.green-src.green);
		blue = src.blue + t*(dst.blue-src.blue);
		alpha = src.alpha + t*(dst.alpha-src.alpha);
	}
	
	public void set(int val) {
		set(android.graphics.Color.red(val), 
			android.graphics.Color.green(val),
			android.graphics.Color.blue(val),
			android.graphics.Color.alpha(val));
	}
	
	public void set(int r, int g, int b) {
		set(((float)r)/255.0f, ((float)g)/255.0f, ((float)b)/255.0f);
	}
	
	public void set(int r, int g, int b, int a) {
		set(((float)r)/255.0f, ((float)g)/255.0f, ((float)b)/255.0f, ((float)a)/255.0f);
	}
	
	public void set(float r, float g, float b) {
		red = r;
		green = g;
		blue = b;
		alpha = 1.0f;
	}
	
	public void set(float r, float g, float b, float a) {
		red = r;
		green = g;
		blue = b;
		alpha = a;
	}
	
	public void reset() {
		red = green = blue = alpha = 1.0f;
	}
	
	public float sum() {
		return red+green+blue+alpha;
	}
}
