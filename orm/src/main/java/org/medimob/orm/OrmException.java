package org.medimob.orm;

import java.text.ParseException;

/**
 * Created by Poopaou on 23/01/2015.
 */
public class OrmException extends RuntimeException {

    private static final long serialVersionUID = -8955268880200499139L;


    public OrmException() {
    }

    public OrmException(String message) {
        super(message);
    }

    public OrmException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrmException(Throwable cause) {
        super(cause);
    }
}
