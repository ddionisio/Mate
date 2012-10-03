package com.renegadeware.m8.gfx;

import com.renegadeware.m8.obj.ObjectManager;

//TODO: add better multiple view support
public class ViewSystem extends ObjectManager {
	protected static final int MAX_VIEW = 4;
	
	private Viewport defaultView;
			
	public ViewSystem(float defaultWidth, float defaultHeight) {
		super(MAX_VIEW);
		
		//at least add 1
		add(defaultView = new Viewport(defaultWidth, defaultHeight));
		commitUpdates();
	}
	
	public Viewport getDefaultView() {
		return defaultView;
	}
	
	public void setDefaultView(Viewport v) {
		remove(defaultView);
		defaultView = v;
		if(v != null) {
			add(defaultView);
		}
		commitUpdates();
	}
	
	@Override
	public void reset() {
		super.reset();
	}

}
