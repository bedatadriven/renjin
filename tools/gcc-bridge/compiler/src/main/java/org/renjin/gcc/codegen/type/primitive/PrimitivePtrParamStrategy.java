package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.Ptr;

import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;


/**
 * Strategy for primitive pointer parameters (e.g. {@code double*} using a wrapped
 * fat pointer type, such as {@link org.renjin.gcc.runtime.IntPtr} or {@link org.renjin.gcc.runtime.DoublePtr}.
 */
public class PrimitivePtrParamStrategy implements ParamStrategy {

  private final GimpleIndirectType type;

  /**
   * The {@link Ptr} subclass type
   */
  private final WrapperType pointerType;
  

  public PrimitivePtrParamStrategy(GimpleType type) {
    this.type = (GimpleIndirectType) type;
    this.pointerType = WrapperType.forPointerType(this.type);
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(pointerType.getWrapperType());
  }

  @Override
  public ExprGenerator emitInitialization(MethodGenerator mv, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    
    // Unpack the wrapper into seperate array and offset fields
    Var arrayVariable = localVars.reserve(parameter.getName() + "$array", pointerType.getArrayType());
    Var offsetVariable = localVars.reserve(parameter.getName() + "$offset", Type.INT_TYPE);
    
    // Load the parameter on the stack
    paramVars.get(0).load(mv);
    
    // duplicate the wrapper instance so we can call GETFIELD twice.
    mv.visitInsn(DUP);

    // Consume the first reference to the wrapper type and push the array field on the stack
    mv.visitFieldInsn(GETFIELD, pointerType.getWrapperType().getInternalName(), "array", pointerType.getArrayType().getDescriptor());

    // Store the array reference in the local variable
    arrayVariable.store(mv, );
    
    // Consume the second reference 
    mv.visitFieldInsn(GETFIELD, pointerType.getWrapperType().getInternalName(), "offset", "I");

    // Store the array reference in the local variable
    offsetVariable.store(mv, );
    
    return new PrimitivePtrVarGenerator(type, arrayVariable, offsetVariable);
  }

  @Override
  public void emitPushParameter(MethodGenerator mv, ExprGenerator parameterValueGenerator) {
    parameterValueGenerator.emitPushPointerWrapper(mv);
  }
  
}
