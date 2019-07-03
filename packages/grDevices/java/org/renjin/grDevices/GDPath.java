package org.renjin.grDevices;

import org.renjin.gcc.runtime.Ptr;

import java.awt.*;
import java.awt.geom.GeneralPath;

class GDPath implements GDObject {

  private GeneralPath path;

  public GDPath(int npoly, Ptr numberOfPointsPerPath, Ptr x, Ptr y, boolean winding) {

    path = new GeneralPath(winding ? GeneralPath.WIND_NON_ZERO : GeneralPath.WIND_EVEN_ODD,
        countPoints(npoly, numberOfPointsPerPath));
    int k = 0;
    int end = 0;
    for (int i = 0; i < npoly; i++) {
      end += numberOfPointsPerPath.getAlignedInt(i);
      path.moveTo((float) x.getAlignedDouble(k), (float) y.getAlignedDouble(k));
      k++;
      for (; k < end; k++) {
        path.lineTo((float) x.getAlignedDouble(k), (float) y.getAlignedDouble(k));
      }
      path.closePath();
    }
  }

  private int countPoints(int npoly, Ptr numberOfPointsPerPath) {
    int count = 0;
    for (int i = 0; i < npoly; i++) {
      count += numberOfPointsPerPath.getAlignedInt(i);
    }
    return count;
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    if (gs.getFill() != null) {
      g2.setColor(gs.getFill());
      g2.fill(path);
      if (gs.getCol() != null) {
        g2.setColor(gs.getCol());
      }
    }
    if (gs.getCol() != null) {
      g2.draw(path);
    }
  }
}
