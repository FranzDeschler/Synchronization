package cloud.codestore.synchronization;

import java.util.Set;

/**
 * Represents a set of items.
 *
 * @param <I> the type of an item.
 */
public interface ItemSet<I>
{
    /**
     * @return the IDs of all items in this {@link ItemSet}.
     */
    Set<String> getItemIds();
    
    /**
     * Checks whether this set contains the item with the given ID.
     *
     * @param itemId the id of an item.
     *
     * @return whether this set contains the given item.
     */
    boolean contains(String itemId);
    
    /**
     * Reads the etag of the item with the given ID from this set.
     *
     * @param itemId the ID of an item.
     *
     * @return the etag of the corresponding item.
     *
     * @throws Exception if the etag could not be calculated.
     */
    String getEtag(String itemId) throws Exception;
    
    /**
     * Reads the item with the given ID from this set.
     *
     * @param itemId the id of the item which should be read.
     *
     * @return the item with the corresponding id.
     *
     * @throws Exception if the item could not be loaded.
     */
    I getItem(String itemId) throws Exception;
    
    /**
     * Adds the given item in this set.
     *
     * @param itemId the ID of the item.
     * @param item the item which should be saved.
     *
     * @throws Exception if the item could not be saved.
     */
    void addItem(String itemId, I item) throws Exception;
    
    /**
     * Deletes the item with the given id from this set.
     *
     * @param itemId the id of the item which should be deleted.
     *
     * @throws Exception if the item could not be deleted.
     */
    void delete(String itemId) throws Exception;
    
    /**
     * Updates the given item in this set.
     *
     * @param itemId the id of the item.
     * @param item the new item which should replace the old one.
     *
     * @throws Exception if the item could not be updated.
     */
    void updateItem(String itemId, I item) throws Exception;
}
