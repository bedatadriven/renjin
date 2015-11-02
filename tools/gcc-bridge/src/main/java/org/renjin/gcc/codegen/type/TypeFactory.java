package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;

/**
 * Responsible for creating Generators for parameters, local variables, and return values
 * for a specific Gimple type.
 */
public abstract class TypeFactory {

  /**
   * 
   * @return a new {@code ParamGenerator} for this type.
   */
  public ParamGenerator paramGenerator() {
    throw new UnsupportedOperationException(getClass().getSimpleName() + " parameters are not implemented.");
  }

  /**
   * 
   * @return a new {@code ReturnGenerator} for this type.
   */
  public ReturnGenerator returnGenerator() {
    throw new UnsupportedOperationException(
        "returning types of " + getClass().getName() + " is not implemented.");
  }


  public VarGenerator varGenerator(LocalVarAllocator allocator) {
    throw new UnsupportedOperationException(
        "local variables of type  " + getClass().getName() + " is not implemented.");
  }

  public VarGenerator addressableVarGenerator(LocalVarAllocator allocator) {
    throw new UnsupportedOperationException(
        "local variables of type  " + getClass().getName() + " is not implemented.");
  }


  public TypeFactory pointerTo() {
    throw new UnsupportedOperationException("unimplemented [" + getClass().getName() + "]");
  }

  public TypeFactory arrayOf(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException("unimplemented [" + getClass().getName() + "]");
  }

  /**
   * Creates a new FieldGenerator for fields of this type.
   * 
   * @param className the full internal name of the class, for example, "org/renjin/gcc/Record$1"
   * @param fieldName the name of the field
   */
  public FieldGenerator fieldGenerator(String className, String fieldName) {
    throw new UnsupportedOperationException("unimplemented [" + getClass().getName() + "]");
  }
}
