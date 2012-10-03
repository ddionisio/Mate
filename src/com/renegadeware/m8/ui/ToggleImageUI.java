package com.renegadeware.m8.ui;

import com.renegadeware.m8.gfx.DrawableBitmap;
import com.renegadeware.m8.gfx.TextureAtlas;

public class ToggleImageUI extends ToggleUI {
	
	protected String atlasId;
	
	protected String baseRef;
	protected String toggleRef;
	
	protected float toggleWidth;
	protected float toggleHeight;
	
	protected final DrawableBitmap baseDrawable;
	protected final DrawableBitmap toggleDrawable;
	
	public ToggleImageUI() {
		super();
		
		baseDrawable = new DrawableBitmap();
		toggleDrawable = new DrawableBitmap();
	}
		
	@Override
	public void load() {
		TextureAtlas atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getByName(atlasId);
		if(atlas != null) {
			baseDrawable.setTextureByAtlas(atlas, baseRef);
			
			toggleDrawable.setTextureByAtlas(atlas, toggleRef);
			toggleDrawable.width = toggleWidth;
			toggleDrawable.height = toggleHeight;
		}
		
		_align();
	}
	
	@Override
	public void unload() {
		toggleDrawable.texture = baseDrawable.texture = null;
	}
	
	private void _align() {
		final float w = getWidth(), h = getHeight();
		
		baseDrawable.width = w;
		baseDrawable.height = h;
		
		toggleDrawable.anchorX = w*0.5f - toggleWidth*0.5f;
		toggleDrawable.anchorY = h*0.5f - toggleHeight*0.5f;
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);

		_align();
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		baseDrawable.color.alpha = alpha;
		baseDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
		
		if(toggle) {
			toggleDrawable.color.alpha = alpha;
			toggleDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
		}
	}

	@Override
	protected void onToggleChange() {
	}
}
