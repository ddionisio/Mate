package com.renegadeware.m8.ui;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.MateActivity;
import com.renegadeware.m8.input.InputSystem;
import com.renegadeware.m8.input.InputXY;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.ObjectManager;
import com.renegadeware.m8.obj.PhasedObjectManager;
import com.renegadeware.m8.util.FixedSizeArray;

public class UISystem extends ObjectManager {
	public static final float DRAG_THRESHOLD = 3.5f;
	public static final float DRAG_THRESHOLD_SQ = DRAG_THRESHOLD*DRAG_THRESHOLD;
	
	private InputXY curTouch;
	private BaseUI curInputUI;
	private UIListener curListener;
	
	//set the maximum uis this object can process
	public UISystem(int capacity) {
		super(capacity);
	}
		
	/**
	 * Use this to intercept any ui with input enabled, but without a listener
	 * @param l
	 */
	public void setInputListener(UIListener l) {
		curListener = l;
	}
	
	@Override
	public void reset() {
		curTouch = null;
		curInputUI = null;
		curListener = null;
	}
	
	public void add(BaseUI ui, UIAnimator startAnimation) {
		if(startAnimation != null) {
			ui.setAnimator(startAnimation, true);
		}
		
		super.add(ui);
	}
	
	@Override
	public void remove(BaseObject object) {
		if(object == curInputUI) {
			curInputUI = null;
		}
		
        super.remove(object);
    }
	
	public void remove(BaseUI ui, UIAnimator endAnimation) {
		if(ui != null && endAnimation != null) {
			ui.setAnimator(endAnimation, true);
			ui._animatorRemoveUIOnEnd = true;
		}
	}
			
	public void removeLast(UIAnimator endAnimation) {
		BaseUI object = (BaseUI)getLast();
		if(object != null && endAnimation != null) {
			object.setAnimator(endAnimation, true);
			object._animatorRemoveUIOnEnd = true;
		}
	}
		
	private BaseUI _getInputUI(InputSystem uiSys) {
		BaseUI foundUI = null;
		
		final int count = getCount();
        if (count > 0) {
        	//go from front to back, ie. last to first
            final Object[] objectArray = getObjects().getArray();
            for (int i = count-1; i >= 0; i--) {
            	BaseUI ui = (BaseUI)objectArray[i];
            	
            	//break early if ui is modal to prevent flow
            	if(ui.inputEnabled && ui.visible && ui._animator == null) {
            		//grab touch based on ui's region if not available
            		if(curTouch == null) {
            			curTouch = ui.getTouch(uiSys);
            		}
            		
            		//otherwise check if we are still in region
            		if(curTouch != null && ui.isInRegion(curTouch.getX(), curTouch.getY(), false)) {
        				//check for any child in found ui, otherwise just set ui as current
        				BaseUI childUI = ui.getChildFromPoint(curTouch.getX(), curTouch.getY(), true);
        				
        				foundUI = childUI != null ? childUI : ui;
        				
        				//do an input callback
        				foundUI.inputTouchPressed(curTouch);
        				
        				if(foundUI.inputListener != null) {
        					foundUI.inputListener.inputTouchPressed(foundUI, curTouch);
        				}
        				else if(curListener != null) {
        					curListener.inputTouchPressed(foundUI, curTouch);
        				}
        			}
            	}
            	
            	if(foundUI != null || ui.modal) { //prevent processing other uis if this is modal
            		break;
            	}
            }
        }
        
        return foundUI;
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {	
		commitUpdates();
		
		//update the UIs
		final FixedSizeArray<BaseObject> objs = getObjects();
        final int count = objs.getCount();
        if (count > 0) {
            final Object[] objectArray = objs.getArray();
            for (int i = 0; i < count; i++) {
                BaseUI ui = (BaseUI)objectArray[i];
                
                ui._update(timeDelta);
				
        		//schedule ui render
        		//if we have an animator, call its update with given drawable and render order
        		if(ui.visible) {
        			final UIAnimator animator = ui._animator;
        			final BaseUI.UIDrawable drawable = ui._drawable;
        			
        			if(animator != null) {
        				animator.update(timeDelta, drawable, ui.phase, ui.x, ui.y);
        					
    					if(animator.isDone()) {
    						//wait for drawings to be done
    						((MateActivity)systemRegistry.contextParameters.context).getRenderer().waitDrawingComplete();
    						
    						//synchronized(ui) { //wait for any drawing of this ui
	    						drawable.resetCurAlpha();
	    						ui.animationComplete(animator);
	    						
	    						if(ui._animatorRemoveOnEnd) {
	    							ui._animator = null;
	    						}
	    						
	    						if(ui._animatorRemoveUIOnEnd) {
	    							remove(ui);
	    							ui._animatorRemoveUIOnEnd = false;
	    						}
    						//}
    					}
        			}
        			else {
        				systemRegistry.renderSystem.scheduleForDraw(drawable, ui.x, ui.y, 1.0f, 1.0f, 0.0f, ui.phase);
        			}
        		}
            }
        }
				
		//process input
		final InputSystem uiSys = BaseObject.systemRegistry.inputSystem;
		
		//check if we currently have a touch in process
		if(curTouch != null) {
			
			//make sure the ui still exists and that its input is still enabled
			//if not, get another ui
			if(curInputUI == null || !curInputUI.inputEnabled) {
				if(curTouch.getPressed()) {
					curInputUI = _getInputUI(uiSys);
					if(curInputUI == null) {
						//no ui found, reset
						curTouch = null;
					}
				}
				else {
					//touch is released, reset input
					curTouch = null;
				}
			}
			else if(curTouch.getPressed()) {
				//check for drag threshold based on pressed pos and current pos
				//then call for drag callback
				float dSq = curTouch.getDistanceSq();
				
				if(dSq >= DRAG_THRESHOLD_SQ ) {
					
					//reset input if drag call returns false
					if(!curInputUI.inputTouchDrag(curTouch, dSq)) {
						//DebugLog.w("Board", "loc: "+curTouch.getX()+", "+curTouch.getY());
						curInputUI = null;
					}
					else if(curInputUI.inputListener != null) {
						curInputUI.inputListener.inputTouchDrag(curInputUI, curTouch);
					}
					else if(curListener != null) {
						curListener.inputTouchDrag(curInputUI, curTouch);
					}
				}
			}
			else {
				//do input callback release
				curInputUI.inputTouchReleased(curTouch);
				
				if(curInputUI.inputListener != null) {
					curInputUI.inputListener.inputTouchReleased(curInputUI, curTouch);
				}
				else if(curListener != null) {
					curListener.inputTouchReleased(curInputUI, curTouch);
				}
				
				curTouch = null;
				curInputUI = null;
			}
		}
		else if(uiSys.touchGetTriggered(BaseObject.systemRegistry.timeSystem.getGameTime())) {
			//for now, just get one touch from any ui region
			//also, assuming uis are not sorted from smallest to largest region
			//and uis are not overlapping
			curTouch = null;
			
			curInputUI = _getInputUI(uiSys);
		}
    }
}
