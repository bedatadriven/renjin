package org.renjin.compiler;

import java.io.PrintWriter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRFunctionTable;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.sexp.SEXP;


public class ExpressionCompiler implements Opcodes {

  private SEXP exp;
  private ClassWriter cw;
  private ClassVisitor cv;
  private GenerationContext generationContext;
 

  public static Class<CompiledBody> compile(ThunkMap thunkMap, SEXP exp) {
    return new ExpressionCompiler(thunkMap).doCompile(exp);
  }
  
  public ExpressionCompiler(ThunkMap thunkMap) {
    super();
    String className = "Body" + System.identityHashCode(exp);
    this.generationContext = new GenerationContext(className,
        new FieldSexpPool(className),
        thunkMap);
  }

  private Class<CompiledBody> doCompile(SEXP exp) {
    this.exp = exp;
    startClass();
    writeImplementation();
    writeConstructor();
    generationContext.getSexpPool().writeFields(cv);
    writeClassEnd();
    
    return new MyClassLoader().defineClass(generationContext.getClassName().replace('/', '.'), cw.toByteArray());
  }

  private void startClass() {

    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    //cv = new CheckClassAdapter(cv);
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, generationContext.getClassName(), null,
        "java/lang/Object", new String[] { "org/renjin/compiler/CompiledBody" });
  }


  private void writeConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(8, l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    
    // initialize sexp pool
  
    generationContext.getSexpPool().writeConstructorBody(mv);
    
    mv.visitInsn(RETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "L" + generationContext.getClassName() + ";", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeImplementation() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "eval", "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;)Lorg/renjin/sexp/SEXP;", null, null);
    mv.visitCode();
    writeBody(mv);
//    mv.visitInsn(ACONST_NULL);
//    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
  
  private void writeBody(MethodVisitor mv) {
    IRFunctionTable functionTable = new IRFunctionTable();
    IRBodyBuilder builder = new IRBodyBuilder(functionTable);
    IRBody body = builder.build(exp);
    
    ByteCodeVisitor visitor = new ByteCodeVisitor(generationContext, mv);
    
    ControlFlowGraph cfg = new ControlFlowGraph(body);
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      
      System.out.println(bb.statementsToString());
      
      visitor.startBasicBlock(bb);
      
    //  List<Statement> statements = TreeBuilder.build(bb);
      for(Statement stmt : bb.getStatements()) {
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
