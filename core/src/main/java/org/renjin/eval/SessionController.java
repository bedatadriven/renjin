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
package org.renjin.eval;

import org.renjin.sexp.StringVector;

import java.io.IOException;


/**
 * Provides implementations for the session-oriented R commands
 * like quit(), browse(), etc.
 *
 * <p>Proper implementations need to be provided by the host environment</p>
 */
public class SessionController {
  
  public enum SaveMode {
    NO,
    YES,
    ASK,
    DEFAULT
  }

  public void quit(Context context, SaveMode saveMode, int exitCode, boolean runLast ) {
    
  }
  
  public boolean isInteractive() {
    return false;
  }

  public int menu(StringVector choices) throws IOException {
    throw new EvalException("menu() is not available");
  }

  /**
   * 
   * @return true if this session's input and output are connected to a proper terminal.
   * (As opposed to a file or GUI console
   */
  public boolean isTerminal() {
    return false;
  }
}
