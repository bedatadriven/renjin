package org.renjin.studio;

import javax.swing.JTabbedPane;

public class DocumentPane extends JTabbedPane {

  public DocumentPane(StudioSession session) {
    add("Test.R", new ScriptEditorFrame(session));
  }
}
