package org.renjin.studio;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class WorkspacePane extends JTabbedPane {

  public WorkspacePane() {
    addTab("Workspace", new JScrollPane());
    addTab("History", new JScrollPane());
  }
}
