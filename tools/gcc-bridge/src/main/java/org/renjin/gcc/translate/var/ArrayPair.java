package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImPrimitiveType;

public class ArrayPair {
  private FunctionContext context;
  private ImPrimitiveType type;
  private String jimpleArrayName;
  private String jimpleOffsetName;

  public ArrayPair(FunctionContext context, String name, ImPrimitiveType type) {
    this.jimpleArrayName = name + "_array";
    this.jimpleOffsetName = name + "_offset";
    this.type = type;
    context.getBuilder().addVarDecl(type.getArrayClass(), jimpleArrayName);
    context.getBuilder().addVarDecl(JimpleType.INT, jimpleOffsetName);
  }

  public void newArray(int length) {
    context.getBuilder().addStatement(jimpleArrayName +
        " = newarray (" + type.asJimple() + ")[" + length + "]");
  }


}
