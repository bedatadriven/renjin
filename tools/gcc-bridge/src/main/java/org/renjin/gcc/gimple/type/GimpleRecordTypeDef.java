package org.renjin.gcc.gimple.type;


import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

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

  public void prettyPrintTo(Appendable out) throws IOException {
    out.append("struct ").append(name).append(" {\n");
    for (GimpleField field : fields) {
      out.append(String.format("    %s %s\n", field.getType(), field.getName()));
    }
    out.append("}\n\n");
  }
}
