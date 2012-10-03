package com.renegadeware.m8.ui;

import java.util.Collection;
import java.util.HashMap;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;
import com.renegadeware.m8.util.DataSection;
import com.renegadeware.m8.util.FixedSizeArray;
import com.renegadeware.m8.util.Util;
import com.renegadeware.m8.util.XmlSection;

public class UIRes extends Resource {
	public static final String UI_TAG = "UI";
	public static final String CLASS_ATTR = "class";
	
	private final HashMap<String, BaseUI> uis;

	public UIRes(ResourceManager creator, int id, String group, boolean isManual,
			ManualResourceLoader loader) {
		super(creator, id, group, isManual, loader);
		
		uis = new HashMap<String, BaseUI>();
	}
	
	public BaseUI getUI(String name) {
		return uis.get(name);
	}
	
	@Override
	protected void prepareImpl() {
		XmlSection xml = XmlSection.createFromResourceId(systemRegistry.contextParameters.context, id);
		if(xml != null) {
			DataSection.SearchIterator it = xml.search(UI_TAG);
			while(it.hasNext()) {
				DataSection uiDat = it.next();
				if(uiDat != null) {					
					try {
						BaseUI newUI = BaseUI.createUIFromDataSection(uiDat);
						
						uis.put(newUI.getName(), newUI);
						
					} catch (Exception e) {
						DebugLog.e("UIRes", e.toString(), e);
					}
				}
			}
		}
	}
	
	@Override
	protected void unprepareImpl() {
		uis.clear();
	} 
		
	@Override
	protected void loadImpl() {
		Collection<BaseUI> vals = uis.values();
		for(BaseUI ui : vals) {
			BaseUI.LoadUI(ui);
		}
	}

	@Override
	protected void unloadImpl() {
		Collection<BaseUI> vals = uis.values();
		for(BaseUI ui : vals) {
			ui.reset();
		}
	}

	@Override
	protected int calculateSize() {
		return 0;
	}
}
