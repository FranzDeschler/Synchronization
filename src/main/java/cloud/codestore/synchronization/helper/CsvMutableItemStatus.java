package cloud.codestore.synchronization.helper;

import cloud.codestore.synchronization.Status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A {@link Status} which saves the content in a file as CSV-String.
 */
public class CsvMutableItemStatus extends AbstractMutableItemStatus
{
    private static final String DELIMITER = ",";
    
    private Path file;
    
    private CsvMutableItemStatus(Map<String, String> itemIdToEtagMap, Path file)
    {
        super(itemIdToEtagMap);
        this.file = file;
    }
    
    /**
     * Creates a new {@link CsvMutableItemStatus} object which contains the data from the given file.
     *
     * @param file the file of the status.
     *
     * @return a {@link CsvMutableItemStatus} object.
     *
     * @throws IOException if the file could not be loaded.
     */
    public static CsvMutableItemStatus load(Path file) throws IOException
    {
        return new CsvMutableItemStatus(loadItemToEtagMap(file), file);
    }
    
    /**
     * Creates a new {@link CsvMutableItemStatus} object which contains the data from the given file.
     * If the file could not be loaded, the status will be empty.
     *
     * @param file the file of the status.
     *
     * @return a {@link CsvMutableItemStatus} object.
     *
     * @throws IOException if the file could not be loaded.
     */
    public static CsvMutableItemStatus loadSilently(Path file)
    {
        try
        {
            return load(file);
        }
        catch(IOException e)
        {
            return new CsvMutableItemStatus(new HashMap<>(), file);
        }
    }
    
    @Override
    public void save(Map<String, String> itemIdToEtagMap) throws IOException
    {
        List<String> lines = itemIdToEtagMap.entrySet()
                                            .stream()
                                            .map(entry -> entry.getKey() + DELIMITER + entry.getValue())
                                            .collect(Collectors.toList());
        
        Files.write(file, lines);
    }
    
    private static Map<String, String> loadItemToEtagMap(Path file) throws IOException
    {
        return Files.lines(file)
                    .map(line -> line.split(DELIMITER))
                    .collect(Collectors.toMap(split -> split[0], split -> split[1]));
    }
}
