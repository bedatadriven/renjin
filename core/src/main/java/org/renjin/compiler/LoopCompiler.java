package org.renjin.compiler;

import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.cfg.UseDefMap;
import org.renjin.compiler.codegen.ByteCodeEmitter;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;

/**
 * Created by alex on 28-9-16.
 */
public class LoopCompiler {


  public static boolean tryCompileAndRun(Context context, Environment rho, FunctionCall call) {

    CompiledBody compiledBody;
    try {

      RuntimeState runtimeState = new RuntimeState(context, rho);
      IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
      IRBody body = builder.build(call);

      ControlFlowGraph cfg = new ControlFlowGraph(body);


      DominanceTree dTree = new DominanceTree(cfg);
      SsaTransformer ssaTransformer = new SsaTransformer(cfg, dTree);
      ssaTransformer.transform();


      UseDefMap useDefMap = new UseDefMap(cfg);
      TypeSolver types = new TypeSolver(cfg, useDefMap);
      types.execute();


      types.verifyFunctionAssumptions(runtimeState);

      ssaTransformer.removePhiFunctions(types);

      System.out.println(cfg);

      ByteCodeEmitter emitter = new ByteCodeEmitter(cfg, types);
      compiledBody = emitter.compile().newInstance();

    } catch (NotCompilableException e) {
      context.warn("Could not compile loop: " + e.toString(context));
      return false;

    } catch (InvalidSyntaxException e) {
      throw new EvalException(e.getMessage());

    } catch (Exception e) {
      throw new EvalException("Exception compiling loop: " + e.getMessage(), e);
    }

    compiledBody.evaluate(context, rho);

    return true;
  }
}
