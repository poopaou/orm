package org.medimob.orm.processor;

import org.medimob.orm.annotation.*;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor6;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * Created by Poopaou on 19/01/2015.
 */
public final class ProcessorUtils {

    private ProcessorUtils() {}

    public static String resolveTableName(TypeElement element, Entity entity) {
        final String tableName;
        if (entity.name().equals("")){
            tableName = element.getSimpleName().toString();
        } else {
            tableName = entity.name().toUpperCase();
        }
        return tableName;
    }

    public static String resolveColumnName(VariableElement element, Column column) {
        final String columnName;
        String property = element.getSimpleName().toString();
        if ("".equals(column.name())){
            columnName = property.toUpperCase();
        } else {
            columnName = column.name().toUpperCase();
        }
        return columnName;
    }

    public static String resolveUniqueName(Unique index, String... columns){
        return resolveName(index.name(), columns);
    }

    public static String resolveIndexName(Index index, String... columns){
        return resolveName(index.name(), columns);
    }

    private static String resolveName(String name, String[] columns) {
        if ("".equals(name)){
            StringBuilder buffer = new StringBuilder();
            for(String col : columns){
                buffer.append('_');
                buffer.append(col.toUpperCase());
            }
            return buffer.toString();
        }
        return name;
    }

    // Checks if element is accessible from the package level.
    public static boolean isAccessibleMethod(Element executableElement){
        final Set<Modifier> modifiers = executableElement.getModifiers();
        return  !modifiers.contains(Modifier.STATIC)
                || !modifiers.contains(Modifier.ABSTRACT)
                || !modifiers.contains(Modifier.PRIVATE);
    }

    // Checks if annotation is present on the class element.
    public static <A extends Annotation> boolean hasAnnotation(Element element,
                                                                Class<A> annotation){
        return element.getAnnotation(annotation) != null;
    }

    public static PackageElement getPackage(Element type) {
        while (type.getKind() != ElementKind.PACKAGE) {
            type = type.getEnclosingElement();
        }
        return (PackageElement) type;
    }

    /**
     * Returns the supertype, or {@code null} if the supertype is a platform
     * class. This is intended for annotation processors that assume platform
     * classes will never be annotated with application annotations.
     *//*
    public static TypeMirror getApplicationSupertype(TypeElement type) {
        TypeMirror supertype = type.getSuperclass();
        return Keys.isPlatformType(supertype.toString()) ? null : supertype;
    }*/

    /** Returns a fully qualified class name to complement {@code type}. */
    public static String adapterName(TypeElement typeElement, String suffix) {
        StringBuilder builder = new StringBuilder();
        rawTypeToString(builder, typeElement, '$');
        builder.append(suffix);
        return builder.toString();
    }

    /** Returns a string for {@code type}. Primitive types are always boxed. */
    public static String typeToString(TypeMirror type) {
        StringBuilder result = new StringBuilder();
        typeToString(type, result, '.');
        return result.toString();
    }

    /** Returns a string for the raw type of {@code type}. Primitive types are always boxed. */
    public static String rawTypeToString(TypeMirror type, char innerClassSeparator) {
        if (!(type instanceof DeclaredType)) {
            throw new IllegalArgumentException("Unexpected type: " + type);
        }
        StringBuilder result = new StringBuilder();
        DeclaredType declaredType = (DeclaredType) type;
        rawTypeToString(result, (TypeElement) declaredType.asElement(), innerClassSeparator);
        return result.toString();
    }

    /**
     * Appends a string for {@code type} to {@code result}. Primitive types are
     * always boxed.
     *
     * @param innerClassSeparator either '.' or '$', which will appear in a
     *     class name like "java.lang.Map.Entry" or "java.lang.Map$Entry".
     *     Use '.' for references to existing types in code. Use '$' to define new
     *     class names and for strings that will be used by runtime reflection.
     */
    public static void typeToString(final TypeMirror type, final StringBuilder result,
                                    final char innerClassSeparator) {
        type.accept(new SimpleTypeVisitor6<Void, Void>() {
            @Override public Void visitDeclared(DeclaredType declaredType, Void v) {
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
            @Override public Void visitPrimitive(PrimitiveType primitiveType, Void v) {
                result.append(box((PrimitiveType) type).getName());
                return null;
            }
            @Override public Void visitArray(ArrayType arrayType, Void v) {
                TypeMirror type = arrayType.getComponentType();
                if (type instanceof PrimitiveType) {
                    result.append(type.toString()); // Don't box, since this is an array.
                } else {
                    typeToString(arrayType.getComponentType(), result, innerClassSeparator);
                }
                result.append("[]");
                return null;
            }
            @Override public Void visitTypeVariable(TypeVariable typeVariable, Void v) {
                result.append(typeVariable.asElement().getSimpleName());
                return null;
            }
            @Override public Void visitError(ErrorType errorType, Void v) {
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
            @Override protected Void defaultAction(TypeMirror typeMirror, Void v) {
                throw new UnsupportedOperationException(
                        "Unexpected TypeKind " + typeMirror.getKind() + " for "  + typeMirror);
            }
        }, null);
    }

    /*private static final AnnotationValueVisitor<Object, Void> VALUE_EXTRACTOR =
            new SimpleAnnotationValueVisitor6<Object, Void>() {
                @Override public Object visitString(String s, Void p) {
                    if ("<error>".equals(s)) {
                        throw new CodeGenerationIncompleteException("Unknown type returned as <error>.");
                    } else if ("<any>".equals(s)) {
                        throw new CodeGenerationIncompleteException("Unknown type returned as <any>.");
                    }
                    return s;
                }
                @Override public Object visitType(TypeMirror t, Void p) {
                    return t;
                }
                @Override protected Object defaultAction(Object o, Void v) {
                    return o;
                }
                @Override public Object visitArray(List<? extends AnnotationValue> values, Void v) {
                    Object[] result = new Object[values.size()];
                    for (int i = 0; i < values.size(); i++) {
                        result[i] = values.get(i).accept(this, null);
                    }
                    return result;
                }
            };*/

    /**
     * Returns the annotation on {@code element} formatted as a Map. This returns
     * a Map rather than an instance of the annotation interface to work-around
     * the fact that Class and Class[] fields won't work at code generation time.
     * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5089128
     */
   /* public static Map<String, Object> getAnnotation(Class<?> annotationType, Element element) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (!rawTypeToString(annotation.getAnnotationType(), '$')
                    .equals(annotationType.getName())) {
                continue;
            }

            Map<String, Object> result = new LinkedHashMap<String, Object>();
            for (Method m : annotationType.getMethods()) {
                result.put(m.getName(), m.getDefaultValue());
            }
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e
                    : annotation.getElementValues().entrySet()) {
                String name = e.getKey().getSimpleName().toString();
                Object value = e.getValue().accept(VALUE_EXTRACTOR, null);
                Object defaultValue = result.get(name);
                if (!lenientIsInstance(defaultValue.getClass(), value)) {
                    throw new IllegalStateException(String.format(
                            "Value of %s.%s is a %s but expected a %s\n    value: %s",
                            annotationType, name, value.getClass().getName(), defaultValue.getClass().getName(),
                            value instanceof Object[] ? Arrays.toString((Object[]) value) : value));
                }
                result.put(name, value);
            }
            return result;
        }
        return null; // Annotation not found.
    }*/

    /**
     * Returns true if {@code value} can be assigned to {@code expectedClass}.
     * Like {@link Class#isInstance} but more lenient for {@code Class<?>} values.
     */
    private static boolean lenientIsInstance(Class<?> expectedClass, Object value) {
        if (expectedClass.isArray()) {
            Class<?> componentType = expectedClass.getComponentType();
            if (!(value instanceof Object[])) {
                return false;
            }
            for (Object element : (Object[]) value) {
                if (!lenientIsInstance(componentType, element)) return false;
            }
            return true;
        } else if (expectedClass == Class.class) {
            return value instanceof TypeMirror;
        } else {
            return expectedClass == value.getClass();
        }
    }

    // TODO(sgoldfed): better format for other types of elements?
    static String elementToString(Element element) {
        switch (element.getKind()) {
            case FIELD:
                // fall through
            case CONSTRUCTOR:
                // fall through
            case METHOD:
                return element.getEnclosingElement() + "." + element;
            default:
                return element.toString();
        }
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
     * Returns the no-args constructor for {@code type}, or null if no such
     * constructor exists.
     */
    public static ExecutableElement getNoArgsConstructor(TypeElement type) {
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

    /**
     * Returns true if generated code can invoke {@code constructor}. That is, if
     * the constructor is non-private and its enclosing class is either a
     * top-level class or a static nested class.
     */
    public static boolean isCallableConstructor(ExecutableElement constructor) {
        if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
            return false;
        }
        TypeElement type = (TypeElement) constructor.getEnclosingElement();
        return type.getEnclosingElement().getKind() == ElementKind.PACKAGE
                || type.getModifiers().contains(Modifier.STATIC);
    }


    /**
     * Returns a user-presentable string like {@code coffee.CoffeeModule}.
     */
    public static String className(ExecutableElement method) {
        return ((TypeElement) method.getEnclosingElement()).getQualifiedName().toString();
    }

    public static boolean isInterface(TypeMirror typeMirror) {
        return typeMirror instanceof DeclaredType
                && ((DeclaredType) typeMirror).asElement().getKind() == ElementKind.INTERFACE;
    }

    static boolean isStatic(Element element) {
        for (Modifier modifier : element.getModifiers()) {
            if (modifier.equals(Modifier.STATIC)) {
                return true;
            }
        }
        return false;
    }

    final static class CodeGenerationIncompleteException extends IllegalStateException {
        public CodeGenerationIncompleteException(String s) {
            super(s);
        }
    }
}
