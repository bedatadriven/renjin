package org.renjin.grDevices;

import org.renjin.gcc.runtime.Ptr;

import java.awt.*;

public class GDPolygon implements GDObject {
  private int n;
  private int[] xi;
  private int[] yi;
  private boolean isPolyline;

  public GDPolygon(int n, Ptr x, Ptr y, boolean isPolyline) {
    this.n = n;
    this.isPolyline = isPolyline;
    int i = 0;
    xi = new int[n];
    yi = new int[n];
    while (i < n) {
      xi[i] = (int) (x.getAlignedDouble(i) + 0.5);
      yi[i] = (int) (y.getAlignedDouble(i) + 0.5);
      i++;
    }
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    if (gs.getFill() != null && !isPolyline) {
      g.setColor(gs.getFill());
      g.fillPolygon(xi, yi, n);
      if (gs.getCol() != null) {
        g.setColor(gs.getCol());
      }
    }
    if (gs.getCol() != null) {
      if (isPolyline) {
        g.drawPolyline(xi, yi, n);
      } else {
        g.drawPolygon(xi, yi, n);
      }
    }
  }
}
