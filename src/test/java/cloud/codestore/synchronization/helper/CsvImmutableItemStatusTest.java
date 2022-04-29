package cloud.codestore.synchronization.helper;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("The status")
class CsvImmutableItemStatusTest
{
    private Path file;
    
    @BeforeEach
    void setUp() throws IOException
    {
        file = Files.createTempFile("status", "csv");
    }
    
    @AfterEach
    void tearDown() throws IOException
    {
        if(file != null)
            Files.delete(file);
    }
    
    @Nested
    @DisplayName("if new")
    class EmptyStatusTest
    {
        private CsvImmutableItemStatus status;
    
        @BeforeEach
        void setUp() throws IOException
        {
            status = CsvImmutableItemStatus.load(file);
        }
    
        @Test
        @DisplayName("is empty")
        void empty()
        {
            assertNotNull(status.getItemIds());
            assertTrue(status.getItemIds().isEmpty());
        }
    
        @Test
        @DisplayName("throws UnsupportedOperationException when trying to add etag")
        void setEtagIllegalState()
        {
            assertThrows(UnsupportedOperationException.class, () -> status.put("123", "etag"));
        }
    
        @Test
        @DisplayName("throws UnsupportedOperationException when trying to get etag")
        void getEtagIllegalState()
        {
            assertThrows(UnsupportedOperationException.class, () -> status.getEtag("123"));
        }
    
        @Nested
        @DisplayName("after adding IDs")
        class FilledStatusTest
        {
            @BeforeEach
            void setUp()
            {
                status.put("123");
                status.put("456");
                status.put("789");
            }
    
            @Test
            @DisplayName("contains the item IDs")
            void containsItems()
            {
                assertTrue(status.contains("123"));
                assertTrue(status.contains("456"));
                assertTrue(status.contains("789"));
            }
    
            @Test
            @DisplayName("supports deletion of items")
            void deleteItems()
            {
                assertTrue(status.contains("456"));
                status.delete("456");
                assertFalse(status.contains("456"));
            }
        }
    
        @Nested
        @DisplayName("after saving data")
        class FilledFileTest
        {
            @BeforeEach
            void setUp() throws IOException
            {
                status.put("123");
                status.put("456");
                status.put("789");
                status.save();
            }
        
            @Test
            @DisplayName("the file contains CSV String")
            void containsCsvString() throws IOException
            {
                String content = Files.readString(file);
                assertEquals("123,456,789", content);
            }
        
            @Test
            @DisplayName("the status can be restored from file")
            void restoreFromFile() throws IOException
            {
                CsvImmutableItemStatus status = CsvImmutableItemStatus.load(file);
            
                assertEquals(3, status.getItemIds().size());
                assertTrue(status.contains("123"));
                assertTrue(status.contains("456"));
                assertTrue(status.contains("789"));
            }
        }
    }
}