//
//  JGDBufferedPanel.java
//  JGR
//
//  Created by Simon Urbanek on Sun Aug 29 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation;
//  version 2.1 of the License.
//  
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//

package org.renjin.grDevices;

import java.awt.*;

public class JGDBufferedPanel extends JGDPanel {
    public long lastSyncAttempt, lastPaint;
    public long syncDelay = 200;
    public long refreshGranularity = 400;
    
    public boolean updateDelayed = false;
    public boolean updateLocked = false;
    public Refresher ref;
    
    public JGDBufferedPanel(double w, double h) {
        this((int)w, (int)h);
    }

    public JGDBufferedPanel(int w, int h) {
        super(w, h);
        (ref = new Refresher(this)).start();
    }        

    public void superPC(Graphics g) {
        super.paintComponent(g);
    }

    public void closeDisplay() { super.closeDisplay(); if (ref!=null) ref.active=false; ref=null; }

    public synchronized void syncDisplay(boolean finish) {
        //System.out.println("Sync("+finish+")");
        if (!finish) {
            lastSyncAttempt=System.currentTimeMillis();
            updateLocked=true;
            return;
        }

        updateLocked=false;
        if (System.currentTimeMillis()-lastSyncAttempt>syncDelay) {
            //System.out.println("Sync allowed, ("+(System.currentTimeMillis()-lastSyncAttempt)+" ms)");
            repaint();
            updateDelayed=false;
        } else updateDelayed=true;
        lastSyncAttempt=System.currentTimeMillis();
    }
    
    public synchronized void paintComponent(Graphics g) {
        //System.out.println("BP: paint");
        superPC(g);
        lastPaint=lastSyncAttempt=System.currentTimeMillis();        
    }
    
    class Refresher extends Thread {
        JGDBufferedPanel c;
        boolean active;

        public Refresher(JGDBufferedPanel c) {
            this.c=c;
        }

        public void run() {
            active=true;
            while (active) {
                try {
                    Thread.sleep(refreshGranularity);
                } catch (Exception e) {}
                if (!active) break;
                //System.out.println("BP: Refresher: delayed="+c.updateDelayed+", locked="+c.updateLocked+", delta="+(System.currentTimeMillis()-c.lastSyncAttempt));
                if (c.updateDelayed && !c.updateLocked && (System.currentTimeMillis()-c.lastSyncAttempt>c.syncDelay))
                    c.syncDisplay(true);
            }
            c=null;
        }
    }
    
}
