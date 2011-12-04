package r.compiler.ir.tac;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Strings;

import r.compiler.ir.tac.instructions.Statement;
import r.lang.Context;
import r.lang.SEXP;

public class IRBlock {
  
  private List<Node> nodes;
  private Object temp[];
  private Statement statements[];
  private int statementCount;
  private int labels[];
  
  public IRBlock(List<Node> nodes, int tempCount) {
    this.nodes = nodes;
    this.statements = new Statement[nodes.size()];
    this.labels = new int[nodes.size()];
    this.temp = new Object[tempCount];
  
    Arrays.fill(labels, -1);
    
    int stmtIndex=0;
    for(Node node : nodes) {
      if(node instanceof Statement) {
        statements[stmtIndex++] = (Statement)node;
      } else if(node instanceof Label) {
        Label label = (Label)node;
        labels[label.getIndex()] = stmtIndex;
      }
    }
    statementCount = stmtIndex;
  }

  public List<Node> getStatements() {
    return nodes;
  }

  public SEXP evaluate(Context context) {
    int i=0;
    while(i < statementCount) {
      Object result = statements[i].interpret(context, temp);
      if(result == null) {
        i++;
      } else if(result instanceof Label) {
        i = labels[ ((Label)result).getIndex() ];
      } else {
        return (SEXP)result;
      }
    }
    return null;
  }
  
  private String labelAt(int instructionIndex) {
    for(int i=0;i!=labels.length;++i) {
      if(labels[i]==instructionIndex) {
        return Strings.padEnd("L" + i, 5, ' ');
      }
    }
    return Strings.repeat(" ", 5);
  }
  
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i!=statementCount;++i) {
      sb.append(labelAt(i))
        .append(Strings.padEnd(i + ":", 4, ' '))
        .append(statements[i])
        .append("\n");
    }
    return sb.toString();
  }
}
