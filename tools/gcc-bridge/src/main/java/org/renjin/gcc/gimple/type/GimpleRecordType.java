package org.renjin.gcc.gimple.type;

import java.util.List;

import com.google.common.collect.Lists;

public class GimpleRecordType extends AbstractGimpleType {
  private String name;
  private String id;
  

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    if(name == null) {
      return "anonymous_" + id;
    }
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "struct " + getName();
  }
}
