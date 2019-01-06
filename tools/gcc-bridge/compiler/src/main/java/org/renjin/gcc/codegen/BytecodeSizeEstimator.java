/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.codegen;

import org.renjin.repackaged.asm.Handle;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.tree.MethodNode;

/**
 * Created by alex on 18-11-16.
 */
public class BytecodeSizeEstimator extends MethodVisitor {

  private int bytes = 0;

  public BytecodeSizeEstimator(int api) {
    super(api);
  }

  @Override
  public void visitInsn(int opcode) {
    bytes++;
  }

  @Override
  public void visitIntInsn(int opcode, int operand) {
    bytes++; // 1 byte for the bytecode itself
    switch (opcode) {
      case Opcodes.SIPUSH:
        bytes += 2;
        break;
      default:
        bytes += 1;
        break;
    }
  }

  @Override
  public void visitVarInsn(int opcode, int var) {

    if(var <= 3) {
      // ILOAD_0, ILOAD_1, etc.
      bytes += 1;
    } else if(var < 0xFF) {
      // ILOAD
      // (index)
      bytes += 2;
    } else {
      // wide
      // ILOAD
      // indexbyte1
      // indexbyte2

      bytes += 4;
    }
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    // getfield|setfield|getstatic|putstatic
    // indexbyte1
    // indexbyte2
    bytes += 3;
  }

  @Override
  public void visitLdcInsn(Object cst) {
    // Size depends on the constant pool index...
    // ldc
    // index
    // (or)
    // ldc_w
    // indexbyte1
    // indexbyte2

    bytes += 2;
  }

  @Override
  public void visitIincInsn(int var, int increment) {
    if(var < 0xFF) {
      // iinc
      // index
      // const
      bytes += 3;
    } else {
      // wide
      // iinc
      // indexbyte1
      // indexbyte2
      // constbyte1
      // constbyte2
      bytes += 6;
    }
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    // invokespecial|invokestatic|etc
    // indexbyte1
    // indexbyte2

    bytes += 3;
  }

  @Override
  public void visitJumpInsn(int opcode, Label label) {
    // if<cond>
    // branchbyte1
    // branchbyte2
    bytes += 3;
  }

  @Override
  public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
    // indexbyte1
    // indexbyte2
    // 0
    // 0
    bytes += 4;
  }

  @Override
  public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
    // tableswitch
    // <0-3 byte pad>
    // defaultbyte1
    // defaultbyte2
    // defaultbyte3
    // defaultbyte4
    // lowbyte1
    // lowbyte2
    // lowbyte3
    // lowbyte4
    // highbyte1
    // highbyte2
    // highbyte3
    // highbyte4
    // jump offsets...
    bytes += 16 + (labels.length * 2);
  }


  @Override
  public void visitMultiANewArrayInsn(String desc, int dims) {
    // multianewarray
    // indexbyte1
    // indexbyte2
    // dimensions
    bytes += 4;
  }

  @Override
  public void visitTypeInsn(int opcode, String type) {
    // instanceof|anewarray|checkcast
    // indexbyte1
    // indexbyte2
    bytes += 3;
  }

  @Override
  public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    // lookupswitch
    // <0-3 byte pad>
    // defaultbyte1
    // defaultbyte2
    // defaultbyte3
    // defaultbyte4
    // npairs1
    // npairs2
    // npairs3
    // npairs4
    // match-offset pairs...
  }

  public static int estimateSize(MethodNode methodNode) {
    BytecodeSizeEstimator estimator = new BytecodeSizeEstimator(Opcodes.ASM5);
    methodNode.accept(estimator);
    return estimator.bytes;
  }
}
