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

import org.renjin.sexp.SEXP;

/**
 * Common class for the _GEDevDesc class
 */
public class GEDevDesc {

  public static final int MAX_GRAPHICS_SYSTEMS = 24;

  /**
   * Stuff that the devices can see (and modify).
   * All detailed in GraphicsDevice.h
   */

  public DevDesc dev;

  /*
   * Stuff about the device that only the graphics engine sees
   * (the devices don't see it).
   */

  /**
   *  toggle for display list status
   */
  public int displayListOn;

  /**
   *  display list
   */
  public SEXP displayList;

  /**
   *  A pointer to the end of the display list to avoid tranversing pairlists
   */
  public SEXP DLlastElt;

  /* The last element of the display list
   * just prior to when the display list
   * was last initialised
   */
  public SEXP savedSnapshot;

  /**
   *  Has the device received any output?
   */
  public int dirty;

  /**
   * Should a graphics call be stored
   * on the display list?
   * Set to FALSE by do_recordGraphics,
   * do_dotcallgr, and do_Externalgr
   * so that nested calls are not
   * recorded on the display list
   */
  public int recordGraphics;


  /**
   * Stuff about the device that only graphics systems see.
   * The graphics engine has no idea what is in here.
   * Used by graphics systems to store system state per device.
   */
  public GESystemDesc[] gesd = new GESystemDesc[MAX_GRAPHICS_SYSTEMS];

  /**
   * per-device setting for 'ask' (use NewFrameConfirm)
   */
  public int ask;

}
