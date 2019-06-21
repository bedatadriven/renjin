package org.renjin.grDevices;

import java.awt.*;

class GDLine implements GDObject {
  private double x1;
  private double y1;
  private double x2;
  private double y2;

  public GDLine(double x1, double y1, double x2, double y2) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    if (gs.getCol() != null) {
      g.drawLine((int) (x1 + 0.5), (int) (y1 + 0.5), (int) (x2 + 0.5), (int) (y2 + 0.5));
    }
  }
}
