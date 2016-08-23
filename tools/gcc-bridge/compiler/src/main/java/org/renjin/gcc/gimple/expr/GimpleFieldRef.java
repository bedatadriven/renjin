package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

/**
 * Gimple expression which evaluates to the name of a field
 */
public class GimpleFieldRef extends GimpleExpr {
  private int id;
  private int offset;
  private int size;
  private String name;

  public GimpleFieldRef() {
  }

  public GimpleFieldRef(int id,  String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * 
   * @return the offset of this field in bits, from the start of the record.
   */
  public int getOffset() {
    return offset;
  }
  
  public int getOffsetBytes() {
    return offset / 8;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

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
  public String toString() {
    if(name == null) {
      return "field@" + offset;
    }
    if(name.contains(".")) {
      return "[" + name + "]";
    } 
    return name;
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    // NOOP: Leaf node
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitFieldRef(this);
  }
}
