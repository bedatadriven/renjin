package org.renjin.compiler.builtins;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.SEXP;

import java.util.List;


public class LengthCall implements Specialization {
  @Override
  public Type getType() {
    return Type.INT_TYPE;
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.INT_PRIMITIVE;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<Expression> arguments) {
    Expression argument = arguments.get(0);
    argument.load(emitContext, mv);
    emitContext.convert(mv, argument.getType(), Type.getType(SEXP.class));
    
    mv.invokeinterface(Type.getInternalName(SEXP.class), "length", 
        Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(SEXP.class)));
  }
}
