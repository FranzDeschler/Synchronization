# Synchronization
## Introduction
This library provides a general-purpose synchronization based on the algorithm presented 
by [Markus Unterwaditzer](https://unterwaditzer.net/2016/sync-algorithm.html) in 2016.
It can be used by anyone who needs to add some kind of synchronization to their application.

Originally, the code was part of the  [{CodeStore}](https://codestore.cloud) application which uses the algorithm
to synchronize code snippets. Now it has been extracted to its own library to provide others the possibility to
build their own synchronization.

The implementation is completely independent of the kind of data which should be synchronized and where it is stored.
The communication with external systems, as well as reading and writing data is done by the main application.

## How it works
The algorithm synchronizes the items of two sides `A` and `B`.
For each side, there is a set which contains the IDs of the items which are currently present on the corresponding side.
Additionally, there is a third set which contains the IDs of the items which were present after the last synchronization.
This set is used to determine whether an item was created or deleted on one side.

To see how the algorithm works in detail, read [Markus Unterwaditzer´s blog](https://unterwaditzer.net/2016/sync-algorithm.html).

### Mutable items
The library provides synchronization for immutable and mutable items.
If the items are mutable, each set additionally contains a kind of checksum (called "etag") of each item
which is used to determine whether the content of an item was changed on one or both sides.
The etag is represented as String but can contain any kind of information (for example a version number) which represents 
the content of an item. Usually an etag is a hash of the item´s content.

The original algorithm assumes that both sides `A` and `B` calculate the etag in different ways.
In contrast, this implementation assumes that the etags are calculated homogeneously.
If the content of an item is equal on both sides, the etags must be equal too. 
So, the `status` only contains one etag of an item instead of two.

## Maven coordinates
```xml
<dependency>
    <groupId>cloud.codestore</groupId>
    <artifactId>synchronization</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Implementation
The library uses two interfaces `ItemSet` and `Status` which represent the items present on each side
and the items present after the last synchronization respectively.
They are used to read, write, delete or update the items.

Both interfaces must be implemented by the main application.
To avoid implementing the entire interface, the library provides some helper classes that already implement common mechanisms.
The code examples in the following sections show how to use this library to synchronize data in a local directory
and a remote server for immutable and mutable items.

Remember to save the status after the synchronization.

### Immutable items
```java
public class CustomLocalItemSet extends AbstractImmutableItemSet<CustomItem> {
    public CustomLocalItemSet(Set<String> itemIDs) {
        super(itemIDs);
    }
    
    // custom read / write / delete logic
}

public class CustomRemoteItemSet extends AbstractImmutableItemSet<CustomItem> {
    public CustomRemoteItemSet(Set<String> itemIDs) {
        super(itemIDs);
    }
    
    // custom read / write / delete logic
}
```
```java
ItemSet<CustomItem> localItems = new CustomLocalItemSet(localItemIDs);
ItemSet<CustomItem> remoteItems = new CustomRemoteItemSet(remoteItemIDs);
Status status = new CsvImmutableItemStatus(Path.of("status.csv"));

Synchronization<CustomItem> sync = new ImmutableItemSynchronization<>(localItems, remoteItems, status);
sync.synchronize();
status.save();
```

### Mutable items
```java
public class CustomLocalItemSet extends AbstractMutableItemSet<CustomItem> {
    public CustomLocalItemSet(Set<String> itemIDs) {
        super(itemIDs);
    }
    
    // custom read / write / update / delete logic
}

public class CustomRemoteItemSet extends AbstractMutableItemSet<CustomItem> {
    public CustomRemoteItemSet(Set<String> itemIDs) {
        super(itemIDs);
    }
    
    // custom read / write / update / delete logic
}
```
```java
ItemSet<CustomItem> localItems = new CustomLocalItemSet(localItemIDs);
ItemSet<CustomItem> remoteItems = new CustomRemoteItemSet(remoteItemIDs);
Status status = new CsvMutableItemStatus(Path.of("status.csv"));

Synchronization<CustomItem> sync = new MutableItemSynchronization<>(localItems, remoteItems, status);
sync.synchronize();
status.save();
```

### Conflict resolving
In case of mutable items, there is a chance of conflicts.
A conflict occurs when an item was changed, and the algorithm cannot determine on which side it was changed.

By default, the synchronization of this item fails with a `UnresolvedConflictException`.
To resolve the conflict during the synchronization, pass a `ConflictResolver` to the `MutableItemSynchronization`.
It will be called in case of a conflict.

```java
sync.setConflictResolver(new CustomConflictResolver());
sync.synchronize();
```

How the conflict is resolved depends on the main application.
The etags may contain a kind of version information about the item.
In this case, the conflict could be resolved by using only the etags.
But in most cases the conflict resolution depends on the content of the items and some user interaction.

The `ConflictResolver` base class provides several methods to load the items as well as some basic mechanisms
to solve the conflict.

The most simple solution would be to always use the item of a certain side.
The methods `applyItemA()` and `applyItemB()` copies the item from `A` to `B` and vice versa respectively.

```java
public class CustomConflictResolver extends ConflictResolver<CustomItem> {
    private boolean allwaysUseA = true; //custom condition
    
    @Override
    public SyncResult resolve(String itemId, String etagA, String etagB) throws Exception {
        if(allwaysUseA)
            return applyItemA();
        else
            return applyItemB();
    }
}
```

A more complex solution could involve a user interaction to let the user decide which item to use or to provide the
possibility to merge both items into a new one which is then stored on both sides.
If the conflict could not be resolved at all, the `ConflictResolver` should throw an `UnresolvedConflictException`.

Note that the `ConflictResolver` is called synchronously.
The synchronization of the item ends as soon as the `resolve` method returns.
If the main application needs to wait for user input or runs an asynchronous task, make sure the
`resolve` method blocks until the result is available.

```java
public class CustomConflictResolver extends ConflictResolver<CustomItem> {
    @Override
    public SyncResult resolve(String itemId, String etagA, String etagB) throws Exception {
        CustomItem itemA = getItemA();
        CustomItem itemB = getItemB();
        
        Future<CustomMergeResult> future = new CustomConflictDialog(itemA, itemB).show();
        CustomMergeResult mergeResult = future.get(); //blocks until result is available
        
        if(mergeResult.wasCanceled())
            throw new UnresolvedConflictException();
        
        if(mergeResult.useA())
            return applyItemA();
        if(mergeResult.useB())
            return applyItemB();
    
        CustomItem mergedItem = mergeResult.getMergedItem();
        return applyItem(mergedItem, mergedItem.getEtag());
    }
}
```

Remember: `A` and `B` is not defined in any way. What `A` and `B` is, is defined by the main application.

## Progress
To track the progress of the synchronization, pass a `ProgressListener` to the `Synchronization`.
It will be called whenever the synchronization of an item was started or finished.

```java
public class CustomProgressListener implements ProgressListener {
    @Override
    public void synchronizationStarted(String itemId) {
        System.out.println("Synchronization started for item " + itemId);
    }
    
    @Override
    public void synchronizationFinished(String itemId) {
        System.out.println("Synchronization finished for item " + itemId);
    }
    
    @Override
    public void synchronizationFailed(String itemId, Throwable exception) {
        if(exception instanceof UnresolvedConflictException)
            System.out.println("Synchronization failed for item " + itemId + " because of an unresolved conflict.");
        else {
            System.out.println("Synchronization failed for item " + itemId);
            exception.printStackTrace();
        }
    }
}
```
```java
sync.setProgressListener(new CustomProgressListener());
sync.synchronize();
```

## Cancellation
The synchronization can be canceled by calling `cancel()`.
This does not interrupt the currently processed item(s).
It only prevents the algorithm from processing further items.
The `synchronize()` method waits for the currently processed item(s) to be finished and returns afterwards.
To check whether the synchronization was executed completely or was canceled (usually by another thread),
you can use the `isCanceled()` method.

```java
Synchronization<CustomItem> sync = new ImmutableItemSynchronization<>(localItems, remoteItems, status);
sync.synchronize();
if(sync.isCanceled())
    System.out.println("The synchronization was canceled.");
else
    System.out.println("The synchronization finished successfully.");
```

## Multithreading

### Synchronous execution
By default, the synchronization is executed synchronously.
So, all items are processed one after the other and the `synchronize()` method returns as soon as all items were
processed or the synchronization was canceled.

### Asynchronous execution
The synchronous execution may block the main application.
If the application needs to stay responsive, the synchronization should be executed in a background task.
That needs to be done by the application itself.
This approach still processes the items one after the other.

```java
Synchronization<CustomItem> sync = new ImmutableItemSynchronization<>(localItems, remoteItems, status);
ExecutorService executorService = Executors.newSingleThreadExecutor();
executorService.execute(() -> sync.synchronize());
executorService.shutdown();
```

### Concurrent processing
To improve performance when synchronizing a large number of items the library provides the possibility to
process the items concurrently in separate threads. To enable this mechanism, set the number of threads 
which should be used for the concurrent processing via the `setThreadCount` method.
In the example below, the library uses ten threads to process the items.
The `synchronize()` method is still executed synchronously and returns as soon as all items were processed 
or the synchronization was canceled.

```java
Synchronization<CustomItem> sync = new ImmutableItemSynchronization<>(localItems, remoteItems, status);
sync.setThreadCount(10);
sync.synchronize();
```

Note that the synchronization of an item involves the use of the `ItemSet`s, `Status` and `ProgressListener`.
You need to make sure that the implementations of these interfaces are thread safe!