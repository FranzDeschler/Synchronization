package cloud.codestore.synchronization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("If a mutable item")
class MutableItemSynchronizationTest
{
    private static final String ITEM_ID = "12345";
    private static final String ETAG = "etag";
    
    @Mock
    private ItemSet<Object> itemSetA;
    @Mock
    private ItemSet<Object> itemSetB;
    @Mock
    private Status status;
    @Mock
    private ConflictResolver<Object> conflictResolver;
    @Mock
    private Object item;
    
    private MutableItemSynchronization<Object> synchronization;
    
    @BeforeEach
    void setUp()
    {
        lenient().when(itemSetA.getItemIds()).thenReturn(Collections.singleton(ITEM_ID));
        lenient().when(itemSetB.getItemIds()).thenReturn(Collections.singleton(ITEM_ID));
        lenient().when(status.getItemIds()).thenReturn(Collections.singleton(ITEM_ID));
        
        synchronization = new MutableItemSynchronization<>(itemSetA, itemSetB, status);
        synchronization.setConflictResolver(conflictResolver);
    }
    
    @Test
    @DisplayName("was created on A - it must be created on B and inserted into the status")
    void createdOnA() throws Exception
    {
        when(itemSetA.getEtag(ITEM_ID)).thenReturn(ETAG);
        
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetA.getItem(ITEM_ID)).thenReturn(item);
        when(itemSetB.contains(ITEM_ID)).thenReturn(false);
        when(status.contains(ITEM_ID)).thenReturn(false);
        
        synchronization.synchronize();
        
        verify(itemSetB).addItem(ITEM_ID, item);
        verify(status).put(ITEM_ID, ETAG);
    }
    
    @Test
    @DisplayName("was created on B - it must be created on A and inserted into the status")
    void createdOnB() throws Exception
    {
        when(itemSetB.getEtag(ITEM_ID)).thenReturn(ETAG);
        
        when(itemSetA.contains(ITEM_ID)).thenReturn(false);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.getItem(ITEM_ID)).thenReturn(item);
        when(status.contains(ITEM_ID)).thenReturn(false);
        
        synchronization.synchronize();
        
        verify(itemSetA).addItem(ITEM_ID, item);
        verify(status).put(ITEM_ID, ETAG);
    }
    
    @Test
    @DisplayName("was deleted on A - it must be deleted on B and in the status")
    void deletedOnA() throws Exception
    {
        when(itemSetA.contains(ITEM_ID)).thenReturn(false);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(true);
        
        synchronization.synchronize();
        
        verify(itemSetB).delete(ITEM_ID);
        verify(status).delete(ITEM_ID);
    }
    
    @Test
    @DisplayName("was deleted on B - it must be deleted on A and from the status")
    void deletedOnB() throws Exception
    {
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(false);
        when(status.contains(ITEM_ID)).thenReturn(true);
        
        synchronization.synchronize();
        
        verify(itemSetA).delete(ITEM_ID);
        verify(status).delete(ITEM_ID);
    }
    
    @Test
    @DisplayName("does not exist on A and B - it must be removed from the status")
    void itemDoesNotExist()
    {
        when(itemSetA.contains(ITEM_ID)).thenReturn(false);
        when(itemSetB.contains(ITEM_ID)).thenReturn(false);
        when(status.contains(ITEM_ID)).thenReturn(true);
        
        synchronization.synchronize();
        
        verify(status).delete(ITEM_ID);
    }
    
    @Test
    @DisplayName("is missing in the status - and etags are equal - it must be added to the status")
    void missingInStatusNoConflict() throws Exception
    {
        when(itemSetA.getEtag(ITEM_ID)).thenReturn(ETAG);
        when(itemSetB.getEtag(ITEM_ID)).thenReturn(ETAG);
        
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(false);
        
        synchronization.synchronize();
        
        verify(status).put(ITEM_ID, ETAG);
    }
    
    @Test
    @DisplayName("is missing in the status - and etags are different - there is a conflict")
    void missingInStatusConflict() throws Exception
    {
        when(itemSetA.getEtag(ITEM_ID)).thenReturn("etagA");
        when(itemSetB.getEtag(ITEM_ID)).thenReturn("etagB");
        
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(false);
        
        synchronization.synchronize();
        
        verify(conflictResolver).resolve(ITEM_ID, "etagA",  "etagB");
    }
    
    @Test
    @DisplayName("was updated on A - it must be updated on B and the status")
    void updatedOnA() throws Exception
    {
        when(status.getEtag(ITEM_ID)).thenReturn(ETAG);
        when(itemSetA.getEtag(ITEM_ID)).thenReturn("etagA");
        when(itemSetB.getEtag(ITEM_ID)).thenReturn(ETAG);
        
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetA.getItem(ITEM_ID)).thenReturn(item);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(true);
        
        synchronization.synchronize();
        
        verify(itemSetB).updateItem(ITEM_ID, item);
        verify(status).put(ITEM_ID, "etagA");
    }
    
    @Test
    @DisplayName("was updated on B - it must be updated on A and in the status")
    void updatedOnB() throws Exception
    {
        when(status.getEtag(ITEM_ID)).thenReturn(ETAG);
        when(itemSetA.getEtag(ITEM_ID)).thenReturn(ETAG);
        when(itemSetB.getEtag(ITEM_ID)).thenReturn("etagB");
        
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.getItem(ITEM_ID)).thenReturn(item);
        when(status.contains(ITEM_ID)).thenReturn(true);
        
        synchronization.synchronize();
        
        verify(itemSetA).updateItem(ITEM_ID, item);
        verify(status).put(ITEM_ID, "etagB");
    }
    
    @Test
    @DisplayName("was updated on A and B - there is a conflict")
    void updatedOnAB() throws Exception
    {
        when(status.getEtag(ITEM_ID)).thenReturn(ETAG);
        when(itemSetA.getEtag(ITEM_ID)).thenReturn("etagA");
        when(itemSetB.getEtag(ITEM_ID)).thenReturn("etagB");
        
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(true);
        
        synchronization.synchronize();
    
        verify(conflictResolver).resolve(ITEM_ID, "etagA",  "etagB");
    }
    
    @Test
    @DisplayName("was not updated - nothing happens")
    void notUpdated() throws Exception
    {
        lenient().when(status.getEtag(ITEM_ID)).thenReturn(ETAG);
        lenient().when(itemSetA.getEtag(ITEM_ID)).thenReturn(ETAG);
        lenient().when(itemSetB.getEtag(ITEM_ID)).thenReturn(ETAG);
        
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(true);
        
        synchronization.synchronize();
        
        verify(itemSetB, never()).getItem(ITEM_ID);
        verify(itemSetB, never()).addItem(ITEM_ID, item);
        verify(itemSetB, never()).updateItem(ITEM_ID, item);
        verify(itemSetB, never()).delete(ITEM_ID);
        
        verify(itemSetA, never()).getItem(ITEM_ID);
        verify(itemSetA, never()).addItem(ITEM_ID, item);
        verify(itemSetA, never()).updateItem(ITEM_ID, item);
        verify(itemSetA, never()).delete(ITEM_ID);
    }
    
    @Test
    @DisplayName("produces a conflict an UnresolvedConflictException is thrown by default")
    void unresolvedConflictExceptionByDefault() throws Exception
    {
        synchronization = new MutableItemSynchronization<>(itemSetA, itemSetB, status);
    
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(true);
        
        when(itemSetA.getEtag(ITEM_ID)).thenReturn("etagA");
        when(itemSetB.getEtag(ITEM_ID)).thenReturn("etagB");
        when(status.getEtag(ITEM_ID)).thenReturn(ETAG);
    
        Assertions.assertThrows(UnresolvedConflictException.class, () -> synchronization.synchronizeItem(ITEM_ID));
    }
}