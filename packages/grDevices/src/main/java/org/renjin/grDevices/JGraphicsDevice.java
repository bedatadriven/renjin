/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.grDevices;

import org.renjin.gnur.api.DevDesc;
import org.renjin.gnur.api.GEContext;

/**
 * Java base class for implementing an R graphics device
 */
public class JGraphicsDevice {

  private double windowWidth;
  private double windowHeight;

  private int fontface = -1;
  private int fontsize = -1;
  private int basefontface = 1;
  private double basefontsize;
  private int fill;
  private int col;
  private int canvas;
  private int holdlevel;

  public JGraphicsDevice(double windowWidth, double windowHeight, double ps) {
    if (ps < 6 || ps > 24) {
      ps = 12;
    }

    this.basefontsize = ps;

    this.windowWidth = windowWidth;
    this.windowHeight = windowHeight;

    this.fill = 0xffffffff; /* transparent, was R_RGB(255, 255, 255); */
    this.col = 0;
    this.canvas = 0xffffff;
    this.holdlevel = 0;
  }

  public double getWindowWidth() {
    return windowWidth;
  }

  public double getWindowHeight() {
    return windowHeight;
  }

  public double getBaseFontSize() {
    return basefontface;
  }

  public int getFontface() {
    return fontface;
  }

  public int getFontsize() {
    return fontsize;
  }

  public int getBasefontface() {
    return basefontface;
  }

  public double getBasefontsize() {
    return basefontsize;
  }

  public int getFill() {
    return fill;
  }

  public int getCol() {
    return col;
  }

  public int getCanvas() {
    return canvas;
  }

  public int getHoldlevel() {
    return holdlevel;
  }

  public void newPage(GEContext context, DevDesc device) {

  }
}
