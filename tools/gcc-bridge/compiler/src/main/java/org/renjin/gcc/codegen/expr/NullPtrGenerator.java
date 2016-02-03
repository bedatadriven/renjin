package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ICONST_0;

public class NullPtrGenerator extends AbstractExprGenerator implements ExprGenerator {

  private GimpleIndirectType type;

  public NullPtrGenerator(GimpleType pointerType) {
    this.type = (GimpleIndirectType) pointerType;
  }

}
