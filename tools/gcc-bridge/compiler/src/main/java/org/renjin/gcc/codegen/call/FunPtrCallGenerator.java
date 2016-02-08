package org.renjin.gcc.codegen.call;

import com.google.common.collect.Lists;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.LValue;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.voidt.VoidReturnStrategy;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleFunctionType;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Emits the bytecode to invoke a function pointer (MethodHandle)
 */
public class FunPtrCallGenerator implements CallGenerator {

  private TypeOracle typeOracle;
  private SimpleExpr methodHandle;
  private final ReturnStrategy returnStrategy;
  private final GimpleFunctionType functionType;

  public FunPtrCallGenerator(TypeOracle typeOracle, GimpleFunctionType type, SimpleExpr methodHandle) {
    this.typeOracle = typeOracle;
    this.methodHandle = methodHandle;
    functionType = type;
    returnStrategy = typeOracle.returnStrategyFor(functionType.getReturnType());
  }


  /**
   * Write the bytecode for invoking a call to a function pointer, which we represent
   * as a MethodHandle in the compiled code. Since GCC permits casting function pointer 
   * signatures willy-nill, we need to infer the actual types from the call.
   *
   * @see <a href="http://stackoverflow.com/questions/559581/casting-a-function-pointer-to-another-type">Stack Overflow discussion</a>
   */
  @Override
  public void emitCall(MethodGenerator mv, final ExprFactory exprFactory, final GimpleCall call) {

    
    // Infer the parameters types from the arguments provided
    // We simply can't trust the GimpleFunctionType provided
    
    final List<ParamStrategy> paramStrategies = Lists.newArrayList();
    
    for (GimpleExpr argumentExpr : call.getOperands()) {
      paramStrategies.add(typeOracle.forParameter(argumentExpr.getType()));
    }
    
    final ReturnStrategy returnStrategy;
    if(call.getLhs() == null) {
      returnStrategy = new VoidReturnStrategy();
    } else {
      returnStrategy = typeOracle.returnStrategyFor(call.getLhs().getType());
    }
    
    // Using this information, we can compose the signature for the invoke call
    // (MethodHandle invoke calls are signature-polymorphic:
    //  https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/MethodHandle.html)
    final String signature = TypeOracle.getMethodDescriptor(returnStrategy, paramStrategies);
    
    
    // Now define the actual value
    SimpleExpr callValue = new SimpleExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return returnStrategy.getType();
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        // Push the method handle onto the stack first
        methodHandle.load(mv);
        
        // ... arguments onto the stack
        for (int i = 0; i < call.getOperands().size(); i++) {
          paramStrategies.get(i).loadParameter(mv, exprFactory.findGenerator(call.getOperand(i)));
        }
        // Use invoke() rather than invokeExact() to smooth over any type differences
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invoke", signature, false);
      }
    };
    
    if(call.getLhs() == null) {
      // if we don't need to store the value, we're done
      callValue.load(mv);
    
    } else {
      // Otherwise unmarshall the return value into an expression and store to the lhs
      LValue lhs = (LValue) exprFactory.findGenerator(call.getLhs());
      Expr rhs = returnStrategy.unmarshall(mv, callValue);
      
      lhs.store(mv, rhs);
    }
  }

}
