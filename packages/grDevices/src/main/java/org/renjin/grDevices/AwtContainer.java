/*

GDCanvas.java
Java Graphics Device

Created by Simon Urbanek on Thu Aug 05 2004.
Copyright (c) 2004-2009 Simon Urbanek. All rights reserved.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation;
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

*/

package org.renjin.grDevices;

import java.awt.*;

public class AwtContainer implements GDContainer {

  private AwtPanel panel;
  private GDState state;
  private Dimension size;

  private int deviceNumber = -1;

  public AwtContainer(int w, int h) {
    this.size = new Dimension(w, h);
    this.state = new GDState();
    this.state.setFont(new Font(null, 0, 12));

    panel = new AwtPanel(size);
    panel.setBackground(Color.white);
    panel.setForceAntiAliasing(true);
  }

  @Override
  public GDState getGState() {
    return state;
  }

  @Override
  public Graphics getGraphics() {
    return panel.getGraphics();
  }

  @Override
  public void setDeviceNumber(int deviceNumber) {
    this.deviceNumber = deviceNumber;
  }

  @Override
  public int getDeviceNumber() {
    return deviceNumber;
  }

  @Override
  public Dimension getSize() {
    return size;
  }

  @Override
  public void closeDisplay() {
    // No action required, frame closed by AwtDevice
  }

  @Override
  public void syncDisplay(boolean finish) {
    panel.setRepaintPaused(!finish);
  }

  @Override
  public synchronized void add(GDObject o) {
    panel.paint(o, state);
  }

  @Override
  public synchronized void reset() {
    panel.reset();
  }

  public AwtPanel getPanel() {
    return panel;
  }
}
