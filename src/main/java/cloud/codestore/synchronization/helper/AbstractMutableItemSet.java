package cloud.codestore.synchronization.helper;

import cloud.codestore.synchronization.ItemSet;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for {@link ItemSet}s of mutable items.
 *
 * @param <I> the type of an item.
 */
public abstract class AbstractMutableItemSet<I> implements ItemSet<I>
{
    private final Map<String, String> itemIdToEtagMap;
    
    /**
     * Creates a new {@link ItemSet} which contains mutable items.
     *
     * @param itemIdToEtagMap a map which contains the IDs of the items as key,
     * and the etag of the corresponding item as value.
     */
    public AbstractMutableItemSet(Map<String, String> itemIdToEtagMap)
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
    public String getEtag(String itemId)
    {
        return itemIdToEtagMap.get(itemId);
    }
}
