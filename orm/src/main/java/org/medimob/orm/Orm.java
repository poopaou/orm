package org.medimob.orm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.medimob.orm.internal.OpenHelper;
import org.medimob.orm.processor.EntityProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static android.content.pm.PackageManager.NameNotFoundException;

/**
 * Created by Poopaou on 26/01/2015.
 */
public final class Orm {

  private static final Object LOCK = new Object();
  private static Orm orm;
  private OpenHelper helper;
  private Map<Class<?>, Model<?>> modelMap;

  private Orm(Context context) throws NameNotFoundException, IOException,
                                      ClassNotFoundException, IllegalAccessException,
                                      InstantiationException {
    modelMap = new HashMap<Class<?>, Model<?>>();
    InputStream is = this.getClass().getResourceAsStream(EntityProcessor.ENTITY_LIST_FILE_PATH);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = reader.readLine()) != null) {
      Class<?> dataClazz = Class.forName(line);
      Class<?> modelClazz = Class.forName(line + EntityProcessor.CLASS_MODEL_SUFFIX);
      Model<?> model = (Model<?>) modelClazz.newInstance();
      modelMap.put(dataClazz, model);
    }

    PackageManager pm = context.getPackageManager();
    ApplicationInfo meta =
        pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
    String name = meta.metaData.getString("org.medimob.orm.dbName");
    int version = meta.metaData.getInt("org.medimob.orm.dbVersion");
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
}
