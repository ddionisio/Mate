package com.renegadeware.m8.gfx;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.renegadeware.m8.ContextParameters;
import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.MateInterface;
import com.renegadeware.m8.R;
import com.renegadeware.m8.gfx.RenderSystem.RenderElement;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.ObjectManager;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceGroupManager;
import com.renegadeware.m8.res.Resource.ResourceException;
import com.renegadeware.m8.util.FixedSizeArray;

import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.SystemClock;

public class MateRenderer implements GLSurfaceView.Renderer {
	public static final int MAX_SURFACE_READY_CALLBACKS = 64;
	
	private static final int PROFILE_REPORT_DELAY = 3000;
	
	private int width;
    private int height;
    
    private float scaleX;
    private float scaleY;
    private long lastTime;
    private int profileFrames;
    private long profileWaitTime;
    private long profileFrameTime;
    private long profileSubmitTime;
    private int profileObjectCount;
    
    private ObjectManager drawQueue;
    private boolean drawQueueChanged;
    private MateInterface game;
    private Object drawLock;
    
    private final FixedSizeArray<SurfaceReadyCallback> surfaceReadyCallbacks;
    
	public MateRenderer(MateInterface game, int gameWidth, int gameHeight) {
		this.game = game;
		width = gameWidth;
        height = gameHeight;
        scaleX = 1.0f;
        scaleY = 1.0f;
        drawQueueChanged = false;
        drawLock = new Object();
        
        surfaceReadyCallbacks = new FixedSizeArray<SurfaceReadyCallback>(MAX_SURFACE_READY_CALLBACKS);
	}
	
	private static final float GL_MAGIC_OFFSET = 0.375f;
	
	//this is a one time initialization of the quad during mate load
	public void init() {
		// Global texture quad, this will create if not yet found
		Grid textureQuad = (Grid)BaseObject.systemRegistry.gridManager.getById(R.id.mate_texture_quad);
		if(textureQuad == null) {
			textureQuad = BaseObject.systemRegistry.gridManager.create(R.id.mate_texture_quad, ResourceGroupManager.InternalResourceGroupName, 1, 1);
			textureQuad.addListener(new Resource.Listener() {
				public void loadingComplete(Resource res) {}
				public void preparingComplete(Resource res) {
					final float[][] positions = { 
							{0.0f, 0.0f, 0.0f}, 
							{1.0f, 0.0f, 0.0f},
							{0.0f, 1.0f, 0.0f}, 
							{1.0f, 1.0f, 0.0f}};
					
					final float u = 0;//GL_MAGIC_OFFSET/128.0f;
					final float v = 0;//GL_MAGIC_OFFSET/128.0f;
					final float u2 = 1;//(128.0f-GL_MAGIC_OFFSET)/128.0f;
					final float v2 = 1;//(128.0f-GL_MAGIC_OFFSET)/128.0f;

					final float[][] uvs = {
							{u, v2},
							{u2, v2},
							{u, v},
							{u2, v}};

					((Grid)res).set(0, 0, positions, uvs);
				}
				public void unloadingComplete(Resource res) {}
			});
		}
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		DebugLog.d("Mate", "onSurfaceCreated");
		
		OpenGLSystem.setGL(gl);
		
		/*
         * Some one-time OpenGL initialization can be made here probably based
         * on features of this particular context
         */
        //gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        /*
         * By default, OpenGL enables features that improve quality but reduce
         * performance. One might want to tweak that especially on software
         * renderer.
         */
        //gl.glDisable(GL10.GL_DITHER);
        gl.glDisable(GL10.GL_LIGHTING);

        //gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
                
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
       
        String extensions = gl.glGetString(GL10.GL_EXTENSIONS); 
        String version = gl.glGetString(GL10.GL_VERSION);
        String renderer = gl.glGetString(GL10.GL_RENDERER);
        boolean isSoftwareRenderer = renderer.contains("PixelFlinger");
        boolean isOpenGL10 = version.contains("1.0");
        boolean supportsDrawTexture = extensions.contains("draw_texture");
        // VBOs are standard in GLES1.1
        // No use using VBOs when software renderering, esp. since older versions of the software renderer
        // had a crash bug related to freeing VBOs.
        boolean supportsVBOs = !isSoftwareRenderer && (!isOpenGL10 || extensions.contains("vertex_buffer_object"));
        ContextParameters params = BaseObject.systemRegistry.contextParameters;
        params.supportsDrawTexture = supportsDrawTexture;
        params.supportsVBOs = supportsVBOs;
          
        hackBrokenDevices();
        
        DebugLog.i("MateRenderer", "Graphics Support: " + version + " (" + renderer + "): " +(supportsDrawTexture ?  "draw texture," : "") + (supportsVBOs ? "vbos" : ""));
                                        
        // texture, buffer reloading after restore
        try {
        	BaseObject.systemRegistry.textureManager.reloadAll(true);
        	BaseObject.systemRegistry.gridManager.reloadAll(true);
		} catch (ResourceException e) {
			DebugLog.e("MateRenderer", e.toString(), e);
		}
		
		//this should happen only once, and texture quad should be available
		Grid textureQuad = (Grid)BaseObject.systemRegistry.gridManager.getById(R.id.mate_texture_quad);
		if(textureQuad != null && !textureQuad.isLoaded()) {
			try {
				textureQuad.load(false);
			} catch(Exception e) {
				DebugLog.e("Mate", "Unable to load quad for rendering.");
			}
		}
						
		game.surfaceCreated();
		
		OpenGLSystem.setGL(null);
	}
	
	private void hackBrokenDevices() {
    	// Some devices are broken.  Fix them here.  This is pretty much the only
    	// device-specific code in the whole project.  Ugh.
        ContextParameters params = BaseObject.systemRegistry.contextParameters;

       
    	if (Build.PRODUCT.contains("morrison")) {
    		// This is the Motorola Cliq.  This device LIES and says it supports
    		// VBOs, which it actually does not (or, more likely, the extensions string
    		// is correct and the GL JNI glue is broken).
    		params.supportsVBOs = false;
    		// TODO: if Motorola fixes this, I should switch to using the fingerprint
    		// (blur/morrison/morrison/morrison:1.5/CUPCAKE/091007:user/ota-rel-keys,release-keys)
    		// instead of the product name so that newer versions use VBOs.
    	}
    }
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		DebugLog.d("Mate", "Surface Size Change: " + width + ", " + height);
        
        //mWidth = w;0
        //mHeight = h;
    	// ensure the same aspect ratio as the game
    	float scaleX = (float)width / this.width;
    	float scaleY =  (float)height / this.height;
    	//final int viewportWidth = (int)(this.width * scaleX);
    	//final int viewportHeight = (int)(this.height * scaleY);
        gl.glViewport(0, 0, width, height);
        this.scaleX = scaleX;
        this.scaleY = scaleY;

        
        /*
         * Set our projection matrix. This doesn't have to be done each time we
         * draw, but usually a new projection needs to be set when the viewport
         * is resized.
         */
        float ratio = (float) this.width / this.height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        
        OpenGLSystem.setGL(gl);
        
        game.surfaceReady();
        
        OpenGLSystem.setGL(null);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		long time = SystemClock.uptimeMillis();
        long time_delta = (time - lastTime);
        
        synchronized(drawLock) {
            if (!drawQueueChanged) {
                while (!drawQueueChanged) {
                    try {
                    	drawLock.wait();
                    } catch (InterruptedException e) {
                        // No big deal if this wait is interrupted.
                    }
                }
            }
            drawQueueChanged = false;
        }
        
        final long wait = SystemClock.uptimeMillis();
                                
        synchronized (this) {
        	OpenGLSystem.setGL(gl);
        	        	        	
        	DrawableBitmap.beginDrawing(gl, width, height);
            Grid.beginDrawing(gl);
        	
        	if (drawQueue != null && drawQueue.getObjects().getCount() > 0) {
        		FixedSizeArray<BaseObject> objects = drawQueue.getObjects();
                Object[] objectArray = objects.getArray();
                final int count = objects.getCount();
                final float screenScaleX = this.scaleX;
                final float screenScaleY = this.scaleY;
 
                profileObjectCount += count;
                for (int i = 0; i < count; i++) {
                    final RenderElement element = (RenderElement)objectArray[i];
                    
                    if(element.mtx != null) {
                    	// TODO: might be better to just make a matrix factory...
                    	synchronized(element.mtx) {
                    		element.drawable.draw(element.mtx, screenScaleX, screenScaleY);
                    	}
                    }
                    else {
                    	element.drawable.draw(element.x, element.y, element.sx, element.sy, element.rot, screenScaleX, screenScaleY);
                    }
                }
        	}
        	
        	Grid.endDrawing(gl);
            DrawableBitmap.endDrawing(gl);
            
            //go through the pending surface ready callbacks
        	final int surfaceReadyCount = surfaceReadyCallbacks.getCount();
        	if(surfaceReadyCount > 0) {
        		Object[] surfaceReadyArray = surfaceReadyCallbacks.getArray();
        		for(int i = 0; i < surfaceReadyCount; i++) {
        			final SurfaceReadyCallback caller = (SurfaceReadyCallback)surfaceReadyArray[i];
        			caller.onSurfaceReady();
        		}
        		surfaceReadyCallbacks.clear();
        	}
            
            OpenGLSystem.setGL(null);
		}
        
        long time2 = SystemClock.uptimeMillis();
        lastTime = time2;
        
        /*profileFrameTime += time_delta;
        profileSubmitTime += time2 - time;
        profileWaitTime += wait - time;
        
        profileFrames++;
        if (profileFrameTime > PROFILE_REPORT_DELAY) {
        	final int validFrames = profileFrames;
            final long averageFrameTime = profileFrameTime / validFrames;
            final long averageSubmitTime = profileSubmitTime / validFrames;
            final float averageObjectsPerFrame = (float)profileObjectCount / validFrames;
            final long averageWaitTime = profileWaitTime / validFrames;

            DebugLog.d("Render Profile", 
            		"Average Submit: " + averageSubmitTime 
            		+ "  Average Draw: " + averageFrameTime 
            		+ " Objects/Frame: " + averageObjectsPerFrame
            		+ " Wait Time: " + averageWaitTime);
           
            profileFrameTime = 0;
            profileSubmitTime = 0;
            profileFrames = 0;
            profileObjectCount = 0;
        }*/
	}
	
	/**
	 * Use this if you want to do anything related to gl, e.g. Loading new textures, change level, etc.
	 * <p>
	 * This will call cb.onSurfaceReady() during onDrawFrame, guaranteeing OpenGL to be valid
	 */
	public synchronized void requestCallback(SurfaceReadyCallback cb) {
		surfaceReadyCallbacks.add(cb);
    }
	
	// TODO: implement multi viewport...
	public synchronized void setDrawQueue(ObjectManager queue) {
		this.drawQueue = queue;
		
		//wait until drawing has finished
    	synchronized(drawLock) {
    		drawQueueChanged = true;
    		drawLock.notify();
    	}
    }
    
    public synchronized void onPause() {
    	// Stop waiting to avoid deadlock.
    	// TODO: this is a hack.  Probably this renderer
    	// should just use GLSurfaceView's non-continuious render
    	// mode.
    	synchronized(drawLock) {
    		drawQueueChanged = true;
    		drawLock.notify();
    	}
    	
    	 try {
         	BaseObject.systemRegistry.textureManager.invalidateAll(true);
         	BaseObject.systemRegistry.gridManager.invalidateAll(true);
 		} catch (ResourceException e) {
 			DebugLog.e("MateRenderer", e.toString(), e);
 		}
    	
    	//surfaceReadyCallbacks.clear();
    }

    /**
     * This function blocks while drawFrame() is in progress, and may be used by other threads to
     * determine when drawing is occurring.
     */
    
    public synchronized void waitDrawingComplete() {
    }
}
