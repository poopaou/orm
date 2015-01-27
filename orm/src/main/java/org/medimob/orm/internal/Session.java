package org.medimob.orm.internal;

import android.util.LongSparseArray;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Poopaou on 23/01/2015.
 */
public class Session<T> {

    private final LongSparseArray<Reference<T>> map;
    private final ReentrantLock lock;

    public Session() {
        map = new LongSparseArray<Reference<T>>();
        lock = new ReentrantLock();
    }

    public T get(long key) {
        lock.lock();
        Reference<T> ref;
        try {
            ref = map.get(key);
        } finally {
            lock.unlock();
        }
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }

    public T getNoLock(long key) {
        Reference<T> ref = map.get(key);
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }

    public void put(long key, T entity) {
        lock.lock();
        try {
            map.put(key, new WeakReference<T>(entity));
        } finally {
            lock.unlock();
        }
    }

    public void putNoLock(long key, T entity) {
        map.put(key, new WeakReference<T>(entity));
    }

    public void remove(Long key) {
        lock.lock();
        try {
            map.remove(key);
        } finally {
            lock.unlock();
        }
    }

    public void remove(Iterable<Long> keys) {
        lock.lock();
        try {
            for (Long key : keys) {
                map.remove(key);
            }
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            map.clear();
        } finally {
            lock.unlock();
        }
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
