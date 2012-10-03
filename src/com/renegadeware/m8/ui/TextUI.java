package com.renegadeware.m8.ui;

import com.renegadeware.m8.ContextParameters;
import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableTextLine;
import com.renegadeware.m8.gfx.Font;

public class TextUI extends BaseUI {
	
	protected String fontId;
	protected float fontPtSize;
	protected String stringId;
	protected Color color; 
	
	protected int stringResId;
	protected final DrawableTextLine textDrawable;

	public TextUI() {
		textDrawable = new DrawableTextLine();
	}
	
	public void setDefaultTextId(String id) {
		stringId = id;
	}
	
	public void setDefaultTextId(int id) {
		stringResId = id;
	}
	
	public void setText(String text) {
		textDrawable.setText(text);
		
		resize(textDrawable.getWidth(), textDrawable.getHeight());
	}
	
	public void setText(int id) {
		ContextParameters cp = systemRegistry.contextParameters;
		if(id != 0) {
			String str = cp.context.getResources().getString(id);
			setText(str);
		}
	}
	
	public void setDefaultText() {
		if(stringResId != 0) {
			setText(stringResId);
		}
		else if(stringId != null) {
			ContextParameters cp = systemRegistry.contextParameters;
			setText(cp.getResourceId(stringId, null));
		}
	}
	
	public void setColor(Color c) {
		if(c == null) {
			if(color != null) {
				textDrawable.color.set(color);
				setAlpha(color.alpha);
			}
		}
		else {
			textDrawable.color.set(c);
			setAlpha(c.alpha);
		}
	}

	@Override
	public void load() {
		if(fontId != null) {
			Font font = (Font)systemRegistry.fontManager.getByName(fontId);
			if(font != null) {
				textDrawable.setFont(font);
				
				setColor(null);
								
				if(fontPtSize > 0.0f) {
					textDrawable.setPointSize(fontPtSize);
				}
			}
		}
		
		/*final ContextParameters cp = systemRegistry.contextParameters;
		
		if(stringResId != 0) {
			String str = cp.context.getResources().getString(stringResId);
			textDrawable.setText(str);
		}
		else if(stringId != null) {
			int id = cp.getResourceId(stringId, null);
			if(id != 0) {
				String str = cp.context.getResources().getString(id);
				textDrawable.setText(str);
			}
		}*/
		
		//resize(textDrawable.getWidth(), textDrawable.getHeight());
		setDefaultText();
	}
		
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		textDrawable.color.alpha = alpha;
		textDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
	}
}
