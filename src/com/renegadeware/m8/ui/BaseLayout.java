package com.renegadeware.m8.ui;

import java.lang.ref.WeakReference;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.util.FixedSizeArray;

public class BaseLayout {
	
	protected FixedSizeArray<BaseUI> children;
	protected FixedSizeArray<BaseUI> childrenAdds;
	protected FixedSizeArray<BaseUI> childrenRemoves;
	
	protected boolean doRefresh;
	
	protected WeakReference<BaseUI> _parent;
	
	public final void setCapacity(int capacity) {
		children = new FixedSizeArray<BaseUI>(capacity);
		childrenAdds = new FixedSizeArray<BaseUI>(capacity);
		childrenRemoves = new FixedSizeArray<BaseUI>(capacity);
	}
	
	public final BaseUI getParent() {
		return _parent != null ? _parent.get() : null;
	}
	
	public final void addChild(BaseUI ui) {
		//remove from previous parent's layout
		BaseUI parent = ui.getParent();
		if(parent != null) {
			BaseLayout layout = parent.getLayout();
			if(layout != null) {
				layout.removeChild(ui);
			}
		}
		
		childrenAdds.add(ui);
	}
	
	public final void removeChild(BaseUI ui) {
		//sanity check
		if(ui._parent == _parent) {
			childrenRemoves.add(ui);
		}
		else {
			DebugLog.w("BaseLayout", "Given ui: "+ui.name+" is not associated with this layout");
		}
	}
	
	public final void removeAllChildren() {
		final int count = children.getCount();
        final Object[] objectArray = children.getArray();
        for (int i = 0; i < count; i++) {
        	childrenRemoves.add((BaseUI)objectArray[i]);
        }
        childrenAdds.clear();
	}
	
	private static final void _resetChildren(FixedSizeArray<BaseUI> array) {
		int count = array.getCount();
        Object[] objectArray = array.getArray();
        for (int i = 0; i < count; i++) {
        	BaseUI ui = (BaseUI)objectArray[i];
        	ui.reset();
        }
	}
	
	protected final void reset() {
		resetImpl();
		
		_resetChildren(children);
		_resetChildren(childrenAdds);
		_resetChildren(childrenRemoves);
		
		//children.clear();
		childrenAdds.clear();
		childrenRemoves.clear();
		
		doRefresh = false;
	}
	
	private static BaseUI _findChild(String name, FixedSizeArray<BaseUI> array, boolean recursive) {
		int count = array.getCount();
        Object[] objectArray = array.getArray();
        for (int i = 0; i < count; i++) {
        	BaseUI ui = (BaseUI)objectArray[i];
        	if(ui.name.compareTo(name) == 0) {
        		return ui;
        	}
        }
        
        if(recursive) {
        	count = array.getCount();
            objectArray = array.getArray();
            for (int i = 0; i < count; i++) {
            	BaseUI ui = (BaseUI)objectArray[i];
            	BaseLayout layout = ui.getLayout();
            	
            	if(layout != null) {
            		BaseUI found = _findChild(name, layout.children, true);
            		if(found != null) {
            			return found;
            		}
            		else {
            			found = _findChild(name, layout.childrenAdds, true);
            			if(found != null) {
            				return found;
            			}
            		}
            	}
            }
        }
        
        return null;
	}
	
	public final BaseUI findChild(String name, boolean recursive) {
		
		BaseUI found = _findChild(name, children, recursive);
		if(found == null) {
			//try pending adds
			found = _findChild(name, childrenAdds, recursive);
		}
        
		return found;
	}
	
	public final int getIndex(BaseUI ui) {
		final int count = children.getCount();
		if(count > 0) {
	        final Object[] objectArray = children.getArray();
	        for (int i = 0; i < count; i++) {
	        	BaseUI cui = (BaseUI)objectArray[i];
	        	if(cui == ui) {
	        		return i;
	        	}
	        }
		}
        
		return -1;
	}
	
	public final BaseUI getChild(int index) {
		return children.get(index);
	}
	
	public final int getNumChildren() {
		return children.getCount();
	}
	
	public final FixedSizeArray<BaseUI> getChildren() {
		return children;
	}
	
	protected final void _refreshChildrenParent() {
		final int count = children.getCount();
		if(count > 0) {
	        final Object[] objectArray = children.getArray();
	        for (int i = 0; i < count; i++) {
	        	BaseUI cui = (BaseUI)objectArray[i];
	        	cui._parent = _parent;
	        }
		}
	}
	
	private void _addChildren(Object[] additionsArray, int additionCount) {
        for (int i = 0; i < additionCount; i++) {
        	BaseUI ui = (BaseUI)additionsArray[i];
        	
            children.add(ui);
            
            ui._parent = _parent;
            
            addChildImpl(ui);
        } 
	}
	
	private void _removeChildren(Object[] removalsArray, int removalCount) {
		for (int i = 0; i < removalCount; i++) {
        	BaseUI ui = (BaseUI)removalsArray[i];
        	
            children.remove(ui, true);
            
            ui._parent = null;
            
            removeChildImpl(ui);
        }
	}
	
	protected final void updateCommit() {
		final int additionCount = childrenAdds.getCount();
        if (additionCount > 0) {
        	//for safety's sake
        	BaseUI parent = getParent();
        	if(parent != null) {
        		synchronized(parent) {
        			_addChildren(childrenAdds.getArray(), additionCount);
        		}
        	}
        	else {
        		_addChildren(childrenAdds.getArray(), additionCount);
        	}
        	
	        childrenAdds.clear();
	        
	        doRefresh = true;
        }
        
        final int removalCount = childrenRemoves.getCount();
        if (removalCount > 0) {
        	//for safety's sake
        	BaseUI parent = getParent();
        	if(parent != null) {
        		synchronized(parent) {
        			_removeChildren(childrenRemoves.getArray(), removalCount);
        		}
        	}
        	else {
        		_removeChildren(childrenRemoves.getArray(), removalCount);
        	}
        	
            childrenRemoves.clear();
            
            doRefresh = true;
        }
	}
	
	protected final void updateRefresh(BaseUI parent) {		       
        if(doRefresh) {
        	refreshImpl(parent);
        	doRefresh = false;
        }
	}
		
	protected void initImpl(int numChildren) {
		
	}
	
	protected void resetImpl() {
		
	}
	
	/**
	 * Use this for when you have a custom way of storing ui
	 * 
	 * @param ui
	 */
	protected void addChildImpl(BaseUI ui) {
		
	}
	
	/**
	 * Use this for when you have a custom way of storing ui
	 * 
	 * @param ui
	 */
	protected void removeChildImpl(BaseUI ui) {
		
	}
	
	/**
	 * Whenever the ui size is changed or a child is added/removed, this is called.
	 * 
	 * @param parent
	 */
	protected void refreshImpl(BaseUI parent) {
		
	}
}
