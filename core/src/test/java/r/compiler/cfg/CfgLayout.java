package r.compiler.cfg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.instructions.Statement;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel;

public class CfgLayout  {

  // Could be greatly improved.
  // See http://ssw.jku.at/General/Staff/TW/Wuerthinger06Bachelor.pdf
  
  private ControlFlowGraph cfg;
  private Dimension size;

  private static final int VERTEX_SPACING = 40;
  private static final int VERTEX_PADDING = 10;

  private class Pos {
    private int row = 0;
    private int col = -1;
    private Point2D point;
    private List<String> lines;
  }

  private Map<BasicBlock, Pos> blockPos = Maps.newHashMap();

  private int numRows;
  private int numCols;

  private int rowHeights[];
  private int colWidths[];

  private Graphics graphics;


  public CfgLayout(ControlFlowGraph cfg, Graphics graphics) {
    super();
    this.cfg = cfg;
    this.graphics = graphics;


    for(BasicBlock bb : cfg.getGraph().getVertices()) {
      blockPos.put(bb, new Pos());
    }

    calcRow(cfg.getEntry(), 0);
    calcCols();
    createText();
    calcRowHeights();
    calcColWidths();
    calcPos();
    
    for(BasicBlock bb : cfg.getGraph().getVertices()) {
      Pos pos = blockPos.get(bb);
      System.out.println("row=" + pos.row + ", col=" + pos.col + ", " + pos.point);
    }
    
    calcSize();
  }

  private void createText() {
    for(BasicBlock bb : cfg.getGraph().getVertices()) {
      Pos pos = blockPos.get(bb);
      pos.lines = Lists.newArrayList();
      for(Statement stmt : bb.getStatements()) {
        // skip final goto statements
        if( !(stmt instanceof GotoStatement) ) {
          pos.lines.add(stmt.toString());
        }
      }
    }
  }

  private void calcRow(BasicBlock bb, int depth) {

    if(depth+1 > numRows) {
      numRows = depth+1;
    }

    Pos pos = blockPos.get(bb);
    if(depth > pos.row) {
      pos.row = depth;
    }

    for(Edge edge : cfg.getGraph().getOutEdges(bb)) {
      if(!edge.isBackEdge()) {
        calcRow(cfg.getGraph().getOpposite(bb, edge), depth+1);
      }
    }
  }

  private void calcRowHeights() {
    rowHeights = new int[numRows];
    for(Pos pos : blockPos.values()) {

      int height = Math.max(1,pos.lines.size()) * graphics.getFontMetrics().getHeight() +
          VERTEX_PADDING * 2;
      if(height > rowHeights[pos.row]) {
        rowHeights[pos.row] = height;
      }
    }
  }

  private void calcColWidths() {
    colWidths = new int[numCols];
    for(Pos pos : blockPos.values()) {
      for(String line : pos.lines) {
        int width = graphics.getFontMetrics().stringWidth(line) +
            VERTEX_PADDING * 2;

        if(width > colWidths[pos.col]) {
          colWidths[pos.col] = width;
        }
      }
    }
  }

  private void calcCols() {
    for(BasicBlock bb : cfg.getGraph().getVertices()) {
      Pos pos = blockPos.get(bb);
      pos.col = lastColAtRow(pos.row) + 1;
      if(pos.col + 1 > numCols) {
        numCols = pos.col + 1;
      }
    }
  }

  private int lastColAtRow(int row) {
    int lastCol = -1;
    for(Pos pos : blockPos.values()) {
      if(pos.row == row && pos.col > lastCol) {
        lastCol = pos.col;
      }
    }
    return lastCol;
  }

  private void calcPos() {
    for(Pos pos : blockPos.values()) {
      int x = VERTEX_SPACING;
      int y = VERTEX_SPACING;
      for(int row=0;row < pos.row; ++row) {
        y += rowHeights[row] + VERTEX_SPACING;
      }
      for(int col=0;col < pos.col; ++col) {
        x += colWidths[col] + VERTEX_SPACING;
      }
      pos.point = new Point2D.Double(
          x + colWidths[pos.col]/2, 
          y + rowHeights[pos.row]/2);
    }
  }

  private void calcSize() {
    int width = VERTEX_SPACING;
    int height = VERTEX_SPACING;
    for(int row=0;row < rowHeights.length; ++row) {
      height += rowHeights[row] + VERTEX_SPACING;
    }
    for(int col=0;col < colWidths.length; ++col) {
      width += colWidths[col] + VERTEX_SPACING;
    }    
    this.size = new Dimension(width, height);
  }
  

  public Dimension getSize() {
    return size;
  }

  public Transformer<BasicBlock, Paint> getVertexFillTransformer() {
    return new Transformer<BasicBlock, Paint>() {

      @Override
      public Paint transform(BasicBlock bb) {
        if(bb == cfg.getEntry()) {
          return Color.GREEN;
        } else if(bb == cfg.getExit()) {
          return Color.RED;
        } else {
          return Color.WHITE;
        }
      }   
    };
  }

  private class PosTransformer implements Transformer<BasicBlock, Point2D> {

    @Override
    public Point2D transform(BasicBlock bb) {
      return blockPos.get(bb).point;
    }
  }

  public Layout<BasicBlock, Edge> getLayout() {
    return new StaticLayout<BasicBlock, Edge>(cfg.getGraph(), new PosTransformer(), size);
  }

  public Transformer<BasicBlock, String> getLabelTransformer() {
    return new Transformer<BasicBlock, String>() {

      @Override
      public String transform(BasicBlock bb) {
        return Joiner.on("\n").join(blockPos.get(bb).lines);
      }
    };
  }

  public Transformer<BasicBlock, Shape> getVertexShapeTransformer() {
    return new Transformer<BasicBlock, Shape>() {
      @Override
      public Shape transform(BasicBlock bb) {
        Pos pos = blockPos.get(bb);
        return new Rectangle(
            -colWidths[pos.col] / 2, -rowHeights[pos.row]/2,
            colWidths[pos.col], rowHeights[pos.row]);
      }
    };
  }

  public VertexLabel<BasicBlock, Edge> getVertexLabelRenderer() {
    return new VertexLabel<BasicBlock, Edge>() {

      @Override
      public void setPositioner(Positioner arg0) {
        // TODO Auto-generated method stub

      }

      @Override
      public void setPosition(Position arg0) {
        // TODO Auto-generated method stub

      }

      @Override
      public void labelVertex(RenderContext<BasicBlock, Edge> context,
          Layout<BasicBlock, Edge> layout, BasicBlock bb, String label) {
        Point2D point = layout.transform(bb);     

        context.getGraphicsContext().setColor(Color.BLACK);
        
        Rectangle rect = (Rectangle) context.getVertexShapeTransformer().transform(bb);
        rect.translate((int)point.getX(), (int)point.getY());
        
        int y = (int)rect.getMinY() + VERTEX_PADDING + 
            context.getGraphicsContext().getFontMetrics().getAscent();
        
        for(String line : blockPos.get(bb).lines) {
          context.getGraphicsContext().drawString(line, 
              (int)(rect.getMinX() + VERTEX_PADDING), y);
          
          y+= context.getGraphicsContext().getFontMetrics().getHeight();
        }
        
      }

      @Override
      public Positioner getPositioner() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Position getPosition() {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }
}