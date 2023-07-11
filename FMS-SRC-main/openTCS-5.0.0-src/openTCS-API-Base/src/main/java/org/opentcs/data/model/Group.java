/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.*;

import static java.util.Objects.requireNonNull;

import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;

/**
 * An aggregation of model elements.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Group
    extends TCSObject<Group>
    implements Serializable {

  /**
   * The model elements aggregated in this group.
   */
  private final Set<TCSObjectReference<?>> members;
  private final int id;
  private final String description;

  /**
   * Creates a new, empty group.
   *
   * @param name This group's name.
   */
  public Group(String name) {
    super(name);
    this.members = new LinkedHashSet<>();
    this.id = 0;
    this.description = null;
  }

  private Group(String name,
                Map<String, String> properties,
                ObjectHistory history,
                Set<TCSObjectReference<?>> members,
                int id,
                String description) {
    super(name, properties, history);
    this.members = new LinkedHashSet<>(requireNonNull(members, "members"));
    this.id = id;
    this.description = description;
  }

  @Override
  public Group withProperty(String key, String value) {
    return new Group(getName(),
                     propertiesWith(key, value),
                     getHistory(),
                     members,
                     id,
                     description);
  }

  @Override
  public Group withProperties(Map<String, String> properties) {
    return new Group(getName(),
                     properties,
                     getHistory(),
                     members,
                     id,
                     description);
  }

  @Override
  public TCSObject<Group> withHistoryEntry(ObjectHistory.Entry entry) {
    return new Group(getName(),
                     getProperties(),
                     getHistory().withEntryAppended(entry),
                     members,
                     id,
                     description);
  }

  @Override
  public TCSObject<Group> withHistory(ObjectHistory history) {
    return new Group(getName(),
                     getProperties(),
                     history,
                     members,
                     id,
                     description);
  }

  /**
   * Returns an unmodifiable set of all members of this group.
   *
   * @return An unmodifiable set of all members of this group.
   */
  public Set<TCSObjectReference<?>> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  /**
   * Creates a copy of this object, with the given members.
   *
   * @param members The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Group withMembers(Set<TCSObjectReference<?>> members) {
    return new Group(getName(),
                     getProperties(),
                     getHistory(),
                     members,
                     id,
                     description);
  }

  public int getId(){return id;}

  public Group withId(int id){
    return new Group(getName(),
            getProperties(),
            getHistory(),
            members,
            id,
            description);
  }

  public String getDescription(){return description;}

  public Group withDescription(String description){
    return new Group(getName(),
            getProperties(),
            getHistory(),
            members,
            id,
            description);
  }
}
