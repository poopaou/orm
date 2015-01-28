package org.medimob.orm.internal;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.medimob.orm.OrmException;
import org.medimob.orm.OrmLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Statement binding and cursor reading utility helper. Handles simple types and Date.
 *
 * Created by Poopaou on 21/01/2015.
 */
public final class TypeUtils {

  private TypeUtils() {
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable Long value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindLong(index, value);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable Boolean value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindLong(index, value ? 1 : 2);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement,
                          @Nullable Character value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindLong(index, value);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable Byte value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindLong(index, value);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable Short value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindLong(index, value);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable Integer value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindLong(index, value);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable Float value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindDouble(index, value);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable Double value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindDouble(index, value);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable String value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindString(index, value);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable byte[] value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindBlob(index, value);
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable Date value, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    statement.bindLong(index, value.getTime());
  }

  /**
   * Bind statement index value.
   *
   * @param statement statement
   * @param value     value to be binned
   * @param index     value's index
   */
  public static void bind(@NonNull SQLiteStatement statement, @Nullable Date value,
                          @NonNull String pattern, int index) {
    if (value == null) {
      statement.bindNull(index);
      return;
    }
    SimpleDateFormat format = new SimpleDateFormat(pattern);
    statement.bindString(index, format.format(value));
  }

  /**
   * Gets Long value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Long getLong(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return cursor.getLong(column);
  }

  /**
   * Gets Boolean value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Boolean getBoolean(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return cursor.getInt(column) == 1;
  }

  /**
   * Gets Char value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Character getChar(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return (char) cursor.getInt(column);
  }

  /**
   * Gets Byte value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Byte getByte(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return (byte) cursor.getInt(column);
  }

  /**
   * Gets Short value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Short getShort(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return (short) cursor.getInt(column);
  }

  /**
   * Gets Integer value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Integer getInt(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return cursor.getInt(column);
  }

  /**
   * Gets Float value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Float getFloat(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return cursor.getFloat(column);
  }

  /**
   * Gets Double value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Double getDouble(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return cursor.getDouble(column);
  }

  /**
   * Gets String value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static String getString(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return cursor.getString(column);
  }

  /**
   * Gets byte array value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static byte[] getByteArray(@NonNull Cursor cursor, int column) {
    return cursor.getBlob(column);
  }

  /**
   * Gets Date value form cursor for column index.
   *
   * @param cursor cursor
   * @param column column's index.
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Date getDate(@NonNull Cursor cursor, int column) {
    if (cursor.isNull(column)) {
      return null;
    }
    return new Date(cursor.getLong(column));
  }

  /**
   * Gets Date value form cursor for column index.
   *
   * @param cursor  cursor
   * @param column  column's index.
   * @param pattern date pattern
   * @return value or null if column's value is null.
   */
  @Nullable
  public static Date getDate(@NonNull Cursor cursor, int column, @NonNull String pattern) {
    if (cursor.isNull(column)) {
      return null;
    }
    SimpleDateFormat format = new SimpleDateFormat(pattern);
    String value = cursor.getString(column);
    try {
      return format.parse(value);
    } catch (ParseException e) {
      OrmLog.e("Failed to parse date : " + value + " patter : " + pattern, e);
      throw new OrmException("Failed to parse date : " + value + " patter : " + pattern, e);
    }
  }
}
