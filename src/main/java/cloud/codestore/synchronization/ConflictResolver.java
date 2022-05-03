package cloud.codestore.synchronization;

import java.util.Objects;

/**
 * The {@link ConflictResolver} is called whenever there is a conflict (an item was updated on both sets).<br>
 * It provides several convenient methods to solve the conflict:<br>
 * <ul>
 *     <li>{@link ConflictResolver#applyItemA()} : replace the item on B with the item on A</li>
 *     <li>{@link ConflictResolver#applyItemB()} : replace the item on A with the item on B</li>
 *     <li>{@link ConflictResolver#applyItem(I, String)} : replace the item on both sides</li>
 * </ul>
 *
 * @param <I> the type of an item.
 */
public abstract class ConflictResolver<I>
{
    private static final ThreadLocal<String> itemId = new ThreadLocal<>();
    private static final ThreadLocal<String> etagA = new ThreadLocal<>();
    private static final ThreadLocal<String> etagB = new ThreadLocal<>();
    
    private ItemSet<I> itemSetA;
    private ItemSet<I> itemSetB;
    private Status status;
    
    void setItemSets(ItemSet<I> itemSetA, ItemSet<I> itemSetB, Status status)
    {
        this.itemSetA = itemSetA;
        this.itemSetB = itemSetB;
        this.status = status;
    }
    
    void setContext(String itemId, String etagA, String etagB)
    {
        ConflictResolver.itemId.set(itemId);
        ConflictResolver.etagA.set(etagA);
        ConflictResolver.etagB.set(etagB);
    }
    
    /**
     * @return the item from {@link ItemSet} A.
     *
     * @throws Exception if the item could not be loaded.
     */
    public I getItemA() throws Exception
    {
        return itemSetA.getItem(itemId.get());
    }
    
    /**
     * @return the item from {@link ItemSet} B.
     *
     * @throws Exception if the item could not be loaded.
     */
    public I getItemB() throws Exception
    {
        return itemSetB.getItem(itemId.get());
    }
    
    /**
     * Reads the item from A and updates it on B.
     *
     * @throws Exception if the item could not be loaded from A or if it could not be updated in B.
     */
    public void applyItemA() throws Exception
    {
        I item = getItemA();
        itemSetB.updateItem(itemId.get(), item);
        status.put(itemId.get(), etagA.get());
    }
    
    /**
     * Reads the item from B and updates it on A.
     *
     * @throws Exception if the item could not be loaded from B or if it could not be updated in A.
     */
    public void applyItemB() throws Exception
    {
        I item = getItemB();
        itemSetA.updateItem(itemId.get(), item);
        status.put(itemId.get(), etagB.get());
    }
    
    /**
     * Replaces the item on both sides, A and B with the given item.
     * This can be used if the items from both sets were merged in some way.
     *
     * @param item the item which should be stored on both sides.
     * @param etag the etag of the item.
     *
     * @throws Exception if the item could not be loaded or updated.
     */
    public void applyItem(I item, String etag) throws Exception
    {
        Objects.requireNonNull(item, "The item must not be null");
        Objects.requireNonNull(etag, "The etag must not be null");
        
        itemSetA.updateItem(itemId.get(), item);
        itemSetB.updateItem(itemId.get(), item);
        status.put(itemId.get(), etag);
    }
    
    /**
     * Resolves the conflict.
     * <br/><br/>
     * Note that the synchronization continues as soon as this method returns.
     * If you want to show a user input to let the user decide what to do, make sure this method blocks until
     * the user interaction has been finished.
     * <br/><br/>
     * If the conflict could not be resolved automatically, or if the user canceled the process,
     * this method shoud throw an {@link UnresolvedConflictException}.
     * <br/><br/>
     *
     * @param itemId the ID of the affected item.
     * @param etagA the etag of the item on side A.
     * @param etagB the etag of the item on side B.
     *
     * @throws UnresolvedConflictException if the conflict could not be resolved.
     * @throws Exception if there was an error.
     */
    public abstract void resolve(String itemId, String etagA, String etagB) throws Exception;
}
