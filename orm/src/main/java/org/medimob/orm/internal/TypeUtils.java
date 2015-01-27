package org.medimob.orm.internal;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import org.medimob.orm.OrmException;
import org.medimob.orm.OrmLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Poopaou on 21/01/2015.
 */
public final class TypeUtils {

    private TypeUtils() {}

    public static Long getLong(Cursor cursor, int column){
        if(cursor.isNull(column)){
            return null;
        }
        return cursor.getLong(column);
    }

    public static void bind(SQLiteStatement statement, Long value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindLong(index, value);
    }

    public static Boolean getBoolean(Cursor cursor, int column){
        if(cursor.isNull(column)){
            return null;
        }
        return cursor.getInt(column) == 1;
    }

    public static void bind(SQLiteStatement statement, Boolean value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindLong(index, value ? 1 : 2);
    }

    public static Character getChar(Cursor cursor, int column){
        if(cursor.isNull(column)){
            return null;
        }
        return cursor.getString(column).charAt(0);
    }

    public static void bind(SQLiteStatement statement, Character value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindLong(index, value);
    }

    public static Byte getByte(Cursor cursor, int column){
        if(cursor.isNull(column)){
            return null;
        }
        return (byte) cursor.getInt(column);
    }

    public static void bind(SQLiteStatement statement,Byte value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindLong(index, value);
    }

    public static Short getShort(Cursor cursor, int index){
        if(cursor.isNull(index)){
            return null;
        }
        return (short) cursor.getInt(index);
    }

    public static void bind(SQLiteStatement statement, Short value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindLong(index, value);
    }

    public static Integer getInt(Cursor cursor, int column){
        if(cursor.isNull(column)){
            return null;
        }
        return cursor.getInt(column);
    }

    public static void bind(SQLiteStatement statement, Integer value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindLong(index, value);
    }

    public static Float getFloat(Cursor cursor, int column){
        if(cursor.isNull(column)){
            return null;
        }
        return cursor.getFloat(column);
    }

    public static void bind(SQLiteStatement statement, Float value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindDouble(index, value);
    }

    public static Double getDouble(Cursor cursor, int column){
        if(cursor.isNull(column)){
            return null;
        }
        return cursor.getDouble(column);
    }

    public static void bind(SQLiteStatement statement, Double value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindDouble(index, value);
    }

    public static String getString(Cursor cursor, int column){
        if(cursor.isNull(column)){
            return null;
        }
        return cursor.getString(column);
    }

    public static void bind(SQLiteStatement statement, String value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindString(index, value);
    }

    public static byte[] getByteArray(Cursor cursor, int column){
        return cursor.getBlob(column);
    }

    public static void bind(SQLiteStatement statement, byte[] value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindBlob(index, value);
    }

    public static Date getDate(Cursor cursor, int column){
        if(cursor.isNull(column)){
            return null;
        }
        return new Date(cursor.getLong(column));
    }

    public static void bind(SQLiteStatement statement, Date value, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        statement.bindLong(index, value.getTime());
    }

    public static Date getDate(Cursor cursor, int column, String pattern){
        if(cursor.isNull(column)){
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        String value = cursor.getString(column);
        try {
            return format.parse(value);
        } catch (ParseException e) {
            OrmLog.e("Failed to parse date : " + value + " patter : " + pattern, e);
            throw new OrmException("Failed to parse date : " + value + " patter : " + pattern, e);
        }
    }

    public static void bind(SQLiteStatement statement, Date value, String pattern, int index){
        if(value == null){
            statement.bindNull(index);
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        statement.bindString(index, format.format(value));

    }
}
