package cloud.codestore.synchronization;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * An item processor which processes the items concurrently.
 */
class ConcurrentItemProcessor extends ItemProcessor
{
    private ExecutorService executorService;
    private Semaphore semaphore;
    private int threadCount;
    
    ConcurrentItemProcessor(Synchronization<?> synchronization, ProgressListener progressListener, int threadCount)
    {
        super(synchronization, progressListener);
        this.threadCount = threadCount;
    }
    
    @Override
    synchronized void cancel()
    {
        super.cancel();
    }
    
    @Override
    synchronized boolean isCanceled()
    {
        return super.isCanceled();
    }
    
    @Override
    void process(Set<String> itemIds)
    {
        executorService = Executors.newFixedThreadPool(threadCount);
        semaphore = new Semaphore(threadCount);
    
        try
        {
            for(String itemId : itemIds)
            {
                semaphore.acquire();
                if(isCanceled())
                    break;
            
                process(itemId);
            }
        
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
    
    @Override
    void process(String itemId)
    {
        executorService.submit(() -> {
            try
            {
                super.process(itemId);
            }
            finally
            {
                semaphore.release();
            }
        });
    }
}
