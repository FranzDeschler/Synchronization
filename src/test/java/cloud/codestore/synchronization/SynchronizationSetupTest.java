package cloud.codestore.synchronization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
@DisplayName("The Synchronization object")
public class SynchronizationSetupTest
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
    @DisplayName("uses the DefaultItemProcessor and DefaultProgressListener by default")
    void defaultItemProcessor() throws Exception
    {
        synchronization.synchronize();
        
        ItemProcessor itemProcessor = getItemProcessor();
        Assertions.assertEquals(itemProcessor.getClass(), DefaultItemProcessor.class);
    }
    
    @Test
    @DisplayName("uses the CustomProgressListener")
    void defaultItemProcessorWithCustomProgressListener() throws Exception
    {
        synchronization.setProgressListener(new CustomProgressListener());
        synchronization.synchronize();
        
        ItemProcessor itemProcessor = getItemProcessor();
        Assertions.assertEquals(itemProcessor.getClass(), DefaultItemProcessor.class);
        ProgressListener progressListener = getProgressListener(itemProcessor);
        Assertions.assertEquals(progressListener.getClass(), CustomProgressListener.class);
    }
    
    @Test
    @DisplayName("uses the ConcurrentItemProcessor")
    void concurrentItemProcessor() throws Exception
    {
        synchronization.setThreadCount(1);
        synchronization.synchronize();
    
        ItemProcessor itemProcessor = getItemProcessor();
        Assertions.assertEquals(itemProcessor.getClass(), ConcurrentItemProcessor.class);
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
