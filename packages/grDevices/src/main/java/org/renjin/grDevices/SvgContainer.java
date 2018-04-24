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

import org.apache.pdfbox.util.Charsets;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.renjin.eval.EvalException;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SvgContainer implements GDContainer {

  private final SVGGraphics2D graphics;
  private final GDState state;
  private final Dimension size;
  private int deviceNumber;
  private final String filenameFormat;

  private boolean empty = true;

  private int pageNumber = 1;

  public SvgContainer(String filenameFormat, int width, int height, Color backgroundColor) {
    super();
    this.filenameFormat = filenameFormat;
    this.size = new Dimension(width, height);
    this.graphics = new SVGGraphics2D(width, height);
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

  @Override
  public int getDeviceNumber() {
    return deviceNumber;
  }

  @Override
  public Dimension getSize() {
    return size;
  }

  public void flush() {
    String svg = graphics.getSVGDocument();

    String filename = String.format(filenameFormat, pageNumber++);
    File to = new File(filename);
    try {
      org.renjin.repackaged.guava.io.Files.write(svg, to, Charsets.UTF_8);
    } catch (IOException e) {
      throw new EvalException("Exception writing to " + to.getAbsolutePath(), e);
    }
  }
}
