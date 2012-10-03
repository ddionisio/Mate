package com.renegadeware.m8.ui;

import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableBitmap;
import com.renegadeware.m8.gfx.Texture;
import com.renegadeware.m8.gfx.TextureAtlas;

public class ImageUI extends BaseUI {
	
	protected String imageId; //use this as a standalone image
	protected String atlasId; //use this if you want an atlas ref rather than image id
	protected String atlasRef;
	protected Color color;
	protected boolean flipH;
	protected boolean flipV;
	
	protected final DrawableBitmap imageDrawable;

	public ImageUI() {
		super();
		
		imageDrawable = new DrawableBitmap();
		imageDrawable.useColor = true;
	}
	
	public Color getColor() {
		return imageDrawable.color;
	}
	
	public void setImage(int resId) {
		imageDrawable.setTextureAutoCrop((Texture)systemRegistry.textureManager.getById(resId));
		flip(flipH, flipV);
	}
	
	public void setImage(String resId) {
		imageDrawable.setTextureAutoCrop((Texture)systemRegistry.textureManager.getByName(resId));
		flip(flipH, flipV);
	}
	
	public void setImageByAtlas(int resId, String ref) {
		TextureAtlas atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getById(resId);
		if(atlas != null) {
			imageDrawable.setTextureByAtlas(atlas, ref);
			flip(flipH, flipV);
		}
	}
	
	public void setImageByAtlas(String resId, String ref) {
		TextureAtlas atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getByName(resId);
		if(atlas != null) {
			imageDrawable.setTextureByAtlas(atlas, ref);
			flip(flipH, flipV);
		}
	}
	
	public void setImageByDefault() {
		if(atlasId != null && atlasRef != null) {
			TextureAtlas atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getByName(atlasId);
			if(atlas != null) {
				imageDrawable.setTextureByAtlas(atlas, atlasRef);
			}
		}
		else if(imageId != null) {
			imageDrawable.setTextureAutoCrop((Texture)systemRegistry.textureManager.getByName(imageId));
		}
	}
	
	public void flip(boolean horizontal, boolean vertical) {
		flipH = horizontal;
		flipV = vertical;
		imageDrawable.setFlip(horizontal, vertical);
	}

	@Override
	public void load() {
		setImageByDefault();
		
		if(color != null) {
			imageDrawable.color.set(color);
			setAlpha(color.alpha);
		}
		
		imageDrawable.width = getWidth();
		imageDrawable.height = getHeight();
	}
	
	@Override
	public void unload() {
		imageDrawable.texture = null;
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);

		imageDrawable.width = w;
		imageDrawable.height = h;
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		imageDrawable.color.alpha = alpha;
		imageDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
	}
}
