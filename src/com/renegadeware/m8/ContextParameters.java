package com.renegadeware.m8;

import android.content.Context;

public final class ContextParameters {
	public boolean debugEnabled;
	public int viewWidth;
    public int viewHeight;
    public Context context;
	public int gameWidth;
	public int gameHeight;
	public int gameHalfWidth;
	public int gameHalfHeight;
	public float viewScaleX;
	public float viewScaleY;
	public boolean supportsDrawTexture;
	public boolean supportsVBOs;
	
	/**
	 * Returns a resource id from current context package.
	 * 
	 * @param name The label of the id, can be defType/id, e.g. string/hello
	 * @param defType Optional
	 * @return The resource id, 0 if not found
	 */
	public int getResourceId(String name, String defType) {
		if(context == null) {
			return 0;
		}
		
		return context.getResources().getIdentifier(name, defType, context.getPackageName());
	}
}
