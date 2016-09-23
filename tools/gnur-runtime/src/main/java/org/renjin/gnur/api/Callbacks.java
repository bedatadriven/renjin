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
// Initial template generated from Callbacks.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;
import org.renjin.sexp.SEXP;

@SuppressWarnings("unused")
public final class Callbacks {

  private Callbacks() { }



  public static boolean Rf_removeTaskCallbackByIndex(int id) {
    throw new UnimplementedGnuApiMethod("Rf_removeTaskCallbackByIndex");
  }

  public static boolean Rf_removeTaskCallbackByName(CharPtr name) {
    throw new UnimplementedGnuApiMethod("Rf_removeTaskCallbackByName");
  }

  public static SEXP R_removeTaskCallback(SEXP which) {
    throw new UnimplementedGnuApiMethod("R_removeTaskCallback");
  }

  // R_ToplevelCallbackEl* Rf_addTaskCallback (R_ToplevelCallback cb, void *data, void(*finalizer)(void *), const char *name, int *pos)
}
