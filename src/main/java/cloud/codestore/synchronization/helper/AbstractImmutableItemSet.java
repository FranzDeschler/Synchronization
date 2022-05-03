package cloud.codestore.synchronization.helper;

import cloud.codestore.synchronization.ItemSet;

import java.util.Collections;
import java.util.Set;

/**
 * Abstract base class for {@link ItemSet}s of immutable items.
 *
 * @param <I> the type of an item.
 */
public abstract class AbstractImmutableItemSet<I> implements ItemSet<I>
{
    private final Set<String> itemsIDs;
    
    /**
     * Creates a new {@link ItemSet} which contains immutable items.
     *
     * @param itemIDs the IDs of the items.
     */
    public AbstractImmutableItemSet(Set<String> itemIDs)
    {
        this.itemsIDs = Collections.synchronizedSet(itemIDs);
    }
    
    @Override
    public Set<String> getItemIds()
    {
        return Collections.unmodifiableSet(itemsIDs);
    }
    
    @Override
    public boolean contains(String itemId)
    {
        return itemsIDs.contains(itemId);
    }
    
    @Override
    public String getEtag(String itemId)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void updateItem(String itemId, I item)
    {
        throw new UnsupportedOperationException();
    }
}
