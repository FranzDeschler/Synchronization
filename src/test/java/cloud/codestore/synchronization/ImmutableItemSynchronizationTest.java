package cloud.codestore.synchronization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("If an immutable item")
class ImmutableItemSynchronizationTest
{
    private static final String ITEM_ID = "12345";
    
    @Mock
    private ItemSet<Object> itemSetA;
    @Mock
    private ItemSet<Object> itemSetB;
    @Mock
    private Status status;
    @Mock
    private Object item;
    
    private Synchronization<Object> synchronization;
    
    @BeforeEach
    void setUp()
    {
        when(itemSetA.getItemIds()).thenReturn(Collections.singleton(ITEM_ID));
        when(itemSetB.getItemIds()).thenReturn(Collections.singleton(ITEM_ID));
        when(status.getItemIds()).thenReturn(Collections.singleton(ITEM_ID));
        
        synchronization = new ImmutableItemSynchronization<>(itemSetA, itemSetB, status);
    }
    
    @Test
    @DisplayName("was created on A - it must be created on B and inserted into the status")
    void createdOnA() throws Exception
    {
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(false);
        when(status.contains(ITEM_ID)).thenReturn(false);
        when(itemSetA.getItem(ITEM_ID)).thenReturn(item);
        
        synchronization.synchronize();
        
        verify(itemSetB).addItem(ITEM_ID, item);
        verify(status).put(ITEM_ID);
    }
    
    @Test
    @DisplayName("was created on B - it must be created on A and inserted into the status")
    void createdOnB() throws Exception
    {
        when(itemSetA.contains(ITEM_ID)).thenReturn(false);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(false);
        when(itemSetB.getItem(ITEM_ID)).thenReturn(item);
        
        synchronization.synchronize();
        
        verify(itemSetA).addItem(ITEM_ID, item);
        verify(status).put(ITEM_ID);
    }
    
    @Test
    @DisplayName("was deleted on A - it must be deleted on B and the status")
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
    @DisplayName("was deleted on B - it must be deleted on A and the status")
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
    @DisplayName("is missing in the status - it must be added to the status")
    void missingInStatus()
    {
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(false);
        
        synchronization.synchronize();
        
        verify(status).put(ITEM_ID);
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
    @DisplayName("was not modified - nothing happens")
    void notModified() throws Exception
    {
        when(itemSetA.contains(ITEM_ID)).thenReturn(true);
        when(itemSetB.contains(ITEM_ID)).thenReturn(true);
        when(status.contains(ITEM_ID)).thenReturn(true);
        
        synchronization.synchronize();
        
        verify(itemSetB, never()).getItem(anyString());
        verify(itemSetB, never()).addItem(anyString(), any());
        verify(itemSetB, never()).delete(anyString());
        
        verify(itemSetA, never()).getItem(anyString());
        verify(itemSetA, never()).addItem(anyString(), any());
        verify(itemSetA, never()).delete(anyString());
    }
}