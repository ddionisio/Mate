package com.renegadeware.m8.res;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import android.util.SparseArray;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.res.Resource.ResourceException;
import com.renegadeware.m8.util.DataSection;
import com.renegadeware.m8.util.Util;
import com.renegadeware.m8.util.XmlSection;

public final class ResourceGroupManager extends BaseObject {

	/** Default resource group name */
	public static final String DefaultResourceGroupName = "General";
	
	/** Internal resource group name (don't use outside of m8) */
	public static final String InternalResourceGroupName = "Internal";
	
	/** Special resource group name which causes resource group to be automatically determined based on searching for the resource in all groups. */
	public static final String AutoDetectResourceGroupName = "Autodetect";
		
	/* **********************************************************************************************
	 * Interfaces
	 * **********************************************************************************************/
	
	/**
	 * <p>
	 * This abstract class defines an interface which is called back during
	 * resource group loading to indicate the progress of the load.
	 * <p>
	 * Resource group loading is in 2 phases - creating resources from
	 * declarations (which includes parsing scripts), and loading
	 * resources. Note that you don't necessarily have to have both; it
	 * is quite possible to just parse all the scripts for a group (see
	 * initGroup), but not to load the resource group.
	 */
	public static interface ResourceGroupListener {
		
		/**
		 * This event is fired when a resource group begins parsing scripts.
		 *
		 * @param groupName The name of the group.
		 * @param count The number of scripts which will be parsed.
		 */
		void resourceGroupScriptingStarted(String groupName, int count);
		
		/**
		 * This event is fired when a script is about to be parsed.
		 *
		 * @param id Resource internal id.
		 * @param skipThisScript A boolean passed which is by default set to
		 *     false. If the event sets this to true, the script will be skipped and not
		 *     parsed. Note that in this case the scriptParseEnded event will not be raised
		 *     for this script.
		 *     
		 * @return The skipThisScript value.
		 */
		boolean scriptParseStarted(int id, boolean skipThisScript);

		/**
		 * This event is fired when the script has been fully parsed.
		 *
		 * @param id Resource internal id.
		 * @param skipped true if the script was skipped and not parsed.
		 */
		void scriptParseEnded(int id, boolean skipped);

		/**
		 * This event is fired when a resource group finished parsing scripts.
		 *
		 * @param groupName The name of the group.
		 */
		void resourceGroupScriptingEnded(String groupName);

		/**
		 * This event is fired when a resource group begins preparing.
		 *
		 * @param groupName The name of the group being prepared.
		 * @param count The number of resource which will be prepared.
		 */
		void resourceGroupPrepareStarted(String groupName, int count);

		/**
		 * This event is fired when a declared resource is about to be prepared.
		 *
		 * @param res The resource being prepared.
		 */
		void resourcePrepareStarted(Resource res);

		/** This event is fired when the resource has been prepared. */
		void resourcePrepareEnded();

		/**
		 * This event is fired when a resource group finished preparing.
		 *
		 * @param groupName The name of the group that just finished preparing.
		 */
		void resourceGroupPrepareEnded(String groupName);

		/**
		 * This event is fired  when a resource group begins loading.
		 *
		 * @param groupName The name of the group being loaded.
		 * @param count The number of Resource which will be loaded.
		 */
		void resourceGroupLoadStarted(String groupName, int count);

		/**
		 * This event is fired when a declared resource is about to be loaded.
		 *
		 * @param res The Resource being loaded.
		 */
		void resourceLoadStarted(Resource res);

		/** This event is fired when the resource has been loaded. */
		void resourceLoadEnded();

		/**
		 * This event is fired when a resource group finished loading.
		 *
		 * @param groupName The name of the group that just finished loading.
		 */
		void resourceGroupLoadEnded(String groupName);
	}
	
	/**
	 * This interface allows users to override resource loading behavior.
	 * By overriding this class' methods, you can change how Resource
	 * are loaded and the behavior for resource name collisions.
	 */
	public static interface ResourceLoadingListener {
		/** This event is called when a resource begins loading. */
		//InputStream resourceLoading(int id, String group, Resource res);

		/** This event is called when a Resource stream has been opened, but not processed yet.
		 *
		 * You may alter the stream if you wish or alter the incoming pointer to point at
		 * another stream if you wish.
		 */
		//void resourceStreamOpened(int id, String group, Resource res, InputStream io);

		/** This event is called when a Resource collides with another existing one in a Resource manager. */
		boolean resourceCollision(Resource res, ResourceManager resMgr);
	};
	
	/** Nested struct defining a Resource declaration. */
	public final class ResourceDeclaration {
		public String resourceType;
		public ManualResourceLoader loader;
		public Resource.Param parameters;
		
		public ResourceDeclaration(String resourceType, ManualResourceLoader loader, Resource.Param parameters) {
			this.resourceType = resourceType;
			this.loader = loader;
			this.parameters = parameters;
		}
	}
	
	/** Resource location entry. */
	public final class ResourceLocation {
		String path; // destination
		boolean recursive;  // If this location was added as recursively.
	}
	
	public final class ResourceGroupException extends Exception {
		private static final long serialVersionUID = -5392230889511703093L;
		
		public ResourceGroupException(String msg) {
			super(msg);
		}
	}
	
	public ResourceGroupManager() { 
		super();

		resManagerMap = Util.newHashMap();

		scriptLoadOrderMap = Util.newTreeMap();

		resGroupListeners = Util.newLinkedList();

		resGroupMap = Util.newHashMap();

		createGroup(DefaultResourceGroupName, true);
		createGroup(InternalResourceGroupName, true); 
	}
	
	/* **********************************************************************************************
	 * Methods
	 * **********************************************************************************************/
	
	/**
	 * Create a resource group.
	 * <p>
	 * A resource group allows you to define a set of resources that can
	 * be loaded / unloaded as a unit. For example, it might be all the
	 * resources used for the level of a game. There is always one predefined
	 * resource group called m8.res.group.DefaultResourceGroupName,
	 * which is typically used to hold all resources which do not need to
	 * be unloaded until shutdown. There is another predefined resource
	 * group called m8.res.group.InternalResourceGroupName too,
	 * which should be used internally only, the resources created
	 * in this group aren't supposed to modify, unload or remove by user.
	 * You can create additional ones so that you can control the life of
	 * your resources in whichever way you wish.
	 * There is one other predefined value,
	 * m8.res.group.AutoDetectResourceGroupName; using this
	 * causes the group name to be derived at load time by searching for
	 * the resource in the resource locations of each group in turn.
	 * <p>
	 * Once you have defined a resource group, resources which will be loaded
	 * as part of it are defined in one of 3 ways:
	 * <ol>
	 * <li>Manually through declareResource(); this is useful for scripted
	 *     declarations since it is entirely generalised, and does not
	 *     create Resource instances right away</li>
	 * <li>Through the use of scripts; some ResourceManager subtypes have
	 *     script formats (e.g. .material, .overlay) which can be used
	 *     to declare resources</li>
	 * <li>By calling ResourceManager::create to create a resource manually.
	 * This resource will go on the list for it's group and will be loaded
	 * and unloaded with that group</li>
	 * </ol>
	 * You must remember to call initGroup if you intend to use
	 * the first 2 types.
	 *
	 * @param name The name to give the resource group.
	 * @param inGlobalPool If true (default), the resource will be loaded even if a different
	 *                   group was requested in the load method as a parameter.
	 */
	public void createGroup(String name, boolean inGlobalPool) {
		// Group mustn't exist
		if(_getResourceGroup(name) != null) {
			DebugLog.e(DTag, "Error creating resource group: " + name +", already exists.");
		}
		
		synchronized(this) {
			DebugLog.i(DTag, "Creating resource group: " + name);
			
			resGroupMap.put(name, new ResourceGroup(name, inGlobalPool));
		}
	}
			
	/**
	 * Initialises a resource group.

	 * After creating a resource group and declaring some resources using declareResource(), but
	 * before you need to use the resources in the group, you
	 * should call this method to initialise the group. By calling this,
	 * you are triggering the following processes:
	 * <ol>
	 * <li>Scripts for all resource types which support scripting are
	 *     parsed from the declared resources, and resources within them are
	 *     created (but not loaded yet).</li>
	 * <li>Creates all the resources which have just pre-declared using
	 *     declareResource (again, these are not loaded yet)</li>
	 * </ol>
	 * So what this essentially does is create a bunch of unloaded Resource entries
	 * in the respective ResourceManagers based on scripts, and resources
	 * you've pre-declared. That means that code looking for these resources
	 * will find them, but they won't be taking up much memory yet, until
	 * they are either used, or they are loaded in bulk using loadResourceGroup.
	 * Loading the resource group in bulk is entirely optional, but has the
	 * advantage of coming with progress reporting as resources are loaded.
	 *
	 * Failure to call this method means that loadResourceGroup will do
	 * nothing, and any resources you define in scripts will not be found.
	 * Similarly, once you have called this method you won't be able to
	 * pick up any new scripts or pre-declared resources, unless you
	 * call clearResourceGroup, set up declared resources, and call this
	 * method again.
	 *
	 * @param name The name of the resource group to initialize.
	 *
	 * @throws ResourceGroupException
	 */
	public synchronized void initGroup(String name) throws ResourceGroupException {
		ResourceGroup grp = _getResourceGroup(name);
		if(grp == null) {
			throw new ResourceGroupException("initGroup: Cannot find group: " + name);
		}
		
		synchronized(grp) {
			// Setup any scripts within the group's declared resources
			_parseResourceGroupScripts(grp);
			
			currentGroup = grp;
			
			// Create the resources that are declared
			_createDeclaredResource(grp);
			
			grp.status = ResourceGroup.StatusInitialized;
			
			currentGroup = null;
		}
	}
	
	/**
	 * Initializes all the resource groups which are yet to initialized.
	 *
	 * @see initGroup
	 */
	public synchronized void initAllGroups() {
		Collection<ResourceGroup> c = resGroupMap.values();
		
		for(ResourceGroup grp : c) {
			synchronized(grp) {
				if(grp.status == ResourceGroup.StatusUninitialized) {
					grp.status = ResourceGroup.StatusInitializing;
					
					// Setup any scripts within the group's declared resources
					_parseResourceGroupScripts(grp);
					
					currentGroup = grp;
					
					// Create the resources that are declared
					_createDeclaredResource(grp);
					
					grp.status = ResourceGroup.StatusInitialized;
					
					currentGroup = null;
				}
			}
		}
	}
	
	/**
	 * Prepares a resource group.
	 * <p>
	 * Prepares any created resources which are part of the named group.
	 * Note that resources must have already been created by calling
	 * ResourceManager::create, or declared using declareResource() or
	 * in a script (such as .material and .overlay). The latter requires
	 * that initialiseResourceGroup has been called.
	 * <p>
	 * When this method is called, this class will callback any ResourceGroupListeners
	 * which have been registered to update them on progress.
	 *
	 * @param name The name of the resource group to prepare.
	 *
	 * @throws ResourceGroupException
	 * @throws ResourceException 
	 */
	public synchronized void prepareGroup(String name) throws ResourceGroupException, ResourceException {
		DebugLog.i(DTag, "Preparing resource group: " + name);
		
		ResourceGroup grp = _getResourceGroup(name);
		if(grp == null) {
			throw new ResourceGroupException("prepareGroup: Cannot find group: " + name);
		}
		
		synchronized(grp) {
			currentGroup = grp;
			
			// Count the resources for starting event
			int resCount = 0;
			
			Collection<LinkedList<Resource>> c = grp.loadResOrderMap.values();
			
			for(LinkedList<Resource> l : c) {
				resCount += l.size();
			}
			
			_fireResourceGroupPrepareStarted(name, resCount);
			
			// Prepare each resource
			for(LinkedList<Resource> resList : c) {
				int n = 0;
				
				Iterator<Resource> lIt = resList.iterator();
				
				while(lIt.hasNext()) {
					Resource res = lIt.next();
					
					_fireResourcePrepareStarted(res);
					
					// note: possibility of cascading additional resources, these resources should be processed within
					res.prepare(false);
					
					_fireResourcePrepareEnded();
					
					++n;
					
					// Did the resource change group? if so, our iterator will have
					// been invalidated
					if(res.getGroup() != name) {
						lIt = resList.iterator();
						Util.advanceIterator(lIt, n);
					}
				}
			}
			
			_fireResourceGroupPrepareEnded(name);
			
			currentGroup = null;
			
			DebugLog.i(DTag, "Finished preparing resource group: " + name);
		}
	}
	
	/**
	 * Loads a resource group.
	 * <p>
	 * Loads any created resources which are part of the named group.
	 * Note that resources must have already been created by calling
	 * ResourceManager::create, or declared using declareResource() or
	 * in a script (such as .material and .overlay). The latter requires
	 * that initialiseResourceGroup has been called.
	 * <p>
	 * When this method is called, this class will callback any ResourceGroupListeners
	 * which have been registered to update them on progress.
	 *
	 * @param name The name of the resource group to load.
	 *
	 * @throws ResourceGroupException
	 * @throws ResourceException 
	 */
	public synchronized void loadGroup(String name) throws ResourceGroupException, ResourceException {
		DebugLog.i(DTag, "Loading resource group: " + name);
		
		ResourceGroup grp = _getResourceGroup(name);
		if(grp == null) {
			throw new ResourceGroupException("loadGroup: Cannot find group: " + name);
		}
		
		synchronized(grp) {
			currentGroup = grp;
			
			grp.status = ResourceGroup.StatusLoading;
			
			// Count the resources for starting event
			int resCount = 0;
			
			Collection<LinkedList<Resource>> c = grp.loadResOrderMap.values();
			
			for(LinkedList<Resource> l : c) {
				resCount += l.size();
			}
			
			_fireResourceGroupLoadStarted(name, resCount);
			
			resCount = 0;
			
			// load each resource
			for(LinkedList<Resource> resList : c) {
				int n = 0;
				
				Iterator<Resource> lIt = resList.iterator();
				while(lIt.hasNext()) {
					Resource res = lIt.next();
					
					_fireResourceLoadStarted(res);
					
					// note: possibility of cascading additional resources, these resources should be processed within
					res.load(false);
					
					_fireResourceLoadEnded();
					
					++n;
					
					// Did the resource change group? if so, our iterator will have
					// been invalidated
					if(res.getGroup() != name) {
						lIt = resList.iterator();
						Util.advanceIterator(lIt, n);
					}
					
					resCount++;
				}
			}
			
			_fireResourceGroupLoadEnded(name);
			
			grp.status = ResourceGroup.StatusLoaded;
			
			currentGroup = null;
			
			DebugLog.i(DTag, "Finished loading resource group: " + name + " number of resources loaded: "+resCount);
		}
	}
	
	/**
	 * Unloads a resource group.
	 * <p>
	 * This method unloads all the resources that have been declared as
	 * being part of the named resource group. Note that these resources
	 * will still exist in their respective ResourceManager classes, but
	 * will be in an unloaded state. If you want to remove them entirely,
	 * you should use clearGroup or destroyGroup.
	 *
	 * @param name The name to of the resource group to unload.
	 * @param reloadableOnly (default: true) If set to true, only unload the resource that is
	 *        reloadable. Because some resources isn't reloadable, they will be
	 *        unloaded but can't load them later. Thus, you might not want to them
	 *        unloaded. Or, you might unload all of them, and then populate them
	 *        manually later.
	 *
	 * @throws ResourceGroupException
	 *
	 * @see Resource.isReloadable
	 */
	@SuppressWarnings("unchecked")
	public synchronized void unloadGroup(String name, boolean reloadableOnly) throws ResourceGroupException {
		DebugLog.i(DTag, "Unloading resource group: " + name);
		
		ResourceGroup grp = _getResourceGroup(name);
		if(grp == null) {
			throw new ResourceGroupException("unloadGroup: Cannot find group: " + name);
		}
		
		synchronized(grp) {
			currentGroup = grp;
			
			// Go through in reverse order and unleash unloading.
			
			// yuck!
			final Object c[] = grp.loadResOrderMap.values().toArray();
			for(int i = c.length-1; i >= 0; --i) {
				final LinkedList<Resource> reses = (LinkedList<Resource>)c[i];
				for(Resource res : reses) {
					if(!reloadableOnly || res.isReloadable()) {
						res.unload();
					}
				}
			}
			
			grp.status = ResourceGroup.StatusInitialized;
			
			currentGroup = null;
			
			DebugLog.i(DTag, "Finished unloading resource group: " + name);
		}
	}
	
	/**
	 * Clears a resource group.
	 * <p>
	 * Make sure to unload the resources prior to calling this!
	 * <p>
	 * This method removes all resources from their ResourceManagers, and then
	 * clears all the members from the list. That means after calling this
	 * method, there are no resources declared as part of the named group
	 * any more.
	 *
	 * @param The name to of the resource group to clear.
	 *
	 * @throws ResourceGroupException
	 */
	public synchronized void clearGroup(String name, boolean clearDeclarations) throws ResourceGroupException {
		DebugLog.i(DTag, "Clearing resource group: " + name);
		
		ResourceGroup grp = _getResourceGroup(name);
		if(grp == null) {
			throw new ResourceGroupException("clearGroup: Cannot find group: " + name);
		}
		
		currentGroup = grp;
		
		_dropGroupContents(grp);
		
		if(clearDeclarations) {
			grp.resDeclarations.clear();
		}
		
		// no longer initialized
		grp.status = ResourceGroup.StatusUninitialized;
		
		currentGroup = null;
		
		DebugLog.i(DTag, "Finished clearing resource group: " + name);
	}
	
	/**
	 * Destroys a resource group, clearing it first, destroying the resources
	 * which are part of it, and then removing it from
	 * the list of resource groups.
	 *
	 * @param name The name of the resource group to destroy.
	 * 
	 * @throws ResourceGroupException
	 */
	public synchronized void destroyGroup(String name) throws ResourceGroupException {
		DebugLog.i(DTag, "Destroying resource group: " + name);
		
		ResourceGroup grp = _getResourceGroup(name);
		if(grp == null) {
			throw new ResourceGroupException("destroyGroup: Cannot find group: " + name);
		}
		
		unloadGroup(name, false);
		
		currentGroup = grp;
		
		_dropGroupContents(grp);
		grp.shutdown();
		
		resGroupMap.remove(name);
		
		currentGroup = null;
	}
	
	/**
	 * Checks the status of a resource group.
	 * <p>
	 * Looks at the state of a resource group.
	 * If initGroup has been called for the resource
	 * group return true, otherwise return false.
	 *
	 * @param name The name to of the resource group to access.
	 *
	 * @throws ResourceGroupException
	 *
	 * @return true if given resource group is initialized.
	 */
	public synchronized boolean isGroupInit(String name) throws ResourceGroupException {
		ResourceGroup grp = _getResourceGroup(name);
		if(grp == null) {
			throw new ResourceGroupException("isGroupInit: Cannot find group: " + name);
		}
		
		return grp.status != ResourceGroup.StatusUninitialized && grp.status != ResourceGroup.StatusInitializing;
	}
	
	/**
	 * Checks the status of a resource group.
	 * <p>
	 * Looks at the state of a resource group.
	 * If loadResourceGroup has been called for the resource
	 * group return true, otherwise return false.
	 *
	 * @param name The name to of the resource group to access.
	 *
	 * @throws ResourceGroupException
	 *
	 * @return true if given resource group is loaded.
	 */
	public boolean isGroupLoaded(String name) throws ResourceGroupException {
		ResourceGroup grp = _getResourceGroup(name);
		if(grp == null) {
			throw new ResourceGroupException("isGroupLoaded: Cannot find group: " + name);
		}
		
		return grp.status == ResourceGroup.StatusLoaded;
	}
	
	/**
	 * Verify if a resource group exists.
	 *
	 * @param name The name of the resource group to look for.
	 *
	 * @return true if given resource group exists.
	 */
	public boolean groupExists(String name) {
		return _getResourceGroup(name) != null;
	}
	
	//TODO: "addLocation" for external resources
	//TODO: "removeLocation" for external resources
	//TODO: "locationExists" for external resources
	//TODO: "declareResource" for external resources
	//TODO: "undeclareResource" for external resources
	//TODO: "openResource" for external resources
	//TODO: "openResources" for external resources
	//TODO: "resourceNames" for external resources
	//TODO: FileInfoListPtr listResourceFileInfo(const String& resGroup, bool dirs = false);
	//TODO: findResourceNames
	//TODO: resourceExists
	//TODO: resourceModifiedTime
	//TODO: resourceLocations
	//TODO: findResourceLocations
	//TODO: createResource
	//TODO: deleteResource
	//TODO: deleteMatchingResources
	//TODO: findScriptLoader
	//TODO: getResourceLocationList
	
	//declareResource(string name) <- use getIdentifier(name, deftype (e.g. "drawable"), getPackageName()) to get id
	
	/**
	 * Declares a resource to be a part of a resource group, allowing you
	 * to load and unload it as part of the group.
	 * <p>
	 * By declaring resources before you attempt to use them, you can
	 * more easily control the loading and unloading of those resources
	 * by their group. Declaring them also allows them to be enumerated,
	 * which means events can be raised to indicate the loading progress
	 * (see ResourceGroupListener). Note that another way of declaring
	 * resources is to use a script specific to the resource type, if
	 * available (e.g. .material).
	 * <p>
	 * Declared resources are not created as Resource instances (and thus
	 * are not available through their ResourceManager) until initGroup
	 * is called, at which point all declared resources will become created
	 * (but unloaded) Resource instances, along with any resources declared
	 * in scripts in resource locations associated with the group.
	 *
	 * @param id From internal resource table.
	 * @param resTypeThe type of the resource.
	 * @param resGroup The name of the group to which it will belong. (use DefaultResourceGroupName as default)
	 * @param loadParams (datasection) An array of name / value pairs which supply custom
	 *        parameters to the resource which will be required before it can
	 *        be loaded. These are specific to the resource type.
	 *
	 * @throws ResourceGroupException
	 */
	public void declareResource(int id, String resType, 
			String resGroup, Resource.Param loadParams) throws ResourceGroupException {
		declareResource(id, resType, resGroup, null, loadParams);
	}
	
	public void declareResource(int id, String resType, 
			String resGroup, ManualResourceLoader loader, Resource.Param loadParams) throws ResourceGroupException {
		
		ResourceGroup grp = _getResourceGroup(resGroup);
		if(grp == null) {
			throw new ResourceGroupException("declareResource: Cannot find group: " + resGroup);
		}
		
		synchronized(grp) {
			grp.resDeclarations.append(
					id,
					new ResourceDeclaration( 
							resType, 
							loader, 
							loadParams));
			
			DebugLog.d(DTag, "Declared: "+id+" of type: "+resType);
		}
	}
	
	public void declareResource(String resName, String resType, 
			String resGroup, Resource.Param loadParams) throws ResourceGroupException {
		declareResource(_getId(resName, resType), resType, resGroup, null, loadParams);
	}
	
	public void declareResource(String resName, String resType, 
			String resGroup, ManualResourceLoader loader, Resource.Param loadParams) throws ResourceGroupException {		
		declareResource(_getId(resName, resType), resType, resGroup, null, loadParams);
	}
	
	/**
	 * Undeclare a resource.
	 * <p>
	 * Note that this will not cause it to be unloaded
	 * if it is already loaded, nor will it destroy a resource which has
	 * already been created if initGroup has been called already.
	 * Only unloadGroup / clearGroup / destroyGroup
	 * will do that.
	 *
	 * @param id From internal resource table.
	 * @param resGroup The name of the group this resource was declared in.
	 *
	 * @throws ResourceGroupException
	 */
	public void undeclareResource(int id, String resGroup) throws ResourceGroupException {
		ResourceGroup grp = _getResourceGroup(resGroup);
		if(grp == null) {
			throw new ResourceGroupException("undeclareResource: Cannot find group: " + resGroup);
		}
		
		synchronized(grp) {
			final SparseArray<ResourceDeclaration> decs = grp.resDeclarations;
			
			for(int i = 0; i < decs.size(); ++i) {
				if(decs.keyAt(i) == id) {
					decs.delete(id);
				}
			}
		}
	}
	
	public void undeclareResource(String resName, String resType, String resGroup) throws ResourceGroupException {
		undeclareResource(_getId(resName, resType), resGroup);
	}
		
	/**
	 * Find out if the id is declared in a group.
	 *
	 * @param resGroup The name of the resource group.
	 * @param id From internal resource table.
	 *
	 * @throws ResourceGroupException
	 *
	 * @return true if given resource in group exists.
	 */
	public synchronized boolean resourceIsDeclared(String resGroup, int id) throws ResourceGroupException {
		ResourceGroup grp = _getResourceGroup(resGroup);
		if(grp == null) {
			throw new ResourceGroupException("resourceIsDeclared: Cannot find group: " + resGroup);
		}
		
		return _resourceIsDeclared(grp, id);
	}
	
	public synchronized boolean resourceIsDeclared(String resGroup, String resName, String resType) throws ResourceGroupException {
		return resourceIsDeclared(resGroup, _getId(resName, resType));
	}
	
	/**
	 * Find out if the resource is declared in any group.
	 *
	 * @param id From internal resource table.
	 *
	 * Returns: true if given resource in any group exists.
	 */
	public synchronized boolean resourceIsDeclared(int id) {
		return _findGroupContainingResourceDeclarationImpl(id) != null;
	}
	
	public synchronized boolean resourceIsDeclared(String resName, String resType) {
		return resourceIsDeclared(_getId(resName, resType));
	}
	
	/**
	 * Find the group in which a resource is declared.
	 *
	 * @param id From internal resource table.
	 *
	 * @throws ResourceGroupException
	 *
	 * @return The group name this resource belongs to.
	 */
	public String resourceIsDeclaredInGroup(int id) throws ResourceGroupException {
		ResourceGroup grp = _findGroupContainingResourceDeclarationImpl(id);
		if(grp == null) {
			throw new ResourceGroupException("resourceDeclaredInGroup: Cannot find group for: " + id);
		}
		
		return grp.name;
	}
	
	public String resourceIsDeclaredInGroup(String resName, String resType) throws ResourceGroupException {
		return resourceIsDeclaredInGroup(_getId(resName, resType));
	}
	
	/**
	 * Adds a ResourceGroupListener which will be called back during
	 * resource loading events.
	 */
	public synchronized void addGroupListener(ResourceGroupListener l) {
		resGroupListeners.add(l);
	}
	
	/** Removes a ResourceGroupListener */
	public synchronized void removeGroupListener(ResourceGroupListener l) {
		resGroupListeners.remove(l);
	}
	
	/**
	 * Is group created with global pool in mind?
	 *
	 * @param name The name of the group.
	 *
	 * @throws ResourceGroupException
	 *
	 * @return true if given group is in global pool.
	 */
	public synchronized boolean isGroupInGlobalPool(String name) throws ResourceGroupException {
		ResourceGroup grp = _getResourceGroup(name);
		if(grp == null) {
			throw new ResourceGroupException("resourceIsDeclared: Cannot find group: " + name);
		}
		
		return grp.inGlobalPool;
	}
			
	/** Shutdown all ResourceManagers and destroy all groups, performed as part of clean-up. */
	@Override
	public final synchronized void reset() {
		// go through each resource manager and remove (unload) all their resources
		final Collection<ResourceManager> c = resManagerMap.values();
		for(ResourceManager rm : c) {
			rm.reset();
		}
		
		resManagerMap.clear();
		
		scriptLoadOrderMap.clear();
		
		// go through each group and uninitialize them
		final Collection<ResourceGroup> grps = resGroupMap.values();
		for(ResourceGroup grp : grps) {
			grp.shutdown();
		}
		
		resGroupMap.clear();
		
		resGroupListeners.clear();
		
		loadingListener = null;
		
		currentGroup = null;
	}
	
	/**
	 * Method for registering a ResourceManager (which should be a singleton).
	 * Creators of plugins can register new ResourceManagers
	 * this way if they wish.
	 * <p>
	 * ResourceManagers that wish to parse scripts must also call
	 * registerScriptLoader.
	 *
	 * @param rm The ResourceManager instance.
	 */
	public synchronized void registerResourceManager(ResourceManager rm) {
		DebugLog.i(DTag, "Registering Resource Manager for type: " + rm.name());
		
		resManagerMap.put(rm.name(), rm);
	}
	
	/**
	 * Method for unregistering a ResourceManager.
	 * <p>
	 * ResourceManagers that wish to parse scripts must also call
	 * unregisterScriptLoader.
	 *
	 * @param resType String identifying the resource type.
	 */
	public void unregisterResourceManager(String resType) {
		DebugLog.i(DTag, "Unregistering Resource Manager for type: " + resType);
		
		ResourceManager rm = resManagerMap.remove(resType);
		if(rm != null) {
			rm.removeAll();
		}
	}
	
	/**
	 * Method for registering a ScriptLoader.
	 * <p>
	 * ScriptLoaders parse scripts when resource groups are initialised.
	 *
	 * @param sl The ScriptLoader instance.
	 */
	public synchronized void registerScriptLoader(ScriptLoader sl) {
		ArrayList<ScriptLoader> scripts = scriptLoadOrderMap.get(sl.loadingOrder());
		if(scripts == null) {
			scripts = Util.newArrayList();
			scriptLoadOrderMap.put(sl.loadingOrder(), scripts); 
		}
		
		scripts.add(sl);
	}
	
	/**
	 * Method for unregistering an IScriptLoader.
	 *
	 * @param sl The IScriptLoader instance.
	 */
	public synchronized void unregisterScriptLoader(ScriptLoader sl) {
		ArrayList<ScriptLoader> scripts = scriptLoadOrderMap.get(sl.loadingOrder());
		if(scripts != null) {
			int ind = scripts.indexOf(sl);
			if(ind >= 0) {
				scripts.remove(ind);
			}
		}
	}
	
	/**
	 * Method for getting a registered ResourceManager.
	 *
	 * @param resType String identifying the resource type.
	 *
	 * @return The ResourceManager instance, null if not found.
	 */
	public synchronized ResourceManager getResourceManager(String resType) {
		return resManagerMap.get(resType);
	}
	
	/**
	* Get a list of the currently defined resource groups.
	* This method intentionally returns a copy rather than a reference in
	* order to avoid any contention issues in multithreaded applications.
	*
	* @return A copy of list of currently defined groups.
	*/
	public synchronized String[] groupNames() {
		return (String[])resGroupMap.keySet().toArray();
	}
	
	/**
	 * Get the sparse array of resource declarations for the specified group name.
	 * This method will directly return the group's declarations, so you better only
	 * use this for read-only.
	 *
	 * @param resGroup The name of the group
	 *
	 * @throws ResourceGroupException
	 *
	 * @return group's resource declarations.
	 */
	public synchronized SparseArray<ResourceDeclaration> getResourceDeclarationList(String resGroup) throws ResourceGroupException {
		ResourceGroup grp = _getResourceGroup(resGroup);
		if(grp == null) {
			throw new ResourceGroupException("getResourceDeclarationList: Cannot find group: " + resGroup);
		}
		
		synchronized(grp) {
			return grp.resDeclarations;
		}
	}
	
	/** Returns the current loading listener */
	public ResourceLoadingListener getLoadingListener() {
		return loadingListener;
	}
	
	/** Sets a new loading listener */
	public void setLoadingListener(ResourceLoadingListener l) {
		loadingListener = l;
	}
	
	public HashMap<String, ResourceManager> managers() {
		return resManagerMap;
	}
	
	public final void createGroupsFromXml(int xmlId, boolean initialize) {
		DataSection ds = XmlSection.createFromResourceId(systemRegistry.contextParameters.context, xmlId);
		
		createGroupsFromDataSection(ds, initialize);
	}
	
	public final void createGroupsFromDataSection(DataSection ds, boolean initialize) {
		for(DataSection group : ds) {
			//get the name and create the group, this has to exist
			String name = group.getAttributeAsString("name", 0);
			if(name != null && name.length() > 0) {
				createGroup(name, true);
				
				//go through its children to declare the resources
				declareResourcesFromDataSection(name, group);
				
				//optional initialize
				if(initialize) {
					try {
						initGroup(name);
					} catch (ResourceGroupException e) {
						DebugLog.w("ResourceGroupManager", "createGroupsFromXml: "+e.toString());
					}
				}
			}
		}
	}
	
	public final void declareResourcesFromDataSection(String groupName, DataSection ds) {
		ResourceGroup grp = _getResourceGroup(groupName);
		if(grp != null) {
			for(DataSection res : ds) {
				String resType = res.sectionName();
				String id = res.getAttributeAsString("id", 0);
				String paramClass = res.getAttributeAsString("param", 0);
								
				//res type needs to be valid
				ResourceManager resMgr = getResourceManager(resType);
				if(resMgr == null) {
					DebugLog.w("ResourceGroupManager", "declareResourcesFromDataSection: Unable to find resource manager for "+resType);
					continue;
				}
				
				//optional param, needed for special types of resources
				Resource.Param params = null;
				if(paramClass != null && paramClass.length() > 0) {
					try {
						params = Util.instantiateClass(paramClass);
						
						//get the variables of the param
						res.readObjectFields(params);
						
					} catch (Exception e) {
						DebugLog.w("ResourceGroupManager", "declareResourcesFromDataSection: "+e.toString());
					}
				}
				
				//declare the resource
				try {
					declareResource(id, resType, groupName, params);
				} catch (ResourceGroupException e) {
					DebugLog.w("ResourceGroupManager", "declareResourcesFromDataSection: "+e.toString());
				}
			}
		}
	}
		
	//////////////////////////////////////////
	//Use for managers only
	
	/** This is only to be used by resource managers */
	public void notifyResourceCreated(Resource res) {
		if(currentGroup != null && res.getGroup() == currentGroup.name) {
			_addCreatedResource(res, currentGroup);
		}
		else {
			final ResourceGroup grp = _getResourceGroup(res.getGroup());
			if(grp != null) {
				_addCreatedResource(res, grp);
			}
		}
	}
	
	/** This is only to be used by resource managers */
	public void notifyResourceRemoved(Resource res) {
		if(currentGroup != null) {
	        // Assuming we're batch unloading, no need to do anything.
	    }
	    else {
	    	final ResourceGroup grp = _getResourceGroup(res.getGroup());
			if(grp != null) {
				synchronized (grp) {
					//TreeMap<Integer, LinkedList<Resource> > loadResOrderMap
					LinkedList<Resource> reses = grp.loadResOrderMap.get(res.getCreator().loadingOrder());
					if(reses != null) {
						reses.remove(res);
					}
				}
			}
	    }
	}
	
	/** This is only to be used by resource managers */
	public void notifyResourceGroupChanged(String oldGroup, Resource res) {
		// Find the old entry
		final ResourceGroup grp = _getResourceGroup(oldGroup);
		if(grp != null) {
			synchronized (grp) {
				grp.resDeclarations.remove(res.id);
				grp.loadResOrderMap.get(res.getCreator().loadingOrder()).remove(res);
			}
		}
		
		// Add to new group
		_addCreatedResource(res, _getResourceGroup(res.getGroup()));
	}
	
	/** This is only to be used by resource managers */
	public synchronized void notifyAllResourcesRemoved(ResourceManager rm) {
		// Go through each group
		final Collection<ResourceGroup> grps = resGroupMap.values();
		for(ResourceGroup grp : grps) {
			synchronized (grp) {
				// Then through the loading order
				final Collection<LinkedList<Resource>> rls = grp.loadResOrderMap.values();
				for(LinkedList<Resource> rl : rls) {
					// Find any resource within the list that matches the given rm,
                    // remove the ones that match rm
					final Iterator<Resource> it = rl.iterator();
					while(it.hasNext()) {
						final Resource r = it.next();
						if(r.getCreator() == rm) {
							it.remove();
						}
					}					
					//...whew
				}
			}
		}
	}
	
	/* **********************************************************************************************
	 * Private Methods
	 * **********************************************************************************************/
	
	private boolean _resourceIsDeclared(ResourceGroup grp, int id) {
		synchronized(grp) {
			return grp.resDeclarations.get(id) != null;
		}
	}
	
	/** 
	 * Parses all the available scripts found in the declared resources
	 * for the given group, for all ResManagers.
	 * <p>
	 * Called as part of initGroup
	 */
	private void _parseResourceGroupScripts(ResourceGroup grp) {
		DebugLog.i(DTag, "Parsing scripts for resource group: " + grp.name);
		
		int scriptCount = 0;
		
		//LinkedList<Pair<ScriptLoader, LinkedList<Integer>>> scriptIdsPairs;
		// Emit script starting
	    _fireResourceGroupScriptingStarted(grp.name, scriptCount);
		
		// Go through each script loaders in order and gather up files associated with the script
	    // Count up the number of files associated with the script.
		final Collection<ArrayList<ScriptLoader>> csls = scriptLoadOrderMap.values();
		for(ArrayList<ScriptLoader> sls : csls) {
			for(ScriptLoader sl : sls) {
				final SparseArray<ResourceDeclaration> decs = grp.resDeclarations;
				for(int i = 0; i < decs.size(); ++i) {
					final ResourceDeclaration dec = decs.valueAt(i);
					
					if(dec.resourceType.compareTo(sl.name()) == 0) {
						int id = decs.keyAt(i);
						
						boolean skipScript = false;
						
						skipScript = _fireScriptStarted(id, skipScript);
						
						if(skipScript) {
							DebugLog.i(DTag, "Skipping script: " + id);
						}
						else {
							DebugLog.i(DTag, "Parsing script: " + id);
							
							/*if(loadingListener != null) {
								loadingListener.resourceStreamOpened();
							}*/
							
							sl.parseScript(id, grp.name);
						}
						
						_fireScriptEnded(id, skipScript);
					}
				}
			}
		}
		
		_fireResourceGroupScriptingEnded(grp.name);
		
		DebugLog.i(DTag, "Finished parsing scripts for resource group: " + grp.name);
	}
	
	/** 
	 * Create all the pre-declared reses.
	 * <p>
	 * Called as part of initialiseResGroup
	 */
	private void _createDeclaredResource(ResourceGroup grp) {
		final SparseArray<ResourceDeclaration> decs = grp.resDeclarations;
		
		for(int i = 0; i < decs.size(); ++i) {
			final ResourceDeclaration dec = decs.valueAt(i);
			
			// Get the resource manager for this type
			final ResourceManager mgr = getResourceManager(dec.resourceType);
			
			// Create the resource
			Resource res = mgr.create(decs.keyAt(i), grp.name, dec.loader != null, dec.loader, dec.parameters);
			
			final int order = mgr.loadingOrder();
			
			// Add the resource to the loading list
			LinkedList<Resource> l = grp.loadResOrderMap.get(order);
			if(l == null) {
				grp.loadResOrderMap.put(order, l = new LinkedList<Resource>());
			}
			
			l.add(res);
		}
	}
	
	/** Adds a created Resource to a group. */
	private void _addCreatedResource(Resource res, ResourceGroup grp) {
		synchronized (grp) {
			final int order = res.getCreator().loadingOrder();
			
			// Add the resource to the loading list
			LinkedList<Resource> l = grp.loadResOrderMap.get(order);
			if(l == null) {
				grp.loadResOrderMap.put(order, l = new LinkedList<Resource>());
			}
			
			l.add(res);
		}
	}
	
	/** Get Resource group */
	private ResourceGroup _getResourceGroup(String name) {
		return resGroupMap.get(name);
	}
	
	/** Drops contents of a group, leave group there, notify ResManagers. */
	private void _dropGroupContents(ResourceGroup grp) {
		synchronized (grp) {
			boolean groupSet = false;
			
			// Set the current group if it hasn't yet
			if(currentGroup == null) {
				currentGroup = grp;
				groupSet = true;
			}
			
			// Delete all the load list entries
			final Collection<LinkedList<Resource>> c = grp.loadResOrderMap.values();
			for(LinkedList<Resource> l : c) {
				for(Resource res : l) {
					res.getCreator().remove(res);
				}
			}
			
			grp.loadResOrderMap.clear();
			
			if(groupSet) {
				currentGroup = null;
			}
		}
	}
	
	/** Internal find method for auto groups */
	private synchronized ResourceGroup _findGroupContainingResourceDeclarationImpl(int id) {
		final Collection<ResourceGroup> c = resGroupMap.values();
		for(ResourceGroup grp : c) {
			if(_resourceIsDeclared(grp, id)) {
				return grp;
			}
		}
		return null;
	}
	
	private synchronized void _fireResourceGroupScriptingStarted(String groupName, int scriptCount) {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourceGroupScriptingStarted(groupName, scriptCount);
		}
	}
	
	/** returns the new skipScript value */
	private synchronized boolean _fireScriptStarted(int id, boolean skipScript) {
		for(ResourceGroupListener l : resGroupListeners) {
			boolean _skipScript = l.scriptParseStarted(id, false);
			if(_skipScript) {
				skipScript = _skipScript;
			}
		}
		
		return skipScript;
	}
	
	private synchronized void _fireScriptEnded(int id, boolean skipped) {
		for(ResourceGroupListener l : resGroupListeners) {
			l.scriptParseEnded(id, skipped);
		}
	}
	
	private synchronized void _fireResourceGroupScriptingEnded(String groupName) {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourceGroupScriptingEnded(groupName);
		}
	}
	
	private synchronized void _fireResourceGroupLoadStarted(String groupName, int resCount) {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourceGroupLoadStarted(groupName, resCount);
		}
	}
	
	private synchronized void _fireResourceLoadStarted(Resource res) {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourceLoadStarted(res);
		}
	}
	
	private synchronized void _fireResourceLoadEnded() {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourceLoadEnded();
		}
	}
	
	private synchronized void _fireResourceGroupLoadEnded(String groupName) {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourceGroupLoadEnded(groupName);
		}
	}
	
	private synchronized void _fireResourceGroupPrepareStarted(String groupName, int resCount) {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourceGroupPrepareStarted(groupName, resCount);
		}
	}
	
	private synchronized void _fireResourcePrepareStarted(Resource res) {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourcePrepareStarted(res);
		}
	}
	
	private synchronized void _fireResourcePrepareEnded() {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourcePrepareEnded();
		}
	}
	
	private synchronized void _fireResourceGroupPrepareEnded(String groupName) {
		for(ResourceGroupListener l : resGroupListeners) {
			l.resourceGroupPrepareEnded(groupName);
		}
	}
	
	private final int _getId(String name, String resType) {
		ResourceManager mgr = resManagerMap.get(resType);
		
		assert mgr != null;
		
		return mgr.getResourceIdByName(name);
	}
	
	/* **********************************************************************************************
	 * Private Properties
	 * **********************************************************************************************/
	
	private static final String DTag = "ResourceGroupManager"; 
	
	// resourceGroupElement: resource declaration list, resource load order
	private final class ResourceGroup {
		
		public static final int StatusUninitialized = 0;
		public static final int StatusInitializing = 1;
		public static final int StatusInitialized = 2;
		public static final int StatusLoading = 3;
		public static final int StatusLoaded = 4;
		
		public final String name;
		
		public int status;
		
		public final SparseArray<ResourceDeclaration> resDeclarations;
		
		public final TreeMap<Integer, LinkedList<Resource> > loadResOrderMap;
		
		public final boolean inGlobalPool;
		
		public ResourceGroup(String name, boolean inGlobalPool) {
			this.name = name;
			this.inGlobalPool = inGlobalPool;
			
			status = StatusUninitialized;
			
			resDeclarations = new SparseArray<ResourceDeclaration>();
			
			loadResOrderMap = Util.newTreeMap();
		}
		
		/** Prepare this group for shutdown - don't notify ResManagers. */
		public void shutdown() {
			
		}
	}
	
	private final HashMap<String, ResourceManager> resManagerMap;
	
	private final TreeMap<Integer, ArrayList<ScriptLoader> > scriptLoadOrderMap;
	
	private ResourceLoadingListener loadingListener;
	
	private final LinkedList<ResourceGroupListener> resGroupListeners;
	
	private final HashMap<String, ResourceGroup> resGroupMap;
	
	// Stored current group - optimisation for when bulk loading a group
	private ResourceGroup currentGroup;
}
