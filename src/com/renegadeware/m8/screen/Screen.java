package com.renegadeware.m8.screen;

/**
 * @author ddionisio
 *
 */
public interface Screen {
	
	/**
	 * Any data that needs to be loaded happen here. Opengl is guaranteed to be valid.
	 */
	public void load();
	
	/**
	 * After the screen is popped, any data that needs to be unloaded happen here.
	 * Opengl is guaranteed to be valid.
	 */
	public void unload();
	
	/**
     * Update this screen.
     * @param timeDelta  The duration since the last update (in seconds).
     */
	public void update(float timeDelta);
	
	/**
	 * Once the screen is loaded, this is called to prepare for entry.
	 * <p>
	 * Use this to start any entrance animation, e.g. fade-in
	 */
	public void startEnter();
	
	/**
	 * Called while this screen is pending push.  All entrance animation happens here.
	 * 
	 * @param timeDelta The duration since the last update (in seconds).
	 * @return false if we are done with entry animation.
	 */
	public boolean enterUpdate(float timeDelta);
	
	/**
	 * Once the screen is popped, they are pending removal.
	 * <p>
	 * Use this to start any exit animation, e.g. fade-out
	 */
	public void startExit();
	
	/**
	 * This is called when this screen is set to pending pop. All exit animation happens here.
	 * <p>
	 * Once exit is done, this screen is ready to be unloaded.
	 * 
	 * @param timeDelta The duration since the last update (in seconds).
	 * @return false if we are done with exit animation.
	 */
	public boolean exitUpdate(float timeDelta);
	
	/**
	 * When another screen is pushed, this is called.  Ideally you'll want to disable input 
	 * and gameplay from update.
	 */
	public abstract void pause();
	
	/**
	 * When the last screen is popped or this is the last push, 
	 * this is called afterwards as this screen becomes the new top.
	 * <p>
	 * Ideally (re)establish input and other game updates.
	 */
	public abstract void resume();
	
	/**
	 * Called when the phone's back button is pressed.
	 * @return true when we want to exit the app., false otherwise
	 */
	public abstract boolean back();
}
