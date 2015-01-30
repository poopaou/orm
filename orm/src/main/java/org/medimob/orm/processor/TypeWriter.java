package org.medimob.orm.processor;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.medimob.orm.Model;
import org.medimob.orm.internal.SqlUtils;
import org.medimob.orm.internal.TypeUtils;
import org.medimob.orm.processor.dll.IndexDefinition;
import org.medimob.orm.processor.dll.PropertyDefinition;
import org.medimob.orm.processor.dll.TriggerDefinition;
import org.medimob.orm.processor.dll.TypeDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Generate Model class helper. Created by Poopaou on 20/01/2015.
 */
class TypeWriter {

  private static final String TABLE_NAME_FIELD = "TABLE_NAME";
  private static final String ID_COLUMN_FIELD = "ID_COLUMN";
  private static final String VERSION_COLUMN_FIELD = "VERSION_COLUMN";
  private static final String QUERY_COLUMNS_FIELD = "QUERY_COLUMNS";
  //private static final String INSERT_COLUMNS_FIELD = "INSERT_COLUMNS";
  //private static final String UPDATE_COLUMNS_FIELD = "UPDATE_COLUMNS";

  public static final String[] CONSTRUCTOR_ARGS = new String[]{
      TABLE_NAME_FIELD,
      ID_COLUMN_FIELD, VERSION_COLUMN_FIELD,
      QUERY_COLUMNS_FIELD,
      //INSERT_COLUMNS_FIELD,
      //UPDATE_COLUMNS_FIELD
  };

  private final Filer filer;

  public TypeWriter(Filer filer) {
    this.filer = filer;
  }

  private static String createSqlInsert(TypeDefinition def, List<String> insertCols) {
    PropertyDefinition versionProp = def.getVersionColumn();
    return SqlUtils.createSqlInsert(def.getTableName(),
                                    insertCols.toArray(new String[insertCols.size()]),
                                    versionProp != null ? versionProp.getColumnName() : null);
  }

  private static String createSqlUpdate(TypeDefinition def, List<String> updateColumns) {
    PropertyDefinition versionProp = def.getVersionColumn();
    return SqlUtils.createSqlUpdate(def.getTableName(),
                                    updateColumns.toArray(new String[updateColumns.size()]),
                                    def.getIdColumn().getColumnName(),
                                    versionProp != null ? versionProp.getColumnName() : null);
  }

  private static String createSqlDelete(TypeDefinition def) {
    PropertyDefinition versionProp = def.getVersionColumn();
    return SqlUtils.createSqlDelete(def.getTableName(), def.getIdColumn().getColumnName(),
                                    versionProp != null ? versionProp.getColumnName() : null);
  }

  private static String formatStringArray(int size) {
    StringBuilder builder = new StringBuilder();
    builder.append("new String[]{");
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append("$S");
    }
    builder.append(" }");
    return builder.toString();

  }

  public void writeType(TypeElement typeElement, TypeDefinition typeDefinition) throws IOException {
    final String typeSimpleName = typeDefinition.getTypeSimpleName();
    final String packageName = typeDefinition.getPackageName();
    final String generatedClassName = typeSimpleName + EntityProcessor.CLASS_MODEL_SUFFIX;

    final PropertyDefinition idField = typeDefinition.getIdColumn();
    final PropertyDefinition versionField = typeDefinition.getVersionColumn();

    // Store columns order in list to preserve order
    // between declaration initialization and bindings
    List<String> queryCols = new ArrayList<String>();
    List<String> insertCols = new ArrayList<String>();
    List<String> updateCols = new ArrayList<String>();

    queryCols.add(idField.getColumnName());
    String columnName;
    for (PropertyDefinition columnDef : typeDefinition.getProperties()) {
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

    // Model Type.
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(generatedClassName)
        .addJavadoc("AUTO GENERATED CLASS : DO NOT MODIFIED !!!")
        .addModifiers(PUBLIC, FINAL)
        .superclass(ParameterizedTypeName
                        .get(ClassName.get(Model.class), ClassName.get(typeElement)));

    // Field : 'TABLE_NAME'
    FieldSpec field = FieldSpec.builder(ClassName.get(String.class), TABLE_NAME_FIELD)
        .addModifiers(PRIVATE, STATIC, FINAL)
        .initializer("$S", typeDefinition.getTableName())
        .build();
    classBuilder.addField(field);

    // Field : 'ID_COLUMN'
    field = FieldSpec.builder(ClassName.get(String.class), ID_COLUMN_FIELD)
        .addModifiers(PRIVATE, STATIC, FINAL)
        .initializer("$S", idField.getColumnName())
        .build();
    classBuilder.addField(field);

    // Field : 'VERSION_COLUMN'
    if (versionField != null) {
      field = FieldSpec.builder(ClassName.get(String.class), VERSION_COLUMN_FIELD)
          .addModifiers(PRIVATE, STATIC, FINAL)
          .initializer("$S", versionField.getColumnName())
          .build();
      classBuilder.addField(field);
    } else {
      field = FieldSpec.builder(ClassName.get(String.class), VERSION_COLUMN_FIELD)
          .addModifiers(PRIVATE, STATIC, FINAL)
          .initializer("$L", "null")
          .build();
      classBuilder.addField(field);
    }

    // Field : 'QUERY_COLUMNS'
    field = FieldSpec.builder(ArrayTypeName.of(String.class), QUERY_COLUMNS_FIELD)
        .addModifiers(PRIVATE, STATIC, FINAL)
        .initializer(formatStringArray(queryCols.size()), queryCols.toArray())
        .build();
    classBuilder.addField(field);

    // Constructor
    MethodSpec method = MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addStatement("super($L, $L, $L, $L)", CONSTRUCTOR_ARGS)
        .build();
    classBuilder.addMethod(method);

    // Method : 'getId'
    method = MethodSpec.methodBuilder("getId")
        .addModifiers(PROTECTED)
        .addAnnotation(Override.class)
        .returns(TypeName.LONG)
        .addParameter(ClassName.get(typeElement), "entity")
        .addStatement("return entity.$L", idField.getFieldName())
        .build();
    classBuilder.addMethod(method);

    if (versionField != null) {
      // Method : 'getVersion'
      method = MethodSpec.methodBuilder("getVersion")
          .addModifiers(PROTECTED)
          .addAnnotation(Override.class)
          .returns(TypeName.LONG)
          .addParameter(ClassName.get(typeElement), "entity")
          .addStatement("return entity.$L", idField.getFieldName())
          .build();
      classBuilder.addMethod(method);
    }

    // Method : 'onCreate'
    method = MethodSpec.methodBuilder("onCreate")
        .addModifiers(PROTECTED)
        .addAnnotation(Override.class)
        .returns(TypeName.VOID)
        .addParameter(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db")
        .addStatement("db.execSQL($S)", buildSqlStatement(typeDefinition))
        .build();
    classBuilder.addMethod(method);

    // Method : 'newInstance'
    method = MethodSpec.methodBuilder("newInstance")
        .addModifiers(PROTECTED)
        .addAnnotation(Override.class)
        .returns(ClassName.get(typeElement))
        .addStatement("return new $T()", ClassName.get(typeElement))
        .build();
    classBuilder.addMethod(method);

    // Method : 'readEntity'
    method = buildReadCursorMethod(typeElement, typeDefinition, queryCols);
    classBuilder.addMethod(method);

    method = MethodSpec.methodBuilder("getInsertStatement")
        .addModifiers(PROTECTED)
        .addAnnotation(Override.class)
        .returns(ClassName.get(String.class))
        .addStatement("return $S", createSqlInsert(typeDefinition, insertCols))
        .build();
    classBuilder.addMethod(method);
    classBuilder.addMethod(bindInsertMethod(typeElement, typeDefinition, insertCols));

    method = MethodSpec.methodBuilder("getUpdateStatement")
        .addModifiers(PROTECTED)
        .addAnnotation(Override.class)
        .returns(ClassName.get(String.class))
        .addStatement("return $S", createSqlUpdate(typeDefinition, updateCols))
        .build();
    classBuilder.addMethod(method);
    classBuilder.addMethod(bindUpdateMethod(typeElement, typeDefinition, updateCols));

    method = MethodSpec.methodBuilder("getDeleteStatement")
        .addModifiers(PROTECTED)
        .addAnnotation(Override.class)
        .returns(ClassName.get(String.class))
        .addStatement("return $S", createSqlDelete(typeDefinition))
        .build();
    classBuilder.addMethod(method);
    classBuilder.addMethod(bindDeleteMethod(typeElement, typeDefinition));

    // write java file.
    JavaFile.builder(packageName, classBuilder.build())
        .build()
        .writeTo(filer);
  }

  private String buildSqlStatement(TypeDefinition typeDefinition)
      throws IOException {

    StringBuilder builder = new StringBuilder();
    builder.append(typeDefinition.getStatement());
    builder.append("( "); // Open table bracket

    // Not null id column
    builder.append(typeDefinition.getIdColumn().getStatement());
    // Others columns definitions
    for (PropertyDefinition propertyDefinition : typeDefinition.getProperties()) {
      builder.append(", ");
      builder.append(propertyDefinition.getStatement());
    }
    //  Nullable version column
    if (typeDefinition.getVersionColumn() != null) {
      builder.append(", ");
      builder.append(typeDefinition.getVersionColumn().getStatement());
    }
    builder.append(");"); // Table definition end

    for (IndexDefinition index : typeDefinition.getIndexes()) {
      builder.append(' ');
      builder.append(index.getStatement());
      builder.append(";");
    }
    for (TriggerDefinition trigger : typeDefinition.getTriggers()) {
      builder.append(' ');
      builder.append(trigger.getStatement());
      builder.append(";");
    }
    return builder.toString();
  }

  private MethodSpec buildReadCursorMethod(TypeElement typeElement, TypeDefinition definition,
                                           List<String> queryCols) throws IOException {

    PropertyDefinition idColumn = definition.getIdColumn();
    ClassName entityType = ClassName.get(typeElement);
    ClassName typeUtilType = ClassName.get(TypeUtils.class);

    MethodSpec.Builder builder = MethodSpec.methodBuilder("readCursor")
        .addModifiers(PROTECTED)
        .addAnnotation(Override.class)
        .addParameter(entityType, "entity")
        .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
        .addStatement("entity.$L = $T.getLong(cursor, $L)", idColumn.getFieldName(), typeUtilType,
                      queryCols.indexOf(idColumn.getColumnName()));

    PropertyDefinition version = definition.getVersionColumn();
    if (version != null) {
      builder.addStatement("entity.$L = $T.getLong(cursor, $L)", version.getFieldName(),
                           typeUtilType, queryCols.indexOf(version.getColumnName()));
    }

    for (PropertyDefinition propertyDefinition : definition.getProperties()) {
      String columnName = propertyDefinition.getColumnName();
      int index = queryCols.indexOf(columnName);
      if (index == -1) {
        continue;
      }
      String field = propertyDefinition.getFieldName();
      switch (propertyDefinition.getPropertyType()) {
        case BOOLEAN:
          builder.addStatement("entity.$L = $T.getBoolean(cursor, $L)", field, typeUtilType, index);
          break;
        case BYTE:
          builder.addStatement("entity.$L = $T.getByte(cursor, $L)", field, typeUtilType, index);
          break;
        case BYTE_ARRAY:
          builder.addStatement("entity.$L = $T.getByteArray(cursor, $L)", field, typeUtilType,
                               index);
          break;
        case CHARACTER:
          builder.addStatement("entity.$L = $T.getChar(cursor, $L)", field, typeUtilType, index);
          break;
        case DATE_LONG:
          builder.addStatement("entity.$L = $T.getDate(cursor, $L)", field, typeUtilType, index);
          break;
        case DATE_STRING:
          builder.addStatement("entity.$L = $T.getDate(cursor, $L, $S)", field, typeUtilType,
                               index, propertyDefinition.getDateFormat());
          break;
        case DOUBLE:
          builder.addStatement("entity.$L = $T.getDouble(cursor, $L)", field, typeUtilType,
                               index);
          break;
        case FLOAT:
          builder.addStatement("entity.$L = $T.getFloat(cursor, $L)", field, typeUtilType,
                               index);
          break;
        case INTEGER:
          builder.addStatement("entity.$L = $T.getInt(cursor, $L)", field, typeUtilType, index);
          break;
        case LONG:
          builder.addStatement("entity.$L = $T.getLong(cursor, $L)", field, typeUtilType, index);
          break;
        case SHORT:
          builder.addStatement("entity.$L = $T.getShort(cursor, $L)", field, typeUtilType,
                               index);
          break;
        case STRING:
          builder.addStatement("entity.$L = $T.getString(cursor, $L)", field, typeUtilType,
                               index);
          break;
        default:
          throw new IllegalStateException("Illegal type " + propertyDefinition.getPropertyType());
      }
    }
    return builder.build();
  }

  private MethodSpec bindInsertMethod(TypeElement element, TypeDefinition definition,
                                      List<String> columns) throws IOException {
    return createBindMethod("bindInsert", element, definition, columns).build();
  }

  private MethodSpec bindUpdateMethod(TypeElement typeElement, TypeDefinition definition,
                                      List<String> columnNames) throws IOException {

    ClassName typeUtilType = ClassName.get(TypeUtils.class);
    MethodSpec.Builder builder =
        createBindMethod("bindUpdate", typeElement, definition, columnNames);
    // Bind id argument.
    builder.addStatement("$T.bind(statement, entity.$L, $L)", typeUtilType,
                         definition.getIdColumn().getFieldName(), columnNames.size());
    // Bind Version argument if not null.
    if (definition.getVersionColumn() != null) {
      builder.addStatement("$T.bind(statement, entity.$L, $L)", typeUtilType,
                           definition.getVersionColumn().getFieldName(), columnNames.size());
    }
    return builder.build();
  }

  private MethodSpec bindDeleteMethod(TypeElement element, TypeDefinition definition)
      throws IOException {

    ClassName typeUtilType = ClassName.get(TypeUtils.class);
    MethodSpec.Builder builder =
        createBindMethod("bindDelete", element, definition, null);
    // Bind id argument.
    builder.addStatement("$T.bind(statement, entity.$L, 0)", typeUtilType,
                         definition.getIdColumn().getFieldName());
    if (definition.getVersionColumn() != null) {
      builder.addStatement("$T.bind(statement, entity.$L, 1)", typeUtilType,
                           definition.getVersionColumn().getFieldName());
    }
    return builder.build();
  }

  private MethodSpec.Builder createBindMethod(String methodName, TypeElement element,
                                              TypeDefinition definition,
                                              List<String> columnNames) throws IOException {

    MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
        .addModifiers(PROTECTED)
        .addAnnotation(Override.class)
        .returns(TypeName.VOID)
        .addParameter(ClassName.get("android.database.sqlite", "SQLiteStatement"), "statement")
        .addParameter(ClassName.get(element), "entity");

    ClassName typeUtilType = ClassName.get(TypeUtils.class);
    if (columnNames != null) {
      for (PropertyDefinition property : definition.getProperties()) {
        int index = columnNames.indexOf(property.getColumnName()) + 1;
        if (index < 0) {
          continue;
        }
        builder.addStatement("$T.bind(statement, entity.$L, $L)", typeUtilType,
                             property.getFieldName(), index);
      }
    }
    return builder;
  }
}
