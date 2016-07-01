package org.renjin.compiler.codegen;


import org.objectweb.asm.*;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.CompiledLoopBody;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

import java.io.PrintWriter;

import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

public class ByteCodeEmitter implements Opcodes {

  private ClassWriter cw;
  private ClassVisitor cv;
  private ControlFlowGraph cfg;
  private String className;

  private TypeSolver types;


  public ByteCodeEmitter(ControlFlowGraph cfg, TypeSolver types) {
    super();
    this.cfg = cfg;
    this.types = types;
    this.className = "Body" + System.identityHashCode(cfg);
  }

  public Class<CompiledBody> compile() {
    startClass(CompiledBody.class);
    writeImplementation();
    writeConstructor();
    writeClassEnd();

    return new MyClassLoader().defineClass(className.replace('/', '.'), cw.toByteArray());
  }

  public Class<CompiledLoopBody> compileLoopBody() {
    startClass(CompiledLoopBody.class);
    writeLoopImplementation();
    writeConstructor();
    writeClassEnd();

    return new MyClassLoader().defineClass(className.replace('/', '.'), cw.toByteArray());
  }
  
  private void startClass(Class<?> interfaceClass) {

//    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    cw = new ClassWriter(0);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out, true));
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null,
            Type.getInternalName(Object.class), new String[] { Type.getInternalName(interfaceClass) });
  }


  private void writeConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(2, 1);
    mv.visitEnd();
  }


  private void writeImplementation() {
    int argumentSize = 3; // this + context + environment
    VariableSlots variableSlots = new VariableSlots(argumentSize, types);
    System.out.println(variableSlots);
    EmitContext emitContext = new EmitContext(cfg, argumentSize, variableSlots);
    
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "evaluate", 
        getMethodDescriptor(Type.VOID_TYPE, getType(Context.class), getType(Environment.class)), 
        null, null);
    mv.visitCode();
    writeBody(emitContext, mv);
    mv.visitEnd();
  }

  private void writeLoopImplementation() {
    int argumentSize = 5; // this + context + environment + sequence + iteration
    VariableSlots variableSlots = new VariableSlots(argumentSize, types);
    System.out.println(variableSlots);
    EmitContext emitContext = new EmitContext(cfg, argumentSize, variableSlots);
    emitContext.setLoopVectorIndex(3);
    emitContext.setLoopIterationIndex(4);


    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "run",
        getMethodDescriptor(Type.VOID_TYPE, getType(Context.class), getType(Environment.class),
        getType(SEXP.class), Type.INT_TYPE),
        null, null);
    
    mv.visitCode();
    
    writeBody(emitContext, mv);
    mv.visitEnd();

  }

  private void writeBody(EmitContext emitContext, MethodVisitor mv) {
    InstructionAdapter instructionAdapter = new InstructionAdapter(mv);

    for(BasicBlock basicBlock : cfg.getBasicBlocks()) {
      if(basicBlock != cfg.getEntry() && basicBlock != cfg.getExit()) {
        for(IRLabel label : basicBlock.getLabels()) {
          mv.visitLabel(emitContext.getAsmLabel(label));
        }

        for(Statement stmt : basicBlock.getStatements()) {
          try {
            stmt.emit(emitContext, instructionAdapter);
          } catch (Exception e) {
            throw new InternalCompilerException("Exception compiling statement " + stmt, e);
          }
        }
      }
    }

    mv.visitMaxs(100, emitContext.getLocalVariableCount());
  }

  private void writeClassEnd() {
    cv.visitEnd();
  }

  private class MyClassLoader extends ClassLoader {
    Class defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }
}
