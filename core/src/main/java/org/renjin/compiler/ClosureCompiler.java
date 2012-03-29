package org.renjin.compiler;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRFunction;
import org.renjin.compiler.ir.tac.IRFunctionTable;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.sexp.Closure;


public class ClosureCompiler implements Opcodes {

  private IRFunction closure;
  private ClassWriter cw;
  private ClassVisitor cv;
  private GenerationContext generationContext;

  public static Class<Closure> compileAndLoad(IRFunction closure) {
    return new ClosureCompiler("Closure" + System.identityHashCode(closure))
      .doCompileAndLoad(closure);
  }
  
  public static Class<Closure> compileAndLoad(Closure closureSexp) {
    IRFunctionTable functionTable = new IRFunctionTable();
    IRBodyBuilder builder = new IRBodyBuilder(functionTable);
    return compileAndLoad(new IRFunction(closureSexp.getFormals(), closureSexp.getBody(), builder.build(closureSexp.getBody())));
  }
  
  public static byte[] compile(String className, IRFunction closure) {
    return new ClosureCompiler(className)
    .doCompile(closure);    
  }
  
  public ClosureCompiler(String className) {
    super();
    
    this.generationContext = new GenerationContext(className,
        new FieldSexpPool(className),
        new ThunkMap(className + "$thunk$"));
  }

  public byte[] doCompile(IRFunction closure) {
    this.closure = closure;
    startClass();
    writeDoEval();
    writeConstructor();
    writeFormals();
    writeBodySexp();
    generationContext.getSexpPool().writeFields(cv);
    writeClassEnd();
    return cw.toByteArray();
  }

  public Class<Closure> doCompileAndLoad(IRFunction closure) {
    return new MyClassLoader().defineClass(generationContext.getClassName().replace('/', '.'), doCompile(closure)); 
  }
  
  private void startClass() {
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {

      @Override
      protected String getCommonSuperClass(String className1, String className2) {
        return super.getCommonSuperClass(
            resolveForwardReference(className1),
            resolveForwardReference(className2));
      }
      
    };
    cv = cw;
    //cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
  //  cv = new CheckClassAdapter(cv);
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, generationContext.getClassName(), null, "org/renjin/sexp/Closure", null);

  }
  
  private String resolveForwardReference(String className) {
    if(className.contains("$closure$")) {
      return "org/renjin/sexp/Closure";
    } else {
      return className;
    }
  }

  private void writeConstructor() {

    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "(Lorg/renjin/sexp/Environment;)V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKESTATIC, generationContext.getClassName(), "createFormals", "()Lorg/renjin/sexp/PairList;");
    mv.visitMethodInsn(INVOKESTATIC, generationContext.getClassName(), "createBody", "()Lorg/renjin/sexp/SEXP;");
    mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/Closure", "<init>", "(Lorg/renjin/sexp/Environment;Lorg/renjin/sexp/PairList;Lorg/renjin/sexp/SEXP;)V");
      
    generationContext.getSexpPool().writeConstructorBody(mv);
    
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeDoEval() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "doApply", 
        "(Lorg/renjin/eval/Context;)Lorg/renjin/sexp/SEXP;", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/eval/Context", "getEnvironment", "()Lorg/renjin/sexp/Environment;");
    mv.visitVarInsn(ASTORE, 2);

    writeDoEvalBody(mv);
    
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
  
  private void writeFormals() {
  
    MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, 
        "createFormals", "()Lorg/renjin/sexp/PairList;", null, null);
    mv.visitCode();
    
    ConstantGeneratingVisitor cgv = new ConstantGeneratingVisitor(mv);
    closure.getFormals().accept(cgv);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 0);
    mv.visitEnd();
  }

  private void writeBodySexp() {

    MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, 
        "createBody", "()Lorg/renjin/sexp/SEXP;", null, null);
    mv.visitCode();
    ConstantGeneratingVisitor cgv = new ConstantGeneratingVisitor(mv);
    closure.getBodyExpression().accept(cgv);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 0);
    mv.visitEnd();
 }
 
  private void writeDoEvalBody(MethodVisitor mv) {
 
    ByteCodeVisitor visitor = new ByteCodeVisitor(generationContext, mv);
    
    
    ControlFlowGraph cfg = new ControlFlowGraph(closure.getBody());
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

  public List<Entry<String, IRFunction>> getNestedClosures() {
    return generationContext.getNestedClosures();
  }

  public ThunkMap getThunkMap() {
    return generationContext.getThunkMap();
  }
}
