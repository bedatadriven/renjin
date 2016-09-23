/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.graphics;

public class TextStyle {
  private double fontSizeFactor = 1.0;
  private Color color = Color.BLACK;
  private String fontName;
  
  public TextStyle() {
  }
  
  public TextStyle(double fontSizeFactor, Color color) {
    super();
    this.fontSizeFactor = fontSizeFactor;
    this.color = color;
  }
  
  public double getFontSizeFactor() {
    return fontSizeFactor;
  }

  public void setFontSizeFactor(double fontSizeFactor) {
    this.fontSizeFactor = fontSizeFactor;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }
  
  @Override
  public TextStyle clone() {
    return new TextStyle(fontSizeFactor, color);
  }
}
