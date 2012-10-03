package com.renegadeware.m8.gfx;

import com.renegadeware.m8.res.Resource;

public final class GridTextParam implements Resource.Param {
	public String fontIdStr; //you can use this in lieu of fontId, otherwise this is null
	public int fontId;
	public int alignment;
	public float pointSize;
	public float maxWidth;
	public Object[] stringParams;
}
