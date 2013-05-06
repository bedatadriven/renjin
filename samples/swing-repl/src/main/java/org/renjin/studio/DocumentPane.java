package org.renjin.studio;

import javax.swing.JTabbedPane;

public class DocumentPane extends JTabbedPane {

  public DocumentPane() {
    add("Test.R", new ScriptEditorFrame());
  }
}
