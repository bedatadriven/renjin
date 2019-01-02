/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.NullVariadicStrategy;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.voidt.VoidReturnStrategy;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.collect.Lists;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * Emits the bytecode to invoke a function pointer (MethodHandle)
 */
public class FunPtrCallGenerator implements CallGenerator {

  private TypeOracle typeOracle;
  private JExpr methodHandle;
  private final ReturnStrategy returnStrategy;
  private final GimpleFunctionType functionType;

  public FunPtrCallGenerator(TypeOracle typeOracle, GimpleFunctionType type, JExpr methodHandle) {
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
    final String signature = TypeOracle.getMethodDescriptor(returnStrategy, paramStrategies, new NullVariadicStrategy());
    
    
    // Now define the actual value
    JExpr callValue = new JExpr() {
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
          paramStrategies.get(i).loadParameter(mv, Optional.of(exprFactory.findGenerator(call.getOperand(i))));
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
      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      GimpleType lhsType = call.getLhs().getType();
      GExpr rhs = returnStrategy.unmarshall(mv, callValue, exprFactory.strategyFor(lhsType));
      
      lhs.store(mv, rhs);
    }
  }

}
