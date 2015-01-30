package org.medimob.orm;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class OrmTest {

  private Orm orm;

  @Before
  public void setUp() {
    orm = Orm.getInstance(Robolectric.application);
  }

  @Test
  public void ormInstanceTest() {
    Orm orm1 = Orm.getInstance(Robolectric.application);
    Orm orm2 = Orm.getInstance(Robolectric.application);
    assertThat(orm1).isEqualTo(orm2);
  }

  @Test
  public void insertTest() {
    AllAnnotationTest test = new AllAnnotationTest();
    long id = orm.insert(AllAnnotationTest.class, test);

    assertThat(id).isGreaterThan(0);

    test = orm.loadById(AllAnnotationTest.class, id);
    assertThat(test).isNotNull();
    assertThat(id).isEqualTo(test.id);

    AllAnnotationTest test2 = orm.loadById(AllAnnotationTest.class, id);
    assertThat(test2).isEqualTo(test);
  }

  @Test
  public void insertFieldTest() {
    AllAnnotationTest inserted = new AllAnnotationTest();
    inserted.objectBoolean = true;
    inserted.primitiveBoolean = false;
    inserted.objectCharacter = 'a';
    inserted.primitiveCharacter = 'b';
    inserted.objectByte = 1;
    inserted.primitiveByte = 2;
    inserted.objectShort = 1;
    inserted.primitiveShort = 2;
    inserted.objectInt = 1;
    inserted.primitiveInt = 2;
    inserted.objectLong = 1l;
    inserted.primitiveLong = 2l;
    inserted.objectFloat = 1.0f;
    inserted.primitiveFloat = 2.0f;
    inserted.objectDouble = 1d;
    inserted.primitiveDouble = 2d;
    inserted.string = "test";
    inserted.stringDate = new Date();
    inserted.longDate = new Date();
    inserted.bytesArray = new byte[]{1, 2, 3};
    long id = orm.insert(AllAnnotationTest.class, inserted);

    assertThat(id).isGreaterThan(0);
    AllAnnotationTest queried = orm.loadById(AllAnnotationTest.class, id);
    assertThat(inserted).isNotNull();
    assertThat(id).isEqualTo(queried.id);

    assertThat(inserted.objectBoolean).isEqualTo(queried.objectBoolean);
    assertThat(inserted.primitiveBoolean).isEqualTo(queried.primitiveBoolean);
    assertThat(inserted.objectCharacter).isEqualTo(queried.objectCharacter);
    assertThat(inserted.primitiveCharacter).isEqualTo(queried.primitiveCharacter);
    assertThat(inserted.objectByte).isEqualTo(queried.objectByte);
    assertThat(inserted.primitiveByte).isEqualTo(queried.primitiveByte);
    assertThat(inserted.objectShort).isEqualTo(queried.objectShort);
    assertThat(inserted.primitiveShort).isEqualTo(queried.primitiveShort);
    assertThat(inserted.objectInt).isEqualTo(queried.objectInt);
    assertThat(inserted.primitiveInt).isEqualTo(queried.primitiveInt);
    assertThat(inserted.objectLong).isEqualTo(queried.objectLong);
    assertThat(inserted.primitiveLong).isEqualTo(queried.primitiveLong);
    assertThat(inserted.objectFloat).isEqualTo(queried.objectFloat);
    assertThat(inserted.primitiveFloat).isEqualTo(queried.primitiveFloat);
    assertThat(inserted.objectDouble).isEqualTo(queried.objectDouble);
    assertThat(inserted.primitiveDouble).isEqualTo(queried.primitiveDouble);
    assertThat(inserted.string).isEqualTo(queried.string);
    assertThat(inserted.stringDate).isEqualTo(queried.stringDate);
    assertThat(inserted.longDate).isEqualTo(queried.longDate);
    assertThat(inserted.bytesArray).isEqualTo(queried.bytesArray);
  }

  @Test
  public void insertConflictTest() {
    AllAnnotationTest inserted = new AllAnnotationTest();
    inserted.id = 1;
    try {
      orm.insert(AllAnnotationTest.class, inserted);
      Assertions.failBecauseExceptionWasNotThrown(OrmException.class);
    } catch (Exception e) {
      assertThat(e instanceof OrmException).isTrue();
    }
  }

}
