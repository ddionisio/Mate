package com.renegadeware.m8.ui;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableGrid;
import com.renegadeware.m8.gfx.Font;
import com.renegadeware.m8.gfx.GridManager;
import com.renegadeware.m8.gfx.GridText;
import com.renegadeware.m8.res.ResourceGroupManager;

public class TextBoxUI extends BaseUI {
	
	protected String fontId;
	protected int alignment;
	protected float fontPtSize;
	protected Color color;
	
	protected String stringId;
	
	protected Object[] stringParams;
		
	//internal
	protected int stringResId;
	
	protected final DrawableGrid textDrawable;

	public TextBoxUI() {
		super();
		
		stringResId = 0;
		
		textDrawable = new DrawableGrid();
		textDrawable.useColor = true;
	}
	
	/**
	 * Only use this if you want to manually set the text id. Call this BEFORE loading this ui or its parent.
	 * @param id
	 */
	public void setTextId(String id) {
		stringId = id;
	}
	
	/**
	 * Only use this if you want to manually set the text id. Call this BEFORE loading this ui or its parent.
	 * @param id
	 */
	public void setTextId(int id) {
		stringResId = id;
	}
	
	/**
	 * Call this before load to specify parameters in the text
	 * @param params
	 */
	public void setTextParams(Object[] params) {
		stringParams = params;
	}
	
	@Override
	public void unload() {
		if(textDrawable.grid != null) {
			textDrawable.grid.unload();
			systemRegistry.gridManager.remove(textDrawable.grid);
			textDrawable.grid = null;
		}
	}
	
	@Override
	public void load() {
		
		Font fnt = (Font) systemRegistry.fontManager.getByName(fontId);
		
		GridManager gm = systemRegistry.gridManager;
		
		if(stringId != null || stringResId != 0) {
			GridText gridText;
			
			if(stringResId != 0) {
				gridText = gm.createText(stringResId, 
						ResourceGroupManager.InternalResourceGroupName, 
						fnt.id, 
						fontPtSize, getWidth(), alignment, stringParams);
			}
			else {
				gridText = gm.createText(stringId, 
						ResourceGroupManager.InternalResourceGroupName, 
						fnt.id, 
						fontPtSize, getWidth(), alignment, stringParams);
			}
			
			try {
				gridText.load(false);
			} catch(Exception e) {
				DebugLog.e("UI", e.toString(), e);
			}
			
			if(gridText.isLoaded()) {			
				textDrawable.grid = gridText;
				textDrawable.texture = fnt.getTexture(0);
				
				if(color != null) {
					textDrawable.color.set(color);
					setAlpha(color.alpha);
				}
				
				resize(gridText.getWidth(), gridText.getHeight());
			}
		}
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		textDrawable.color.alpha = alpha;
		textDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
	}
}
