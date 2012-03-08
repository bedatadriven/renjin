package r.compiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import r.lang.BuiltinFunction;
import r.lang.ComplexVector;
import r.lang.DoubleVector;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.IntVector;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.SexpVisitor;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Vector;

public class ConstantGeneratingVisitor extends SexpVisitor<Void> implements Opcodes {
  private MethodVisitor mv;

  public ConstantGeneratingVisitor(MethodVisitor mv) {
    super();
    this.mv = mv;
  }

  @Override
  public void visit(DoubleVector vector) {
    
    if(vector.length() == 1) {
      mv.visitTypeInsn(NEW, "r/lang/DoubleVector");
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_1);
      mv.visitIntInsn(NEWARRAY, T_DOUBLE);
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_0);
      mv.visitLdcInsn(vector.getElementAsDouble(0));
      mv.visitInsn(DASTORE);
      mv.visitMethodInsn(INVOKESPECIAL, "r/lang/DoubleVector", "<init>", "([D)V");
    } else {
      throw new UnsupportedOperationException("only double vectors of length 1 are implemented");
    }
  }
   
  @Override
  public void visit(ComplexVector vector) {
    mv.visitTypeInsn(NEW, "r/lang/ComplexVector");
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

    mv.visitMethodInsn(INVOKESPECIAL, "r/lang/ComplexVector", "<init>", "([Lorg/apache/commons/math/complex/Complex;)V");

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
    mv.visitTypeInsn(NEW, "r/lang/IntVector");
    mv.visitInsn(DUP);
    
    pushIntArray(vector);

    mv.visitMethodInsn(INVOKESPECIAL, "r/lang/IntVector", "<init>", "([I)V");    
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
    mv.visitTypeInsn(NEW, "r/lang/StringVector");
    mv.visitInsn(DUP);
    
    ByteCodeUtil.pushInt(mv, vector.length());
    mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
    for(int i=0;i!=vector.length();++i) {
      if(!vector.isElementNA(i)) {
        mv.visitInsn(DUP);
        ByteCodeUtil.pushInt(mv, i);
        mv.visitLdcInsn(vector.getElement(i));
        mv.visitInsn(AASTORE);
      }
    }
    mv.visitMethodInsn(INVOKESPECIAL, "r/lang/StringVector", "<init>", "([Ljava/lang/String;)V");
  }

  @Override
  public void visit(Null nullExpression) {
    mv.visitFieldInsn(GETSTATIC, "r/lang/Null", "INSTANCE", "Lr/lang/Null;");
  }
  
  @Override
  public void visit(Symbol symbol) {
    if(symbol == Symbol.UNBOUND_VALUE) {
      mv.visitFieldInsn(GETSTATIC, "r/lang/Symbol", "UNBOUND_VALUE", "Lr/lang/Symbol;");
    } else if(symbol == Symbol.MISSING_ARG) {
      mv.visitFieldInsn(GETSTATIC, "r/lang/Symbol", "MISSING_ARG", "Lr/lang/Symbol;");
    } else {
      mv.visitLdcInsn(symbol.getPrintName());
      mv.visitMethodInsn(INVOKESTATIC, "r/lang/Symbol", "get", "(Ljava/lang/String;)Lr/lang/Symbol;");
    }
  }

  @Override
  public void visit(PairList.Node node) {
    mv.visitTypeInsn(NEW, "r/lang/PairList$Node");
    mv.visitInsn(DUP);
    
    node.getRawTag().accept(this);
    node.getValue().accept(this);
    node.getNext().accept(this);
    mv.visitMethodInsn(INVOKESPECIAL, "r/lang/PairList$Node", "<init>", "(Lr/lang/SEXP;Lr/lang/SEXP;Lr/lang/PairList;)V");
  }

  @Override
  public void visit(FunctionCall call) {
 
    mv.visitTypeInsn(NEW, "r/lang/FunctionCall");
    mv.visitInsn(DUP);
    call.getFunction().accept(this);
    call.getArguments().accept(this);
   // Null.INSTANCE.accept(this);
    mv.visitMethodInsn(INVOKESPECIAL, "r/lang/FunctionCall", "<init>", "(Lr/lang/SEXP;Lr/lang/PairList;)V");
  }
  
  @Override
  public void visit(BuiltinFunction builtin) {
    mv.visitLdcInsn(builtin.getName());
    mv.visitMethodInsn(INVOKESTATIC, "org/renjin/Primitives", "getBuiltin", "(Ljava/lang/String;)Lr/lang/SEXP;");
  }
 
  @Override
  public void visit(LogicalVector vector) {
    if(vector.length() == 1) {
      if(vector.getElementAsRawLogical(0) == 1) {
        mv.visitFieldInsn(GETSTATIC, "r/lang/LogicalVector", "TRUE", "Lr/lang/LogicalVector;");
      } else if(vector.getElementAsRawLogical(0) == 0) {
        mv.visitFieldInsn(GETSTATIC, "r/lang/LogicalVector", "FALSE", "Lr/lang/LogicalVector;");
      } else {        
        mv.visitFieldInsn(GETSTATIC, "r/lang/LogicalVector", "NA_VECTOR", "Lr/lang/LogicalVector;");
      }
    } else {
      mv.visitTypeInsn(NEW, "r/lang/LogicalVector");
      mv.visitInsn(DUP);
      
      pushIntArray(vector);

      mv.visitMethodInsn(INVOKESPECIAL, "r/lang/IntVector", "<init>", "([I)V");    
    }
  }

  @Override
  protected void unhandled(SEXP exp) {
    throw new UnsupportedOperationException("Constant generation nyi for " + exp);
  }
}
