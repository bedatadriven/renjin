package org.renjin.grDevices;

import java.awt.*;

public class GDColor implements GDObject {
  private Color gc;

  public GDColor(int col) {
    if ((col & 0xff000000) == 0) {
      gc = null; // opacity=0 -> no color -> don't paint
    } else {
      gc = Colors.valueOf(col);
    }
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    gs.setCol(gc);
    if (gc != null) {
      g.setColor(gc);
    }
  }
}
