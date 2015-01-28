package org.medimob.orm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Poopaou on 20/01/2015.
 */
public final class SqlUtils {

  private SqlUtils() {
  }

  /**
   * Create sql delete.
   * @param tableName table's name.
   * @param selection selections columns.
   * @return sql statement.
   */
  @NonNull
  public static String createSqlDelete(@NonNull String tableName, @Nullable String[] selection) {
    StringBuilder builder = new StringBuilder("DELETE FROM ");
    builder.append(tableName);
    if (selection != null && selection.length > 0) {
      builder.append(" WHERE ");
      appendColumnsEqValue(builder, tableName, selection);
    }
    return builder.toString();
  }

  /**
   * Create sql insert.
   *
   * @param tableName table's name.
   * @param columns   inserted columns.
   * @param version   version columns.
   * @return sql statement.
   */
  @NonNull
  public static String createSqlInsert(@NonNull String tableName, @NonNull String[] columns,
                                       @Nullable String version) {
    StringBuilder builder = new StringBuilder("INSERT INTO ");
    builder.append(tableName).append(" (");
    appendColumns(builder, columns);
    if (version != null) {
      builder.append(',');
      builder.append(version);
    }
    builder.append(") VALUES (");
    appendPlaceholders(builder, columns.length);
    if (version != null) {
      builder.append(",0");
    }
    builder.append(')');
    return builder.toString();
  }

  /**
   * Create sql update.
   *
   * @param tableName     table's name.
   * @param updateColumns update columns.
   * @param id            id column.
   * @param version       version columns.
   * @return sql statement.
   */
  @NonNull
  public static String createSqlUpdate(@NonNull String tableName, @NonNull String[] updateColumns,
                                       @NonNull String id, @Nullable String version) {
    StringBuilder builder = new StringBuilder("UPDATE ");
    builder.append(tableName).append(" SET ");
    appendColumnsEqualPlaceholders(builder, updateColumns);
    if (version != null) {
      builder.append(',');
      builder.append(version);
      builder.append("=");
      builder.append(version);
      builder.append(" + 1");
    }

    builder.append(" WHERE ");
    builder.append(id);
    builder.append("=?");
    if (version != null) {
      builder.append(',');
      builder.append(version);
      builder.append("=?");
    }
    return builder.toString();
  }

  private static StringBuilder appendColumnsEqualPlaceholders(StringBuilder builder,
                                                              String[] columns) {
    for (int i = 0; i < columns.length; i++) {
      appendColumn(builder, columns[i]).append("=?");
      if (i < columns.length - 1) {
        builder.append(',');
      }
    }
    return builder;
  }

  private static StringBuilder appendColumnsEqValue(StringBuilder builder, String tableAlias,
                                                    String[] columns) {
    for (int i = 0; i < columns.length; i++) {
      appendColumn(builder, tableAlias, columns[i]).append("=?");
      if (i < columns.length - 1) {
        builder.append(',');
      }
    }
    return builder;
  }

  private static StringBuilder appendColumn(StringBuilder builder, String tableAlias,
                                            String column) {
    builder.append(tableAlias).append(".'").append(column).append('\'');
    return builder;
  }

  private static StringBuilder appendColumn(StringBuilder builder, String column) {
    builder.append('\'').append(column).append('\'');
    return builder;
  }

  private static void appendColumns(StringBuilder builder, String[] columns) {
    int length = columns.length;
    for (int i = 0; i < length; i++) {
      builder.append('\'').append(columns[i]).append('\'');
      if (i < length - 1) {
        builder.append(',');
      }
    }
  }

  private static StringBuilder appendPlaceholders(StringBuilder builder, int count) {
    for (int i = 0; i < count; i++) {
      if (i < count - 1) {
        builder.append("?,");
      } else {
        builder.append('?');
      }
    }
    return builder;
  }
}
