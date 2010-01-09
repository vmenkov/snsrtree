package dd.engine;

/** A class implementing this interface can be associated with a
 * {@link FrontierContext}, so that the Frontier Finder can report to
 * it on its progress as it goes along. This can be used for passing
 * progress messages into a GUI or Web UI, and for receiving cancellation 
 * requests.
 */
public interface Callback {
    /** Sends a text message to e.g. a GUI.
	@param msg Message to send
	@return true if the Frontier Finder should continue; false if
	cancellation is requested
     */
    public boolean callback(String msg);
    
}