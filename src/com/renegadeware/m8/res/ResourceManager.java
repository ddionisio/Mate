package com.renegadeware.m8.res;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.R;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.res.Resource.ResourceException;

import android.content.Context;
import android.util.SparseArray;

public abstract class ResourceManager extends BaseObject implements ScriptLoader {
	
	public static final int DEFAULT_ORDER_TEXTURE = 1000;
	public static final int DEFAULT_ORDER_TEXTURE_ATLAS = 2000;
	public static final int DEFAULT_ORDER_AUDIO = 3000;
	public static final int DEFAULT_ORDER_FONT = 4000;
	public static final int DEFAULT_ORDER_GRID = 5000;
	public static final int DEFAULT_ORDER_SPRITE = 6000;
	public static final int DEFAULT_ORDER_UI = 7000;
	
	//defType -> use for android internal resource table
	
	public final class ResourceManagerException extends Exception {
		private static final long serialVersionUID = 2054511712910545233L;
		
		public ResourceManagerException(String msg) {
			super("ResourceManagerException - " + msg);
		}
	}
	
	// Be sure to call this first on construction
	protected ResourceManager(int capacity) {
		super();
		
		resources = capacity > 0 ? new SparseArray<Resource>(capacity) : new SparseArray<Resource>();
		
		memoryUsage = 0;
		
		verbose = false;
	}
	
	/**
	 * Creates a new blank resource, but not loaded.
	 * <p>
	 * Resource managers handle disparate types of resources, so if you want
	 * to get at the detailed interface of this resource, you'll have to
	 * cast the result to the subclass you know you're creating.
	 *
	 * @param id The identifier from internal resources.
	 * @param group The group to attach this resource to.
	 * @param isManual (default: false) If this resource is manually loaded.
	 * @param loader (default: null) The loader if this resource is manually loaded.
	 * @param params (default: null) Parameters when creating an instance of this resource.
	 * 
	 * @return The new resource.
	 */
	public Resource create(int id, String group, boolean isManual, ManualResourceLoader loader, Resource.Param params) {
		Resource ret = createImpl(id, group, isManual, loader, params);
		
		if(params != null) {
			ret.setParameters(params);
		}
		
		addImpl(ret);
		
		// notify group manager
		systemRegistry.resourceGroupManager.notifyResourceCreated(ret);
		
		return ret;
	}
	
	public static final class CreateOrRetrievePair {
		public Resource res;
		public boolean isCreated;
		
		public CreateOrRetrievePair(Resource res, boolean isCreated) {
			this.res = res;
			this.isCreated = isCreated;
		}
	}
	
	/**
	 * Create a new resource, or retrieve an existing one with the same
	 * name if it already exists.
	 *
	 * This is a helper function for convenience, instead of calling getByName()
	 * followed by create() if getByName returns null.
	 *
	 * @param id The identifier from internal resources.
	 * @param group The group to attach this resource to.
	 * @param isManual (default: false) If this resource is manually loaded.
	 * @param loader (default: null) The loader if this resource is manually loaded.
	 * @param params (default: null) Parameters when creating an instance of this resource.
	 * 
	 * @return A pair, the first element being the resource, and the second being
	 *     	   an indicator specifying whether the resource was newly created.
	 */
	public CreateOrRetrievePair createOrRetrieve(int id, String group, boolean isManual, 
			ManualResourceLoader loader, Resource.Param params) {
		Resource res = getById(id);
		boolean created = false;
		if(res == null) {
			res = create(id, group, isManual, loader, params);
		}
		return new CreateOrRetrievePair(res, created);
	}
	
	/**
	 * Set a limit on the amount of memory this resource handler may use.
	 * <p>
     * If, when asked to load a new resource, the manager believes it will exceed this memory
     * budget, it will temporarily unload a resource to make room for the new one. This unloading
     * is not permanent and the Resource is not destroyed; it simply needs to be reloaded when
     * next used.
     *
	 * @param bytes Number of bytes to limit.
	 */
	public void setMemoryBudget(int bytes) {
		memoryBudget = bytes;
		checkUsage();
	}
	
	/** Get the memory budget number of bytes. */
	public int getMemoryBudget() {
		return memoryBudget;
	}
	
	/** Get the current memory usage in bytes. */
	public int memoryUsage() {
		return memoryUsage;
	}
	
	/**
	 * Unloads a single resource by id.
	 * <p>
	 * Unloaded resources are not removed, they simply free up their memory
	 * as much as they can and wait to be reloaded.
	 * 
	 * @param id The id from internal resource.
	 *
	 * @see ResourceGroupManager
	 */
	public void unload(int id) {
		Resource res = getById(id);
		if(res != null) {
			res.unload();
		}
	}

	/** Unload resource by name */
	public void unload(String name) {
		Resource res = getByName(name);
		if(res != null) {
			res.unload();
		}
	}
	
	/**
	 * Unloads all resources.
	 * <p>
	 * Unloaded resources are not removed, they simply free up their memory
	 * as much as they can and wait to be reloaded.
	 *
	 * @param reloadableOnly If true (the default), only unload the resource that
	 *        is reloadable. Because some resources isn't reloadable, they will be
	 *        unloaded but can't load them later. Thus, you might not want to them
	 *        unloaded. Or, you might unload all of them, and then populate them
	 *        manually later.
	 *
	 * @see Resource.isReloadable
	 */
	public synchronized void unloadAll(boolean reloadableOnly) {
		for(int i = 0; i < resources.size(); ++i) {
			final Resource res = resources.valueAt(i);
			if(!reloadableOnly || res.isReloadable()) {
				res.unload();
			}
		}
	}
	
	/** This calls removeAll() */
	@Override
	public void reset() {
		removeAll();
	}
	
	/**
	 * Caused all currently loaded resources to be reloaded.
	 * <p>
	 * All resources currently being held in this manager which are also
	 * marked as currently loaded will be unloaded, then loaded again.
	 *
	 * @param reloadableOnly If true (the default), only reload the resource that
	 *        is reloadable. Because some resources isn't reloadable, they will be
	 *        unloaded but can't loaded again. Thus, you might not want to them
	 *        unloaded. Or, you might unload all of them, and then populate them
	 *        manually later.
	 * @throws ResourceException 
	 *
	 * @see Resource.isReloadable
	 */
	public synchronized void reloadAll(boolean reloadableOnly) throws ResourceException {
		int numReloaded = 0;
		for(int i = 0; i < resources.size(); ++i) {
			final Resource res = resources.valueAt(i);
			if(!reloadableOnly || res.isReloadable()) {
				res.reload();
				numReloaded++;
			}
		}
		
		DebugLog.d("Mate", "resource manager "+name()+" reloaded: "+numReloaded);
	}
	
	public synchronized void invalidateAll(boolean reloadableOnly) throws ResourceException {
		int numInvalidated = 0;
		for(int i = 0; i < resources.size(); ++i) {
			final Resource res = resources.valueAt(i);
			if(!reloadableOnly || res.isReloadable()) {
				res.invalidateImpl();
				numInvalidated++;
			}
		}
		
		DebugLog.d("Mate", "resource manager "+name()+" num invalidated: "+numInvalidated);
	}
	
	/**
	 * Remove a single resource by ref.
	 * <p>
	 * Removes a single resource, meaning it will be removed from the list
	 * of valid resources in this manager, also causing it to be unloaded.
	 * <p>
	 * The word 'Destroy' is not used here, since
	 * if any other pointers are referring to this resource, it will persist
	 * until they have finished with it; however to all intents and purposes
	 * it no longer exists and will likely get destroyed imminently.
	 * <p>
	 * If you do have shared pointers to resources hanging around after the
	 * ResourceManager is destroyed, you may get problems on destruction of
	 * these resources if they were relying on the manager (especially if
	 * it is a plugin). If you find you get problems on shutdown in the
	 * destruction of resources, try making sure you release all your
	 * shared pointers before you shutdown your app.
	 */
	public void remove(Resource res) {
		removeImpl(res);
	}
	
	public void remove(String resName) {
		Resource res = getByName(resName);
		if(res != null) {
			removeImpl(res);
		}
	}
	
	public void remove(int id) {
		Resource res = getById(id);
		if(res != null) {
			removeImpl(res);
		}
	}
	
	/** Removes all resources. */
	public synchronized void removeAll() {
		resources.clear();
		
		systemRegistry.resourceGroupManager.notifyAllResourcesRemoved(this);
	}
	
	/** Retrieves the resource by id. */
	public synchronized Resource getById(int id) {
		return resources.get(id);
	}
	
	/** Retrieves the resource by name, which is parsed to a resource id. */
	public synchronized Resource getByName(String name) {
		if(name == null || name.length() == 0) {
			return null;
		}
		
		return resources.get(getResourceIdByName(name));
	}
	
	/** Returns whether the resource exists in this manager. */
	public boolean resourceExists(int id) {
		return resources.get(id) != null;
	}
	
	/** Returns whether the resource by name exists in this manager. */
	public boolean resourceExists(String name) {
		return resources.get(getResourceIdByName(name)) != null;
	}
	
	/** Use in Resource only!<p>Notify this manager that a resource which it manages has been * 'touched', i.e. used. */
	public void notifyResourceTouched(Resource res) {
	}

	/** Use in Resource only!<p>Notify this manager that a resource which it manages has been loaded. */
	public synchronized void notifyResourceLoaded(Resource res) {
	    memoryUsage += res.getSize();
	}

	/** Use in Resource only!<p>Notify this manager that a resource which it manages has been unloaded. */
	public synchronized void notifyResourceUnloaded(Resource res) {
	    memoryUsage -= res.getSize();
	}
	
	/**
	 * Generic prepare method, used to create a Resource specific to this
	 * ResourceManager without using one of the specialised 'prepare' methods
	 * (containing per-Resource-type parameters).
	 *
	 * @param id The id within the internal resource.
	 * @param group The resource group to which this resource will belong.
	 * @param isManual (default: false) Is the resource to be manually loaded? If so, you should
	 *        provide a value for the loader parameter
	 * @param loader (default: null) The manual loader which is to perform the required actions
	 *        when this resource is loaded; only applicable when you specify true
	 *        for the previous parameter
	 * @param loadParams (default: null) Array of string values.
	 * @param backgroundThread (default: false) boolean which lets the load routine know if it
	 *        is being run on the background resource loading thread
	 * @throws ResourceException 
	 */
	public Resource prepare(int id, String group, boolean isManual, ManualResourceLoader loader,
			Resource.Param loadParams, boolean backgroundThread) throws ResourceException {
		Resource res = createOrRetrieve(id, group, isManual, loader, loadParams).res;
		res.prepare(backgroundThread);
    	return res;
    }
	
	public Resource prepare(String name, String group, boolean isManual, ManualResourceLoader loader,
			Resource.Param loadParams, boolean backgroundThread) throws ResourceException {
    	return prepare(getResourceIdByName(name), group, isManual, loader, loadParams, backgroundThread);
    }
	
	/**
	 * Generic load method, used to create a Resource specific to this
	 * ResourceManager without using one of the specialised 'load' methods
	 * (containing per-Resource-type parameters).
	 *
	 * @param id The id within the internal resource.
	 * @param group The resource group to which this resource will belong.
	 * @param isManual (default: false) Is the resource to be manually loaded? If so, you should
	 *        provide a value for the loader parameter
	 * @param loader (default: null) The manual loader which is to perform the required actions
	 *        when this resource is loaded; only applicable when you specify true
	 *        for the previous parameter
	 * @param loadParams (default: null) Array of string values.
	 * @param backgroundThread (default: false) boolean which lets the load routine know if it
	 *        is being run on the background resource loading thread
	 * @throws ResourceException 
	 */
	public Resource load(int id, String group, boolean isManual, ManualResourceLoader loader,
			Resource.Param loadParams, boolean backgroundThread) throws ResourceException {
		Resource res = createOrRetrieve(id, group, isManual, loader, loadParams).res;
		res.load(backgroundThread);
    	return res;
    }
	
	public Resource load(String name, String group, boolean isManual, ManualResourceLoader loader,
			Resource.Param loadParams, boolean backgroundThread) throws ResourceException {
    	return load(getResourceIdByName(name), group, isManual, loader, loadParams, backgroundThread);
    }
	
	// Make sure to implement this if you are going to have this manager parse scripts.
    public void parseScript(int id, String groupName) {}
    
    /** Sets whether this manager and its resources habitually produce log output */
    public void setVerbose(boolean v) { 
    	verbose = v; 
    }

    /** Gets whether this manager and its resources habitually produce log output */
    public boolean getVerbose() { 
    	return verbose; 
    }
    
    /**
     * Returns an associative array of resources (k=int id, v=Resource).
     * <p>
     * Use of this is asynchronous.
     */
    public SparseArray<Resource> getResources() { 
    	return resources; 
    }
    
    /**
     * Returns the internal resource id based on given name.  Make sure that context of the app has been
     * initialized and set.
     * 
     * @param name The resource label. If you specify name by definition/id, ie. string/hello,
     *             then definition type will be 'string' and id will be 'hello'
     * @return id, 0 if not found or invalid
     */
    public int getResourceIdByName(String name) {
    	final Context c = systemRegistry.contextParameters.context;
    	
    	assert c != null;
    	
    	int id = c.getResources().getIdentifier(name, defType(), c.getPackageName());
    	
    	if(id == 0) {
    		DebugLog.e(DTag, "Unable to find resource id for: "+name);
    	}
    	
    	return id;
    }
    
    /**
     * Returns the internal resource id based on given filename.  Make sure that context of the app has been
     * initialized and set.  This will strip the extension, but does not cull folder path
     * 
     * @param filename The name of the file in this format: file.ext
     * @return id, 0 if not found or invalid
     */
    public int getResourceIdByFilename(String filename) {
    	final Context c = systemRegistry.contextParameters.context;
    	
    	assert c != null;
    	
    	int id = 0;
    	
    	int eInd = filename.lastIndexOf('.');
		if(eInd > 0) {
			String name = filename.substring(0, eInd);
			
			id = c.getResources().getIdentifier(name, defType(), c.getPackageName());
	    	
	    	if(id == 0) {
	    		DebugLog.e(name(), "Unable to find resource id for: "+name);
	    	}
		}
		else {
			DebugLog.e(name(), "Unable to find file extension for: "+filename);
		}
    	    	    	
    	return id;
    }
    
    /* **********************************************************************************************
	 * Protected Methods
	 * **********************************************************************************************/
    
    /**
     * Create a new resource instance compatible with this manager (no custom
     * parameters are populated at this point).
     * <p>
     * Subclasses must override this method and create a subclass of Resource.
     *
     * @param id The unique id for use with internal resource.
     * @param group The name of the resource group to attach this new resource to.
     * @param isManual Is this resource manually loaded? If so, you should really
     *        populate the loader parameter in order that the load process
     *        can call the loader back when loading is required.
     * @param loader Pointer to a ManualLoader implementation which will be called
     *        when the Resource wishes to load (should be supplied if you set
     *        isManual to true). You can in fact leave this parameter null
     *        if you wish, but the Resource will never be able to reload if
     *        anything ever causes it to unload. Therefore provision of a proper
     *        ManualLoader instance is strongly recommended.
     * @param params If any parameters are required to create an instance,
     *        they should be supplied here as an array of strings. These do not need
     *        to be set on the instance (handled elsewhere), just used if required
     *        to differentiate which concrete class is created.
     */
     protected abstract Resource createImpl(int id, String group, boolean isManual,
                            ManualResourceLoader loader, Resource.Param params);
     
     /** Add a newly created resource to the manager */
     protected synchronized void addImpl(Resource res) {
    	final int id = res.id;
    	
    	boolean exists = resources.get(id) != null;
    	
    	if(exists) {
    		// Resolve the collision
    		final ResourceGroupManager.ResourceLoadingListener l = systemRegistry.resourceGroupManager.getLoadingListener();
    		if(l != null) {
    			if(l.resourceCollision(res, this)) {
    				// Try to add it again
    				assert(resources.get(id) == null);
    				
    		    	resources.append(id, res);
    			}
    		}
    	}
    	else {
    		resources.append(id, res);
    	}
     }
     
     /** Remove a resource from this manager; remove it from the lists. */
     protected synchronized void removeImpl(Resource res) {   	 
    	 resources.delete(res.id);
    	 
    	 // notify the group manager
    	 systemRegistry.resourceGroupManager.notifyResourceRemoved(res);
     }
     
     /** Checks memory usage and pages out if required. */
     protected void checkUsage() {
    	 
     }
	
    /* **********************************************************************************************
	 * Properties
	 * **********************************************************************************************/
    
    protected static final String DTag = "ResourceManager";
	
    protected SparseArray<Resource> resources;
	
	private int memoryUsage;
	private int memoryBudget;
	
	private boolean verbose;
	
	// Make sure to fill these!
	
	//int loadingOrder();
	//String name();
	//String defType();
}
