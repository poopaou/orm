package org.medimob.orm.processor;

import org.medimob.orm.annotation.Check;
import org.medimob.orm.annotation.Column;
import org.medimob.orm.annotation.Entity;
import org.medimob.orm.annotation.Id;
import org.medimob.orm.annotation.Index;
import org.medimob.orm.annotation.NotNull;
import org.medimob.orm.annotation.Table;
import org.medimob.orm.annotation.Trigger;
import org.medimob.orm.annotation.Unique;
import org.medimob.orm.annotation.Version;
import org.medimob.orm.processor.dll.ConstraintDefinitionBuilder;
import org.medimob.orm.processor.dll.Constraints;
import org.medimob.orm.processor.dll.IndexDefinitionBuilder;
import org.medimob.orm.processor.dll.TriggerDefinitionBuilder;
import org.medimob.orm.processor.dll.TypeDefinitionBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import static org.medimob.orm.processor.ProcessorUtils.getNoArgsConstructor;
import static org.medimob.orm.processor.ProcessorUtils.resolveIndexName;
import static org.medimob.orm.processor.ProcessorUtils.resolveTableName;
import static org.medimob.orm.processor.ProcessorUtils.resolveUniqueName;

/**
 * Entity annotation processor.
 *
 * Created by Poopaou on 16/01/2015.
 */
public class EntityProcessor extends AbstractProcessor {

  public static final String CLASS_MODEL_SUFFIX = "$$Model";
  public static final String ENTITY_LIST_FILE_PATH = "META-INF/com.medimob.Entity";

  @SuppressWarnings("unchecked" )
  private static final List<Class<? extends Annotation>> SUPPORTED_ANNOTATION = Arrays.asList(
      Check.class,
      Column.class,
      Id.class,
      Index.class,
      NotNull.class,
      Table.class,
      Trigger.class,
      Unique.class,
      Version.class
  );

  private Elements elementUtils;
  private PropertyProcessor propertyProcessor;
  private List<String> proceededTypeMap;
  private TypeWriter typeWriter;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    this.elementUtils = processingEnv.getElementUtils();
    this.propertyProcessor = new PropertyProcessor(processingEnv);
    proceededTypeMap = new ArrayList<String>();
    typeWriter = new TypeWriter(processingEnv.getFiler());
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    HashSet<String> supportTypes = new HashSet<String>();
    supportTypes.add(Entity.class.getCanonicalName());
    for (Class<? extends Annotation> c : SUPPORTED_ANNOTATION) {
      supportTypes.add(c.getCanonicalName());
    }
    return supportTypes;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(Entity.class)) {
      try {
        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.NOTE, "Processing class", element);

        TypeElement typeElement = (TypeElement) element;
        // If the class as already be proceeded skip execution.
        final String typeQualifiedName = typeElement.getQualifiedName().toString();

        // Validate class Access :
        // - A least Package protected.
        // - With a no arg constructor.
        // - not a inner class.
        validateClassElement(typeElement);

        final String tableName =
            resolveTableName(typeElement, typeElement.getAnnotation(Entity.class));
        final PackageElement packageElement = elementUtils.getPackageOf(element);

        TypeDefinitionBuilder tableBuilder = new TypeDefinitionBuilder()
            .setTableName(tableName)
            .setTypeQualifiedName(typeQualifiedName)
            .setTypeSimpleName(element.getSimpleName().toString())
            .setPackageName(packageElement.getQualifiedName().toString());

        // Map columns.
        List<? extends Element> members = elementUtils.getAllMembers(typeElement);
        for (VariableElement field : ElementFilter.fieldsIn(members)) {
          propertyProcessor.process(field, tableBuilder, tableName);
        }

        processTable(tableBuilder, tableName, element.getAnnotation(Table.class));

        typeWriter.writeType(tableBuilder.build());
        proceededTypeMap.add(typeQualifiedName);

      } catch (MappingException e) {
        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
      } catch (IOException e) {
        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
      }
    }

    if (roundEnv.processingOver()) {
      try {
        FileObject fo = processingEnv.getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", ENTITY_LIST_FILE_PATH);
        BufferedWriter bufferedWriter = new BufferedWriter(fo.openWriter());
        for (String type : proceededTypeMap) {
          bufferedWriter.append(type);
          bufferedWriter.newLine();
        }
        bufferedWriter.close();
      } catch (IOException e) {
        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.ERROR, e.getMessage());
      }
    }
    return true;
  }

  private void validateClassElement(TypeElement element) throws MappingException {
    if (!element.getKind().equals(ElementKind.CLASS)) {
      throw new MappingException("The @Entity annotation can only be applied to classes"
                                 + element.getSimpleName());
    }
    if (element.getModifiers().contains(Modifier.PRIVATE)) {
      throw new MappingException("The @Entity annotation class cannot be private "
                                 + element.getSimpleName());
    }
    // Check if class is not a nested class.
    Element enclosingElement = element.getEnclosingElement();
    if (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
      throw new MappingException("The @Entity annotation class does not support nested classes"
                                 + element.getSimpleName());
    }

    if (getNoArgsConstructor(element) == null) {
      throw new MappingException("The @Entity annotated class must provide a no arg constructor"
                                 + element.getSimpleName());
    }
  }

  protected void processTable(TypeDefinitionBuilder tableBuilder, String tableName, Table table)
      throws MappingException {
    if (table != null) {
      tableBuilder.setTemporary(table.temp());
      processChecksConstraints(tableBuilder, table.checks());
      processIndexes(tableBuilder, tableName, table.indexes());
      processTriggers(tableBuilder, tableName, table.triggers());
      processUniqueConstraints(tableBuilder, table.uniques());
    }
  }

  protected void processUniqueConstraints(TypeDefinitionBuilder tableBuilder, Unique[] uniques)
      throws MappingException {
    if (uniques != null) {
      for (Unique unique : uniques) {
        tableBuilder.addConstraint(ConstraintDefinitionBuilder.newTableConstraint()
                                       .setType(Constraints.UNIQUE)
                                       .setName(resolveUniqueName(unique, unique.columns()))
                                       .setConflictClause(unique.onConflict())
                                       .build());
      }
    }
  }

  protected void processTriggers(TypeDefinitionBuilder tableBuilder, String tableName,
                                 Trigger[] triggers) throws MappingException {
    if (triggers != null) {
      for (Trigger trigger : triggers) {
        tableBuilder.addTrigger(new TriggerDefinitionBuilder()
                                    .setName(trigger.name())
                                    .setTableName(tableName)
                                    .setStatements(trigger.statements())
                                    .setForEachRow(trigger.forEach())
                                    .setTemporary(trigger.temp())
                                    .setTriggerType(trigger.type())
                                    .setWhen(trigger.when())
                                    .build());
      }
    }
  }

  protected void processIndexes(TypeDefinitionBuilder tableBuilder, String tableName,
                                Index[] indexes) throws MappingException {
    if (indexes != null) {
      for (Index index : indexes) {
        tableBuilder.addIndex(new IndexDefinitionBuilder()
                                  .setName(resolveIndexName(index))
                                  .setTableName(tableName)
                                  .setColumns(index.columns())
                                  .setUnique(index.unique())
                                  .setWhere(index.where())
                                  .build());
      }
    }
  }

  protected void processChecksConstraints(TypeDefinitionBuilder tableBuilder, Check[] checks)
      throws MappingException {
    if (checks != null) {
      for (Check check : checks) {
        tableBuilder.addConstraint(ConstraintDefinitionBuilder.newTableConstraint()
                                       .setType(Constraints.CHECK)
                                       .setName(check.name())
                                       .setExp(check.exp())
                                       .build());
      }
    }
  }
}
