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
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AwtContainer implements GDContainer, MouseListener {

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

  LocatorSync lsCallback = null;

  @Override
  public synchronized boolean prepareLocator(LocatorSync ls) {
    if (lsCallback != null && lsCallback != ls) // make sure we cause no deadlock
      lsCallback.triggerAction(null);
    lsCallback = ls;

    return true;
  }

  // MouseListener for the Locator support
  @Override
  public void mouseClicked(MouseEvent e) {
    if (lsCallback != null) {
      double[] pos = null;
      if ((e.getModifiers() & InputEvent.BUTTON1_MASK) > 0 && (e.getModifiers() & (InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) == 0) { // B1 = return position
        pos = new double[2];
        pos[0] = (double) e.getX();
        pos[1] = (double) e.getY();
      }

      // pure security measure to make sure the trigger doesn't mess with the locator sync object
      LocatorSync ls = lsCallback;
      lsCallback = null; // reset the callback - we'll get a new one if necessary
      ls.triggerAction(pos);
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    // No action
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    // No action
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    // No action
  }

  @Override
  public void mouseExited(MouseEvent e) {
    // No action
  }

  public AwtPanel getPanel() {
    return panel;
  }
}
