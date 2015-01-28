package org.medimob.orm.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * entity cache session
 * Created by Poopaou on 23/01/2015.
 */
public class Session<T> {

  private final LongSparseArray<Reference<T>> map;
  private final ReentrantLock lock;

  public Session() {
    map = new LongSparseArray<Reference<T>>();
    lock = new ReentrantLock();
  }

  /**
   * Gets entity from cache (with cache lock).
   *
   * @param key entity ids.
   * @return entity or null not cached
   */
  @Nullable
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

  /**
   * Gets entity from cache (without cache lock).
   *
   * @param key entity ids.
   * @return entity or null not cached
   */
  @Nullable
  public T getNoLock(long key) {
    Reference<T> ref = map.get(key);
    if (ref != null) {
      return ref.get();
    } else {
      return null;
    }
  }

  /**
   * Put entity in cache (with cache lock).
   *
   * @param key    entity ids.
   * @param entity entity.
   */
  public void put(long key, @NonNull T entity) {
    lock.lock();
    try {
      map.put(key, new WeakReference<T>(entity));
    } finally {
      lock.unlock();
    }
  }

  /**
   * Put entity in cache (without cache lock).
   *
   * @param key    entity ids.
   * @param entity entity.
   */
  public void putNoLock(long key, @NonNull T entity) {
    map.put(key, new WeakReference<T>(entity));
  }

  /**
   * Remote entity from cache (with cache lock).
   *
   * @param key key
   */
  public void remove(long key) {
    lock.lock();
    try {
      map.remove(key);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Remote all entities from cache (with cache lock).
   *
   * @param keys keys
   */
  public void remove(@NonNull Iterable<Long> keys) {
    lock.lock();
    try {
      for (Long key : keys) {
        map.remove(key);
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Clear cache.
   */
  public void clear() {
    lock.lock();
    try {
      map.clear();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Lock cache access.
   */
  public void lock() {
    lock.lock();
  }

  /**
   * Unlock cache access.
   */
  public void unlock() {
    lock.unlock();
  }
}
