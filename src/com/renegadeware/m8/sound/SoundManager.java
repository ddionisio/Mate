package com.renegadeware.m8.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;

/**
 * Manager for playing sounds. Do not use this for playing long samples (e.g. music)
 * 
 * @author ddionisio
 *
 */
public class SoundManager extends ResourceManager {
	public static final String Type = "sound";
	
	private static final int MAX_STREAMS = 8;
    private static final int MAX_SOUNDS = 1;
    
    SoundPool soundPool;
    AudioManager audioManager;
    
    boolean isSoundEnabled;

	public SoundManager() {
		super(MAX_SOUNDS);
		
		soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
		
		audioManager = (AudioManager)systemRegistry.contextParameters.context.getSystemService(Context.AUDIO_SERVICE);
		
		isSoundEnabled = true;
	}
	
	public synchronized void enableSound(boolean bYes) {
		isSoundEnabled = bYes;
	}
	
	/**
	 * Get the global audio volume from android.
	 * 
	 * @return [0.0, 1.0]
	 */
	public float getGlobalVolume() {
		return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}
	
	/* ******************
	 * Stream stuff
	 */
	
	public void pause(int streamId) {
		soundPool.pause(streamId);
	}
	
	public void resume(int streamId) {
		if(isSoundEnabled) {
			soundPool.resume(streamId);
		}
	}
	
	public void setLoop(int streamId, int loop) {
		soundPool.setLoop(streamId, loop);
	}
	
	public void setPriority(int streamId, int priority) {
		soundPool.setPriority(streamId, priority);
	}
	
	public void setRate(int streamId, float rate) {
		soundPool.setRate(streamId, rate);
	}
	
	public void setVolume(int streamId, float volume) {
		float v = volume*getGlobalVolume();
		
		soundPool.setVolume(streamId, v, v);
	}
	
	public void stop(int streamId) {
		soundPool.stop(streamId);
	}
			
	@Override
	public void reset() {
		super.reset();
		
		//release stuff
		soundPool.release();
		
		soundPool = null;
		
		audioManager = null;
	}

	@Override
	public int loadingOrder() {
		return DEFAULT_ORDER_AUDIO;
	}

	@Override
	public String name() {
		return Type;
	}

	@Override
	public String defType() {
		return "raw";
	}

	@Override
	protected Resource createImpl(int id, String group, boolean isManual,
			ManualResourceLoader loader, Resource.Param params) {
		Sound ret = new Sound(this, id, group, isManual, loader);
		ret.setParameters(params);
		
		return ret; 
	}

}
