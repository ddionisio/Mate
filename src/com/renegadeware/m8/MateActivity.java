package com.renegadeware.m8;

import com.renegadeware.m8.gfx.FontManager;
import com.renegadeware.m8.gfx.GridManager;
import com.renegadeware.m8.gfx.MateRenderer;
import com.renegadeware.m8.gfx.OpenGLSystem;
import com.renegadeware.m8.gfx.RenderSystem;
import com.renegadeware.m8.gfx.SpriteManager;
import com.renegadeware.m8.gfx.SurfaceReadyCallback;
import com.renegadeware.m8.gfx.TextureAtlasManager;
import com.renegadeware.m8.gfx.TextureManager;
import com.renegadeware.m8.gfx.ViewSystem;
import com.renegadeware.m8.input.InputSystem;
import com.renegadeware.m8.input.MultiTouchFilter;
import com.renegadeware.m8.input.SingleTouchFilter;
import com.renegadeware.m8.input.TouchFilter;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.ObjectRegistry;
import com.renegadeware.m8.res.ResourceGroupManager;
import com.renegadeware.m8.screen.ScreenSystem;
import com.renegadeware.m8.sound.SoundManager;
import com.renegadeware.m8.ui.UIResManager;
import com.renegadeware.m8.util.Util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public abstract class MateActivity extends Activity implements MateInterface {
	
	protected Mate root;
	protected MateRenderer renderer;
	protected GLSurfaceView surfaceView;
	protected TouchFilter touchFilter;
    
    /* **********************************************************************************************
	 * Methods
	 * **********************************************************************************************/
    
    /* **********************************************************************************************
	 * Overrides
	 * **********************************************************************************************/
	
	/** Called when the activity is first created. */
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        isBackKeyPressed = false;
        isMenuKeyPressed = false;
        
        // request fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        final Resources r = getResources();
        
        final String prefName = r.getString(R.string.preference);
        isDebug = r.getBoolean(R.bool.debug);
        
        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        
        if (isDebug) {
        	DebugLog.setDebugLogging(true);
        } else {
        	DebugLog.setDebugLogging(false);
        }
        
        DebugLog.d("Mate", "onCreate");
        
        ///////////////////////////////////////////////////////////
        //initialize surface
        setContentView(R.layout.main);
        
        surfaceView = (GLSurfaceView)findViewById(R.id.glsurfaceview);
        surfaceView.setEGLConfigChooser(false);
        if (isDebug) { surfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR); }// | GLSurfaceView.DEBUG_LOG_GL_CALLS); }                       
                
        int defaultWidth = r.getDimensionPixelOffset(R.dimen.defaultWidth);
        int defaultHeight = r.getDimensionPixelOffset(R.dimen.defaultHeight);      
                
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        if (dm.widthPixels != defaultWidth) {
        	//if(defaultWidth > defaultHeight) {
        		float ratio =((float)dm.widthPixels) / dm.heightPixels;
        		defaultWidth = (int)(defaultHeight * ratio);
        	/*}
        	else {
        		float ratio =((float)dm.heightPixels) / dm.widthPixels;
        		defaultHeight = (int)(defaultWidth * ratio);
        	}*/
        }
        
        renderer = new MateRenderer(this, defaultWidth, defaultHeight);
        surfaceView.setRenderer(renderer);
        
        ContextParameters cp = new ContextParameters(); 
        cp.debugEnabled = isDebug;
        cp.viewWidth = dm.widthPixels;
        cp.viewHeight = dm.heightPixels;
        cp.gameWidth = defaultWidth;
        cp.gameHeight = defaultHeight;
        cp.gameHalfWidth = defaultWidth>>1;
        cp.gameHalfHeight = defaultHeight>>1;
        cp.viewScaleX = (float)dm.widthPixels / defaultWidth;
        cp.viewScaleY = (float)dm.heightPixels / defaultHeight;
        cp.context = this;
        
        final ObjectRegistry sysReg = BaseObject.systemRegistry;
        
        sysReg.contextParameters = cp;
        
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) {
        	//single touch filter
        	touchFilter = new SingleTouchFilter();
        }
        else {
        	//multi touch filter
        	touchFilter = new MultiTouchFilter();
        }
        
        ///////////////////////////////////////////////////////////
        //initialize core systems
        Util.initRandom();
        
        sysReg.openGLSystem = new OpenGLSystem(null);
        
        // TODO: might be optional
        sysReg.screenSystem = new ScreenSystem();
        
     // This activity uses the media stream.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
                        
        ///////////////////////////////////////////////////////////
        //initialize resource managers 
        final ResourceGroupManager resGrpMgr = sysReg.resourceGroupManager = new ResourceGroupManager();
        
        TextureManager txtMgr = new TextureManager();
        sysReg.textureManager = txtMgr;
        resGrpMgr.registerResourceManager(txtMgr);
                        
        TextureAtlasManager txtAMgr = new TextureAtlasManager();
        sysReg.textureAtlasManager = txtAMgr;
        resGrpMgr.registerResourceManager(txtAMgr);
        resGrpMgr.registerScriptLoader(txtAMgr);
        
        // TODO: these should be game specific
        GridManager gridMgr = new GridManager();
        sysReg.gridManager = gridMgr;
        resGrpMgr.registerResourceManager(gridMgr);
        
        SpriteManager sprMgr = new SpriteManager();
        sysReg.spriteManager = sprMgr;
        resGrpMgr.registerResourceManager(sprMgr);
        resGrpMgr.registerScriptLoader(sprMgr);
        
        FontManager fntMgr = new FontManager();
        sysReg.fontManager = fntMgr;
        resGrpMgr.registerResourceManager(fntMgr);
        resGrpMgr.registerScriptLoader(fntMgr);
        
        UIResManager uiResMgr = new UIResManager();
        sysReg.uiResManager = uiResMgr;
        resGrpMgr.registerResourceManager(uiResMgr);
        
        SoundManager sndMgr = new SoundManager();
        sysReg.soundManager = sndMgr;
        resGrpMgr.registerResourceManager(sndMgr);
                        
        //vertex buffer, sound manager
                
        ///////////////////////////////////////////////////////////
        //Systems
        sysReg.renderSystem =  new RenderSystem();
        
        InputSystem input = new InputSystem();
        sysReg.inputSystem = input;
        sysReg.registerForReset(input);
        
        ViewSystem view = new ViewSystem(defaultWidth, defaultHeight);
        sysReg.viewSystem = view;
        sysReg.registerForReset(view);
        
        ///////////////////////////////////////////////////////////
        //initialize game loop
        root = new Mate();
        root.bootstrap(renderer);
        
        //add systems to root
        
        //screen system
        if(sysReg.screenSystem != null) {
        	root.add(sysReg.screenSystem);
        }
                
        //load internal data
        try {
        	if(!resGrpMgr.isGroupLoaded(ResourceGroupManager.InternalResourceGroupName)) {
        		resGrpMgr.loadGroup(ResourceGroupManager.InternalResourceGroupName);
        	}			
        } catch (Exception e) {
        	DebugLog.e("MateRenderer", e.toString(), e);
        }
        
        //renderer specific initializations
        renderer.init();
        
        // Game specific
        init(prefs);
    }
    
    @Override
    protected final void onDestroy() {
    	DebugLog.d("Mate", "onDestroy()");
    	
    	// Game specific
    	destroy();
    	
    	// stop the game thread
    	root.stop();
    	
    	//unregister all resource managers, clear out resource groups, and unload any stray resources
    	BaseObject.systemRegistry.resourceGroupManager.reset();
    	
    	super.onDestroy();
    }
    
    @Override
    protected final void onStart() {
    	super.onStart();
    	
    	DebugLog.d("Mate", "onStart");
    	
    	start();
    }
                
    @Override
    protected final void onResume() {
    	super.onResume();
    	
        isBackKeyPressed = false;
        isMenuKeyPressed = false;
        
        // Preferences may have changed while we were paused.
        final Resources r = getResources();
        
        final String prefName = r.getString(R.string.preference);
        isDebug = r.getBoolean(R.bool.debug);
        
        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        
        if (isDebug) {
        	DebugLog.setDebugLogging(true);
        } else {
        	DebugLog.setDebugLogging(false);
        }
        
        BaseObject.systemRegistry.contextParameters.context = this;
        BaseObject.systemRegistry.contextParameters.debugEnabled = isDebug;
                        
        surfaceView.onResume();
        
        root.resume();
        
        DebugLog.d("Mate", "onResume");
                        
        resume(prefs);
    }
    
    @Override
    protected final void onPause() {
    	super.onPause();
        
        surfaceView.onPause();
        
        root.pause();
                                        
        renderer.onPause(); //?
        
        DebugLog.d("Mate", "onPause");
        
        pause();
    }
    
    @Override
    protected final void onStop() {
    	super.onStop();
    	
    	DebugLog.d("Mate", "onStop");
    	
    	stop();
    }
    
    @Override
    public final boolean onTouchEvent(MotionEvent event) {
    	if(!root.isPaused()) {
    		//call touch filter
    		if(root.isRunning()) {
    			touchFilter.updateTouch(event);
    		}
    		
    		final long time = System.currentTimeMillis();
    		
    		if (event.getAction() == MotionEvent.ACTION_MOVE && time - lastTouchTime < 32) {
    			// Sleep so that the main thread doesn't get flooded with UI events.
		        try {
		            Thread.sleep(32);
		        } catch (InterruptedException e) {
		            // No big deal if this sleep is interrupted.
		        }
		        
		        renderer.waitDrawingComplete();
    		}
    		
    		lastTouchTime = time;
    	}
    	
    	return true;
    }
    
    @Override
	public final boolean onKeyDown(int keyCode, KeyEvent event) {
    	boolean result = true;
    	
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		final long time = System.currentTimeMillis();
    		if (time - lastRollTime > ROLL_TO_FACE_BUTTON_DELAY &&
    				time - lastTouchTime > ROLL_TO_FACE_BUTTON_DELAY) {
    			isBackKeyPressed = true;
    		}
    	}
    	else if(keyCode == KeyEvent.KEYCODE_MENU) {
    		final long time = System.currentTimeMillis();
    		if (time - lastRollTime > ROLL_TO_FACE_BUTTON_DELAY &&
    				time - lastTouchTime > ROLL_TO_FACE_BUTTON_DELAY) {
    			isMenuKeyPressed = true;
    		}
    		
    		if (isDebug) {
	        	result = false;	// Allow the debug menu to come up in debug mode.
	        }
    	}
    	else {
    		result = super.onKeyDown(keyCode, event);
    		// TODO: add input bind system here
    		//result =
    		
    		// Sleep so that the main thread doesn't get flooded with UI events.
		    try {
		        Thread.sleep(4);
		    } catch (InterruptedException e) {
		        // No big deal if this sleep is interrupted.
		    }
    	}
    	
		return result;
    }
    
    @Override
	public final boolean onKeyUp(int keyCode, KeyEvent event) {
    	boolean result = true;
    	
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		if(isBackKeyPressed) {
    			back();
    			isBackKeyPressed = false;
    		}
    	}
    	else if(keyCode == KeyEvent.KEYCODE_MENU) {
    		if(isMenuKeyPressed) {
    			menu();
    			isMenuKeyPressed = false;
    		}
    		
    		if (isDebug) {
	        	result = false;	// Allow the debug menu to come up in debug mode.
	        }
    	}
    	else {
    		result = super.onKeyUp(keyCode, event);
    		// TODO: add input bind system here
    		//result =
    		
    		// Sleep so that the main thread doesn't get flooded with UI events.
		    try {
		        Thread.sleep(4);
		    } catch (InterruptedException e) {
		        // No big deal if this sleep is interrupted.
		    }
    	}
    	
		return result;
    }
    
    public Mate getRoot() {
    	return root;
    }
    
    public SharedPreferences getPrefs() {
    	final Resources r = getResources();
    	final String prefName = r.getString(R.string.preference);
        return getSharedPreferences(prefName, MODE_PRIVATE);
    }
    
    public void requestSurfaceReadyCall(SurfaceReadyCallback scb) {
    	renderer.requestCallback(scb);
    }
    
    public MateRenderer getRenderer() {
    	return renderer;
    }
    
    /* **********************************************************************************************
	 * Internal
	 * **********************************************************************************************/
    private static final int ROLL_TO_FACE_BUTTON_DELAY = 400;
    
    private boolean isDebug = false;
    private boolean isBackKeyPressed = false;
    private boolean isMenuKeyPressed = false;
    private long lastTouchTime = 0L;
    private long lastRollTime = 0L;
}
