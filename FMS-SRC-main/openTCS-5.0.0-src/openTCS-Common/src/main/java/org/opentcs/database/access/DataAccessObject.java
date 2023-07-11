package org.opentcs.database.access;

import java.util.Collection;

public interface DataAccessObject<T> {

  Collection<T> getAll();

  long add(T obj);

  boolean modify(T obj);

  boolean remove(long id);
}
