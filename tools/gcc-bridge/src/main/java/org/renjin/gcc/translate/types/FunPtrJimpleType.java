package org.renjin.gcc.translate.types;

import org.renjin.gcc.jimple.JimpleType;

public class FunPtrJimpleType extends JimpleType {

  private final String fcqn;

  public FunPtrJimpleType(String fcqn) {
    super();
    this.fcqn = fcqn;
  }
  
  
  
  @Override
  public boolean isFunctionPointer() {
    return true;
  }



  @Override
  public String toString() {
    return fcqn;
  }
  
}
