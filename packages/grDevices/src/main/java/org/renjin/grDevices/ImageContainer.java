/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Native;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ImageContainer implements GDContainer {

  private final GDState state;
  private final Dimension size;
  private int deviceNumber;
  private final BufferedImage bufferedImage;
  private final String filenameFormat;
  private String formatName;

  private Graphics2D graphics;
  private boolean empty = true;

  private int pageNumber = 1;

  public ImageContainer(String filenameFormat, String formatName, Color backgroundColor, int width, int height) {
    this.filenameFormat = filenameFormat;
    this.formatName = formatName;
    this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    this.size = new Dimension(width, height);
    this.graphics = bufferedImage.createGraphics();
    this.graphics.setBackground(backgroundColor);
    this.graphics.clearRect(0, 0, (int)size.getWidth(), (int)size.getHeight());
    this.state = new GDState();
  }

  @Override
  public void add(GDObject o) {
    empty = false;
    o.paint(null, state, graphics);
  }

  @Override
  public void reset() {
    if(!empty) {
      flush();
    }
    graphics = new SVGGraphics2D((int)size.getWidth(), (int)size.getHeight());
    graphics.clearRect(0, 0, (int) size.getWidth(), (int) size.getHeight());
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
    flush();
  }

  private void flush() {
    try {
      formatName = "png";
      PrintWriter stdOut = Native.currentContext().getSession().getStdOut();

      String filename = String.format(filenameFormat, pageNumber++);
      File outputFile = new File(filename).getAbsoluteFile();
      if(!outputFile.getParentFile().exists() || !outputFile.getParentFile().isDirectory()) {
        throw new EvalException("could not open file '" + outputFile.getAbsolutePath() + "'");
      }

      stdOut.println("Writing to " + filename) ;
      stdOut.flush();
      ImageIO.write(bufferedImage, formatName, outputFile);
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
