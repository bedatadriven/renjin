package org.renjin.gcc.gimple.type;


import com.google.common.collect.Lists;

import java.util.List;

public class GimpleRecordTypeDef {
  private String id;
  private String name;
  
  private boolean union;
  
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

  /**
   * @return true if this record definition is a union, that is, its fields 
   * share the same position in memory.
   */
  public boolean isUnion() {
    return union;
  }

  public void setUnion(boolean union) {
    this.union = union;
  }

  public List<GimpleField> getFields() {
    return fields;
  }

  public String toString() {
    StringBuilder out = new StringBuilder();
    out.append("struct ").append(name).append(" {\n");
    for (GimpleField field : fields) {
      out.append(String.format("    %s %s\n", field.getType(), field.getName()));
    }
    out.append("}");
    return out.toString();
  }
}
