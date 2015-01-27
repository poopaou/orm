package org.medimob.orm.processor.dll;

import org.medimob.orm.processor.MappingException;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Poopaou on 21/01/2015.
 */
public final class DefinitionUtils {

    private DefinitionUtils() {
    }

    public static void notNull(Object property, String message) throws MappingException {
        if (property == null){
            throw new MappingException(message);
        }
    }

    public static void notEmpty(String property, String message) throws MappingException {
        if (property == null || property.isEmpty()){
            throw new MappingException(message);
        }
    }

    public static void notEmpty(Collection<?> property, String message) throws MappingException {
        if (property == null || property.isEmpty()){
            throw new MappingException(message);
        }
    }

    public static  <E> void addOrThrowIfExist(Set<E> set, E element, String message) throws MappingException {
        if (!set.add(element)){
            throw new MappingException(message);
        }
    }

}
