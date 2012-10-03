package com.renegadeware.m8.util;

public final class Flag {
	private int flags;
	
	public Flag() {
		flags = 0;
	}
	
	public Flag(int mask) {
		flags = mask;
	}
	
	public final void set(int mask) {
    	flags |= mask;
    }
    
    public final void remove(int mask) {
    	flags &= ~mask;
    }
    
    public final void flip(int mask) {
    	flags ^= mask;
    }
    
    public final int get(int mask) {
    	return flags & mask;
    }
    
    public final boolean check(int mask) {
    	return (flags & mask) == mask;
    }
    
    public final int getMasks() {
    	return flags;
    }
    
    public final long getValue() {
    	return flags & 0xffffffff;
    }
    
    public final void clear() {
    	flags = 0;
    }
}
