package org.renjin.gcc.gimple.expr;


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
}

