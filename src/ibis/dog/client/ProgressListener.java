package ibis.dog.client;

/**
 * Interface for passing messages from the client to a user interface
 * 
 */
public interface ProgressListener {

    public void message(String message);
    
    /**
     * @param progress a number between 0.0 and 1.0 denoting the progress
     * 
     * @return false if the user is no longer interested in the result, and the
     * operation can be aborted
     */
    public boolean progress(double progress);
}
