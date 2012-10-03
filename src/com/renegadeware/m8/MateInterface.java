package com.renegadeware.m8;

import android.content.SharedPreferences;

public interface MateInterface {
	/**
	 * Called during activity creation, this is where you want to initialize specific systems, game, and
	 * ready any resources for used throughout the app.
	 * 
	 * @param prefs Preferences for this App.
	 */
    public void init(SharedPreferences prefs);
    
    public void start();
    
    public void resume(SharedPreferences prefs);
    
    public void pause();
    
    public void stop();
    
    public void destroy();
    
    /**
     * Called when surface has been created, this will also reload any graphics specific data: textures, buffers
     */
    public void surfaceCreated();
    
    /**
     * Called after surface change completes in renderer, usually when orientation changes.
     */
    public void surfaceReady();
    
    /**
     * Called when back button is pressed
     */
    public void back();
    
    /**
     * Called when menu button is pressed
     */
    public void menu();
}
