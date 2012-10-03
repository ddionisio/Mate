package com.renegadeware.m8.gfx;

import android.graphics.BitmapFactory;

import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;

public class TextureManager extends ResourceManager {
	public static final String Type = "texture";
	
	static final int DEFAULT_SIZE = 512;
	
	BitmapFactory.Options bitmapOptions;
	
	public TextureManager() {
		super(DEFAULT_SIZE);
	}
		
	@Override
	public int loadingOrder() {
		return DEFAULT_ORDER_TEXTURE;
	}

	@Override
	public String name() {
		return Type;
	}

	@Override
	public String defType() {
		return "drawable";
	}

	@Override
	protected Resource createImpl(int id, String group, boolean isManual,
			ManualResourceLoader loader, Resource.Param createParams) {
		return new Texture(this, id, group, isManual, loader);
	}
}
