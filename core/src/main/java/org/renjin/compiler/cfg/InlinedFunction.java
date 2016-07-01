package org.renjin.compiler.cfg;

import com.google.common.collect.Lists;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.ReadParam;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.sexp.Closure;

import java.util.List;


public class InlinedFunction {

  private final SsaTransformer ssaTransformer;
  private final DominanceTree dTree;
  private final ControlFlowGraph cfg;
  private final UseDefMap useDefMap;
  private final TypeSolver types;
  private final List<ReadParam> params;

  private List<ReturnStatement> returnStatements = Lists.newArrayList();

  public InlinedFunction(Context context, Closure closure) {

    IRBodyBuilder builder = new IRBodyBuilder(context, closure.getEnclosingEnvironment());
    IRBody body = builder.buildFunctionBody(closure);

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

    Label exitLabel = new Label();
    
    for(BasicBlock basicBlock : cfg.getBasicBlocks()) {
      if(basicBlock != cfg.getEntry() && basicBlock != cfg.getExit()) {
        for(IRLabel label : basicBlock.getLabels()) {
          mv.visitLabel(emitContext.getAsmLabel(label));
        }
        for(Statement stmt : basicBlock.getStatements()) {
          try {
            if(stmt instanceof ReturnStatement) {
              // Instead of returning, just push the return value on the stack
              // and jump to the exit point for the function.
              stmt.getRHS().load(emitContext, mv);
              mv.goTo(exitLabel);

            } else {
              stmt.emit(emitContext, mv);
            }
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
