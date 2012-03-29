package org.renjin.graphics;

import java.util.List;


/**
 *  the color palette which is used when a col= has a numeric index.
 */
public class ColorPalette {
  private List<Color> colors;

  public Color get(int index) {
    return colors.get(index % colors.size());
  }
}
