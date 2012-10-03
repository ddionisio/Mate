package com.renegadeware.m8.gfx;

import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.PhasedObject;
import com.renegadeware.m8.obj.TObjectPool;

/**
 * Used for dialogs, guis, and other crap.  Allocate first with constant data, then
 * make sure to call setBodySize to initialize display data.
 * 
 * @author ddionisio
 *
 */
public class ObjectFrame extends PhasedObject {
	
	public static final int PARAM_CORNER_UPPER_LEFT = 0;
	public static final int PARAM_CORNER_UPPER_RIGHT = 1;
	public static final int PARAM_CORNER_LOWER_LEFT = 2;
	public static final int PARAM_CORNER_LOWER_RIGHT = 3;
	public static final int PARAM_BORDER_TOP = 4;
	public static final int PARAM_BORDER_BOTTOM = 5;
	public static final int PARAM_BORDER_LEFT = 6;
	public static final int PARAM_BORDER_RIGHT = 7;
	
	public static final int INDEX_CORNER_UPPER_LEFT = 0;
	public static final int INDEX_CORNER_UPPER_RIGHT = 1;
	public static final int INDEX_CORNER_LOWER_LEFT = 2;
	public static final int INDEX_CORNER_LOWER_RIGHT = 3;
	public static final int INDEX_BORDER_TOP = 0;
	public static final int INDEX_BORDER_BOTTOM = 1;
	public static final int INDEX_BORDER_LEFT = 2;
	public static final int INDEX_BORDER_RIGHT = 3;
	
	public class FrameParameter {
		public String texture; //texture within the atlas
		public float width;
		public float height;
		public boolean flipH;
		public boolean flipV;
		public boolean borderRepeat; //used for border to determine if we want to tile repeatedly
	}
	
	protected final TObjectPool<DrawableBitmap> pool;
	
	protected final int renderOrder;
	
	protected final Texture texture;
	
	protected final CornerData[] corners;
	protected final BorderData[] borders;
	
	protected final DrawableObject drawableBody;
	
	protected float x;
	protected float y;
	protected float bodyWidth;
	protected float bodyHeight;
	protected float scaleX;
	protected float scaleY;
	protected float anchorX;
	protected float anchorY;

	public ObjectFrame(
			TObjectPool<DrawableBitmap> drawableBitmapPool,
			int renderOrder,
			int textureAtlasId, 
			String bodyTexture, Color bodyColor, 
			FrameParameter[] frames) {
		
		this.pool = drawableBitmapPool;
		this.renderOrder = renderOrder;
		
		TextureAtlas atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getById(textureAtlasId);
		assert atlas != null : "Atlas not found!";
		
		this.texture = atlas.getTexture();
		
		// create the drawable element for the body,
		// this is optional and can be disabled by not passing color or color alpha is 0
		if(bodyColor != null && bodyColor.alpha > 0.0f) {
			//if given bodyTexture is null or empty, then just use a solid color
			if(bodyTexture != null && bodyTexture.length() > 0) {
				int[] crop = atlas.getElement(bodyTexture);
				assert crop != null : "Body texture not found!";
				
				DrawableBitmap drawable = new DrawableBitmap(this.texture, bodyColor, 1, 1);
				drawable.setCrop(crop[0], crop[1], crop[2], crop[3]);
				
				this.drawableBody = drawable;
			}
			else {
				this.drawableBody = new DrawableRect(bodyColor);
			}
		}
		else {
			this.drawableBody = null;
		}
						
		CornerData[] corners = new CornerData[4];
		BorderData[] borders = new BorderData[4];
		
		//fill in corner data
		for(int i = 0; i < 4; i++) {
			corners[i] = new CornerData(atlas, frames[i]);
		}
		
		corners[INDEX_CORNER_UPPER_LEFT].x = -corners[INDEX_CORNER_UPPER_LEFT].width;
		corners[INDEX_CORNER_LOWER_LEFT].x = -corners[INDEX_CORNER_LOWER_LEFT].width;
		
		corners[INDEX_CORNER_LOWER_LEFT].y = -corners[INDEX_CORNER_LOWER_LEFT].height;
		corners[INDEX_CORNER_LOWER_RIGHT].y = -corners[INDEX_CORNER_LOWER_RIGHT].height;
		
		//fill in border data
		for(int i = 0; i < 4; i++) {
			borders[i] = new BorderData(atlas, frames[i+4]);
		}
		
		borders[INDEX_BORDER_LEFT].x = -borders[INDEX_BORDER_LEFT].width;
		
		borders[INDEX_BORDER_BOTTOM].y = -borders[INDEX_BORDER_BOTTOM].height;
		
		this.corners = corners;
		this.borders = borders;
		
		scaleX = scaleY = 1.0f;
		anchorX = anchorY = 0.0f;
	}
			
	public void setBodySize(float width, float height) {
		if(bodyWidth != width) {
			bodyWidth = width;
			
			//adjust the corners at the right side			
			corners[INDEX_CORNER_LOWER_RIGHT].x = corners[INDEX_CORNER_UPPER_RIGHT].x = width;
			
			//adjust the horizontal borders
			adjustHorizontalBorders(INDEX_BORDER_TOP, width);
			adjustHorizontalBorders(INDEX_BORDER_BOTTOM, width);
			
			//adjust the right border
			borders[INDEX_BORDER_RIGHT].x = width;
		}
		
		if(bodyHeight != height) {
			bodyHeight = height;
			
			//adjust the corners at the top			
			corners[INDEX_CORNER_UPPER_LEFT].y = corners[INDEX_CORNER_UPPER_RIGHT].y = height;
			
			//adjust the vertical borders
			adjustVerticalBorders(INDEX_BORDER_LEFT, height);
			adjustVerticalBorders(INDEX_BORDER_RIGHT, height);
			
			//adjust the top border
			borders[INDEX_BORDER_TOP].y = height;
		}
	}
	
	public void setBodyLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void setScale(float x, float y) {
		scaleX = x;
		scaleY = y;
	}
	
	public void setAnchor(float x, float y) {
		anchorX = x;
		anchorY = y;
	}
	
	public float getBodyX() {
		return x;
	}
	
	public float getBodyY() {
		return y;
	}
	
	@Override
	public void reset() {
		scaleX = scaleY = 1.0f;
		anchorX = anchorY = 0.0f;
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		float x = this.x + anchorX*scaleX;
		float y = this.y + anchorY*scaleY;
		
		final RenderSystem rs = systemRegistry.renderSystem;
		
		//draw the body
		if(drawableBody != null) {
			rs.scheduleForDraw(drawableBody, x, y, bodyWidth*scaleX, bodyHeight*scaleY, 0, renderOrder);
		}
		
		//corners
		for(int i = 0; i < 4; ++i) {
			final CornerData corner = corners[i];
			final DrawableBitmap d = pool.allocate();
			d.texture = texture;
			System.arraycopy(corner.crop, 0, d.crop, 0, 4);
			d.anchorX = corner.x;
			d.anchorY = corner.y;
			d.width = corner.width;
			d.height = corner.height;
			
			rs.scheduleForDraw(d, x, y, scaleX, scaleY, 0, renderOrder);
		}
		
		//borders
		drawHorizontalBorder(rs, INDEX_BORDER_TOP, x, y);
		drawHorizontalBorder(rs, INDEX_BORDER_BOTTOM, x, y);
		
		drawVerticalBorder(rs, INDEX_BORDER_LEFT, x, y);
		drawVerticalBorder(rs, INDEX_BORDER_RIGHT, x, y);
	}
	
	private void drawHorizontalBorder(RenderSystem rs, int index, float x, float y) {
		final BorderData border = borders[index];
		
		if(border.borderRepeat) {
			for(float cellX = border.x; cellX < bodyWidth; cellX += border.cellSize) {
				final DrawableBitmap d = pool.allocate();
				d.texture = texture;
				System.arraycopy(border.crop, 0, d.crop, 0, 4);
				d.anchorX = cellX;
				d.anchorY = border.y;
				d.width = border.cellSize;
				d.height = border.height;
				
				rs.scheduleForDraw(d, x, y, scaleX, scaleY, 0, renderOrder);
			}
		}
		else {
			final DrawableBitmap d = pool.allocate();
			d.texture = texture;
			System.arraycopy(border.crop, 0, d.crop, 0, 4);
			d.anchorX = border.x;
			d.anchorY = border.y;
			d.width = bodyWidth;
			d.height = border.height;
			
			rs.scheduleForDraw(d, x, y, scaleX, scaleY, 0, renderOrder);
		}
	}
	
	private void drawVerticalBorder(RenderSystem rs, int index, float x, float y) {
		final BorderData border = borders[index];
		
		if(border.borderRepeat) {
			for(float cellY = border.y; cellY < bodyHeight; cellY += border.cellSize) {
				final DrawableBitmap d = pool.allocate();
				d.texture = texture;
				System.arraycopy(border.crop, 0, d.crop, 0, 4);
				d.anchorX = border.x;
				d.anchorY = cellY;
				d.width = border.width;
				d.height = border.cellSize;
				
				rs.scheduleForDraw(d, x, y, scaleX, scaleY, 0, renderOrder);
			}
		}
		else {
			final DrawableBitmap d = pool.allocate();
			d.texture = texture;
			System.arraycopy(border.crop, 0, d.crop, 0, 4);
			d.anchorX = border.x;
			d.anchorY = border.y;
			d.width = border.width;
			d.height = bodyHeight;
			
			rs.scheduleForDraw(d, x, y, scaleX, scaleY, 0, renderOrder);
		}
	}
	
	private void adjustHorizontalBorders(int index, float width) {
		BorderData b = borders[index];
		if(b.borderRepeat) {
			b.cellSize = width/b.width;
		}
		else {
			b.cellSize = width;
		}
	}
	
	private void adjustVerticalBorders(int index, float height) {
		BorderData b = borders[index];
		if(b.borderRepeat) {
			b.cellSize = height/b.height;
		}
		else {
			b.cellSize = height;
		}
	}

	private class CornerData {
		public final int[] crop;
		public final float width;
		public final float height;
		
		public float x;
		public float y;
		
		public CornerData(TextureAtlas atlas, FrameParameter frame) {
			int[] atlasCrop = atlas.getElement(frame.texture);
			assert atlasCrop != null : "Corner texture not found!";
						
			if(frame.flipH || frame.flipV) {
				int[] crop = atlasCrop.clone();
				
				if(frame.flipH) {
					crop[0] += crop[2];
					crop[2] = -crop[2];
				}
				
				if(frame.flipV) {
					crop[1] -= crop[3];
					crop[3] = -crop[3];
				}
				
				this.crop = crop;
			}
			else {
				crop = atlasCrop;
			}
			
			x = 0;
			y = 0;
			width = frame.width;
			height = frame.height;
		}
	}
	
	private class BorderData extends CornerData {
		public final boolean borderRepeat;
		
		public float cellSize;
		
		public BorderData(TextureAtlas atlas, FrameParameter frame) {
			super(atlas, frame);
			
			cellSize = 0;
			borderRepeat = frame.borderRepeat;
		}
	}
}
