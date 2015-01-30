package org.medimob.orm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.medimob.orm.internal.Session;
import org.medimob.orm.internal.SqlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Entity model. 
 * Created by Poopaou on 23/01/2015.
 */
public abstract class Model<T> {

  private final Session<T> session;
  // Statements locks :
  // ensure that database statements
  // and binding are executed
  // synchronously.
  private final Object insertLock = new Object();
  private final Object updateLock = new Object();
  private final Object deleteLock = new Object();
  // Entity attributes.
  private final String tableName;
  private final String[] queryColumns;

  // Model properties.
  private final String idColumn;
  private final String versionColumn;
  private final String idSelection;
  private final ThreadLocal<String[]> selectArgsLocal;
  // Database lazy compiled statements.
  private SQLiteStatement insertStatement;
  private SQLiteStatement updateStatement;
  private SQLiteStatement deleteStatement;
  private SQLiteStatement deleteByIdStatement;
  private OpenHelper helper;

  protected Model(String tableName, String idColumn, String versionColumn, String[] queryColumns) {
    this.tableName = tableName;
    this.idColumn = idColumn;
    this.idSelection = idColumn + "=?";
    this.versionColumn = versionColumn;

    this.queryColumns = queryColumns;

    this.session = new Session<T>();
    this.selectArgsLocal = new ThreadLocal<String[]>();
  }

  private static long[] toPrimitive(List<Long> longs) {
    long[] array = new long[longs.size()];
    for (int i = 0; i < longs.size(); i++) {
      array[i] = longs.get(i);
    }
    return array;
  }

  void attach(OpenHelper helper) {
    this.helper = helper;
  }

  private SQLiteDatabase getWritableDatabase() {
    if (helper == null) {
      throw new IllegalStateException("Model must be get through Orm instance.");
    }
    return helper.getWritableDatabase();
  }

  private SQLiteDatabase getReadableDatabase() {
    if (helper == null) {
      throw new IllegalStateException("Model must be get through Orm instance.");
    }
    return helper.getReadableDatabase();
  }

  /**
   * Raw query for single result (result will not be cached).
   *
   * @param selection    selection
   * @param selectionArg selection argument.
   * @return result or nul if not found.
   */
  public T rawQuerySingle(@NonNull String selection,
                          @Nullable String[] selectionArg) {
    return rawQuerySingle(selection, selectionArg, null);
  }

  /**
   * Raw query for single result (result will not be cached).
   *
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param having       having clause.
   * @return result or nul if not found.
   */
  public T rawQuerySingle(@NonNull String selection,
                          @Nullable String[] selectionArg, @Nullable String having) {
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor =
        db.query(tableName, queryColumns, selection, selectionArg, null, having, null, "1");
    try {
      if (cursor.moveToFirst()) {
        T entity = newInstance();
        readCursor(entity, cursor);
        return entity;
      }
      return null;
    } finally {
      cursor.close();
    }
  }

  /**
   * Raw query for result (result will not be cached).
   *
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param orderBy      having clause.
   * @param limit        limit clause.
   * @return result or nul if not found.
   */
  public List<T> rawQuery(@NonNull String selection,
                          @Nullable String[] selectionArg, @Nullable String orderBy,
                          @Nullable String limit) {
    return rawQuery(selection, selectionArg, null, null, orderBy, limit);
  }

  /**
   * Raw query for result (result will not be cached).
   *
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param groupBy      groupBy clause.
   * @param having       having clause.
   * @param orderBy      having clause.
   * @param limit        limit clause.
   * @return result or nul if not found.
   */
  public List<T> rawQuery(@NonNull String selection,
                          @Nullable String[] selectionArg, @Nullable String groupBy,
                          @Nullable String having, @Nullable String orderBy,
                          @Nullable String limit) {
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor =
        db.query(tableName, queryColumns, selection, selectionArg, groupBy, having, orderBy, limit);
    try {
      if (cursor.moveToFirst()) {
        List<T> results = new ArrayList<T>(cursor.getCount());
        do {
          T entity = newInstance();
          readCursor(entity, cursor);
          results.add(entity);
        }
        while (cursor.moveToNext());

        return results;
      }
      return Collections.emptyList();
    } finally {
      cursor.close();
    }
  }

  /**
   * Load entity by id (result will be cached).
   *
   * @param id entity id
   * @return entity or null if not found.
   */
  public T loadById(long id) {
    T entity = session.get(id);
    if (entity != null) {
      return entity;
    }
    // Cache miss. Load entity from db.
    String[] args = getIdSelectArgs();
    args[0] = String.valueOf(id);
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.query(tableName, queryColumns, idSelection, args, null, null, null);
    try {
      if (cursor.moveToFirst()) {
        entity = newInstance();
        readCursor(entity, cursor);
        session.put(id, entity);
      }
      return entity;
    } finally {
      cursor.close();
    }
  }

  private String[] getIdSelectArgs() {
    String[] args = selectArgsLocal.get();
    if (args == null) {
      args = new String[1];
      selectArgsLocal.set(args);
    }
    return args;
  }

  /**
   * Insert entity inside transaction.
   *
   * @param entities entities to insert.
   * @return entities ids.
   */
  public long[] insertInTx(@NonNull T... entities) {
    return insertInTx(Arrays.asList(entities));
  }

  /**
   * Insert entity inside transaction.
   *
   * @param entities entities to insert.
   * @return entities ids.
   */
  public long[] insertInTx(@NonNull Iterable<T> entities) {
    SQLiteDatabase db = getWritableDatabase();
    if (db.isDbLockedByCurrentThread()) {
      synchronized (insertLock) {
        ArrayList<Long> ids = new ArrayList<Long>();
        for (T entity : entities) {
          ids.add(insertInsideSynchronized(db, entity));
        }
        return toPrimitive(ids);
      }
    } else {
      db.beginTransaction();
      synchronized (insertLock) {
        try {
          ArrayList<Long> ids = new ArrayList<Long>();
          for (T entity : entities) {
            ids.add(insertInsideSynchronized(db, entity));
          }
          db.setTransactionSuccessful();
          return toPrimitive(ids);
        } catch (SQLiteException e) {
          OrmLog.e("Insert failed, transaction failed", e);
          throw e;
        } finally {
          db.endTransaction();
        }
      }
    }
  }

  /**
   * Insert entity inside transaction.
   *
   * @param entity entity to insert.
   * @return entity ids.
   */
  public long insertInTx(@NonNull T entity) {
    SQLiteDatabase db = getWritableDatabase();
    if (db.isDbLockedByCurrentThread()) {
      synchronized (insertLock) {
        return insertInsideSynchronized(db, entity);
      }
    } else {
      db.beginTransaction();
      synchronized (insertLock) {
        try {
          long id = insertInsideSynchronized(db, entity);
          db.setTransactionSuccessful();
          return id;
        } catch (SQLiteException e) {
          OrmLog.e("Insert failed, transaction failed", e);
          throw e;
        } finally {
          db.endTransaction();
        }
      }
    }
  }

  /**
   * Insert entity without transaction.
   *
   * @param entity entity to insert.
   * @return entity ids.
   */
  public long insert(@NonNull T entity) {
    synchronized (insertLock) {
      SQLiteDatabase db = getWritableDatabase();
      return insertInsideSynchronized(db, entity);
    }
  }

  private long insertInsideSynchronized(SQLiteDatabase db, T entity) {
    if (getId(entity) > 0) {
      throw new OrmException("Entity is not new");
    }
    if (insertStatement == null) {
      insertStatement = db.compileStatement(getInsertStatement());
    }
    bindInsert(insertStatement, entity);
    return insertStatement.executeInsert();
  }

  /**
   * Update entity inside transaction.
   *
   * @param entities entities to update.
   * @return update count.
   */
  public int updateInTx(@NonNull T... entities) {
    return updateInTx(Arrays.asList(entities));
  }

  /**
   * Update entity inside transaction.
   *
   * @param entities entities to update.
   * @return update count.
   */
  public int updateInTx(@NonNull Iterable<T> entities) {
    SQLiteDatabase db = getWritableDatabase();
    if (db.isDbLockedByCurrentThread()) {
      synchronized (updateLock) {
        int count = 0;
        for (T entity : entities) {
          updateInsideSynchronized(db, entity);
          count++;
        }
        return count;
      }
    } else {
      db.beginTransaction();
      synchronized (updateLock) {
        try {
          int count = 0;
          for (T entity : entities) {
            updateInsideSynchronized(db, entity);
            count++;
          }
          db.setTransactionSuccessful();
          return count;
        } catch (SQLiteException e) {
          OrmLog.e("Update failed, transaction failed", e);
          throw e;
        } finally {
          db.endTransaction();
        }
      }
    }
  }

  /**
   * Update entity inside transaction.
   *
   * @param db     database.
   * @param entity entity to update.
   * @return true if entity has been updated.
   */
  public boolean updateInTx(SQLiteDatabase db, T entity) {
    if (db.isDbLockedByCurrentThread()) {
      synchronized (updateLock) {
        return updateInsideSynchronized(db, entity);
      }
    } else {
      db.beginTransaction();
      synchronized (updateLock) {
        try {
          if (updateInsideSynchronized(db, entity)) {
            db.setTransactionSuccessful();
            return true;
          }
          return false;
        } catch (SQLiteException e) {
          OrmLog.e("update error", e);
          throw e;
        } finally {
          db.endTransaction();
        }
      }
    }
  }

  /**
   * Update entity without transaction.
   *
   * @param entity entity to update.
   * @return true if entity has been updated.
   */
  public boolean update(@NonNull T entity) {
    synchronized (updateLock) {
      SQLiteDatabase db = getWritableDatabase();
      return updateInsideSynchronized(db, entity);
    }
  }

  private boolean updateInsideSynchronized(SQLiteDatabase db, T entity) {
    if (updateStatement == null) {
      updateStatement = db.compileStatement(getUpdateStatement());
    }

    bindUpdate(updateStatement, entity);
    if (updateStatement.executeUpdateDelete() == 1) {
      session.remove(getId(entity));
      return true;
    }
    return false;
  }

  /**
   * Delete entities by ids without transaction.
   *
   * @param ids entities to delete ids.
   * @return delete count.
   */
  public int deleteById(@NonNull long... ids) {
    SQLiteDatabase db = getWritableDatabase();
    synchronized (deleteLock) {
      int count = 0;
      for (long id : ids) {
        deleteInsideSynchronized(db, id);
        count++;
      }
      return count;
    }
  }

  /**
   * Delete entities by ids in transaction.
   *
   * @param ids entities to delete ids.
   * @return delete count.
   */
  public int deleteByIdInTx(@NonNull long... ids) {
    SQLiteDatabase db = getWritableDatabase();
    if (db.isDbLockedByCurrentThread()) {
      synchronized (deleteLock) {
        int count = 0;
        for (long id : ids) {
          deleteInsideSynchronized(db, id);
          count++;
        }
        return count;
      }
    } else {
      db.beginTransaction();
      synchronized (deleteLock) {
        try {
          int count = 0;
          for (long id : ids) {
            deleteInsideSynchronized(db, id);
            count++;
          }
          db.setTransactionSuccessful();
          return count;
        } catch (SQLiteException e) {
          OrmLog.e("Delete failed, transaction failed", e);
          throw e;
        } finally {
          db.endTransaction();
        }
      }
    }
  }

  /**
   * Delete entities in transaction.
   *
   * @param entities entities to delete.
   * @return delete count.
   */
  public int deleteInTx(@NonNull T... entities) {
    return deleteInTx(Arrays.asList(entities));
  }

  /**
   * Delete entities in transaction.
   *
   * @param entities entities to delete.
   * @return delete count.
   */
  public int deleteInTx(@NonNull Iterable<T> entities) {
    SQLiteDatabase db = getWritableDatabase();
    if (db.isDbLockedByCurrentThread()) {
      synchronized (deleteLock) {
        int count = 0;
        for (T entity : entities) {
          deleteInsideSynchronized(db, entity);
          count++;
        }
        return count;
      }
    } else {
      db.beginTransaction();
      synchronized (deleteLock) {
        try {
          int count = 0;
          for (T entity : entities) {
            deleteInsideSynchronized(db, entity);
            count++;
          }
          db.setTransactionSuccessful();
          return count;
        } catch (SQLiteException e) {
          OrmLog.e("Delete failed, transaction failed", e);
          throw e;
        } finally {
          db.endTransaction();
        }
      }
    }
  }

  /**
   * Delete entity in transaction.
   *
   * @param entity entity to delete.
   * @return true if entity has been deleted.
   */
  public boolean deleteInTx(@NonNull T entity) {
    SQLiteDatabase db = getWritableDatabase();
    if (db.isDbLockedByCurrentThread()) {
      synchronized (deleteLock) {
        return deleteInsideSynchronized(db, entity);
      }
    } else {
      db.beginTransaction();
      synchronized (deleteLock) {
        try {
          if (deleteInsideSynchronized(db, entity)) {
            db.setTransactionSuccessful();
            return true;
          }
          return false;
        } catch (SQLiteException e) {
          OrmLog.e("Delete failed, transaction failed", e);
          throw e;
        } finally {
          db.endTransaction();
        }
      }
    }
  }

  /**
   * Delete entity without transaction.
   *
   * @param entity entity to delete.
   * @return true if entity has been deleted.
   */
  public boolean delete(@NonNull T entity) {
    SQLiteDatabase db = getWritableDatabase();
    synchronized (deleteLock) {
      return deleteInsideSynchronized(db, entity);
    }
  }

  private boolean deleteInsideSynchronized(SQLiteDatabase db, T entity) {
    if (deleteStatement == null) {
      deleteStatement = db.compileStatement(getDeleteStatement());
    }
    bindDelete(deleteStatement, entity);
    if (deleteStatement.executeUpdateDelete() == 1) {
      session.remove(getId(entity));
      return true;
    }
    return false;
  }

  private boolean deleteInsideSynchronized(SQLiteDatabase db, long id) {
    if (deleteByIdStatement == null) {
      final String sql = SqlUtils.createSqlDelete(tableName, idColumn, null);
      deleteByIdStatement = db.compileStatement(sql);
    }
    deleteStatement.bindLong(1, id);
    if (deleteStatement.executeUpdateDelete() == 1) {
      session.remove(id);
      return true;
    }
    return false;
  }

  /**
   * @return true if version is enabled.
   */
  public boolean isVersionEnabled() {
    return versionColumn != null;
  }

  protected abstract long getId(T entity);

  protected long getVersion(T entity) {
    return 0;
  }

  protected abstract T newInstance();

  protected abstract void readCursor(T entity, Cursor cursor);

  protected abstract void onCreate(SQLiteDatabase db);

  protected abstract String getInsertStatement();

  protected abstract void bindInsert(SQLiteStatement statement, T entity);

  protected abstract String getUpdateStatement();

  protected abstract void bindUpdate(SQLiteStatement statement, T entity);

  protected abstract String getDeleteStatement();

  protected abstract void bindDelete(SQLiteStatement statement, T entity);
}
