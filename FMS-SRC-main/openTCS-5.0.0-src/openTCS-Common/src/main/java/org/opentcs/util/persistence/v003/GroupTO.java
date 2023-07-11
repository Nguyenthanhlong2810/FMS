/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v003;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.*;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name","members", "properties","description","id"})
public class GroupTO
    extends PlantModelElementTO {

  private List<MemberTO> members = new ArrayList<>();
  private String description="";
  private int id;
  @XmlElement(name = "member")
  public List<MemberTO> getMembers() {
    return members;
  }

  public GroupTO setMembers(@Nonnull List<MemberTO> members) {
    requireNonNull(members, "members");
    this.members = members;
    return this;
  }
  @XmlAttribute(name = "description")
  public String getDescription(){
    return description;
  }

  public GroupTO setDescription(String description) {
    this.description = description;
    return this;
  }

  @XmlAttribute(name = "id")
  public int getId(){
    return id;
  }

  public GroupTO setId(int id) {
    this.id = id;
    return this;
  }

}
