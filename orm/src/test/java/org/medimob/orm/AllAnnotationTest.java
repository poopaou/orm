package org.medimob.orm;

import com.google.common.io.Files;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.medimob.orm.annotation.Column;
import org.medimob.orm.annotation.DateField;
import org.medimob.orm.annotation.Entity;
import org.medimob.orm.annotation.Id;
import org.medimob.orm.annotation.Version;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.medimob.orm.ProcessorTestUtilities.OrmProcessors;

@Entity
public class AllAnnotationTest {

  ////////////////////////////
  @Id
  long id;
  ////////////////////////////
  @Column
  Boolean objectBoolean;
  @Column
  boolean primitiveBoolean;
  ////////////////////////////
  @Column
  Character objectCharacter;
  @Column
  char primitiveCharacter;
  ////////////////////////////
  @Column
  Byte objectByte;
  @Column
  byte primitiveByte;
  ////////////////////////////
  @Column
  Short objectShort;
  @Column
  short primitiveShort;
  ////////////////////////////
  @Column
  Integer objectInt;
  @Column
  int primitiveInt;
  ////////////////////////////
  @Column
  Long objectLong;
  @Column
  long primitiveLong;
  ////////////////////////////
  @Column
  Float objectFloat;
  @Column
  float primitiveFloat;
  ////////////////////////////
  @Column
  Double objectDouble;
  @Column
  double primitiveDouble;
  ////////////////////////////
  @Column
  String string;
  ////////////////////////////
  @Column(dateType = DateField.DATE_STRING)
  Date stringDate;
  @Column(dateType = DateField.DATE_LONG)
  Date longDate;
  ////////////////////////////
  @Column
  byte[] bytesArray;
  ////////////////////////////
  @Version
  long version;

  @Test
  public void allAnnotation() throws IOException {
    // !! Be careful with the working direct when running test !!
    // This test should be run from this module directory.
    File file = new File("src/test/java/org/medimob/orm/AllAnnotationTest.java");
    String content = Files.toString(file, StandardCharsets.UTF_8);

    ASSERT.about(javaSource())
        .that(JavaFileObjects.forSourceString("org.medimob.orm.AllAnnotationTest", content))
        .processedWith(OrmProcessors())
        .compilesWithoutError();
  }
}
