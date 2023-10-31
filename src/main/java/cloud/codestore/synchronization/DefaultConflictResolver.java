package cloud.codestore.synchronization;

/**
 * The default conflict resolver which always throws an {@link UnresolvedConflictException}.
 */
class DefaultConflictResolver<I> extends ConflictResolver<I>
{
    @Override
    void setContext(String itemId, String etagA, String etagB) {}

    @Override
    public void resolve(String itemId, String etagA, String etagB) throws Exception
    {
        throw new UnresolvedConflictException();
    }
}
