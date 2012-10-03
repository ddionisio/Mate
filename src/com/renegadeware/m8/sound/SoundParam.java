package com.renegadeware.m8.sound;

import com.renegadeware.m8.res.Resource;

public final class SoundParam implements Resource.Param {
	public float volume = 1.0f; //range [0.0, 1.0]
	public int priority = Sound.PRIORITY_NORMAL; //0 = lowest
	public int loop = 0; //-1 = infinite loop
	public float rate = 1.0f; //range [0.5, 2.0]
}
