package r.compiler.ir.tac;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import r.compiler.ir.tac.instructions.Statement;
import r.lang.Context;
import r.lang.SEXP;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class IRBlock {
  
  private Object temp[];
  private Statement statements[];
  private int labels[];
  
  public IRBlock(List<Statement> statements, Map<Label, Integer> labels, int tempCount) {
    this.statements = statements.toArray(new Statement[statements.size()]);
    this.labels = new int[labels.size()];
    this.temp = new Object[tempCount];
  
    Arrays.fill(this.labels, -1);
    
    for(Entry<Label,Integer> label : labels.entrySet()) {
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
    for(int i=0;i!=statements.length;++i) {
      sb.append(labelAt(i))
        .append(Strings.padEnd(i + ":", 4, ' '))
        .append(statements[i])
        .append("\n");
    }
    return sb.toString();
  }
}
