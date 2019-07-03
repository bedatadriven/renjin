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

import org.renjin.eval.Session;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.sexp.ListVector;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * JavaGD is an implementation of the {@link GraphicsDevice} protocol which displays the R graphics in
 * an AWT window (via {@link AwtContainer}). It can be used as an example for implementing custom display classes
 * which can then be used by JavaGD. Three sample back-ends are included in the JavaGD sources: {@link AwtContainer} (AWT)
 */
public class AwtDevice extends GraphicsDevice implements WindowListener {

  /**
   * frame containing the graphics canvas
   */
  private Frame frame;

  private final LocatorSync locator = new LocatorSync();

  /**
   * default, public constructor - creates a new JavaGD instance.
   * The actual window (and canvas) is not created until {@link #open} is called.
   */
  public AwtDevice(Session session, ListVector options) {
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
    if (frame != null) {
      close();
    }

    AwtContainer awtContainer = new AwtContainer((int)w, (int)h);
    awtContainer.getPanel().addMouseListener(locator);

    this.container = awtContainer;

    frame = new Frame("Renjin");
    frame.setSize(new Dimension((int)w, (int)h));
    frame.setResizable(false);
    frame.add(awtContainer.getPanel());
    frame.setVisible(true);
    frame.addWindowListener(this);
  }

  @Override
  public void activate() {
    super.activate();
    if (frame != null) {
      frame.requestFocus();
      frame.setTitle("Renjin " + ((deviceNumber > 0) ? ("(" + (deviceNumber + 1) + ")") : "") + " *active*");
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
      frame.setTitle("Renjin " + ((deviceNumber > 0) ? ("(" + (deviceNumber + 1) + ")") : ""));
    }
  }

  @Override
  public void newPage(int deviceNumber) { // new API: provides the device Nr.
    super.newPage(deviceNumber);
    if (frame != null) {
      frame.setTitle("Renjin (" + (deviceNumber + 1) + ")" + (isActive() ? " *active*" : ""));
    }
  }

  @Override
  public Ptr locator() {
    return locator
        .waitForClick()
        .map(p -> new DoublePtr(p.getX(), p.getX()))
        .orElse(DoublePtr.NULL);
  }

  /*-- WindowListener interface methods */

  /**
   * listener response to "Close" - effectively invokes <code>dev.off()</code> on the device
   */
  @Override
  public void windowClosing(WindowEvent e) {
    // TODO: execute dev.off() in the enclosing session
  }

  @Override
  public void windowClosed(WindowEvent e) {
    // No action
  }

  @Override
  public void windowOpened(WindowEvent e) {
    // No action
  }

  @Override
  public void windowIconified(WindowEvent e) {
    // No action
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
    // No action
  }

  @Override
  public void windowActivated(WindowEvent e) {
    // No action
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
    // No action
  }

}
