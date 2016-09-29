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
import org.renjin.sexp.SEXP;

/**
 * Created by alex on 28-9-16.
 */
public class Compiler {

  public static CompiledBody tryCompile(Context context, Environment rho, SEXP expression) {
    try {

      RuntimeState runtimeState = new RuntimeState(context, rho);
      IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
      IRBody body = builder.build(expression);

      ControlFlowGraph cfg = new ControlFlowGraph(body);


      DominanceTree dTree = new DominanceTree(cfg);
      SsaTransformer ssaTransformer = new SsaTransformer(cfg, dTree);
      ssaTransformer.transform();

      System.out.println(cfg);

      UseDefMap useDefMap = new UseDefMap(cfg);
      TypeSolver types = new TypeSolver(cfg, useDefMap);
      types.execute();


      types.verifyFunctionAssumptions(runtimeState);

      ssaTransformer.removePhiFunctions(types);

      System.out.println(cfg);

      ByteCodeEmitter emitter = new ByteCodeEmitter(cfg, types);
      return emitter.compile().newInstance();

    } catch (NotCompilableException e) {
      System.out.println(e.toString());
      context.warn("Could not compile loop: " + e.toString(context));
      return null;

    } catch (InvalidSyntaxException e) {
      e.printStackTrace();
      throw new EvalException(e.getMessage());

    } catch (Exception e) {
      throw new EvalException("Exception compiling loop: " + e.getMessage(), e);
    }
  }

  public static boolean tryCompileAndRun(Context context, Environment rho, SEXP expression) {

    CompiledBody body = tryCompile(context, rho, expression);
    if(body == null) {
      return false;
    }

    body.evaluate(context, rho);

    return true;
  }
}
