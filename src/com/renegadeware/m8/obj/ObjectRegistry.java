package com.renegadeware.m8.obj;

import com.renegadeware.m8.ContextParameters;
import com.renegadeware.m8.TimeSystem;
import com.renegadeware.m8.gfx.FontManager;
import com.renegadeware.m8.gfx.GridManager;
import com.renegadeware.m8.gfx.OpenGLSystem;
import com.renegadeware.m8.gfx.RenderSystem;
import com.renegadeware.m8.gfx.SpriteManager;
import com.renegadeware.m8.gfx.TextureAtlasManager;
import com.renegadeware.m8.gfx.TextureManager;
import com.renegadeware.m8.gfx.ViewSystem;
import com.renegadeware.m8.input.InputSystem;
import com.renegadeware.m8.res.ResourceGroupManager;
import com.renegadeware.m8.screen.ScreenSystem;
import com.renegadeware.m8.sound.SoundManager;
import com.renegadeware.m8.ui.UIResManager;

/**
 * The object registry manages a collection of global singleton objects.  However, it differs from
 * the standard singleton pattern in a few important ways:
 * - The objects managed by the registry have an undefined lifetime.  They may become invalid at 
 *   any time, and they may not be valid at the beginning of the program.
 * - The only object that is always guaranteed to be valid is the ObjectRegistry itself.
 * - There may be more than one ObjectRegistry, and there may be more than one instance of any of
 *   the systems managed by ObjectRegistry allocated at once.  For example, separate threads may
 *   maintain their own separate ObjectRegistry instances.
 */
public class ObjectRegistry extends BaseObjectRegistry {
	
	public ContextParameters contextParameters;
	
	//Systems
	public OpenGLSystem openGLSystem;
	public RenderSystem renderSystem;
	public TimeSystem timeSystem;
	public InputSystem inputSystem;
	public ViewSystem viewSystem;
	public ScreenSystem screenSystem;
	
	//Managers
	public ResourceGroupManager resourceGroupManager;
		
	public TextureManager textureManager;
	public GridManager gridManager;
	public TextureAtlasManager textureAtlasManager;
	public SpriteManager spriteManager;
	public FontManager fontManager;
	public UIResManager uiResManager;
	public SoundManager soundManager;
}
