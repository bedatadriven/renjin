package org.renjin.gcc;

/**
 * An existing JVM class to which the record type jvm_rect will be mapped
 */
public class JvmRect {
  
  private int width;
  private int height;

  public JvmRect(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public static int area(JvmRect rect) {
    return rect.width * rect.height;
  }
}
