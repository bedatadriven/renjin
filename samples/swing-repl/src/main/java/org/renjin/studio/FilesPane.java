package org.renjin.studio;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class FilesPane extends JTabbedPane {

  public FilesPane() {
    addTab("Files", new JScrollPane());
  }
}
