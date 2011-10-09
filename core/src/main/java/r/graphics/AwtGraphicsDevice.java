package r.graphics;

import r.lang.graphics.GraphicsDevice;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Graphics device using the java.awt toolkit for
 * drawing
 */
public class AwtGraphicsDevice extends GraphicsDevice {

  private Graphics2D g2d;

  public AwtGraphicsDevice(Graphics2D g2d) {
    this.g2d = g2d;
  }

  @Override
  public void setPlotWindow(double x1, double y1, double x2, double y2) {

    Rectangle deviceBounds = g2d.getDeviceConfiguration().getBounds();

    AffineTransform tx = new AffineTransform();
    tx.scale(deviceBounds.getWidth() / (x2-x1), -deviceBounds.getHeight() / (y2-y1));
    tx.translate(-x1, -y2);

    g2d.setTransform(tx);
  }

  @Override
  public void drawRectangle(double xleft, double ybottom, double xright, double ytop, r.lang.graphics.Color fillColor, r.lang.graphics.Color borderColor) {
    Rectangle2D rect = new Rectangle2D.Double(xleft, ybottom, (xright-xleft), (ytop-ybottom));

    if(!fillColor.isTransparent()) {
      g2d.setPaint(toAwtColor(fillColor));
      g2d.fill(rect);
    }

    if(!borderColor.isTransparent()) {
      g2d.setColor(toAwtColor(borderColor));
      g2d.setStroke(currentStroke());
      g2d.draw(rect);
    }
  }
  
  private Stroke currentStroke() {
    // lwd is actually in device coordinates, so we need to "unscale"
    // before passing to AWT
    double lwd = getParameters().getLineWidth();
    lwd /= g2d.getTransform().getScaleX();
    return new BasicStroke((float) lwd);
  }

  private java.awt.Color toAwtColor(r.lang.graphics.Color color) {
    return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
  }
  
}
