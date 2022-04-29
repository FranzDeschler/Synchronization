package cloud.codestore.synchronization.helper;

import cloud.codestore.synchronization.Status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link Status} which saves the content in a file as CSV-String.
 */
public class CsvImmutableItemStatus extends AbstractImmutableItemStatus
{
    private static final String DELIMITER = ",";
    
    private Path file;
    
    private CsvImmutableItemStatus(Set<String> itemIDs, Path file)
    {
        super(itemIDs);
        this.file = file;
    }
    
    /**
     * Creates a new {@link CsvImmutableItemStatus} object which contains the data from the given file.
     *
     * @param file the file of the status.
     *
     * @return a {@link CsvImmutableItemStatus} object.
     *
     * @throws IOException if the file could not be loaded.
     */
    public static CsvImmutableItemStatus load(Path file) throws IOException
    {
        return new CsvImmutableItemStatus(loadItemIDs(file), file);
    }
    
    /**
     * Creates a new {@link CsvImmutableItemStatus} object which contains the data from the given file.
     * If the file could not be loaded, the status will be empty.
     *
     * @param file the file of the status.
     *
     * @return a {@link CsvImmutableItemStatus} object.
     *
     * @throws IOException if the file could not be loaded.
     */
    public static CsvImmutableItemStatus loadSilently(Path file)
    {
        try
        {
            return load(file);
        }
        catch(IOException e)
        {
            return new CsvImmutableItemStatus(new HashSet<>(), file);
        }
    }
    
    @Override
    public void save(Set<String> itemIDs) throws IOException
    {
        Files.writeString(file, String.join(DELIMITER, itemIDs));
    }
    
    private static Set<String> loadItemIDs(Path file) throws IOException
    {
        String content = Files.readString(file);
        if(!content.isEmpty())
            return Arrays.stream(content.split(DELIMITER)).collect(Collectors.toSet());
    
        return new HashSet<>();
    }
}
