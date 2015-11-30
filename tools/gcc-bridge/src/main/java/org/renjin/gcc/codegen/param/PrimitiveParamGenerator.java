package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.AddressablePrimitiveVarGenerator;
import org.renjin.gcc.codegen.var.PrimitiveVarGenerator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.Collections;
import java.util.List;

/**
 * Parameter that is a simple primitive (e.g. double, int, etc)
 */
public class PrimitiveParamGenerator extends ParamGenerator {

  private GimplePrimitiveType type;

  public PrimitiveParamGenerator(GimpleType gimpleType) {
    this.type = (GimplePrimitiveType) gimpleType;
  }

  public GimplePrimitiveType getType() {
    return type;
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(type.jvmType());
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor mv, GimpleParameter parameter, int startIndex, LocalVarAllocator localVars) {
    PrimitiveVarGenerator var = new PrimitiveVarGenerator(type, startIndex);

    if(parameter.isAddressable()) {
      AddressablePrimitiveVarGenerator addressableVar = new AddressablePrimitiveVarGenerator(
          type, localVars.reserve(type.jvmType()));  
      
      addressableVar.emitStore(mv, var);
      return addressableVar;
    
    } else {
      // No initialization required, already set to the local variable
      return var;
    }
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    parameterValueGenerator.emitPrimitiveValue(mv);
  }
}
