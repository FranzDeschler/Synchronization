package cloud.codestore.synchronization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The concurrent item processor")
class ConcurrentItemProcessorTest
{
    @Mock
    private Synchronization<Object> synchronization;
    
    @Test
    @DisplayName("processes items concurrently")
    void synchronousProcessing() throws Exception
    {
        doAnswer(invocation -> {
            Thread.sleep(1000);
            return null;
        }).when(synchronization).synchronizeItem(anyString());
    
        ItemProcessor itemProcessor = createProcessor(3);
        itemProcessor.process(Set.of());
    
        long start = System.currentTimeMillis();
        itemProcessor.process(Set.of("1", "2", "3"));
        long end = System.currentTimeMillis();
    
        assertTrue(end - start < 3100);
        verify(synchronization, times(3)).synchronizeItem(anyString());
    }
    
    @Test
    @DisplayName("stops processing further items when cancelled")
    void stopProcessingOnCancel() throws Exception
    {
        ItemProcessor itemProcessor = createProcessor(1);
        AtomicInteger count = new AtomicInteger();
        
        doAnswer(invocation -> {
            if(count.incrementAndGet() == 3)
                itemProcessor.cancel();
            
            Thread.sleep(500);
            return null;
        }).when(synchronization).synchronizeItem(anyString());
    
        itemProcessor.process(Set.of("1", "2", "3", "4", "5"));
        
        assertTrue(itemProcessor.isCanceled());
        verify(synchronization, times(3)).synchronizeItem(anyString());
    }
    
    @Test
    @DisplayName("waits for a free thread for processing items")
    void waitForFreeThread() throws Exception
    {
        ItemProcessor itemProcessor = createProcessor(1);
        long start = System.currentTimeMillis();
        doAnswer(invocation -> {
            if("1".equals(invocation.getArgument(0)))
            {
                Thread.sleep(2500);
            }
            else
            {
                // item 2 is processed after 2.5 seconds
                assertTrue(start - System.currentTimeMillis() >= 2500);
            }
            return null;
        }).when(synchronization).synchronizeItem(anyString());
        
        itemProcessor.process(Set.of("1", "2"));
    }
    
    @Test
    @DisplayName("stops processing further items when cancelled")
    void cancelStopsFurtherProcessing() throws Exception
    {
        ItemProcessor itemProcessor = createProcessor(1);
        AtomicInteger count = new AtomicInteger();
        
        doAnswer(invocation -> {
            if(count.incrementAndGet() == 3)
                itemProcessor.cancel();
    
            Thread.sleep(500);
            return null;
        }).when(synchronization).synchronizeItem(anyString());
        
        itemProcessor.process(Set.of("1", "2", "3", "4", "5", "6"));
        
        verify(synchronization, times(3)).synchronizeItem(anyString());
    }
    
    @Test
    @DisplayName("waits for active tasks to finish when cancelled")
    void cancelWaitsForActiveTasks() throws Exception
    {
        ItemProcessor itemProcessor = createProcessor(3);
        AtomicInteger count = new AtomicInteger();
        AtomicInteger finishedTasks = new AtomicInteger();
    
        doAnswer(invocation -> {
            if(count.incrementAndGet() == 3)
                itemProcessor.cancel();
        
            Thread.sleep(1000);
            finishedTasks.incrementAndGet();
            return null;
        }).when(synchronization).synchronizeItem(anyString());
    
        itemProcessor.process(Set.of("1", "2", "3"));
        
        assertEquals(3, finishedTasks.get());
    }
    
    private ItemProcessor createProcessor(int threadCount)
    {
        return new ConcurrentItemProcessor(synchronization, new DefaultProgressListener(), threadCount);
    }
}