package org.renjin.graphics.geom;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class Rectangle {
  private double x1;
  private double y1;
  private double x2;
  private double y2;
  
  public static final Rectangle UNIT_RECT = new Rectangle(0, 1, 0, 1);
  
  public Rectangle(double x1, double x2, double y1, double y2) {
    super();
    assert x1 <= x2;
    assert y1 <= y2;
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }
  

  public static Rectangle from(SEXP exp) {
    if(exp instanceof Vector) {
      throw new IllegalArgumentException();
    } else {
      return from((Vector)exp);
    }
  }
  
  public static Rectangle from(Vector vector) {
    if(vector.length() != 4) {
      throw new IllegalArgumentException("Expected vector of length 4 (x1, x2, y1, x2)");
    }
    return new Rectangle(
        vector.getElementAsDouble(0),
        vector.getElementAsDouble(1),
        vector.getElementAsDouble(2),
        vector.getElementAsDouble(3));
  }
 
  public static Rectangle from(Vector xlim, Vector ylim) {
    return new Rectangle(
        xlim.getElementAsDouble(0),
        xlim.getElementAsDouble(1),
        ylim.getElementAsDouble(0),
        ylim.getElementAsDouble(1));
  }

  public double getX1() {
    return x1;
  }

  public double getY1() {
    return y1;
  }

  public double getX2() {
    return x2;
  }

  public double getY2() {
    return y2;
  }
  
  public double getWidth() {
    return x2-x1;
  }
  
  public double getHeight() {
    return y2-y1;
  }
  
  public DoubleVector toVector() {
    return new DoubleArrayVector(x1, x2, y1, y2);
  }

  public Rectangle normalize(Rectangle rect) {
    return new Rectangle(
        normalizeX(rect.getX1()),
        normalizeX(rect.getX2()),
        normalizeY(rect.getY2()),
        normalizeY(rect.getY1()));
  }
  
  public Point normalize(Point point) {
    return new Point(
        normalizeX(point.getX()),
        normalizeY(point.getY()));
  }
  
  private double normalizeX(double x) {
    return (x-x1) / getWidth(); 
  }
  
  private double normalizeY(double y) {
    return (y2 - y) / getHeight();
  }
  
  private double denormalizeX(double x) {
    return x1 + (x * getWidth());
  }
  
  private double denormalizeY(double y) {
    return y2 - (y * getHeight());
  }
  
  public Rectangle denormalize(Rectangle rect) {
    return new Rectangle(
        denormalizeX(rect.x1),
        denormalizeX(rect.x2),
        denormalizeY(rect.y2),
        denormalizeY(rect.y1));
  }

  public Point denormalize(Point point) {
    return new Point(
        denormalizeX(point.getX()),
        denormalizeY(point.getY()));
  }
  
  public Dimension size() {
    return new Dimension(getWidth(), getHeight());
  }


  public Rectangle apply(Margins margins) {
      return new Rectangle(
          x1 + margins.getLeft(),
          x2 - margins.getRight(),
          y1 + margins.getTop(),
          y2 - margins.getBottom());
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(x1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(x2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Rectangle other = (Rectangle) obj;
    if (Double.doubleToLongBits(x1) != Double.doubleToLongBits(other.x1))
      return false;
    if (Double.doubleToLongBits(x2) != Double.doubleToLongBits(other.x2))
      return false;
    if (Double.doubleToLongBits(y1) != Double.doubleToLongBits(other.y1))
      return false;
    if (Double.doubleToLongBits(y2) != Double.doubleToLongBits(other.y2))
      return false;
    return true;
  }


  @Override
  public String toString() {
    return "Rectangle [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2
        + "]";
  }


  public Rectangle withXLimits(double newX1, double newX2) {
    return new Rectangle(newX1, newX2, y1, y2);
  }
  
  public Rectangle withYLimits(double newY1, double newY2) {
    return new Rectangle(x1, x2, newY1, newY2);
  }
}
