package cloud.codestore.synchronization;

/**
 * Exception in case there was a conflict which could not be resolved.
 */
public class UnresolvedConflictException extends Exception
{
    public UnresolvedConflictException() {}
    
    public UnresolvedConflictException(String message)
    {
        super(message);
    }
}
