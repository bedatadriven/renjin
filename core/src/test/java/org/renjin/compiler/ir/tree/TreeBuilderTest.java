package org.renjin.compiler.ir.tree;

import java.util.List;

import org.junit.Test;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.ElementAccess;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LocalVariable;
import org.renjin.compiler.ir.tac.expressions.PrimitiveCall;
import org.renjin.compiler.ir.tac.expressions.Temp;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.compiler.ir.tree.TreeBuilder;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;


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
            primitiveCall("-")));
    bb.addStatement(
        new Assignment(
            temp(6),
            primitiveCall("*", temp(5), var("xbar", 3))));
    bb.addStatement(
        new Assignment(
            temp(7),
            primitiveCall("[", var("x", 0), var("n", 3))));
    bb.addStatement(
        new Assignment(
            temp(8),
            primitiveCall("+", temp(6), temp(7))));
    bb.addStatement(
        new Assignment(
            var("xbar", 4),
           primitiveCall("/", temp(8), var("n", 3))));

   
    TreeBuilder builder = new TreeBuilder();
    List<Statement> trees = builder.build(bb);

    for(Statement tree : trees) {
      System.out.println(tree);
    }
    
  }

  private PrimitiveCall primitiveCall(String fnName, Expression... arguments) {
    // create dummy fcall
    PairList.Builder args = new PairList.Builder();
    for(Expression argument : arguments) {
      args.add(new StringVector(argument.toString()));
    }
    return new PrimitiveCall(new FunctionCall(Symbol.get(fnName), args.build()), fnName, arguments);
  }

  private Temp temp(int index) {
    return new Temp(index-1);
  }

  private SsaVariable var(String name, int version) {
    return new SsaVariable(new EnvironmentVariable(name), version);
  }
}
