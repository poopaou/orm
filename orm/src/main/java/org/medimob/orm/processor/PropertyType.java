package org.medimob.orm.processor;

/**
 * Field property type.
 */
public enum PropertyType {
    BOOLEAN ("NUMERIC"),
    CHARACTER("TEXT"),
    BYTE("INTEGER"),
    SHORT("INTEGER"),
    INTEGER("INTEGER"),
    LONG("INTEGER"),
    FLOAT("REAL"),
    DOUBLE("REAL"),
    DATE_LONG("NUMERIC"),
    DATE_STRING("TEXT"),
    STRING("TEXT"),
    BYTE_ARRAY("");

    public String mappedType;
    PropertyType(String mappedType){
        this.mappedType = mappedType;
    }
}
