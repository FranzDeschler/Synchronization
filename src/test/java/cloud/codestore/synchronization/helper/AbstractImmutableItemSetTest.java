package cloud.codestore.synchronization.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("The AbstractImmutableItemSet")
class AbstractImmutableItemSetTest
{
    private AbstractImmutableItemSet<Object> itemSet;
    
    @BeforeEach
    void setUp()
    {
        itemSet = new AbstractImmutableItemSet<>(Set.of("123", "456", "789")) {
            @Override
            public Object getItem(String itemId) throws Exception
            {
                return null;
            }
    
            @Override
            public void addItem(String itemId, Object item) {}
    
            @Override
            public void delete(String itemId) {}
        };
    }
    
    @Test
    @DisplayName("returns item ids")
    void returnsItemIds()
    {
        assertEquals(Set.of("123", "456", "789"), itemSet.getItemIds());
    }
    
    @Test
    @DisplayName("checks whether item exists")
    void checksWhetherItemExists()
    {
        assertTrue(itemSet.contains("123"));
        assertFalse(itemSet.contains("321"));
    }
    
    @Test
    @DisplayName("throws UnsupportedOperationException when trying to get etag")
    void getEtagIllegalState()
    {
        assertThrows(UnsupportedOperationException.class, () -> itemSet.getEtag("123"));
    }
    
    @Test
    @DisplayName("throws UnsupportedOperationException when trying to update an item")
    void setEtagIllegalState()
    {
        assertThrows(UnsupportedOperationException.class, () -> itemSet.updateItem("123", new Object()));
    }
}