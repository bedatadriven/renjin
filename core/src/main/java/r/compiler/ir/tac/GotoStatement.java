package r.compiler.ir.tac;

public class GotoStatement implements Statement {

  private final Label target;

  public GotoStatement(Label target) {
    this.target = target;
  }

  public Label getTarget() {
    return target;
  }

  @Override
  public String toString() {
    return "goto " + target;
  } 
}
