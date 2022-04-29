package cloud.codestore.synchronization;

import java.util.Set;

/**
 * An item processor which processes the items synchronously one after the other.
 */
class DefaultItemProcessor extends ItemProcessor
{
    DefaultItemProcessor(Synchronization<?> synchronization, ProgressListener progressListener)
    {
        super(synchronization, progressListener);
    }
    
    @Override
    void process(Set<String> itemIds)
    {
        for(String itemId : itemIds)
        {
            process(itemId);
            if(isCanceled())
                return;
        }
    }
}
