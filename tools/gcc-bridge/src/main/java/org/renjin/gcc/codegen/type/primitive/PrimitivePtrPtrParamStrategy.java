package org.renjin.gcc.codegen.type.primitive;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.gcc.runtime.Ptr;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for pointer to primitive pointer parameters, for example {@code double**} that use a single
 * {@link ObjectPtr} parameter. Each element of {@link ObjectPtr#array} is an instance of a primitive fat pointer 
 * wrapper, such as {@link org.renjin.gcc.runtime.IntPtr} or {@link org.renjin.gcc.runtime.DoublePtr}.
 */
public class PrimitivePtrPtrParamStrategy extends ParamStrategy {

  private final GimpleIndirectType type;

  /**
   * The {@link Ptr} subclass type
   */
  private final WrapperType pointerType;
  
  public PrimitivePtrPtrParamStrategy(GimpleType type) {
    this.type = (GimpleIndirectType) type;
    this.pointerType = WrapperType.forPointerType((GimpleIndirectType) type.getBaseType());
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(ObjectPtr.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    return new PtrPtrExpr(paramVars.get(0));
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    if(!parameterValueGenerator.getPointerType().equals(WrapperType.OBJECT_PTR)) {
      throw new InternalCompilerException("Type mismatch: " + parameterValueGenerator.getClass().getName());
    }
    parameterValueGenerator.emitPushPointerWrapper(mv);
  }
  
  private class PtrPtrExpr extends AbstractExprGenerator {

    private Var varIndex;
    
    public PtrPtrExpr(Var varIndex) {
      this.varIndex = varIndex;
    }

    @Override
    public GimpleType getGimpleType() {
      return type;
    }


    @Override
    public void emitPushPointerWrapper(MethodVisitor mv) {
      varIndex.load(mv);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      emitPushPointerWrapper(mv);
      WrapperType.OBJECT_PTR.emitUnpackArrayAndOffset(mv, pointerType);
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      valueGenerator.emitPushPointerWrapper(mv);
      varIndex.store(mv);
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedPrimitivePtr(this);
    }

    @Override
    public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
      return new PrimitivePtrPtrPlus(this, offsetInBytes);
    }
  }
  
}
