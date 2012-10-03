package com.renegadeware.m8.ui;

public class FlowVerticalLayout extends BaseLayout {
	
	/** 0 = uniform height based on parent, 1 = height based on each child */
	public int mode;
	public float paddingX;
	public float paddingY;
	
	/** if mode = 1, then this is used to determine adjustment of y. 0 = bottom, 1 = center, 2 = top */
	public int adjustMode; 

	public FlowVerticalLayout() {
		mode = 0;
	}

	@Override
	protected void addChildImpl(BaseUI ui) {
		
	}
	
	@Override
	protected void removeChildImpl(BaseUI ui) {
		
	}
	
	@Override
	protected void refreshImpl(BaseUI parent) {
		final int count = children.getCount();
		if(count > 0) {
			//since opengl is default to bottom to top,
			//arrange the ui from last child to first child, starting at the bottom
			float curY = 0;
			
			float width = parent.getMaxWidth();
			float height = parent.getHeight();
			
			float heightPerUnit = mode == 0 ? height/count : 0;
			
			float spacingY = 0;
			
	        final Object[] objectArray = children.getArray();
	        for (int i = count-1; i >= 0; i--) {
	        	BaseUI cui = (BaseUI)objectArray[i];
	        	
	        	spacingY = cui.layoutSpacing;
	        	
	        	float cw = cui.getWidth();
	        	float ch = cui.getHeight();
	        	
	        	if(mode == 1) {
	        		heightPerUnit = ch;
	        	}
	        	
	        	if(heightPerUnit > 0) {
		        	switch(cui.anchorH) {
		        	case BaseUI.ANCHOR_LEFT:
		        		cui.x = paddingX;
		        		break;
		        		
		        	case BaseUI.ANCHOR_RIGHT:
		        		cui.x = width - paddingX;
		        		break;
		        		
		        	case BaseUI.ANCHOR_CENTER:
		        		cui.x = width*0.5f;
		        		break;
		        		
		        	case BaseUI.ANCHOR_STRETCH:
		        		cui.x = paddingX;
		        		cw = width - paddingX*2;
		        		break;
		        	}
		        	
		        	switch(cui.anchorV) {
		        	case BaseUI.ANCHOR_BOTTOM:
		        		cui.y = curY + paddingY;
		        		break;
		        		
		        	case BaseUI.ANCHOR_TOP:
		        		cui.y = curY + heightPerUnit - paddingY;
		        		break;
		        		
		        	case BaseUI.ANCHOR_CENTER:
		        		cui.y = curY + heightPerUnit*0.5f;
		        		break;
		        		
		        	case BaseUI.ANCHOR_STRETCH:
		        		cui.y = curY + paddingY;
		        		ch = heightPerUnit - paddingY*2;
		        		break;
		        	}
		        	
		        	cui.resize(cw, ch);
		        	
		        	curY += heightPerUnit + spacingY;
	        	}
	        }
	        
	        float newH = curY-spacingY;
	        
	        if(mode == 1) {
	        	if(height < newH) {
	        		parent.resize(width, newH+paddingY*2);
	        	}
	        	else if(adjustMode > 0) {
		        	float hh = adjustMode == 1 ? height*0.5f : height - newH;
		        	//parent height is larger
		        	//reposition children to be centered
		        	for (int i = 0; i < count; i++) {
		        		BaseUI cui = (BaseUI)objectArray[i];
		        		cui.y += hh;
		        	}
		        }
	        }
		}
	}
}
