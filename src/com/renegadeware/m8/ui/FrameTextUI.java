package com.renegadeware.m8.ui;

import com.renegadeware.m8.ContextParameters;
import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableFrame;
import com.renegadeware.m8.gfx.DrawableTextLine;
import com.renegadeware.m8.gfx.Font;
import com.renegadeware.m8.gfx.GridFrame;

public class FrameTextUI extends FrameUI {
	
	protected String fontId;
	protected String stringId;
	
	protected Color fontColor;
	protected float fontPtSize;
	
	protected float paddingX;
	protected float paddingY;
	
	protected final DrawableTextLine textDrawable;

	public FrameTextUI() {
		super();
		
		textDrawable = new DrawableTextLine();
		textDrawable.setAlign(DrawableTextLine.ALIGN_CENTER);
		textDrawable.setAlignHeight(DrawableTextLine.ALIGN_CENTER);
	}
	
	public void setText(String text) {
		textDrawable.setText(text);
		
		float txtH = textDrawable.getHeight()+paddingY*2;
		float h = getHeight();
		
		resize(textDrawable.getWidth()+paddingX*2, txtH > h ? txtH : h);
	}
	
	public void setText(int resId) {
		ContextParameters cp = systemRegistry.contextParameters;
		setText(cp.context.getResources().getString(resId));
	}
		
	@Override
	public void load() {
		super.load();
		
		if(fontId != null) {
			Font font = (Font)systemRegistry.fontManager.getByName(fontId);
			if(font != null) {
				textDrawable.setFont(font);
				
				if(fontColor != null) {
					textDrawable.color.set(fontColor);
				}
								
				if(fontPtSize > 0.0f) {
					textDrawable.setPointSize(fontPtSize);
				}
			}
		}
		
		if(stringId != null) {
			ContextParameters cp = systemRegistry.contextParameters;
			int id = cp.getResourceId(stringId, null);
			if(id != 0) {
				String str = cp.context.getResources().getString(id);
				setText(str);
			}
		}
	}
		
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		super.render(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY, alpha);
		
		if(!textDrawable.isEmpty()) {
			textDrawable.color.alpha = alpha;
			textDrawable.draw(x+getWidth()*0.5f*scaleX, y+getHeight()*0.5f*scaleY, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
		}
	}
}
