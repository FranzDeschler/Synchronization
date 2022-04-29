package cloud.codestore.synchronization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The synchronization")
class ProgressListenerTest
{
    @Mock
    private ProgressListener progressListener;
    @Mock
    private Synchronization<Object> synchronization;
    
    private ItemProcessor itemProcessor;
    
    @BeforeEach
    void setUp()
    {
        itemProcessor = new DefaultItemProcessor(synchronization, progressListener);
    }
    
    @Test
    @DisplayName("calls progress listener when synchronization of an item starts")
    void callProgressListenerOnStart()
    {
        itemProcessor.process(Set.of("1", "2", "3"));
        
        verify(progressListener).synchronizationStarted("1");
        verify(progressListener).synchronizationStarted("2");
        verify(progressListener).synchronizationStarted("3");
    }
    
    @Test
    @DisplayName("calls progress listener when the synchronization of an item successfully finishes")
    void callProgressListenerOnFinish()
    {
        itemProcessor.process(Set.of("1", "2", "3"));
    
        verify(progressListener).synchronizationFinished("1");
        verify(progressListener).synchronizationFinished("2");
        verify(progressListener).synchronizationFinished("3");
    }
    
    @Test
    @DisplayName("calls progress listener when the synchronization of an item fails")
    void callProgressListenerOnError() throws Exception
    {
        Exception exception = new UnresolvedConflictException();
        doThrow(exception).when(synchronization).synchronizeItem(anyString());
    
        itemProcessor.process(Set.of("1"));
    
        verify(progressListener).synchronizationFailed("1", exception);
    }
}