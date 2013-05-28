package org.renjin.gcc.translate.expr;


import org.renjin.gcc.jimple.JimpleExpr;

public class ArrayRef {
  private JimpleExpr arrayExpr;
  private JimpleExpr indexExpr;

  public ArrayRef(JimpleExpr arrayExpr, JimpleExpr indexExpr) {
    this.arrayExpr = arrayExpr;
    this.indexExpr = indexExpr;
  }

  public ArrayRef(String jimpleName, int offset) {
    this.arrayExpr = new JimpleExpr(jimpleName);
    this.indexExpr = JimpleExpr.integerConstant(offset);
  }

  public ArrayRef(String jimpleArrayName, String jimpleOffsetName) {
    this.arrayExpr = new JimpleExpr(jimpleArrayName);
    this.indexExpr = new JimpleExpr(jimpleOffsetName);

  }

  public ArrayRef(String jimpleArrayName, JimpleExpr indexExpr) {
    this.arrayExpr = new JimpleExpr(jimpleArrayName);
    this.indexExpr = indexExpr;
  }

  public JimpleExpr getArrayExpr() {
    return arrayExpr;
  }

  public JimpleExpr getIndexExpr() {
    return indexExpr;
  }
}
