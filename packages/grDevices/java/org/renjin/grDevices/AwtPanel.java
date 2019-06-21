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

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwtPanel extends Panel {

  private final int bufferWidth;
  private final int bufferHeight;

  private boolean forceAntiAliasing;

  private transient Image bufferImage;
  private transient Graphics bufferGraphics;

  private transient boolean paused = false;

  /**
   * Keep track of whether a repaint is already scheduled.
   *
   * <p>Painting of the buffer image will take place in the AWT event loop thread,
   * not on the main Renjin thread.</p>
   */
  private AtomicBoolean pendingPaint = new AtomicBoolean(false);

  AwtPanel(Dimension size) {
    setSize(size);
    bufferWidth = (int) size.getWidth();
    bufferHeight = (int) size.getHeight();
  }

  public void setForceAntiAliasing(boolean forceAntiAliasing) {
    this.forceAntiAliasing = forceAntiAliasing;
  }

  private void initBuffer() {
    bufferImage = createImage(bufferWidth, bufferHeight);
    if(bufferImage == null) {
      throw new IllegalStateException("null buffer image");
    }
    bufferGraphics = bufferImage.getGraphics();

    if (forceAntiAliasing && bufferGraphics instanceof Graphics2D) {
      Graphics2D g2d = (Graphics2D) bufferGraphics;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    resetBuffer();
  }

  @Override
  public void update(Graphics g) {
    // Do not clear panel before painting
    paint(g);
  }

  /**
   * This method will be called by the AWT event thread.
   *
   */
  @Override
  public void paint(Graphics g) {
    pendingPaint.set(false);
    g.drawImage(bufferImage, 0, 0,  this);
  }

  public void paint(GDObject o, GDState state) {
    if(bufferImage == null) {
      initBuffer();
    }

    o.paint(this, state, bufferGraphics);
    if(!paused) {
      scheduleRepaint();
    }
  }

  public Graphics getBufferGraphics() {
    return bufferGraphics;
  }

  public void reset() {
    resetBuffer();
    scheduleRepaint();
  }

  /**
   * Schedules a repaint of this panel, if there is not already an outstanding request to repaint.
   */
  private void scheduleRepaint() {
    if(pendingPaint.compareAndSet(false, true)) {
      repaint();
    }
  }

  private void resetBuffer() {
    if(bufferGraphics != null) {
      bufferGraphics.clearRect(0, 0, bufferWidth, bufferHeight);
    }
  }

  public void setRepaintPaused(boolean paused) {
    this.paused = paused;
    if(!paused) {
      scheduleRepaint();
    }
  }
}