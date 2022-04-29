package cloud.codestore.synchronization;

import java.io.IOException;
import java.util.Set;

/**
 * Represents the set of items which were present after the last synchronization.
 * It is used to determine whether an item was deleted or created.
 * <br/>
 * See <a href="https://unterwaditzer.net/2016/sync-algorithm.html">unterwaditzer.net</a>
 */
public interface Status
{
    /**
     * @return the IDs of all items which are present in the status.
     */
    Set<String> getItemIds();
    
    /**
     * @param itemId the id of an item.
     *
     * @return whether the item is present in the status.
     */
    boolean contains(String itemId);
    
    /**
     * Adds the given item ID to the status.
     *
     * @param itemId the ID of an item.
     */
    void put(String itemId);
    
    /**
     * Adds the given item ID and the etag to the status.
     * If the ID already exists in the status, the corresponding etag is updated.
     *
     * @param itemId the ID of an item.
     * @param etag the etag of the corresponding item.
     */
    void put(String itemId, String etag);
    
    /**
     * Reads the etag of the item with the given ID from the status.
     *
     * @param itemId the ID of an item.
     *
     * @return the etag of the corresponding item.
     */
    String getEtag(String itemId);
    
    /**
     * Deletes the item with the given ID from the status.
     *
     * @param itemId the id of an item.
     */
    void delete(String itemId);
    
    /**
     * Saves this status.
     * This method is called as soon as the synchronization has been finished.
     */
    void save() throws IOException;
}
