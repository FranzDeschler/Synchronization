package cloud.codestore.synchronization.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("The AbstractMutableItemSet")
class AbstractMutableItemSetTest
{
    private AbstractMutableItemSet<Object> itemSet;
    
    @BeforeEach
    void setUp()
    {
        Map<String, String> map = new HashMap<>();
        map.put("123", "abc");
        map.put("456", "def");
        map.put("789", "ghi");
        
        itemSet = new AbstractMutableItemSet<>(map) {
            @Override
            public Object getItem(String itemId)
            {
                return null;
            }
    
            @Override
            public void addItem(String itemId, Object item) {}
    
            @Override
            public void delete(String itemId) {}
    
            @Override
            public void updateItem(String itemId, Object item) {}
        };
    }
    
    @Test
    @DisplayName("returns item ids")
    void returnsItemIds()
    {
        assertEquals(Set.of("123", "456", "789"), itemSet.getItemIds());
    }
    
    @Test
    @DisplayName("can check whether item exists")
    void checksWhetherItemExists()
    {
        assertTrue(itemSet.contains("123"));
        assertFalse(itemSet.contains("321"));
    }
    
    @Test
    @DisplayName("returns corresponding eTag")
    void getEtag()
    {
        assertEquals("abc", itemSet.getEtag("123"));
        assertEquals("def", itemSet.getEtag("456"));
        assertEquals("ghi", itemSet.getEtag("789"));
    }
}