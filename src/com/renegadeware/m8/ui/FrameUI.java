package com.renegadeware.m8.ui;

import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableFrame;
import com.renegadeware.m8.gfx.GridFrame;

public class FrameUI extends BaseUI {
	
	protected String frameId;
	protected Color frameColor;
	
	protected final DrawableFrame frameDrawable;

	public FrameUI() {
		frameDrawable = new DrawableFrame();
		frameDrawable.useColor = true;
	}
	
	public final GridFrame getFrame() {
		return frameDrawable.getFrame();
	}

	@Override
	public void load() {
		if(frameId != null) {
			GridFrame frame = (GridFrame)systemRegistry.gridManager.getByName(frameId);
			if(frame != null) {
				frameDrawable.setFrame(frame);
				
				if(frameColor == null) {
					frameColor = new Color(frame.getBodyColor());
				}
				
				frameDrawable.bodyColorOverride = new Color(frameColor);
				
				frameDrawable.width = getWidth();
				frameDrawable.height = getHeight();
			}
		}
	}
	
	@Override
	public boolean isInRegion(float x, float y, boolean useAbsolute) {
		float ax = getAdjustedX(), ay = getAdjustedY();
		
		if(useAbsolute) {
			for(BaseUI parent = getParent(); parent != null; parent = parent.getParent()) {
				ax += parent.getAdjustedX();
				ay += parent.getAdjustedY();
			}
		}
		
		GridFrame f = frameDrawable.getFrame();
		
		return ax-f.getLeftExtend() <= x && x <= ax+getWidth()+f.getRightExtend() 
		&& ay-f.getBottomExtend() <= y && y <= ay+getHeight()+f.getTopExtend();
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);
		frameDrawable.width = w;
		frameDrawable.height = h;
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		frameDrawable.color.alpha = alpha;
		frameDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
	}
}
