/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.graphics.device;

import org.renjin.graphics.GraphicParameters;
import org.renjin.graphics.GraphicsDeviceDriver;
import org.renjin.graphics.geom.Dimension;
import org.renjin.graphics.geom.Rectangle;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * Graphics device using the java.awt toolkit for
 * drawing
 */
public class AwtGraphicsDevice implements GraphicsDeviceDriver {

  private final Graphics2D g2d;
  private org.renjin.graphics.geom.Dimension size;

  public AwtGraphicsDevice(Graphics2D g2d, org.renjin.graphics.geom.Dimension sizeInches) {
    this.g2d = g2d;
    this.size = sizeInches;
  }
  
  public AwtGraphicsDevice(Graphics2D g2d) {
    this.g2d = g2d;
    java.awt.Rectangle bounds = g2d.getDeviceConfiguration().getBounds();    
    size = new org.renjin.graphics.geom.Dimension(bounds.getWidth() / 72d, bounds.getHeight() / 72d);
  }

  @Override
  public void drawRectangle(org.renjin.graphics.geom.Rectangle rect, 
                            org.renjin.graphics.Color fillColor, 
                            org.renjin.graphics.Color borderColor,
                            GraphicParameters parameters) {
    
    Rectangle2D shape = new Rectangle2D.Double(rect.getX1(), rect.getY1(), rect.getWidth(), rect.getHeight());

    if(!fillColor.isTransparent()) {
      g2d.setPaint(toAwtColor(fillColor));
      g2d.fill(shape);
    }

    if(!borderColor.isTransparent()) {
      g2d.setColor(toAwtColor(borderColor));
      g2d.setStroke(currentStroke(parameters));
      g2d.draw(shape);
    }
  }
  
  private Stroke currentStroke(GraphicParameters parameters) {
    double lwd = parameters.getLineWidth();
    return new BasicStroke((float) lwd);
  }

  private java.awt.Color toAwtColor(org.renjin.graphics.Color color) {
    return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
  }

  @Override
  public Dimension getInchesPerPixel() {
    Rectangle bounds = getDeviceRegion();
    return new org.renjin.graphics.geom.Dimension(
        size.getWidth() / bounds.getWidth(),
        size.getHeight() / bounds.getHeight());
  }

  @Override
  public Dimension getCharacterSize() {
    return new Dimension(10.8, 14.4);
  }

  @Override
  public Rectangle getDeviceRegion() {
    java.awt.Rectangle bounds = g2d.getDeviceConfiguration().getBounds();
    return new Rectangle(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
  }
  
}
