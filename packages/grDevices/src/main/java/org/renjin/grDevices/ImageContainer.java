/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
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

import org.renjin.eval.EvalException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageContainer implements GDContainer {

  private final Graphics2D graphics;
  private final GDState state;
  private final Dimension size;
  private int deviceNumber;
  private final BufferedImage bufferedImage;
  private final String filename;
  private String formatName;

  public ImageContainer(String filename, String formatName, int width, int height) {
    this.filename = filename;
    this.formatName = formatName;
    this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    this.size = new Dimension(width, height);
    this.graphics = bufferedImage.createGraphics();
    this.state = new GDState();
  }

  @Override
  public void add(GDObject o) {
    o.paint(null, state, graphics);
  }

  @Override
  public void reset() {
  }

  @Override
  public GDState getGState() {
    return state;
  }

  @Override
  public Graphics getGraphics() {
    return graphics;
  }

  @Override
  public boolean prepareLocator(LocatorSync ls) {
    return false;
  }

  @Override
  public void syncDisplay(boolean finish) {
  }

  @Override
  public void setDeviceNumber(int dn) {
    this.deviceNumber = dn;
  }

  @Override
  public void closeDisplay() {
    try {
      formatName = "png";
      ImageIO.write(bufferedImage, formatName, new File(filename));
    } catch (IOException e) {
      throw new EvalException("Failed to write image: " + e.getMessage(), e);
    }
  }

  @Override
  public int getDeviceNumber() {
    return deviceNumber;
  }

  @Override
  public Dimension getSize() {
    return size;
  }
}
