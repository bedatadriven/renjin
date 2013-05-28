package org.renjin.gcc.gimple;

public class GimpleLabel {
  private String name;

  public GimpleLabel(String name) {
    super();
    this.name = name.replace(" ", "");
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
