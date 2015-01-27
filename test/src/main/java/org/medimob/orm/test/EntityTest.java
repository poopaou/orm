package org.medimob.orm.test;

import org.medimob.orm.annotation.*;

import java.util.Date;

/**
 * Created by Poopaou on 20/01/2015.
 */
@Entity
public class EntityTest {

    @Id
    Long id;

    @Column(name = "column_bool" ,notNull = true, unique = true, indexed = true, check = "aBoolean is not null")
    Boolean aBoolean;

    @Column(name = "column_char")
    @NotNull(onConflict = Conflict.FAIL)
    Character aChar;

    @Column
    Byte aByte;

    @Column
    Short aShort;

    @Column
    Integer anInt;

    @Column
    Long aLong;

    @Column
    Float aFloat;

    @Column
    Double aDouble;

    @Column
    String aString;

    @Column(dateType = DateField.DATE_STRING)
    Date aDate;

    @Column
    byte[] aBytesArray;

    @Version
    long version;
}
