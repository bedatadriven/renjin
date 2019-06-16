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
// Initial template generated from Rinterface.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.annotations.GlobalVar;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.primitives.io.connections.Connection;

@SuppressWarnings("unused")
public final class Rinterface {

  private Rinterface() { }

  @GlobalVar
  public static Connection R_Consolefile() {
    throw new UnimplementedGnuApiMethod("R_Consolefile");
  }

  public static void R_RestoreGlobalEnv() {
    throw new UnimplementedGnuApiMethod("R_RestoreGlobalEnv");
  }

  public static void R_RestoreGlobalEnvFromFile(BytePtr p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("R_RestoreGlobalEnvFromFile");
  }

  public static void R_SaveGlobalEnv() {
    throw new UnimplementedGnuApiMethod("R_SaveGlobalEnv");
  }

  public static void R_SaveGlobalEnvToFile(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("R_SaveGlobalEnvToFile");
  }

  public static void R_FlushConsole() {
    throw new UnimplementedGnuApiMethod("R_FlushConsole");
  }

  public static void R_ClearerrConsole() {
    throw new UnimplementedGnuApiMethod("R_ClearerrConsole");
  }

  public static void R_Suicide(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("R_Suicide");
  }

  public static BytePtr R_HomeDir() {
    throw new UnimplementedGnuApiMethod("R_HomeDir");
  }

  public static void R_setupHistory() {
    throw new UnimplementedGnuApiMethod("R_setupHistory");
  }

  public static void Rf_jump_to_toplevel() {
    throw new UnimplementedGnuApiMethod("Rf_jump_to_toplevel");
  }

  public static void Rf_mainloop() {
    throw new UnimplementedGnuApiMethod("Rf_mainloop");
  }

  public static void Rf_onintr() {
    throw new UnimplementedGnuApiMethod("Rf_onintr");
  }

  public static void process_site_Renviron() {
    throw new UnimplementedGnuApiMethod("process_site_Renviron");
  }

  public static void process_system_Renviron() {
    throw new UnimplementedGnuApiMethod("process_system_Renviron");
  }

  public static void process_user_Renviron() {
    throw new UnimplementedGnuApiMethod("process_user_Renviron");
  }

  public static void R_setStartTime() {
    throw new UnimplementedGnuApiMethod("R_setStartTime");
  }

  public static void fpu_setup(boolean p0) {
    throw new UnimplementedGnuApiMethod("fpu_setup");
  }
}
