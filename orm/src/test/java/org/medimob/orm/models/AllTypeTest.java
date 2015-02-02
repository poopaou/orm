package org.medimob.orm.models;

import org.medimob.orm.annotation.DateField;
import org.medimob.orm.annotation.Id;
import org.medimob.orm.annotation.Model;
import org.medimob.orm.annotation.Property;
import org.medimob.orm.annotation.Version;

import java.util.Date;

@Model
public class AllTypeTest {

  ////////////////////////////
  @Id
  long id;
  ////////////////////////////
  @Property
  Boolean objectBoolean;
  @Property
  boolean primitiveBoolean;
  ////////////////////////////
  @Property
  Character objectCharacter;
  @Property
  char primitiveCharacter;
  ////////////////////////////
  @Property
  Byte objectByte;
  @Property
  byte primitiveByte;
  ////////////////////////////
  @Property
  Short objectShort;
  @Property
  short primitiveShort;
  ////////////////////////////
  @Property
  Integer objectInt;
  @Property
  int primitiveInt;
  ////////////////////////////
  @Property
  Long objectLong;
  @Property
  long primitiveLong;
  ////////////////////////////
  @Property
  Float objectFloat;
  @Property
  float primitiveFloat;
  ////////////////////////////
  @Property
  Double objectDouble;
  @Property
  double primitiveDouble;
  ////////////////////////////
  @Property
  String string;
  ////////////////////////////
  @Property(dateType = DateField.DATE_STRING)
  Date stringDate;
  @Property(dateType = DateField.DATE_LONG)
  Date longDate;
  ////////////////////////////
  @Property
  byte[] bytesArray;
  ////////////////////////////
  @Version
  long version;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Boolean getObjectBoolean() {
    return objectBoolean;
  }

  public void setObjectBoolean(Boolean objectBoolean) {
    this.objectBoolean = objectBoolean;
  }

  public boolean isPrimitiveBoolean() {
    return primitiveBoolean;
  }

  public void setPrimitiveBoolean(boolean primitiveBoolean) {
    this.primitiveBoolean = primitiveBoolean;
  }

  public Character getObjectCharacter() {
    return objectCharacter;
  }

  public void setObjectCharacter(Character objectCharacter) {
    this.objectCharacter = objectCharacter;
  }

  public char getPrimitiveCharacter() {
    return primitiveCharacter;
  }

  public void setPrimitiveCharacter(char primitiveCharacter) {
    this.primitiveCharacter = primitiveCharacter;
  }

  public Byte getObjectByte() {
    return objectByte;
  }

  public void setObjectByte(Byte objectByte) {
    this.objectByte = objectByte;
  }

  public byte getPrimitiveByte() {
    return primitiveByte;
  }

  public void setPrimitiveByte(byte primitiveByte) {
    this.primitiveByte = primitiveByte;
  }

  public Short getObjectShort() {
    return objectShort;
  }

  public void setObjectShort(Short objectShort) {
    this.objectShort = objectShort;
  }

  public short getPrimitiveShort() {
    return primitiveShort;
  }

  public void setPrimitiveShort(short primitiveShort) {
    this.primitiveShort = primitiveShort;
  }

  public Integer getObjectInt() {
    return objectInt;
  }

  public void setObjectInt(Integer objectInt) {
    this.objectInt = objectInt;
  }

  public int getPrimitiveInt() {
    return primitiveInt;
  }

  public void setPrimitiveInt(int primitiveInt) {
    this.primitiveInt = primitiveInt;
  }

  public Long getObjectLong() {
    return objectLong;
  }

  public void setObjectLong(Long objectLong) {
    this.objectLong = objectLong;
  }

  public long getPrimitiveLong() {
    return primitiveLong;
  }

  public void setPrimitiveLong(long primitiveLong) {
    this.primitiveLong = primitiveLong;
  }

  public Float getObjectFloat() {
    return objectFloat;
  }

  public void setObjectFloat(Float objectFloat) {
    this.objectFloat = objectFloat;
  }

  public float getPrimitiveFloat() {
    return primitiveFloat;
  }

  public void setPrimitiveFloat(float primitiveFloat) {
    this.primitiveFloat = primitiveFloat;
  }

  public Double getObjectDouble() {
    return objectDouble;
  }

  public void setObjectDouble(Double objectDouble) {
    this.objectDouble = objectDouble;
  }

  public double getPrimitiveDouble() {
    return primitiveDouble;
  }

  public void setPrimitiveDouble(double primitiveDouble) {
    this.primitiveDouble = primitiveDouble;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public Date getStringDate() {
    return stringDate;
  }

  public void setStringDate(Date stringDate) {
    this.stringDate = stringDate;
  }

  public Date getLongDate() {
    return longDate;
  }

  public void setLongDate(Date longDate) {
    this.longDate = longDate;
  }

  public byte[] getBytesArray() {
    return bytesArray;
  }

  public void setBytesArray(byte[] bytesArray) {
    this.bytesArray = bytesArray;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
