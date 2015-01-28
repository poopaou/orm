package org.medimob.orm;

/**
 * Created by Poopaou on 20/01/2015.
 */
public final class SqlUtils {

  private SqlUtils() {
  }

  /**
   * Remember: SQLite does not support joins nor table alias for DELETE.
   */
  public static String createSqlDelete(String tablename, String[] columns) {
    StringBuilder builder = new StringBuilder("DELETE FROM ");
    builder.append(tablename);
    if (columns != null && columns.length > 0) {
      builder.append(" WHERE ");
      appendColumnsEqValue(builder, tablename, columns);
    }
    return builder.toString();
  }

  public static String createSqlInsert(String insertInto, String tableName, String[] columns,
                                       String version) {
    StringBuilder builder = new StringBuilder(insertInto);
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

  public static String createSqlUpdate(String tablename, String[] updateColumns, String id,
                                       String version) {
    StringBuilder builder = new StringBuilder("UPDATE ");
    builder.append(tablename).append(" SET ");
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
