package com.renegadeware.m8;

import com.renegadeware.m8.gfx.MateRenderer;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.ObjectManager;

//serves as root for all components
public class Mate extends ObjectManager {			
	private boolean isRunning;
	private boolean isBootstrapComplete;
	
	private MateThread gameThread;
	private Thread game;
	
	private TimeSystem gameTime;
	
	public Mate() {
		super();
		
		isRunning = false;
		isBootstrapComplete = false;
	}
	
	public void bootstrap(MateRenderer renderer) {
		
		gameTime = new TimeSystem();
		systemRegistry.timeSystem = gameTime;
		systemRegistry.registerForReset(gameTime);
		
    	if(!isBootstrapComplete) {
    		    		
			gameThread = new MateThread(renderer);
			gameThread.setRoot(this);
			
			isBootstrapComplete = true;
    	}
    	
    }
	
	@Override
    public void update(float timeDelta, BaseObject parent) {
		gameTime.update(timeDelta, parent);
        final float newTimeDelta = gameTime.getFrameDelta();  // The time system may warp time.
        super.update(newTimeDelta, parent);
    }
	
	public void start() {
		if(!isRunning) {
			assert game == null;
			
			// run the garbage collect
			Runtime r = Runtime.getRuntime();
			r.gc();
			
			DebugLog.d("Mate", "Start!");
			game = new Thread(gameThread);
			game.setName("Mate");
			game.start();
			isRunning = true;
			
			AllocationGuard.sGuardActive = false;
		}
		else {
			gameThread.resume();
		}
	}
	
	public void pause() {
		if(isRunning) {
			gameThread.pause();
		}
	}
	
	public void resume() {
		if(isRunning) {
			gameThread.resume();
		}
	}
	
	public void stop() {
		if(isRunning) {
			DebugLog.d("Mate", "Stop!");
			
			if(gameThread.isPaused()) {
				gameThread.resume();
			}
			
			gameThread.stop();
			
			try {
				game.join();
			} catch(InterruptedException e) {
				game.interrupt();
			}
			
			game = null;
			isRunning = false;
			
			AllocationGuard.sGuardActive = false;
		}
	}
	
	public boolean isBootstrapComplete() {
		return isBootstrapComplete;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public boolean isPaused() {
		return isRunning && gameThread != null && gameThread.isPaused();
	}
}
