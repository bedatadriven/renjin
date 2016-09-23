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
package org.renjin.graphics.internals;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.graphics.GraphicsDevice;
import org.renjin.graphics.GraphicsDevices;
import org.renjin.graphics.geom.Point;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.DataParallel;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Recycle;


public class Graphics {

  public static final int DEVICE = 1;
  public static final int NDC = 2;
  public static final int NIC = 7;
  public static final int NFC = 8;
  public static final int USER = 13;
  public static final int INCHES = 14;
  public static final int NPC = 17;

  @Internal
  @DataParallel
  public static double grconvertX(@Current Context context, @Recycle double x, int from, int to) {
    return grConvert(context, new Point(x, Double.NaN), from, to).getX();
  }

  @Internal
  @DataParallel
  public static double grconvertY(@Current Context context, @Recycle double y, int from, int to) {
    return grConvert(context, new Point(Double.NaN, y), from, to).getY();
  }
 
  private static Point grConvert(Context context, Point point, int from, int to) {
    GraphicsDevice active = context.getSession().getSingleton(GraphicsDevices.class).getActive();
    Point device = toDeviceCoordinates(active, point, from);
    return fromDeviceCoordinates(active, device, to);
  }

  private static Point toDeviceCoordinates(GraphicsDevice active, Point point, int from) {
    switch(from) {
    case USER:
      return active.userToDevice(point);
    default:
      throw new EvalException("Invalid 'from' argument");
    }
  }
  
  private static Point fromDeviceCoordinates(GraphicsDevice active, Point point, int to) {
    switch(to) {
    case DEVICE:
       return point;
    default:
      throw new EvalException("Invalid 'to' argument");
    }
  }
  
}
