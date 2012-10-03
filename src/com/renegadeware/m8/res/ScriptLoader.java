package com.renegadeware.m8.res;

public interface ScriptLoader {
	/**
	 * Parse a script file.
	 *
	 * @param id Reference to an internal resource file.
	 * @param groupName The name of a resource group which should be used if any resources
	 *     are created during the parse of this script.
	 */
	void parseScript(int id, String groupName);

	/**
	 * Gets the relative loading order of scripts of this type.
	 * <p>
	 * There are dependencies between some kinds of scripts, and to enforce
	 * this all implementors of this interface must define a loading order.
	 *
	 * @return A value representing the relative loading order of these scripts
	 * compared to other script users, where higher values load later.
	 */
	int loadingOrder();
	
	/**
	 * The name of this loader.  Make sure this is unique from all other loaders.
	 * @return A string identifier.
	 */
	String name();
	
	/**
	 * Definition type for used with associating a name from an internal resource id.
	 *  
	 * @return The string type, e.g. "drawable", "raw", etc.
	 */
	String defType();
}
