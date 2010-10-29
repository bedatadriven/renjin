/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.compiler;

import r.lang.PrimitiveSexp;
import r.lang.SEXP;
import r.lang.SymbolExp;
import r.parser.ParseUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Maintains a mapping between R symbol names and
 * Java symbol names used in code generation.
 */
class SymbolMap {
  private Map<SymbolExp, String> symbolNames = new HashMap<SymbolExp, String>();
  private HashSet<String> reservedWords = createReservedWordSet();



  public SymbolMap() {
  }

  /**
   * Returns or creates a unique, valid Java symbol for a given
   * R symbol
   */
  public String getSymbolName(SymbolExp symbolExp) {
    String name = symbolNames.get(symbolExp);
    if (name == null) {
      name = makeUniqueJavaName(symbolExp);
      symbolNames.put(symbolExp, name);
    }
    return name;
  }

  private String makeUniqueJavaName(SymbolExp symbolExp) {
    String javaSymbol = makeSymbolName(symbolExp);
    if (symbolNames.containsValue(javaSymbol)) {
      int index = 2;
      while (symbolNames.containsValue(javaSymbol + index)) {
        index++;
      }
      javaSymbol = javaSymbol + index;
    }
    return javaSymbol;
  }

  private String makeSymbolName(SymbolExp symbol) {
    if (symbol.getValue() instanceof PrimitiveSexp) {
      return javaNameFromPrimitive(symbol, symbol.getValue());

    } else if (symbol.getInternal() instanceof PrimitiveSexp) {
      return javaNameFromPrimitive(symbol, symbol.getInternal());

    } else {
      return javaNameFromRName(symbol);
    }
  }

  private String javaNameFromPrimitive(SymbolExp symbol, SEXP value) {
    Class fnClass = ((PrimitiveSexp) value).getFunctionClass();
    // unimplemented functions have a null class
    if (fnClass == null) {
      return javaNameFromRName(symbol);
    }
    if (fnClass.getEnclosingClass() == null) {
      return lowerFirst(fnClass.getSimpleName());
    } else {
      return lowerFirst(fnClass.getEnclosingClass().getSimpleName()) +
          fnClass.getSimpleName();
    }
  }

  private String lowerFirst(String name) {
    return Character.toLowerCase((char) name.codePointAt(0)) +
        name.substring(1);
  }

  private String javaNameFromRName(SymbolExp symbol) {
    StringBuilder javaNameBuilder = new StringBuilder();
    String rName = symbol.getPrintName();

    int firstChar = rName.codePointAt(0);
    if (!Character.isJavaIdentifierStart(firstChar)) {
      javaNameBuilder.append('_');
    }
    if (Character.isJavaIdentifierPart(firstChar)) {
      javaNameBuilder.appendCodePoint(Character.toLowerCase(firstChar));
    }
    for (int i = 1; i < rName.length(); ++i) {
      if (Character.isJavaIdentifierPart(rName.codePointAt(i))) {
        javaNameBuilder.appendCodePoint(rName.codePointAt(i));
      } else {
        javaNameBuilder.append('_');
      }
    }
    String javaName = javaNameBuilder.toString();
    if(reservedWords.contains(javaName)) {
      return "_" + javaName;
    } else {
      return javaName;
    }
  }

  public String getSymbolDefinitions() {
    StringBuilder defs = new StringBuilder();

    defs.append("    SymbolTable symbolTable = rho.getGlobalContext().getSymbolTable();\n");
    for (Map.Entry<SymbolExp, String> entry : symbolNames.entrySet()) {
      defs.append("    SymbolExp ").append(entry.getValue())
          .append(" = symbolTable.install(\"");
      ParseUtil.appendEscaped(defs, entry.getKey().getPrintName());
      defs.append("\");\n");
    }
    return defs.toString();
  }

  private HashSet<String> createReservedWordSet() {
    return new HashSet<String>(Arrays.asList(

      /* java reserved words */
      "abstract",
      "continue",
      "for",
      "new",
      "switch",
      "assert",
      "default",
      "goto",
      "package",
      "synchronized",
      "boolean",
      "do",
      "if",
      "private",
      "this",
      "break",
      "double",
      "implements",
      "protected",
      "throw",
      "byte",
      "else",
      "import",
      "public",
      "throws",
      "case",
      "enum",
      "instanceof",
      "return",
      "transient",
      "catch",
      "extends",
      "int",
      "short",
      "try",
      "char",
      "final",
      "interface",
      "return",
      "transient",
      "catch",
      "extends",
      "int",
      "short",
      "try",
      "char",
      "final",
      "interface",
      "static",
      "void",
      "class",
      "finally",
      "long",
      "strictfp",
      "volatile",
      "const",
      "float",
      "native",
      "super",
      "while",

      /* Literals that can't be used as identifiers */
      "true",
      "false",
      "null",

      /* Identifiers that we define ourselves */
      "symbolTable",
      "rho",
      "c",
      "c_int"

    ));
  }
}