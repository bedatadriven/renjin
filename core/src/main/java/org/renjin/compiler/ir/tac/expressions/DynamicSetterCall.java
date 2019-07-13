package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.primitives.special.AssignLeftFunction;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class DynamicSetterCall implements Expression {
  private final FunctionCall call;
  private final Expression functionExpr;
  private final Expression rhs;
  private final int forwardedArgumentIndex;

  public DynamicSetterCall(FunctionCall call, Expression functionExpr, Expression rhs) {
    this.call = call;
    this.functionExpr = functionExpr;
    this.rhs = rhs;
    this.forwardedArgumentIndex = call.findEllipsisArgumentIndex();
  }


  @Override
  public boolean isPure() {
    return false;
  }

  public IRArgument getArgument(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    return ValueBounds.UNBOUNDED;
  }

  @Override
  public ValueBounds getValueBounds() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        writeCall(context, mv);
      }
    };
  }

  private void writeCall(EmitContext context, InstructionAdapter mv) {

    // Store the RHS in a temporary variable, we need it twice
    int rhsVar = context.getLocalVarAllocator().reserve(Type.getType(SEXP.class));
    rhs.getCompiledExpr(context).loadSexp(context, mv);
    mv.visitVarInsn(Opcodes.ASTORE, rhsVar);

    // Collect the arguments, with the additional value argument at the end
//    List<String> argumentNames = DynamicCall.argumentNames(call);
//    List<SexpLoader> promisedArguments = DynamicCall.argumentPromises(call);
//
//    argumentNames.add("value");
//    promisedArguments.add((c, m) -> mv.visitVarInsn(Opcodes.ALOAD, rhsVar));
//
//    SexpLoader setterCall = (c, m) -> loadSetterCall(c, m, rhsVar);
//
//    DynamicCall.writeCall(context, mv,
//        functionExpr.getCompiledExpr(context),
//        setterCall,
//        argumentNames,
//        promisedArguments,
//        forwardedArgumentIndex);
    throw new UnsupportedOperationException("TODO");
  }

  private void loadSetterCall(EmitContext context, InstructionAdapter mv, int rhsVar) {
    context.constantSexp(call).loadSexp(context, mv);
    mv.checkcast(Type.getType(FunctionCall.class));

    mv.visitVarInsn(Opcodes.ALOAD, rhsVar);

    mv.invokestatic(Type.getInternalName(AssignLeftFunction.class), "setterCall",
        Type.getMethodDescriptor(Type.getType(FunctionCall.class),
            Type.getType(FunctionCall.class),
            Type.getType(SEXP.class)), false);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("dynamic ").append(functionExpr).append("<-").append("(");
    boolean needsComma = false;
    for (PairList.Node node : call.getArguments().nodes()) {
      if(needsComma) {
        s.append(", ");
      }
      if(node.hasTag()) {
        s.append(node.getName()).append(" = ");
      }
      if(node.getValue() != Symbol.MISSING_ARG) {
        s.append(node.getValue());
      }
      needsComma = true;
    }
    if(needsComma) {
      s.append(", ");
      s.append("value = ");
      s.append(rhs);
    }
    s.append(")");
    return s.toString();
  }
}
