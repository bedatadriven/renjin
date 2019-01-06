/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.hash.Hashing;
import org.renjin.repackaged.guava.io.Files;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class SvgContainer implements GDContainer {

  private static final String RENJINCI_PLOT_DIR = System.getenv("RENJINCI_PLOT_DIR");

  private final SVGGraphics2D graphics;
  private final GDState state;
  private final Dimension size;
  private int deviceNumber;
  private final Session session;
  private final String filenameFormat;

  private boolean empty = true;

  private int pageNumber = 1;

  public SvgContainer(Session session, String filenameFormat, int width, int height, Color backgroundColor) {
    super();
    this.session = session;
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

    try {

      if(!Strings.isNullOrEmpty(RENJINCI_PLOT_DIR)) {
        flushCiObject(svg);
      }

      String filename = String.format(filenameFormat, pageNumber++);
      FileObject fileObject = session.getFileSystemManager().resolveFile(filename);

      try(Writer writer = new OutputStreamWriter(fileObject.getContent().getOutputStream(), StandardCharsets.UTF_8)) {
        writer.write(svg);
      }
    } catch (IOException e) {
      throw new EvalException("Failed to write SVG file: " + e.getMessage(), e);
    }
  }

  private void flushCiObject(String svg) throws IOException {
    String hashCode = Hashing.sha256().hashString(svg, StandardCharsets.UTF_8).toString();

    File plotDir = new File(RENJINCI_PLOT_DIR);
    if(!plotDir.exists()) {
      boolean created = plotDir.mkdirs();
      if(!created) {
        throw new IOException("Could not create directory " + plotDir.getAbsolutePath());
      }
    }

    File plotFile = new File(plotDir, hashCode + ".svg");

    Files.write(svg, plotFile, StandardCharsets.UTF_8);

    session.getStdOut().println("<<<<plot:" + hashCode + ".svg>>>>");
    session.getStdOut().flush();
  }
}
