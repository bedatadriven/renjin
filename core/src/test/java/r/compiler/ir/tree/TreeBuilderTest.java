package r.compiler.ir.tree;

import java.util.List;

import org.junit.Test;

import r.compiler.cfg.BasicBlock;
import r.compiler.ir.ssa.SsaVariable;
import r.compiler.ir.tac.statements.Statement;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.ElementAccess;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.LocalVariable;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.compiler.ir.tac.expressions.Temp;
import r.compiler.ir.tac.statements.Assignment;

public class TreeBuilderTest {
  
  @Test
  public void trees() {
    
//    n₃ ← τ₄[Λ0₂]
//    τ₅ ← primitive<->(n₃, 1.0)
//    τ₆ ← primitive<*>(τ₅, xbar₃)
//    τ₇ ← primitive<[>(x₀, n₃)
//    τ₈ ← primitive<+>(τ₆, τ₇)
//    xbar₄ ← primitive</>(τ₈, n₃)
    
    BasicBlock bb = new BasicBlock(null);
    LocalVariable lv1 = new LocalVariable("lambda", 1);
    bb.addStatement(
        new Assignment(
            var("n", 3),
            new ElementAccess(temp(4), lv1)));
    bb.addStatement(
        new Assignment(
            temp(5),
            new PrimitiveCall(null, "-", var("n",3), new Constant(1))));
    bb.addStatement(
        new Assignment(
            temp(6),
            new PrimitiveCall(null, "*", temp(5), var("xbar", 3))));
    bb.addStatement(
        new Assignment(
            temp(7),
            new PrimitiveCall(null, "[", var("x", 0), var("n", 3))));
    bb.addStatement(
        new Assignment(
            temp(8),
            new PrimitiveCall(null, "+", temp(6), temp(7))));
    bb.addStatement(
        new Assignment(
            var("xbar", 4),
            new PrimitiveCall(null, "/", temp(8), var("n", 3))));

   
    TreeBuilder builder = new TreeBuilder();
    List<Statement> trees = builder.build(bb);

    for(Statement tree : trees) {
      System.out.println(tree);
    }
    
  }

  private Temp temp(int index) {
    return new Temp(index-1);
  }

  private SsaVariable var(String name, int version) {
    return new SsaVariable(new EnvironmentVariable(name), version);
  }
}
