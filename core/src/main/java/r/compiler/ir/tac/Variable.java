package r.compiler.ir.tac;

import r.lang.Symbol;

public class Variable implements LValue, SimpleExpr {

  private final Symbol name;
  
  public Variable(Symbol name) {
    this.name = name;
  }
  
  public Symbol getName() {
    return name;
  }
  
  @Override
  public String toString() {
    return name.toString();
  }
}
