/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.studio.console;

import java.io.PrintStream;
import java.io.Reader;

/**
 The capabilities of a minimal console for R.
 Stream I/O and optimized print for output.

 @author  @author Patrick Niemeyer (pat@pat.net)

 */
public interface Console {
  public Reader getIn();
  public PrintStream getOut();
  public PrintStream getErr();
  public void println( Object o );
  public void print( Object o );
  public void error( Object o );
  public int getCharactersPerLine();
}
