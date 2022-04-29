package cloud.codestore.synchronization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("A conflict resolver")
class ConflictResolverTest
{
    private static final String ITEM_ID = "12345";
    private static final String ETAG_A = "etagA";
    private static final String ETAG_B = "etagB";
    
    @Mock
    private ItemSet<Object> itemSetA;
    @Mock
    private ItemSet<Object> itemSetB;
    @Mock
    private Status status;
    @Mock
    private Object item;
    
    private ConflictResolver<Object> conflictResolver;
    
    @BeforeEach
    void setUp() throws Exception
    {
        lenient().when(itemSetA.getItem(ITEM_ID)).thenReturn(item);
        lenient().when(itemSetB.getItem(ITEM_ID)).thenReturn(item);
    }
    
    @Test
    @DisplayName("may replace item B with item A")
    void applyA() throws Exception
    {
        conflictResolver = init(new ConflictResolver<>() {
            @Override
            public void resolve(String itemId, String etagA, String etagB) throws Exception
            {
                applyItemA();
            }
        });
        
        conflictResolver.resolve(ITEM_ID, ETAG_A, ETAG_B);
        
        verify(itemSetB).updateItem(ITEM_ID, item);
        verify(status).put(ITEM_ID, ETAG_A);
    }
    
    @Test
    @DisplayName("may replace item A with item B")
    void applyB() throws Exception
    {
        conflictResolver = init(new ConflictResolver<>() {
            @Override
            public void resolve(String itemId, String etagA, String etagB) throws Exception
            {
                applyItemB();
            }
        });
        
        conflictResolver.resolve(ITEM_ID, ETAG_A, ETAG_B);
        
        verify(itemSetA).updateItem(ITEM_ID, item);
        verify(status).put(ITEM_ID, ETAG_B);
    }
    
    @Test
    @DisplayName("may replace items on both sides with merged item")
    void applyMergedItem() throws Exception
    {
        conflictResolver = init(new ConflictResolver<>() {
            @Override
            public void resolve(String itemId, String etagA, String etagB) throws Exception
            {
                applyItem("MergedItem", "etagC");
            }
        });
        
        conflictResolver.resolve(ITEM_ID, ETAG_A, ETAG_B);
        
        verify(itemSetA).updateItem(ITEM_ID, "MergedItem");
        verify(itemSetB).updateItem(ITEM_ID, "MergedItem");
        verify(status).put(ITEM_ID, "etagC");
    }
    
    private ConflictResolver<Object> init(ConflictResolver<Object> conflictResolver)
    {
        conflictResolver.setItemSets(itemSetA, itemSetB, status);
        conflictResolver.setContext(ITEM_ID, ETAG_A, ETAG_B);
        return conflictResolver;
    }
}