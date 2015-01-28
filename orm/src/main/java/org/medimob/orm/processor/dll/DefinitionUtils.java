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

  /**
   * Checks if parameter property is not null or throw a new Mapping exception.
   *
   * @param property checked property.
   * @param message  error message.
   * @throws MappingException if property is null.
   */
  public static void notNull(Object property, String message) throws MappingException {
    if (property == null) {
      throw new MappingException(message);
    }
  }

  /**
   * Checks if parameter string property is not null or empty otherwise throw a new Mapping
   * exception.
   *
   * @param property checked property.
   * @param message  error message.
   * @throws MappingException if property is null or empty.
   */
  public static void notEmpty(String property, String message) throws MappingException {
    if (property == null || property.isEmpty()) {
      throw new MappingException(message);
    }
  }


  /**
   * Checks if parameter collection property is not null or empty otherwise throw a new Mapping
   * exception.
   *
   * @param property checked property.
   * @param message  error message.
   * @throws MappingException if property is null or empty.
   */
  public static void notEmpty(Collection<?> property, String message) throws MappingException {
    if (property == null || property.isEmpty()) {
      throw new MappingException(message);
    }
  }

  /**
   * Add or throw a new Mapping exception if the element already exist in collection.
   *
   * @param set     collection where the element will be added.
   * @param element the element to add.
   * @param message the error message.
   * @param <E>     element type.
   * @throws MappingException if element already exist in collection.
   */
  public static <E> void addOrThrowIfExist(Set<E> set, E element, String message)
      throws MappingException {
    if (!set.add(element)) {
      throw new MappingException(message);
    }
  }
}
