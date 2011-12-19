package r.compiler.ir.optimize;

import r.base.Primitives;
import r.compiler.cfg.BasicBlock;
import r.compiler.cfg.ControlFlowGraph;
import r.compiler.ir.tac.expressions.DynamicCall;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.compiler.ir.tac.statements.Statement;
import r.lang.PrimitiveFunction;

/**
 * Not really a valid optimization, but a way to test the 
 * compiler at this early stage when dynamic calls are not
 * fully implemented.
 */
public class AssumePrimitiveOptimization implements IntraProcedureOptimization {

  @Override
  public void optimize(ControlFlowGraph cfg) {
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      for(int i = 0; i != bb.getStatements().size(); ++i) {
        Statement stmt = bb.getStatements().get(i);
        if(stmt.getRHS() instanceof DynamicCall) {
          DynamicCall call = (DynamicCall) stmt.getRHS();
          if(call.getName() instanceof EnvironmentVariable) {
            EnvironmentVariable function = (EnvironmentVariable) call.getName();
            PrimitiveFunction builtin = Primitives.getBuiltin(function.getName());
            if(builtin != null) {
              bb.replaceStatement(i, 
                  stmt.withRHS(
                      new PrimitiveCall(function.getName(), call.getArguments())));
            }
          }
        }
      }
    }
  }
}
