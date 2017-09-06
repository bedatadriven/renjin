/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.grDevices;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gnur.api.DevDesc;
import org.renjin.gnur.api.GEContext;
import org.renjin.gnur.api.GraphicsEngine;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class Devices {

  private static double jGDdpiX = 100.0;
  private static double jGDdpiY = 100.0;
  private static double jGDasp  = 1.0;

  public static boolean newJavaGD_Open(DevDesc dd, BytePtr dsp, double w, double h, double ps) {

    JGraphicsDevice xd = new JGraphicsDevice(w, h, ps);

    //  xd = Rf_allocNewJavaGDDeviceDesc(initps);
    // newJavGD_Open
    //Rf_setNewJavaGDDeviceData((NewDevDesc*)(dd), 0.6, xd);


    /*	Set up Data Structures. */
    //  setupJavaGDfunctions(dd);

    /* Set required graphics parameters. */

    /* Window Dimensions in Pixels */
    /* Initialise the clipping rect too */

    dd.setLeft(0);
    dd.setClipLeft(0);

    dd.setRight(xd.getWindowWidth());
    dd.setClipRight(xd.getWindowWidth());

    dd.setBottom(xd.getWindowHeight());
    dd.setClipBottom(xd.getWindowHeight());

    dd.setTop(0);
    dd.setClipTop(0);

    /* Nominal Character Sizes in Pixels */

    dd.cra[0] = 8;
    dd.cra[1] = 11;

    /* Character Addressing Offsets */
    /* These are used to plot a single plotting character */
    /* so that it is exactly over the plotting point */

    dd.xCharOffset = 0.4900;
    dd.yCharOffset = 0.3333;
    dd.yLineBias = 0.1;

    /* Inches per raster unit */

    dd.ipr[0] = 1/jGDdpiX;
    dd.ipr[1] = 1/jGDdpiY;

    dd.canClip = 1;
    dd.canHAdj = 2;
    dd.canChangeGamma = 0;

    dd.startps = xd.getBaseFontSize();
    dd.startcol = xd.getCol();
    dd.startfill = xd.getFill();
    dd.startlty = GraphicsEngine.LTY_SOLID;
    dd.startfont = 1;
    dd.startgamma = 0.6;

    dd.deviceSpecific =  xd;

    dd.displayListOn = 1;

    dd.newPage = findMethodHandle("newPage");

    return true;
  }

  private static MethodHandle findMethodHandle(String name) {
    for (Method method : Devices.class.getMethods()) {
      if(method.getName().equals(name)) {
        try {
          return MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException("Could not access method " + method, e);
        }
      }
    }
    throw new IllegalStateException("No such method: " + name);
  }

  public static void newPage(GEContext context, DevDesc device) {
    ((JGraphicsDevice) device.deviceSpecific).newPage(context, device);
  }

}
