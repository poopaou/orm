package org.medimob.orm.processor;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.squareup.javawriter.JavaWriter;

import org.medimob.orm.Model;
import org.medimob.orm.internal.TypeUtils;
import org.medimob.orm.processor.dll.PropertyDefinition;
import org.medimob.orm.processor.dll.TypeDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by Poopaou on 20/01/2015.
 */
class TypeWriter {

  private static final String TABLE_NAME_FIELD = "TABLE_NAME";
  private static final String ID_COLUMN_FIELD = "ID_COLUMN";
  private static final String VERSION_COLUMN_FIELD = "VERSION_COLUMN";
  private static final String QUERY_COLUMNS_FIELD = "QUERY_COLUMNS";
  private static final String INSERT_COLUMNS_FIELD = "INSERT_COLUMNS";
  private static final String UPDATE_COLUMNS_FIELD = "UPDATE_COLUMNS";
  private static final String UTIL_CLASS_NAME = TypeUtils.class.getName();

  private final Filer filer;

  public TypeWriter(Filer filer) {
    this.filer = filer;
  }

  private static String formatStringArray(List<String> columns) {
    StringBuilder builder = new StringBuilder();
    builder.append("new String[]{\n");
    boolean first = true;
    for (String col : columns) {
      if (first) {
        first = false;
      } else {
        builder.append(", ");
      }
      builder.append(quoteString(col));
    }
    builder.append("\n}");
    return builder.toString();
  }

  private static String quoteString(String string) {
    StringBuilder builder = new StringBuilder(string.length());
    quoteString(builder, string);
    return builder.toString();
  }

  private static void quoteString(StringBuilder builder, String string) {
    builder.append('"').append(string).append('"');
  }

  public void writeType(TypeDefinition typeDefinition) throws IOException {
    final String typeSimpleName = typeDefinition.getTypeSimpleName();

    JavaFileObject file =
        filer.createSourceFile(typeSimpleName + EntityProcessor.CLASS_MODEL_SUFFIX);
    JavaWriter writer = new JavaWriter(file.openWriter());

    writer.emitSingleLineComment("AUTO GENERATED CLASS : DO NOT MODIFIED !!!");
    writer.emitPackage(typeDefinition.getPackageName());

    final PropertyDefinition idField = typeDefinition.getIdColumn();
    final PropertyDefinition versionField = typeDefinition.getVersionColumn();

    // Imports
    writer.emitImports(
        Cursor.class,
        SQLiteStatement.class
    );
    writer.emitEmptyLine();
    writer.emitImports(
        typeDefinition.getTypeQualifiedName(),
        Model.class.getCanonicalName(),
        TypeUtils.class.getCanonicalName()
    );

    // Store columns order in list to preserve order
    // between declaration initialization and bindings
    List<String> queryCols = new ArrayList<String>();
    List<String> insertCols = new ArrayList<String>();
    List<String> updateCols = new ArrayList<String>();

    //queryCols.add(idField.getColumnName());
    String columnName;
    for (PropertyDefinition columnDef : typeDefinition.getColumns()) {
      columnName = columnDef.getColumnName();
      queryCols.add(columnName);
      if (columnDef.isInsertable()) {
        insertCols.add(columnName);
      }
      if (columnDef.isUpdateable()) {
        updateCols.add(columnName);
      }
    }
    if (versionField != null) {
      queryCols.add(versionField.getColumnName());
    }

    // Type begin
    writer.emitEmptyLine();
    writer.beginType(typeDefinition.getTypeSimpleName() + "$$Dao", "class",
                     EnumSet.of(PUBLIC, FINAL),
                     "Model<" + typeSimpleName + ">");

    // Table name constant.
    writer.emitEmptyLine();
    writer.emitField("String", TABLE_NAME_FIELD, EnumSet.of(PRIVATE, STATIC, FINAL),
                     quoteString(typeDefinition.getTableName()));

    writer.emitEmptyLine();
    writer.emitField("String", ID_COLUMN_FIELD, EnumSet.of(PRIVATE, STATIC, FINAL),
                     quoteString(idField.getColumnName()));

    writer.emitEmptyLine();
    writer.emitField("String", VERSION_COLUMN_FIELD, EnumSet.of(PRIVATE, STATIC, FINAL),
                     versionField != null ? quoteString(versionField.getColumnName()) : "null");

    writer.emitEmptyLine();
    writer.emitField("String[]", QUERY_COLUMNS_FIELD, EnumSet.of(PRIVATE, STATIC, FINAL),
                     formatStringArray(queryCols));
    writer.emitEmptyLine();
    writer.emitField("String[]", INSERT_COLUMNS_FIELD, EnumSet.of(PRIVATE, STATIC, FINAL),
                     formatStringArray(insertCols));
    writer.emitEmptyLine();
    writer.emitField("String[]", UPDATE_COLUMNS_FIELD, EnumSet.of(PRIVATE, STATIC, FINAL),
                     formatStringArray(updateCols));

    // BEGIN : constructor :
    // No arg constructor parent fields are
    // initialized with class static fields.
    writer.emitEmptyLine();
    writer.beginConstructor(EnumSet.of(PUBLIC));
    writer.emitStatement("super(%s, %s, %s, %s, %s, %s)", TABLE_NAME_FIELD, ID_COLUMN_FIELD,
                         VERSION_COLUMN_FIELD,
                         QUERY_COLUMNS_FIELD, INSERT_COLUMNS_FIELD, UPDATE_COLUMNS_FIELD);
    writer.endConstructor(); // END

    // BEGIN : "getId" method (mandatory).
    writer.emitEmptyLine();
    writer.beginMethod("long", "getId", EnumSet.of(PUBLIC, FINAL), typeSimpleName, "entity");
    writer.emitStatement("return entity.%s", idField.getFieldName());
    writer.endMethod();
    // END

    if (versionField != null) {
      // BEGIN : "getVersion" method :
      // Override return value if version
      // field is present.
      writer.emitEmptyLine();
      writer.beginMethod("long", "getVersion", EnumSet.of(PUBLIC, FINAL), typeSimpleName, "entity");
      writer.emitStatement("return entity.%s", versionField.getFieldName());
      writer.endMethod();
      // END
    }

    // BEGIN : "readEntity" method (mandatory).
    writer.emitEmptyLine();
    readCursorForFields(writer, typeDefinition, queryCols);

    // BEGIN : BindUpdate method (mandatory).
    writer.emitEmptyLine();
    createBindMethod("bindUpdate", writer, typeDefinition, updateCols);

    // BEGIN : bindInsert method (mandatory)
    writer.emitEmptyLine();
    createBindMethod("bindInsert", writer, typeDefinition, insertCols);

    // Type end.
    writer.endType();
    writer.close();
  }

   /* private String formatSqlStatement(TypeDefinition typeDefinition){
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        builder.append(typeDefinition.getStatement());

        builder.append( "(\"\n"); // Open table bracket

        // Not null id column
        builder.append("+ \"");
        builder.append(typeDefinition.getIdColumn().getStatement());
        builder.append("\"\n");

        // Others columns definitions
        for (PropertyDefinition propertyDefinition : typeDefinition.getColumns()){
            builder.append("+ \",");
            builder.append(propertyDefinition.getStatement());
            builder.append("\"\n");
        }
        //  Nullable version column
        if (typeDefinition.getVersionColumn() != null){
            builder.append("+ \",");
            builder.append(typeDefinition.getVersionColumn().getStatement());
            builder.append("\"\n");
        }
        builder.append("+ \"); \"\n"); // Table definition end

        for (IndexDefinition index : typeDefinition.getIndexes()){
            builder.append("+ \"");
            builder.append(index.getStatement());
            builder.append("; \"\n");
        }

        for (TriggerDefinition trigger : typeDefinition.getTriggers()){
            builder.append("+ \"");
            builder.append(trigger.getStatement());
            builder.append("; \"\n");
        }
        return builder.toString();
    }*/

  private void readCursorForFields(JavaWriter writer, TypeDefinition definition,
                                   List<String> queryCols) throws IOException {
    writer.beginMethod(definition.getTypeSimpleName(), "readEntity", EnumSet.of(PUBLIC, FINAL),
                       "Cursor", "cursor");
    writer.emitStatement("final %s entity = new %s()", definition.getTypeSimpleName(),
                         definition.getTypeSimpleName());

    // Handle id column
    PropertyDefinition idColumn = definition.getIdColumn();
    writer.emitStatement("entity.%s = TypeUtils.getLong(cursor, %s)", idColumn.getFieldName(),
                         queryCols.indexOf(idColumn.getColumnName()));

    for (PropertyDefinition propertyDefinition : definition.getColumns()) {
      String columnName = propertyDefinition.getColumnName();
      int index = queryCols.indexOf(columnName);
      if (index == -1) {
        continue;
      }
      String field = propertyDefinition.getFieldName();
      switch (propertyDefinition.getPropertyType()) {
        case BOOLEAN:
          writer.emitStatement("entity.%s = %s.getBoolean(cursor, %s)", field, UTIL_CLASS_NAME,
                               index);
          break;
        case BYTE:
          writer.emitStatement("entity.%s = %s.getByte(cursor, %s)", field, UTIL_CLASS_NAME, index);
          break;
        case BYTE_ARRAY:
          writer.emitStatement("entity.%s = %s.getByteArray(cursor, %s)", field, UTIL_CLASS_NAME,
                               index);
          break;
        case CHARACTER:
          writer.emitStatement("entity.%s = %s.getChar(cursor, %s)", field, UTIL_CLASS_NAME, index);
          break;
        case DATE_LONG:
          writer.emitStatement("entity.%s = %s.getDate(cursor, %s)", field, UTIL_CLASS_NAME, index);
          break;
        case DATE_STRING:
          writer.emitStatement("entity.%s = %s.getDate(cursor, %s, %s)", field, UTIL_CLASS_NAME,
                               index, quoteString(propertyDefinition.getDateFormat()));
          break;
        case DOUBLE:
          writer.emitStatement("entity.%s = %s.getDouble(cursor, %s)", field, UTIL_CLASS_NAME,
                               index);
          break;
        case FLOAT:
          writer.emitStatement("entity.%s = %s.getFloat(cursor, %s)", field, UTIL_CLASS_NAME,
                               index);
          break;
        case INTEGER:
          writer.emitStatement("entity.%s = %s.getInt(cursor, %s)", field, UTIL_CLASS_NAME, index);
          break;
        case LONG:
          writer.emitStatement("entity.%s = %s.getLong(cursor, %s)", field, UTIL_CLASS_NAME, index);
          break;
        case SHORT:
          writer.emitStatement("entity.%s = %s.getShort(cursor, %s)", field, UTIL_CLASS_NAME,
                               index);
          break;
        case STRING:
          writer.emitStatement("entity.%s = %s.getString(cursor, %s)", field, UTIL_CLASS_NAME,
                               index);
          break;
        default:
          throw new IllegalStateException("Illegal type " + propertyDefinition.getPropertyType());
      }
    }

    // Handle version column
    PropertyDefinition version = definition.getVersionColumn();
    if (version != null) {
      writer.emitStatement("entity.%s = %s.getLong(cursor, %s)", idColumn.getFieldName(),
                           UTIL_CLASS_NAME,
                           queryCols.indexOf(idColumn.getColumnName()));
    }

    writer.emitStatement("return entity");
    writer.endMethod();
    // END
  }

  private void createBindMethod(String methodName, JavaWriter writer, TypeDefinition typeDefinition,
                                List<String> columnNames) throws IOException {
    writer.beginMethod("void", methodName, EnumSet.of(PUBLIC, FINAL), "SQLiteStatement",
                       "statement",
                       typeDefinition.getTypeSimpleName(), "entity");

    for (PropertyDefinition definition : typeDefinition.getColumns()) {
      int index = columnNames.indexOf(definition.getColumnName()) + 1;
      if (index < 0) {
        continue;
      }
      writer.emitStatement("%s.bind(statement, entity.%s, %s)", UTIL_CLASS_NAME,
                           definition.getFieldName(), index);
    }
    writer.endMethod(); // END
  }
}
