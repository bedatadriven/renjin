package r.compiler.ir.tac;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import r.compiler.ir.tac.statements.Statement;
import r.lang.Context;
import r.lang.SEXP;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class IRScope {
  
  private Object temp[];
  private Statement statements[];
  private int labels[];
  
  public IRScope(List<Statement> statements, Map<IRLabel, Integer> labels, int tempCount) {
    this.statements = statements.toArray(new Statement[statements.size()]);
    this.labels = new int[labels.size()];
    this.temp = new Object[tempCount];
  
    Arrays.fill(this.labels, -1);
    
    for(Entry<IRLabel,Integer> label : labels.entrySet()) {
        this.labels[label.getKey().getIndex()] = label.getValue();
    }
  }

  public List<Statement> getStatements() {
    return Lists.newArrayList(statements);
  }

  public SEXP evaluate(Context context) {
    int i=0;
    while(i < statements.length) {
      Object result = statements[i].interpret(context, temp);
      if(result == null) {
        i++;
      } else if(result instanceof IRLabel) {
        i = labels[ ((IRLabel)result).getIndex() ];
      } else {
        return (SEXP)result;
      }
    }
    return null;
  }

  public int getLabelInstructionIndex(IRLabel label) {
    return labels[label.getIndex()];
  }
  
  public IRLabel getIntructionLabel(int instructionIndex) {
    for(int i=0;i!=labels.length;++i) {
      if(labels[i]==instructionIndex) {
        return new IRLabel(i);
      }
    }
    return null;
  }
 
  public boolean isLabeled(int instructionIndex) {
    for(int i=0;i!=labels.length;++i) {
      if(labels[i] == instructionIndex) {
        return true;
      }
    }
    return false;
  }
  
  private String labelAt(int instructionIndex) {
    IRLabel label = getIntructionLabel(instructionIndex);
    
    return label == null ? Strings.repeat(" ", 5) :
      Strings.padEnd(label.toString(), 5, ' ');
  } 
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i!=statements.length;++i) {
      appendLineTo(sb, i);
    }
    return sb.toString();
  }

  public void appendLineTo(StringBuilder sb, int i) {
    sb.append(labelAt(i))
      .append(Strings.padEnd(i + ":", 4, ' '))
      .append(statements[i])
      .append("\n");
  }
}
