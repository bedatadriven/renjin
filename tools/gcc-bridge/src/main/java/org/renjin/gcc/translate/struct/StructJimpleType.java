package org.renjin.gcc.translate.struct;

import org.renjin.gcc.jimple.JimpleType;

/**
 * 
 * Reference to a JVM class that will be constructed
 * for a struct, but does not yet exist.
 */
public class StructJimpleType extends JimpleType {

  private final String fqcn;

  public StructJimpleType(String fqcn) {
    super();
    this.fqcn = fqcn;
  }
  
  @Override
  public String toString() {
    return fqcn;
  }
  
}
