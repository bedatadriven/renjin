package org.renjin.grDevices;

import java.awt.*;

public class GDLinePar implements GDObject {
  private BasicStroke bs;

  public GDLinePar(double lwd, int lty) {
    bs = null;
    if (lty == 0) {
      bs = new BasicStroke((float) lwd);
    } else if (lty == -1) {
      bs = new BasicStroke(0f);
    } else {
      int l = 0;
      int dt = lty;
      while (dt > 0) {
        dt >>= 4;
        l++;
      }
      float[] dash = new float[l];
      dt = lty;
      l = 0;
      while (dt > 0) {
        int rl = dt & 15;
        dash[l++] = (float) rl;
        dt >>= 4;
      }
      bs = new BasicStroke((float) lwd, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, dash, 0f);
    }
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    if (bs != null) {
      ((Graphics2D) g).setStroke(bs);
    }
  }
}
