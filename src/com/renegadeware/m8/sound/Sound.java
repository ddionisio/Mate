package com.renegadeware.m8.sound;

import android.media.SoundPool;

import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;

public class Sound extends Resource {
	public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_HIGH = 2;
    public static final int PRIORITY_MUSIC = 3;
    
    private int soundId;    

	public Sound(ResourceManager creator, int id, String group,
			boolean isManual, ManualResourceLoader loader) {
		super(creator, id, group, isManual, loader);
	}
	
	/**
	 * Play the sound with settings from param if valid. Otherwise it just plays it in default settings.
	 * 
	 * @return The stream, 0 if failed.
	 */
	public int play() {
		int stream = 0;
		
		SoundManager mgr = (SoundManager)getCreator();
		
		synchronized(mgr) {
			if(mgr.isSoundEnabled) {
				SoundPool soundPool = mgr.soundPool;
				
				if(params == null) {
					float v = mgr.getGlobalVolume();
					
					stream = soundPool.play(soundId, v, v, PRIORITY_NORMAL, 0, 1.0f);
				}
				else {
					SoundParam p = (SoundParam)params;
					
					float v = p.volume * mgr.getGlobalVolume();
					
					stream = soundPool.play(soundId, v, v, p.priority, p.loop, p.rate);
				}
			}
			
			return stream;
		}
	}
	
	/**
	 * Play the sound with given settings.
	 * 
	 * @param volume [0.0, 1.0]
	 * @param priority 0 = lowest, use PRIORITY_*
	 * @param loop -1 = infinite loop, 0 = play once, > 1 = play this many times
	 * @param rate [0.5, 2.0]
	 * @return
	 */
	public int play(float volume, int priority, int loop, float rate) {
		SoundManager mgr = (SoundManager)getCreator();
		
		synchronized(mgr) {
			if(mgr.isSoundEnabled) {
				return mgr.soundPool.play(soundId, volume, volume, priority, loop, rate);
			}
			else {
				return 0;
			}
		}
	}

	@Override
	protected void loadImpl() {
		SoundPool soundPool = ((SoundManager)getCreator()).soundPool;
		
		soundId = soundPool.load(systemRegistry.contextParameters.context, id, 1);
	}

	@Override
	protected void unloadImpl() {
		SoundPool soundPool = ((SoundManager)getCreator()).soundPool;
		
		soundPool.unload(soundId);
	}

	@Override
	protected int calculateSize() {
		return 0;
	}

}
