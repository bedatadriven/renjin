package org.renjin.compiler.cfg;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRFunctionTable;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;

public class CfgExplorer {


  public static void main(String[] args) throws MalformedURLException, IOException {

    //showCfg("y <- 0; for(i in 1:10) y <- y + i; sqrt(y + 3 * x)");
    // showCfg("y<-1; if(q) y<-y+1 else y<-4; y");
    showCfg(Resources.toString(Resources.getResource(CfgExplorer.class, "cytron.R"), Charsets.UTF_8));
  }


  private static void showCfg(String rcode) {

    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    IRFunctionTable functionTable = new IRFunctionTable();
    IRBody block = new IRBodyBuilder(functionTable).build(ast);

    System.out.println(block);
    
    final ControlFlowGraph cfg = new ControlFlowGraph(block);
   
    System.out.println(cfg);
    
    
    JFrame frame = new JFrame("Simple Graph View");
    frame.setVisible(true);       
    
    // The Layout<V, E> is parameterized by the vertex and edge types
    CfgLayout layout = new CfgLayout(cfg, frame.getGraphics());
    
    BasicVisualizationServer<BasicBlock,Edge> vv = 
        new BasicVisualizationServer<BasicBlock,Edge>(layout.getLayout());
    vv.getRenderContext().setVertexLabelTransformer(layout.getLabelTransformer());
    vv.getRenderContext().setVertexShapeTransformer(layout.getVertexShapeTransformer());
    vv.getRenderContext().setVertexFillPaintTransformer(layout.getVertexFillTransformer());
    vv.getRenderer().setVertexLabelRenderer(layout.getVertexLabelRenderer());
    
    vv.setPreferredSize(layout.getSize()); //Sets the viewing area size

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JScrollPane scrollPane = new JScrollPane(vv);
    
    
    frame.getContentPane().add(scrollPane); 
    frame.pack();
  }
}
