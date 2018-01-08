/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.studio;


import org.apache.commons.vfs2.FileSystemException;
import org.renjin.studio.console.Console;
import org.renjin.studio.console.ConsoleFrame;
import org.renjin.studio.console.Repl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;


public class MainFrame extends JFrame {

  private ConsoleFrame console;
  private DocumentPane documentTab;
  private WorkspacePane workspaceTab;
  private FilesPane filesTab;
  private StudioSession session;
  
  public MainFrame(StudioSession session) {
    super("Renjin Studio");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    this.session = new StudioSession();
    
    console = new ConsoleFrame();
    session.setStdOut(new PrintWriter(console.getOut()));
    
    documentTab = new DocumentPane(session);
    workspaceTab = new WorkspacePane();
    filesTab = new FilesPane();
    
//    JSplitPane westVertPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//        documentTab, console);
//    westVertPane.setResizeWeight(0.50);
//
//    JSplitPane eastVertPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//        workspaceTab, filesTab);
//    eastVertPane.setResizeWeight(0.50);
//
//    JSplitPane horizPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
//        westVertPane, eastVertPane);
//    horizPane.setResizeWeight(0.50);
//
//    add(horizPane, BorderLayout.CENTER);
    
    add(console, BorderLayout.CENTER);
    
    //setupMenu();
    
    setSize(650, 450);
    setVisible(true);
  }

  private Console getConsole() {
    return console;
  }

  public static void main(String[] args) throws FileSystemException {

    loadNativeLookAndFeel();

    StudioSession session = new StudioSession();
    
    MainFrame mainFrame = new MainFrame(session);
    

    Repl interpreter = new Repl( mainFrame.getConsole(), session );
    new Thread ( interpreter ).start();

  
  }

  private static void loadNativeLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void setupMenu() {
    //Create the menu bar.
    JMenuBar menuBar = new JMenuBar();

    //Build the first menu.
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_A);
    menuBar.add(fileMenu);
    
    JMenu newMenu = new JMenu("New");
    newMenu.setMnemonic(KeyEvent.VK_N);
    
    JMenuItem newScriptItem = new JMenuItem("R Script");
    newMenu.add(newScriptItem);
    
    fileMenu.add(newMenu);
    
    setJMenuBar(menuBar);
  }
}
