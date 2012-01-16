package r.compiler.ir;

import java.io.PrintWriter;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import r.compiler.CompiledBody;
import r.compiler.cfg.BasicBlock;
import r.compiler.cfg.ControlFlowGraph;
import r.compiler.ir.tac.IRBody;
import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.IRFunctionTable;
import r.compiler.ir.tac.statements.Statement;
import r.compiler.ir.tree.TreeBuilder;
import r.lang.SEXP;

public class ExpressionCompiler implements Opcodes {

  private SEXP exp;
  private String className;
  private ClassWriter cw;
  private ClassVisitor cv;

  public static Class<CompiledBody> compile(SEXP exp) {
    return new ExpressionCompiler().doCompile(exp);
  }

  private Class<CompiledBody> doCompile(SEXP exp) {
    this.exp = exp;
    startClass();
    writeConstructor();
    writeImplementation();
    writeClassEnd();

    return new MyClassLoader().defineClass(className.replace('/', '.'), cw.toByteArray());
  }

  private void startClass() {
    className = "Body" + System.identityHashCode(exp);

    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null,
        "java/lang/Object", new String[] { "r/compiler/CompiledBody" });
  }


  private void writeConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(8, l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mv.visitInsn(RETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeImplementation() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "eval", "(Lr/lang/Context;Lr/lang/Environment;)Lr/lang/SEXP;", null, null);
    mv.visitCode();
    writeBody(mv);
//    mv.visitInsn(ACONST_NULL);
//    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 3);
    mv.visitEnd();
  }

  private void writeBody(MethodVisitor mv) {
    IRFunctionTable functionTable = new IRFunctionTable();
    IRBodyBuilder builder = new IRBodyBuilder(functionTable);
    IRBody body = builder.build(exp);

    ByteCodeVisitor visitor = new ByteCodeVisitor(mv);
    
    
    ControlFlowGraph cfg = new ControlFlowGraph(body);
    for(BasicBlock bb : cfg.getBasicBlocks()) {

      List<Statement> statements = TreeBuilder.build(bb);
      for(Statement stmt : statements) {
        stmt.accept(visitor);
      }
    }
  }

  private void writeClassEnd() {
    cv.visitEnd();
  }

  class MyClassLoader extends ClassLoader {
    public Class defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }

}
