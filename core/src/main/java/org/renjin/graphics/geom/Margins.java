package org.renjin.graphics.geom;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class Margins {
  
  private double bottom;
  private double left;
  private double top;
  private double right;
  
  public Margins() {
    
  }
  
  public Margins(double bottom, double left, double top, double right) {
    super();
    this.bottom = bottom;
    this.left = left;
    this.top = top;
    this.right = right;
  }
  
   public static Margins fromExp(SEXP exp) {
     if(!(exp instanceof Vector)) {
       throw new IllegalArgumentException("vector required");
     } 
     Vector vector = (Vector)exp;
     if(exp.length() != 4) {
       throw new IllegalArgumentException("vector of length 4 required");
     }
     return new Margins(
         vector.getElementAsDouble(0),
         vector.getElementAsDouble(1),
         vector.getElementAsDouble(2),
         vector.getElementAsDouble(3));
   }

  public double getBottom() {
    return bottom;
  }

  public double getLeft() {
    return left;
  }

  public double getTop() {
    return top;
  }

  public double getRight() {
    return right;
  }
  
  public Vector toVector() {
    return new DoubleArrayVector(bottom, left, top, right);
  }

  public Margins multiplyBy(Dimension size) {
    return new Margins(
        bottom * size.getHeight(), 
        left * size.getWidth(), 
        top * size.getHeight(), 
        right * size.getWidth());
  }
}
