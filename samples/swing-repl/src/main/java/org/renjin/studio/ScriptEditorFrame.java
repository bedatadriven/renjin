package org.renjin.studio;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class ScriptEditorFrame extends JPanel {

  public ScriptEditorFrame() {

    RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
    textArea.setCodeFoldingEnabled(true);
    textArea.setAntiAliasingEnabled(true);
    
    RTextScrollPane sp = new RTextScrollPane(textArea);
    sp.setFoldIndicatorEnabled(true);
  
    add(sp, BorderLayout.CENTER);
  }
}
