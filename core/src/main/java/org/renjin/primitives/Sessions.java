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
package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.SessionController.SaveMode;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;

/**
 * Implementation of interactive session related primitives like q(), interactive(),
 * menu(), etc
 * 
 */
public class Sessions {

  private Sessions() {}


  @Internal
  public static void quit(@Current Context context, String saveMode, int exitCode, boolean runLast) {
    context.getSession().getSessionController().quit(context, SaveMode.valueOf(saveMode.toUpperCase()), exitCode, runLast);
  }
  
  /**
   * @return  TRUE when R is being used interactively and FALSE otherwise.
   */
  @Builtin
  public static boolean interactive(@Current Context context) {
    return context.getSession().getSessionController().isInteractive();
  }

  @Internal
  public static String readline(@Current Context context, String prompt) {
    return context.getSession().getSessionController().readLine(prompt);
  }
}
