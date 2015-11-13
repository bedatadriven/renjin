package org.renjin.gcc;

import org.renjin.gcc.runtime.ObjectPtr;

/**
 * An existing JVM class to which the record type jvm_rect will be mapped
 */
public class JvmRect {
  
  public int width;
  public int height;

  public JvmRect() {
  }

  public JvmRect(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public static int area(JvmRect rect) {
    return rect.width * rect.height;
  }
  
  public static int areas(ObjectPtr<JvmRect> rects) {
    int area = 0;
    for(int i=0;i<2;++i) {
      area += area(rects.get(i));
    }
    return area;
  }
}
