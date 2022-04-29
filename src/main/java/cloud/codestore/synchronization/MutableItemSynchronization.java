package cloud.codestore.synchronization;

import java.util.Objects;

/**
 * Synchronizes mutable items.
 * If an item exists on both sides A and B and one side was updated, the item will be updated on the opposite side.
 *
 * @param <I> the type of an item.
 */
public class MutableItemSynchronization<I> extends Synchronization<I>
{
    private ConflictResolver<I> conflictResolver;
    
    public MutableItemSynchronization(ItemSet<I> itemSetA, ItemSet<I> itemSetB, Status status)
    {
        super(itemSetA, itemSetB, status);
    }
    
    @Override
    public void setConflictResolver(ConflictResolver<I> conflictResolver)
    {
        Objects.requireNonNull(conflictResolver);
        conflictResolver.setItemSets(getItemSetA(), getItemSetB(), getStatus());
        this.conflictResolver = conflictResolver;
    }
    
    @Override
    void synchronizeItem(String itemId) throws Exception
    {
        boolean existsOnA = getItemSetA().contains(itemId);
        boolean existsOnB = getItemSetB().contains(itemId);
        boolean existsInStatus = getStatus().contains(itemId);
    
        if(existsOnA && existsOnB)
        {
            String etagA = getItemSetA().getEtag(itemId);
            String etagB = getItemSetB().getEtag(itemId);
        
            if(existsInStatus)
            {
                String statusEtag = getStatus().getEtag(itemId);
                if(wasUpdatedOnA(statusEtag, etagA) && wasUpdatedOnB(statusEtag, etagB))
                    resolveConflict(itemId, etagA, etagB);
                else if(wasUpdatedOnA(statusEtag, etagA))
                    updateOnB(itemId, etagA);
                else if(wasUpdatedOnB(statusEtag, etagB))
                    updateOnA(itemId, etagB);
            }
            else
            {
                if(Objects.equals(etagA, etagB))
                    addToStatus(itemId, etagA);
                else
                    resolveConflict(itemId, etagA, etagB);
            }
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
            deleteFromStatus(itemId);
    }
    
    private void addToStatus(String itemId, String etag)
    {
        getStatus().put(itemId, etag);
    }
    
    private void deleteFromStatus(String itemId)
    {
        getStatus().delete(itemId);
    }
    
    private void deleteFromA(String itemId) throws Exception
    {
        getItemSetA().delete(itemId);
        deleteFromStatus(itemId);
    }
    
    private void deleteFromB(String itemId) throws Exception
    {
        getItemSetB().delete(itemId);
        deleteFromStatus(itemId);
    }
    
    private void createOnA(String itemId) throws Exception
    {
        I item = getItemSetB().getItem(itemId);
        getItemSetA().addItem(itemId, item);
        String etag = getItemSetB().getEtag(itemId);
        addToStatus(itemId, etag);
    }
    
    private void createOnB(String itemId) throws Exception
    {
        I item = getItemSetA().getItem(itemId);
        getItemSetB().addItem(itemId, item);
        String etag = getItemSetA().getEtag(itemId);
        addToStatus(itemId, etag);
    }
    
    private void updateOnB(String itemId, String etagA) throws Exception
    {
        I item = getItemSetA().getItem(itemId);
        getItemSetB().updateItem(itemId, item);
        addToStatus(itemId, etagA);
    }
    
    private void updateOnA(String itemId, String etagB) throws Exception
    {
        I item = getItemSetB().getItem(itemId);
        getItemSetA().updateItem(itemId, item);
        addToStatus(itemId, etagB);
    }
    
    private boolean wasUpdatedOnA(String statusEtag, String etagA)
    {
        return !Objects.equals(statusEtag, etagA);
    }
    
    private boolean wasUpdatedOnB(String statusEtag, String etagB)
    {
        return !Objects.equals(statusEtag, etagB);
    }
    
    private void resolveConflict(String itemId, String etagA, String etagB) throws Exception
    {
        if(conflictResolver == null)
            throw new UnresolvedConflictException();
        
        conflictResolver.setContext(itemId, etagA, etagB);
        conflictResolver.resolve(itemId, etagA, etagB);
    }
}
