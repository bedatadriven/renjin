package org.renjin.studio;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.SEXP;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class ScriptEditorFrame extends JPanel {

  private RSyntaxTextArea textArea;
  private StudioSession session;


  public ScriptEditorFrame(StudioSession session) {
    super(new BorderLayout());
    
    this.session = session;
    
 // RSyntaxDocument doc = new RSyntaxDocument(new RenjinTokenMakerFactory(), RenjinTokenMakerFactory.SYNTAX_STYLE_R);
    
    RSyntaxDocument doc = new RSyntaxDocument(TokenMakerFactory.getDefaultInstance(), "text/plain");
    
    textArea = new RSyntaxTextArea(doc, null, 20, 60);
    textArea.setCodeFoldingEnabled(true);
    textArea.setAntiAliasingEnabled(true);
    textArea.setText("f<-function() { \n\t42\n}");
    textArea.addKeyListener(new KeyAdapter() {

      @Override
        public void keyPressed(KeyEvent ke ) {  
        if(ke.getKeyCode() == KeyEvent.VK_ENTER && 
            ke.getModifiers() == KeyEvent.CTRL_MASK)  {
          try {
            executeSelection();
          } catch (IOException e) {

          }
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
    runButton.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          executeSelection();
        } catch (IOException e) {
          e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
      }
    });
    
    JToolBar toolBar = new JToolBar();
    toolBar.add(saveButton);
    toolBar.add(runButton);
    
    add(toolBar, BorderLayout.PAGE_START);
  
  }
  

  private void executeSelection() throws IOException {
    RParser parser = new RParser(new StringReader(textArea.getSelectedText()));
    List<SEXP> expressions = Lists.newArrayList();
    while(parser.parse()) {
      expressions.add(parser.getResult());
    }
    
    ExpressionVector vector = new ExpressionVector(expressions);
    session.evaluate(vector);
    
    
  }
}
