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
