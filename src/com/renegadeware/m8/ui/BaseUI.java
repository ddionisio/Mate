package com.renegadeware.m8.ui;

import java.lang.ref.WeakReference;

import com.renegadeware.m8.gfx.DrawableObject;
import com.renegadeware.m8.input.InputSystem;
import com.renegadeware.m8.input.InputXY;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.PhasedObject;
import com.renegadeware.m8.util.DataSection;
import com.renegadeware.m8.util.FixedSizeArray;
import com.renegadeware.m8.util.Util;
import com.renegadeware.m8.util.XmlSection;

public class BaseUI extends PhasedObject {
	
	public static final int ANCHOR_LEFT = 0;
	public static final int ANCHOR_RIGHT = 1;
	public static final int ANCHOR_BOTTOM = 0;
	public static final int ANCHOR_TOP = 1;
	public static final int ANCHOR_CENTER = 2;
	public static final int ANCHOR_STRETCH = 3;
	
	public static final BaseUI createUIFromDataSection(DataSection ds) throws Exception {
		//instantiate ui
		BaseUI ui = null;
		
		//check to see if ui is from another file
		String file = ds.getAttributeAsString("file", 0);
		if(file != null && file.length() > 0) {
			DataSection fileDs = XmlSection.createFromResourceId(systemRegistry.contextParameters.context, Util.getResourceIdByName(file));
			
			//possible override from current ds
			fileDs.copySections(ds);
			
			ui = createUIFromDataSection(fileDs);
		}
		else {
			//allocate ui based on class attribute, this has to exist
			String uiClassName = ds.getAttributeAsString("class", 0);
			if(uiClassName.length() > 0) {
				Object newObj = Util.instantiateClass(uiClassName);
				if(newObj == null) {
					throw new Exception("createUIFromDataSection: Unable to initialize "+uiClassName);
				}
				else if(!(newObj instanceof BaseUI)) {
					throw new Exception("createUIFromDataSection: "+uiClassName+" is not an instance of BaseUI");				
				}
				
				ui = (BaseUI)newObj;
			}
			else {
				//just assume a standard base ui
				ui = new BaseUI();
			}
			
			initUIFromDataSection(ui, ds);
		}
		
		return ui;
	}
	
	public static final void initUIFromDataSection(BaseUI ui, DataSection ds) throws Exception {
		//read the ui sub tags to get the ui's data and layout
		for(DataSection sds : ds) {
			if(sds.sectionName().compareTo("Data") == 0) {
				//read the UI data from this section
				sds.readObjectFields(ui);
			}
			else if(sds.sectionName().compareTo("Layout") == 0) {
				//allocate the layout
				ui.setLayout(createLayoutFromDataSection(sds));
			}
		}
	}
	
	public static final BaseLayout createLayoutFromDataSection(DataSection ds) throws Exception {
		BaseLayout layout = null;
		
		//allocate layout based on class attribute
		String layoutClassName = ds.getAttributeAsString("class", 0);
		if(layoutClassName.length() > 0) {
			Object newObj = Util.instantiateClass(layoutClassName);
			if(!(newObj instanceof BaseLayout)) {
				throw new Exception("createLayoutFromDataSection: "+layoutClassName+" is not an instance of BaseLayout");				
			}
			
			layout = (BaseLayout)newObj;
		}
		else {
			//assume a base layout
			layout = new BaseLayout();
		}
			
		DataSection childrenDs = null;
		
		//go through layout's data section
		for(DataSection sds : ds) {
			if(sds.sectionName().compareTo("Data") == 0) {
				//read the layout data from this section
				sds.readObjectFields(layout);
			}
			else if(sds.sectionName().compareTo("Children") == 0) {
				childrenDs = sds;
			}
		}
		
		//get layout children and create the sub ui
		//for each new ui, add to layout
		if(childrenDs != null) {
			//set the children container capacity for the layout if applicable
			layout.initImpl(childrenDs.countChildren());
			if(layout.getChildren() == null) {
				layout.setCapacity(childrenDs.countChildren());
			}
			
			for(DataSection sds : childrenDs) {
				//int index = sds.getAttributeAsInt("index", layout.getNumChildren());
				
				BaseUI childUI = createUIFromDataSection(sds);
				if(childUI != null) {
					layout.addChild(childUI);
				}
			}
			
			layout.updateCommit();
		}
		else {
			layout.initImpl(1);
			if(layout.getChildren() == null) {
				layout.setCapacity(1);
			}
		}
		
		return layout;
	}
	
	public static void LoadUI(BaseUI ui) {
		//call the ui load then go through layout's children to load them
		//afterward, refresh layout
		
		BaseLayout layout = ui.getLayout();
		if(layout != null) {
			FixedSizeArray<BaseUI> children = layout.getChildren();
			int count = children.getCount();
			Object[] objects = children.getArray();
			for(int i = 0; i < count; i++) {
				BaseUI cui = (BaseUI)objects[i];
				LoadUI(cui);
			}
			
			layout.doRefresh = true;
		}
		
		ui.load();
	}
	
	public float x;
	public float y;
	
	protected String name;
	
	private float width;
	private float height;
	
	protected int anchorH;
	protected int anchorV;
	
	protected float layoutSpacing;
	
	protected int index; //used by layout
	
	private BaseLayout _layout;
	
	protected UIListener inputListener;
	
	final UIDrawable _drawable;
	
	protected boolean inputEnabled;
	protected boolean visible;
	protected boolean modal;
	
	UIAnimator _animator;
	boolean _animatorRemoveOnEnd;
	boolean _animatorRemoveUIOnEnd;
			
	protected WeakReference<BaseUI> _parent;
			
	public BaseUI() {
		super();
		
		name = "";
		_drawable = new UIDrawable();
		index = -1;
		visible = true;
	}
		
	public final String getName() {
		return name;
	}
	
	public final BaseUI getParent() {
		return _parent != null ? _parent.get() : null;
	}
	
	public final void setLayout(BaseLayout newLayout) {
		//remove reference to this ui from previous layout, if it exists
		if(_layout != null) {
			_layout._parent = null;
		}
		
		_layout = newLayout;
		
		if(_layout != null) {
			_layout._parent = new WeakReference<BaseUI>(this);
			_layout._refreshChildrenParent();
			_layout.doRefresh = true;
		}
	}
	
	public final BaseLayout getLayout() {
		return _layout;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(BaseLayout parentLayout, int i) {
		index = i;
		parentLayout.doRefresh = true;
	}
	
	/**
	 * Enable input process for this ui.  This will check for input for itself and
	 * propagate it to its children.
	 * 
	 * @param enable
	 */
	public void enableInput(boolean enable) {
		inputEnabled = enable;
	}
	
	public boolean isInputEnabled() {
		return inputEnabled;
	}
	
	public final void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public final void setModal(boolean enable) {
		this.modal = enable;
	}
	
	public final UIListener getInputListener() {
		return inputListener;
	}
	
	public final void setInputListener(UIListener listener) {
		inputListener = listener;
	}
	
	public final int getAnchorH() {
		return anchorH;
	}
	
	public final int getAnchorV() {
		return anchorV;
	}
	
	public final void setAnchor(int horizontal, int vertical) {
		anchorH = horizontal;
		anchorV = vertical;
	}
	
	public final float getWidth() {
		return width;
	}
	
	public final float getHeight() {
		return height;
	}
	
	//returns the actual X based on anchor
	public final float getAdjustedX() {
		switch(anchorH) {
		case ANCHOR_CENTER:
			return x - width*0.5f;
		case ANCHOR_RIGHT:
			return x - width;
		default:
			return x;
		}
	}
	
	//returns the actual Y based on anchor
	public final float getAdjustedY() {
		switch(anchorV) {
		case ANCHOR_CENTER:
			return y - height*0.5f;
		case ANCHOR_TOP:
			return y - height;
		default:
			return y;
		}
	}
	
	public final float getAbsoluteX() {
		float x = getAdjustedX();
		
		for(BaseUI parent = getParent(); parent != null; parent = parent.getParent()) {
			x += parent.getAdjustedX();
		}
		
		return x;
	}
	
	public final float getAbsoluteY() {
		float y = getAdjustedY();
		
		for(BaseUI parent = getParent(); parent != null; parent = parent.getParent()) {
			y += parent.getAdjustedY();
		}
		
		return y;
	}
	
	public final void refreshLayout() {
		if(_layout != null) {
			_layout.doRefresh = true;
		}
	}
	
	public final BaseUI findChild(String name, boolean recursive) {
		return _layout != null ? _layout.findChild(name, recursive) : null;
	}
	
	//gets a child from given point
	public final BaseUI getChildFromPoint(float x, float y, boolean checkInput) {		
		if(_layout != null) {
			//localize the position relative to our region
			final float rx = x - getAdjustedX();
			final float ry = y - getAdjustedY();
			
			final FixedSizeArray<BaseUI> children = _layout.getChildren();
			final int count = children.getCount();
			final Object[] objects = children.getArray();
				
			for(int i = 0; i < count; i++) {
				BaseUI child = (BaseUI)objects[i];
				
				//check if it fits
				if(child.isInRegion(rx, ry, false)) {
					//if child has a layout, then better see if there is a hit
					//otherwise, just return this child
					BaseUI cRet = child.getChildFromPoint(rx, ry, checkInput);
					
					if(cRet != null && (!checkInput || cRet.inputEnabled)) {
						return cRet;
					}
					else if(!checkInput || child.inputEnabled) {
						return child;
					}
				} 
			}
		}
		
		return null;
	}
	
	/**
	 * Get the largest width of this ui, the result can be greater than the ui's if it has children.
	 * @return maximum width
	 */
	public final float getMaxWidth() {
		float max = width;
		
		if(_layout != null) {
			final FixedSizeArray<BaseUI> children = _layout.getChildren();
			final int count = children.getCount();
			final Object[] objects = children.getArray();
				
			for(int i = 0; i < count; i++) {
				BaseUI child = (BaseUI)objects[i];
				float w = child.getWidth();
				if(w > max) {
					max = w;
				}
			}
		}
		
		return max;
	}
	
	/**
	 * Get the largest height of this ui, the result can be greater than the ui's if it has children.
	 * @return maximum height
	 */
	public final float getMaxHeight() {
		float max = height;
		
		if(_layout != null) {
			final FixedSizeArray<BaseUI> children = _layout.getChildren();
			final int count = children.getCount();
			final Object[] objects = children.getArray();
				
			for(int i = 0; i < count; i++) {
				BaseUI child = (BaseUI)objects[i];
				float h = child.getHeight();
				if(h > max) {
					max = h;
				}
			}
		}
		
		return max;
	}
	
	public boolean isInRegion(float x, float y, boolean useAbsolute) {
		float ax = getAdjustedX(), ay = getAdjustedY();
		
		if(useAbsolute) {
			for(BaseUI parent = getParent(); parent != null; parent = parent.getParent()) {
				ax += parent.getAdjustedX();
				ay += parent.getAdjustedY();
			}
		}
		
		return ax <= x && x <= ax+width && ay <= y && y <= ay+height;
	}
	
	protected final InputXY getTouch(InputSystem inputSys) {
		//go through children
		
		//get adjusted x,y for region
		return inputSys.touchFindPointerInRegion(getAdjustedX(), getAdjustedY(), width, height);
	}
	
	public final void setAlpha(float a) {
		_drawable.setAlpha(a);
	}
	
	public final float getAlpha() {
		return _drawable.getAlpha();
	}
	
	/**
	 * This will set the ui's animator and start it.
	 * 
	 * @param a The animation to set
	 * @param removeOnEnd true if we want the animator to be set to null on end
	 */
	public final void setAnimator(UIAnimator a, boolean removeOnEnd) {
		_animator = a;
		if(_animator != null) {
			_drawable.resetCurAlpha();
			_animator.start();
		}
		
		_animatorRemoveOnEnd = removeOnEnd;
	}
	
	public final UIAnimator getAnimator() {
		return _animator;
	}
	
	public void doLayoutUpdate() {
		final BaseLayout layout = _layout;
		if(layout != null) {
			layout.updateCommit();
			
			layout.updateRefresh(this);
			
			final FixedSizeArray<BaseUI> children = layout.getChildren();
			final int count = children.getCount();
			
			if(count > 0) {
				final Object[] objects = children.getArray();
				
				for(int i = 0; i < count; i++) {
					((BaseUI)objects[i]).doLayoutUpdate();
				}
			}
		}
	}
	
	protected final void _update(float timeDelta) {
		update(timeDelta);
		
		final BaseLayout layout = _layout;
		if(layout != null) {
			layout.updateCommit();
			
			layout.updateRefresh(this);
			
			final FixedSizeArray<BaseUI> children = layout.getChildren();
			final int count = children.getCount();
			
			if(count > 0) {
				final Object[] objects = children.getArray();
				
				for(int i = 0; i < count; i++) {
					((BaseUI)objects[i])._update(timeDelta);
				}
			}
		}
	}
	
	/**
	 * This is not used
	 */
	@Override
    public final void update(float timeDelta, BaseObject parent) {
	}
	
	@Override
	public final synchronized void reset() {
		if(_layout != null) {
			_layout.reset();
		}
		
		unload();
		
		inputListener = null;
		
		_animator = null;
		_animatorRemoveOnEnd = false;
		_animatorRemoveUIOnEnd = false;
	}
	
	//////////////////////////////////////////////
	// Implementations you should fill up
	
	public void load() {
	}
	
	public void unload() {
	}
		
	public void resize(float w, float h) {
		if(_layout != null && (width != w || height != h)) {
			_layout.doRefresh = true;
			
			BaseUI parent = getParent();
			if(parent != null && parent._layout != null) {
				parent._layout.doRefresh = true;
			}
		}
						
		width = w;
		height = h;
	}
	
	protected void update(float timeDelta) {
	}
			
	//you'll want to directly call any drawable's render function here directly
	//or do something manually
	//note: this is called within the render thread
	protected void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
	}
	
	protected void inputTouchPressed(InputXY input) {
	}
	
	protected void inputTouchReleased(InputXY input) {
	}
	
	/**
	 * Drag call by the ui system when input is moving.  Return false if you want to cancel
	 * this ui from input.
	 * 
	 * @param input The input data from the input system.
	 * @param distanceSq the distance squared from original press to current location
	 * @return false if we want to cancel this ui from input.
	 */
	protected boolean inputTouchDrag(InputXY input, float distanceSq) {
		return false;
	}
	
	protected void animationComplete(UIAnimator anim) {
		
	}
	
	public final class UIDrawable extends DrawableObject {
		
		private float alpha; // this is generally constant
		
		// only used for animation and children, a child's curAlpha is overridden
		// by its parent
		private float curAlpha;
		
		public UIDrawable() {
			curAlpha = alpha = 1.0f;
		}
		
		public void setCurAlpha(float a) {
			curAlpha = a;
		}
		
		public float getCurAlpha() {
			return curAlpha;
		}
		
		public void setAlpha(float a) {
			alpha = curAlpha = a;
		}
		
		public float getAlpha() {
			return alpha;
		}
		
		protected void resetCurAlpha() {
			curAlpha = alpha;
		}

		@Override
		public void draw(float x, float y, float scaleX, float scaleY,
				float rotate, float screenScaleX, float screenScaleY) {
			if(scaleX == 0.0f || scaleY == 0.0f) {
				return;
			}
			
			final BaseUI ui = BaseUI.this;
			//synchronized with this ui, to avoid conflict such as unload, adding children
			synchronized(ui) {
				//adjust location based on anchor
				float adjustedX = x;
				float adjustedY = y;
				float width = ui.getWidth();
				float height = ui.getHeight();
				
				switch(anchorH) {
				case ANCHOR_CENTER:
					adjustedX -= width*0.5f*scaleX;
					break;
				case ANCHOR_RIGHT:
					adjustedX -= width*scaleX;
					break;
				}
				
				switch(anchorV) {
				case ANCHOR_CENTER:
					adjustedY -= height*0.5f*scaleY;
					break;
				case ANCHOR_TOP:
					adjustedY -= height*scaleY;
					break;
				}
				
				//call this ui's rendering
				if(curAlpha > 0.0f) {
					ui.render(adjustedX, adjustedY, scaleX, scaleY, rotate, screenScaleX, screenScaleY, curAlpha);
					
					//render the layout's children
					final BaseLayout _layout = ui._layout;
					if(_layout != null) {
						final FixedSizeArray<BaseUI> children = _layout.getChildren();
						final int count = children.getCount();
						
						if(count > 0) {
							final Object[] objects = children.getArray();
							
							for(int i = 0; i < count; i++) {
								BaseUI cui = (BaseUI)objects[i];
								if(cui.visible) {
									cui._drawable.curAlpha = curAlpha*cui._drawable.alpha;
									
									cui._drawable.draw(
											adjustedX + cui.x*scaleX,
											adjustedY + cui.y*scaleY, 
											scaleX, scaleY, 
											rotate, 
											screenScaleX, screenScaleY);
								}
							}
						}
					}
				}
			}
		}

		@Override
		public void draw(float[] mtx, float screenScaleX, float screenScaleY) {
			//this should never be called!
		}
	}
}
