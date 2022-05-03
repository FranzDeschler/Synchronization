package cloud.codestore.synchronization;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract base class for synchronizing mutable or immutable items.
 * @param <I> the type of an item.
 */
public abstract class Synchronization<I>
{
    private final ItemSet<I> itemSetA;
    private final ItemSet<I> itemSetB;
    private final Status status;
    
    private ProgressListener progressListener = new DefaultProgressListener();
    private ItemProcessor itemProcessor = new DefaultItemProcessor(this, progressListener);
    
    /**
     * @param itemSetA an {@link ItemSet} which represents the items on side A.
     * @param itemSetB an {@link ItemSet} which represents the items on side B.
     * @param status the {@link Status} which represents the items which were present after the last synchronization.
     */
    Synchronization(ItemSet<I> itemSetA, ItemSet<I> itemSetB, Status status)
    {
        Objects.requireNonNull(itemSetA, "The itemSet A must not be null");
        Objects.requireNonNull(itemSetB, "The itemSet B must not be null");
        Objects.requireNonNull(status, "The status must not be null");
        
        this.itemSetA = itemSetA;
        this.itemSetB = itemSetB;
        this.status = status;
    }
    
    /**
     * Executes the synchronization of the provided {@link ItemSet}s.
     */
    public void synchronize()
    {
        synchronize(getAllItemIds());
    }
    
    /**
     * Executes the synchronization of a single item.
     *
     * @param itemId the Id of the item to synchronize.
     */
    public void synchronize(String itemId)
    {
        synchronize(Collections.singleton(itemId));
    }
    
    /**
     * Synchronizes the items specified by the given IDs.
     *
     * @param itemIds the IDs of the items which should be synchronized.
     */
    public void synchronize(Set<String> itemIds)
    {
        Objects.requireNonNull(itemIds);
        itemProcessor.process(itemIds);
    }
    
    /**
     * Cancels the synchronization.
     */
    public void cancel()
    {
        itemProcessor.cancel();
    }
    
    /**
     * @return whether the synchronization was canceled.
     */
    public boolean isCanceled()
    {
        return itemProcessor.isCanceled();
    }
    
    /**
     * @param progressListener a {@link ProgressListener} which should be called when the synchronization
     *         of an item was started of finished.
     */
    public void setProgressListener(ProgressListener progressListener)
    {
        this.progressListener = progressListener;
    }
    
    /**
     * @param conflictResolver the {@link ConflictResolver} which should be called in case of a conflict.
     */
    public void setConflictResolver(ConflictResolver<I> conflictResolver) {}
    
    /**
     * Sets the number of threads to use for the synchronization.
     * By default, this value is 0 which means, the synchronization is executed synchronously.
     * <br/>
     * Note that if the thread count is greater than 1, the {@link ProgressListener} and the {@link ConflictResolver}
     * must be thread safe!
     *
     * @param threadCount the number of threads to use for the synchronization.
     *
     * @throws IllegalArgumentException if {@code threadCount} is less than 0.
     */
    public void setThreadCount(int threadCount)
    {
        if(threadCount < 0)
            throw new IllegalArgumentException("Thread count cannot be less than 0");
        
        if(threadCount == 0)
            itemProcessor = new DefaultItemProcessor(this, progressListener);
        else
            itemProcessor = new ConcurrentItemProcessor(this, progressListener, threadCount);
    }
    
    private Set<String> getAllItemIds()
    {
        Set<String> result = new HashSet<>();
        result.addAll(getItemSetA().getItemIds());
        result.addAll(getItemSetB().getItemIds());
        result.addAll(getStatus().getItemIds());
        return result;
    }
    
    ItemSet<I> getItemSetA()
    {
        return itemSetA;
    }
    
    ItemSet<I> getItemSetB()
    {
        return itemSetB;
    }
    
    Status getStatus()
    {
        return status;
    }
    
    abstract void synchronizeItem(String itemId) throws Exception;
}
