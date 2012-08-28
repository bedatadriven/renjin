package org.renjin.compiler.pipeline.accessor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.objectweb.asm.*;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.primitives.vector.DeferredComputation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class ComputationAccessor extends Accessor {

  private DeferredComputation computation;
  private List<Accessor> operands = Lists.newArrayList();
  private Map<String, Accessor> fieldMap = Maps.newHashMap();

  public ComputationAccessor(DeferredNode node, InputGraph inputGraph) {
    super();
    this.computation = node.getComputation();
    for(DeferredNode operand : node.getOperands()) {
      operands.add(Accessors.create(operand, inputGraph));
    }
    visitComputationClass(new FieldMapper());
  }

  @Override
  public void init(ComputeMethod method) {
    for(Accessor operand : operands) {
      operand.init(method);
    }
  }

  @Override
  public void pushLength(ComputeMethod method) {
    transformMethod(method, "length", Lists.<Integer>newArrayList());
  }

  @Override
  public void pushDouble(ComputeMethod method) {
    throw new UnsupportedOperationException();
    //transformMethod(method, "getElementAsDouble", Lists.newArrayList(indexLocal));
  }

  private void transformMethod(ComputeMethod method, String methodToRewrite, ArrayList<Integer> parameters) {
    visitComputationClass(new Transformer(method, methodToRewrite, parameters));
  }

  private void visitComputationClass(ClassVisitor classVisitor) {
    try {
      ClassReader reader = new ClassReader(computation.getClass().getName());
      reader.accept(classVisitor, 0);
    } catch (IOException e) {
      throw new RuntimeException("Exception transforming class", e);
    }
  }

  private class FieldMapper extends ClassVisitor {
    public FieldMapper() {
      super(ASM4);
    }

    @Override
    public FieldVisitor visitField(int access, final String name, String desc, String signature, Object value) {
      return new FieldVisitor(ASM4) {
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
          if(desc.equals("Lorg/renjin/primitives/annotations/Operand;")) {
            fieldMap.put(name, operands.get(0));
          }
          return super.visitAnnotation(desc, visible);
        }
      };
    }


  }

  private class Transformer extends ClassVisitor {

    private ComputeMethod method;
    private String methodToRewrite;
    private ArrayList<Integer> parameters;

    private Transformer(ComputeMethod method, String methodToRewrite, ArrayList<Integer> parameters) {
      super(ASM4);
      this.method = method;
      this.methodToRewrite = methodToRewrite;
      this.parameters = parameters;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      if(name.endsWith(methodToRewrite)) {
        return new MethodRewriter(method, parameters);
      } else {
        return null;
      }
    }
  }

  private class MethodRewriter extends MethodVisitor {
    private ComputeMethod method;
    private ArrayList<Integer> parameters;
    private Map<Integer, Integer> localVariableMapping = Maps.newHashMap();

    public MethodRewriter(ComputeMethod method, ArrayList<Integer> parameters) {
      super(ASM4, method.getVisitor());
      this.method = method;
      this.parameters = parameters;
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      mv.visitVarInsn(opcode, mapLocalVariableIndex(var, opcode));
    }

    private int mapLocalVariableIndex(int var, int opcode) {
      if(var < parameters.size()) {
        return parameters.get(var);

      } else {
        Integer mappedLocal = localVariableMapping.get(var);
        if(mappedLocal == null) {
          mappedLocal = method.reserveLocal(localSize(opcode));
        }
        return mappedLocal;
      }
    }

    private int localSize(int opcode) {
      if(opcode == Opcodes.DLOAD || opcode == Opcodes.DSTORE) {
        return 2;
      } else {
        return 1;
      }
    }

    @Override
    public void visitInsn(int opcode) {
      switch(opcode) {
        case DRETURN:
        case IRETURN:
          // leave argument on stack
          break;
        default:
          mv.visitInsn(opcode);
      }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      if(!fieldMap.containsKey(name)) {
        throw new RuntimeException("Can't translate code because of field reference:" + name);
      }
    }
  }
}
