package org.renjin.compiler.cfg;

import com.google.common.collect.Lists;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.expressions.ReadParam;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Symbol;

import java.util.List;
import java.util.Set;


public class InlinedFunction {

  private final RuntimeState runtimeState;

  private final SsaTransformer ssaTransformer;
  private final DominanceTree dTree;
  private final ControlFlowGraph cfg;
  private final UseDefMap useDefMap;
  private final TypeSolver types;
  private final List<ReadParam> params;

  private List<ReturnStatement> returnStatements = Lists.newArrayList();

  /**
   * @param closure the closure to inline
   * @param arguments the names of the formals that will be supplied to this inline call
   */
  public InlinedFunction(RuntimeState parentState, Closure closure, Set<Symbol> arguments) {

    runtimeState = new RuntimeState(parentState, closure.getEnclosingEnvironment());
    
    IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
    IRBody body = builder.buildFunctionBody(closure, arguments);

    cfg = new ControlFlowGraph(body);
    dTree = new DominanceTree(cfg);
    ssaTransformer = new SsaTransformer(cfg, dTree);
    ssaTransformer.transform();
    useDefMap = new UseDefMap(cfg);
    types = new TypeSolver(cfg, useDefMap);
    params = body.getParams();

    for (Statement statement : body.getStatements()) {
      if(statement instanceof ReturnStatement) {
        returnStatements.add((ReturnStatement) statement);
      }
    }
  }

  public ControlFlowGraph getCfg() {
    return cfg;
  }

  public SsaTransformer getSsaTransformer() {
    return ssaTransformer;
  }

  public List<ReadParam> getParams() {
    return params;
  }


  public void updateParam(int i, ValueBounds argumentBounds) {
    params.get(i).updateBounds(argumentBounds);

  }
  
  public ValueBounds computeBounds() {
    
    System.out.println(cfg);
    
    types.execute();
    types.dumpBounds();
    
    if(returnStatements.size() == 1) {
      return returnStatements.get(0).getRHS().getValueBounds();
    } else {
      throw new UnsupportedOperationException("TODO");
    }
  }
  
  public void writeInline(EmitContext emitContext, InstructionAdapter mv) {
    
    // Last check for assumption violations
    types.verifyFunctionAssumptions(runtimeState);

    Label exitLabel = new Label();
    
    for(BasicBlock basicBlock : cfg.getBasicBlocks()) {
      if(basicBlock != cfg.getEntry() && basicBlock != cfg.getExit()) {
        for(IRLabel label : basicBlock.getLabels()) {
          mv.visitLabel(emitContext.getAsmLabel(label));
        }
        for(Statement stmt : basicBlock.getStatements()) {
          try {
            if (stmt instanceof ReturnStatement) {
              // Instead of returning, just push the return value on the stack
              // and jump to the exit point for the function.
              stmt.getRHS().load(emitContext, mv);
              mv.goTo(exitLabel);

            } else {
              stmt.emit(emitContext, mv);
            }
          } catch (NotCompilableException e) {
            throw e;
          } catch (Exception e) {
            throw new InternalCompilerException("Exception compiling statement " + stmt, e);
          }
        }
      }
    }
    mv.mark(exitLabel);
  }
  

  @Override
  public String toString() {
    return cfg.toString();
  }

  public TypeSolver getTypes() {
    return types;
  }
}
