package com.renegadeware.m8.res;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.obj.BaseObject;

public abstract class Resource extends BaseObject {
	
	public final class ResourceException extends Exception {
		private static final long serialVersionUID = -232790952642011950L;
		
		public ResourceException(String msg) {
			super("ResourceException - " + msg);
		}
	}
	
	public static interface Param {
		
	}
	
	public static interface Listener {
		/**
         * Called whenever the resource finishes loading.
         * <p>
         * If a Resource has been marked as background loaded (@see Resource::setBackgroundLoaded),
         * the call does not itself occur in the thread which is doing the loading;
         * when loading is complete a response indicator is placed with the
         * ResourceGroupManager, which will then be sent back to the
         * listener as part of the application's primary frame loop thread.
         */
        void loadingComplete(Resource res);

        /**
         * Called whenever the resource finishes preparing (paging into memory).
         * <p>
         * If a Resource has been marked as background loaded
         * the call does not itself occur in the thread which is doing the preparing;
         * when preparing is complete a response indicator is placed with the
         * ResourceGroupManager, which will then be sent back to the
         * listener as part of the application's primary frame loop thread.
         */
        void preparingComplete(Resource res);

        /** Called whenever the resource has been unloaded. */
        void unloadingComplete(Resource res);
	}
	
	/** Not loaded */
	public static final int StatusUnloaded = 0;
	
	/** Loading is in progress */
	public static final int StatusLoading = 1;
	
	/** Fully loaded */
	public static final int StatusLoaded = 2;
	
	/** Currently unloading */
	public static final int StatusUnloading = 3;
	
	/** Fully prepared */
	public static final int StatusPrepared = 4;
	
	/** Preparing is in progress */
	public static final int StatusPreparing = 5;
	
	/**
	 * Standard constructor.
	 *
	 * @param creator The ResourceManager that is creating this resource
	 * @param id The id where the resource is loaded
	 * @param group The name of the resource group to which this resource belongs
	 * @param isManual (default: false) Is this resource manually loaded? If so, you should really
	 *        populate the loader parameter in order that the load process
	 *        can call the loader back when loading is required.
	 * @param loader (default: null) a ManualResourceLoader implementation which will be called
	 *        when the Resource wishes to load (should be supplied if you set
	 *        isManual to true). You can in fact leave this parameter null
	 *        if you wish, but the Resource will never be able to reload if
	 *        anything ever causes it to unload. Therefore provision of a proper
	 *        ManualResourceLoader instance is strongly recommended.
	 */
	public Resource(ResourceManager creator, int id, String group, boolean isManual, ManualResourceLoader loader) {
		super();
		
		this.creator = new WeakReference<ResourceManager>(creator);
		this.id = id;
		this.group = group;
		
		loadingStatus = StatusUnloaded;
		loadingStatusLock = new Object();
		
		isBackgroundLoaded = false;
		size = 0;
		this.isManual = isManual;
		this.loader = loader;
		stateCount = 0;
		listenerList = new LinkedList<Listener>();
	}
	
	protected Resource(int id) {
		super();
		
		this.id = id;
		
		loadingStatus = StatusUnloaded;
		loadingStatusLock = new Object();
		
		isBackgroundLoaded = false;
		size = 0;
		isManual = false;
		loader = null;
		listenerList = new LinkedList<Listener>();
		creator = null;
	}
		 
	/**
	 * Prepares the resource for load, if it is not already.  One can call prepare()
	 * before load(), but this is not required as load() will call prepare()
	 * itself, if needed. Both load() and prepare() are thread-safe.
	 *
	 * @param backgroundThread (default: false) Whether this is occurring in a background thread
	 * 
	 * @throws ResourceException
	 */
	public void prepare(boolean backgroundThread) throws ResourceException {
		int prevStatus = loadingStatus;
		
		if(prevStatus != StatusUnloaded && prevStatus != StatusPreparing) {
			return;
		}
		
		// Set the preparing, if the current state is not unloaded, then we're in trouble
        if(!loadingStatusCas(StatusUnloaded, StatusPreparing)) {
        	// Someone else is preparing?
        	while(loadingStatus == StatusPreparing) {
        		try {
        			loadingStatusLock.wait();
				} catch (InterruptedException e) {
				}
        	}
        	
        	final int status = loadingStatus;
        	if(status != StatusPrepared && status != StatusLoading && status != StatusLoaded) {
        		throw new ResourceException("prepare: Another thread failed in resource operation.");
        	}
        	
        	return;
        }
        
        // Now preparing this guy
        try {
        	synchronized (this) {

        		if(isManual) {
        			if(loader != null) {
        				loader.prepare(this);
        			}
        			else {
        				DebugLog.w(DTag, "preparing "+id+" of type "+getCreator().name()+" was defined as manually loaded, but no manual loader was provided. This resource will be lost if it has to be reloaded.");
        			}
        		}
        		else {
        			prepareImpl();
        		}
        	}
        } catch(Exception e) {
        	DebugLog.w("Resource", e.toString());
        	
        	setLoadingStatus(StatusUnloaded);
        }
        
        setLoadingStatus(StatusPrepared);
        
        if(!backgroundThread) {
        	firePreparingComplete(false);
        }
	}
	
	/**
	 * Loads the resource, if it is not already.
	 * <p>
	 * If the resource is loaded from a file, loading is automatic. If not,
	 * if for example this resource gained it's data from procedural calls
	 * rather than loading from a file, then this resource will not reload
	 * on it's own.
	 *
	 * @param backgroundThread (default: false) Indicates whether the caller of this method is
	 *        the background resource loading thread.
	 *        
	 * @throws ResourceException
	 */
	public void load(boolean backgroundThread) throws ResourceException {
		// Early-out without lock (mitigate perf cost of ensuring loaded)
		// Don't load if:
		// 1. We're already loaded
		// 2. Another thread is loading right now
		// 3. We're marked for background loading and this is not the background
		//    loading thread we're being called by
		
		if(isBackgroundLoaded && !backgroundThread) {
			return;
		}
		
		// This next section is to deal with cases where 2 threads are fighting over
		// who gets to prepare / load - this will only usually happen if loading is escalated
		boolean keepChecking = true;
		int old = StatusUnloaded;
		while (keepChecking) {
			// quick check that avoids any synchronisation
			old = loadingStatus;
			
			if(old == StatusPreparing) {
				while(loadingStatus == StatusPreparing) {
					try {
						loadingStatusLock.wait();
					} catch (InterruptedException e) {
					}
				}
				old = loadingStatus;
			}
			
			if(old != StatusUnloaded && old != StatusPrepared && old != StatusLoading) {
				return;
			}
			
			// atomically do slower check to make absolutely sure,
			// and set the load state to LOADING
			if(old == StatusLoading || !loadingStatusCas(old, StatusLoading)) {
				while(loadingStatus == StatusLoading) {
					try {
						loadingStatusLock.wait();
					} catch (InterruptedException e) {
					}
				}
				
				final int status = loadingStatus;
				if(status == StatusPrepared || status == StatusPreparing) {
					// another thread is preparing, loop around
					continue;
				}
				else if(status != StatusLoaded) {
					throw new ResourceException("load: Another thread failed in resource operation");
				}
				
				return;
			}
			keepChecking = false;
		}
		
		try {
			synchronized (this) {
				if(isManual) {
					preLoadImpl();
					// Load from manual loader
					if(loader != null) {
						loader.load(this);
					}
					else {
						DebugLog.w(DTag, "loading "+id+" of type "+getCreator().name()+" was defined as manually loaded, but no manual loader was provided. This resource will be lost if it has to be reloaded.");
					}
					postLoadImpl();
				}
				else {
					if(old == StatusUnloaded) {
						prepareImpl();
						if(!backgroundThread) {
							firePreparingComplete(false);
						}
					}
					
					preLoadImpl();
					
					old = StatusPrepared;
					
					loadImpl();
					
					postLoadImpl();
				}
				
				size = calculateSize();
			}
		} catch(Exception e) {
			DebugLog.w("Resource", e);
        	setLoadingStatus(StatusUnloaded);
        }
		
		setLoadingStatus(StatusLoaded);
		dirtyState();
		
		final ResourceManager c = getCreator();
		if(c != null) {
			c.notifyResourceLoaded(this);
		}
		
		if(!backgroundThread) {
			fireLoadingComplete(false);
			
			DebugLog.d(DTag, "loading resource: "+id+" of type: "+c.name()+" success.");
		}
	}
	
	/**
	 * Reloads the resource, if it is already loaded.
	 * Calls unload() and then load() again, if the resource is already
	 * loaded. If it is not loaded already, then nothing happens.
	 * @throws ResourceException 
	 */
	public synchronized void reload() throws ResourceException {
		if(loadingStatus == StatusLoaded) {
			unload();
			load(false);
		}
		else {
			DebugLog.w("Mate", "reloading resource: "+id+" is already loaded.");
		}
	}
	
    /** Returns true if the Resource is reloadable, false otherwise. */
    public boolean isReloadable() {
    	return !isManual || loader != null;
    }
    
    /** Is this resource manually loaded? */
    public boolean isManuallyLoaded() {
    	return isManual;
    }
	
	/** Unloads the resource; this is not permanent, the resource can be reloaded later if required. */
    public void unload() {
    	int prevStatus = loadingStatus;
    	if(prevStatus != StatusLoaded && prevStatus != StatusPrepared) {
    		return;
    	}
    	
    	synchronized (this) {
			if(prevStatus == StatusPrepared) {
				unprepareImpl();
			}
			else {
				preUnloadImpl();
				unloadImpl();
				postUnloadImpl();
			}
		}
    	
    	setLoadingStatus(StatusUnloaded);
    	
    	size = 0;
    	
    	// Notify only if we actually unloaded stuff, prepared doesn't count
    	final ResourceManager mgr = getCreator();
    	if(prevStatus == StatusLoaded &&  mgr != null) {
    		mgr.notifyResourceUnloaded(this);
    	}
    	
    	fireUnloadingComplete();
    	
    	DebugLog.d(DTag, "unloading resource: "+id+" success.");
    }
    
    /** Calls unload */
    @Override
    public void reset() {
    	unload();
    	unprepareImpl();
    }
    
    /** Retrieves info about the size of the resource in bytes. */
    public int getSize() {
    	return size;
    }
    
    /** 'Touches' the resource to indicate it has been used. 
     * 
     * @throws ResourceException 
     */
    public void touch() throws ResourceException {
    	load(false);
    	
    	final ResourceManager mgr = getCreator();
    	if(mgr != null)
    		mgr.notifyResourceTouched(this);
    }
    
    /** Returns true if the Resource has been prepared, false otherwise. */
    public boolean isPrepared() {
    	return loadingStatus == StatusPrepared;
    }

    /** Returns true if the Resource has been loaded, false otherwise. */
    public boolean isLoaded() {
    	return loadingStatus == StatusLoaded;
    }

    /** Returns whether the resource is currently in the process of background loading. */
    public boolean isLoading() {
    	return loadingStatus == StatusLoading;
    }

    /** Returns the current loading state. */
    public int getLoadingStatus() {
    	return loadingStatus;
    }
    
    /**
     * Returns whether this Resource has been earmarked for background loading.
     * If a resource has been marked
     * for background loading, then it won't load on demand like normal
     * when load() is called. Instead, it will ignore request to load()
     * except if the caller indicates it is the background loader. Any
     * other users of this resource should check isLoaded(), and if that
     * returns false, don't use the resource and come back later.
     */
    public boolean isBackgroundLoaded() {
    	return isBackgroundLoaded;
    }
    
    /**
     * Tells the resource whether it is background loaded or not.
     * <p>
     * Note that calling this only
     * defers the normal on-demand loading behaviour of a resource, it
     * does not actually set up a thread to make sure the resource gets
     * loaded in the background. You should use ResourceBackgroundLoadingQueue
     * to manage the actual loading (which will call this method itself).
     */
    public void isBackgroundLoaded(boolean yes) {
    	isBackgroundLoaded = yes;
    }
    
    /**
     * Escalates the loading of a background loaded resource.
     * <p>
     * If a resource is set to load in the background, but something needs
     * it before it's been loaded, there could be a problem. If the user
     * of this resource really can't wait, they can escalate the loading
     * which basically pulls the loading into the current thread immediately.
     * If the resource is already being loaded but just hasn't quite finished
     * then this method will simply wait until the background load is complete.
     * 
     * @throws ResourceException 
     */
    public void escalateLoading() throws ResourceException {
    	 load(true);
    	 fireLoadingComplete(true);
    }
    
    /** Register a listener on this resource. */
    public void addListener(Listener l) {
    	synchronized (listenerList) {
			listenerList.add(l);
		}
    }
    
    /** Remove a listener on this resource. */
    void removeListener(Listener l) {
    	synchronized (listenerList) {
			listenerList.remove(l);
		}
    }
    
	/** Gets the group which this resource is a member of */
	public String getGroup() {
		return group;
	}
	
	/**
	 * Change the resource group ownership of a Resource.
	 * <p>
	 * This method is generally reserved for internal use, although
	 * if you really know what you're doing you can use it to move
	 * this resource from one group to another.
	 *
	 * @param newGroup Name of the new group
	 */
	public void setGroup(String newGroup) {
		if(group != newGroup) {
			final String oldGrp = group;
			group = newGroup;

			systemRegistry.resourceGroupManager.notifyResourceGroupChanged(oldGrp, this);
		}
	}
	
	/** Gets the manager which created this resource */
	public ResourceManager getCreator() {
		return creator.get();
	}
	
	/**
	 * Get the origin of this resource, e.g. a script id.
	 * <p>
	 * This property will only contain something if the creator of
	 * this resource chose to populate it. Script loaders are advised
	 * to populate it.
	 */
	public int getOrigin() { 
		return origin; 
	}

	/** Notify this resource of it's origin */
	public void notifyOrigin(int origin) { 
		this.origin = origin; 
	}
	
	/**
	 * Returns the number of times this resource has changed state, which
	 * generally means the number of times it has been loaded. Objects that
	 * build derived data based on the resource can check this value against
	 * a copy they kept last time they built this derived data, in order to
	 * know whether it needs rebuilding. This is a nice way of monitoring
	 * changes without having a tightly-bound callback.
	 */
	public int getStateCount() {
		return stateCount;
	}
	
	/**
	 * Manually mark the state of this resource as having been changed.
	 * <p>
	 * You only need to call this from outside if you explicitly want derived
	 * objects to think this object has changed.
	 *
	 * @see stateCount
	 */
	public void dirtyState() {
		stateCount++;
	}
	
	public void setParameters(Param params) {
		this.params = params;
	}
	
	/** Firing of loading complete event */
    public void fireLoadingComplete(boolean wasBackgroundLoaded) {
    	synchronized (listenerList) {
    		for(Listener l : listenerList) {
    			l.loadingComplete(this);
    		}
    	}
    }
    
    /** Firing of preparing complete event */
    public void firePreparingComplete(boolean wasBackgroundLoaded) {
    	synchronized (listenerList) {
    		for(Listener l : listenerList) {
    			l.preparingComplete(this);
    		}
    	}
    }
    
    /** Firing of unloading complete event */
    public void fireUnloadingComplete() {
    	synchronized (listenerList) {
    		for(Listener l : listenerList) {
    			l.unloadingComplete(this);
    		}
    	}
    }
	
    /** identification from the internal resource */
	public final int id;
	
	/** The name of the resource group */
	protected String group;
	/** Is this resource going to be background loaded? */
	protected boolean isBackgroundLoaded;
	/** Is this file manually loaded? */
	protected boolean isManual;
	/** The size of the resource in bytes */
	protected int size;
	/** Origin of this resource (e.g. script resource id) - optional */
	protected int origin;
	/** Optional manual loader; if provided, data is loaded from here instead of a file */
	protected ManualResourceLoader loader;
	/** State count, the number of times this resource has changed state */
	protected int stateCount;
	
	protected final LinkedList<Listener> listenerList;
	
	protected Param params;
		
	protected void setLoadingStatus(int s) {
		synchronized (loadingStatusLock) {
			loadingStatus = s;
			loadingStatusLock.notifyAll();
		}
	}
	
	protected boolean loadingStatusCas(int old, int now) {
		synchronized (loadingStatusLock) {
			if(loadingStatus != old) {
				return false;
			}
			loadingStatus = now;
			loadingStatusLock.notifyAll();
			return true;
		}
	}
	
	// /////////////////////////////////////////////////////////////////
    // Implementations for actual data loading:
	
	/**
	 * Internal hook to perform actions before the load process, but
	 * after the resource has been marked as 'loading'.
	 * This call will occur even when using a ManualResourceLoader
	 * (when loadImpl is not actually called)
	 */
	protected void preLoadImpl() {}

	/**
	 * Internal hook to perform actions after the load process, but
	 * before the resource has been marked as fully loaded.
	 * This call will occur even when using a ManualResourceLoader
	 * (when loadImpl is not actually called)
	 */
	protected void postLoadImpl() {}

	/** Internal hook to perform actions before the unload process. */
	protected void preUnloadImpl() {}

	/** Internal hook to perform actions after the unload process, but
	    before the resource has been marked as fully unloaded. */
	protected void postUnloadImpl() {}
	
	/**
	 * Used for when reloading, we do not want to call unload from primitive calls
	 */
	protected void invalidateImpl() {}

	/** Internal implementation of the meat of the 'prepare' action. */
	protected void prepareImpl() {}

	/**
	 * Internal function for undoing the 'prepare' action.  Called when
	 * the load is completed, and when resources are unloaded when they
	 * are prepared but not yet loaded.
	 */
	protected void unprepareImpl() {}

	/** Internal implementation of the meat of the 'load' action, only called if this
	        resource is not being loaded from a ManualResourceLoader. */
	protected abstract void loadImpl();

	/** Internal implementation of the 'unload' action; called regardless of
	        whether this resource is being loaded from a ManualResourceLoader. */
	protected abstract void unloadImpl();

	/** Calculate the size of a resource; this will only be called after 'load' */
	protected abstract int calculateSize();
	
	
	/** Is the resource currently loaded? */
	private int loadingStatus;
	private final Object loadingStatusLock;
	
	private final WeakReference<ResourceManager> creator; 
	
	private static final String DTag = "Resource"; 
}
