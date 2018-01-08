/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Multimap;

import java.util.HashMap;
import java.util.Map;

public class BytecodeMap extends MethodVisitor {

  public static class Var {
    private String name;
    private String desc;
    private int index;

    public Var(String name, String desc, int index) {
      this.name = name;
      this.desc = desc;
      this.index = index;
    }

    public String getName() {
      return name;
    }

    public String getDesc() {
      return desc;
    }

    public int getIndex() {
      return index;
    }
  }

  private final Multimap<Label, Var> varMap = HashMultimap.create();
  private final Map<Label, Integer> lineMap = new HashMap<>();

  private final Map<Integer, Var> currentVars = new HashMap<>();
  private int currentLine = -1;

  public BytecodeMap() {
    super(Opcodes.ASM5);
  }

  @Override
  public void visitLineNumber(int line, Label start) {
    lineMap.put(start, line);
  }

  @Override
  public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
    varMap.put(start, new Var(name, desc, index));
  }

  public boolean isStarted() {
    return currentLine != -1;
  }

  public int getCurrentLine() {
    return currentLine;
  }

  public Var getCurrentVar(int index) {
    return currentVars.get(index);
  }

  public void onLabel(Label label) {
    Integer line = lineMap.get(label);
    if(line != null) {
      currentLine = line;
    }

    for (Var var : varMap.get(label)) {
      currentVars.put(var.index, var);
    }
  }
}
