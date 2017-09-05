//
//  JGDPanel.java
//  JGR
//
//  Created by Simon Urbanek on Thu Aug 05 2004.
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
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.lang.reflect.Method;

public class JGDPanel extends JPanel implements GDContainer, MouseListener {
    Vector l;
    boolean listChanged;
    public static boolean forceAntiAliasing=true;
    GDState gs;
    Dimension lastSize;
    public int devNr=-1;
    Dimension prefSize;

    public JGDPanel(double w, double h) {
        this((int)w, (int)h);
    }

    public JGDPanel(int w, int h) {
        super(true);
        setOpaque(true);
        setSize(w, h);
        prefSize=new Dimension(w,h);
        l=new Vector();
        gs=new GDState();
        gs.f=new Font(null,0,12);
        setSize(w,h);
        lastSize=getSize();
	addMouseListener(this);
        setBackground(Color.white);
    }

    public GDState getGState() { return gs; }

    public void setDeviceNumber(int dn) { devNr=dn; }
    public int getDeviceNumber() { return devNr; }
    public void closeDisplay() {}
    
    public synchronized void cleanup() {
        reset();
        l=null;
    }

    LocatorSync lsCallback=null;

    public synchronized boolean prepareLocator(LocatorSync ls) {
	if (lsCallback!=null && lsCallback!=ls) // make sure we cause no deadlock
	    lsCallback.triggerAction(null);
	lsCallback=ls;
	
	return true;
    }

    // MouseListener for the Locator support
    public void mouseClicked(MouseEvent e) {
	if (lsCallback!=null) {
	    double[] pos = null;
	    if ((e.getModifiers()&InputEvent.BUTTON1_MASK)>0 && (e.getModifiers()&(InputEvent.BUTTON2_MASK|InputEvent.BUTTON3_MASK))==0) { // B1 = return position
		pos = new double[2];
		pos[0] = (double)e.getX();
		pos[1] = (double)e.getY();
	    }

	    // pure security measure to make sure the trigger doesn't mess with the locator sync object
	    LocatorSync ls=lsCallback;
	    lsCallback=null; // reset the callback - we'll get a new one if necessary
	    ls.triggerAction(pos);
	}
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void initRefresh() {
        //System.out.println("resize requested");
        try { // for now we use no cache - just pure reflection API for: Rengine.getMainEngine().eval("...")
            Class c=Class.forName("org.rosuda.JRI.Rengine");
            if (c==null)
                System.out.println(">> can't find Rengine, automatic resizing disabled. [c=null]");
            else {
                Method m=c.getMethod("getMainEngine",null);
                Object o=m.invoke(null,null);
                if (o!=null) {
                    Class[] par=new Class[1];
                    par[0]=Class.forName("java.lang.String");
                    m=c.getMethod("eval",par);
                    Object[] pars=new Object[1];
                    pars[0]="try(JavaGD:::.javaGD.resize("+devNr+"),silent=TRUE)";
                    m.invoke(o, pars);
                }
            }
        } catch (Exception e) {
            System.out.println(">> can't find Rengine, automatic resizing disabled. [x:"+e.getMessage()+"]");
        }
    }

    public void syncDisplay(boolean finish) {
        repaint();
    }
    
    public synchronized Vector getGDOList() { return l; }

    public synchronized void add(GDObject o) {
        l.add(o);
        listChanged=true;
    }

    public synchronized void reset() {
        l.removeAllElements();
        listChanged=true;
    }

    public Dimension getPreferredSize() {
        return new Dimension(prefSize);
    }
    
    public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension d=getSize();
        if (!d.equals(lastSize)) {
            initRefresh();
            lastSize=d;
            return;
        }

        if (forceAntiAliasing) {
            Graphics2D g2=(Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        int i=0, j=l.size();
        g.setFont(gs.f);
        g.setClip(0,0,d.width,d.height); // reset clipping rect
        g.setColor(Color.white);
        g.fillRect(0,0,d.width,d.height);
        while (i<j) {
            GDObject o=(GDObject) l.elementAt(i++);
            o.paint(this, gs, g);
        }
    }
    
}
