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
package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.TabExpander;
import java.awt.*;

public class RenjinToken extends Token {

  @Override
  public int getListOffset(RSyntaxTextArea textArea, TabExpander e, float x0,
      float x) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public float getWidthUpTo(int numChars, RSyntaxTextArea textArea,
      TabExpander e, float x0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Rectangle listOffsetToView(RSyntaxTextArea textArea, TabExpander e,
      int pos, int x0, Rectangle rect) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public float paint(Graphics2D g, float x, float y, RSyntaxTextArea host,
      TabExpander e, float clipStart) {
    // TODO Auto-generated method stub
    return 0;
  }

}
