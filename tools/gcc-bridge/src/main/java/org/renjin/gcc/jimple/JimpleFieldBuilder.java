package org.renjin.gcc.jimple;

import com.google.common.collect.Lists;

import java.util.List;

public class JimpleFieldBuilder {
  private JimpleType type;
  private String name;
  private List<JimpleModifiers> modifiers;

  public JimpleType getType() {
    return type;
  }

  public void setType(JimpleType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<JimpleModifiers> getModifiers() {
    return modifiers;
  }

  public void setModifiers(JimpleModifiers... modifiers) {
    this.modifiers = Lists.newArrayList(modifiers);
  }

  public void write(JimpleWriter w) {
    StringBuilder sb = new StringBuilder();
    if (modifiers.contains(JimpleModifiers.PUBLIC)) {
      sb.append("public ");
    }
    if (modifiers.contains(JimpleModifiers.STATIC)) {
      sb.append("static ");
    }
    sb.append(type);
    sb.append(" ");
    sb.append(name);
    sb.append(";");
    w.println(sb.toString());
  }
}
