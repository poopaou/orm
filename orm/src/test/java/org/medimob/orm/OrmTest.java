package org.medimob.orm;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.medimob.orm.models.AllTypeTest;
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
    AllTypeTest test = new AllTypeTest();
    long id = orm.insert(AllTypeTest.class, test);

    assertThat(id).isGreaterThan(0);

    test = orm.loadById(AllTypeTest.class, id);
    assertThat(test).isNotNull();
    assertThat(id).isEqualTo(test.getId());

    AllTypeTest test2 = orm.loadById(AllTypeTest.class, id);
    assertThat(test2).isEqualTo(test);
  }

  @Test
  public void insertFieldTest() {
    AllTypeTest inserted = new AllTypeTest();
    inserted.setObjectBoolean(true);
    inserted.setPrimitiveBoolean(false);
    inserted.setObjectCharacter('a');
    inserted.setPrimitiveCharacter('b');
    inserted.setObjectByte((byte) 1);
    inserted.setPrimitiveByte((byte) 2);
    inserted.setObjectShort((short) 1);
    inserted.setPrimitiveShort((short) 2);
    inserted.setObjectInt(1);
    inserted.setPrimitiveInt(2);
    inserted.setObjectLong(1l);
    inserted.setPrimitiveLong(2l);
    inserted.setObjectFloat(1.0f);
    inserted.setPrimitiveFloat(2.0f);
    inserted.setObjectDouble(1d);
    inserted.setPrimitiveDouble(2d);
    inserted.setString("test");
    inserted.setStringDate(new Date());
    inserted.setLongDate(new Date());
    inserted.setBytesArray(new byte[]{1, 2, 3});
    long id = orm.insert(AllTypeTest.class, inserted);

    assertThat(id).isGreaterThan(0);
    AllTypeTest queried = orm.loadById(AllTypeTest.class, id);
    assertThat(inserted).isNotNull();
    assertThat(id).isEqualTo(queried.getId());

    assertThat(inserted.getObjectBoolean()).isEqualTo(queried.getObjectBoolean());
    assertThat(inserted.isPrimitiveBoolean()).isEqualTo(queried.isPrimitiveBoolean());
    assertThat(inserted.getObjectCharacter()).isEqualTo(queried.getObjectCharacter());
    assertThat(inserted.getPrimitiveCharacter()).isEqualTo(queried.getPrimitiveCharacter());
    assertThat(inserted.getObjectByte()).isEqualTo(queried.getObjectByte());
    assertThat(inserted.getPrimitiveByte()).isEqualTo(queried.getPrimitiveByte());
    assertThat(inserted.getObjectShort()).isEqualTo(queried.getObjectShort());
    assertThat(inserted.getPrimitiveShort()).isEqualTo(queried.getPrimitiveShort());
    assertThat(inserted.getObjectInt()).isEqualTo(queried.getObjectInt());
    assertThat(inserted.getPrimitiveInt()).isEqualTo(queried.getPrimitiveInt());
    assertThat(inserted.getObjectLong()).isEqualTo(queried.getObjectLong());
    assertThat(inserted.getPrimitiveLong()).isEqualTo(queried.getPrimitiveLong());
    assertThat(inserted.getObjectFloat()).isEqualTo(queried.getObjectFloat());
    assertThat(inserted.getPrimitiveFloat()).isEqualTo(queried.getPrimitiveFloat());
    assertThat(inserted.getObjectDouble()).isEqualTo(queried.getObjectDouble());
    assertThat(inserted.getPrimitiveDouble()).isEqualTo(queried.getPrimitiveDouble());
    assertThat(inserted.getString()).isEqualTo(queried.getString());
    assertThat(inserted.getStringDate()).isEqualTo(queried.getStringDate());
    assertThat(inserted.getLongDate()).isEqualTo(queried.getLongDate());
    assertThat(inserted.getBytesArray()).isEqualTo(queried.getBytesArray());
  }

  @Test
  public void insertConflictTest() {
    AllTypeTest inserted = new AllTypeTest();
    inserted.setId(1);
    try {
      orm.insert(AllTypeTest.class, inserted);
      Assertions.failBecauseExceptionWasNotThrown(OrmException.class);
    } catch (Exception e) {
      assertThat(e instanceof OrmException).isTrue();
    }
  }

  @Test
  public void updateTest() {
    AllTypeTest test = new AllTypeTest();
    long id = orm.insert(AllTypeTest.class, test);

    test = orm.loadById(AllTypeTest.class, id);
    test.setString("test1");
    orm.update(AllTypeTest.class, test);

    test = orm.loadById(AllTypeTest.class, id);
    assertThat(test.getString()).isEqualTo("test1");

    test.setString("test2");
    test = orm.loadById(AllTypeTest.class, id);
    assertThat(test.getString()).isEqualTo("test2");
  }

  @Test
  public void updateConflictTest() {
    AllTypeTest test = new AllTypeTest();
    long id = orm.insert(AllTypeTest.class, test);

    test = orm.loadById(AllTypeTest.class, id);
    orm.update(AllTypeTest.class, test);
    try {
      // Cannot be updated without reload.
      orm.update(AllTypeTest.class, test);
      Assertions.failBecauseExceptionWasNotThrown(OrmException.class);
    } catch (Exception e) {
      assertThat(e instanceof OrmException).isTrue();
    }
  }

  @Test
  public void deleteTest() {
    AllTypeTest test = new AllTypeTest();
    long id = orm.insert(AllTypeTest.class, test);

    test = orm.loadById(AllTypeTest.class, id);
    orm.delete(AllTypeTest.class, test);

    test = orm.loadById(AllTypeTest.class, id);
    assertThat(test).isNull();
  }

  @Test
  public void deleteByIdTest() {
    AllTypeTest test = new AllTypeTest();
    long id = orm.insert(AllTypeTest.class, test);

    orm.deleteById(AllTypeTest.class, id);

    test = orm.loadById(AllTypeTest.class, id);
    assertThat(test).isNull();
  }

}
