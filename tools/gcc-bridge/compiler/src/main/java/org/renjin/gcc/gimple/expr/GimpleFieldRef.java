package org.renjin.gcc.gimple.expr;

/**
 * Gimple expression which evaluates to the name of a field
 */
public class GimpleFieldRef extends GimpleExpr {


  private int id;
  private int offset;
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
    return name;
  }
}
