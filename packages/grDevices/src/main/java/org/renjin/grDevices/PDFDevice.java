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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.renjin.eval.EvalException;
import org.renjin.sexp.ListVector;

import java.io.IOException;

public class PDFDevice extends GraphicsDevice {

  private PDDocument document;

  public PDFDevice(ListVector deviceOptions) {

  }

  @Override
  public void open(double w, double h) {
    super.open(w, h);
    document = new PDDocument();
  }

  @Override
  public void close() {
    super.close();
    try {
      document.close();
    } catch (IOException e) {
      throw new EvalException("Exception closing PDF Document", e);
    }
  }
}
