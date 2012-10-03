package com.renegadeware.m8.ui;

public class AnchorLayout extends BaseLayout {
	 
	public float paddingX;
	public float paddingY;

	public AnchorLayout() {
	}

	@Override
	protected void refreshImpl(BaseUI parent) {
		final int count = children.getCount();
		if(count > 0) {
			float width = parent.getWidth();
			float height = parent.getHeight();
			
			final Object[] objectArray = children.getArray();
	        for (int i = 0; i < count; i++) {
	        	BaseUI ui = (BaseUI)objectArray[i];
	        	
	        	float w = ui.getWidth();
	        	float h = ui.getHeight();
	        	
	        	switch(ui.anchorH) {
	        	case BaseUI.ANCHOR_LEFT:
	        		ui.x = paddingX;
	        		break;
	        		
	        	case BaseUI.ANCHOR_RIGHT:
	        		ui.x = width - paddingX;
	        		break;
	        		
	        	case BaseUI.ANCHOR_CENTER:
	        		ui.x = width*0.5f;
	        		break;
	        		
	        	case BaseUI.ANCHOR_STRETCH:
	        		ui.x = paddingX;
	        		w = width - paddingX*2;
	        		break;
	        	}
	        	
	        	switch(ui.anchorV) {
	        	case BaseUI.ANCHOR_BOTTOM:
	        		ui.y = paddingY;
	        		break;
	        		
	        	case BaseUI.ANCHOR_TOP:
	        		ui.y = height - paddingY;
	        		break;
	        		
	        	case BaseUI.ANCHOR_CENTER:
	        		ui.y = height*0.5f;
	        		break;
	        		
	        	case BaseUI.ANCHOR_STRETCH:
	        		ui.y = paddingY;
	        		h = height - paddingY*2;
	        		break;
	        	}
	        	
	        	ui.resize(w, h);
	        }
		}
	}
}
