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
package org.renjin.graphics;

import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;


/*
 *  R allows there to be multiple devices in
 *  existence at the same time.  Only one device is the
 *  active device and all drawing occurs in this device
 *
 *  Each device has its own set of graphics parameters
 *  so that switching between devices, switches between
 *  their graphical contexts (e.g., if you set the line
 *  width on one device then switch to another device,
 *  don't expect to be using the line width you just set!)
 *
 */
public class GraphicsDevices {

  private List<GraphicsDevice> devices = Lists.newArrayList();
  private GraphicsDevice active;

  public boolean isEmpty() {
    return devices.isEmpty();
  }

  public GraphicsDevice getActive() {
    if(active == null) {
      throw new IllegalStateException("No current device");
    }
    return active;
  }

  public void setActive(GraphicsDevice device) {
    this.active = device;
    if(!devices.contains(device)) {
      devices.add(device);
    }
  }

}
