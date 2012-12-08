package org.renjin.gcc.translate;

import org.renjin.gcc.jimple.JimpleType;

public class InvokerJimpleType extends JimpleType {
  
  private final String fqcn;

  public InvokerJimpleType(String fqcn) {
    super();
    this.fqcn = fqcn;
  }
  
  @Override
  public String toString() {
    return fqcn;
  }
  
}
