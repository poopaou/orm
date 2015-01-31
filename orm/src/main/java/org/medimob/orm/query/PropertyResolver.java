package org.medimob.orm.query;

/**
 * Created by Poopaou on 30/01/2015.
 */
public interface PropertyResolver {

  public String resolveColumnForProperty(String property);

  public String resolveIdColumn();
}
