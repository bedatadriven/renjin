package org.renjin.grDevices;

import java.awt.*;

public class GDClip implements GDObject {
  private double x1;
  private double y1;
  private double x2;
  private double y2;

  public GDClip(double x1, double y1, double x2, double y2) {
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
    g.setClip((int) (x1 + 0.5), (int) (y1 + 0.5), (int) (x2 - x1 + 1.7), (int) (y2 - y1 + 1.7));
  }
}
