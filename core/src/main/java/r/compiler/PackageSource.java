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

import r.lang.SEXP;
import r.parser.ParseUtil;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

class PackageSource {

  private Map<String, String> symbolSources = new HashMap<String, String>();

  public void addSymbol(String name, SEXP exp) {

    JavaSourceWritingVisitor visitor = new JavaSourceWritingVisitor();
    exp.accept(visitor);

    symbolSources.put(name, visitor.getBody());
  }

  public void writeTo(String packageName, String className, PrintStream writer) {
    writer.println(String.format("package %s;", packageName));
    writer.println();
    writer.println("import r.lang.*;");
    writer.println("import r.compiler.runtime.AbstractPackage;");
    writer.println();
    writer.println("import static r.lang.Logical.TRUE;");
    writer.println("import static r.lang.Logical.FALSE;");
    writer.println("import static r.lang.Logical.NA;");

    writer.println("import org.apache.commons.math.complex.Complex;");

    writer.println();
    writer.println("public class " + className + " extends AbstractPackage {");
    writer.println();
    writer.println("  public " + className + "(EnvExp enclosing) {");
    writer.println("    super(enclosing);");
    for(Map.Entry<String, String> symbol : symbolSources.entrySet()) {
      writer.println("    setVariable(symbols.install(" + ParseUtil.formatStringLiteral(symbol.getKey(), null) +
          "), " + symbol.getValue() + ");");
    }
    writer.println("  }");
    writer.println();
    writer.println("}");
  }





}
