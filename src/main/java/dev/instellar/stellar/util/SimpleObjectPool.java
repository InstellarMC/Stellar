package dev.instellar.stellar.util;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

import java.util.function.Consumer;
import java.util.function.Function;

/// Stolen from [RelativityMC/FlowSched](https://github.com/RelativityMC/FlowSched/blob/32ca919e6c865cbdea1f4e03525903dc8a26a7fd/src/main/java/com/ishland/flowsched/structs/SimpleObjectPool.java)
/// Copyright (c) Ishland (RelativityMC), licensed under MIT License
/// Modified for Stellar
@NullMarked
public class SimpleObjectPool<T> {

    private final Function<SimpleObjectPool<T>, T> constructor;
    private final Consumer<T> initializer;
    private final Consumer<T> postRelease;
    private final int size;

    private final Object[] cachedObjects;
    private int allocatedCount = 0;

    public SimpleObjectPool(
            final Function<SimpleObjectPool<T>, T> constructor,
            final Consumer<T> initializer,
            final Consumer<T> postRelease,
            final int size) {
        this.constructor = Preconditions.checkNotNull(constructor);
        this.initializer = Preconditions.checkNotNull(initializer);
        this.postRelease = Preconditions.checkNotNull(postRelease);
        Preconditions.checkArgument(size > 0, "size must be greater than 0");
        this.cachedObjects = new Object[size];
        this.size = size;

        for (int i = 0; i < size; i++) {
            final T object = constructor.apply(this);
            this.cachedObjects[i] = object;
        }
    }

    public T alloc() {
        final T object;
        synchronized (this) {
            if (this.allocatedCount >= this.size) { // oversized, falling back to normal alloc
                object = this.constructor.apply(this);
                return object;
            }

            // get an object from the array
            final int ordinal = this.allocatedCount++;
            object = (T) this.cachedObjects[ordinal];
            this.cachedObjects[ordinal] = null;
        }

        this.initializer.accept(object); // initialize the object

        return object;
    }

    public void release(T object) {
        synchronized (this) {
            if (this.allocatedCount == 0) return; // pool is full
            this.postRelease.accept(object);
            this.cachedObjects[--this.allocatedCount] = object; // store the object into the pool
        }
    }

}
