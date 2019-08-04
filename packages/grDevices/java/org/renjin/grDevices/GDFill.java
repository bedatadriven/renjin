package org.renjin.grDevices;

import java.awt.*;

public class GDFill implements GDObject {
  private Color gc;

  public GDFill(int col) {
    if ((col & 0xff000000) == 0) {
      gc = null; // opacity=0 -> no color -> don't paint
    } else {
      gc = new Color(((float) (col & 255)) / 255f,
          ((float) ((col >> 8) & 255)) / 255f,
          ((float) ((col >> 16) & 255)) / 255f,
          ((float) ((col >> 24) & 255)) / 255f);
    }
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    gs.setFill(gc);
  }
}
