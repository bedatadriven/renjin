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
package org.renjin.gnur.api;

import java.lang.invoke.MethodHandle;

public class GESystemDesc {
  /** An array of information about each graphics system that
   * has registered with the graphics engine.
   * This is used to store graphics state for each graphics
   * system on each device.
   */
  public Object systemSpecific;

  /*
   * An array of function pointers, one per graphics system that
   * has registered with the graphics engine.
   *
   * system_Callback is called when the graphics engine wants
   * to give a graphics system the chance to play with its
   * device-specific information (stored in systemSpecific)
   * There are two parameters:  an "event" to tell the graphics
   * system why the graphics engine has called this function,
   * and the systemSpecific pointer.  The graphics engine
   * has to pass the systemSpecific pointer because only
   * the graphics engine will know what array index to use.
   */
  public MethodHandle callback;
}
