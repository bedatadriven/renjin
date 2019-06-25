package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.FunctionLoader;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.primitives.special.AssignLeftFunction;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

public class DynamicSetterCall implements Expression {
  private final FunctionCall call;
  private final FunctionLoader functionLoader;
  private final String functionName;
  private final Expression rhs;

  public DynamicSetterCall(FunctionCall call, FunctionLoader functionLoader, String functionName, Expression rhs) {
    this.call = call;
    this.functionLoader = functionLoader;
    this.functionName = functionName;
    this.rhs = rhs;
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

    functionLoader.loadFunction(context, mv);

    // Store the RHS in a temporary variable, we need it twice
    int rhsVar = context.getLocalVarAllocator().reserve(Type.getType(SEXP.class));

    rhs.getCompiledExpr(context).loadSexp(context, mv);
    mv.visitVarInsn(Opcodes.ASTORE, rhsVar);

    // Now we need to invoke:
    //   SEXP applyPromised(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch);
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

    // When invoking the setter, we have to create a new call object
    // that includes the evaluated value of rhs
    loadSetterCall(context, mv, rhsVar);

    loadArgumentNames(context, mv);
    loadArgumentValues(context, mv, rhsVar);

    // Dispatch table is empty
    mv.aconst(null);

    mv.invokeinterface(Type.getInternalName(Function.class), "applyPromised",
        Type.getMethodDescriptor(Type.getType(SEXP.class),
            Type.getType(Context.class),
            Type.getType(Environment.class),
            Type.getType(FunctionCall.class),
            Type.getType(String[].class),
            Type.getType(SEXP[].class),
            Type.getType(DispatchTable.class)));
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

  private void loadArgumentValues(EmitContext context, InstructionAdapter mv, int rhsVar) {

    int numArguments = call.getArguments().length() + 1;

    mv.iconst(numArguments);
    mv.newarray(Type.getType(SEXP.class));

    int i = 0;
    for (SEXP argumentValue : call.getArguments().values()) {
      mv.dup();
      mv.iconst(i);
      DynamicCall.loadArgumentPromise(context, mv, argumentValue);
      mv.visitInsn(Opcodes.AASTORE);
      i++;
    }

    mv.dup();
    mv.iconst(i);
    mv.visitVarInsn(Opcodes.ALOAD, rhsVar);
    mv.visitInsn(Opcodes.AASTORE);

  }

  private void loadArgumentNames(EmitContext context, InstructionAdapter mv) {
    mv.iconst(call.getArguments().length() + 1);
    mv.newarray(Type.getType(String.class));
    int i = 0;
    for (PairList.Node node : call.getArguments().nodes()) {
      if(node.hasTag()) {
        mv.dup();
        mv.iconst(i);
        mv.aconst(node.getName());
        mv.visitInsn(Opcodes.AASTORE);
      }
      i++;
    }
    mv.dup();
    mv.iconst(i);
    mv.aconst("value");
    mv.visitInsn(Opcodes.AASTORE);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("dynamic ").append(functionName + "<-").append("(");
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
