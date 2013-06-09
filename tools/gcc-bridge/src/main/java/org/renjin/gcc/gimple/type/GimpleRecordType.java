package org.renjin.gcc.gimple.type;

import java.util.List;

import com.google.common.collect.Lists;

public class GimpleRecordType extends AbstractGimpleType {
  private String name;
  private int id;
  

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    if(name == null) {
      return "anonymous" + Math.abs(id);
    }
    return name;
  } 
  


  @Override
  public String toString() {
    return "struct " + getName();
  }
}
