/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
//package org.renjin.gcc.codegen.call;
//
//import com.google.common.collect.Lists;
//import org.objectweb.asm.Opcodes;
//import org.objectweb.asm.Type;
//import org.renjin.gcc.InternalCompilerException;
//import org.renjin.gcc.codegen.MethodGenerator;
//import org.renjin.gcc.codegen.expr.ExprFactory;
//import org.renjin.gcc.codegen.expr.ExprGenerator;
//import org.renjin.gcc.codegen.type.ParamStrategy;
//import org.renjin.gcc.codegen.type.ReturnStrategy;
//import org.renjin.gcc.codegen.type.TypeOracle;
//import org.renjin.gcc.codegen.expr.ConstantValue;
//import org.renjin.gcc.codegen.expr.LValue;
//import org.renjin.gcc.codegen.var.Values;
//import org.renjin.gcc.gimple.expr.GimpleExpr;
//import org.renjin.gcc.gimple.statement.GimpleCall;
//import org.renjin.gcc.gimple.type.GimpleIndirectType;
//import org.renjin.gcc.gimple.type.GimplePrimitiveType;
//import org.renjin.gcc.gimple.type.GimpleType;
//
//import java.lang.reflect.Method;
//import java.util.List;
//
///**
// * Generates a call to an existing JVM method.
// */
//public class StaticMethodCallGenerator implements CallGenerator {
//  
//  private TypeOracle typeOracle;
//  private Method method;
//
//  private List<ParamStrategy> paramStrategies = null;
//  private ReturnStrategy returnStrategy = null;
//
//  public StaticMethodCallGenerator(TypeOracle typeOracle, Method method) {
//    this.typeOracle = typeOracle;
//    this.method = method;
//  }
//
//  private ReturnStrategy returnGenerator() {
//    if(returnStrategy == null) {
//      returnStrategy = typeOracle.forReturnValue(method);
//    }
//    return returnStrategy;
//  }
//
//
//  @Override
//  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
//
//
//    // Make a list of call arguments
//    List<ExprGenerator> argumentGenerators = Lists.newArrayList();
//    for (GimpleExpr gimpleExpr : call.getOperands()) {
//      argumentGenerators.add(exprFactory.findGenerator(gimpleExpr));
//    }
//
//    checkArity(argumentGenerators);
//
//    CallExpr returnValue = new CallExpr(argumentGenerators);
//
//    // If we don't need the return value, then invoke and pop any result off the stack
//    if(call.getLhs() == null) {
//      returnValue.load(mv);
//      mv.pop(returnValue.getType());
//
//    } else {
//
//      ExprGenerator callExpr = functionGenerator.getReturnStrategy().unmarshall(mv, returnValue);
//      LValue lhs = (LValue) exprFactory.findGenerator(call.getLhs());
//
//      lhs.store(mv, callExpr);
//    }
//    
//    
//
//    // The number of fixed (gimple) parameters expected, excluding var args
//    // the number of Jvm arguments may be greater
//    int fixedArgCount = paramStrategies.size();
//
//
//    // Push all (fixed) parameters on the stack
//    for (int i = 0; i < fixedArgCount; i++) {
//      ParamStrategy paramStrategy = getParamStrategies().get(i);
//      paramStrategy.emitPushParameter(mv, argumentGenerators.get(i));
//    }
//    
//    // if this method accepts var args, then we pass the remaining arguments as an Object[] array
//    if(method.isVarArgs()) {
//      int varArgCount = argumentGenerators.size() - fixedArgCount;
//      Values.constantInt(varArgCount).load(mv);
//      mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Object.class));
//      
//      for(int i=0;i<varArgCount;++i) {
//        mv.visitInsn(Opcodes.DUP);
//        Values.constantInt(i).load(mv);
//        pushVarArg(mv, argumentGenerators.get(fixedArgCount + i));
//        mv.visitInsn(Opcodes.AASTORE);
//      }
//    }
//    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()),
//        method.getName(), Type.getMethodDescriptor(method), false);
//  }
//
//
////
////  @Override
////  public void emitCallAndPopResult(MethodGenerator mv, List<ExprGenerator> argumentGenerators) {
////    emitCall(mv, argumentGenerators);
////    switch (Type.getReturnType(method).getSize()) {
////      case 0:
////        // NOOP
////        break;
////      case 1:
////        mv.visitInsn(Opcodes.POP);
////        break;
////      case 2:
////        mv.visitInsn(Opcodes.POP2);
////        break;
////    }
////  }
//
//  private void pushVarArg(MethodGenerator mv, ExprGenerator exprGenerator) {
////    GimpleType type = exprGenerator.getGimpleType();
////    if(type instanceof GimplePrimitiveType) {
////      exprGenerator.emitPushBoxedPrimitiveValue(mv);
////    } else if(type instanceof GimpleIndirectType) {
////      exprGenerator.emitPushPointerWrapper(mv);
////    } else {
////      throw new UnsupportedOperationException("type: " + type);
////    }
//    throw new UnsupportedOperationException();
//  }
//
//  private void checkArity(List<ExprGenerator> argumentGenerators) {
//    if(method.isVarArgs()) {
//      if(argumentGenerators.size() < getParamStrategies().size()) {
//        throw new InternalCompilerException(String.format(
//            "Arity mismatch: expected at least %d args to method %s.%s(), called with %d" ,
//            paramStrategies.size(),
//            method.getDeclaringClass().getName(),
//            method.getName(),
//            argumentGenerators.size()));
//      }  
//    } else {
//      if(argumentGenerators.size() != getParamStrategies().size()) {
//        throw new InternalCompilerException(String.format(
//            "Arity mismatch: expected %d args to method %s.%s(), called with %d" ,
//            paramStrategies.size(),
//            method.getDeclaringClass().getName(),
//            method.getName(),
//            argumentGenerators.size()));
//      }
//    }
//    
//  }
//
// 
//}
