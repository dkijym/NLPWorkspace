/*
 * Created on Sept 7, 2004
 *
 * 
 */
package metdemo.Window;

// TODO: Auto-generated Javadoc
/**
 * The Interface EMTWindow.
 *
 * @author Shlomo Hershkop
 * 
 * Interface to define an EMT window.
 * Will allow generic processing and treatment of any window no matter what the underlying display is.
 */
public interface EMTWindow {

	/**
	 * Sets the window id.
	 *
	 * @param ID the new iD
	 */
	public abstract void setID(int ID);
	
	
	/**
	 * Initialization of window components.
	 */
	public abstract void initializeWindow();
	
	
	/**
	 * Whether this window has been setup already.
	 *
	 * @return true, if successful
	 */
	public abstract boolean hasBeenInitialized();
	
	/**
	 * Gets the window id.
	 *
	 * @return the id, -1 if not set
	 */
	public abstract int GetID();
	
	/*
	 * Redraws the window with the new user information (if applicable)
	 */
	/**
	 * Update.
	 */
	public abstract void Update();
	
	
	/**
	 * Will clear the window and reset all information.
	 *
	 */
	public abstract void Clear();
	
	/**
	 * 
	 * @return an object representing its state to be saved
	 */
	//public abstract Object saveState();
	
	/**
	 * Loads its state based on some object
	 * @param o
	 */
	//public abstract void loadState(Object o);
	
	
	
	
	
}
