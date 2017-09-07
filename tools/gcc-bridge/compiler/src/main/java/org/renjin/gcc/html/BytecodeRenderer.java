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
package org.renjin.gcc.html;

import org.renjin.repackaged.asm.*;
import org.renjin.repackaged.asm.tree.AbstractInsnNode;
import org.renjin.repackaged.asm.tree.MethodNode;
import org.renjin.repackaged.asm.util.Printer;
import org.renjin.repackaged.guava.escape.Escaper;
import org.renjin.repackaged.guava.html.HtmlEscapers;

import java.util.HashMap;
import java.util.Map;

public class BytecodeRenderer {

  public static final Escaper ESCAPER = HtmlEscapers.htmlEscaper();
  private StringBuilder html = new StringBuilder();
  private MethodNode methodNode;

  private final BytecodeMap bytecodeMap = new BytecodeMap();
  private final Map<Label, Integer> labelMap = new HashMap<>();

  public BytecodeRenderer(MethodNode methodNode) {
    this.methodNode = methodNode;
    this.methodNode.accept(bytecodeMap);
  }

  private void startIns() {
    html.append("<div class=\"bcins");
    if(bytecodeMap.isStarted()) {
      html.append(" SL SL").append(bytecodeMap.getCurrentLine());
    }
    html.append("\">");
  }

  private void opcode(int opcode) {
    opcode(Printer.OPCODES[opcode]);
  }

  private void opcode(String opcode) {
    html.append(String.format("<span class=\"opcode\">%s</span>", opcode));
  }

  private void operand(int operand) {
    html.append(" ");
    html.append("<span class=\"bcldc\">");
    html.append(operand);
    html.append("</span>");
  }

  private void variable(int index) {
    html.append(" ");
    html.append("<span class=\"bcvar\">");
    BytecodeMap.Var currentVar = bytecodeMap.getCurrentVar(index);
    if(currentVar == null) {
      html.append(index);
    } else {
      html.append(ESCAPER.escape(currentVar.getName()));
    }
    html.append("</span>");

    if(currentVar != null) {
      html.append(" ");
      description(currentVar.getDesc());
    }
  }

  private void label(Label label) {
    html.append(" ");
    html.append(String.format("<span class=\"bclabel\">%s</span>", friendly(label)));

  }

  private String friendly(Label label) {
    Integer number = labelMap.get(label);
    if(number == null) {
      number = labelMap.size() + 1;
      labelMap.put(label, number);
    }
    return "L" + number;
  }

  private void typeOperand(String type) {
    html.append(" ");
    type(type);
  }

  private void type(String type) {
    String shortName = shortNameOfType(type);

    html.append(String.format("<span class=\"bctype\" title=\"%s\">%s</span>",
        ESCAPER.escape(type),
        ESCAPER.escape(shortName)));
  }

  private void type(Type type) {
    if(type.getSort() == Type.OBJECT) {
      type(type.getInternalName());
    } else if(type.getSort() == Type.ARRAY) {
      html.append("[");
      type(type.getElementType());
    } else {
      html.append(type.toString());
    }
  }

  private String shortNameOfType(String type) {
    int lastSlash = type.lastIndexOf('/');
    return type.substring(lastSlash + 1);
  }


  private void signature(String owner, String name, String desc) {
    html.append(" ");
    typeOperand(owner);
    html.append(String.format(".<span class\"bcmember\">%s</span>", ESCAPER.escape(name)));
    description(desc);
  }

  private void description(String desc) {
    int i = 0;

    while(i < desc.length()) {
      if(desc.charAt(i) == 'L') {
        int endOfType = desc.indexOf(';', i);
        type(desc.substring(i+1, endOfType));
        i = endOfType + 1;
      } else {
        html.append(desc.charAt(i));
        i++;
      }
    }
  }

  private void endIns() {
    html.append("</div>");
  }

  public String render() {

    html.append("<div class=\"bcmethod\">");
    type(Type.getReturnType(methodNode.desc));
    html.append(" ");
    html.append(methodNode.name);
    html.append("(");
    Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
    for (int i = 0; i < argumentTypes.length; i++) {
      if(i > 0) {
        html.append(", ");
      }
      type(argumentTypes[i]);
    }
    html.append(")");
    html.append("</div>");

    for (int i = 0; i < methodNode.instructions.size(); i++) {
      final AbstractInsnNode node = methodNode.instructions.get(i);
      node.accept(new MethodVisitor(Opcodes.ASM5) {
        @Override
        public void visitParameter(String name, int access) {
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
          return null;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
          return null;
        }

        @Override
        public void visitAttribute(Attribute attr) {
        }

        @Override
        public void visitCode() {
          super.visitCode();
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        }

        @Override
        public void visitInsn(int opcode) {
          startIns();
          opcode(opcode);
          endIns();
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
          startIns();
          opcode(opcode);
          operand(operand);
          endIns();
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
          startIns();
          opcode(opcode);
          variable(var);
          endIns();
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
          startIns();
          opcode(opcode);
          typeOperand(type);
          endIns();
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
          startIns();
          opcode(opcode);
          signature(owner, name, desc);
          endIns();
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
          startIns();
          opcode(opcode);
          signature(owner, name, desc);
          endIns();
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
          startIns();
          opcode(opcode);
          signature(owner, name, desc);
          endIns();
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
          startIns();
          opcode("INVOKEDYN");
          endIns();

        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
          startIns();
          opcode(opcode);
          label(label);
          endIns();
        }

        @Override
        public void visitLabel(Label label) {
          bytecodeMap.onLabel(label);
          html.append(String.format("<div class=\"bclabel\">%s:</div>", friendly(label)));
        }

        @Override
        public void visitLdcInsn(Object cst) {
          startIns();
          opcode("LDC");
          html.append(" ");
          html.append("<span class=\"bcldc\">");
          if(cst instanceof String) {
            html.append(ESCAPER.escape(GimpleRenderer.stringLiteral((String) cst)));
          } else {
            html.append(cst.toString());
          }
          html.append("</span>");
          endIns();
        }

        @Override
        public void visitIincInsn(int var, int increment) {
          startIns();
          opcode("IINC");
          variable(var);
          operand(increment);
          endIns();
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
          startIns();
          opcode("TABLESWITCH");
          endIns();
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
          startIns();
          opcode("LOOKUPSWITCH");
          endIns();
        }

        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
          startIns();
          opcode("ANEWMULTI");
          typeOperand(desc);
          operand(dims);
          endIns();
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
          return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
          return null;
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
          return null;
        }

        @Override
        public void visitLineNumber(int line, Label start) {
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
        }

        @Override
        public void visitEnd() {
        }
      });
    }
    return html.toString();
  }


}
