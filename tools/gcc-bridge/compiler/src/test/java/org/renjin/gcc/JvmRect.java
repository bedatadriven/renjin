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
  
  public void set(JvmRect o) {
    this.width = o.width;
    this.height = o.height;
  }

  public static int area(ObjectPtr<JvmRect> rect) {
    return rect.get().width * rect.get().height;
  }
  
  public static int areas(ObjectPtr<JvmRect> rects) {
    int area = 0;
    int i = 0;
    // end of array is marked by rect with zero width
    while(rects.get(i).width != 0) {
      area += (rects.get(i).width * rects.get(i).height);
      i++;
    }
    return area;
  }
}
