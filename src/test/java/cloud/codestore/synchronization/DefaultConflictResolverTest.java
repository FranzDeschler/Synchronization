package cloud.codestore.synchronization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("The default conflict resolver")
class DefaultConflictResolverTest
{
    private ConflictResolver<?> conflictResolver = new DefaultConflictResolver<>();

    @Test
    @DisplayName("always throws an UnresolvedConflictException")
    void alwaysThrowException()
    {
        assertThrows(UnresolvedConflictException.class, () -> conflictResolver.resolve(null, null, null));
    }
}