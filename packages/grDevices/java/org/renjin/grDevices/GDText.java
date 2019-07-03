package org.renjin.grDevices;

import java.awt.*;

class GDText implements GDObject {
  private double x;
  private double y;
  private double r;
  private double h;
  private String txt;

  public GDText(double x, double y, double r, double h, String txt) {
    this.x = x;
    this.y = y;
    this.r = r;
    this.h = h;
    this.txt = txt;
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    if (gs.getCol() != null) {
      double rx = x;
      double ry = y;
      double hc = 0d;

      if (h != 0d) {
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(txt);
        hc = ((double) w) * h;
        rx = x - (((double) w) * h);
      }
      int ix = (int) (rx + 0.5);
      int iy = (int) (ry + 0.5);

      if (r != 0d) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(x, y);
        double rr = -r / 180d * Math.PI;
        g2d.rotate(rr);
        if (hc != 0d) {
          g2d.translate(-hc, 0d);
        }
        g2d.drawString(txt, 0, 0);
        if (hc != 0d) {
          g2d.translate(hc, 0d);
        }
        g2d.rotate(-rr);
        g2d.translate(-x, -y);
      } else {
        g.drawString(txt, ix, iy);
      }
    }
  }
}
