package com.renegadeware.m8.ui;

import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableBitmap;
import com.renegadeware.m8.gfx.TextureAtlas;

public class ButtonImageUI extends BaseButtonUI {
	protected String atlasId;
	protected String atlasRefUp;
	protected String atlasRefDown;
	protected String atlasRefDisabled;
	
	protected boolean hFlip;
	protected boolean vFlip;
	
	protected Color colorUp;
	
	protected Color colorDown;
	
	protected Color colorDisabled;
			
	//internal data
	protected final DrawableBitmap imageDrawable;
	
	protected TextureAtlas atlas;

	public ButtonImageUI() {
		super();
						
		imageDrawable = new DrawableBitmap();
		imageDrawable.useColor = true;
	}
	
	public void setAtlas(int atlasId) {
		atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getById(atlasId);
	}
	
	public void setAtlasRefStates(String up, String down, String disabled) {
		atlasRefUp = up;
		atlasRefDown = down;
		atlasRefDisabled = disabled;
	}
	
	@Override
	public void load() {
		if(atlasId != null) {
			atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getByName(atlasId);
		}
		
		super.load();
	}
	
	@Override
	public void resize(float w, float h) {
		imageDrawable.width = w;
		imageDrawable.height = h;

		super.resize(w, h);
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		imageDrawable.color.alpha = alpha;
		imageDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
		
		super.render(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY, alpha);
	}

	protected void setState(int newState) {
		super.setState(newState);
		
		String ref = null;
		Color clr = null;
		
		switch(state) {
		case STATE_DOWN:
			ref = atlasRefDown;
			clr = colorDown;
			break;
		case STATE_DISABLED:
			ref = atlasRefDisabled;
			clr = colorDisabled;
			break;
		}
		
		//set the texture
		if(atlas != null) {
			imageDrawable.setTextureByAtlas(atlas, ref != null ? ref : atlasRefUp);
			imageDrawable.setFlip(hFlip, vFlip);
			imageDrawable.width = getWidth();
			imageDrawable.height = getHeight();
			
			if(clr != null) {
				imageDrawable.color.set(clr);
			}
			else if(colorUp != null) {
				imageDrawable.color.set(colorUp);
			}
			else {
				imageDrawable.color.reset();
			}
		}
	}
	
	/**
	 * Adjust the scale of the string to fit inside the frame
	 */
	protected void adjustTextScale() {
		if(textDrawable.getWidth() > getWidth()) {
			textScale = getWidth()/textDrawable.getWidth();
		}
		else if(textDrawable.getHeight() > getHeight()) {
			textScale = getHeight()/textDrawable.getHeight();
		}
		else {
			textScale = 1.0f;
		}
	}
}
