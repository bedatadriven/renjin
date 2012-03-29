package org.renjin.compiler;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.renjin.sexp.FunctionCall;


/**
 * Support classes which makes available SEXP literals 
 * to compiled java classs.
 */
public interface SexpPool {

  void writeFields(ClassVisitor cv);
  void writeConstructorBody(MethodVisitor mv);
  void writeStaticInitializerBody(MethodVisitor mv);

  void pushSexp(MethodVisitor mv, FunctionCall call, String string);

}