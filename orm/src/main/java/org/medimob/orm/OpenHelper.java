package org.medimob.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Collection;

/**
 * Bd open helper.
 * Created by Poopaou on 26/01/2015.
 */
class OpenHelper extends SQLiteOpenHelper {

  private final Collection<Model<?>> models;

  public OpenHelper(Context context, String name, int version, Collection<Model<?>> models) {
    super(context, name, null, version);
    this.models = models;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    for (Model<?> m : models) {
      m.onCreate(db);
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO.
  }
}
