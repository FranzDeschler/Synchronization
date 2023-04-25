package cloud.codestore.synchronization;

/**
 * The {@link ProgressListener} is called whenever the synchronization of an item was started or finished.
 */
public interface ProgressListener
{
    /**
     * Called before the first item is synchronized.
     *
     * @param numberOfItems the total number of items to be synchronized.
     */
    void numberOfItems(int numberOfItems);

    /**
     * Called whenever the synchronization of an item started.
     *
     * @param itemId the ID of the corresponding item.
     */
    void synchronizationStarted(String itemId);
    
    /**
     * Called whenever the synchronization of an item was successfully finished.
     *
     * @param itemId the ID of the corresponding item.
     */
    void synchronizationFinished(String itemId);
    
    /**
     * Called whenever the synchronization of an item failed.
     *
     * @param itemId the ID of the corresponding item.
     * @param exception the Exception which caused the error.
     */
    void synchronizationFailed(String itemId, Throwable exception);
}
