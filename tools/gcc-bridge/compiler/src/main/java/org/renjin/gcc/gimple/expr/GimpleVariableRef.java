package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.guava.base.Predicate;

public class GimpleVariableRef extends GimpleLValue implements GimpleSymbolRef {

  private String name;
  private int id;

  public GimpleVariableRef() {
  }

  public GimpleVariableRef(int id, GimpleType type) {
    this.id = id;
    this.setType(type);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimpleVariableRef that = (GimpleVariableRef) o;

    if (id != that.id) {
      return false;
    }
    return !(name != null ? !name.equals(that.name) : that.name != null);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + id;
    return result;
  }

  @Override
  public String toString() {
    if (name != null) {
      return name;
    } else {
      return "T" + Math.abs(id);
    }
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    // NOOP: Leaf node
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitVariableRef(this);
  }
}
