package org.renjin.compiler.ir.tac.statements;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.VariableStorage;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Collections;
import java.util.List;


public class ReturnStatement implements Statement, BasicBlockEndingStatement {

  private Expression returnValue;
  
  private List<Symbol> environmentVariableNames = Lists.newArrayList();
  private List<Expression> environmentVariables = Lists.newArrayList();
  
  public ReturnStatement(Expression returnValue) {
    super();
    this.returnValue = returnValue;
  }
  
  public Expression getReturnValue() {
    return returnValue;
  }
  

  @Override
  public Expression getRHS() {
    return returnValue;
  }
  
  @Override
  public void setRHS(Expression newRHS) {
    this.returnValue = newRHS;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  public void addEnvironmentVariables(Iterable<EnvironmentVariable> variables) {
    for (EnvironmentVariable variable : variables) {
      addEnvironmentVariable(variable.getName(), variable);
    }
  }
  
  public void addEnvironmentVariable(Symbol symbol, LValue lValue) {
    environmentVariableNames.add(symbol);
    environmentVariables.add(lValue);
  }
  
  @Override
  public int getChildCount() {
    return 1 + environmentVariableNames.size();
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return returnValue;
    } else {
      return environmentVariables.get(index - 1);
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      returnValue = child;
    } else {
      environmentVariables.set(childIndex - 1, child);
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitReturn(this);
  }

  @Override
  public int emit(EmitContext emitContext, InstructionAdapter mv) {

    // Set the current local variables back into the environment
    for (int i = 0; i < environmentVariableNames.size(); i++) {

      VariableStorage variableStorage = emitContext.getVariableStorage((LValue) environmentVariables.get(i));
      if(variableStorage != null) {
        // Environment.setVariable(String, SEXP)
        mv.load(emitContext.getEnvironmentVarIndex(), Type.getType(Environment.class));
        mv.aconst(environmentVariableNames.get(i).getPrintName());
        mv.load(variableStorage.getSlotIndex(), variableStorage.getType());

        emitContext.convert(mv, variableStorage.getType(), Type.getType(SEXP.class));

        mv.invokevirtual(Type.getInternalName(Environment.class), "setVariable",
            Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(SEXP.class)), false);

      }
    }
    
    mv.areturn(Type.VOID_TYPE);
    return 0;
  }


  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("return ");
    s.append(returnValue);
    for (int i = 0; i < environmentVariableNames.size(); i++) {
      s.append(", ");
      s.append(environmentVariableNames.get(i));
      s.append(" = ");
      s.append(environmentVariables.get(i));
    }
    return s.toString();
  }
}
