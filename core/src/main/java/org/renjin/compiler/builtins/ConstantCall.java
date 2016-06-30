package org.renjin.compiler.builtins;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;

import java.util.Iterator;
import java.util.List;

/**
 * Call to a builtin with constant arguments whose value is known at compile-time.
 */
public class ConstantCall implements Specialization {
  private Object constantValue;
  private Type type;
  private ValueBounds valueBounds;
  
  public ConstantCall(Object constantValue) {
    this.constantValue = constantValue;

    if (constantValue instanceof Integer) {
      type = Type.INT_TYPE;
      valueBounds = ValueBounds.of(IntVector.valueOf((Integer) constantValue));
    
    } else if(constantValue instanceof Double) {
      type = Type.DOUBLE_TYPE;
      valueBounds = ValueBounds.of(DoubleVector.valueOf((Double) constantValue));
    
    } else {
      throw new UnsupportedOperationException("constantValue: " + constantValue);
    }
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
    if(constantValue instanceof Integer) {
      mv.iconst((Integer) constantValue);
    } else if(constantValue instanceof Double) {
      mv.dconst((Double) constantValue);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public static ConstantCall evaluate(JvmMethod method, List<ValueBounds> arguments) {
    List<JvmMethod.Argument> formals = method.getAllArguments();
    Object[] args = new Object[formals.size()];
    for (int i = 0; i < formals.size(); i++) {
      method.getAllArguments();
    }
    Iterator<ValueBounds> it = arguments.iterator();
    int argI = 0;
    for (JvmMethod.Argument formal : formals) {
      if(formal.isContextual() || formal.isVarArg() || formal.isNamedFlag()) {
        throw new UnsupportedOperationException("formal: " + formal);
      }
      ValueBounds argument = it.next();
      args[argI++] = argument.getConstantValue();
    }

    Object constantValue;
    try {
      constantValue = method.getMethod().invoke(null, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new ConstantCall(constantValue);
  }
}
