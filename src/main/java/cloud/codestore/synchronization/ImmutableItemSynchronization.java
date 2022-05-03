package cloud.codestore.synchronization;

/**
 * Synchronizes immutable items.
 * If an item exists on both sides A and B, none of them gets updated regardless of the content.
 *
 * @param <I> the type of an item.
 */
public class ImmutableItemSynchronization<I> extends Synchronization<I>
{
    /**
     * @param itemSetA an {@link ItemSet} which represents the items on side A.
     * @param itemSetB an {@link ItemSet} which represents the items on side B.
     * @param status the {@link Status} which represents the items which were present after the last synchronization.
     */
    public ImmutableItemSynchronization(ItemSet<I> itemSetA, ItemSet<I> itemSetB, Status status)
    {
        super(itemSetA, itemSetB, status);
    }
    
    @Override
    void synchronizeItem(String itemId) throws Exception
    {
        boolean existsOnA = getItemSetA().contains(itemId);
        boolean existsOnB = getItemSetB().contains(itemId);
        boolean existsInStatus = getStatus().contains(itemId);
        
        if(existsOnA && existsOnB)
        {
            if(!existsInStatus)
                addToStatus(itemId);
        }
        else if(existsOnA)
        {
            if(existsInStatus)
                deleteFromA(itemId);
            else
                createOnB(itemId);
        }
        else if(existsOnB)
        {
            if(existsInStatus)
                deleteFromB(itemId);
            else
                createOnA(itemId);
        }
        else if(existsInStatus)
            deleteInStatus(itemId);
    }
    
    private void addToStatus(String itemId)
    {
        getStatus().put(itemId);
    }
    
    private void deleteInStatus(String itemId)
    {
        getStatus().delete(itemId);
    }
    
    private void deleteFromA(String itemId) throws Exception
    {
        getItemSetA().delete(itemId);
        deleteInStatus(itemId);
    }
    
    private void deleteFromB(String itemId) throws Exception
    {
        getItemSetB().delete(itemId);
        deleteInStatus(itemId);
    }
    
    private void createOnB(String itemId) throws Exception
    {
        I item = getItemSetA().getItem(itemId);
        getItemSetB().addItem(itemId, item);
        addToStatus(itemId);
    }
    
    private void createOnA(String itemId) throws Exception
    {
        I item = getItemSetB().getItem(itemId);
        getItemSetA().addItem(itemId, item);
        addToStatus(itemId);
    }
}
