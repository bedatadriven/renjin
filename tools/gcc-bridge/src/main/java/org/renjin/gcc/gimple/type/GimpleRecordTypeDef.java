package org.renjin.gcc.gimple.type;


import com.google.common.collect.Lists;

import java.util.List;

public class GimpleRecordTypeDef {
  private String id;
  private String name;

  private List<GimpleField> fields = Lists.newArrayList();


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<GimpleField> getFields() {
    return fields;
  }

}
