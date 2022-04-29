package cloud.codestore.synchronization;

import java.util.Set;

abstract class ItemProcessor
{
    private Synchronization<?> synchronization;
    private ProgressListener progressListener;
    private boolean canceled;
    
    ItemProcessor(Synchronization<?> synchronization, ProgressListener progressListener)
    {
        this.synchronization = synchronization;
        this.progressListener = progressListener;
    }
    
    void cancel()
    {
        canceled = true;
    }
    
    boolean isCanceled()
    {
        return canceled;
    }
    
    /**
     * Processes the items with the given IDs.
     *
     * @param itemIds the IDs of the items which should be processed.
     */
    abstract void process(Set<String> itemIds);
    
    /**
     * Processes a single item.
     *
     * @param itemId the id of the item to process
     */
    void process(String itemId)
    {
        try
        {
            progressListener.synchronizationStarted(itemId);
            synchronization.synchronizeItem(itemId);
            progressListener.synchronizationFinished(itemId);
        }
        catch(Exception exception)
        {
            progressListener.synchronizationFailed(itemId, exception);
        }
    }
}
