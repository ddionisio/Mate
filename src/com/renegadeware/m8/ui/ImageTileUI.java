package com.renegadeware.m8.ui;

public class ImageTileUI extends ImageUI {
	
	protected int numTileH;
	protected int numTileV;
	
	//internal
	protected float tileWidth;
	protected float tileHeight;

	public ImageTileUI() {
		super();
		
		numTileH = numTileV = 1;
	}

	@Override
	public void load() {
		super.load();
		
		_calculateTileSize();
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);
		
		_calculateTileSize();
	}
	
	private void _calculateTileSize() {
		final float w = getWidth();
		final float h = getHeight();
		
		tileWidth = w/numTileH;
		tileHeight = h/numTileV;
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		imageDrawable.color.alpha = alpha;
		
		imageDrawable.anchorY = y;
		
		for(int r = 0; r < numTileV; r++) {
			
			imageDrawable.anchorX = x;
			
			for(int c = 0; c < numTileH; c++) {
				imageDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
				
				imageDrawable.anchorX += tileWidth;
			}
			
			imageDrawable.anchorY += tileHeight;
		}
	}
}
