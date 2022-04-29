package cloud.codestore.synchronization.helper;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("The status")
class CsvMutableItemStatusTest
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
        private CsvMutableItemStatus status;
    
        @BeforeEach
        void setUp() throws IOException
        {
            status = CsvMutableItemStatus.load(file);
        }
    
        @Test
        @DisplayName("is empty")
        void empty()
        {
            assertNotNull(status.getItemIds());
            assertTrue(status.getItemIds().isEmpty());
        }
    
        @Test
        @DisplayName("throws UnsupportedOperationException when trying to add item without etag")
        void putItemWithoutEtag()
        {
            assertThrows(UnsupportedOperationException.class, () -> status.put("123"));
        }
    
        @Nested
        @DisplayName("after saving data")
        class FilledFileTest
        {
            @BeforeEach
            void setUp() throws IOException
            {
                status.put("123", "abc");
                status.put("456", "def");
                status.put("789", "ghi");
                status.save();
            }

            @Test
            @DisplayName("the file contains CSV String")
            void containsCsvString() throws IOException
            {
                List<String> lines = Files.readAllLines(file);
                
                assertEquals(3, lines.size());
                assertTrue(lines.contains("123,abc"));
                assertTrue(lines.contains("456,def"));
                assertTrue(lines.contains("789,ghi"));
            }
    
            @Test
            @DisplayName("the status can be restored from file")
            void restoreFromFile() throws IOException
            {
                CsvMutableItemStatus status = CsvMutableItemStatus.load(file);
    
                assertEquals(3, status.getItemIds().size());
                assertTrue(status.contains("123"));
                assertTrue(status.contains("456"));
                assertTrue(status.contains("789"));
                
                assertEquals("abc", status.getEtag("123"));
                assertEquals("def", status.getEtag("456"));
                assertEquals("ghi", status.getEtag("789"));
            }
        }
    }
}