/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;

/**
 * A location at which a {@link Vehicle} may perform an action.
 * <p>
 * A location must be linked to at least one {@link Point} to be reachable for a vehicle.
 * It may be linked to multiple points.
 * As long as a link's specific set of allowed operations is empty (which is the default), all
 * operations defined by the location's referenced {@link LocationType} are allowed at the linked
 * point.
 * If the link's set of allowed operations is not empty, only the operations contained in it are
 * allowed at the linked point.
 * </p>
 *
 * @see LocationType
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Location
    extends TCSResource<Location>
    implements Serializable {

  /**
   * This location's position in mm.
   */
  private final Triple position;
  /**
   * A reference to this location's type.
   */
  private final TCSObjectReference<LocationType> type;
  /**
   * A set of links attached to this location.
   */
  private final Set<Link> attachedLinks;

  private final long width;

  private final long height;

  /**
   * Creates a new Location.
   *
   * @param name The new location's name.
   * @param type The new location's type.
   */
  public Location(String name, TCSObjectReference<LocationType> type) {
    super(name);
    this.type = requireNonNull(type, "type");
    this.position = new Triple(0, 0, 0);
    this.attachedLinks = new HashSet<>();
    this.width = 30;
    this.height = 30;
  }

  private Location(String name,
                   Map<String, String> properties,
                   ObjectHistory history,
                   TCSObjectReference<LocationType> locationType,
                   Triple position,
                   Set<Link> attachedLinks,
                   long width,
                   long height) {
    super(name, properties, history);
    this.type = requireNonNull(locationType, "locationType");
    this.position = requireNonNull(position, "position");
    this.attachedLinks = new HashSet<>(requireNonNull(attachedLinks, "attachedLinks"));
    this.width = width;
    this.height = height;
  }

  @Override
  public Location withProperty(String key, String value) {
    return new Location(getName(),
                        propertiesWith(key, value),
                        getHistory(),
                        type,
                        position,
                        attachedLinks,
                        width,
                        height);
  }

  @Override
  public Location withProperties(Map<String, String> properties) {
    return new Location(getName(),
                        properties,
                        getHistory(),
                        type,
                        position,
                        attachedLinks,
                        width,
                        height);
  }

  @Override
  public TCSObject<Location> withHistoryEntry(ObjectHistory.Entry entry) {
    return new Location(getName(),
                        getProperties(),
                        getHistory().withEntryAppended(entry),
                        type,
                        position,
                        attachedLinks,
                        width,
                        height);
  }

  @Override
  public TCSObject<Location> withHistory(ObjectHistory history) {
    return new Location(getName(),
                        getProperties(),
                        history,
                        type,
                        position,
                        attachedLinks,
                        width,
                        height);
  }

  public long getWidth() {
    return width;
  }
  public Location withWidth(long width) {
    return new Location(getName(),
            getProperties(),
            getHistory(),
            type,
            position,
            attachedLinks,
            width,
            height);
  }

  public long getHeight() {
    return height;
  }

  public Location withHeight(long height) {
    return new Location(getName(),
            getProperties(),
            getHistory(),
            type,
            position,
            attachedLinks,
            width,
            height);
  }
  /**
   * Returns the physical coordinates of this location in mm.
   *
   * @return The physical coordinates of this location in mm.
   */
  public Triple getPosition() {
    return position;
  }

  /**
   * Creates a copy of this object, with the given position.
   *
   * @param position The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Location withPosition(Triple position) {
    return new Location(getName(),
                        getProperties(),
                        getHistory(),
                        type,
                        position,
                        attachedLinks,
                        width,
                        height);
  }

  /**
   * Returns a reference to the type of this location.
   *
   * @return A reference to the type of this location.
   */
  public TCSObjectReference<LocationType> getType() {
    return type;
  }

  /**
   * Returns a set of links attached to this location.
   *
   * @return A set of links attached to this location.
   */
  public Set<Link> getAttachedLinks() {
    return Collections.unmodifiableSet(attachedLinks);
  }

  /**
   * Creates a copy of this object, with the given attached links.
   *
   * @param attachedLinks The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Location withAttachedLinks(@Nonnull Set<Link> attachedLinks) {
    return new Location(getName(),
                        getProperties(),
                        getHistory(),
                        type,
                        position,
                        attachedLinks,
                        width,
                        height);
  }

  /**
   * A link connecting a point and a location, expressing that the location is
   * reachable from the point.
   */
  public static class Link
      implements Serializable {

    /**
     * A reference to the location end of this link.
     */
    private final TCSResourceReference<Location> location;
    /**
     * A reference to the point end of this link.
     */
    private final TCSResourceReference<Point> point;
    /**
     * The operations allowed at this link.
     */
    private final Set<String> allowedOperations;

    /**
     * Creates a new Link.
     *
     * @param location A reference to the location end of this link.
     * @param point A reference to the point end of this link.
     */
    public Link(TCSResourceReference<Location> location,
                TCSResourceReference<Point> point) {
      this.location = requireNonNull(location, "location");
      this.point = requireNonNull(point, "point");
      this.allowedOperations = new TreeSet<>();
    }

    private Link(TCSResourceReference<Location> location,
                 TCSResourceReference<Point> point,
                 Set<String> allowedOperations) {
      this.location = requireNonNull(location, "location");
      this.point = requireNonNull(point, "point");
      this.allowedOperations = new TreeSet<>(requireNonNull(allowedOperations,
                                                            "allowedOperations"));
    }

    /**
     * Returns a reference to the location end of this link.
     *
     * @return A reference to the location end of this link.
     */
    public TCSResourceReference<Location> getLocation() {
      return location;
    }

    /**
     * Returns a reference to the point end of this link.
     *
     * @return A reference to the point end of this link.
     */
    public TCSResourceReference<Point> getPoint() {
      return point;
    }

    /**
     * Returns the operations allowed at this link.
     *
     * @return The operations allowed at this link.
     */
    public Set<String> getAllowedOperations() {
      return Collections.unmodifiableSet(allowedOperations);
    }

    /**
     * Checks if a vehicle is allowed to execute a given operation at this link.
     *
     * @param operation The operation to be checked.
     * @return <code>true</code> if, and only if, vehicles are allowed to
     * execute the given operation at his link.
     */
    public boolean hasAllowedOperation(String operation) {
      requireNonNull(operation, "operation");
      return allowedOperations.contains(operation);
    }

    /**
     * Creates a copy of this object, with the given allowed operations.
     *
     * @param allowedOperations The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Link withAllowedOperations(Set<String> allowedOperations) {
      return new Link(location, point, allowedOperations);
    }

    /**
     * Checks if this object is equal to another one.
     * Two <code>Link</code>s are equal if they both reference the same location
     * and point ends.
     *
     * @param obj The object to compare this one to.
     * @return <code>true</code> if, and only if, <code>obj</code> is also a
     * <code>Link</code> and reference the same location and point ends as this
     * one.
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Link) {
        Link other = (Link) obj;
        return point.equals(other.getPoint())
            && location.equals(other.getLocation());
      }
      else {
        return false;
      }
    }

    /**
     * Returns a hash code for this link.
     * The hash code of a <code>Location.Link</code> is computed as the
     * exclusive OR (XOR) of the hash codes of the associated location and point
     * references.
     *
     * @return A hash code for this link.
     */
    @Override
    public int hashCode() {
      return location.hashCode() ^ point.hashCode();
    }
  }
}
