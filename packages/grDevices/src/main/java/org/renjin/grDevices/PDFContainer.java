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

public class PDFContainer implements GDContainer {

  @Override
  public void add(GDObject o) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void reset() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GDState getGState() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Graphics getGraphics() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean prepareLocator(LocatorSync ls) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void syncDisplay(boolean finish) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setDeviceNumber(int dn) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void closeDisplay() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int getDeviceNumber() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Dimension getSize() {
    throw new UnsupportedOperationException("TODO");
  }
}
