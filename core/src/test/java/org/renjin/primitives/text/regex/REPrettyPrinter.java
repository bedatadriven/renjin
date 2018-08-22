/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.primitives.text.regex;

import org.renjin.repackaged.guava.base.Strings;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class REPrettyPrinter {

  private final Map<Character, String> opNames = new HashMap<>();

  public REPrettyPrinter() {
    for (Field field : ExtendedRE.class.getDeclaredFields()) {
      if(field.getName().startsWith("OP_")) {
        try {
          opNames.put((Character)field.get(null), field.getName());
        } catch (IllegalAccessException e) {
          throw new Error(e);
        }
      }
    }
  }

  public String prettyPrint(REProgram program) {
    return prettyPrint(program.instruction, program.lenInstruction);
  }

  private String prettyPrint(char[] instruction, int lastNode) {
    StringBuilder s = new StringBuilder();
    s.append("RE{\n");
    int node = 0;
    while (node < lastNode) {
      char opcode = instruction[node /* + offsetOpcode */];
      int next = node + (short) instruction[node + ExtendedRE.OFFSET_NEXT];
      int opdata = instruction[node + ExtendedRE.OFFSET_OPDATA];

      s.append(Strings.padStart(node + ": ", 5, ' '));
      s.append(opNames.get(opcode));

      switch (opcode) {

        case ExtendedRE.OP_ATOM:
          printStringOpdata(s, instruction, node + ExtendedRE.NODE_SIZE, opdata);
          break;

        default:
          if(opdata > 0) {
            s.append("[").append(opdata).append("]");
          }
      }
      s.append("\n");

      if(opcode == ExtendedRE.OP_END) {
        break;
      }

      node = next;
    }
    s.append("}\n");
    return s.toString();
  }

  private void printStringOpdata(StringBuilder s, char[] instruction, int start, int length) {
    s.append("[");
    for (int i = 0; i < length; i++) {
      s.append(instruction[start + i]);
    }
    s.append("]");
  }

}
