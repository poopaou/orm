package org.medimob.orm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.medimob.orm.processor.EntityProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.pm.PackageManager.NameNotFoundException;

/**
 * Orm manger.
 *
 * Created by Poopaou on 26/01/2015.
 */
public final class Orm {

  private static final Object LOCK = new Object();
  private static Orm orm;
  private OpenHelper helper;
  private Map<Class<?>, AbstractModel<?>> modelMap;

  private Orm(Context context) throws NameNotFoundException, IOException,
                                      ClassNotFoundException, IllegalAccessException,
                                      InstantiationException {
    modelMap = new HashMap<Class<?>, AbstractModel<?>>();
    InputStream is = getClass().getClassLoader()
        .getResourceAsStream(EntityProcessor.ENTITY_LIST_FILE_PATH);

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = reader.readLine()) != null) {
      Class<?> dataClazz = Class.forName(line);
      Class<?> modelClazz = Class.forName(line + EntityProcessor.CLASS_MODEL_SUFFIX);
      AbstractModel<?> model = (AbstractModel<?>) modelClazz.newInstance();
      modelMap.put(dataClazz, model);
    }

    PackageManager pm = context.getPackageManager();
    ApplicationInfo meta =
        pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
    String name = meta.metaData.getString("orm.dbName");
    //int version = meta.metaData.get("orm.dbVersion" );
    int version = 1; //FIXME handle db visioning
    helper = new OpenHelper(context.getApplicationContext(), name, version, modelMap.values());
  }

  /**
   * Get ORM instance.
   *
   * @param context context.
   * @return instance.
   */
  public static Orm getInstance(Context context) {
    synchronized (LOCK) {
      if (orm == null) {
        try {
          orm = new Orm(context);
        } catch (Exception e) {
          OrmLog.e("Failed to initialize ORM", e);
          throw new IllegalStateException(e);
        }
      }
      return orm;
    }
  }

  /**
   * Raw query for single result (result will not be cached).
   *
   * @param clazz        data class.
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param <T>          class type
   * @return result or nul if not found.
   */
  public <T> T rawQuerySingle(@NonNull Class<T> clazz, @NonNull String selection,
                              @Nullable String[] selectionArg) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.rawQuerySingle(selection, selectionArg);
  }

  /**
   * Raw query for single result (result will not be cached).
   *
   * @param clazz        data class.
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param having       having clause.
   * @param <T>          class type
   * @return result or nul if not found.
   */
  public <T> T rawQuerySingle(@NonNull Class<T> clazz, @NonNull String selection,
                              @Nullable String[] selectionArg, @Nullable String having) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.rawQuerySingle(selection, selectionArg, having);
  }

  /**
   * Raw query for result (result will not be cached).
   *
   * @param clazz        data class.
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param orderBy      having clause.
   * @param limit        limit clause.
   * @param <T>          class type
   * @return result or nul if not found.
   */
  public <T> List<T> rawQuery(@NonNull Class<T> clazz, @NonNull String selection,
                              @Nullable String[] selectionArg, @Nullable String orderBy,
                              @Nullable String limit) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.rawQuery(selection, selectionArg, orderBy, limit);
  }

  /**
   * Raw query for result (result will not be cached).
   *
   * @param clazz        data class.
   * @param selection    selection
   * @param selectionArg selection argument.
   * @param groupBy      groupBy clause.
   * @param having       having clause.
   * @param orderBy      having clause.
   * @param limit        limit clause.
   * @param <T>          class type
   * @return result or nul if not found.
   */
  public <T> List<T> rawQuery(@NonNull Class<T> clazz, @NonNull String selection,
                              @Nullable String[] selectionArg, @Nullable String groupBy,
                              @Nullable String having, @Nullable String orderBy,
                              @Nullable String limit) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.rawQuery(selection, selectionArg, groupBy, having,
                          orderBy, limit);
  }

  /**
   * Load entity by id (result will be cached).
   *
   * @param clazz data class.
   * @param id    entity id
   * @param <T>   class type
   * @return entity or null if not found.
   */
  public <T> T loadById(@NonNull Class<T> clazz, long id) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.loadById(id);
  }

  /**
   * Insert entity inside transaction.
   *
   * @param clazz    data class.
   * @param entities entities to insert.
   * @param <T>      class type
   * @return entities ids.
   */
  public <T> long[] insertInTx(@NonNull Class<T> clazz, @NonNull T... entities) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.insertInTx(entities);
  }

  /**
   * Insert entity inside transaction.
   *
   * @param clazz    data class.
   * @param entities entities to insert.
   * @param <T>      class type
   * @return entities ids.
   */
  public <T> long[] insertInTx(@NonNull Class<T> clazz, @NonNull Iterable<T> entities) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.insertInTx(entities);
  }

  /**
   * Insert entity inside transaction.
   *
   * @param clazz  data class.
   * @param entity entity to insert.
   * @param <T>    class type
   * @return entity ids.
   */
  public <T> long insertInTx(@NonNull Class<T> clazz, @NonNull T entity) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.insertInTx(entity);
  }

  /**
   * Insert entity without transaction.
   *
   * @param clazz  data class.
   * @param entity entity to insert.
   * @param <T>    class type
   * @return entity ids.
   */
  public <T> long insert(@NonNull Class<T> clazz, @NonNull T entity) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.insert(entity);
  }

  /**
   * Update entity inside transaction.
   *
   * @param clazz    data class.
   * @param entities entities to update.
   * @param <T>      class type
   * @return update count.
   */
  public <T> int updateInTx(@NonNull Class<T> clazz, @NonNull T... entities) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.updateInTx(entities);
  }

  /**
   * Update entity inside transaction.
   *
   * @param clazz    data class.
   * @param entities entities to update.
   * @param <T>      class type
   * @return update count.
   */
  public <T> int updateInTx(@NonNull Class<T> clazz, @NonNull Iterable<T> entities) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.updateInTx(entities);
  }

  /**
   * Update entity inside transaction.
   *
   * @param clazz  data class.
   * @param entity entity to update.
   * @param <T>    class type
   * @return true if entity has been updated.
   */
  public <T> boolean updateInTx(@NonNull Class<T> clazz, T entity) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.updateInTx(helper.getWritableDatabase(), entity);
  }

  /**
   * Update entity without transaction.
   *
   * @param clazz  data class.
   * @param entity entity to update.
   * @param <T>    class type
   * @return true if entity has been updated.
   */
  public <T> boolean update(@NonNull Class<T> clazz, @NonNull T entity) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.update(entity);
  }

  /**
   * Delete entities by ids without transaction.
   *
   * @param clazz data class.
   * @param ids   entities to delete ids.
   * @param <T>   class type
   * @return delete count.
   */
  public <T> int deleteById(@NonNull Class<T> clazz, @NonNull long... ids) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.deleteById(ids);
  }

  /**
   * Delete entities by ids in transaction.
   *
   * @param clazz data class.
   * @param ids   entities to delete ids.
   * @param <T>   class type
   * @return delete count.
   */
  public <T> int deleteByIdInTx(@NonNull Class<T> clazz, @NonNull long... ids) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.deleteByIdInTx(ids);
  }

  /**
   * Delete entities in transaction.
   *
   * @param clazz    data class.
   * @param entities entities to delete.
   * @param <T>      class type
   * @return delete count.
   */
  public <T> int deleteInTx(@NonNull Class<T> clazz, @NonNull T... entities) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.deleteInTx(entities);
  }

  /**
   * Delete entities in transaction.
   *
   * @param clazz    data class.
   * @param entities entities to delete.
   * @param <T>      class type
   * @return delete count.
   */
  public <T> int deleteInTx(@NonNull Class<T> clazz, @NonNull Iterable<T> entities) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.deleteInTx(entities);
  }

  /**
   * Delete entity in transaction.
   *
   * @param clazz  data class.
   * @param entity entity to delete.
   * @param <T>    class type
   * @return true if entity has been deleted.
   */
  public <T> boolean deleteInTx(@NonNull Class<T> clazz, @NonNull T entity) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.deleteInTx(entity);
  }

  /**
   * Delete entity in transaction.
   *
   * @param clazz  data class.
   * @param entity entity to delete.
   * @param <T>    class type
   * @return true if entity has been deleted.
   */
  public <T> boolean delete(@NonNull Class<T> clazz, @NonNull T entity) {
    AbstractModel<T> model = getModelInstanceOrThrow(clazz);
    return model.deleteInTx(entity);
  }

  public <T> AbstractModel<T> getRepository(Class<T> clazz) {
    return getModelInstanceOrThrow(clazz);
  }

  @NonNull
  @SuppressWarnings("unchecked")
  private <T> AbstractModel<T> getModelInstanceOrThrow(Class<T> clazz) {
    AbstractModel<T> model = (AbstractModel<T>) modelMap.get(clazz);
    if (model == null) {
      throw new IllegalStateException(clazz.getCanonicalName() + " is not a registered entity");
    }
    model.attach(helper);
    return model;
  }
}
