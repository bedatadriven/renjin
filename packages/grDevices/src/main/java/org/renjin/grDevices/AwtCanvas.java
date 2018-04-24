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

import org.renjin.eval.Session;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class AwtCanvas extends Canvas implements GDContainer, MouseListener {

  private static boolean forceAntiAliasing = true;
  private final Session session;

  private List<GDObject> list;
  private boolean listChanged;

  private GDState state;

  private Refresher refresher;

  private Dimension lastSize;

  public int deviceNumber = -1;

  public AwtCanvas(Session session, int w, int h) {
    this.session = session;
    list = new ArrayList<>();
    state = new GDState();
    state.font = new Font(null, 0, 12);
    setSize(w, h);
    lastSize = getSize();
    setBackground(Color.white);
    addMouseListener(this);
    (refresher = new Refresher(this)).start();
  }

  public GDState getGState() {
    return state;
  }

  public void setDeviceNumber(int dn) {
    deviceNumber = dn;
  }

  public int getDeviceNumber() {
    return deviceNumber;
  }

  public void closeDisplay() {
  }

  public synchronized void cleanup() {
    refresher.active = false;
    refresher.interrupt();
    reset();
    refresher = null;
    list = null;
  }

  public void syncDisplay(boolean finish) {
    repaint();
  }

  public void initRefresh() {

    FunctionCall function = FunctionCall.newCall(Symbol.get(":"),
        StringVector.valueOf("grDevices"),
        StringVector.valueOf(".javaGD.resize"));

    FunctionCall resizeCall = FunctionCall.newCall(function, IntVector.valueOf(deviceNumber));

    session.enqueueEvaluation(resizeCall);
  }

  public synchronized void add(GDObject o) {
    list.add(o);
    listChanged = true;
  }

  public synchronized void reset() {
    list.clear();
    listChanged = true;
  }

  LocatorSync lsCallback = null;

  public synchronized boolean prepareLocator(LocatorSync ls) {
    if (lsCallback != null && lsCallback != ls) // make sure we cause no deadlock
      lsCallback.triggerAction(null);
    lsCallback = ls;

    return true;
  }

  // MouseListener for the Locator support
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

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }


  long lastUpdate;
  long lastUpdateFinished;
  boolean updatePending = false;

  public void update(Graphics g) {
    if (System.currentTimeMillis() - lastUpdate < 200) {
      updatePending = true;
      if (System.currentTimeMillis() - lastUpdateFinished > 700) {
        g.setColor(Color.white);
        g.fillRect(0, 0, 250, 25);
        g.setColor(Color.blue);
        g.drawString("Building plot... (" + list.size() + " objects)", 10, 10);
        lastUpdateFinished = System.currentTimeMillis();
      }
      lastUpdate = System.currentTimeMillis();
      return;
    }
    updatePending = false;
    super.update(g);
    lastUpdateFinished = lastUpdate = System.currentTimeMillis();
  }

  class Refresher extends Thread {
    AwtCanvas c;
    boolean active;

    public Refresher(AwtCanvas c) {
      this.c = c;
    }

    public void run() {
      active = true;
      while (active) {
        try {
          Thread.sleep(300);
        } catch (Exception e) {
        }
        if (!active) {
          break;
        }
        if (c.updatePending && (System.currentTimeMillis() - lastUpdate > 200)) {
          c.repaint();
        }
      }
      c = null;
    }
  }

  @Override
  public synchronized void paint(Graphics g) {
    updatePending = false;
    Dimension d = getSize();
    if (!d.equals(lastSize)) {
      initRefresh();
      lastSize = d;
      return;
    }

    if (forceAntiAliasing) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    g.setFont(state.font);
    g.setClip(0, 0, d.width, d.height); // reset clipping rect

    for (GDObject gdObject : list) {
      gdObject.paint(this, state, g);
    }

    lastUpdate = System.currentTimeMillis();
  }
}
