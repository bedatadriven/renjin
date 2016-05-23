package org.renjin.gcc.gimple.expr;


import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleExprVisitor;

public class GimpleComplexConstant extends GimpleConstant {

  private GimpleRealConstant real;
  private GimpleRealConstant im;

  public GimpleRealConstant getReal() {
    return real;
  }

  public void setReal(GimpleRealConstant real) {
    this.real = real;
  }

  public GimpleRealConstant getIm() {
    return im;
  }

  public void setIm(GimpleRealConstant im) {
    this.im = im;
  }

  @Override
  public String toString() {
    return real + "+" + im + "i";
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    real = (GimpleRealConstant) replaceOrDescend(real, predicate, newExpr);
    im = (GimpleRealConstant) replaceOrDescend(im, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitComplexConstant(this);
  }
}

