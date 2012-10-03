package com.renegadeware.m8.screen;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.renegadeware.m8.MateActivity;
import com.renegadeware.m8.gfx.SurfaceReadyCallback;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.TObjectPool;
import com.renegadeware.m8.util.FixedSizeArray;

/**
 * 
 * @author ddionisio
 *
 */
public final class ScreenSystem extends BaseObject {
	public static final int MAX_STACK = 12;
	
	/** the current active screens, with the top in control */
	final FixedSizeArray<Screen> stack;	
	final FixedSizeArray<Screen> _stackRemove;
	final FixedSizeArray<Screen> _stackAdd;
	
	/** 
	 * Once a screen is loaded, they are ready to be pushed to the stack.
	 * These are processed by the screen pusher for enter animation one at a time.
	 * Once a screen is done with enter update, they are added to the stack.
	 **/
	final FixedSizeArray<Screen> pendingPush;
	
	/**
	 * When a screen is popped, they are added to the pending pop.
	 * The screen popper calls screen.exitUpdate until it returns true,
	 * then it adds the screen to pendingUnload.
	 */
	final FixedSizeArray<Screen> pendingPop;
	
	/**
	 * When a screen is pushed, they are added to pending load.  Once loaded,
	 * they are then put to pending push.  This will activate the screen pusher if
	 * there are no longer any screens to load.
	 */
	final FixedSizeArray<Screen> pendingLoad;
	
	/**
	 * After a screen finishes exit update, they are ready to be unloaded. Once
	 * unloaded, they are completely free.
	 */
	final FixedSizeArray<Screen> pendingUnload;
	
	final ScreenLoaderPool screenLoaderPool;
	final ScreenUnloaderPool screenUnloaderPool;
	
	/**
	 * When active, the pusher calls screen.enterUpdate one at a time, when a screen
	 * is done with update, they are added to the stack.  Once all pending pushes are
	 * finish, the top of the stack is resumed to gain control (screen.resume)
	 */
	final ObjectScreenPusher screenPusher;
	
	/**
	 * When active, the popper calls screen.exitUpdate one at a time, when a screen
	 * is done with update, they are then put to pending unload. Once all are popped,
	 * If there are any pending load, they are processed to poll load.
	 */
	final ObjectScreenPopper screenPopper;
	
	final Lock lock;
	
	boolean inLock;
	
	public ScreenSystem() {		
		stack = new FixedSizeArray<Screen>(MAX_STACK);
		_stackRemove = new FixedSizeArray<Screen>(MAX_STACK);
		_stackAdd = new FixedSizeArray<Screen>(MAX_STACK);
		
		pendingPush = new FixedSizeArray<Screen>(MAX_STACK);
		pendingPop = new FixedSizeArray<Screen>(MAX_STACK);
		
		pendingLoad = new FixedSizeArray<Screen>(MAX_STACK);
		pendingUnload = new FixedSizeArray<Screen>(MAX_STACK);
		
		screenLoaderPool = new ScreenLoaderPool(MAX_STACK);
		screenUnloaderPool = new ScreenUnloaderPool(MAX_STACK);
		
		screenPusher = new ObjectScreenPusher();
		screenPopper = new ObjectScreenPopper();
				
		lock = new ReentrantLock();
		
		inLock = false;
	}
	
	public void lock() {
		lock.lock();
	}
	
	public void unlock() {
		lock.unlock();
	}
	
	public int getScreenStackInd(Screen s) {
		boolean ok = lock.tryLock();
		if(!ok && !inLock) {
			lock.lock();
			ok = true;
		}
		
		try {
			return stack.find(s, true);
		} finally {
			if(ok) {
				lock.unlock();
			}
		}
	}
	
	public void push(Screen s) {
		boolean ok = lock.tryLock();
		if(!ok && !inLock) {
			lock.lock();
			ok = true;
		}
						
		try {
			//make sure this screen is not in the stack or pending push/pop/load/unload
			if(!(isInStack(s) || isPendingPush(s) || isPendingPop(s) 
					|| isPendingLoad(s) || isPendingUnload(s))) {
				//pause current top stack
				if(pendingPush.getCount() == 0) {
					Screen top = stack.getLast();
					if(top != null && !isPendingPop(top) && !isPendingUnload(top)) {
						top.pause();
					}
				}
				
				pendingLoad.add(s);
				
				//only poll load if there are no pending pops
				//the screen popper will poll load for us later if that's the case
				if(!screenPopper.isActive) {
					//poll a surface ready request to initialize the screen
					//some screens require openGL to be valid during load
					//so this will guarantee it is available.
					MateActivity mate = (MateActivity)systemRegistry.contextParameters.context;
					ScreenLoader loader = screenLoaderPool.allocate();
					loader.setScreen(s);
					mate.requestSurfaceReadyCall(loader);
				}
			}
		} finally {
			if(ok) {
				lock.unlock();
			}
		}
	}
	
	/**
	 * Pop the stack up to and including the given s, unless exclude is true.
	 * @param s The screen to pop up to.
	 * @param exclude if true, then given screen is not popped.
	 */
	public void popTo(Screen s, boolean exclude) {
		boolean ok = lock.tryLock();
		if(!ok && !inLock) {
			lock.lock();
			ok = true;
		}
		
		try {
			//make sure this screen is not in the stack or pending push/pop/load/unload
			if(!(isPendingPush(s) || isPendingPop(s) 
					|| isPendingLoad(s) || isPendingUnload(s))) {
				//move the entire stack to pop
				//set each screen to exit mode
				for(int i = stack.getCount()-1; i >= 0; i--) {
					Screen sPop = stack.get(i);
					if(sPop == s) {
						if(!exclude) {
							pendingPop.add(sPop);
							_stackRemove.add(sPop);
						}
						
						break;
					}
					else {
						pendingPop.add(sPop);
						_stackRemove.add(sPop);
					}
				}
								
				//start up the screen popper update
				screenPopper.isActive = true;
			}
		} finally {
			if(ok) {
				lock.unlock();
			}
		}
	}
	
	public void pop() {
		boolean ok = lock.tryLock();
		if(!ok && !inLock) {
			lock.lock();
			ok = true;
		}
		
		try {
			Screen curScreen = stack.getLast();
			if(curScreen != null) {
				pendingPop.add(curScreen);
								
				//start up the screen popper update
				screenPopper.isActive = true;
				
				_stackRemove.add(curScreen);
			}
		} finally {
			if(ok) {
				lock.unlock();
			}
		}
	}
	
	public void replace(Screen s) {
		boolean ok = lock.tryLock();
		if(!ok && !inLock) {
			lock.lock();
			ok = true;
		}
		
		try {
			//make sure this screen is not in the stack or pending push/pop/load/unload
			if(!(isInStack(s) || isPendingPush(s) || isPendingPop(s) 
					|| isPendingLoad(s) || isPendingUnload(s))) {
				//move the entire stack to pop
				//set each screen to exit mode
				for(int i = stack.getCount()-1; i >= 0; i--) {
					Screen sPop = stack.get(i);
					pendingPop.add(sPop);
					
					_stackRemove.add(sPop);
				}
								
				//start up the screen popper update
				screenPopper.isActive = true;
												
				//put in pending load, once all pop is finished this will be loaded
				pendingLoad.add(s);
			}
		} finally {
			if(ok) {
				lock.unlock();
			}
		}
	}
	
	public Screen getTopScreen() {
		boolean ok = lock.tryLock();
		if(!ok && !inLock) {
			lock.lock();
			ok = true;
		}
		
		try {
			return stack.getLast();
		} finally {
			if(ok) {
				lock.unlock();
			}
		}
	}
	
	public int getActiveScreenCount() {
		boolean ok = lock.tryLock();
		if(!ok && !inLock) {
			lock.lock();
			ok = true;
		}
		
		try {
			return stack.getCount();
		} finally {
			if(ok) {
				lock.unlock();
			}
		}
	}

	@Override
	public void reset() {
		boolean ok = lock.tryLock();
		if(!ok && !inLock) {
			lock.lock();
			ok = true;
		}
		
		try {
			//clear out all pending pushes, pops, loads, unloads
			pendingPush.clear();
			pendingPop.clear();
			pendingLoad.clear();
			pendingUnload.clear();
			
			//clear stack
			stack.clear();
			_stackAdd.clear();
			_stackRemove.clear();
						
			screenPusher.isActive = false;
			screenPopper.isActive = false;
		} finally {
			if(ok) {
				lock.unlock();
			}
		}
	}
	
	void _commitStackChanges() {		
		int popCount = _stackRemove.getCount();
		int pushCount = _stackAdd.getCount();
		
		if(popCount > 0) {
			Object[] removes = _stackRemove.getArray();
			for(int i = 0; i < popCount; i++) {
				stack.remove((Screen)removes[i], false);
			}
			
			_stackRemove.clear();
			
			if(pushCount == 0 && pendingPush.getCount() == 0 && pendingLoad.getCount() == 0) {
				Screen top = stack.getLast();
				top.resume();
			}
		}
				
		if(pushCount > 0) {
			Object[] adds = _stackAdd.getArray();
			Screen s = null;
			for(int i = 0; i < pushCount; i++) {
				s = (Screen)adds[i];
				stack.add(s);
			}
			
			_stackAdd.clear();
			
			//resume or pause the last pushed screen
			if(s != null) {
				//if there are no incoming pushes, then resume
				if(pendingPush.getCount() == 0 && pendingLoad.getCount() == 0) {
					s.resume();
				}
				else { //pause it since this wasn't done from ScreenPusher
					s.pause();
				}
			}
		}
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		//TODO: allow for overlay check to update from top to bottom of stack?
		if(lock.tryLock()) {
			try {
				inLock = true;
				
				_commitStackChanges();
				
				final int stackCount = stack.getCount();
				if(stackCount > 0) {
					final Object[] _stackArray = stack.getArray();
					
					for(int i = 0; i < stackCount; i++) {
						final Screen s = (Screen)_stackArray[i];
						s.update(timeDelta);
					}
				}
				
				screenPopper.update(timeDelta);
				screenPusher.update(timeDelta);
			} finally {
				inLock = false;
				lock.unlock();
			}
		}
	}
	
	boolean isInStack(Screen s) {
		return stack.find(s, true) != -1;
	}
	
	boolean isPendingPush(Screen s) {
		return pendingPush.find(s, true) != -1;
	}
	
	boolean isPendingPop(Screen s) {
		return pendingPop.find(s, true) != -1;
	}
	
	boolean isPendingLoad(Screen s) {
		return pendingLoad.find(s, true) != -1;
	}
	
	boolean isPendingUnload(Screen s) {
		return pendingUnload.find(s, true) != -1;
	}
	
	public boolean isPending() {
		boolean ret = false;
		
		boolean ok = lock.tryLock();
		if(!ok && !inLock) {
			lock.lock();
			ok = true;
		}
		
		try {
			ret = pendingPush.getCount() > 0 || pendingPop.getCount() > 0 || pendingLoad.getCount() > 0;
		} finally {
			if(ok) {
				lock.unlock();
			}
		}
		
		return ret;
	}

	class ObjectScreenPusher {
		public boolean isActive;
		
		private Screen curScreen;
		
		public ObjectScreenPusher() {
			isActive = false;
			curScreen = null;
		}
		
		private void _getNewCurScreen(ScreenSystem sys) {
			//((MateActivity)systemRegistry.contextParameters.context).getRenderer().waitDrawingComplete();
			
			if (sys.pendingPush.getCount() > 0) {
				curScreen = sys.pendingPush.get(0);
				curScreen.startEnter();
				curScreen.enterUpdate(0);
			}
			else {
				curScreen = null;
				
				//all pending push is done
				//our purpose is done, remove ourself from update
				isActive = false;
			}
		}
		
		public void update(float timeDelta) {
			if(!isActive) {
				return;
			}
			
			final ScreenSystem sys = ScreenSystem.this;
				
			//under extreme circumstances, screen popper might be active
			//don't do anything until it's done
			if(sys.screenPopper.isActive) {
				return;
			}
			
			if(curScreen != null) {
				//once done, remove from pending push and put it in stack
				if(!curScreen.enterUpdate(timeDelta)) {
					//remove from pending, add to stack
					sys.pendingPush.remove(curScreen, false);
					sys._stackAdd.add(curScreen);
					
					if(sys.pendingPush.getCount() > 0) {
						curScreen.pause();
					}
					
					_getNewCurScreen(sys);
				}
			}
			else {
				_getNewCurScreen(sys);
			}
		}
	}
	
	class ObjectScreenPopper {
		public boolean isActive;
		
		private Screen curScreen;
		
		public ObjectScreenPopper() {
			isActive = false;
			curScreen = null;
		}
		
		private void _getNewCurScreen(ScreenSystem sys, MateActivity mate) {
			//mate.getRenderer().waitDrawingComplete();
			
			if (sys.pendingPop.getCount() > 0) {
				curScreen = sys.pendingPop.get(0);
				curScreen.resume();
				curScreen.startExit();
				curScreen.exitUpdate(0);
			}
			else {
				curScreen = null;
				
				//all pending pop is done
				//poll loader for all pending load
				//the last screen to get loaded will initialize the screen pusher
				for(int i = 0; i < sys.pendingLoad.getCount(); i++) {
					Screen s = sys.pendingLoad.get(i);
					ScreenLoader loader = sys.screenLoaderPool.allocate();
					loader.setScreen(s);
					mate.requestSurfaceReadyCall(loader);
				}
				
				//our purpose is done, remove ourself from update
				isActive = false;
			}
		}
		
		public void update(float timeDelta) {
			if(!isActive) {
				return;
			}
			
			//once all is out, prepare transition in put all pending push to stack, 
			//or start up enter animations for pending push and activate screen pusher 
			final ScreenSystem sys = ScreenSystem.this;

			final MateActivity mate = (MateActivity)systemRegistry.contextParameters.context;
			
			if(curScreen != null) {
				//update the pending screens, since these are no longer in the stack
				//and are still valid
				int count = sys.pendingPop.getCount();
				Object[] array = sys.pendingPop.getArray();
				for(int i = count-1; i > 0; i--) {
					Screen s = (Screen)array[i];
					s.update(timeDelta);
				}
				
				if(!curScreen.exitUpdate(timeDelta)) {
					sys.pendingPop.remove(curScreen, false);
					
					sys.pendingUnload.add(curScreen);
					
					//poll for unload
					ScreenUnloader unloader = sys.screenUnloaderPool.allocate();
					unloader.setScreen(curScreen);
					mate.requestSurfaceReadyCall(unloader);
					
					_getNewCurScreen(sys, mate);
				}
			}
			else {
				_getNewCurScreen(sys, mate);
			}
		}
	}
			
	class ScreenLoader implements SurfaceReadyCallback {
		private Screen screen;
		
		public void setScreen(Screen s) {
			screen = s;
		}

		@Override
		public void onSurfaceReady() {
			final ScreenSystem sys = ScreenSystem.this;
			sys.lock.lock();
			
			try {				
				//make sure the screen is still in pendingLoad
				if(isPendingLoad(screen)) {
					screen.load();
					
					sys.pendingLoad.remove(screen, true);
					sys.pendingPush.add(screen);
										
					//activate the screen pusher to play the enter animation for these screens
					if(sys.pendingLoad.getCount() == 0) {
						java.lang.System.gc();
						
						sys.screenPusher.isActive = true;
					}
				}
				
				screen = null;
				sys.screenLoaderPool.release(this);
			} finally {
				sys.lock.unlock();
			}
		}
	}
	
	class ScreenLoaderPool extends TObjectPool<ScreenLoader> {
		public ScreenLoaderPool(int size) {
			super(size);
		}

		@Override
		protected void fill() {
			int size = getSize();
            for (int x = 0; x < size; x++) {
            	ScreenLoader entry = new ScreenLoader();
                getAvailable().add(entry);
            }
		}
	}
	
	class ScreenUnloader implements SurfaceReadyCallback {
		private Screen screen;
		
		public void setScreen(Screen s) {
			screen = s;
		}
		
		@Override
		public void onSurfaceReady() {			
			final ScreenSystem sys = ScreenSystem.this;
			sys.lock.lock();
			
			try {				
				//isPendingUnload
				//make sure the screen is still in pendingUnload
				if(isPendingUnload(screen)) {
					screen.unload();
					sys.pendingUnload.remove(screen, true);
				}
				
				screen = null;
				sys.screenUnloaderPool.release(this);
			} finally {
				sys.lock.unlock();
			}
		}
	}
	
	class ScreenUnloaderPool extends TObjectPool<ScreenUnloader> {
		public ScreenUnloaderPool(int size) {
			super(size);
		}

		@Override
		protected void fill() {
			int size = getSize();
            for (int x = 0; x < size; x++) {
            	ScreenUnloader entry = new ScreenUnloader();
                getAvailable().add(entry);
            }
		}
	}
}
