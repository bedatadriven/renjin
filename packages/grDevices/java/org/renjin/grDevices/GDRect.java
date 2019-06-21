package org.renjin.grDevices;

import java.awt.*;

class GDRect implements GDObject {
  private double x1;
  private double y1;
  private double x2;
  private double y2;

  public GDRect(double x1, double y1, double x2, double y2) {
    double tmp;
    if (x1 > x2) {
      tmp = x1;
      x1 = x2;
      x2 = tmp;
    }
    if (y1 > y2) {
      tmp = y1;
      y1 = y2;
      y2 = tmp;
    }
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    int x = (int) (x1 + 0.5);
    int y = (int) (y1 + 0.5);
    int w = (int) (x2 + 0.5) - x;
    int h = (int) (y2 + 0.5) - y;
    if (gs.getFill() != null) {
      g.setColor(gs.getFill());
      g.fillRect(x, y, w + 1, h + 1);
      if (gs.getCol() != null) {
        g.setColor(gs.getCol());
      }
    }
    if (gs.getCol() != null) {
      g.drawRect(x, y, w, h);
    }
  }
}
