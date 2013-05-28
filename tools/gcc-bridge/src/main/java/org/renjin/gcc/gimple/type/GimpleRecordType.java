package org.renjin.gcc.gimple.type;

import java.util.List;

import com.google.common.collect.Lists;

public class GimpleRecordType extends AbstractGimpleType {
  private String name;
  private int id;
  
  private List<GimpleField> fields = Lists.newArrayList();
    
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
  

  public List<GimpleField> getFields() {
    return fields;
  }

  @Override
  public String toString() {
    return "struct " + name;
  }
}
