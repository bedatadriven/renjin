/*

JavaGD - Java Graphics Device for R
JavaGD.java - default GDInterface implementation for use in JavaGD

Copyright (C) 2004-2009  Simon Urbanek

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

import org.renjin.sexp.ListVector;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * JavaGD is an implementation of the {@link GraphicsDevice} protocol which displays the R graphics in
 * an AWT window (via {@link GDCanvas}). It can be used as an example for implementing custom display classes
 * which can then be used by JavaGD. Three sample back-ends are included in the JavaGD sources: {@link GDCanvas} (AWT)
 */
public class JavaGD extends GraphicsDevice implements WindowListener {

  /**
   * frame containing the graphics canvas
   */
  private Frame frame;

  /**
   * default, public constructor - creates a new JavaGD instance. The actual window (and canvas) is not created until {@link #open} is called.
   */
  public JavaGD(ListVector options) {
    super();
  }

  /**
   * creates a new graphics window containing a canvas
   *
   * @param w width of the canvas
   * @param h height of the canvas
   */
  @Override
  public void open(double w, double h) {
    super.open(w, h);

    if (frame != null) {
      close();
    }

    frame = new Frame("JavaGD");
    frame.addWindowListener(this);
    container = new GDCanvas(w, h);
    frame.add((GDCanvas) container);
    frame.pack();
    frame.setVisible(true);
  }

  @Override
  public void activate() {
    super.activate();
    if (frame != null) {
      frame.requestFocus();
      frame.setTitle("JavaGD " + ((deviceNumber > 0) ? ("(" + (deviceNumber + 1) + ")") : "") + " *active*");
    }
  }

  @Override
  public void close() {
    super.close();
    if (frame != null) {
      container = null;
      frame.removeAll();
      frame.dispose();
      frame = null;
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();
    if (frame != null) {
      frame.setTitle("JavaGD " + ((deviceNumber > 0) ? ("(" + (deviceNumber + 1) + ")") : ""));
    }
  }

  @Override
  public void newPage(int deviceNumber) { // new API: provides the device Nr.
    super.newPage(deviceNumber);
    if (frame != null) {
      frame.setTitle("JavaGD (" + (deviceNumber + 1) + ")" + (isActive() ? " *active*" : ""));
    }
  }

  /*-- WindowListener interface methods */

  /**
   * listener response to "Close" - effectively invokes <code>dev.off()</code> on the device
   */
  public void windowClosing(WindowEvent e) {
    if (container != null) executeDevOff();
  }

  public void windowClosed(WindowEvent e) {
  }

  public void windowOpened(WindowEvent e) {
  }

  public void windowIconified(WindowEvent e) {
  }

  public void windowDeiconified(WindowEvent e) {
  }

  public void windowActivated(WindowEvent e) {
  }

  public void windowDeactivated(WindowEvent e) {
  }

}
