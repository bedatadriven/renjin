/*
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
package org.renjin.gnur.api;


public class GEContext {

  /*
   * Colours
   *
   * NOTE:  Alpha transparency included in col & fill
   */

  /**
   *  pen colour (lines, text, borders, ...)
   */
  public int col;

  /**
   * fill colour (for polygons, circles, rects, ...)
   */
  public int fill;

  /**
   *  Gamma correction
   */
  public double gamma;

  /*
   * Line characteristics
   */
  public double lwd;          /* Line width (roughly number of pixels) */

  /**
   *  Line type (solid, dashed, dotted, ...)
   */
  public int lty;

  /**
   *  Line end
   */
  public int /*R_GE_lineend */ lend;


  /**
   *  line join
   */
  public int /* R_GE_linejoin */ ljoin;

  /**
   *  line mitre
   */
  public double lmitre;


  /*
   * Text characteristics
   */

  /**
   *  Character expansion (font size = fontsize*cex)
   */
  public double cex;

  /**
   *  Font size in points
   */
  public double ps;

  /**
   *  Line height (multiply by font size)
   */
  public double lineheight;

  /**
   *  Font face (plain, italic, bold, ...)
   */
  public int fontface;

  /**
   *  Font family
   */
  public byte fontfamily[] = new byte[201];
}
