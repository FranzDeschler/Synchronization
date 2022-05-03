package cloud.codestore.synchronization.helper;

import cloud.codestore.synchronization.Status;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Abstract base class for {@link Status} which contains immutable items.
 */
public abstract class AbstractImmutableItemStatus implements Status
{
    private final Set<String> itemIDs;
    
    /**
     * Creates a new {@link Status} which contains immutable items.
     *
     * @param itemIDs the IDs of the items.
     */
    public AbstractImmutableItemStatus(Set<String> itemIDs)
    {
        this.itemIDs = Collections.synchronizedSet(itemIDs);
    }
    
    @Override
    public Set<String> getItemIds()
    {
        return Collections.unmodifiableSet(itemIDs);
    }
    
    @Override
    public boolean contains(String itemId)
    {
        return itemIDs.contains(itemId);
    }
    
    @Override
    public void put(String itemId)
    {
        itemIDs.add(itemId);
    }
    
    @Override
    public void put(String itemId, String etag)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getEtag(String itemId)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void delete(String itemId)
    {
        itemIDs.remove(itemId);
    }
    
    @Override
    public void save() throws IOException
    {
        save(itemIDs);
    }
    
    /**
     * Saves this status.
     * This method is called as soon as the synchronization has been finished.
     *
     * @param itemIDs the content of the status so save.
     *
     * @throws IOException if the status could not be saved.
     */
    public abstract void save(Set<String> itemIDs) throws IOException;
}
