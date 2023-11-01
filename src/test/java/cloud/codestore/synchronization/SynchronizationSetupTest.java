package cloud.codestore.synchronization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The Synchronization object")
class SynchronizationSetupTest
{
    @Mock
    private ItemSet<Object> itemSetA;
    @Mock
    private ItemSet<Object> itemSetB;
    @Mock
    private Status status;
    
    private Synchronization<Object> synchronization;
    
    @BeforeEach
    void setUp()
    {
        synchronization = new ImmutableItemSynchronization<>(itemSetA, itemSetB, status);
    }
    
    @Test
    @DisplayName("uses the default ItemProcessor and ProgressListener by default")
    void defaultItemProcessor() throws Exception
    {
        synchronization.synchronize();
        
        ItemProcessor itemProcessor = getItemProcessor();
        assertEquals(itemProcessor.getClass(), DefaultItemProcessor.class);
        ProgressListener progressListener = getProgressListener(itemProcessor);
        assertEquals(progressListener.getClass(), DefaultProgressListener.class);
    }
    
    @Test
    @DisplayName("uses a custom ProgressListener")
    void defaultItemProcessorWithCustomProgressListener() throws Exception
    {
        synchronization.setProgressListener(new CustomProgressListener());
        synchronization.synchronize();
        
        ItemProcessor itemProcessor = getItemProcessor();
        assertEquals(itemProcessor.getClass(), DefaultItemProcessor.class);
        ProgressListener progressListener = getProgressListener(itemProcessor);
        assertEquals(progressListener.getClass(), CustomProgressListener.class);
    }
    
    @Test
    @DisplayName("uses the ConcurrentItemProcessor")
    void concurrentItemProcessor() throws Exception
    {
        synchronization.setThreadCount(1);
        synchronization.synchronize();
    
        ItemProcessor itemProcessor = getItemProcessor();
        assertEquals(itemProcessor.getClass(), ConcurrentItemProcessor.class);
    }

    @Test
    @DisplayName("passes the total number of items to the progress listener")
    void callProgressListenerNrOfItems()
    {
        ProgressListener progressListener = mock(ProgressListener.class);

        when(itemSetA.getItemIds()).thenReturn(Set.of("1", "2", "3"));
        when(itemSetB.getItemIds()).thenReturn(Set.of("3", "4", "5"));
        when(status.getItemIds()).thenReturn(Set.of("4", "5", "6"));

        synchronization.setProgressListener(progressListener);
        synchronization.synchronize();

        verify(progressListener).numberOfItems(6);
    }

    private ItemProcessor getItemProcessor() throws Exception
    {
        Field field = Synchronization.class.getDeclaredField("itemProcessor");
        field.setAccessible(true);
        return (ItemProcessor) field.get(synchronization);
    }
    
    private ProgressListener getProgressListener(ItemProcessor itemProcessor) throws Exception
    {
        Field field = ItemProcessor.class.getDeclaredField("progressListener");
        field.setAccessible(true);
        return (ProgressListener) field.get(itemProcessor);
    }
    
    private static class CustomProgressListener implements ProgressListener
    {
        @Override
        public void numberOfItems(int numberOfItems) {}

        @Override
        public void synchronizationStarted(String itemId) {}
    
        @Override
        public void synchronizationFinished(String itemId) {}
    
        @Override
        public void synchronizationFailed(String itemId, Throwable exception) {}
    }
}
