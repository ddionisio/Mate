package com.renegadeware.m8.ui;

import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableFrame;
import com.renegadeware.m8.gfx.GridFrame;

public class ButtonFrameUI extends BaseButtonUI {
	
	protected String frameId;
	protected Color frameColor;
	protected Color frameColorDown;
	protected Color frameColorDisabled;
	
	protected boolean frameColorUseBody;
	
	protected final DrawableFrame frameDrawable;

	public ButtonFrameUI() {
		super();
		
		frameColorUseBody = true; //default
		
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
				frameDrawable.width = getWidth();
				frameDrawable.height = getHeight();
				
				if(frameColor == null) {
					frameColor = new Color(frameColorUseBody ? frame.getBodyColor() : Color.WHITE);
				}
			}
		}
						
		super.load();
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
		frameDrawable.width = w;
		frameDrawable.height = h;
		super.resize(w, h);
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		frameDrawable.color.alpha = alpha;
		frameDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
		
		super.render(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY, alpha);
	}
	
	protected void setState(int newState) {
		super.setState(newState);
		
		Color _frameColor = frameColor;
		
		switch(state) {
		case STATE_DOWN:
			_frameColor = frameColorDown;
			break;
		case STATE_DISABLED:
			_frameColor = frameColorDisabled;
			break;
		}
		
		if(frameDrawable.getFrame() != null) {
			if(frameColorUseBody) {
				frameDrawable.bodyColorOverride = _frameColor;
			}
			else {
				frameDrawable.color.set(_frameColor);
			}
		}
	}
}
