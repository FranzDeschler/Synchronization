package cloud.codestore.synchronization;

/**
 * Default progress listener which does nothing.
 */
class DefaultProgressListener implements ProgressListener
{
    @Override
    public void synchronizationStarted(String itemId) {}
    
    @Override
    public void synchronizationFinished(String itemId) {}
    
    @Override
    public void synchronizationFailed(String itemId, Throwable exception) {}
}
