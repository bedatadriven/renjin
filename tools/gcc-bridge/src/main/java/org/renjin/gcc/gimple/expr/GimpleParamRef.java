package org.renjin.gcc.gimple.expr;

import java.util.Collections;

public class GimpleParamRef extends GimpleLValue implements SymbolRef {

  private int id;
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public Iterable<? extends SymbolRef> getSymbolRefs() {
    return Collections.singleton(this);
  }

  @Override
  public String toString() {
    return name;
  }

}
