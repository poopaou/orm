package org.medimob.orm;

import com.google.common.primitives.Longs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.medimob.orm.internal.Session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
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
  private final String[] insertColumns;
  private final String[] updateColumns;
  // Model properties.
  private final String idColumn;
  private final String versionColumn;
  private final String idSelection;
  private final ThreadLocal<String[]> selectArgsLocal;
  // Database lazy compiled statements.
  private SQLiteStatement insertStatement;
  private SQLiteStatement updateStatement;
  private SQLiteStatement deleteStatement;

  protected Model(String tableName, String idColumn, String versionColumn, String[] queryColumns,
                  String[] insertColumns, String[] updateColumns) {
    this.tableName = tableName;
    this.idColumn = idColumn;
    this.idSelection = idColumn + "=?";
    this.versionColumn = versionColumn;

    this.queryColumns = queryColumns;
    this.insertColumns = insertColumns;
    this.updateColumns = updateColumns;

    this.session = new Session<T>();
    this.selectArgsLocal = new ThreadLocal<String[]>();
  }

  /**
   * Raw query for single result (result will not be cached).
   *
   * @param db           database.
   * @param selection    selection
   * @param selectionArg selection argument.
   * @return result or nul if not found.
   */
  public T rawQuerySingle(@NonNull SQLiteDatabase db, @NonNull String selection,
                          @Nullable String[] selectionArg) {
    return rawQuerySingle(db, selection, selectionArg, null);
  }

  /**
   * Raw query for single result (result will not be cached).
   *
   * @param db           database.
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param having       having clause.
   * @return result or nul if not found.
   */
  public T rawQuerySingle(@NonNull SQLiteDatabase db, @NonNull String selection,
                          @Nullable String[] selectionArg, @Nullable String having) {
    Cursor cursor =
        db.query(tableName, queryColumns, selection, selectionArg, null, having, null, "1");
    try {
      if (cursor.moveToFirst()) {
        return readEntity(cursor);
      }
      return null;
    } finally {
      cursor.close();
    }
  }

  /**
   * Raw query for result (result will not be cached).
   *
   * @param db           database.
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param orderBy      having clause.
   * @param limit        limit clause.
   * @return result or nul if not found.
   */
  public List<T> rawQuery(@NonNull SQLiteDatabase db, @NonNull String selection,
                          @Nullable String[] selectionArg, @Nullable String orderBy,
                          @Nullable String limit) {
    return rawQuery(db, selection, selectionArg, null, null, orderBy, limit);
  }

  /**
   * Raw query for result (result will not be cached).
   *
   * @param db           database.
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param groupBy      groupBy clause.
   * @param having       having clause.
   * @param orderBy      having clause.
   * @param limit        limit clause.
   * @return result or nul if not found.
   */
  public List<T> rawQuery(@NonNull SQLiteDatabase db, @NonNull String selection,
                          @Nullable String[] selectionArg, @Nullable String groupBy,
                          @Nullable String having, @Nullable String orderBy,
                          @Nullable String limit) {
    Cursor cursor =
        db.query(tableName, queryColumns, selection, selectionArg, groupBy, having, orderBy, limit);
    try {
      if (cursor.moveToFirst()) {
        List<T> results = new ArrayList<T>(cursor.getCount());
        do {
          results.add(readEntity(cursor));
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
   * @param db database
   * @param id entity id
   * @return entity or null if not found.
   */
  public T loadById(@NonNull SQLiteDatabase db, long id) {
    T entity = session.get(id);
    if (entity != null) {
      return entity;
    }
    // Cache miss. Load entity from db.
    String[] args = getIdSelectArgs();
    args[0] = String.valueOf(id);
    Cursor cursor = db.query(tableName, queryColumns, idSelection, args, null, null, null);
    try {
      if (cursor.moveToFirst()) {
        entity = readEntity(cursor);
        session.put(id, entity);
      }
      return null;
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
   * @param db       database.
   * @param entities entities to insert.
   * @return entities ids.
   */
  public long[] insertInTx(@NonNull SQLiteDatabase db, @NonNull T... entities) {
    return insertInTx(db, Arrays.asList(entities));
  }

  /**
   * Insert entity inside transaction.
   *
   * @param db       database.
   * @param entities entities to insert.
   * @return entities ids.
   */
  public long[] insertInTx(@NonNull SQLiteDatabase db, @NonNull Iterable<T> entities) {
    if (db.isDbLockedByCurrentThread()) {
      synchronized (insertLock) {
        ArrayList<Long> ids = new ArrayList<Long>();
        for (T entity : entities) {
          ids.add(insertInsideSynchronized(db, entity));
        }
        return Longs.toArray(ids);
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
          return Longs.toArray(ids);
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
   * @param db     database.
   * @param entity entity to insert.
   * @return entity ids.
   */
  public long insertInTx(@NonNull SQLiteDatabase db, @NonNull T entity) {
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
   * @param db     database.
   * @param entity entity to insert.
   * @return entity ids.
   */
  public long insert(@NonNull SQLiteDatabase db, @NonNull T entity) {
    synchronized (insertLock) {
      return insertInsideSynchronized(db, entity);
    }
  }

  private long insertInsideSynchronized(SQLiteDatabase db, T entity) {
    if (getId(entity) > 0) {
      throw new OrmException("Entity is not new");
    }
    if (insertStatement == null) {
      String version = versionColumn != null ? versionColumn : null;
      final String sql = SqlUtils.createSqlInsert("INSERT INTO", tableName, insertColumns, version);
      insertStatement = db.compileStatement(sql);
    }
    bindInsert(insertStatement, entity);
    return insertStatement.executeInsert();
  }

  /**
   * Update entity inside transaction.
   *
   * @param db       database.
   * @param entities entities to update.
   * @return update count.
   */
  public int updateInTx(@NonNull SQLiteDatabase db, @NonNull T... entities) {
    return updateInTx(db, Arrays.asList(entities));
  }

  /**
   * Update entity inside transaction.
   *
   * @param db       database.
   * @param entities entities to update.
   * @return update count.
   */
  public int updateInTx(@NonNull SQLiteDatabase db, @NonNull Iterable<T> entities) {
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
   * @param db     database.
   * @param entity entity to update.
   * @return true if entity has been updated.
   */
  public boolean update(@NonNull SQLiteDatabase db, @NonNull T entity) {
    synchronized (updateLock) {
      return updateInsideSynchronized(db, entity);
    }
  }

  private boolean updateInsideSynchronized(SQLiteDatabase db, T entity) {
    if (updateStatement == null) {
      String version = versionColumn != null ? versionColumn : null;
      final String sql = SqlUtils.createSqlUpdate(tableName, updateColumns, idColumn, version);
      updateStatement = db.compileStatement(sql);
    }

    bindUpdate(updateStatement, entity);
    bindSelection(updateStatement, entity, updateColumns.length);
    if (updateStatement.executeUpdateDelete() == 1) {
      session.remove(getId(entity));
      return true;
    }
    return false;
  }

  private void bindSelection(SQLiteStatement updateStatement, T entity, int offset) {
    updateStatement.bindLong(offset, getId(entity));
    if (versionColumn != null) {
      updateStatement.bindLong(offset + 1, getVersion(entity));
    }
  }

  /**
   * Delete entities by ids without transaction.
   *
   * @param db  database
   * @param ids entities to delete ids.
   * @return delete count.
   */
  public int deleteById(@NonNull SQLiteDatabase db, @NonNull long... ids) {
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
   * @param db  database
   * @param ids entities to delete ids.
   * @return delete count.
   */
  public int deleteByIdInTx(@NonNull SQLiteDatabase db, @NonNull long... ids) {
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
   * @param db       database
   * @param entities entities to delete.
   * @return delete count.
   */
  public int deleteInTx(@NonNull SQLiteDatabase db, @NonNull T... entities) {
    return deleteInTx(db, Arrays.asList(entities));
  }

  /**
   * Delete entities in transaction.
   *
   * @param db       database
   * @param entities entities to delete.
   * @return delete count.
   */
  public int deleteInTx(@NonNull SQLiteDatabase db, @NonNull Iterable<T> entities) {
    if (db.isDbLockedByCurrentThread()) {
      synchronized (deleteLock) {
        int count = 0;
        for (T entity : entities) {
          deleteInsideSynchronized(db, getId(entity));
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
            deleteInsideSynchronized(db, getId(entity));
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
   * @param db     database
   * @param entity entity to delete.
   * @return true if entity has been deleted.
   */
  public boolean deleteInTx(@NonNull SQLiteDatabase db, @NonNull T entity) {
    if (db.isDbLockedByCurrentThread()) {
      synchronized (deleteLock) {
        return deleteInsideSynchronized(db, getId(entity));
      }
    } else {
      db.beginTransaction();
      synchronized (deleteLock) {
        try {
          if (deleteInsideSynchronized(db, getId(entity))) {
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
   * @param db     database
   * @param entity entity to delete.
   * @return true if entity has been deleted.
   */
  public boolean delete(@NonNull SQLiteDatabase db, @NonNull T entity) {
    synchronized (deleteLock) {
      return deleteInsideSynchronized(db, getId(entity));
    }
  }

  private boolean deleteInsideSynchronized(@NonNull SQLiteDatabase db, long id) {
    if (deleteStatement == null) {
      final String sql = SqlUtils.createSqlDelete(tableName, new String[]{idSelection});
      deleteStatement = db.compileStatement(sql);
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

  protected abstract T readEntity(Cursor cursor);

  protected abstract void bindUpdate(SQLiteStatement statement, T entity);

  protected abstract void bindInsert(SQLiteStatement statement, T entity);

  public abstract void onCreate(SQLiteDatabase db);
}
