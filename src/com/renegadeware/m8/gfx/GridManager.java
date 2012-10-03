package com.renegadeware.m8.gfx;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;

public class GridManager extends ResourceManager {
	public static final String Type = "grid";
	
	private static final int GRID_LIST_SIZE = 256;

	public GridManager() {
		super(0);
	}

	@Override
	public int loadingOrder() {
		return DEFAULT_ORDER_GRID;
	}

	@Override
	public String name() {
		return Type;
	}

	@Override
	public String defType() {
		return "drawable";
	}
	
	public Grid create(int id, String group, int numCol, int numRow) {
		Grid ret = (Grid)getById(id);
		if(ret == null) {
			ret = new Grid(numCol, numRow, this, id, group);
			
			addImpl(ret);
			
			// notify group manager
			systemRegistry.resourceGroupManager.notifyResourceCreated(ret);
		}
		
		return ret;
	}
		
	public Grid create(String name, String group, int numCol, int numRow) {
		return create(getResourceIdByName(name), group, numCol, numRow);
	}
	
	/**
	 * Creates a static text display resource.
	 * 
	 * @param stringId
	 * @param group
	 * @param fontId
	 * @param pointSize The size of the font, e.g. 8,16,32,etc. if 0, use the size based on the font's data.
	 * @param maxWidth The horizontal constraint of the text for used as a textbox. If 0, then width is based on the sum of each character's width in the text.
	 * @param alignment Used for when creating a textbox when width > 0.
	 * @return A resource data that is ready to be loaded by the resource manager within its group.
	 */
	public GridText createText(int stringId, String group, int fontId, float pointSize, 
			float maxWidth, int alignment, Object[] stringParams) {
		GridText ret = (GridText)getById(stringId);
		if(ret == null) {
			GridTextParam params = new GridTextParam();
			params.fontId = fontId;
			params.pointSize = pointSize;
			params.maxWidth = maxWidth;
			params.alignment = alignment;
			params.stringParams = stringParams;
			
			ret = new GridText(this, stringId, group);
			ret.setParameters(params);
			
			addImpl(ret);
			
			// notify group manager
			systemRegistry.resourceGroupManager.notifyResourceCreated(ret);
		}
		
		return ret;
	}
	
	public GridText createText(String stringId, String group, int fontId, float pointSize, 
			float maxWidth, int alignment, Object[] stringParams) {				
		return createText(getResourceIdByName(stringId), group, fontId, pointSize, maxWidth, alignment, stringParams);
	}
	
	public GridFrame createFrame(int id, String group) {
		GridFrame ret = (GridFrame)getById(id);
		if(ret == null) {
			ret = new GridFrame(this, id, group);
			
			addImpl(ret);
			
			// notify group manager
			systemRegistry.resourceGroupManager.notifyResourceCreated(ret);
		}
		
		return ret;
	}

	@Override
	protected Resource createImpl(int id, String group, boolean isManual,
			ManualResourceLoader loader, Resource.Param params) {
		//check params type to determine which grid to make
		if(params != null) {
			if(params instanceof GridTextParam) {
				return (Resource)new GridText(this, id, group);
			}
			else if(params instanceof GridFrameParam) {
				return (Resource)new GridFrame(this, id, group);
			}
		}
		
		return (Resource)new Grid(this, id, group, isManual, loader);
	}
}
