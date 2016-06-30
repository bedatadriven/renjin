package org.renjin.compiler.builtins;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.invoke.model.JvmMethod;

import java.util.List;

/**
 * A double-precision scalar operation that can be implemented using a JVM bytecode.
 */
public class DoubleBinaryOp implements Specialization {
  
  private int opcode;
  
  public DoubleBinaryOp(int opcode) {
    this.opcode = opcode;
  }

  @Override
  public Type getType() {
    return Type.DOUBLE_TYPE;
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.DOUBLE_PRIMITIVE;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<Expression> arguments) {
    assert  arguments.size() == 2;
    Expression x = arguments.get(0);
    Expression y = arguments.get(1);

    x.load(emitContext, mv);
    emitContext.convert(mv, x.getType(), Type.DOUBLE_TYPE);

    y.load(emitContext, mv);
    emitContext.convert(mv, y.getType(), Type.DOUBLE_TYPE);
    
    mv.visitInsn(opcode);
  }

  public static DoubleBinaryOp trySpecialize(String name, JvmMethod overload) {
    List<JvmMethod.Argument> formals = overload.getPositionalFormals();
    if(formals.size() == 2 &&
        formals.get(0).getClazz().equals(double.class) &&
        formals.get(0).getClazz().equals(double.class)) {

      switch (name) {
        case "+":
          return new DoubleBinaryOp(Opcodes.DADD);
        case "-":
          return new DoubleBinaryOp(Opcodes.DSUB);
        case "*":
          return new DoubleBinaryOp(Opcodes.DMUL);
        case "/":
          return new DoubleBinaryOp(Opcodes.DDIV);
      }
    }
    return null;
  }
}
