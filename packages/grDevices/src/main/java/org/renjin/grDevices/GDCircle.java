package org.renjin.grDevices;

import java.awt.*;

class GDCircle implements GDObject {
  private double x;
  private double y;
  private double r;

  public GDCircle(double x, double y, double r) {
    this.x = x;
    this.y = y;
    this.r = r;
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    if (gs.getFill() != null) {
      g.setColor(gs.getFill());
      g.fillOval((int) (x - r + 0.5), (int) (y - r + 0.5), (int) (r + r + 1.5), (int) (r + r + 1.5));
      if (gs.getCol() != null) {
        g.setColor(gs.getCol());
      }
    }
    if (gs.getCol() != null) {
      g.drawOval((int) (x - r + 0.5), (int) (y - r + 0.5), (int) (r + r + 1.5), (int) (r + r + 1.5));
    }
  }
}
