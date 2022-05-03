package cloud.codestore.synchronization;

/**
 * Exception in case there was a conflict which could not be resolved.
 */
public class UnresolvedConflictException extends Exception
{
    /**
     * Creates a new {@link UnresolvedConflictException} without a message.
     */
    public UnresolvedConflictException() {}
    
    /**
     * Creates a new {@link UnresolvedConflictException} with the given message.
     *
     * @param message the message of this exception.
     */
    public UnresolvedConflictException(String message)
    {
        super(message);
    }
}
