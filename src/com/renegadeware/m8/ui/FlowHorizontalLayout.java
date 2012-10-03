package com.renegadeware.m8.ui;

public class FlowHorizontalLayout extends BaseLayout {
	
	/** 0 = uniform width based on parent, 1 = width based on each child */
	public int mode;
	public float paddingX;
	public float paddingY;
	
	/** if mode = 1, then this is used to determine adjustment of y. 0 = bottom, 1 = center, 2 = top */
	public int adjustMode; 

	public FlowHorizontalLayout() {
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
			float curX = 0;
			float width = parent.getWidth();
			float height = parent.getMaxHeight();
			
			float widthPerUnit = mode == 0 ? width/count : 0;
			
			float spacingX=0;
						
	        final Object[] objectArray = children.getArray();
	        for (int i = 0; i < count; i++) {
	        	BaseUI cui = (BaseUI)objectArray[i];
	        	
	        	spacingX = cui.layoutSpacing;
	        	
	        	float cw = cui.getWidth();
	        	float ch = cui.getHeight();
	        	
	        	if(mode == 1) {
	        		widthPerUnit = cw;
	        	}
	        	
	        	switch(cui.anchorH) {
	        	case BaseUI.ANCHOR_LEFT:
	        		cui.x = curX + paddingX;
	        		break;
	        		
	        	case BaseUI.ANCHOR_RIGHT:
	        		cui.x = curX + widthPerUnit - paddingX;
	        		break;
	        		
	        	case BaseUI.ANCHOR_CENTER:
	        		cui.x = curX + widthPerUnit*0.5f;
	        		break;
	        		
	        	case BaseUI.ANCHOR_STRETCH:
	        		cui.x = curX + paddingX;
	        		cw = widthPerUnit - paddingX*2;
	        		break;
	        	}
	        	
	        	switch(cui.anchorV) {
	        	case BaseUI.ANCHOR_BOTTOM:
	        		cui.y = paddingY;
	        		break;
	        		
	        	case BaseUI.ANCHOR_TOP:
	        		cui.y = height - paddingY;
	        		break;
	        		
	        	case BaseUI.ANCHOR_CENTER:
	        		cui.y = height*0.5f;
	        		break;
	        		
	        	case BaseUI.ANCHOR_STRETCH:
	        		cui.y = paddingY;
	        		ch = height - paddingY*2;
	        		break;
	        	}
	        	
	        	cui.resize(cw, ch);
	        	
	        	curX += widthPerUnit + spacingX;
	        }
	        
	        float newW = curX-spacingX;
	        
	        if(mode == 1) {
	        	if(width < newW) {
	        		parent.resize(newW, height);
	        	}
	        	else if(adjustMode > 0) {
		        	float ww = adjustMode == 1 ? width*0.5f : width - newW;
		        	//parent width is larger
		        	//reposition children to be centered
		        	for (int i = 0; i < count; i++) {
		        		BaseUI cui = (BaseUI)objectArray[i];
		        		cui.x -= ww;
		        	}
		        }
	        }
		}
	}
}
