/*

LocatorSync.java
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * a simple synchronization class that can be used by a separate thread to wake JavaGD from waiting for a
 * locator result.
 */
public class LocatorSync implements MouseListener {

  private static final Point CANCELLED = new Point();

  private final BlockingQueue<Point> points = new ArrayBlockingQueue<>(10);

  private boolean waiting = false;

  /**
   * Waits for a mouse a click
   */
  public synchronized Optional<Point> waitForClick() {
    points.clear();
    waiting = true;

    try {
      Point point = points.take();
      if(point == CANCELLED) {
        return Optional.empty();
      } else {
        return Optional.of(point);
      }
    } catch (InterruptedException e) {
      // Mark this thread as interrupted and
      // and return NULL to signal that it was cancelled
      Thread.currentThread().interrupt();
      return Optional.empty();
    } finally {
      waiting = false;
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if(waiting) {
      if(e.getButton() == MouseEvent.BUTTON1) {
        // Offer will return false if the queue is full, but that
        // it is ok because that means we already have our event.
        points.offer(e.getPoint());

      } else {

        // Clicking the right button cancels the sequence.
        // We pass a specific instance to signal the cancellation
        points.offer(CANCELLED);
      }
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
}
