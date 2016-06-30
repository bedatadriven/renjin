package org.renjin.compiler.builtins;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.invoke.model.JvmMethod;

import java.util.List;


/**
 * Call to a single static method overload of a builtin.
 */
public class StaticMethodCall implements Specialization {

  private final JvmMethod method;
  private final ValueBounds valueBounds;

  public StaticMethodCall(JvmMethod method) {
    this.method = method;
    this.valueBounds = ValueBounds.of(method.getReturnType());
  }

  public Specialization furtherSpecialize(List<ValueBounds> argumentBounds) {
    if (ValueBounds.allConstant(argumentBounds)) {
      return ConstantCall.evaluate(method, argumentBounds);
    }
    return this;
  }

  @Override
  public Type getType() {
    return Type.getType(method.getReturnType());
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<Expression> arguments) {

    for (JvmMethod.Argument argument : method.getAllArguments()) {
      if(argument.isContextual()) {
        throw new UnsupportedOperationException("TODO");
      } else {
        Expression argumentExpr = arguments.get(argument.getIndex());
        argumentExpr.load(emitContext, mv);
        emitContext.convert(mv, argumentExpr.getType(), Type.getType(argument.getClazz()));
      }
    }

    mv.invokestatic(Type.getInternalName(method.getDeclaringClass()), method.getName(),
        Type.getMethodDescriptor(method.getMethod()), false);

  }
}
