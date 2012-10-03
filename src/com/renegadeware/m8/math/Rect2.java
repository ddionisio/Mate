package com.renegadeware.m8.math;

import com.renegadeware.m8.AllocationGuard;

public class Rect2 extends AllocationGuard {
	public float x;
	public float y;
	public float width;
	public float height;
	
	public Rect2() {
		super();
	}
	
	public Rect2(float x, float y, float width, float height) {
		super();
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Rect2(Rect2 other) {
		super();
		set(other);
	}
	
	public void set(Rect2 other) {
		x = other.x;
		y = other.y;
		width = other.width;
		height = other.height;
	}
}
