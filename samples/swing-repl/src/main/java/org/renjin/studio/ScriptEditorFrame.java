package org.renjin.studio;

import java.awt.BorderLayout;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.RTextScrollPane;

public class ScriptEditorFrame extends JPanel {

  public ScriptEditorFrame() {
    super(new BorderLayout());
    
    final RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
    textArea.setCodeFoldingEnabled(true);
    textArea.setAntiAliasingEnabled(true);
    textArea.addKeyListener(new KeyAdapter() {

      @Override
        public void keyPressed(KeyEvent ke ) {  
        if(ke.getKeyCode() == KeyEvent.VK_ENTER && 
            ke.getModifiers() == KeyEvent.CTRL_MASK)  {  
          System.out.println("Execute: " + textArea.getSelectedText());  
        }
      }
    });
    RTextScrollPane sp = new RTextScrollPane(textArea);
    sp.setFoldIndicatorEnabled(true);
    sp.setIconRowHeaderEnabled(true);
    
    Gutter gutter = sp.getGutter();
    
    try {
      Icon icon = new ImageIcon(ImageIO.read(getClass().getResource("/breakpoint.png")));
      GutterIconInfo info = gutter.addLineTrackingIcon(0, icon);
      
    
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    add(sp);
  
    JButton saveButton = new JButton("Save");
    
    JButton runButton = new JButton("Run");
    
    JToolBar toolBar = new JToolBar();
    toolBar.add(saveButton);
    toolBar.add(runButton);
    
    add(toolBar, BorderLayout.PAGE_START);
  
  }
}
