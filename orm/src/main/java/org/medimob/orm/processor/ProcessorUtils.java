package org.medimob.orm.processor;

import android.support.annotation.NonNull;

import org.medimob.orm.annotation.Index;
import org.medimob.orm.annotation.Model;
import org.medimob.orm.annotation.Property;
import org.medimob.orm.annotation.Unique;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * Commons processor utils.
 * Created by Poopaou on 19/01/2015.
 */
public final class ProcessorUtils {

  private ProcessorUtils() {
  }

  /**
   * Revolve table name.
   *
   * @param element type.
   * @param model   entity.
   * @return table's name.
   */
  public static String resolveTableName(@NonNull TypeElement element, @NonNull Model model) {
    final String tableName;
    if ("".equals(model.name())) {
      tableName = element.getSimpleName().toString();
    } else {
      tableName = model.name().toUpperCase();
    }
    return tableName;
  }

  /**
   * Resolve column name.
   *
   * @param element field.
   * @param column  column.
   * @return column's name.
   */
  public static String resolveColumnName(@NonNull VariableElement element,
                                         @NonNull Property column) {
    final String columnName;
    String property = element.getSimpleName().toString();
    if ("".equals(column.name())) {
      columnName = property.toUpperCase();
    } else {
      columnName = column.name().toUpperCase();
    }
    return columnName;
  }

  /**
   * Revolve unique constraint name.
   *
   * @param index   annotation.
   * @param columns columns.
   * @return unique name.
   */
  public static String resolveUniqueName(@NonNull Unique index, @NonNull String... columns) {
    return resolveName(index.name(), columns);
  }

  /**
   * Revolve index constraint name.
   *
   * @param index   annotation.
   * @param columns columns.
   * @return unique name.
   */
  public static String resolveIndexName(@NonNull Index index, @NonNull String... columns) {
    return resolveName(index.name(), columns);
  }

  private static String resolveName(String name, String[] columns) {
    if ("".equals(name)) {
      StringBuilder buffer = new StringBuilder();
      for (String col : columns) {
        buffer.append('_');
        buffer.append(col.toUpperCase());
      }
      return buffer.toString();
    }
    return name;
  }

  /**
   * Gets package form element.
   *
   * @param type element
   * @return package.
   */
  public static PackageElement getPackage(@NonNull Element type) {
    while (type.getKind() != ElementKind.PACKAGE) {
      type = type.getEnclosingElement();
    }
    return (PackageElement) type;
  }

  /**
   * Returns a string for {@code type}. Primitive types are always boxed.
   */
  public static String typeToString(@NonNull TypeMirror type) {
    StringBuilder result = new StringBuilder();
    typeToString(type, result, '.');
    return result.toString();
  }

  /**
   * Appends a string for {@code type} to {@code result}. Primitive types are always boxed.
   *
   * @param innerClassSeparator either '.' or '$', which will appear in a class name like
   *                            "java.lang.Map.Entry" or "java.lang.Map$Entry". Use '.' for
   *                            references to existing types in code. Use '$' to define new class
   *                            names and for strings that will be used by runtime reflection.
   */
  public static void typeToString(final TypeMirror typeMirror, final StringBuilder result,
                                  final char innerClassSeparator) {
    typeMirror.accept(new SimpleTypeVisitor6<Void, Void>() {
      @Override
      public Void visitDeclared(DeclaredType declaredType, Void type) {
        TypeElement typeElement = (TypeElement) declaredType.asElement();
        rawTypeToString(result, typeElement, innerClassSeparator);
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (!typeArguments.isEmpty()) {
          result.append("<");
          for (int i = 0; i < typeArguments.size(); i++) {
            if (i != 0) {
              result.append(", ");
            }
            typeToString(typeArguments.get(i), result, innerClassSeparator);
          }
          result.append(">");
        }
        return null;
      }

      @Override
      public Void visitPrimitive(PrimitiveType primitiveType, Void type) {
        result.append(box((PrimitiveType) typeMirror).getName());
        return null;
      }

      @Override
      public Void visitArray(ArrayType arrayType, Void type) {
        TypeMirror typeMirror = arrayType.getComponentType();
        if (typeMirror instanceof PrimitiveType) {
          result.append(typeMirror.toString()); // Don't box, since this is an array.
        } else {
          typeToString(arrayType.getComponentType(), result, innerClassSeparator);
        }
        result.append("[]");
        return null;
      }

      @Override
      public Void visitTypeVariable(TypeVariable typeVariable, Void type) {
        result.append(typeVariable.asElement().getSimpleName());
        return null;
      }

      @Override
      public Void visitError(ErrorType errorType, Void type) {
        // Error type found, a type may not yet have been generated, but we need the type
        // so we can generate the correct code in anticipation of the type being available
        // to the compiler.

        // Paramterized types which don't exist are returned as an error type whose name is "<any>"
        if ("<any>".equals(errorType.toString())) {
          throw new CodeGenerationIncompleteException(
              "Type reported as <any> is likely a not-yet generated parameterized type.");
        }
        // TODO(cgruber): Figure out a strategy for non-FQCN cases.
        result.append(errorType.toString());
        return null;
      }

      @Override
      protected Void defaultAction(TypeMirror typeMirror, Void type) {
        throw new UnsupportedOperationException(
            "Unexpected TypeKind " + typeMirror.getKind() + " for " + typeMirror);
      }
    }, null);
  }

  static void rawTypeToString(StringBuilder result, TypeElement type,
                              char innerClassSeparator) {
    String packageName = getPackage(type).getQualifiedName().toString();
    String qualifiedName = type.getQualifiedName().toString();
    if (packageName.isEmpty()) {
      result.append(qualifiedName.replace('.', innerClassSeparator));
    } else {
      result.append(packageName);
      result.append('.');
      result.append(
          qualifiedName.substring(packageName.length() + 1).replace('.', innerClassSeparator));
    }
  }

  private static Class<?> box(PrimitiveType primitiveType) {
    switch (primitiveType.getKind()) {
      case BYTE:
        return Byte.class;
      case SHORT:
        return Short.class;
      case INT:
        return Integer.class;
      case LONG:
        return Long.class;
      case FLOAT:
        return Float.class;
      case DOUBLE:
        return Double.class;
      case BOOLEAN:
        return Boolean.class;
      case CHAR:
        return Character.class;
      case VOID:
        return Void.class;
      default:
        throw new AssertionError();
    }
  }

  /**
   * Returns the no-args constructor for {@code type}, or null if no such constructor exists.
   */
  public static ExecutableElement getNoArgsConstructor(@NonNull TypeElement type) {
    for (Element enclosed : type.getEnclosedElements()) {
      if (enclosed.getKind() != ElementKind.CONSTRUCTOR) {
        continue;
      }
      ExecutableElement constructor = (ExecutableElement) enclosed;
      if (constructor.getParameters().isEmpty()) {
        return constructor;
      }
    }
    return null;
  }

  static final class CodeGenerationIncompleteException extends IllegalStateException {

    private static final long serialVersionUID = -4929894043984842443L;

    public CodeGenerationIncompleteException(String message) {
      super(message);
    }
  }
}
