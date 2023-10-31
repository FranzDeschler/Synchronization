package cloud.codestore.synchronization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The default item processor")
class DefaultItemProcessorTest
{
    @Mock
    private Synchronization<Object> synchronization;
    private ItemProcessor itemProcessor;
    
    @BeforeEach
    void setUp()
    {
        itemProcessor = new DefaultItemProcessor(synchronization, new DefaultProgressListener());
    }
    
    @Test
    @DisplayName("processes items in a row")
    void synchronousProcessing() throws Exception
    {
        doAnswer(invocation -> {
            Thread.sleep(1000);
            return null;
        }).when(synchronization).synchronizeItem(anyString());
    
        long start = System.currentTimeMillis();
        itemProcessor.process(Set.of("1", "2", "3", "4", "5"));
        long end = System.currentTimeMillis();
    
        assertTrue(end - start >= 5000);
        verify(synchronization, times(5)).synchronizeItem(anyString());
    }
    
    @Test
    @DisplayName("stops processing further items when cancelled")
    void stopProcessingOnCancel() throws Exception
    {
        doAnswer(new Answer()
        {
            private int count;

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (++count >= 3)
                    itemProcessor.cancel();
                return null;
            }
        }).when(synchronization).synchronizeItem(anyString());
    
        itemProcessor.process(Set.of("1", "2", "3", "4", "5"));
        
        assertTrue(itemProcessor.isCanceled());
        verify(synchronization, times(3)).synchronizeItem(anyString());
    }
}