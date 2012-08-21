package org.renjin.compiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.sexp.*;


public class ConstantGeneratingVisitor extends SexpVisitor<Void> implements Opcodes {
  private MethodVisitor mv;

  public ConstantGeneratingVisitor(MethodVisitor mv) {
    super();
    this.mv = mv;
  }

  @Override
  public void visit(DoubleVector vector) {
    
    if(vector.length() == 1) {
      mv.visitTypeInsn(NEW, "org/renjin/sexp/DoubleArrayVector");
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_1);
      mv.visitIntInsn(NEWARRAY, T_DOUBLE);
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_0);
      mv.visitLdcInsn(vector.getElementAsDouble(0));
      mv.visitInsn(DASTORE);
      mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/DoubleArrayVector", "<init>", "([D)V");
    } else {
      throw new UnsupportedOperationException("only double vectors of length 1 are implemented");
    }
  }
   
  @Override
  public void visit(ComplexVector vector) {
    mv.visitTypeInsn(NEW, "org/renjin/sexp/ComplexVector");
    mv.visitInsn(DUP);
    
    ByteCodeUtil.pushInt(mv, vector.length());
    mv.visitTypeInsn(ANEWARRAY, "org/apache/commons/math/complex/Complex");
    for(int i=0;i!=vector.length();++i) {
      mv.visitInsn(DUP);
      ByteCodeUtil.pushInt(mv, i);
      mv.visitTypeInsn(NEW, "org/apache/commons/math/complex/Complex");
      mv.visitInsn(DUP);
      pushDouble(vector.getElementAsComplex(i).getReal());
      pushDouble(vector.getElementAsComplex(i).getImaginary());
      mv.visitMethodInsn(INVOKESPECIAL, "org/apache/commons/math/complex/Complex", "<init>", "(DD)V");
      mv.visitInsn(AASTORE);
    }

    mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/ComplexVector", "<init>", "([Lorg/apache/commons/math/complex/Complex;)V");

  }

  private void pushDouble(double x) {
    if(x == 0) {
     mv.visitInsn(DCONST_0);
    } else if(x==1) {
      mv.visitInsn(DCONST_1);
    } else {
      mv.visitLdcInsn(Double.valueOf(x));
    }     
  }

  @Override
  public void visit(IntVector vector) {
    mv.visitTypeInsn(NEW, "org/renjin/sexp/IntArrayVector");
    mv.visitInsn(DUP);
    
    pushIntArray(vector);

    mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/IntArrayVector", "<init>", "([I)V");
  }

  private void pushIntArray(Vector vector) {
    ByteCodeUtil.pushInt(mv, vector.length());
    mv.visitIntInsn(NEWARRAY, T_INT);
    for(int i=0;i!=vector.length();++i) {
      mv.visitInsn(DUP);
      ByteCodeUtil.pushInt(mv, i);
      mv.visitLdcInsn(vector.getElementAsInt(i));
      mv.visitInsn(IASTORE);
    }
  }

  @Override
  public void visit(StringVector vector) {
    mv.visitTypeInsn(NEW, "org/renjin/sexp/StringArrayVector");
    mv.visitInsn(DUP);
    
    ByteCodeUtil.pushInt(mv, vector.length());
    mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
    for(int i=0;i!=vector.length();++i) {
      if(!vector.isElementNA(i)) {
        mv.visitInsn(DUP);
        ByteCodeUtil.pushInt(mv, i);
        mv.visitLdcInsn(vector.getElementAsString(i));
        mv.visitInsn(AASTORE);
      }
    }
    mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/StringArrayVector", "<init>", "([Ljava/lang/String;)V");
  }

  @Override
  public void visit(Null nullExpression) {
    mv.visitFieldInsn(GETSTATIC, "org/renjin/sexp/Null", "INSTANCE", "Lorg/renjin/sexp/Null;");
  }
  
  @Override
  public void visit(Symbol symbol) {
    if(symbol == Symbol.UNBOUND_VALUE) {
      mv.visitFieldInsn(GETSTATIC, "org/renjin/sexp/Symbol", "UNBOUND_VALUE", "Lorg/renjin/sexp/Symbol;");
    } else if(symbol == Symbol.MISSING_ARG) {
      mv.visitFieldInsn(GETSTATIC, "org/renjin/sexp/Symbol", "MISSING_ARG", "Lorg/renjin/sexp/Symbol;");
    } else {
      mv.visitLdcInsn(symbol.getPrintName());
      mv.visitMethodInsn(INVOKESTATIC, "org/renjin/sexp/Symbol", "get", "(Ljava/lang/String;)Lorg/renjin/sexp/Symbol;");
    }
  }

  @Override
  public void visit(PairList.Node node) {
    mv.visitTypeInsn(NEW, "org/renjin/sexp/PairList$Node");
    mv.visitInsn(DUP);
    
    node.getRawTag().accept(this);
    node.getValue().accept(this);
    node.getNext().accept(this);
    mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/PairList$Node", "<init>", "(Lorg/renjin/sexp/SEXP;Lorg/renjin/sexp/SEXP;Lorg/renjin/sexp/PairList;)V");
  }

  @Override
  public void visit(FunctionCall call) {
 
    mv.visitTypeInsn(NEW, "org/renjin/sexp/FunctionCall");
    mv.visitInsn(DUP);
    call.getFunction().accept(this);
    call.getArguments().accept(this);
   // Null.INSTANCE.accept(this);
    mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/FunctionCall", "<init>", "(Lorg/renjin/sexp/SEXP;Lorg/renjin/sexp/PairList;)V");
  }
  
  @Override
  public void visit(BuiltinFunction builtin) {
    mv.visitLdcInsn(builtin.getName());
    mv.visitMethodInsn(INVOKESTATIC, "org/renjin/Primitives", "getBuiltin", "(Ljava/lang/String;)Lorg/renjin/sexp/SEXP;");
  }
 
  @Override
  public void visit(LogicalVector vector) {
    if(vector.length() == 1) {
      if(vector.getElementAsRawLogical(0) == 1) {
        mv.visitFieldInsn(GETSTATIC, "org/renjin/sexp/LogicalVector", "TRUE", "Lorg/renjin/sexp/LogicalVector;");
      } else if(vector.getElementAsRawLogical(0) == 0) {
        mv.visitFieldInsn(GETSTATIC, "org/renjin/sexp/LogicalVector", "FALSE", "Lorg/renjin/sexp/LogicalVector;");
      } else {        
        mv.visitFieldInsn(GETSTATIC, "org/renjin/sexp/LogicalVector", "NA_VECTOR", "Lorg/renjin/sexp/LogicalVector;");
      }
    } else {
      mv.visitTypeInsn(NEW, "org/renjin/sexp/LogicalVector");
      mv.visitInsn(DUP);
      
      pushIntArray(vector);

      mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/IntArrayVector", "<init>", "([I)V");
    }
  }

  @Override
  protected void unhandled(SEXP exp) {
    throw new UnsupportedOperationException("Constant generation nyi for " + exp);
  }
}
