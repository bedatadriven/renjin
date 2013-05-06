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

package org.renjin.studio;


import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.apache.commons.vfs2.FileSystemException;
import org.renjin.eval.Context;
import org.renjin.studio.console.Console;
import org.renjin.studio.console.ConsoleFrame;
import org.renjin.studio.console.Repl;


public class MainFrame extends JFrame {

  private ConsoleFrame console;
  private DocumentPane documentTab;
  private WorkspacePane workspaceTab;
  private FilesPane filesTab;
  
  public MainFrame() {
    super("Renjin Studio");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    console = new ConsoleFrame();
    documentTab = new DocumentPane();
    workspaceTab = new WorkspacePane();
    filesTab = new FilesPane();
    
    JSplitPane westVertPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        documentTab, console);
    westVertPane.setResizeWeight(0.50);
    
    JSplitPane eastVertPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        workspaceTab, filesTab);
    eastVertPane.setResizeWeight(0.50);
    
    JSplitPane horizPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
        westVertPane, eastVertPane);
    horizPane.setResizeWeight(0.50);  
    
    add(horizPane, BorderLayout.CENTER);
    
    setupMenu();
    
    setSize(650, 450);
    setVisible(true);
  }

  private Console getConsole() {
    return console;
  }

  public static void main(String[] args) throws FileSystemException {

    loadNativeLookAndFeel();

    MainFrame mainFrame = new MainFrame();
    
    Context topLevelContext = Context.newTopLevelContext();
    topLevelContext.getSession().setStdOut(new PrintWriter(mainFrame.getConsole().getOut()));
    //topLevelContext.getGlobals().setSessionController(new CliSessionController(mainFrame.));

    Repl interpreter = new Repl( mainFrame.getConsole(), topLevelContext );
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
