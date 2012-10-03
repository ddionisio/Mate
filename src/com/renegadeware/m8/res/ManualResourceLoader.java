package com.renegadeware.m8.res;

public interface ManualResourceLoader {
	
	/**
	 * Called when a resource wishes to prepare.
	 * 
	 * @param res The resource that wants to prepare.
	 */
	void prepare(Resource res);

	/**
	 * Called when a resource wishes to load. Note that this could get
	 * called in a background thread. Thus, you must not access the rendersystem from
	 * this callback.  Do that stuff in load.
	 * 
	 * @param res The resource that wants to load.
	 */
	void load(Resource res);
}
