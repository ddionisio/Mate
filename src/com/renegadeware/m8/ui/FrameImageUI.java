package com.renegadeware.m8.ui;

import com.renegadeware.m8.gfx.DrawableBitmap;
import com.renegadeware.m8.gfx.Texture;
import com.renegadeware.m8.gfx.TextureAtlas;

/**
 * Frame with image
 * @author ddionisio
 *
 */
public class FrameImageUI extends FrameUI {
	
	protected String imageId; //use this as a standalone image
	protected String atlasId; //use this if you want an atlas ref rather than image id
	protected String atlasRef;
	protected float paddingX;
	protected float paddingY;
	protected boolean hFlip;
	protected boolean vFlip;
	
	protected final DrawableBitmap imageDrawable;

	public FrameImageUI() {
		super();
		
		imageDrawable = new DrawableBitmap();
		imageDrawable.useColor = true;
	}

	public void setImage(int resId) {
		imageDrawable.setTextureAutoCrop((Texture)systemRegistry.textureManager.getById(resId));
		flip(hFlip, vFlip);
	}
	
	public void setImage(String resId) {
		imageDrawable.setTextureAutoCrop((Texture)systemRegistry.textureManager.getByName(resId));
		flip(hFlip, vFlip);
	}
	
	public void setImageByAtlas(int resId, String ref) {
		TextureAtlas atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getById(resId);
		if(atlas != null) {
			imageDrawable.setTextureByAtlas(atlas, ref);
			flip(hFlip, vFlip);
		}
	}
	
	public void setImageByAtlas(String resId, String ref) {
		TextureAtlas atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getByName(resId);
		if(atlas != null) {
			imageDrawable.setTextureByAtlas(atlas, ref);
			flip(hFlip, vFlip);
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
		
		flip(hFlip, vFlip);
	}
	
	public void flip(boolean horizontal, boolean vertical) {
		hFlip = horizontal;
		vFlip = vertical;
		imageDrawable.setFlip(horizontal, vertical);
	}

	@Override
	public void load() {
		super.load();
		
		setImageByDefault();
		
		/*if(frameColor != null) {
			imageDrawable.color.set(frameColor);
		}*/
		
		resize(getWidth(), getHeight());
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);

		imageDrawable.width = w-paddingX*2;
		imageDrawable.height = h-paddingY*2;
		imageDrawable.anchorX = paddingX;
		imageDrawable.anchorY = paddingY;
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		super.render(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY, alpha);
		
		imageDrawable.color.alpha = alpha;
		imageDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
	}
}
