/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.desktop;


import r.interpreter.Console;
import r.interpreter.Interpreter;

import javax.swing.*;
import java.awt.*;


public class MainFrame extends JFrame {

  JConsole console;

  public MainFrame()
  {
    super("Renjin");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JDesktopPane desktop = new JDesktopPane();
    desktop.setBackground(new Color(171, 171, 171));
    
    JInternalFrame internalFrame = new JInternalFrame("R Console", true, true, true, true);
    console = new JConsole();

    internalFrame.add(console, BorderLayout.CENTER);
    internalFrame.setBounds(25, 25, 600, 300);
    internalFrame.setVisible(true);

    desktop.add(internalFrame);

    add(desktop, BorderLayout.CENTER);

    setSize(650, 450);
    setVisible(true);
  }

  private Console getConsole() {
    return console;
  }

  public static void main(String[] args) {

    loadNativeLookAndFeel();

    MainFrame mainFrame = new MainFrame();

    Interpreter interpreter = new Interpreter( mainFrame.getConsole() );
    new Thread ( interpreter ).start();
  }

  private static void loadNativeLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
