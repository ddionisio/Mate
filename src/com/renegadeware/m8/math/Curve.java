package com.renegadeware.m8.math;

import java.util.Comparator;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.util.FixedSizeArray;

public class Curve {
	
	public enum LoopType {
		Constant,
		Cycle,
		CycleOffset,
		Linear,
		Oscillate
	}
	
	private static final PositionCompare comparator = new PositionCompare();
	private final CurveKey searchItem; 
	
	//TODO: comparator based on point
	private final FixedSizeArray<CurveKey> keys;
	
	public LoopType preLoop;
	public LoopType postLoop;
	
	public Curve(int capacity) {
		keys = new FixedSizeArray<CurveKey>(capacity);
		keys.setComparator(comparator);
		
		searchItem = new CurveKey();
	}
			
	public float evaluate(float position) {
		float result = 0;
		
		final int count = keys.getCount();
		
		if(count > 0) {
			final Object[] array = keys.getArray();
			
			keys.sort(false);
			
			final CurveKey begin = (CurveKey)array[0];
			
			if(count == 1) {
				 return begin.value;
			}
			
			final CurveKey end = (CurveKey)array[count-1];
			
			if(position < begin.position) {
				switch(preLoop) {
				case Linear:
					return begin.value + position*begin.tangentIn;
				default:
					//constant
					return begin.value;
				}
			}
			else if(position > end.position) {
				switch(postLoop) {
				case Linear:
					return begin.value + position*begin.tangentOut;
				default:
					//constant
					return end.value;
				}
			}
			else {
				searchItem.position = position;								
				int i = keys.find(searchItem, comparator);
				
				if(i < 0) {
					float ti2;//, ti1;
					float to1;//, to2;
					float v1,v2;//,v0,v3;
					float ds, de;
					
					i = -(i + 1) - 1;
					
					CurveKey p = i < count ? (CurveKey)array[i] : end;
					
					if(p.continuity == CurveKey.Continuity.Step) {
						return p.value;
					}
					
					//ti1 = p.tangentIn;
					to1 = p.tangentOut;
					v1 = p.value;
					
					ds = p.position;
					
					//get values for i-1
					/*if(i-1 < 0) {
						switch(preLoop) {
						default:
							v0 = begin.value;
							break;
						}
					}
					else {
						v0 = ((CurveKey)array[i-1]).value;
					}*/
										
					//get values for i+1
					if(i+1 >= count) {
						switch(preLoop) {
						default:
							ti2 = end.tangentIn;
							//to2 = end.tangentOut;
							v2 = end.value;
							de = end.position;
							break;
						}
					}
					else {
						CurveKey p1 = (CurveKey)array[i+1];
						ti2 = p1.tangentIn;
						//to2 = p1.tangentOut;
						v2 = p1.value;
						de = p1.position;
					}
					
					//get values for i+1
					/*if(i+2 >= count) {
						switch(preLoop) {
						default:
							v3 = end.value;
							break;
						}
					}
					else {
						v3 = ((CurveKey)array[i+2]).value;
					}*/
					
					//float d1 = ti1*(v1-v0) + to1*(v2-v1);
					//float d2 = ti2*(v2-v1) + to2*(v3-v2);
					
					float t = ds == de ? 0 : (position-ds)/(de-ds);
					
					//return Math.hermite(v1, d1, v2, d2, t);
					return Math.hermite(v1, to1, v2, ti2, t);
				}
				else {
					return keys.get(i).value;
				}
			}
		}
		
		return result;
	}
	
	public int getIndex(CurveKey key) {
		return keys.find(key, false);
	}
	
	public void add(CurveKey key) {
		keys.add(key);
	}
	
	public void remove(CurveKey key) {
		keys.remove(key, false);
	}
	
	public void remove(int index) {
		keys.remove(index);
	}
	
	public void computeTangent(int index, int type) {
		
	}
	
	public void computeTangent(int index, int typeIn, int typeOut) {
		
	}
	
	public void computeTangents(int type) {
		
	}
	
	public void computeTangents(int typeIn, int typeOut) {
		
	}
	
	private static class PositionCompare implements Comparator<CurveKey> {

		@Override
		public int compare(CurveKey object1, CurveKey object2) {
			int result = 0;
            if (object1 != null && object2 != null) {
            	float v = object1.position - object2.position;
            	if(object1.position == object2.position) {
            		result = 0;
            	}
            	else if(object1.position < object2.position) {
            		result = -1;
            	}
            	else {
            		result = 1;
            	}
            } else if (object1 == null && object2 != null) {
                result = 1;
            } else if (object2 == null && object1 != null) {
                result = -1;
            }
            return result;
		}
	}
	
	private static final String TAG_PRELOOP = "PreLoop";
	private static final String TAG_POSTLOOP = "PostLoop";
	private static final String TAG_KEYS = "Keys";
	
	public static final Curve load(int resourceXML) {
		final Context context = BaseObject.systemRegistry.contextParameters.context;
		assert context != null;
		
		Curve curve = null;
		
		LoopType preLoop = LoopType.Constant, postLoop = LoopType.Constant;
		
		XmlResourceParser xml = context.getResources().getXml(resourceXML);
		try {
			String tag = "";
			
			for(int eventType = xml.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xml.next()) {
				if(eventType == XmlPullParser.START_TAG) {
					tag = xml.getName();
				}
				else if(eventType == XmlPullParser.TEXT) {
					if(tag.compareTo(TAG_KEYS) == 0) {
						String[] params = xml.getText().split("\\s+");
						int len = params.length;
						
						if(len > 0 && len % 5 == 0) {
							if(curve == null) {
								curve = new Curve(len/5);
							}
							
							for(int i = 0; i < len; i+=5) {
								CurveKey key = new CurveKey();
								
								key.position = Float.parseFloat(params[i]);
								key.value = Float.parseFloat(params[i+1]);
								key.tangentIn = Float.parseFloat(params[i+2]);
								key.tangentOut = Float.parseFloat(params[i+3]);
								
								if(params[i].compareTo(CurveKey.Continuity.Step.name()) == 0) {
									key.continuity = CurveKey.Continuity.Step;
								}
								
								curve.add(key);
							}
						}
						else {
							throw new Exception("Curve keys expecting 5 parameters, found only: "+len);
						}
					}
					else {
						int loop = 0;
						
						if(tag.compareTo(TAG_PRELOOP) == 0) {
							loop = 1;
						}
						else if(tag.compareTo(TAG_POSTLOOP) == 0) {
							loop = 2;
						}
						
						LoopType t = LoopType.Constant;
						
						if(loop != 0) {
							String txt = xml.getText();
							if(txt.compareTo(LoopType.Cycle.name()) == 0) {
								t = LoopType.Cycle;
							}
							else if(txt.compareTo(LoopType.CycleOffset.name()) == 0) {
								t = LoopType.CycleOffset;
							}
							else if(txt.compareTo(LoopType.Linear.name()) == 0) {
								t = LoopType.Linear;
							}
							else if(txt.compareTo(LoopType.Oscillate.name()) == 0) {
								t = LoopType.Oscillate;
							}
							
							switch(loop) {
							case 1:
								preLoop = t;
								break;
								
							case 2:
								postLoop = t;
								break;
							}
						}
					}
				}
				else if(eventType == XmlPullParser.END_TAG) {
					tag = "";
				}
			}
			
		} catch (Exception e) {
			DebugLog.e("Curve", e.toString(), e);
		}
		finally {
			xml.close();
		}
		
		if(curve != null) {
			curve.preLoop = preLoop;
			curve.postLoop = postLoop;
		}
		
		return curve;
	}
}
