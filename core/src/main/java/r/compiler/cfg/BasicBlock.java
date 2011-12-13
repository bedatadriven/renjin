package r.compiler.cfg;

import java.util.List;

import r.compiler.ir.tac.IRBlock;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.instructions.IfStatement;
import r.compiler.ir.tac.instructions.ReturnStatement;
import r.compiler.ir.tac.instructions.Statement;

import com.google.common.collect.Lists;

public class BasicBlock {
  private final IRBlock parent;
  private int index;
  
  private IRLabel label;
  private List<Statement> statements = Lists.newArrayList();
  
  public BasicBlock(IRBlock parent) {
    super();
    this.parent = parent;
  }
  
  public void addStatement(Statement statement) {
    statements.add(statement);
  }
  
  public List<Statement> getStatements() {
    return statements;
  }
  
  public void setIndex(int index) {
    this.index = index;
  }

  public static BasicBlock createWithStartAt(IRBlock parent, int statementIndex) {
    BasicBlock block = new BasicBlock(parent);
    block.label = parent.getIntructionLabel(statementIndex);
    block.statements = Lists.newArrayList();
    block.statements.add(parent.getStatements().get(statementIndex));
    return block;
  }

  public IRLabel getLabel() {
    return label;
  }
  
  public boolean isLabeled() {
    return label != null;
  }
  
  public Statement getTerminal() {
    return statements.get(statements.size() - 1);
  }
  
  public boolean returns() {
    return getTerminal() instanceof ReturnStatement;
  }
  
  public boolean fallsThrough() {
    Statement terminal = getTerminal();
    return !( terminal instanceof GotoStatement ||
              terminal instanceof IfStatement ||
              terminal instanceof ReturnStatement);
  }
  
  public Iterable<IRLabel> targets() {
    return getTerminal().possibleTargets();
  }

  public String statementsToString() {
    StringBuilder sb = new StringBuilder();
    if(label != null) {
      sb.append(label).append("\n");
    }
    for(Statement statment : statements) {
      sb.append(statment).append("\n");
    }
    return sb.toString();
  }
  
  @Override
  public String toString() {
    return "BB" + index;
  }
  
}
