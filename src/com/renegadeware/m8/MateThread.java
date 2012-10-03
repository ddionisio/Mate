/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.renegadeware.m8;

import com.renegadeware.m8.gfx.MateRenderer;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.ObjectManager;

import android.os.SystemClock;

/** 
 * The GameThread contains the main loop for the game engine logic.  It invokes the game graph,
 * manages synchronization of input events, and handles the draw queue swap with the rendering
 * thread.
 */
public class MateThread implements Runnable {
    private long mLastTime;
    
    private ObjectManager root;
    private MateRenderer renderer;
    private boolean finished;
    private boolean pause = false;
    private Object pauseLock;
    private int profileFrames;
    private long profileTime;
    
    private static final float PROFILE_REPORT_DELAY = 3.0f;
    
    public MateThread(MateRenderer renderer) {
        mLastTime = SystemClock.uptimeMillis();
        this.renderer = renderer;
        finished = false;
        pause = false;
        pauseLock = new Object();
    }

    public void run() {
        mLastTime = SystemClock.uptimeMillis();
        finished = false;
        while (!finished) {
            if (root != null) {
                //mRenderer.waitDrawingComplete();
                
                final long time = SystemClock.uptimeMillis();
                final long timeDelta = time - mLastTime;
                long finalDelta = timeDelta;
                if (timeDelta > 12) {
                    float secondsDelta = (time - mLastTime) * 0.001f;
                    if (secondsDelta > 0.1f) {
                        secondsDelta = 0.1f;
                    }
                    mLastTime = time;
    
                    root.update(secondsDelta, null);
                        
                    BaseObject.systemRegistry.renderSystem.swap(renderer);
                                        
                    final long endTime = SystemClock.uptimeMillis();
                    
                    finalDelta = endTime - time;
                    
                    profileTime += finalDelta;
                    profileFrames++;
                    if (profileTime > PROFILE_REPORT_DELAY * 1000) {
                        final long averageFrameTime = profileTime / profileFrames;
                        DebugLog.d("Game Profile", "Average: " + averageFrameTime);
                        profileTime = 0;
                        profileFrames = 0;
                        //mGameRoot.sSystemRegistry.hudSystem.setFPS(1000 / (int)averageFrameTime);
                    }
                }
                // If the game logic completed in less than 16ms, that means it's running
                // faster than 60fps, which is our target frame rate.  In that case we should
                // yield to the rendering thread, at least for the remaining frame.
               
                if (finalDelta < 16) {
                    try {
                        Thread.sleep(16 - finalDelta);
                    } catch (InterruptedException e) {
                        // Interruptions here are no big deal.
                    }
                }
                
                synchronized(pauseLock) {
                    if (pause) {
                    	/*SoundSystem sound = BaseObject.sSystemRegistry.soundSystem;
                    	if (sound != null) {
                    		sound.pauseAll();
                    		BaseObject.sSystemRegistry.inputSystem.releaseAllKeys();
                    	}*/
                        while (pause) {
                            try {
                            	pauseLock.wait();
                            } catch (InterruptedException e) {
                                // No big deal if this wait is interrupted.
                            }
                        }
                    }
                }
            } 
        }
        // Make sure our dependence on the render system is cleaned up.
        //BaseObject.sSystemRegistry.renderSystem.emptyQueues(mRenderer);
    }

    public void stop() {
    	synchronized (pauseLock) {
            pause = false;
            finished = true;
            pauseLock.notifyAll();
    	}
    }
    
    public void pause() {
        synchronized (pauseLock) {
            pause = true;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            pause = false;
            pauseLock.notifyAll();
        }
    }
    
    public boolean isPaused() {
        return pause;
    }

    public void setRoot(ObjectManager root) {
        this.root = root;
    }
    
}
