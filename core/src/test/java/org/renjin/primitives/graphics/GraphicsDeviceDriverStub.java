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
package org.renjin.primitives.graphics;

import org.renjin.graphics.Color;
import org.renjin.graphics.GraphicParameters;
import org.renjin.graphics.GraphicsDeviceDriver;
import org.renjin.graphics.geom.Dimension;
import org.renjin.graphics.geom.Rectangle;


public class GraphicsDeviceDriverStub implements GraphicsDeviceDriver {

  private Rectangle deviceRegion;
  private Dimension size;
  
  public GraphicsDeviceDriverStub(Rectangle deviceRegion, Dimension size) {
    super();
    this.deviceRegion = deviceRegion;
    this.size = size;
  }

  public GraphicsDeviceDriverStub(int widthPixels, int heightPixels) {
    deviceRegion = new Rectangle(0, widthPixels, 0, heightPixels);
    size = new Dimension(widthPixels / 72d, heightPixels / 72d);
  }

  @Override
  public Dimension getInchesPerPixel() {
    return new Dimension(size.getWidth() / deviceRegion.getWidth(), 
                         size.getHeight() / deviceRegion.getHeight());
  }

  @Override
  public Dimension getCharacterSize() {
    return new Dimension(10.8, 14.4);
  }

  @Override
  public void drawRectangle(Rectangle bounds, Color fillColor,
      Color borderColor, GraphicParameters parameters) {
      
    
  }

  @Override
  public Rectangle getDeviceRegion() {
    return deviceRegion;
  }

  public Dimension getSize() {
    return size;
  }
  
}
