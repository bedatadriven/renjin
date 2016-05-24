package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.runtime.UnsatisfiedLinkException;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static org.objectweb.asm.Type.getMethodDescriptor;

/**
 * Throws a runtime exception.
 */
public class UnimplCallGenerator implements CallGenerator, MethodHandleGenerator {

  public static final Type HANDLE_TYPE = Type.getType(MethodHandle.class);
  private String functionName;

  public UnimplCallGenerator(String functionName) {
    this.functionName = functionName;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    Type exceptionType = Type.getType(UnsatisfiedLinkException.class);
    mv.anew(exceptionType);
    mv.dup();
    mv.aconst(functionName);
    mv.invokeconstructor(exceptionType, Type.getType(String.class));
    mv.athrow();
  }

  @Override
  public SimpleExpr getMethodHandle() {

    // Create a method handle that throws the UnsatisifiedLinkException.

    return new SimpleExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return HANDLE_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        //    MethodHandle methodHandle = MethodHandles.throwException(void.class, UnsatisfiedLinkException.class);
        //    methodHandle = MethodHandles.insertArguments(methodHandle, 0, functionName);
        //    return methodHandle;
        mv.aconst(Type.getType(UnsatisfiedLinkException.class));
        mv.invokestatic(MethodHandles.class, "throwException", getMethodDescriptor(HANDLE_TYPE, Type.getType(Class.class)));
        mv.iconst(0);
        mv.aconst(functionName);
        mv.invokestatic(MethodHandles.class, "insertArguments", getMethodDescriptor(HANDLE_TYPE, Type.INT_TYPE, Type.getType(Object.class)));
      }
    };
  }
}