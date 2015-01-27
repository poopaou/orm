package org.medimob.orm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import com.google.common.primitives.Longs;
import org.medimob.orm.internal.Session;

import java.util.*;

/**
 * Created by Poopaou on 23/01/2015.
 */
public abstract class Model<T> {

    private final Session<T> session;

    // Database lazy compiled statements.
    private SQLiteStatement insertStatement;
    private SQLiteStatement updateStatement;
    private SQLiteStatement deleteStatement;

    // Statements locks :
    // ensure that database statements
    // and binding are executed
    // synchronously.
    private final Object insertLock = new Object();
    private final Object updateLock = new Object();
    private final Object deleteLock = new Object();

    // Entity attributes.
    private final String   tableName;

    private final String[] queryColumns;
    private final String[] insertColumns;
    private final String[] updateColumns;

    // Model properties.
    private final String idColumn;
    private final String versionColumn;

    private final String   idSelection;

    private final ThreadLocal<String[]> selectArgsLocal;

    protected Model(String tableName, String idColumn, String versionColumn, String[] queryColumns, String[] insertColumns, String[] updateColumns) {
        this.tableName = tableName;
        this.idColumn =  idColumn;
        this.idSelection = idColumn + "=?";
        this.versionColumn = versionColumn;

        this.queryColumns = queryColumns;
        this.insertColumns = insertColumns;
        this.updateColumns = updateColumns;

        this.session = new Session<T>();
        this.selectArgsLocal = new ThreadLocal<String[]>();
    }

    public T rawQuerySingle(SQLiteDatabase db, String selection, String[] selectionArg){
        return rawQuerySingle(db, selection, selectionArg, null);
    }

    public T rawQuerySingle(SQLiteDatabase db, String selection, String[] selectionArg, String having){
        Cursor cursor = db.query(tableName, queryColumns, selection, selectionArg, null, having, null, "1");
        try {
            if (cursor.moveToFirst()){
                return readEntity(cursor);
            }
            return null;
        }
        finally {
            cursor.close();
        }
    }

    public List<T> rawQuery(SQLiteDatabase db, String selection, String[] selectionArg, String orderBy, String limit){
        return rawQuery(db, selection, selectionArg, null, null, orderBy, limit);
    }

    public List<T> rawQuery(SQLiteDatabase db, String selection, String[] selectionArg, String groupBy, String having, String orderBy, String limit){
        Cursor cursor = db.query(tableName, queryColumns, selection, selectionArg, groupBy, having, orderBy, limit);
        try {
            if (cursor.moveToFirst()){
                List<T> results = new ArrayList<T>(cursor.getCount());
                do {
                    results.add(readEntity(cursor));
                }
                while (cursor.moveToNext());

                return results;
            }
            return Collections.emptyList();
        }
        finally {
            cursor.close();
        }
    }

    public T loadById(SQLiteDatabase db, long id){
        T entity = session.get(id);
        if(entity != null){
            return entity;
        }
        // Cache miss. Load entity from db.
        String[] args = getIdSelectArgs();
        args[0] = String.valueOf(id);
        Cursor cursor = db.query(tableName, queryColumns, idSelection, args, null, null, null);
        try {
            if (cursor.moveToFirst()){
                entity = readEntity(cursor);
                session.put(id, entity);
            }
            return null;
        }
        finally {
            cursor.close();
        }
    }

    private String[] getIdSelectArgs(){
        String[] args = selectArgsLocal.get();
        if (args == null){
            args = new String[1];
            selectArgsLocal.set(args);
        }
        return args;
    }

    public long[] insertInTx(SQLiteDatabase db, T... entities){
        return insertInTx(db, Arrays.asList(entities));
    }

    public long[] insertInTx(SQLiteDatabase db, Iterable<T> entities){
        if(db.isDbLockedByCurrentThread()){
            synchronized (insertLock){
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

    public long insertInTx(SQLiteDatabase db, T entity){
        if(db.isDbLockedByCurrentThread()){
            synchronized (insertLock){
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

    public long insert(SQLiteDatabase db, T entity){
        synchronized (insertLock) {
            return insertInsideSynchronized(db, entity);
        }
    }

    private long insertInsideSynchronized(SQLiteDatabase db, T entity){
        if(getId(entity) > 0){
            throw new OrmException("Entity is not new");
        }
        if(insertStatement == null){
            String version = versionColumn != null ? versionColumn : null;
            final String sql = SqlUtils.createSqlInsert("INSERT INTO", tableName, insertColumns, version);
            insertStatement = db.compileStatement(sql);
        }
        bindInsert(insertStatement, entity);
        return insertStatement.executeInsert();
    }

    public int updateInTx(SQLiteDatabase db, T... entities){
        return updateInTx(db, Arrays.asList(entities));
    }

    public int updateInTx(SQLiteDatabase db, Iterable<T> entities){
        if(db.isDbLockedByCurrentThread()){
            synchronized (updateLock){
                int count = 0;
                for (T entity : entities) {
                    updateInsideSynchronized(db, entity);
                    count ++;
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
                        count ++;
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

    public boolean updateInTx(SQLiteDatabase db, T entity){
        if(db.isDbLockedByCurrentThread()){
            synchronized (updateLock){
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

    public boolean update(SQLiteDatabase db, T entity){
        synchronized (updateLock) {
            return updateInsideSynchronized(db, entity);
        }
    }

    private boolean updateInsideSynchronized(SQLiteDatabase db, T entity) {
        if(updateStatement == null){
            String version = versionColumn != null ? versionColumn : null;
            final String sql = SqlUtils.createSqlUpdate(tableName, updateColumns, idColumn, version);
            updateStatement = db.compileStatement(sql);
        }

        bindUpdate(updateStatement, entity);
        bindSelection(updateStatement, entity, updateColumns.length);
        if (updateStatement.executeUpdateDelete() == 1){
            session.remove(getId(entity));
            return true;
        }
        return false;
    }

    private void bindSelection(SQLiteStatement updateStatement, T entity, int offset) {
        updateStatement.bindLong(offset, getId(entity));
        if (versionColumn != null){
            updateStatement.bindLong(offset + 1, getVersion(entity));
        }
    }

    public int delete(SQLiteDatabase db, long... ids){
        synchronized (deleteLock) {
            int count = 0;
            for (long id : ids) {
                deleteInsideSynchronized(db, id);
                count++;
            }
            return count;
        }
    }

    public int deleteInTx(SQLiteDatabase db, long... ids){
        if(db.isDbLockedByCurrentThread()){
            synchronized (deleteLock){
                int count = 0;
                for (long id : ids) {
                    deleteInsideSynchronized(db, id);
                    count ++;
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
                        count ++;
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

    public int deleteInTx(SQLiteDatabase db, T... entities){
        return deleteInTx(db, Arrays.asList(entities));
    }

    public int deleteInTx(SQLiteDatabase db, Iterable<T> entities){
        if(db.isDbLockedByCurrentThread()){
            synchronized (deleteLock){
                int count = 0;
                for (T entity : entities) {
                    deleteInsideSynchronized(db, getId(entity));
                    count ++;
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
                        count ++;
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

    public boolean deleteInTx(SQLiteDatabase db, T entity){
        if(db.isDbLockedByCurrentThread()){
            synchronized (deleteLock){
                return deleteInsideSynchronized(db, getId(entity));
            }
        } else {
            db.beginTransaction();
            synchronized (deleteLock) {
                try {
                    if (deleteInsideSynchronized(db, getId(entity))){
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

    public boolean delete(SQLiteDatabase db, T entity){
        synchronized (deleteLock) {
            return deleteInsideSynchronized(db, getId(entity));
        }
    }

    private boolean deleteInsideSynchronized(SQLiteDatabase db, long id) {
        if(deleteStatement == null){
            final String sql = SqlUtils.createSqlDelete(tableName, new String[]{idSelection});
            deleteStatement = db.compileStatement(sql);
        }
        deleteStatement.bindLong(1, id);
        if (deleteStatement.executeUpdateDelete() == 1){
            session.remove(id);
            return true;
        }
        return false;
    }

    public boolean isVersionEnabled(){
        return versionColumn != null;
    }

    protected abstract long getId(T entity);

    protected long getVersion(T entity){
        return 0;
    }

    protected abstract T readEntity(Cursor cursor);

    protected abstract void bindUpdate(SQLiteStatement statement, T entity);

    protected abstract void bindInsert(SQLiteStatement statement, T entity);

    public abstract void onCreate(SQLiteDatabase db);
}
