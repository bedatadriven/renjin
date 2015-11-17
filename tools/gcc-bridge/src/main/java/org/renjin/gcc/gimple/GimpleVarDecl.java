package org.renjin.gcc.gimple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.SymbolRef;
import org.renjin.gcc.gimple.type.GimpleType;

public class GimpleVarDecl {
  private int id;
  private GimpleType type;
  private String name;
  private GimpleExpr value;
  
  @JsonProperty("const")
  private boolean constant;
  
  private boolean extern;

  /**
   * True if this local variable is addressable
   */
  private boolean addressable;

  public GimpleVarDecl() {

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public GimpleType getType() {
    return type;
  }

  public void setType(GimpleType type) {
    this.type = type;
  }

  public String getName() {
    if (name != null) {
      return name;
    } else {
      return "T" + Math.abs(id);
    }
  }
  
  public boolean isNamed() {
    return name != null;
  }

  public boolean isConstant() {
    return constant;
  }

  public void setConstant(boolean constant) {
    this.constant = constant;
  }

  public void setName(String name) {
    this.name = name;
  }

  public GimpleExpr getValue() {
    return value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  public boolean isAddressable() {
    return addressable;
  }

  public void setAddressable(boolean addressable) {
    this.addressable = addressable;
  }

  @Override
  public String toString() {
    return type + " " + (name == null ? "T" + Math.abs(id) : name);
  }

  /**
   * 
   * @return true f this variable declaration has external linkage, that is, it is visible outside
   * of the compilation unit.
   */
  public boolean isExtern() {
    return extern;
  }

  public void setExtern(boolean extern) {
    this.extern = extern;
  }
  
  public Predicate<GimpleExpr> isReference() {
    return new Predicate<GimpleExpr>() {
      @Override
      public boolean apply(GimpleExpr input) {
        return input instanceof SymbolRef && 
            ((SymbolRef) input).getId() == id;
      }
    };
  }
}
