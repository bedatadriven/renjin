/*

GDInterface.java
Java Graphics Device

Created by Simon Urbanek on Thu Aug 05 2004.
Copyright (c) 2004-2009 Simon Urbanek. All rights reserved.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation;
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

*/

package org.renjin.grDevices;

import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.Ptr;

import java.awt.*;

/**
 * <code>GraphicsDevice</code> defines an interface (and provides a simple implementation) between the JavaGD R device
 * and the Java code. Any back-end that desires to display R graphics in Java can subclass this class are provide its
 * name to JavaGD package via JAVAGD_CLASS_NAME environment variable. The default implementation handles most
 * callbacks, but subclasses should override at least {@link #open} to create an instance of {@link GDContainer}
 * {@link #container} which will be used for all subsequent drawing.
 */
public abstract class GraphicsDevice {

  /**
   * flag indicating whether this device is active (current) in R
   */
  private boolean active = false;

  /**
   * flag indicating whether hold is in progress
   */
  private boolean holding = false;

  /**
   * device number as supplied by R in {@link #newPage()} (-1 if undefined)
   */
  int deviceNumber = -1;

  /**
   * container that will receive all drawing methods. It should be created by subclasses in the {@link #open} method.
   */
  protected GDContainer container = null;



  /**
   * requests a new device of the specified size
   *
   * @param w width of the device
   * @param h height of the device
   */
  public abstract void open(double w, double h);

  /**
   * the device became active (current)
   */
  public void activate() {
    active = true;
  }

  /**
   * draw a circle
   *
   * @param x x coordinate of the center
   * @param y y coordinate of the center
   * @param r radius
   */
  public void circle(double x, double y, double r) {
    if (container == null) return;
    container.add(new GDCircle(x, y, r));
  }

  /**
   * clip drawing to the specified region
   *
   * @param x0 left coordinate
   * @param x1 right coordinate
   * @param y0 top coordinate
   * @param y1 bottom coordinate
   */
  public void clip(double x0, double x1, double y0, double y1) {
    if (container == null) {
      return;
    }
    container.add(new GDClip(x0, y0, x1, y1));
  }

  /**
   * close the display
   */
  public void close() {
    if (container != null) {
      container.closeDisplay();
    }
  }

  /**
   * the device became inactive (i.e. another device is now current)
   */
  public void deactivate() {
    active = false;
  }

  public boolean isActive() {
    return active;
  }


  public void hold() {
    // unimplemented - this call is now obsolete in R
  }

  /**
   * hold/flush
   *
   * @param flush if <code>false</code> then the device started holding and no
   *              updates should be shown on screen, if <code>true</code> then the device should
   *              flush right away and resume normal operation after than. Note that
   *              the flush must either be synchronous, or it must be guaranteed that
   *              shown content will be identical to the state up till now, otherwise
   *              the device will break animations.
   */
  public void flush(boolean flush) {
    holding = !flush;
    if (flush && container != null)
      container.syncDisplay(false);
  }

  /**
   * Block until the user selects a point from the canvas, or until
   * the operation is cancelled.
   *
   * @return array of indices or <code>null</code> is cancelled
   */
  public Ptr locator() {
    return DoublePtr.NULL;
  }

  /**
   * draw a line
   *
   * @param x1 x coordinate of the origin
   * @param y1 y coordinate of the origin
   * @param x2 x coordinate of the end
   * @param y2 y coordinate of the end
   */
  public void line(double x1, double y1, double x2, double y2) {
    if (container == null) {
      return;
    }
    container.add(new GDLine(x1, y1, x2, y2));
  }

  /**
   * retrieve font metrics info for the given unicode character
   *
   * @param ch character (encoding may depend on the font type)
   * @return an array consisting for three doubles: ascent, descent and width
   */
  public double[] metricInfo(int ch) {
    double[] res = new double[3];
    double ascent = 0.0;
    double descent = 0.0;
    double width = 8.0;

    if (container != null) {
      Graphics g = container.getGraphics();
      if (g != null) {
        Font f = container.getGState().getFont();
        if (f != null) {
          FontMetrics fm = g.getFontMetrics(container.getGState().getFont());
          if (fm != null) {
            ascent = (double) fm.getAscent();
            descent = (double) fm.getDescent();
            width = (double) fm.charWidth((ch == 0) ? 77 : ch);
          }
        }
      }
    }
    res[0] = ascent;
    res[1] = descent;
    res[2] = width;
    return res;
  }

  /**
   * R signalled a mode change
   *
   * @param mode mode as signalled by R (currently 0=R stopped drawing, 1=R started drawing, 2=graphical input exists)
   */
  public void mode(int mode) {
    if (!holding && container != null) container.syncDisplay(mode == 0);
  }

  /**
   * create a new, blank page (old API, not used anymore)
   */
  public void newPage() {
    if (container != null) container.reset();
  }

  /**
   * create a new, blank page
   *
   * @param devNr device number assigned to this device by R
   */
  public void newPage(int devNr) { // new API: provides the device Nr.
    this.deviceNumber = devNr;
    if (container != null) {
      container.reset();
      container.setDeviceNumber(devNr);
    }
  }

  /**
   * create multi-polygon path
   *
   * @param nper
   * @param x
   * @param y
   * @param winding : use winding rule (true) or odd-even rule (false)
   */
  public void path(int npoly, Ptr nper, Ptr x, Ptr y, boolean winding) {
    if (container == null) {
      return;
    }
    container.add(new GDPath(npoly, nper, x, y, winding));
  }

  public void polygon(int n, Ptr x, Ptr y) {
    if (container == null) {
      return;
    }
    container.add(new GDPolygon(n, x, y, false));
  }

  public void polyline(int n, Ptr x, Ptr y) {
    if (container == null) {
      return;
    }
    container.add(new GDPolygon(n, x, y, true));
  }

  public void rect(double x0, double y0, double x1, double y1) {
    if (container == null) {
      return;
    }
    container.add(new GDRect(x0, y0, x1, y1));
  }

  public void raster(byte[] image, int imageWidth, int imageHeight, double x, double y, double w, double h, double rot, boolean interpolate) {
    if (container == null) {
      return;
    }
    container.add(new GDRaster(image, imageWidth, imageHeight, x, y, w, h, rot, interpolate));
  }

  /**
   * retrieve the current size of the device
   *
   * @return an array of four doubles: 0, width, height, 0
   */
  public double[] size() {
    double[] res = new double[4];
    double width = 0d;
    double height = 0d;
    if (container != null) {
      Dimension d = container.getSize();
      width = d.getWidth();
      height = d.getHeight();
    }
    res[0] = 0d;
    res[1] = width;
    res[2] = height;
    res[3] = 0;
    return res;
  }

  /**
   * retrieve width of the given text when drawn in the current font
   *
   * @param str text
   * @return width of the text
   */
  public double strWidth(String str) {
    double width = (double) (8 * str.length()); // rough estimate
    if (container != null) { // if canvas is active, we can do better
      Graphics g = container.getGraphics();
      if (g != null) {
        Font f = container.getGState().getFont();
        if (f != null) {
          FontMetrics fm = g.getFontMetrics(f);
          if (fm != null) width = (double) fm.stringWidth(str);
        }
      }
    }
    return width;
  }

  /**
   * draw text
   *
   * @param x    x coordinate of the origin
   * @param y    y coordinate of the origin
   * @param str  text to draw
   * @param rot  rotation (in degrees)
   * @param hadj horizontal adjustment with respect to the text size (0=left-aligned wrt origin, 0.5=centered,
   *             1=right-aligned wrt origin)
   */
  public void text(double x, double y, String str, double rot, double hadj) {
    if (container == null) return;
    container.add(new GDText(x, y, rot, hadj, str));
  }

  /*-- GDC - manipulation of the current graphics state */

  /**
   * set drawing color
   *
   * @param cc color
   */
  public void setColor(int cc) {
    if (container == null) return;
    container.add(new GDColor(cc));
  }

  /**
   * set fill color
   *
   * @param cc color
   */
  public void setFill(int cc) {
    if (container == null) return;
    container.add(new GDFill(cc));
  }

  /**
   * set line width and type
   *
   * @param lwd line width (see <code>lwd</code> parameter in R)
   * @param lty line type (see <code>lty</code> parameter in R)
   */
  public void setLine(double lwd, int lty) {
    if (container == null) return;
    container.add(new GDLinePar(lwd, lty));
  }

  /**
   * set current font
   *
   * @param cex        character expansion (see <code>cex</code> parameter in R)
   * @param ps         point size (see <code>ps</code> parameter in R - for all practical purposes the
   *                   requested font size in points is <code>cex * ps</code>)
   * @param lineheight line height
   * @param fontface   font face (see <code>font</code> parameter in R: 1=plain, 2=bold, 3=italic,
   *                   4=bold-italic, 5=symbol)
   * @param fontfamily font family (see <code>family</code> parameter in R)
   */
  public void setFont(double cex, double ps, double lineheight, int fontface, String fontfamily) {
    if (container == null) return;
    GDFont f = new GDFont(cex, ps, lineheight, fontface, fontfamily);
    container.add(f);
    container.getGState().setFont(f.getFont());
  }

  /**
   * returns the device number
   *
   * @return device number or -1 is unknown
   */
  public int getDeviceNumber() {
    return (container == null) ? deviceNumber : container.getDeviceNumber();
  }

}