package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.VariableStorage;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.NullExpression;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.Collections;
import java.util.List;

/**
 * Updates the values of local variables in the calling environment
 */
public class UpdateStatement implements Statement {

  private List<Symbol> environmentVariableNames = Lists.newArrayList();
  private List<Expression> environmentVariables = Lists.newArrayList();


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
  public void setChild(int childIndex, Expression child) {
    environmentVariables.set(childIndex, child);
  }

  @Override
  public int getChildCount() {
    return environmentVariables.size();
  }

  @Override
  public Expression childAt(int index) {
    return environmentVariables.get(index);
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  @Override
  public Expression getRHS() {
    return NullExpression.INSTANCE;
  }

  @Override
  public void setRHS(Expression newRHS) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitUpdate(this);
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

        if(variableStorage.getType().getSort() != Type.OBJECT) {
          ValueBounds bounds = environmentVariables.get(i).getValueBounds();
          if(bounds.isAttributeConstant()) {
            AttributeMap attributes = bounds.getConstantAttributes();
            if(attributes != AttributeMap.EMPTY) {
              generateAttributes(mv, attributes);
            }
          } else {
            throw new UnsupportedOperationException("Lost attributes");
          }
        }

        mv.invokevirtual(Type.getInternalName(Environment.class), "setVariable",
            Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(SEXP.class)), false);

      }
    }
    return 0;
  }


  private void generateAttributes(InstructionAdapter mv, AttributeMap constantAttributes) {

    // SEXP should be on the stack
    // Create new AttributeMap.Builder

    Type builderType = Type.getType(AttributeMap.Builder.class);
    mv.invokestatic(Type.getInternalName(AttributeMap.class), "newBuilder",
        Type.getMethodDescriptor(builderType), false);

    // Now Builder is on the stack...
    for (PairList.Node node : constantAttributes.nodes()) {
      if(node.getTag() == Symbols.CLASS) {
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "setClass",
            Type.getMethodDescriptor(builderType, Type.getType(SEXP.class)), false);

      } else if(node.getTag() == Symbols.NAMES) {
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "setNames",
            Type.getMethodDescriptor(builderType, Type.getType(SEXP.class)), false);

      } else if(node.getTag() == Symbols.DIM) {
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "setDim",
            Type.getMethodDescriptor(builderType, Type.getType(SEXP.class)), false);

      } else if(node.getTag() == Symbols.DIMNAMES) {
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "setDimNames",
            Type.getMethodDescriptor(builderType, Type.getType(SEXP.class)), false);

      } else {
        mv.aconst(node.getTag().getPrintName());
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "set",
            Type.getMethodDescriptor(builderType, Type.getType(String.class), Type.getType(SEXP.class)), false);
      }
    }
    // Stack:
    // SEXP AttrbuteMap.Builder
    mv.invokeinterface(Type.getInternalName(SEXP.class), "setAttributes",
        Type.getMethodDescriptor(Type.getType(SEXP.class), builderType));

  }

  private void pushConstant(InstructionAdapter mv, SEXP value) {
    if(value instanceof StringVector) {
      if(value.length() == 1) {
        mv.visitLdcInsn(((StringVector) value).getElementAsString(0));
        mv.invokestatic(Type.getInternalName(StringVector.class), "valueOf",
            Type.getMethodDescriptor(Type.getType(StringVector.class), Type.getType(String.class)), false);
        return;
      }
    }

    throw new UnsupportedOperationException("TODO: constant = " + value);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("update ");
    for (int i = 0; i < environmentVariables.size(); i++) {
      if(i > 0) {
        s.append(", ");
      }
      s.append(environmentVariableNames.get(i));
      s.append(" = ");
      s.append(environmentVariables.get(i));
    }
    return s.toString();
  }
}
