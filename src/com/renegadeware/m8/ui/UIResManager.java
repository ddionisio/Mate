package com.renegadeware.m8.ui;

import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;

public class UIResManager extends ResourceManager {
	
	public static final String Type = "ui";

	public UIResManager() {
		super(0);
	}
	
	/**
	 * Convenience for getting a UI directly with given id and ui name.
	 * 
	 * @param resId The resource UI to look for given uiName.
	 * @param uiName The name of the UI to look for in resId.
	 * @return The UI if found.
	 */
	public BaseUI getUI(int resId, String uiName) {
		UIRes res = (UIRes)getById(resId);
		if(res != null) {
			return res.getUI(uiName);
		}
		
		return null;
	}
	
	/**
	 * Convenience for getting a UI directly with given id and ui name.
	 * 
	 * @param resId The resource UI to look for given uiName.
	 * @param uiName The name of the UI to look for in resId.
	 * @return The UI if found.
	 */
	public BaseUI getUI(String resId, String uiName) {
		UIRes res = (UIRes)getByName(resId);
		if(res != null) {
			return res.getUI(uiName);
		}
		
		return null;
	}

	@Override
	public int loadingOrder() {
		return DEFAULT_ORDER_UI;
	}

	@Override
	public String name() {
		return Type;
	}

	@Override
	public String defType() {
		return "drawable";
	}
	
	@Override
	protected Resource createImpl(int id, String group, boolean isManual,
			ManualResourceLoader loader, Resource.Param params) {
		return (Resource)new UIRes(this, id, group, isManual, loader);
	}
}
