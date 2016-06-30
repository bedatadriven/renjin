package org.renjin.compiler.builtins;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;

import java.util.List;

/**
 * Specialization for builtins that are marked {@link org.renjin.invoke.annotations.DataParallel} and
 * whose arguments are "recycled" for multiple calls.
 */
public class DataParallelCall implements Specialization {

  private final String name;
  private final JvmMethod method;
  private List<ValueBounds> argumentTypes;
  private final ValueBounds valueBounds;
  private final Type type;

  public DataParallelCall(Primitives.Entry primitive, JvmMethod method, List<ValueBounds> argumentTypes) {
    this.name = primitive.name;
    this.method = method;
    this.argumentTypes = argumentTypes;
    this.valueBounds = ValueBounds.vector(method.getReturnType(), computeLengths(argumentTypes));
    this.type = valueBounds.storageType();
  }

  private int computeLengths(List<ValueBounds> argumentTypes) {
    List<ValueBounds> recycledArguments = Lists.newArrayList();
    for (int i = 0; i < method.getPositionalFormals().size(); i++) {
      JvmMethod.Argument formal = method.getPositionalFormals().get(i);
      if(formal.isRecycle()) {
        recycledArguments.add(argumentTypes.get(i));
      }
    }

    int maxLength = 1;
    for (ValueBounds recycledArgument : recycledArguments) {
      if(recycledArgument.getLength() == ValueBounds.UNKNOWN_LENGTH) {
        return ValueBounds.UNKNOWN_LENGTH;
      } else if(recycledArgument.getLength() == 0) {
        return 0;
      } else if (recycledArgument.getLength() > maxLength) {
        maxLength = recycledArgument.getLength();
      }
    }
    return maxLength;
  }

  public Specialization specializeFurther() {
    if(valueBounds.getLength() == 1) {
      DoubleBinaryOp op = DoubleBinaryOp.trySpecialize(name, method);
      if(op != null) {
        return op;
      }

      return new StaticMethodCall(method);
    }
    
    return this;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<Expression> arguments) {
    throw new UnsupportedOperationException();
  }
}
