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

import org.apache.commons.vfs2.FileObject;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Container for writing a raster images to files.
 */
public class ImageContainer implements GDContainer {

  private final GDState state;
  private final Dimension size;
  private int deviceNumber;
  private final BufferedImage bufferedImage;
  private final Session session;
  private final String filenameFormat;
  private String formatName;

  private Graphics2D graphics;
  private boolean empty = true;

  private int pageNumber = 1;

  public ImageContainer(Session session, String filenameFormat, String formatName, Color backgroundColor, int width, int height) {
    this.session = session;
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
  public void syncDisplay(boolean finish) {
    // No action: we do not have a display
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
      String filename = String.format(filenameFormat, pageNumber++);
      FileObject fileObject = session.getFileSystemManager().resolveFile(filename);

      try(OutputStream outputStream = fileObject.getContent().getOutputStream()) {
        ImageIO.write(bufferedImage, formatName, outputStream);
      }
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
