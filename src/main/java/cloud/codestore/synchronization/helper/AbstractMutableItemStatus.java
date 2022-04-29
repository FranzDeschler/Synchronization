package cloud.codestore.synchronization.helper;

import cloud.codestore.synchronization.Status;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for {@link Status} which contains mutable items.
 */
public abstract class AbstractMutableItemStatus implements Status
{
    private final Map<String, String> itemIdToEtagMap;
    
    /**
     * @param itemIdToEtagMap a map which maps the id of an item to its corresponding etag.
     */
    public AbstractMutableItemStatus(Map<String, String> itemIdToEtagMap)
    {
        this.itemIdToEtagMap = Collections.synchronizedMap(itemIdToEtagMap);
    }
    
    @Override
    public Set<String> getItemIds()
    {
        return Collections.unmodifiableSet(itemIdToEtagMap.keySet());
    }
    
    @Override
    public boolean contains(String itemId)
    {
        return itemIdToEtagMap.containsKey(itemId);
    }
    
    @Override
    public void put(String itemId)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void put(String itemId, String etag)
    {
        itemIdToEtagMap.put(itemId, etag);
    }
    
    @Override
    public String getEtag(String itemId)
    {
        return itemIdToEtagMap.get(itemId);
    }
    
    @Override
    public void delete(String itemId)
    {
        itemIdToEtagMap.remove(itemId);
    }
    
    @Override
    public void save() throws IOException
    {
        save(itemIdToEtagMap);
    }
    
    /**
     * Saves this status.
     * This method is called as soon as the synchronization has been finished.
     *
     * @param itemIdToEtagMap the content of the status so save.
     *
     * @throws IOException if the status could not be saved.
     */
    public abstract void save(Map<String, String> itemIdToEtagMap) throws IOException;
}
