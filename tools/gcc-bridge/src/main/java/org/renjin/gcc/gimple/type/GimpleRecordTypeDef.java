package org.renjin.gcc.gimple.type;


import com.google.common.collect.Lists;

import java.util.List;

public class GimpleRecordTypeDef {
  private int id;
  private String name;

  private List<GimpleField> fields = Lists.newArrayList();


  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<GimpleField> getFields() {
    return fields;
  }

}
